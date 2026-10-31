package ssh

import (
	"context"
	"errors"
	"sync"
	"time"

	"github.com/google/uuid"
)

// askUser publishes prompts as a pending auth for the dialing user and waits for
// their answers. The auth leaves the store once answered or abandoned. banner is
// the server's pre-login banner, if it sent one, so the user sees what a terminal would.
func askUser(ctx context.Context, dialConfig SSHDialConfig, sshContext *GlobalSSHContext, banner, name, instruction string, prompts []Prompt) ([]string, error) {
	auth := NewSSHInteractiveAuth(uuid.New().String(), dialConfig)
	auth.banner = banner
	sshContext.InteractiveAuthStore.AddAuth(auth)
	defer sshContext.InteractiveAuthStore.RemoveAuth(dialConfig.userId, auth.AuthId)
	return auth.Challenge(ctx, name, instruction, prompts)
}

type Prompt struct {
	Text string `json:"text"`
	Echo bool   `json:"echo"` // false => secret (password/OTP), render masked
}

type Challenger interface {
	Challenge(ctx context.Context, name, instruction string, prompts []Prompt) ([]string, error)
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

type SSHInteractiveAuth struct {
	AuthId string

	CreatedAt time.Time

	dialConfig SSHDialConfig

	// banner is set before the auth is published and never changes, so it needs
	// no lock.
	banner string

	// mu guards the challenge fields: Challenge writes them on the dialing goroutine
	// while an HTTP handler reads them to list the auth.
	mu          sync.Mutex
	name        string
	prompts     []Prompt
	instruction string
	response    []string
	authErr     error
	answered    bool

	authWaitSignal chan struct{}
}

func NewSSHInteractiveAuth(authId string, dialConfig SSHDialConfig) *SSHInteractiveAuth {
	return &SSHInteractiveAuth{
		AuthId:               authId,
		CreatedAt:            time.Now(),
		dialConfig:           dialConfig,
		authWaitSignal:       make(chan struct{}),
	}
}

func (s *SSHInteractiveAuth) Challenge(ctx context.Context, name, instruction string, prompts []Prompt) ([]string, error) {
	s.mu.Lock()
	s.name = name
	s.instruction = instruction
	s.prompts = prompts
	s.authErr = nil
	s.mu.Unlock()

	// Give up when the dial is abandoned, or the goroutine waits forever for an
	// answer nobody is going to send.
	select {
	case <-s.authWaitSignal:
	case <-ctx.Done():
		return nil, ctx.Err()
	}

	s.mu.Lock()
	defer s.mu.Unlock()
	//slog.Info("Received interactive response for auth ", "authId", s.AuthId, "response", s.response, "error", s.authErr)
	return s.response, s.authErr
}

// ErrAuthAnswered is returned when an auth that already has a response is
// answered again, say from a second browser tab.
var ErrAuthAnswered = errors.New("interactive auth has already been answered")

// Should be set from the websocket server. Only the first call wins: closing
// authWaitSignal twice would panic.
func (s *SSHInteractiveAuth) SetResponse(response []string, err error) error {
	s.mu.Lock()
	defer s.mu.Unlock()
	if s.answered {
		return ErrAuthAnswered
	}
	s.answered = true
	s.response = response
	s.authErr = err
	close(s.authWaitSignal)
	return nil
}

func (s *SSHInteractiveAuth) Read() ([]Prompt, string) {
	s.mu.Lock()
	defer s.mu.Unlock()
	return s.prompts, s.instruction
}

func (s *SSHInteractiveAuth) Name() string {
	s.mu.Lock()
	defer s.mu.Unlock()
	return s.name
}

// Banner is the text the server sent before login ("NCSA Delta System ..."), if any.
func (s *SSHInteractiveAuth) Banner() string {
	return s.banner
}

func (s *SSHInteractiveAuth) Target() (host string, port int, loginUser, credentialID string) {
	c := s.dialConfig
	return c.host, c.port, c.sshUsername, c.cred.ID
}

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
