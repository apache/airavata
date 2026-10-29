package ssh

import (
	"context"
	"errors"
	"log/slog"
	"sync"
	"time"
)

type Prompt struct {
	Text string `json:"text"`
	Echo bool   `json:"echo"` // false => secret (password/OTP), render masked
}

type Challenger interface {
	Challenge(ctx context.Context, name, instruction string, prompts []Prompt) ([]string, error)
}

type SSHInteractiveSessionStore struct {
	// List of interactive SSH sessions keyed by user ID, then by session ID
	Sessions map[string]map[string]*SSHInteractiveSession
	lock     sync.Mutex
}

func newSSHInteractiveSessionStore() *SSHInteractiveSessionStore {
	return &SSHInteractiveSessionStore{
		Sessions: make(map[string]map[string]*SSHInteractiveSession),
	}
}

func (store *SSHInteractiveSessionStore) GetSession(userId, sessionId string) (*SSHInteractiveSession, bool) {
	store.lock.Lock()
	defer store.lock.Unlock()

	userSessions, ok := store.Sessions[userId]
	if !ok {
		return nil, false
	}
	session, ok := userSessions[sessionId]
	return session, ok
}

func (store *SSHInteractiveSessionStore) AddSession(session *SSHInteractiveSession) {
	store.lock.Lock()
	defer store.lock.Unlock()

	userSessions, ok := store.Sessions[session.dialConfig.userId]
	if !ok {
		userSessions = make(map[string]*SSHInteractiveSession)
		store.Sessions[session.dialConfig.userId] = userSessions
	}
	userSessions[session.SessionId] = session
}

func (store *SSHInteractiveSessionStore) RemoveSession(userId, sessionId string) {
	store.lock.Lock()
	defer store.lock.Unlock()

	userSessions, ok := store.Sessions[userId]
	if !ok {
		return
	}
	delete(userSessions, sessionId)
	if len(userSessions) == 0 {
		delete(store.Sessions, userId)
	}
}

func (store *SSHInteractiveSessionStore) ListSessionsForUser(userId string) []*SSHInteractiveSession {
	store.lock.Lock()
	defer store.lock.Unlock()

	userSessions, ok := store.Sessions[userId]
	if !ok {
		return nil
	}
	sessions := make([]*SSHInteractiveSession, 0, len(userSessions))
	for _, session := range userSessions {
		sessions = append(sessions, session)
	}
	return sessions
}

type SSHInteractiveSession struct {
	SessionId string

	CreatedAt time.Time

	dialConfig SSHDialConfig

	// mu guards the challenge fields: Challenge writes them on the dialing goroutine
	// while an HTTP handler reads them to list the session.
	mu          sync.Mutex
	name        string
	prompts     []Prompt
	instruction string
	response    []string
	sessionErr  error
	answered    bool

	sessionWaitSignal chan struct{}

	subscriberWaitGroup  sync.WaitGroup
	subscriberWaitSignal chan struct{}
}

func NewSSHInteractiveSession(sessionId string, dialConfig SSHDialConfig) *SSHInteractiveSession {
	return &SSHInteractiveSession{
		SessionId:            sessionId,
		CreatedAt:            time.Now(),
		dialConfig:           dialConfig,
		subscriberWaitSignal: make(chan struct{}),
		sessionWaitSignal:    make(chan struct{}),
	}
}

func (s *SSHInteractiveSession) Challenge(ctx context.Context, name, instruction string, prompts []Prompt) ([]string, error) {
	s.mu.Lock()
	s.name = name
	s.instruction = instruction
	s.prompts = prompts
	s.sessionErr = nil
	s.mu.Unlock()

	// Give up when the dial is abandoned, or the goroutine waits forever for an
	// answer nobody is going to send.
	select {
	case <-s.sessionWaitSignal:
	case <-ctx.Done():
		return nil, ctx.Err()
	}

	s.mu.Lock()
	defer s.mu.Unlock()
	//slog.Info("Received interactive response for session ", "sessionId", s.SessionId, "response", s.response, "error", s.sessionErr)
	return s.response, s.sessionErr
}

func (s *SSHInteractiveSession) Done() {
	// Only call done when the ssh client created and pushed to the pool.
	// This will release all the pending client requests waiting on this session.
	close(s.subscriberWaitSignal)
}

// ErrSessionAnswered is returned when a session that already has a response is
// answered again, say from a second browser tab.
var ErrSessionAnswered = errors.New("interactive session has already been answered")

// Should be set from the websocket server. Only the first call wins: closing
// sessionWaitSignal twice would panic.
func (s *SSHInteractiveSession) SetResponse(response []string, err error) error {
	s.mu.Lock()
	defer s.mu.Unlock()
	if s.answered {
		return ErrSessionAnswered
	}
	s.answered = true
	s.response = response
	s.sessionErr = err
	close(s.sessionWaitSignal)
	return nil
}

func (s *SSHInteractiveSession) Read() ([]Prompt, string) {
	s.mu.Lock()
	defer s.mu.Unlock()
	return s.prompts, s.instruction
}

func (s *SSHInteractiveSession) Name() string {
	s.mu.Lock()
	defer s.mu.Unlock()
	return s.name
}

func (s *SSHInteractiveSession) Target() (host string, port int, loginUser, credentialID string) {
	c := s.dialConfig
	return c.host, c.port, c.sshUsername, c.cred.ID
}

func (s *SSHInteractiveSession) BlockUntilDone() {
	s.subscriberWaitGroup.Add(1)
	defer s.subscriberWaitGroup.Done()
	<-s.subscriberWaitSignal
}

type GlobalSSHContext struct {
	InteractiveSessionStore *SSHInteractiveSessionStore
}

func NewGlobalSSHContext() *GlobalSSHContext {
	return &GlobalSSHContext{
		InteractiveSessionStore: newSSHInteractiveSessionStore(),
	}
}
