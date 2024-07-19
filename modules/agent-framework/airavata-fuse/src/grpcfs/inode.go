// place for data-side helper functions

package grpcfs

import (
	"context"
	"fmt"
	"grpcfs/pb"
	"log"
	"os"
	"path/filepath"
	"sync"
	"sync/atomic"

	"github.com/jacobsa/fuse/fuseops"
	"github.com/jacobsa/fuse/fuseutil"
)

var (
	uid                     = uint32(os.Getuid())
	gid                     = uint32(os.Getgid())
	allocatedInodeId uint64 = fuseops.RootInodeID
)

type Inode interface {
	Id() fuseops.InodeID
	Path() string
	String() string
	Attributes() (*fuseops.InodeAttributes, error)
	ListChildren(inodes *sync.Map) ([]*fuseutil.Dirent, error)
	Contents() ([]byte, error)
}

func getOrCreateInode(inodes *sync.Map, fsClient pb.FuseServiceClient, ctx context.Context, parentId fuseops.InodeID, name string) (Inode, error) {
	log.Print("inode.getOrCreateInode - called. ", name)
	parent, found := inodes.Load(parentId)
	if !found {
		log.Print("inode.getOrCreateInode - no parent Inode: ", parentId)
		return nil, nil
	}
	parentPath := parent.(Inode).Path()
	path := filepath.Join(parentPath, name)
	log.Print("inode.getOrCreateInode - resolved path: ", path)

	fileInfo, err := getStat(fsClient, ctx, path)
	if err != nil {
		log.Print("inode.getOrCreateInode - no path stats: ", path)
		return nil, err
	}
	log.Print("inode.getOrCreateInode - got file stats: ", path, fileInfo)
	// stat, _ := fileInfo.Sys().(*Sys)

	entry, _ := NewInode(path, fsClient)
	// entry := &inodeEntry{
	// 	id:     fuseops.InodeID(stat.Ino),
	// 	path:   path,
	// 	client: fsClient,
	// }
	storedEntry, _ := inodes.LoadOrStore(entry.Id(), entry)
	return storedEntry.(Inode), nil
}

func nextInodeID() (next fuseops.InodeID) {
	nextInodeId := atomic.AddUint64(&allocatedInodeId, 1)
	return fuseops.InodeID(nextInodeId)
}

type inodeEntry struct {
	id     fuseops.InodeID
	path   string
	client pb.FuseServiceClient
}

func NewInode(path string, client pb.FuseServiceClient) (Inode, error) {
	return &inodeEntry{
		id:     nextInodeID(),
		path:   path,
		client: client,
	}, nil
}

func (in *inodeEntry) Id() fuseops.InodeID {
	return in.id
}

func (in *inodeEntry) Path() string {
	return in.path
}

func (in *inodeEntry) String() string {
	return fmt.Sprintf("%v::%v", in.id, in.path)
}

func (in *inodeEntry) Attributes() (*fuseops.InodeAttributes, error) {
	log.Print("inodeEntry.Attributes - called. ", in.path)
	fileInfo, err := getStat(in.client, context.TODO(), in.path)
	if err != nil {
		return &fuseops.InodeAttributes{}, err
	}

	return &fuseops.InodeAttributes{
		Size:  uint64(fileInfo.Size()),
		Nlink: 1,
		Mode:  fileInfo.Mode(),
		Mtime: fileInfo.ModTime(),
		Uid:   uid,
		Gid:   gid,
	}, nil
}

func (in *inodeEntry) ListChildren(inodes *sync.Map) ([]*fuseutil.Dirent, error) {
	log.Print("inodeEntry.ListChildren - called. ", in.path)
	children, err := readDir(in.client, context.TODO(), in.path)
	if err != nil {
		log.Print("inodeEntry.ListChildren - error in readDir. ", in.path)
		return nil, err
	}
	dirents := []*fuseutil.Dirent{}
	for i, child := range children {

		childInode, err := getOrCreateInode(inodes, in.client, context.TODO(), in.id, child.Name())
		if err != nil || childInode == nil {
			continue
		}

		var childType fuseutil.DirentType
		if child.IsDir() {
			childType = fuseutil.DT_Directory
		} else if child.Type()&os.ModeSymlink != 0 {
			childType = fuseutil.DT_Link
		} else {
			childType = fuseutil.DT_File
		}

		dirents = append(dirents, &fuseutil.Dirent{
			Offset: fuseops.DirOffset(i + 1),
			Inode:  childInode.Id(),
			Name:   child.Name(),
			Type:   childType,
		})
	}
	return dirents, nil
}

func (in *inodeEntry) Contents() ([]byte, error) {
	log.Print("inodeEntry.Contents - called. ", in.path)
	res, err := readFile(in.client, context.TODO(), in.path)
	return res, err
}
