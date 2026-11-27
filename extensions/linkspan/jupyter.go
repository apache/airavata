package linkspan

import (
	"context"
	"crypto/subtle"
	"net/url"

	"github.com/apache/airavata/internal/auth"
	"github.com/apache/airavata/internal/httpx"

	"github.com/apache/airavata/internal/common"
)

type JupyterServerResponse struct {
	ProcessID string `json:"processId"`
	Linked    bool   `json:"linked"`
	BaseURL   string `json:"baseUrl"`
	Token     string `json:"token"`
}

// JupyterServer returns the caller's access to their process's Jupyter Server. Only
// the owner may have it: the token runs code as the owner's cluster account.
func (s *LinkService) JupyterServer(ctx context.Context, processID string) (*JupyterServerResponse, error) {
	principal, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return nil, err
	}
	process, err := s.processes.FindByID(ctx, processID)
	if err != nil {
		return nil, common.NotFoundAs(err, "Process %s not found", processID)
	}
	if !process.OwnedBy(principal.Name) {
		return nil, httpx.Forbidden("Only the owner of process %s can open its Jupyter Server", processID)
	}
	found, err := s.links.FindByProcessID(ctx, processID)
	if err != nil {
		return nil, common.NotFoundAs(err, "Process %s has not been submitted, so it has no Jupyter Server", processID)
	}
	return &JupyterServerResponse{
		ProcessID: processID,
		Linked:    s.registry.Linked(processID),
		BaseURL:   "/api/v1/processes/" + url.PathEscape(processID) + "/jupyter/",
		Token:     found.JupyterToken,
	}, nil
}

// JupyterPort returns the port of the process's Jupyter Server when token is its
// Jupyter token, and false otherwise.
func (s *LinkService) JupyterPort(ctx context.Context, processID, token string) (uint16, bool) {
	if token == "" {
		return 0, false
	}
	found, err := s.links.FindByProcessID(ctx, processID)
	if err != nil || subtle.ConstantTimeCompare([]byte(token), []byte(found.JupyterToken)) != 1 {
		return 0, false
	}
	return uint16(found.JupyterPort), true
}
