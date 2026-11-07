package ssh

import (
	"errors"
	"sync"
)

type GlobalSSHContext struct {
	InteractiveAuthStore *SSHInteractiveAuthStore
	ConnectionPool       *SSHConnectionPool
}

func NewGlobalSSHContext() *GlobalSSHContext {
	return &GlobalSSHContext{
		InteractiveAuthStore: newSSHInteractiveAuthStore(),
		ConnectionPool:       newSSHConnectionPool(),
	}
}

type SSHInteractiveAuthStore struct {
	// List of interactive SSH auths keyed by user ID, then by auth ID
	Auths map[string]map[string]*SSHInteractiveAuth
	lock  sync.Mutex
	// User ID mapping to subscriber channels
	SubscriberQueue map[string]chan struct{}
}

func newSSHInteractiveAuthStore() *SSHInteractiveAuthStore {
	return &SSHInteractiveAuthStore{
		Auths:           make(map[string]map[string]*SSHInteractiveAuth),
		SubscriberQueue: make(map[string]chan struct{}),
	}
}

// Lookups take the caller's connection and see only auths whose login it started.
func (store *SSHInteractiveAuthStore) GetAuth(userId, connection, authId string) (*SSHInteractiveAuth, bool) {
	store.lock.Lock()
	defer store.lock.Unlock()

	userAuths, ok := store.Auths[userId]
	if !ok {
		return nil, false
	}
	auth, ok := userAuths[authId]
	if !ok || auth.connection != connection {
		return nil, false
	}
	return auth, true
}

func (store *SSHInteractiveAuthStore) AddAuth(auth *SSHInteractiveAuth) {
	store.lock.Lock()
	defer store.lock.Unlock()

	userAuths, ok := store.Auths[auth.dialConfig.userId]
	if !ok {
		userAuths = make(map[string]*SSHInteractiveAuth)
		store.Auths[auth.dialConfig.userId] = userAuths
	}
	userAuths[auth.AuthId] = auth
	store.resetSubscriberQueue(auth.dialConfig.userId)
}

func (store *SSHInteractiveAuthStore) RemoveAuth(userId, authId string) {
	store.lock.Lock()
	defer store.lock.Unlock()

	userAuths, ok := store.Auths[userId]
	if !ok {
		return
	}

	if auth, ok := userAuths[authId]; ok {
		if !auth.Answered() {
			_ = auth.SetResponse(nil, errAuthWithdrawn) // Mark as answered (true) to unblock subscribers waiting for this auth
		}
		delete(userAuths, authId)
	}

	if len(userAuths) == 0 {
		delete(store.Auths, userId)
	}
	store.resetSubscriberQueue(userId)
}

// NOTE: This does not have its own locking; it assumes the caller has locked the store.
func (store *SSHInteractiveAuthStore) resetSubscriberQueue(userId string) {
	if ch, ok := store.SubscriberQueue[userId]; ok {
		close(ch)
		delete(store.SubscriberQueue, userId)
	}
	store.SubscriberQueue[userId] = make(chan struct{})
}

func (store *SSHInteractiveAuthStore) ListAuthsForUser(userId, connection string) []*SSHInteractiveAuth {
	store.lock.Lock()
	defer store.lock.Unlock()

	userAuths, ok := store.Auths[userId]
	if !ok {
		return nil
	}
	auths := make([]*SSHInteractiveAuth, 0, len(userAuths))
	for _, auth := range userAuths {
		if auth.connection == connection {
			auths = append(auths, auth)
		}
	}
	return auths
}

// errAuthWithdrawn is what an auth removed before it was answered resolves with.
var errAuthWithdrawn = errors.New("interactive auth withdrawn")

// This is for retrieving the earliest created interactive auth for a user.
// It only returns auths that have not been answered yet, or nil if there are none.
// The returned channel closes the next time the user's auths are added or removed.
// Both are taken under one lock, so a change right after the call is never missed.
func (store *SSHInteractiveAuthStore) NextAuthForUser(userId, connection string) (*SSHInteractiveAuth, <-chan struct{}) {
	store.lock.Lock()
	defer store.lock.Unlock()

	ch, ok := store.SubscriberQueue[userId]
	if !ok {
		ch = make(chan struct{})
		store.SubscriberQueue[userId] = ch
	}

	earliest := (*SSHInteractiveAuth)(nil)
	for _, auth := range store.Auths[userId] {
		if (earliest == nil || auth.CreatedAt.Before(earliest.CreatedAt)) && auth.Answered() == false && auth.connection == connection {
			earliest = auth
		}
	}
	return earliest, ch
}
