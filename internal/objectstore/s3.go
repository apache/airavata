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

package objectstore

import (
	"context"
	"errors"
	"fmt"
	"io"
	"net/http"
	"net/url"
	"os"

	"github.com/minio/minio-go/v7"
	"github.com/minio/minio-go/v7/pkg/credentials"
)

// awsEndpoint is used when a Config names no endpoint.
const awsEndpoint = "https://s3.amazonaws.com"

// noSuchKey is the S3 error code for a missing object.
const noSuchKey = "NoSuchKey"

// Config says which bucket to open and how. SecretAccessKey and SessionToken are
// secrets: a Config must not be logged, and is not retained once New returns.
type Config struct {
	// EndpointURL is the service to talk to, such as https://minio.example.edu:9000.
	// Empty means AWS S3.
	EndpointURL string
	Region      string
	Bucket      string

	// PathStyle addresses the bucket as endpoint/bucket rather than bucket.endpoint.
	PathStyle bool

	AccessKeyID     string
	SecretAccessKey string
	SessionToken    string
}

// S3Store is a Store backed by any S3-compatible service. TLS certificates are always
// verified on an https endpoint.
type S3Store struct {
	client *minio.Client
	bucket string
}

var _ Store = (*S3Store)(nil)

// New returns a store for the bucket cfg names. It makes no network call, so a store
// that cannot be reached fails on first use rather than here.
func New(cfg Config) (*S3Store, error) {
	return newStore(cfg, nil)
}

// newStore is New with the HTTP transport replaceable. Nil is the client's default,
// which verifies certificates; only a test passes anything else.
func newStore(cfg Config, transport http.RoundTripper) (*S3Store, error) {
	if cfg.Bucket == "" {
		return nil, errors.New("object store config names no bucket")
	}
	if cfg.AccessKeyID == "" || cfg.SecretAccessKey == "" {
		return nil, errors.New("object store config is missing an access key or secret")
	}
	host, secure, err := parseEndpoint(cfg.EndpointURL)
	if err != nil {
		return nil, err
	}

	client, err := minio.New(host, &minio.Options{
		Creds:        credentials.NewStaticV4(cfg.AccessKeyID, cfg.SecretAccessKey, cfg.SessionToken),
		Secure:       secure,
		Transport:    transport,
		Region:       cfg.Region,
		BucketLookup: bucketLookup(cfg.PathStyle),
	})
	if err != nil {
		return nil, fmt.Errorf("creating object store client for %s: %w", host, err)
	}
	return &S3Store{client: client, bucket: cfg.Bucket}, nil
}

// parseEndpoint splits an endpoint URL into the host:port the client wants and whether
// it uses TLS.
func parseEndpoint(raw string) (host string, secure bool, err error) {
	if raw == "" {
		raw = awsEndpoint
	}
	u, err := url.Parse(raw)
	if err != nil {
		return "", false, fmt.Errorf("parsing object store endpoint: %w", err)
	}
	if (u.Scheme != "http" && u.Scheme != "https") || u.Host == "" {
		return "", false, fmt.Errorf("object store endpoint %q must be an http or https URL", raw)
	}
	return u.Host, u.Scheme == "https", nil
}

func bucketLookup(pathStyle bool) minio.BucketLookupType {
	if pathStyle {
		return minio.BucketLookupPath
	}
	return minio.BucketLookupDNS
}

// Stat implements Store.
func (s *S3Store) Stat(ctx context.Context, key string) (*ObjectInfo, error) {
	info, err := s.client.StatObject(ctx, s.bucket, key, minio.StatObjectOptions{})
	if err != nil {
		return nil, s.wrap("reading metadata of", key, err)
	}
	out := toObjectInfo(info)
	return &out, nil
}

// Download implements Store.
//
// The object is streamed rather than held in memory. It is not left to FGetObject,
// which keeps a partial file beside localPath when a transfer fails, where nothing
// would clean it up. The file is private to this user because the data may be.
func (s *S3Store) Download(ctx context.Context, key, localPath string) (err error) {
	object, err := s.client.GetObject(ctx, s.bucket, key, minio.GetObjectOptions{})
	if err != nil {
		return s.wrap("reading", key, err)
	}
	defer object.Close()

	// GetObject is lazy. Asking for the size surfaces a missing object or a refused
	// request before there is a file to clean up.
	if _, err := object.Stat(); err != nil {
		return s.wrap("reading", key, err)
	}

	file, err := os.OpenFile(localPath, os.O_WRONLY|os.O_CREATE|os.O_TRUNC, 0o600)
	if err != nil {
		return fmt.Errorf("creating %s: %w", localPath, err)
	}
	defer func() {
		if closeErr := file.Close(); err == nil && closeErr != nil {
			err = fmt.Errorf("closing %s: %w", localPath, closeErr)
		}
		if err != nil {
			os.Remove(localPath)
		}
	}()

	if _, err := io.Copy(file, object); err != nil {
		return s.wrap("reading", key, err)
	}
	return nil
}

// Upload implements Store.
func (s *S3Store) Upload(ctx context.Context, localPath, key string) error {
	if _, err := s.client.FPutObject(ctx, s.bucket, key, localPath, minio.PutObjectOptions{}); err != nil {
		return s.wrap("writing", key, err)
	}
	return nil
}

// List implements Store.
//
// It uses the client's single-page call, because the higher-level ListObjects hides the
// continuation token a caller needs to ask for the next page. That call takes no
// context, so it runs aside and the caller stops waiting when ctx ends; the request
// itself finishes on its own.
func (s *S3Store) List(ctx context.Context, opts ListOptions) (*Listing, error) {
	if err := ctx.Err(); err != nil {
		return nil, err
	}
	maxKeys := opts.MaxKeys
	if maxKeys <= 0 || maxKeys > DefaultMaxKeys {
		maxKeys = DefaultMaxKeys
	}

	type result struct {
		page minio.ListBucketV2Result
		err  error
	}
	done := make(chan result, 1)
	core := minio.Core{Client: s.client}
	go func() {
		page, err := core.ListObjectsV2(s.bucket, opts.Prefix, "", opts.ContinuationToken, opts.Delimiter, maxKeys)
		done <- result{page, err}
	}()

	var r result
	select {
	case <-ctx.Done():
		return nil, ctx.Err()
	case r = <-done:
	}
	if r.err != nil {
		return nil, fmt.Errorf("listing bucket %s: %w", s.bucket, r.err)
	}

	out := &Listing{
		Objects:        make([]ObjectInfo, 0, len(r.page.Contents)),
		CommonPrefixes: make([]string, 0, len(r.page.CommonPrefixes)),
	}
	for _, object := range r.page.Contents {
		out.Objects = append(out.Objects, toObjectInfo(object))
	}
	for _, prefix := range r.page.CommonPrefixes {
		out.CommonPrefixes = append(out.CommonPrefixes, prefix.Prefix)
	}
	if r.page.IsTruncated {
		out.NextContinuationToken = r.page.NextContinuationToken
	}
	return out, nil
}

func toObjectInfo(info minio.ObjectInfo) ObjectInfo {
	return ObjectInfo{
		Key:          info.Key,
		Size:         info.Size,
		ETag:         info.ETag,
		LastModified: info.LastModified,
	}
}

// wrap adds which object failed, and turns a missing object into ErrNotFound.
func (s *S3Store) wrap(action, key string, err error) error {
	if minio.ToErrorResponse(err).Code == noSuchKey {
		return fmt.Errorf("%s s3://%s/%s: %w", action, s.bucket, key, ErrNotFound)
	}
	return fmt.Errorf("%s s3://%s/%s: %w", action, s.bucket, key, err)
}
