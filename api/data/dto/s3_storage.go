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
	"net/url"
	"regexp"
	"strings"

	"github.com/apache/airavata/internal/httpx"
	"github.com/apache/airavata/internal/ptr"

	creddto "github.com/apache/airavata/api/credentials/dto"
	model "github.com/apache/airavata/api/data/model"
)

// defaultS3Region is used when a request omits the region. It is what MinIO and most
// other self-hosted stores answer to, and what the AWS SDKs fall back to.
const defaultS3Region = "us-east-1"

// maxEndpointURLLength matches the endpoint_url column.
const maxEndpointURLLength = 2048

// bucketNamePattern is the S3 bucket naming rule: 3-63 characters of lower-case
// letters, digits, dots and hyphens, starting and ending with a letter or digit. It is
// the strictest common rule across S3-compatible stores, so a name that passes it is
// valid on all of them.
var bucketNamePattern = regexp.MustCompile(`^[a-z0-9][a-z0-9.-]{1,61}[a-z0-9]$`)

// regionPattern accepts AWS-style region names (us-east-1) as well as the free-form
// names self-hosted stores are configured with, without admitting anything that could
// alter a URL or a signature.
var regionPattern = regexp.MustCompile(`^[A-Za-z0-9][A-Za-z0-9_-]{0,63}$`)

// S3DataStorageRequest is the create/update payload for an S3 storage.
//
// There is no owner field: ownership comes from the access token and is immutable, as
// for an SCP storage.
type S3DataStorageRequest struct {
	DataName *string `json:"dataName"`

	EndpointURL *string `json:"endpointUrl"`
	Region      *string `json:"region"`
	BucketName  string  `json:"bucketName"`
	PathStyle   *bool   `json:"pathStyle"`

	S3CredentialID string `json:"s3CredentialId"`
}

// Validate implements httpx.Validator.
func (r *S3DataStorageRequest) Validate() []httpx.FieldError {
	var c httpx.Constraints
	c.NotBlankPtr("dataName", "Data name cannot be blank", r.DataName)
	c.NotBlank("s3CredentialId", "S3 credential id cannot be blank", r.S3CredentialID)
	if !validBucketName(r.BucketName) {
		c.Add("bucketName", "Bucket name must be 3-63 lower-case letters, digits, dots or hyphens, starting and ending with a letter or digit")
	}
	if v := ptr.NonBlank(r.Region); v != nil && !regionPattern.MatchString(*v) {
		c.Add("region", "Region must be letters, digits, hyphens or underscores")
	}
	if v := ptr.NonBlank(r.EndpointURL); v != nil {
		if msg := endpointURLProblem(*v); msg != "" {
			c.Add("endpointUrl", msg)
		}
	}
	return c.Fields()
}

func validBucketName(name string) bool {
	return bucketNamePattern.MatchString(name) && !strings.Contains(name, "..")
}

// endpointURLProblem describes what is wrong with an endpoint URL, or returns "" when
// nothing is. An endpoint is a scheme and an authority and nothing else: credentials in
// the URL would be stored in the clear, and a path, query or fragment would be silently
// dropped or, worse, end up in every request.
func endpointURLProblem(raw string) string {
	if len(raw) > maxEndpointURLLength {
		return "Endpoint URL is too long"
	}
	u, err := url.Parse(raw)
	if err != nil {
		return "Endpoint URL is not a valid URL"
	}
	switch {
	case u.Scheme != "http" && u.Scheme != "https":
		return "Endpoint URL must start with http:// or https://"
	case u.Hostname() == "":
		return "Endpoint URL must name a host"
	case u.User != nil:
		return "Endpoint URL must not carry credentials; register them as an S3 credential"
	case (u.Path != "" && u.Path != "/") || u.RawQuery != "" || u.Fragment != "" || u.Opaque != "":
		return "Endpoint URL must be a scheme, host and optional port only"
	}
	return ""
}

// ApplyS3DataStorageRequest copies the mutable fields of a request onto an entity. The
// credential is resolved by the service, which is what turns an unknown id into a 404.
//
// A blank endpoint means AWS, an omitted region the default, and an omitted path-style
// flag virtual-hosted addressing. A trailing slash on the endpoint is dropped so the
// same store is not stored two ways.
func ApplyS3DataStorageRequest(dst *model.S3DataStorage, src *S3DataStorageRequest) {
	dst.Name = src.DataName
	dst.EndpointURL = nil
	if v := ptr.NonBlank(src.EndpointURL); v != nil {
		dst.EndpointURL = ptr.To(strings.TrimSuffix(*v, "/"))
	}
	dst.Region = ptr.FromOr(ptr.NonBlank(src.Region), defaultS3Region)
	dst.BucketName = src.BucketName
	dst.PathStyle = ptr.From(src.PathStyle)
}

// S3DataStorageResponse is the read model for an S3 storage.
//
// The credential is inlined as its safe summary — name and access key ID, never the
// secret, which the credential response type has no field for at all.
type S3DataStorageResponse struct {
	DataID   string  `json:"dataId"`
	DataName *string `json:"dataName"`
	OwnerID  *string `json:"ownerId"`

	EndpointURL *string `json:"endpointUrl"`
	Region      string  `json:"region"`
	BucketName  string  `json:"bucketName"`
	PathStyle   bool    `json:"pathStyle"`

	S3CredentialID *string                       `json:"s3CredentialId"`
	S3Credential   *creddto.S3CredentialResponse `json:"s3Credential"`

	Permission *string `json:"permission,omitempty"`
}

func ToS3DataStorageResponse(s *model.S3DataStorage) S3DataStorageResponse {
	out := S3DataStorageResponse{
		DataID:         s.ID,
		DataName:       s.Name,
		OwnerID:        s.OwnerID,
		EndpointURL:    s.EndpointURL,
		Region:         s.Region,
		BucketName:     s.BucketName,
		PathStyle:      s.PathStyle,
		S3CredentialID: s.S3CredentialID,
	}
	if s.S3Credential != nil {
		cred := creddto.ToS3CredentialResponse(s.S3Credential)
		out.S3Credential = &cred
	}
	return out
}

// ToS3DataStorageResponseWith is ToS3DataStorageResponse with the caller's effective
// permission attached.
func ToS3DataStorageResponseWith(s *model.S3DataStorage, permission string) S3DataStorageResponse {
	out := ToS3DataStorageResponse(s)
	out.Permission = &permission
	return out
}

func ToS3DataStorageResponses(in []model.S3DataStorage) []S3DataStorageResponse {
	out := make([]S3DataStorageResponse, 0, len(in))
	for i := range in {
		out = append(out, ToS3DataStorageResponse(&in[i]))
	}
	return out
}
