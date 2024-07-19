package main

import (
	"flag"
	"log"
	"os"
	"os/signal"
	"path/filepath"

	"grpcfs"

	"github.com/jacobsa/fuse"
)

var logger = log.Default()

func handleErrIfAny(err error, message string) {
	if err != nil {
		logger.Fatalf("%s: %v\n", message, err)
	}
}

func logState(message string, v ...any) {
	logger.Print(message, v)
}

func main() {

	var mountPoint string
	var servePath string

	flag.StringVar(&mountPoint, "mount", "", "Mount point")
	flag.StringVar(&servePath, "serve", "", "Path to serve")
	flag.Parse()

	if mountPoint == "" || servePath == "" {
		logger.Fatal("Please specify both mount point and path to serve")
	}

	mountPoint, err := filepath.Abs(mountPoint)
	handleErrIfAny(err, "Invalid mount point")

	server, err := grpcfs.FuseServer("127.0.0.1:50000", servePath, logger)
	handleErrIfAny(err, "Error starting fuse server")

	cfg := &fuse.MountConfig{
		FSName:      "grpcFS",
		Subtype:     "airavata",
		VolumeName:  "GRPC FS - Airavata",
		ReadOnly:    false,
		ErrorLogger: logger,
	}
	mfs, err := fuse.Mount(mountPoint, server, cfg)
	handleErrIfAny(err, "Error when mounting fs")

	logState("running until interrupt", mfs)
	sigCh := make(chan os.Signal, 1)
	signal.Notify(sigCh, os.Interrupt)
	<-sigCh
	logState("interrupt received, terminating.")

	if err := fuse.Unmount(mountPoint); err != nil {
		logger.Fatalf("Unmount fail: %v\n", err)
	}
}
