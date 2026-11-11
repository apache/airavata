/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
 */

package service

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

	"github.com/apache/airavata/internal/auth"
	"github.com/apache/airavata/internal/httpx"
	"github.com/apache/airavata/internal/link"

	dto "github.com/apache/airavata/api/process/dto"
	model "github.com/apache/airavata/api/process/model"
	"github.com/apache/airavata/api/process/repository"
)

// LinkService makes Airavata the control plane a job's Linkspan links to, in the role
// cs-plane plays for CyberShuttle: it issues each submission its link and Jupyter
// tokens, admits the WebSocket that offers the link token, and opens the job's Jupyter
// Server to the process owner through a proxy.
type LinkService struct {
	links     *repository.LinkRepository
	processes *repository.ProcessRepository
	registry  *link.Registry
	publicURL string
}

// NewLinkService returns a link service. publicURL is where jobs reach this server,
// http(s)://host[:port]; the link URL is its ws(s) form.
func NewLinkService(links *repository.LinkRepository, processes *repository.ProcessRepository, registry *link.Registry, publicURL string) *LinkService {
	return &LinkService{links: links, processes: processes, registry: registry, publicURL: strings.TrimRight(publicURL, "/")}
}

// Registry is the live links this service admits.
func (s *LinkService) Registry() *link.Registry { return s.registry }

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
func (s *LinkService) Issue(ctx context.Context, processID string) (*link.Issued, error) {
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
	control, jupyter := link.Ports(processID)
	if err := s.links.Save(ctx, &model.ProcessLink{
		ProcessID:     processID,
		LinkTokenHash: digest(linkToken),
		JupyterToken:  jupyterToken,
		ControlPort:   int32(control),
		JupyterPort:   int32(jupyter),
		IssuedAt:      time.Now().UnixMilli(),
	}); err != nil {
		return nil, err
	}
	return &link.Issued{URL: linkURL, LinkToken: linkToken, JupyterToken: jupyterToken, ControlPort: control, JupyterPort: jupyter}, nil
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

// JupyterServer returns the caller's access to their process's Jupyter Server. Only
// the owner may have it: the token runs code as the owner's cluster account.
func (s *LinkService) JupyterServer(ctx context.Context, processID string) (*dto.JupyterServerResponse, error) {
	principal, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return nil, err
	}
	process, err := s.processes.FindByID(ctx, processID)
	if err != nil {
		return nil, notFoundAs(err, "Process %s not found", processID)
	}
	if !process.OwnedBy(principal.Name) {
		return nil, httpx.Forbidden("Only the owner of process %s can open its Jupyter Server", processID)
	}
	found, err := s.links.FindByProcessID(ctx, processID)
	if err != nil {
		return nil, notFoundAs(err, "Process %s has not been submitted, so it has no Jupyter Server", processID)
	}
	return &dto.JupyterServerResponse{
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
