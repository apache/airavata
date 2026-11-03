package ssh

import (
	"context"
	"errors"
	"log/slog"
	"sync"
	"time"

	gossh "golang.org/x/crypto/ssh"
)

// poolIdleTimeout is how long a connection nobody is using stays open for the next
// caller. Long enough to carry a burst of staging activities for one process, short
// enough that an abandoned host does not hold a login session for good.
const poolIdleTimeout = 5 * time.Minute

// poolKeepaliveInterval paces the probe that notices a connection the network dropped
// without a reset, which would otherwise sit in the pool looking healthy.
const poolKeepaliveInterval = 30 * time.Second

// poolKey names what a connection was authenticated as. It is derived from the dial
// config rather than being the config itself: the credential carries pointers, so two
// configs for the same key would compare unequal by identity alone.
type poolKey struct {
	host         string
	port         int
	sshUsername  string
	credentialID string
	userId       string
}

func poolKeyFor(dialConfig SSHDialConfig) poolKey {
	return poolKey{
		host:         dialConfig.host,
		port:         dialConfig.port,
		sshUsername:  dialConfig.sshUsername,
		credentialID: dialConfig.cred.ID,
		userId:       dialConfig.userId,
	}
}

// poolEntry is one connection, or the dial that will produce it. ready is closed once
// the dial settles, after which exactly one of client and err is set.
type poolEntry struct {
	// ready is closed once the dial settles, after which exactly one of client and err is set.
	ready  chan struct{}
	client *gossh.Client
	err    error
	refs   int
	idle   *time.Timer

	// prompted is closed the first time the dial asks its owner for an interactive
	// login, and authID names the auth asked most recently. authID is guarded by the
	// pool lock.
	prompted chan struct{}
	authID   string
}

// SSHConnectionPool shares one authenticated connection between every caller dialing
// with the same config. Besides saving handshakes, this keeps an interactive login
// from asking the user for a password or OTP once per command.
type SSHConnectionPool struct {
	entries map[poolKey]*poolEntry
	lock    sync.Mutex
}

func newSSHConnectionPool() *SSHConnectionPool {
	return &SSHConnectionPool{
		entries: make(map[poolKey]*poolEntry),
	}
}

// pooledClient is a borrowed connection. Close hands it back to the pool rather than
// closing it, so callers keep the defer client.Close() they would write anyway.
type pooledClient struct {
	*gossh.Client
	once    sync.Once
	release func()
}

func (c *pooledClient) Close() error {
	c.once.Do(c.release)
	return nil
}

// acquire returns a live connection for dialConfig, dialing one if there is none.
// Callers that arrive while a dial is in flight wait for it rather than starting their
// own, so concurrent work against one host shares a single login.
//
// Under WithDeferredInteractiveAuth a caller stops waiting once the dial asks for an
// interactive login and gets InteractiveAuthRequiredError instead. A dial such a caller
// starts runs detached from its context, so the login can still be answered after it
// has returned.
func (p *SSHConnectionPool) acquire(ctx context.Context, dialConfig SSHDialConfig, dial func(context.Context) (*gossh.Client, error)) (*pooledClient, error) {
	key := poolKeyFor(dialConfig)
	deferred := defersInteractiveAuth(ctx)
	for {
		p.lock.Lock()
		entry, ok := p.entries[key]
		if !ok {
			entry = &poolEntry{ready: make(chan struct{}), prompted: make(chan struct{})}
			p.entries[key] = entry
		}
		entry.refs++
		if entry.idle != nil {
			entry.idle.Stop()
			entry.idle = nil
		}
		p.lock.Unlock()

		if !ok {
			hook := func(authID string) { p.markPrompted(entry, authID) }
			if deferred {
				dialCtx, cancel := context.WithTimeout(withAuthPromptHook(context.WithoutCancel(ctx), hook), deferredDialTimeout)
				go func() {
					defer cancel()
					client, err := dial(dialCtx)
					p.settle(key, entry, client, err)
				}()
			} else {
				client, err := dial(withAuthPromptHook(ctx, hook))
				p.settle(key, entry, client, err)
			}
		}

		// A nil channel never fires, so only a deferring caller hears the prompt.
		var prompted <-chan struct{}
		if deferred {
			prompted = entry.prompted
		}

		select {
		case <-entry.ready:
		case <-prompted:
			// A login answered by now has nothing left to wait for, so ready wins a tie.
			select {
			case <-entry.ready:
			default:
				p.lock.Lock()
				authID := entry.authID
				p.lock.Unlock()
				p.release(key, entry)
				return nil, &InteractiveAuthRequiredError{
					Host:      dialConfig.host,
					Port:      dialConfig.port,
					LoginUser: dialConfig.sshUsername,
					AuthID:    authID,
				}
			}
		case <-ctx.Done():
			p.release(key, entry)
			return nil, ctx.Err()
		}

		if entry.err == nil {
			return &pooledClient{Client: entry.client, release: func() { p.release(key, entry) }}, nil
		}
		p.release(key, entry)
		// The dial ran on whichever caller got there first, so it may have failed only
		// because that caller gave up. That says nothing about this caller's chances.
		if ok && isContextError(entry.err) && ctx.Err() == nil {
			continue
		}
		return nil, entry.err
	}
}

// settle records the outcome of a dial and wakes everyone waiting on it.
func (p *SSHConnectionPool) settle(key poolKey, entry *poolEntry, client *gossh.Client, err error) {
	p.lock.Lock()
	entry.client, entry.err = client, err
	if entry.err != nil {
		// A failed dial is not cached: the next caller should try again.
		p.evictLocked(key, entry)
	} else if entry.refs == 0 {
		// Everyone who wanted it stopped waiting during an interactive login. It is
		// kept for whoever asks next, on the same clock as a connection handed back.
		p.idleLocked(key, entry)
	}
	p.lock.Unlock()
	close(entry.ready)

	if entry.client != nil {
		go p.watch(key, entry)
	}
}

// markPrompted records that entry's dial is waiting on its owner to answer authID, and
// wakes the callers deferring that wait. Only the first prompt wakes anyone; a later one
// in the same login, an OTP after a password say, just updates the id.
func (p *SSHConnectionPool) markPrompted(entry *poolEntry, authID string) {
	p.lock.Lock()
	defer p.lock.Unlock()
	entry.authID = authID
	select {
	case <-entry.prompted:
	default:
		close(entry.prompted)
	}
}

// watch drops the entry once its connection dies, whether the server closed it or the
// keepalive gave up on it, so the next caller dials afresh instead of failing.
func (p *SSHConnectionPool) watch(key poolKey, entry *poolEntry) {
	closed := make(chan struct{})
	go func() {
		entry.client.Wait()
		slog.Info("SSH connection exited after wait", "host", key.host, "port", key.port, "user", key.sshUsername)
		close(closed)
	}()

	ticker := time.NewTicker(poolKeepaliveInterval)
	defer ticker.Stop()
	for {
		select {
		case <-closed:
			p.lock.Lock()
			p.evictLocked(key, entry)
			p.lock.Unlock()
			return
		case <-ticker.C:
			if !keepalive(entry.client, poolKeepaliveInterval) {
				slog.Info("Closing unresponsive pooled ssh connection", "host", key.host, "port", key.port, "user", key.sshUsername)
				entry.client.Close()
				p.lock.Lock()
				p.evictLocked(key, entry)
				p.lock.Unlock()
			}
		}
	}
}

// keepalive reports whether the server answered a no-op global request within timeout.
// SendRequest has no deadline of its own, so a stalled one is abandoned to the caller,
// which closes the connection and so unblocks it.
func keepalive(client *gossh.Client, timeout time.Duration) bool {
	answered := make(chan error, 1)
	go func() {
		_, _, err := client.SendRequest("keepalive@openssh.com", true, nil)
		answered <- err
	}()
	select {
	case err := <-answered:
		return err == nil
	case <-time.After(timeout):
		return false
	}
}

// release returns one reference. The last one out leaves the connection open for
// poolIdleTimeout in case another caller wants it.
func (p *SSHConnectionPool) release(key poolKey, entry *poolEntry) {
	p.lock.Lock()
	defer p.lock.Unlock()

	entry.refs--
	if entry.refs > 0 || entry.client == nil {
		return
	}
	p.idleLocked(key, entry)
}

// idleLocked closes entry's connection after poolIdleTimeout unless a caller takes it
// first. The caller holds the pool lock.
func (p *SSHConnectionPool) idleLocked(key poolKey, entry *poolEntry) {
	var timer *time.Timer
	timer = time.AfterFunc(poolIdleTimeout, func() {
		p.lock.Lock()
		defer p.lock.Unlock()
		// A caller may have taken the entry, and perhaps released it again under a
		// fresh timer, between this one firing and the lock.
		if entry.idle != timer {
			return
		}
		p.evictLocked(key, entry)
		entry.client.Close()
	})
	entry.idle = timer
}

// evictLocked removes entry from the pool if it is still the one held for key. A
// connection that is evicted while borrowed stays open until its borrowers finish
// with it; only new callers are sent elsewhere.
func (p *SSHConnectionPool) evictLocked(key poolKey, entry *poolEntry) {
	slog.Info("Evicting pooled ssh connection", "host", key.host, "port", key.port, "user", key.sshUsername)
	if p.entries[key] == entry {
		delete(p.entries, key)
	}
	slog.Info("SSH connection pool size after eviction", "size", len(p.entries))
}

// Close closes every pooled connection, borrowed or not.
func (p *SSHConnectionPool) Close() {
	p.lock.Lock()
	entries := p.entries
	p.entries = make(map[poolKey]*poolEntry)
	p.lock.Unlock()

	for _, entry := range entries {
		select {
		case <-entry.ready:
			if entry.client != nil {
				entry.client.Close()
			}
		default:
			// Still dialing: the connection lands in an entry nobody can reach and is
			// closed by the idle timer once its callers are done with it.
		}
	}
}

func isContextError(err error) bool {
	return errors.Is(err, context.Canceled) || errors.Is(err, context.DeadlineExceeded)
}
