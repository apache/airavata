package main

import (
	"context"
	"grpcfs/pb"
	"log"
	"net"
	"os"
	"os/signal"
	"syscall"

	"golang.org/x/sys/unix"

	"google.golang.org/grpc"
	"google.golang.org/protobuf/types/known/timestamppb"
)

var logger = log.Default()

func handleErr(err error, message string) error {
	if err != nil {
		logger.Printf("%s: %v\n", message, err)
		return err
	}
	return nil
}

func logState(message string, v ...any) {
	logger.Print(message, v)
}

type server struct {
	pb.FuseServiceServer
}

func (s *server) StatFs(ctx context.Context, req *pb.StatFsReq) (*pb.StatFsRes, error) {
	path := req.Name
	rpcCtx := req.Context
	logger.Print("received valid StatFS request. ", path, rpcCtx)
	stat := &unix.Statfs_t{}
	unix.Statfs(req.Name, stat)
	res := &pb.StatFsRes{
		Result: &pb.StatFs{
			BlockSize:       uint32(stat.Bsize),
			Blocks:          stat.Blocks,
			BlocksFree:      stat.Bfree,
			BlocksAvailable: stat.Bavail,
			IoSize:          uint32(stat.Bsize),
			InodesFree:      stat.Ffree,
			Inodes:          stat.Files,
		},
	}
	return res, nil
}

func (s *server) FileInfo(ctx context.Context, req *pb.FileInfoReq) (*pb.FileInfoRes, error) {
	path := req.Name
	rpcCtx := req.Context
	logger.Print("received valid FileInfo request. ", path, rpcCtx)
	fileInfo, err := os.Stat(path)
	if handleErr(err, "os.Stat failed") != nil {
		return nil, err
	}
	stat := fileInfo.Sys().(*syscall.Stat_t)
	res := &pb.FileInfoRes{
		Result: &pb.FileInfo{
			Name:    fileInfo.Name(),
			Size:    fileInfo.Size(),
			Mode:    uint32(fileInfo.Mode()),
			ModTime: timestamppb.New(fileInfo.ModTime()),
			IsDir:   fileInfo.IsDir(),
			Ino:     stat.Ino,
		},
	}
	logger.Print("responded valid FileInfo. ", res.Result)
	return res, nil
}

// TODO implement any locks here
func (s *server) OpenDir(ctx context.Context, req *pb.OpenDirReq) (*pb.OpenDirRes, error) {
	path := req.Name
	rpcCtx := req.Context
	logger.Print("received valid OpenDir request. ", path, rpcCtx)
	res := &pb.OpenDirRes{
		Result: &pb.OpenedDir{},
	}
	return res, nil
}

// TODO implement any locks here
func (s *server) OpenFile(ctx context.Context, req *pb.OpenFileReq) (*pb.OpenFileRes, error) {
	path := req.Name
	rpcCtx := req.Context
	logger.Print("received valid OpenFile request. ", path, rpcCtx)
	res := &pb.OpenFileRes{
		Result: &pb.OpenedFile{},
	}
	return res, nil
}

func (s *server) ReadDir(ctx context.Context, req *pb.ReadDirReq) (*pb.ReadDirRes, error) {
	path := req.Name
	rpcCtx := req.Context
	logger.Print("received valid ReadDir request. ", path, rpcCtx)
	entries, err := os.ReadDir(path)
	if handleErr(err, "os.ReadDir failed") != nil {
		return nil, err
	}
	resEntries := []*pb.DirEntry{}
	for _, entry := range entries {
		info, err := entry.Info()
		if handleErr(err, "entry.Info() failed") != nil {
			return nil, err
		}
		obj := pb.DirEntry{
			Name:     entry.Name(),
			IsDir:    entry.IsDir(),
			FileMode: uint32(entry.Type()),
			Info: &pb.FileInfo{
				Name:    info.Name(),
				Size:    info.Size(),
				Mode:    uint32(info.Mode()),
				ModTime: timestamppb.New(info.ModTime()),
				IsDir:   info.IsDir(),
				Ino:     info.Sys().(*syscall.Stat_t).Ino,
			},
		}
		resEntries = append(resEntries, &obj)
	}
	res := &pb.ReadDirRes{
		Result: resEntries,
	}

	return res, nil
}

func (s *server) ReadFile(ctx context.Context, req *pb.ReadFileReq) (*pb.ReadFileRes, error) {
	path := req.Name
	rpcCtx := req.Context
	logger.Print("received valid ReadFile request. ", path, rpcCtx)
	file, err := os.ReadFile(path)
	if handleErr(err, "os.Stat failed") != nil {
		return nil, err
	}
	// Only Dst is used
	res := &pb.ReadFileRes{
		Result: &pb.FileEntry{
			Dst: file,
		},
	}
	return res, nil
}

func (s *server) WriteFile(ctx context.Context, req *pb.WriteFileReq) (*pb.WriteFileRes, error) {
	path := req.Name
	rpcCtx := req.Context
	data := req.Data
	offset := req.Offset
	// TODO properly use offset
	logger.Print("received valid WriteFile request. ", path, rpcCtx, offset)
	err := os.WriteFile(path, data, 0666)
	if handleErr(err, "os.WriteFile failed") != nil {
		return nil, err
	}
	res := &pb.WriteFileRes{
		Result: true,
	}
	return res, nil
}

func (s *server) SetInodeAtt(ctx context.Context, req *pb.SetInodeAttReq) (*pb.SetInodeAttRes, error) {
	path := req.Name
	rpcCtx := req.Context
	// updated values
	size := req.Size
	mode := req.FileMode
	atime := req.ATime
	mtime := req.MTime
	logger.Print("received valid SetInodeAtt request. ", path, rpcCtx, size, mode, atime, mtime)
	if size != nil {
		os.Truncate(path, int64(*size))
	}
	if mode != nil {
		os.Chmod(path, os.FileMode(*mode))
	}
	if (atime != nil) && (mtime != nil) {
		os.Chtimes(path, atime.AsTime(), mtime.AsTime())
	}
	// once updated, get and return latest values
	fileInfo, err := os.Stat(path)
	if handleErr(err, "os.Stat failed") != nil {
		return nil, err
	}
	res := &pb.SetInodeAttRes{
		Result: &pb.InodeAtt{
			Size:     uint64(fileInfo.Size()),
			FileMode: uint32(fileInfo.Mode()),
			Mtime:    timestamppb.New(fileInfo.ModTime()),
			Atime:    timestamppb.New(fileInfo.ModTime()),
		},
	}
	return res, nil
}

func main() {

	listener, err := net.Listen("tcp", "127.0.0.1:50000")
	if handleErr(err, "Could not start GRPC server") != nil {
		os.Exit(1)
	}

	s := grpc.NewServer()
	pb.RegisterFuseServiceServer(s, &server{})

	go s.Serve(listener)
	logState("running until interrupt")

	sigCh := make(chan os.Signal, 1)
	signal.Notify(sigCh, os.Interrupt)
	<-sigCh
	logState("interrupt received, terminating.")
}
