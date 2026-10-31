package ssh

import (
	"context"
	"errors"
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

type SSHInteractiveAuth struct {
	AuthId     string
	CreatedAt  time.Time
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

	// Holds challenge until a response is set. Closed when SetResponse is called.
	authWaitSignal chan struct{}
}

func NewSSHInteractiveAuth(authId string, dialConfig SSHDialConfig) *SSHInteractiveAuth {
	return &SSHInteractiveAuth{
		AuthId:         authId,
		CreatedAt:      time.Now(),
		dialConfig:     dialConfig,
		authWaitSignal: make(chan struct{}),
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
