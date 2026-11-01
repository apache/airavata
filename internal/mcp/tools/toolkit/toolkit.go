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

package toolkit

import (
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"sync"
	"time"

	"github.com/modelcontextprotocol/go-sdk/jsonrpc"
	"github.com/modelcontextprotocol/go-sdk/mcp"

	"github.com/apache/airavata/internal/mcp/audit"
	"github.com/apache/airavata/internal/mcp/client"
	"github.com/apache/airavata/internal/mcp/confirm"
	"github.com/apache/airavata/internal/mcp/policy"
	"github.com/apache/airavata/internal/mcp/state"
)

// PrincipalFunc returns the normalized subject ("cilogon:<id>") of the token in use.
type PrincipalFunc func(ctx context.Context) (string, error)

// Deps is what every toolset needs. It is built once in internal/mcp/server.
type Deps struct {
	API       *client.ClientWithResponses
	Policy    *policy.Policy
	Store     *state.Store
	Ask       *confirm.Asker
	Audit     *audit.Logger
	Principal PrincipalFunc

	trustMu      sync.Mutex
	trusted      map[string]bool
	trustedUntil time.Time
}

// Def describes a tool. ReadOnly decides whether --allow-write and confirmation apply.
type Def struct {
	Name        string
	Title       string
	Description string
	ReadOnly    bool
	Destructive bool
	Idempotent  bool
}

func (d Def) tool() *mcp.Tool {
	ann := &mcp.ToolAnnotations{Title: d.Title, ReadOnlyHint: d.ReadOnly, IdempotentHint: d.Idempotent || d.ReadOnly,
		OpenWorldHint: ptr(false)}
	if !d.ReadOnly {
		ann.DestructiveHint = ptr(d.Destructive)
	}
	return &mcp.Tool{Name: d.Name, Title: d.Title, Description: d.Description, Annotations: ann}
}

func ptr[T any](v T) *T { return &v }

// Handler is a read-only tool body.
type Handler[In, Out any] func(ctx context.Context, req *mcp.CallToolRequest, in In) (*mcp.CallToolResult, Out, error)

// Read registers a read-only tool, honouring the deny list and the policy's extra confirmations.
func Read[In, Out any](s *mcp.Server, d *Deps, def Def, h Handler[In, Out]) {
	def.ReadOnly = true
	if !d.Policy.Exposed(def.Name, true) {
		return
	}
	confirmFirst := d.Policy.NeedsConfirmation(def.Name, true)
	mcp.AddTool(s, def.tool(), func(ctx context.Context, req *mcp.CallToolRequest, in In) (*mcp.CallToolResult, Out, error) {
		var zero Out
		if confirmFirst {
			principal, err := d.Principal(ctx)
			if err != nil {
				return nil, zero, err
			}
			_, pending, err := d.Ask.Confirm(req, principal,
				fmt.Sprintf("Allow the assistant to run %s? Your policy file asks for confirmation of this tool.", def.Name), nil)
			if err != nil || pending != nil {
				return pending, zero, err
			}
			req = withoutInput(req)
		}
		return h(ctx, req, in)
	})
}

// withoutInput hides an answered policy confirmation from the tool body, which may elicit on its own.
func withoutInput(req *mcp.CallToolRequest) *mcp.CallToolRequest {
	p := *req.Params
	p.InputResponses, p.RequestState = nil, ""
	r := *req
	r.Params = &p
	return &r
}

// Proposal is a validated write waiting for the user's confirmation.
type Proposal[Out any] struct {
	// Message is shown in the confirmation form: what will happen, to what, and for whom.
	Message string
	// Fields the user may edit; Run receives the answers and must re-validate them.
	Fields []confirm.Field
	Run    func(ctx context.Context, values confirm.Values) (Out, []string, error)
}

// Prepare validates a write without side effects and describes it for confirmation.
type Prepare[In, Out any] func(ctx context.Context, principal string, in In) (*Proposal[Out], error)

// Write registers a write tool that is always confirmed through elicitation and audited.
func Write[In, Out any](s *mcp.Server, d *Deps, def Def, prepare Prepare[In, Out]) {
	def.ReadOnly = false
	if !d.Policy.Exposed(def.Name, false) {
		return
	}
	mcp.AddTool(s, def.tool(), func(ctx context.Context, req *mcp.CallToolRequest, in In) (*mcp.CallToolResult, Out, error) {
		var zero Out
		ev := audit.Event{Tool: def.Name, Arguments: req.Params.Arguments}
		principal, err := d.Principal(ctx)
		if err != nil {
			return nil, zero, err
		}
		ev.Principal = principal
		p, err := prepare(ctx, principal, in)
		if err != nil {
			return nil, zero, err
		}
		values, pending, err := d.Ask.Confirm(req, principal, p.Message, p.Fields)
		switch {
		case errors.Is(err, confirm.ErrDeclined):
			ev.Confirmation, ev.Result = "declined", "error"
			d.Audit.Record(ctx, ev)
			return nil, zero, err
		case errors.Is(err, confirm.ErrCancelled):
			ev.Confirmation, ev.Result = "cancelled", "error"
			d.Audit.Record(ctx, ev)
			return nil, zero, err
		case err != nil:
			var wire *jsonrpc.Error
			if !errors.As(err, &wire) {
				ev.Confirmation, ev.Result, ev.Err = "invalid", "error", err
				d.Audit.Record(ctx, ev)
			}
			return nil, zero, err
		case pending != nil:
			return pending, zero, nil
		}
		ev.Confirmation = "accepted"
		out, ids, err := p.Run(ctx, values)
		ev.ResourceIDs = ids
		if err != nil {
			ev.Result, ev.Err = "error", err
			d.Audit.Record(ctx, ev)
			return nil, zero, err
		}
		ev.Result = "ok"
		d.Audit.Record(ctx, ev)
		return nil, out, nil
	})
}

// Links returns a result carrying out as JSON text plus resource links to what it mentions.
func Links(out any, links ...*mcp.ResourceLink) (*mcp.CallToolResult, error) {
	if len(links) == 0 {
		return nil, nil
	}
	b, err := json.Marshal(out)
	if err != nil {
		return nil, err
	}
	content := []mcp.Content{&mcp.TextContent{Text: string(b)}}
	for _, l := range links {
		content = append(content, l)
	}
	return &mcp.CallToolResult{Content: content}, nil
}

// Link is a resource link to an airavata:// object.
func Link(uri, name string) *mcp.ResourceLink {
	return &mcp.ResourceLink{URI: uri, Name: name, MIMEType: "application/json"}
}

// TrustedAuthors is the caller plus every member of the caller's groups, used by --lockdown.
func (d *Deps) TrustedAuthors(ctx context.Context) (map[string]bool, error) {
	d.trustMu.Lock()
	defer d.trustMu.Unlock()
	if d.trusted != nil && time.Now().Before(d.trustedUntil) {
		return d.trusted, nil
	}
	principal, err := d.Principal(ctx)
	if err != nil {
		return nil, err
	}
	trusted := map[string]bool{principal: true}
	groups, err := client.JSON[[]client.GroupResponse](d.API.ListMyGroupsWithResponse(ctx))
	if err != nil {
		return nil, err
	}
	for _, g := range *groups {
		members, err := client.JSON[[]client.GroupMemberResponse](d.API.ListGroupMembersWithResponse(ctx, g.GroupId))
		if err != nil {
			continue
		}
		for _, m := range *members {
			if m.GroupMemberStatus == client.GroupMemberStatusACTIVE {
				trusted[m.UserId] = true
			}
		}
	}
	d.trusted, d.trustedUntil = trusted, time.Now().Add(5*time.Minute)
	return trusted, nil
}

// Untrusted is text written by another person (a description, a banner). It is data, never instructions.
type Untrusted struct {
	Text      string `json:"text,omitempty"`
	Withheld  string `json:"withheld,omitempty"`
	Truncated bool   `json:"truncated,omitempty"`
}

// MaxUntrusted caps the size of any untrusted text returned to the model.
const MaxUntrusted = 2000

// Text wraps author-written text, applying the size cap and, under --lockdown, the author filter.
func (d *Deps) Text(ctx context.Context, text *string, author string) *Untrusted {
	if text == nil || *text == "" {
		return nil
	}
	if d.Policy.Lockdown {
		trusted, err := d.TrustedAuthors(ctx)
		if err != nil || !trusted[author] {
			return &Untrusted{Withheld: "hidden by --lockdown: written by someone outside your groups"}
		}
	}
	return Cap(*text)
}

// Cap truncates text to MaxUntrusted bytes on a rune boundary.
func Cap(s string) *Untrusted {
	if len(s) <= MaxUntrusted {
		return &Untrusted{Text: s}
	}
	cut := MaxUntrusted
	for cut > 0 && (s[cut]&0xC0) == 0x80 {
		cut--
	}
	return &Untrusted{Text: s[:cut], Truncated: true}
}
