package ssh

import (
	"context"
	"fmt"
	"time"
)

// deferredDialTimeout bounds a login left running after its caller stopped waiting on
// it. The server's own LoginGraceTime usually ends an unanswered login well before
// this; the bound is for one that never does, so the dial cannot outlive it for good.
const deferredDialTimeout = 30 * time.Minute

// InteractiveAuthRequiredError is what a dial under WithDeferredInteractiveAuth returns
// once the host asks for a password or OTP. The login carries on in the background,
// published to the dialing user as a pending interactive auth, so that once it is
// answered the connection is in the pool for the caller's next attempt.
type InteractiveAuthRequiredError struct {
	Host      string
	Port      int
	LoginUser string
	AuthID    string
}

func (e *InteractiveAuthRequiredError) Error() string {
	return fmt.Sprintf("%s@%s:%d is waiting for an interactive login (auth %s)", e.LoginUser, e.Host, e.Port, e.AuthID)
}

type deferredAuthKey struct{}

// WithDeferredInteractiveAuth marks ctx so that a dial made under it does not wait for
// its owner to answer an interactive login, but returns InteractiveAuthRequiredError as
// soon as one is asked for. It is meant for workflow activities, which should hand the
// wait back to the workflow rather than hold a worker for as long as it takes.
func WithDeferredInteractiveAuth(ctx context.Context) context.Context {
	return context.WithValue(ctx, deferredAuthKey{}, true)
}

func defersInteractiveAuth(ctx context.Context) bool {
	deferred, _ := ctx.Value(deferredAuthKey{}).(bool)
	return deferred
}

type authPromptKey struct{}

// withAuthPromptHook has askUserInteractive call hook with the id of every auth it
// publishes under ctx, which is how the pool learns a dial is waiting on its owner.
func withAuthPromptHook(ctx context.Context, hook func(authID string)) context.Context {
	return context.WithValue(ctx, authPromptKey{}, hook)
}

func notifyAuthPrompt(ctx context.Context, authID string) {
	if hook, ok := ctx.Value(authPromptKey{}).(func(string)); ok {
		hook(authID)
	}
}
