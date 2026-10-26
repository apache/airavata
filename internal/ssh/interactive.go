package ssh

import "context"

type Prompt struct {
	Text string `json:"text"`
	Echo bool   `json:"echo"` // false => secret (password/OTP), render masked
}

type Challenger interface {
	Challenge(ctx context.Context, name, instruction string, prompts []Prompt) ([]string, error)
}

type InteractiveSession struct {
}

func (s *InteractiveSession) Challenge(ctx context.Context, name, instruction string, prompts []Prompt) ([]string, error) {
	// Implement the logic to handle the challenge here.
	// This is a placeholder and should be replaced with actual handling code.
	return nil, nil
}
