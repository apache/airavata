package ssh

import (
	"context"
	"crypto/ed25519"
	"crypto/rand"
	"errors"
	"net"
	"sync"
	"sync/atomic"
	"testing"
	"time"

	credmodel "github.com/apache/airavata/api/credentials/model"
	gossh "golang.org/x/crypto/ssh"
)

// pipeDialer returns a dial func that serves each connection from an loopback SSH
// server, counting how many connections it was asked for.
func pipeDialer(t *testing.T, dials *atomic.Int32) func(context.Context) (*gossh.Client, error) {
	t.Helper()
	_, hostKey, err := ed25519.GenerateKey(rand.Reader)
	if err != nil {
		t.Fatal(err)
	}
	signer, err := gossh.NewSignerFromKey(hostKey)
	if err != nil {
		t.Fatal(err)
	}
	serverConfig := &gossh.ServerConfig{NoClientAuth: true}
	serverConfig.AddHostKey(signer)

	// Loopback TCP rather than net.Pipe: both ends send their version line before
	// reading, which deadlocks on an unbuffered pipe.
	listener, err := net.Listen("tcp", "127.0.0.1:0")
	if err != nil {
		t.Fatal(err)
	}
	t.Cleanup(func() { listener.Close() })
	go func() {
		for {
			serverSide, err := listener.Accept()
			if err != nil {
				return
			}
			go serveNoChannels(serverSide, serverConfig)
		}
	}()

	return func(ctx context.Context) (*gossh.Client, error) {
		dials.Add(1)
		clientSide, err := net.Dial("tcp", listener.Addr().String())
		if err != nil {
			return nil, err
		}
		conn, chans, reqs, err := gossh.NewClientConn(clientSide, "loopback", &gossh.ClientConfig{

			User:            "test",
			HostKeyCallback: gossh.InsecureIgnoreHostKey(),
		})
		if err != nil {
			return nil, err
		}
		return gossh.NewClient(conn, chans, reqs), nil
	}
}

// serveNoChannels completes the handshake and answers global requests, which is all
// the pool needs from a server.
func serveNoChannels(conn net.Conn, config *gossh.ServerConfig) {
	_, chans, reqs, err := gossh.NewServerConn(conn, config)
	if err != nil {
		return
	}
	go func() {
		for req := range reqs {
			req.Reply(true, nil)
		}
	}()
	for ch := range chans {
		ch.Reject(gossh.Prohibited, "no channels")
	}
}

func testDialConfig() SSHDialConfig {
	return NewSSHDialConfig("example.org", 22, "alice", credmodel.SSHCredential{ID: "cred-1"}, "user-1")
}

func TestPoolSharesConcurrentDials(t *testing.T) {
	pool := newSSHConnectionPool()
	defer pool.Close()
	var dials atomic.Int32
	dial := pipeDialer(t, &dials)

	var wg sync.WaitGroup
	clients := make([]*pooledClient, 8)
	for i := range clients {
		wg.Add(1)
		go func() {
			defer wg.Done()
			c, err := pool.acquire(t.Context(), testDialConfig(), dial)
			if err != nil {
				t.Error(err)
				return
			}
			clients[i] = c
		}()
	}
	wg.Wait()
	if n := dials.Load(); n != 1 {
		t.Fatalf("dialed %d times, want 1", n)
	}
	for _, c := range clients[1:] {
		if c.Client != clients[0].Client {
			t.Fatal("callers were handed different connections")
		}
	}
	for _, c := range clients {
		c.Close()
	}

	// Released, but still within the idle timeout: reused.
	c, err := pool.acquire(t.Context(), testDialConfig(), dial)
	if err != nil {
		t.Fatal(err)
	}
	defer c.Close()
	if c.Client != clients[0].Client || dials.Load() != 1 {
		t.Fatal("idle connection was not reused")
	}
}

func TestPoolSeparatesConfigs(t *testing.T) {
	pool := newSSHConnectionPool()
	defer pool.Close()
	var dials atomic.Int32
	dial := pipeDialer(t, &dials)

	other := testDialConfig()
	other.userId = "user-2"
	a, err := pool.acquire(t.Context(), testDialConfig(), dial)
	if err != nil {
		t.Fatal(err)
	}
	defer a.Close()
	b, err := pool.acquire(t.Context(), other, dial)
	if err != nil {
		t.Fatal(err)
	}
	defer b.Close()
	if a.Client == b.Client || dials.Load() != 2 {
		t.Fatal("different users shared a connection")
	}
}

func TestPoolRedialsAfterConnectionDies(t *testing.T) {
	pool := newSSHConnectionPool()
	defer pool.Close()
	var dials atomic.Int32
	dial := pipeDialer(t, &dials)

	first, err := pool.acquire(t.Context(), testDialConfig(), dial)
	if err != nil {
		t.Fatal(err)
	}
	first.Client.Close()
	first.Close()
	first.Client.Wait()

	// The watcher evicts asynchronously, so give it the chance before asserting.
	for range 100 {
		pool.lock.Lock()
		_, present := pool.entries[poolKeyFor(testDialConfig())]
		pool.lock.Unlock()
		if !present {
			break
		}
		time.Sleep(10 * time.Millisecond)
	}

	second, err := pool.acquire(t.Context(), testDialConfig(), dial)
	if err != nil {
		t.Fatal(err)
	}
	defer second.Close()
	if second.Client == first.Client || dials.Load() != 2 {
		t.Fatal("dead connection was handed out again")
	}
}

func TestPoolDoesNotCacheFailedDials(t *testing.T) {
	pool := newSSHConnectionPool()
	defer pool.Close()
	failing := func(context.Context) (*gossh.Client, error) { return nil, errors.New("refused") }
	if _, err := pool.acquire(t.Context(), testDialConfig(), failing); err == nil {
		t.Fatal("expected the dial error")
	}

	var dials atomic.Int32
	c, err := pool.acquire(t.Context(), testDialConfig(), pipeDialer(t, &dials))
	if err != nil {
		t.Fatal(err)
	}
	c.Close()
	if dials.Load() != 1 {
		t.Fatal("failed dial was cached")
	}
}
