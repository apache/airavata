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
	"strings"
	"unicode"

	"github.com/apache/airavata/internal/httpx"

	model "github.com/apache/airavata/api/data/model"
)

// maxReplicaPathLength matches the path column, and maxS3ObjectKeyLength is the S3
// limit on a key, in bytes.
const (
	maxReplicaPathLength = 2048
	maxS3ObjectKeyLength = 1024
)

// DataProductReplicaRequest names one more location of a product. Only a secondary
// replica is added this way: the primary location is the product's own storage and
// path, changed by updating the product.
type DataProductReplicaRequest struct {
	DataStorageID   string                 `json:"dataStorageId"`
	DataStorageType *model.DataStorageType `json:"dataStorageType"`
	Path            string                 `json:"path"`
}

// StorageType returns the requested storage type, defaulting to SCP as a product does.
func (r *DataProductReplicaRequest) StorageType() model.DataStorageType {
	if r.DataStorageType == nil {
		return model.DataStorageTypeSCP
	}
	return *r.DataStorageType
}

// Validate implements httpx.Validator.
func (r *DataProductReplicaRequest) Validate() []httpx.FieldError {
	var c httpx.Constraints
	c.NotBlank("dataStorageId", "Data storage id cannot be blank", r.DataStorageID)
	c.NotBlank("path", "Path cannot be blank", r.Path)
	if !r.StorageType().ValidForReplica() {
		c.Add("dataStorageType", "Data storage type must be one of SCP, S3")
	}
	if len(r.Path) > maxReplicaPathLength {
		c.Add("path", "Path is too long")
	} else if r.StorageType() == model.DataStorageTypeS3 {
		if msg := objectKeyProblem(r.Path); msg != "" {
			c.Add("path", msg)
		}
	}
	return c.Fields()
}

// objectKeyProblem describes what is wrong with an S3 object key, or returns "" when
// nothing is. S3 itself accepts nearly any key, but a leading slash, a "." or ".."
// segment, or a control character is treated differently by different clients and
// stores, so the same key could end up naming a different object.
func objectKeyProblem(key string) string {
	switch {
	case len(key) > maxS3ObjectKeyLength:
		return "S3 object key must be at most 1024 bytes"
	case strings.HasPrefix(key, "/"):
		return "S3 object key must not start with a slash"
	case strings.IndexFunc(key, unicode.IsControl) >= 0:
		return "S3 object key must not contain control characters"
	}
	for _, segment := range strings.Split(key, "/") {
		if segment == "." || segment == ".." {
			return "S3 object key must not contain . or .. segments"
		}
	}
	return ""
}

// DataProductReplicaResponse is the read model for one location of a product.
type DataProductReplicaResponse struct {
	ReplicaID       string                `json:"replicaId"`
	DataProductID   string                `json:"dataProductId"`
	DataStorageID   string                `json:"dataStorageId"`
	DataStorageType model.DataStorageType `json:"dataStorageType"`
	Path            string                `json:"path"`
	IsPrimary       bool                  `json:"isPrimary"`
	CreatedAt       int64                 `json:"createdAt"`
}

func ToDataProductReplicaResponse(r *model.DataProductReplica) DataProductReplicaResponse {
	return DataProductReplicaResponse{
		ReplicaID:       r.ID,
		DataProductID:   r.DataProductID,
		DataStorageID:   r.DataStorageID,
		DataStorageType: r.DataStorageType,
		Path:            r.Path,
		IsPrimary:       r.IsPrimary,
		CreatedAt:       r.CreatedAt,
	}
}

func ToDataProductReplicaResponses(in []model.DataProductReplica) []DataProductReplicaResponse {
	out := make([]DataProductReplicaResponse, 0, len(in))
	for i := range in {
		out = append(out, ToDataProductReplicaResponse(&in[i]))
	}
	return out
}
