package ssh

import (
	"context"
	"sync"
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

	dialConfig SSHDialConfig

	prompts     []Prompt
	instruction string
	response    []string
	sessionErr  error

	sessionWaitSignal chan struct{}

	subscriberWaitGroup  sync.WaitGroup
	subscriberWaitSignal chan struct{}
}

func NewSSHInteractiveSession(sessionId string, dialConfig SSHDialConfig) *SSHInteractiveSession {
	return &SSHInteractiveSession{
		SessionId:            sessionId,
		dialConfig:           dialConfig,
		subscriberWaitSignal: make(chan struct{}),
		sessionWaitSignal:    make(chan struct{}),
	}
}

func (s *SSHInteractiveSession) Challenge(ctx context.Context, name, instruction string, prompts []Prompt) ([]string, error) {
	s.instruction = instruction
	s.prompts = prompts

	s.sessionErr = nil
	<-s.sessionWaitSignal

	return s.response, s.sessionErr
}

func (s *SSHInteractiveSession) Done() {
	// Only call done when the ssh client created and pushed to the pool.
	// This will release all the pending client requests waiting on this session.
	close(s.subscriberWaitSignal)
}

// Should be set from the websocket server
func (s *SSHInteractiveSession) SetResponse(response []string, err error) {
	s.response = response
	s.sessionErr = err
	close(s.sessionWaitSignal)
}

func (s *SSHInteractiveSession) Read() ([]Prompt, string) {
	return s.prompts, s.instruction
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
