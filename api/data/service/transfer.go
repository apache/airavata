/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
 */

package service

import (
	"context"
	"errors"
	"fmt"
	"io"
	"net/http"
	"os"
	"path"
	"path/filepath"

	"gorm.io/gorm"

	"github.com/apache/airavata/internal/httpx"

	credmodel "github.com/apache/airavata/api/credentials/model"
	dto "github.com/apache/airavata/api/data/dto"
	model "github.com/apache/airavata/api/data/model"
	"github.com/apache/airavata/api/data/repository"
	iamrepo "github.com/apache/airavata/api/iam/repository"
	sharingmodel "github.com/apache/airavata/api/sharing/model"
	sharingrepo "github.com/apache/airavata/api/sharing/repository"
	sharingsvc "github.com/apache/airavata/api/sharing/service"
)

// Endpoint is the host and account one transfer runs against.
type Endpoint struct {
	Host      string
	Port      int
	LoginUser string
	Key       credmodel.SSHCredential
}

// Transport moves a single file between this server and the host an Endpoint names.
type Transport interface {
	Download(ctx context.Context, ep Endpoint, remotePath, localPath string) error
	Upload(ctx context.Context, ep Endpoint, localPath, remotePath string) error
	MakeDirectory(ctx context.Context, ep Endpoint, remoteDir string) error
}

// Download is the handle a caller streams a product's bytes from.
type Download struct {
	FileName string
	Size     int64

	io.ReadCloser
}

type stagedFile struct {
	*os.File
	dir string
}

func (f *stagedFile) Close() error {
	err := f.File.Close()
	if rmErr := os.RemoveAll(f.dir); err == nil {
		err = rmErr
	}
	return err
}

type DataTransferService struct {
	productAccess
	db        *gorm.DB
	storages  *repository.SCPDataStorageRepository
	transport Transport
}

// NewDataTransferService returns a transfer service moving bytes through transport.
func NewDataTransferService(
	db *gorm.DB,
	products *repository.DataProductRepository,
	sharing *sharingrepo.Repository,
	storages *repository.SCPDataStorageRepository,
	members *iamrepo.GroupMemberRepository,
	transport Transport,
) *DataTransferService {
	return &DataTransferService{
		productAccess: productAccess{
			access:   sharingsvc.NewAccess(members),
			products: products,
			sharing:  sharing,
		},
		db:        db,
		storages:  storages,
		transport: transport,
	}
}

// Download stages the product's remote file locally and returns a handle to it.
func (s *DataTransferService) Download(ctx context.Context, id string) (*Download, error) {
	product, err := s.requireProduct(ctx, id)
	if err != nil {
		return nil, err
	}
	if _, _, err := s.require(ctx, product, sharingmodel.AccessPermissionRead); err != nil {
		return nil, err
	}
	remotePath, ep, err := s.locate(ctx, product)
	if err != nil {
		return nil, err
	}

	dir, err := os.MkdirTemp("", "airavata-download-")
	if err != nil {
		return nil, fmt.Errorf("creating staging directory for data product %s: %w", product.ID, err)
	}
	// Everything from here to the handed-back handle has to clean the directory up
	// itself: nothing else knows about it yet.
	staged := filepath.Join(dir, "content")

	if err := s.transport.Download(ctx, ep, remotePath, staged); err != nil {
		os.RemoveAll(dir)
		return nil, upstream(err, "Could not read data product %s from %s", product.ID, ep.Host)
	}

	file, err := os.Open(staged)
	if err != nil {
		os.RemoveAll(dir)
		return nil, fmt.Errorf("opening staged copy of data product %s: %w", product.ID, err)
	}
	info, err := file.Stat()
	if err != nil {
		file.Close()
		os.RemoveAll(dir)
		return nil, fmt.Errorf("sizing staged copy of data product %s: %w", product.ID, err)
	}

	return &Download{
		FileName:   path.Base(remotePath),
		Size:       info.Size(),
		ReadCloser: &stagedFile{File: file, dir: dir},
	}, nil
}

// Upload writes content to the product's remote path, replacing whatever was there.
func (s *DataTransferService) Upload(ctx context.Context, id string, content io.Reader) (*dto.DataProductResponse, error) {
	product, err := s.requireProduct(ctx, id)
	if err != nil {
		return nil, err
	}
	held, _, err := s.require(ctx, product, sharingmodel.AccessPermissionWrite)
	if err != nil {
		return nil, err
	}
	remotePath, ep, err := s.locate(ctx, product)
	if err != nil {
		return nil, err
	}

	staged, cleanup, err := stage(content, product.ID)
	if err != nil {
		return nil, err
	}
	defer cleanup()

	// The parent directory is created first for the same reason a staging task creates
	// it: a product may name a path under a directory that no run has made yet, and
	// scp will not make it.
	if remoteDir := path.Dir(remotePath); remoteDir != "." && remoteDir != "/" {
		if err := s.transport.MakeDirectory(ctx, ep, remoteDir); err != nil {
			return nil, upstream(err, "Could not create %s on %s for data product %s", remoteDir, ep.Host, product.ID)
		}
	}
	if err := s.transport.Upload(ctx, ep, staged, remotePath); err != nil {
		return nil, upstream(err, "Could not write data product %s to %s", product.ID, ep.Host)
	}

	provisioned := model.ProvisionStatusProvisioned
	product.ProvisionStatus = &provisioned
	if err := s.products.Save(ctx, product); err != nil {
		return nil, err
	}

	out := dto.ToDataProductResponseWith(product, string(held))
	return &out, nil
}

// stage writes content to a temporary file and returns its path with the function that
// removes it.
func stage(content io.Reader, productID string) (string, func(), error) {
	file, err := os.CreateTemp("", "airavata-upload-")
	if err != nil {
		return "", nil, fmt.Errorf("creating staging file for data product %s: %w", productID, err)
	}
	name := file.Name()
	cleanup := func() {
		file.Close()
		os.Remove(name)
	}

	if _, err := io.Copy(file, content); err != nil {
		cleanup()
		var tooLarge *http.MaxBytesError
		if errors.As(err, &tooLarge) {
			return "", nil, &httpx.Error{
				Status:  http.StatusRequestEntityTooLarge,
				Message: fmt.Sprintf("Uploaded content exceeds the %d byte limit", tooLarge.Limit),
				Err:     err,
			}
		}
		return "", nil, httpx.BadRequest("Could not read the uploaded content: %v", err)
	}
	if err := file.Close(); err != nil {
		cleanup()
		return "", nil, fmt.Errorf("closing staging file for data product %s: %w", productID, err)
	}
	return name, cleanup, nil
}

func (s *DataTransferService) locate(ctx context.Context, product *model.DataProduct) (string, Endpoint, error) {
	var ep Endpoint

	if !product.IsFile {
		return "", ep, httpx.BadRequest(
			"Data product %s is a directory: only a file data product can be uploaded or downloaded", product.ID)
	}
	if product.Path == nil || *product.Path == "" {
		return "", ep, httpx.Conflict("Data product %s names no path to transfer", product.ID)
	}
	if product.DataStorageType != model.DataStorageTypeSCP {
		return "", ep, httpx.BadRequest(
			"Data product %s lives on %s storage, which cannot be transferred through this API",
			product.ID, product.DataStorageType)
	}
	if product.DataStorageID == nil {
		return "", ep, httpx.Conflict("Data product %s names no storage to transfer through", product.ID)
	}

	storage, err := s.storages.FindByID(ctx, *product.DataStorageID)
	if err != nil {
		return "", ep, notFoundAs(err, "SCP data storage not found: %s", *product.DataStorageID)
	}
	if storage.HostName == nil || storage.LoginUser == nil || storage.SSHCredential == nil {
		return "", ep, httpx.Conflict(
			"SCP data storage %s is missing the host, login user or key a transfer needs", storage.ID)
	}

	port := 22
	if storage.Port != nil && *storage.Port != 0 {
		port = *storage.Port
	}
	return *product.Path, Endpoint{
		Host:      *storage.HostName,
		Port:      port,
		LoginUser: *storage.LoginUser,
		Key:       *storage.SSHCredential,
	}, nil
}

// upstream reports a failure of the host a transfer ran against rather than of the
// request. The cause is wrapped rather than written into the message: an SSH failure
// names internal state, and the handler logs anything at 500 or above.
func upstream(err error, format string, args ...any) *httpx.Error {
	return &httpx.Error{
		Status:  http.StatusBadGateway,
		Message: fmt.Sprintf(format, args...),
		Err:     err,
	}
}
