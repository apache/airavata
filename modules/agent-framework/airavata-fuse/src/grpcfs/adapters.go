// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at

//   http://www.apache.org/licenses/LICENSE-2.0

// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

// Place for mapper structs/interfaces

package grpcfs

import (
	pb "grpcfs/pb"
	"io/fs"
	"time"
)

type FileInfoBridge struct {
	info pb.FileInfo
}

func (b *FileInfoBridge) Name() string {
	return b.info.Name
}

func (b *FileInfoBridge) Size() int64 {
	return b.info.Size
}

func (b *FileInfoBridge) Mode() fs.FileMode {
	return fs.FileMode(b.info.Mode)
}

func (b *FileInfoBridge) ModTime() time.Time {
	return b.info.ModTime.AsTime()
}

func (b *FileInfoBridge) IsDir() bool {
	return b.info.IsDir
}

type Sys struct {
	Ino uint64
}

func (b *FileInfoBridge) Sys() any {
	return &Sys{
		Ino: b.info.Ino,
	}
}

type DirEntryBridge struct {
	info pb.DirEntry
}

func (b *DirEntryBridge) Name() string {
	return b.info.Name
}

func (b *DirEntryBridge) IsDir() bool {
	return b.info.IsDir
}

func (b *DirEntryBridge) Type() fs.FileMode {
	return fs.FileMode(b.info.FileMode)
}

func (b *DirEntryBridge) Info() (fs.FileInfo, error) {
	info := &FileInfoBridge{info: *b.info.Info}
	return info, nil
}
