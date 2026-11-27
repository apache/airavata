package linkspan

import (
	"context"
	"crypto/rand"
	"crypto/sha256"
	"crypto/subtle"
	"encoding/base64"
	"encoding/hex"
	"fmt"
	"net/url"
	"strings"
	"time"

	"github.com/apache/airavata/api/process/repository"
)

// LinkService makes Airavata the control plane a job's Linkspan links to, in the role
// cs-plane plays for CyberShuttle: it issues each submission its link and Jupyter
// tokens, admits the WebSocket that offers the link token, and opens the job's Jupyter
// Server to the process owner through a proxy.
type LinkService struct {
	links     *LinkRepository
	processes *repository.ProcessRepository
	registry  *Registry
	publicURL string
}

// NewLinkService returns a link service. publicURL is where jobs reach this server,
// http(s)://host[:port]; the link URL is its ws(s) form.
func NewLinkService(links *LinkRepository, processes *repository.ProcessRepository, registry *Registry, publicURL string) *LinkService {
	return &LinkService{links: links, processes: processes, registry: registry, publicURL: strings.TrimRight(publicURL, "/")}
}

// Registry is the live links this service admits.
func (s *LinkService) Registry() *Registry { return s.registry }

// LinkURL is the WebSocket URL a process's Linkspan dials.
func (s *LinkService) LinkURL(processID string) (string, error) {
	u, err := url.Parse(s.publicURL)
	if err != nil || u.Host == "" {
		return "", fmt.Errorf("public URL %q is not an absolute http(s) URL", s.publicURL)
	}
	switch u.Scheme {
	case "http":
		u.Scheme = "ws"
	case "https":
		u.Scheme = "wss"
	default:
		return "", fmt.Errorf("public URL %q is not an absolute http(s) URL", s.publicURL)
	}
	return u.JoinPath("api/v1/processes", url.PathEscape(processID), "link").String(), nil
}

// Issue mints the link and Jupyter tokens for a submission of the process, replacing
// any issued before, and returns what the job is handed.
func (s *LinkService) Issue(ctx context.Context, processID string) (*Issued, error) {
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
	return &Issued{URL: linkURL, LinkToken: linkToken, JupyterToken: jupyterToken, ControlPort: control, JupyterPort: jupyter}, nil
}

// Authenticate reports whether token is the link token last issued to the process.
func (s *LinkService) Authenticate(ctx context.Context, processID, token string) bool {
	if token == "" {
		return false
	}
	found, err := s.links.FindByProcessID(ctx, processID)
	if err != nil {
		return false
	}
	return subtle.ConstantTimeCompare([]byte(digest(token)), []byte(found.LinkTokenHash)) == 1
}

func newSecret() (string, error) {
	b := make([]byte, 32)
	if _, err := rand.Read(b); err != nil {
		return "", err
	}
	return base64.RawURLEncoding.EncodeToString(b), nil
}

func digest(token string) string {
	sum := sha256.Sum256([]byte(token))
	return hex.EncodeToString(sum[:])
}
