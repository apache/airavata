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
