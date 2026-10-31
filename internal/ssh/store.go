package ssh

import "sync"

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
}

func newSSHInteractiveAuthStore() *SSHInteractiveAuthStore {
	return &SSHInteractiveAuthStore{
		Auths: make(map[string]map[string]*SSHInteractiveAuth),
	}
}

func (store *SSHInteractiveAuthStore) GetAuth(userId, authId string) (*SSHInteractiveAuth, bool) {
	store.lock.Lock()
	defer store.lock.Unlock()

	userAuths, ok := store.Auths[userId]
	if !ok {
		return nil, false
	}
	auth, ok := userAuths[authId]
	return auth, ok
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
}

func (store *SSHInteractiveAuthStore) RemoveAuth(userId, authId string) {
	store.lock.Lock()
	defer store.lock.Unlock()

	userAuths, ok := store.Auths[userId]
	if !ok {
		return
	}
	delete(userAuths, authId)
	if len(userAuths) == 0 {
		delete(store.Auths, userId)
	}
}

func (store *SSHInteractiveAuthStore) ListAuthsForUser(userId string) []*SSHInteractiveAuth {
	store.lock.Lock()
	defer store.lock.Unlock()

	userAuths, ok := store.Auths[userId]
	if !ok {
		return nil
	}
	auths := make([]*SSHInteractiveAuth, 0, len(userAuths))
	for _, auth := range userAuths {
		auths = append(auths, auth)
	}
	return auths
}
