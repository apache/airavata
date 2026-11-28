package linkspan

import (
	"context"
	"crypto/subtle"
	"net/url"
	"strconv"

	"github.com/apache/airavata/internal/auth"
	"github.com/apache/airavata/internal/httpx"
	"time"

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

// Issued is what a process's job is handed so its Linkspan can link: where to dial,
// the token to offer, and the ports and Jupyter token its servers use.
type JupyterLinkConfig struct {
	URL          string
	LinkToken    string
	JupyterToken string
	ControlPort  uint16
	JupyterPort  uint16
}

// Env is the job environment that carries Issued, under the names Linkspan reads
// (LINKSPAN_LINK_TOKEN, JUPYTER_TOKEN) and the ones its documented job script uses
// (CS_LINK_URL, CS_CONTROL_PORT), plus CS_JUPYTER_PORT for the workflow's addr.
func (i *JupyterLinkConfig) Env() map[string]string {
	return map[string]string{
		"CS_LINK_URL":         i.URL,
		"CS_CONTROL_PORT":     strconv.Itoa(int(i.ControlPort)),
		"CS_JUPYTER_PORT":     strconv.Itoa(int(i.JupyterPort)),
		"LINKSPAN_LINK_TOKEN": i.LinkToken,
		"JUPYTER_TOKEN":       i.JupyterToken,
	}
}

// Issue mints the link and Jupyter tokens for a submission of the process, replacing
// any issued before, and returns what the job is handed.
func (s *LinkService) IssueJupyterLinkConfig(ctx context.Context, processID string) (*JupyterLinkConfig, error) {
	linkURL, err := s.LinkURL(processID)
	if err != nil {
		return nil, err
	}
	linkToken, err := newSecret()
	if err != nil {
		return nil, err
	}
	jupyterToken, err := newSecret()
	if err != nil {
		return nil, err
	}
	control, jupyter := Ports(processID)
	if err := s.links.Save(ctx, &ProcessLink{
		ProcessID:     processID,
		LinkTokenHash: digest(linkToken),
		JupyterToken:  jupyterToken,
		ControlPort:   int32(control),
		JupyterPort:   int32(jupyter),
		IssuedAt:      time.Now().UnixMilli(),
	}); err != nil {
		return nil, err
	}
	return &JupyterLinkConfig{URL: linkURL, LinkToken: linkToken, JupyterToken: jupyterToken, ControlPort: control, JupyterPort: jupyter}, nil
}
