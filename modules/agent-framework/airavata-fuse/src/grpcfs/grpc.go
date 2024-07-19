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

// place for grpc calls

package grpcfs

import (
	"context"
	pb "grpcfs/pb"
	"io/fs"
	"log"
	"time"

	"google.golang.org/protobuf/types/known/timestamppb"
)

var ctxt = &pb.RPCContext{}

func getStatFs(fsClient pb.FuseServiceClient, ctx context.Context, root string) (*pb.StatFs, error) {
	req := &pb.StatFsReq{
		Name:    root,
		Context: ctxt,
	}
	res, err := fsClient.StatFs(ctx, req)
	if err != nil {
		return nil, err
	}
	raw := res.Result
	if raw == nil {
		return nil, ctx.Err()
	}
	return raw, err
}

func getStat(fsClient pb.FuseServiceClient, ctx context.Context, path string) (fs.FileInfo, error) {
	log.Print("grpc.getStat - path=", path)
	req := &pb.FileInfoReq{
		Name:    path,
		Context: ctxt,
	}
	log.Print("grpc.getStat - calling fsClient.FileInfo for ", path)
	res, err := fsClient.FileInfo(ctx, req)
	if err != nil {
		log.Print("grpc.getStat - fsClient.FileInfo raised error. ", err)
		return nil, err
	}
	raw := res.Result
	if raw == nil {
		return nil, ctx.Err()
	}
	result := &FileInfoBridge{info: *raw}
	return result, err
}

func readDir(fsClient pb.FuseServiceClient, ctx context.Context, path string) ([]fs.DirEntry, error) {
	req := &pb.ReadDirReq{
		Name:    path,
		Context: ctxt,
	}
	res, err := fsClient.ReadDir(ctx, req)
	if err != nil {
		log.Print("grpc.readDir - fsClient.ReadDir raised error. ", err)
		return nil, err
	}
	raw := res.Result
	var entries []fs.DirEntry
	for _, entry := range raw {
		entries = append(entries, &DirEntryBridge{info: *entry})
	}
	return entries, err
}

func readFile(fsClient pb.FuseServiceClient, ctx context.Context, path string) ([]byte, error) {
	req := &pb.ReadFileReq{
		Name:    path,
		Context: ctxt,
	}
	res, err := fsClient.ReadFile(ctx, req)
	return res.Result.Dst, err
}

func writeFile(fsClient pb.FuseServiceClient, ctx context.Context, path string, data []byte, offset int64) (bool, error) {
	req := &pb.WriteFileReq{
		Name:    path,
		Context: ctxt,
	}
	res, err := fsClient.WriteFile(ctx, req)
	return res.Result, err
}

func setInodeAttributes(fsClient pb.FuseServiceClient, ctx context.Context, path string, size *uint64, mode *uint32, atime *time.Time, mtime *time.Time) (*pb.InodeAtt, error) {
	var at *timestamppb.Timestamp
	var mt *timestamppb.Timestamp
	if atime != nil {
		at = timestamppb.New(*atime)
	}
	if mtime != nil {
		mt = timestamppb.New(*mtime)
	}
	req := &pb.SetInodeAttReq{
		Name:     path,
		Context:  ctxt,
		Size:     size,
		FileMode: mode,
		ATime:    at,
		MTime:    mt,
	}
	res, err := fsClient.SetInodeAtt(ctx, req)
	if err != nil {
		log.Print("grpc.setInodeAttributes - fsClient.SetInodeAtt raised error. ", err)
		return nil, err
	}
	return res.Result, err
}
