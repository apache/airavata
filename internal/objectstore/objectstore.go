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

// Package objectstore moves files between this server and an object store.
//
// Transfers go through a local file, because that is how the server moves data: S3 to a
// temporary file to SCP to the cluster, so a cluster never needs the store's credentials.
package objectstore

import (
	"context"
	"errors"
	"time"
)

// ErrNotFound reports that no object exists at the key asked for.
var ErrNotFound = errors.New("object not found")

// DefaultMaxKeys is the page size a List uses when none is asked for, and the most any
// S3 service returns in one page.
const DefaultMaxKeys = 1000

// ObjectInfo describes one stored object.
type ObjectInfo struct {
	Key          string
	Size         int64
	ETag         string
	LastModified time.Time
}

// ListOptions selects one page of a listing.
type ListOptions struct {
	Prefix string

	// Delimiter rolls every key containing it after the prefix up into a common prefix.
	// "/" lists one level of a folder-like tree; "" lists every key under the prefix.
	Delimiter string

	// ContinuationToken is the NextContinuationToken of the previous page.
	ContinuationToken string

	// MaxKeys caps objects and common prefixes together. Zero means DefaultMaxKeys.
	MaxKeys int
}

// Listing is one page of objects and common prefixes.
type Listing struct {
	Objects        []ObjectInfo
	CommonPrefixes []string

	// NextContinuationToken is empty on the last page.
	NextContinuationToken string
}

// Store is a bucket of objects.
type Store interface {
	// Stat describes one object, or returns ErrNotFound.
	Stat(ctx context.Context, key string) (*ObjectInfo, error)

	// Download writes an object to localPath, replacing any file there. Nothing is
	// left at localPath if it fails. It returns ErrNotFound for a missing object.
	Download(ctx context.Context, key, localPath string) error

	// Upload writes the file at localPath to an object, replacing any object there.
	// Large files are sent as a multipart upload.
	Upload(ctx context.Context, localPath, key string) error

	// List returns one page of the objects matching opts, in key order.
	List(ctx context.Context, opts ListOptions) (*Listing, error)
}
