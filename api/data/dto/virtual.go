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

package dto

import (
	"github.com/apache/airavata/internal/httpx"

	model "github.com/apache/airavata/api/data/model"
)

// VirtualDataDirectoryRequest is the create/update payload for a directory node.
//
// There is no owner field: ownership comes from the access token at creation and is
// never rewritten, so a caller cannot register a dataset as someone else's.
//
// ParentDirectoryID is how a node is placed, and changing it on an update moves the
// node. A nil parent means a root — the top of a new dataset. An update replaces the
// whole node, as everywhere else in this API, so a caller renaming a nested directory
// has to send its parent back with the new name; leaving the field out asks for a
// root, and the node is moved out to become one.
//
// DataProductID is set only on a directory standing for a registered directory
// product; a plain container leaves it nil and holds children instead.
type VirtualDataDirectoryRequest struct {
	DirectoryName     *string `json:"directoryName"`
	ParentDirectoryID *string `json:"parentDirectoryId"`
	DataProductID     *string `json:"dataProductId"`
}

// Validate implements httpx.Validator.
func (r *VirtualDataDirectoryRequest) Validate() []httpx.FieldError {
	var c httpx.Constraints
	c.NotBlankPtr("directoryName", "Directory name cannot be blank", r.DirectoryName)
	validateNodeName(&c, "directoryName", r.DirectoryName)
	return c.Fields()
}

// Name returns the requested name. Validation has already rejected a nil or blank one.
func (r *VirtualDataDirectoryRequest) Name() string {
	if r.DirectoryName == nil {
		return ""
	}
	return *r.DirectoryName
}

// VirtualDataDirectoryResponse is the read model for a directory node.
//
// Permission is what the calling principal may do with it: WRITE for the owner and for
// admins, otherwise whatever the strongest share reaching this node or any ancestor
// grants. It is a property of the request rather than of the record.
//
// Files and Directories are filled only by the endpoints that return contents; a
// listing leaves them out rather than returning an empty array, so "not asked for" and
// "empty directory" stay distinct.
type VirtualDataDirectoryResponse struct {
	VirtualDataDirectoryID string  `json:"virtualDataDirectoryId"`
	DirectoryName          *string `json:"directoryName"`
	ParentDirectoryID      *string `json:"parentDirectoryId"`
	DataProductID          *string `json:"dataProductId"`
	OwnerID                *string `json:"ownerId"`
	CreatedAt              int64   `json:"createdAt"`

	Permission *string `json:"permission,omitempty"`

	Files       []VirtualDataFileResponse      `json:"virtualDataFiles,omitempty"`
	Directories []VirtualDataDirectoryResponse `json:"virtualDataDirectories,omitempty"`
}

func ToVirtualDataDirectoryResponse(d *model.VirtualDataDirectory) VirtualDataDirectoryResponse {
	return VirtualDataDirectoryResponse{
		VirtualDataDirectoryID: d.ID,
		DirectoryName:          d.DirectoryName,
		ParentDirectoryID:      d.ParentDirectoryID,
		DataProductID:          d.DataProductID,
		OwnerID:                d.OwnerID,
		CreatedAt:              d.CreatedAt,
	}
}

// ToVirtualDataDirectoryResponseWith is ToVirtualDataDirectoryResponse with the
// caller's effective permission attached.
func ToVirtualDataDirectoryResponseWith(d *model.VirtualDataDirectory, permission string) VirtualDataDirectoryResponse {
	out := ToVirtualDataDirectoryResponse(d)
	out.Permission = &permission
	return out
}

func ToVirtualDataDirectoryResponses(in []model.VirtualDataDirectory) []VirtualDataDirectoryResponse {
	out := make([]VirtualDataDirectoryResponse, 0, len(in))
	for i := range in {
		out = append(out, ToVirtualDataDirectoryResponse(&in[i]))
	}
	return out
}

// ApplyVirtualDataDirectoryRequest copies the mutable fields of a request onto an
// entity. The owner and the creation time are never written from a request.
func ApplyVirtualDataDirectoryRequest(dst *model.VirtualDataDirectory, src *VirtualDataDirectoryRequest) {
	dst.DirectoryName = src.DirectoryName
	dst.ParentDirectoryID = src.ParentDirectoryID
	dst.DataProductID = src.DataProductID
}

// VirtualDataFileRequest is the create/update payload for a leaf.
//
// Both references are required: a file is a product placed under a directory, and it
// is nothing without either half.
type VirtualDataFileRequest struct {
	FileName          *string `json:"fileName"`
	ParentDirectoryID string  `json:"parentDirectoryId"`
	DataProductID     string  `json:"dataProductId"`
}

// Validate implements httpx.Validator.
func (r *VirtualDataFileRequest) Validate() []httpx.FieldError {
	var c httpx.Constraints
	c.NotBlankPtr("fileName", "File name cannot be blank", r.FileName)
	validateNodeName(&c, "fileName", r.FileName)
	c.NotBlank("parentDirectoryId", "Parent directory id cannot be blank", r.ParentDirectoryID)
	c.NotBlank("dataProductId", "Data product id cannot be blank", r.DataProductID)
	return c.Fields()
}

// Name returns the requested name. Validation has already rejected a nil or blank one.
func (r *VirtualDataFileRequest) Name() string {
	if r.FileName == nil {
		return ""
	}
	return *r.FileName
}

// VirtualDataFileResponse is the read model for a leaf. Permission is inherited from
// the directory the file sits under, since a file carries no shares of its own.
type VirtualDataFileResponse struct {
	VirtualDataFileID string  `json:"virtualDataFileId"`
	FileName          *string `json:"fileName"`
	ParentDirectoryID *string `json:"parentDirectoryId"`
	DataProductID     *string `json:"dataProductId"`
	CreatedAt         int64   `json:"createdAt"`

	Permission *string `json:"permission,omitempty"`
}

func ToVirtualDataFileResponse(f *model.VirtualDataFile) VirtualDataFileResponse {
	return VirtualDataFileResponse{
		VirtualDataFileID: f.ID,
		FileName:          f.FileName,
		ParentDirectoryID: f.ParentDirectoryID,
		DataProductID:     f.DataProductID,
		CreatedAt:         f.CreatedAt,
	}
}

// ToVirtualDataFileResponseWith is ToVirtualDataFileResponse with the caller's
// effective permission attached.
func ToVirtualDataFileResponseWith(f *model.VirtualDataFile, permission string) VirtualDataFileResponse {
	out := ToVirtualDataFileResponse(f)
	out.Permission = &permission
	return out
}

func ToVirtualDataFileResponsesWith(in []model.VirtualDataFile, permission string) []VirtualDataFileResponse {
	out := make([]VirtualDataFileResponse, 0, len(in))
	for i := range in {
		out = append(out, ToVirtualDataFileResponseWith(&in[i], permission))
	}
	return out
}

// ApplyVirtualDataFileRequest copies the mutable fields of a request onto an entity.
func ApplyVirtualDataFileRequest(dst *model.VirtualDataFile, src *VirtualDataFileRequest) {
	dst.FileName = src.FileName
	dst.ParentDirectoryID = &src.ParentDirectoryID
	dst.DataProductID = &src.DataProductID
}

// validateNodeName rejects the names that would make a dataset path ambiguous. A node
// name is one path segment, so a separator or a relative-path element in it would let
// a tree describe a location it does not have.
func validateNodeName(c *httpx.Constraints, field string, name *string) {
	if name == nil {
		return
	}
	switch *name {
	case ".", "..":
		c.Add(field, "Name cannot be . or ..")
		return
	}
	for _, r := range *name {
		if r == '/' || r == '\\' || r == 0 {
			c.Add(field, "Name cannot contain a path separator")
			return
		}
	}
}
