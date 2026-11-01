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

package confirm

import (
	"crypto/aes"
	"crypto/cipher"
	"crypto/rand"
	"crypto/sha256"
	"encoding/base64"
	"encoding/hex"
	"encoding/json"
	"errors"
	"fmt"
	"time"

	"github.com/google/jsonschema-go/jsonschema"
	"github.com/modelcontextprotocol/go-sdk/jsonrpc"
	"github.com/modelcontextprotocol/go-sdk/mcp"
)

// StateTTL bounds how long a confirmation request may be answered.
const StateTTL = 10 * time.Minute

const inputKey = "airavata_input"

// Field is one value shown in the form that the user may edit before confirming.
type Field struct {
	Name        string
	Title       string
	Description string
	Type        string // "integer", "number", "string" or "boolean"
	Default     any
	Minimum     *float64
	Maximum     *float64
	Enum        []string
}

// Values are the answers read back from an accepted form.
type Values map[string]any

// Int returns an integer answer, or def when the field was not answered.
func (v Values) Int(name string, def int64) int64 {
	switch x := v[name].(type) {
	case float64:
		return int64(x)
	case int64:
		return x
	case int:
		return int64(x)
	case json.Number:
		if n, err := x.Int64(); err == nil {
			return n
		}
	}
	return def
}

// String returns a string answer, or def when the field was not answered.
func (v Values) String(name, def string) string {
	if s, ok := v[name].(string); ok && s != "" {
		return s
	}
	return def
}

// Asker issues and checks elicitations through multi round-trip requests.
type Asker struct {
	aead cipher.AEAD
	now  func() time.Time
}

func New() (*Asker, error) {
	key := make([]byte, 32)
	if _, err := rand.Read(key); err != nil {
		return nil, err
	}
	block, err := aes.NewCipher(key)
	if err != nil {
		return nil, err
	}
	aead, err := cipher.NewGCM(block)
	if err != nil {
		return nil, err
	}
	return &Asker{aead: aead, now: time.Now}, nil
}

type sealed struct {
	Principal string `json:"p"`
	Tool      string `json:"t"`
	ArgsHash  string `json:"h"`
	Expires   int64  `json:"e"`
	Confirm   bool   `json:"c"`
}

// ErrDeclined is returned when the user explicitly declines.
var ErrDeclined = errors.New("the user declined. Do not retry this action with different arguments to get around the decision; ask the user what they want instead")

// ErrCancelled is returned when the form was dismissed or could not be shown, without an explicit choice.
var ErrCancelled = errors.New("the confirmation was dismissed or could not be shown, so nothing was done; the user did not decline. " +
	"Ask the user whether to try again; if they saw no form, their client may not be able to show confirmations")

// Confirm asks the user to confirm a write; a non-nil result means the answer is pending.
func (a *Asker) Confirm(req *mcp.CallToolRequest, principal, message string, fields []Field) (Values, *mcp.CallToolResult, error) {
	return a.ask(req, principal, message, fields, true)
}

// Choose asks the user to pick values (for example an enum) without confirming a write.
func (a *Asker) Choose(req *mcp.CallToolRequest, principal, message string, fields []Field) (Values, *mcp.CallToolResult, error) {
	return a.ask(req, principal, message, fields, false)
}

// Pending reports whether this call carries an answer to an earlier elicitation.
func Pending(req *mcp.CallToolRequest) bool {
	return req.Params != nil && len(req.Params.InputResponses) > 0
}

func (a *Asker) ask(req *mcp.CallToolRequest, principal, message string, fields []Field, needConfirm bool) (Values, *mcp.CallToolResult, error) {
	argsHash, err := hashArgs(req.Params.Arguments)
	if err != nil {
		return nil, nil, err
	}
	if !Pending(req) {
		if err := requireElicitation(req); err != nil {
			return nil, nil, err
		}
		state, err := a.seal(sealed{Principal: principal, Tool: req.Params.Name, ArgsHash: argsHash,
			Expires: a.now().Add(StateTTL).Unix(), Confirm: needConfirm})
		if err != nil {
			return nil, nil, err
		}
		return nil, &mcp.CallToolResult{
			InputRequests: mcp.InputRequestMap{inputKey: &mcp.ElicitParams{
				Mode:            "form",
				Message:         message,
				RequestedSchema: schema(fields),
			}},
			RequestState: state,
		}, nil
	}

	st, err := a.open(req.Params.RequestState)
	if err != nil {
		return nil, nil, err
	}
	switch {
	case st.Principal != principal:
		return nil, nil, errors.New("this confirmation was issued to a different user")
	case st.Tool != req.Params.Name || st.ArgsHash != argsHash:
		return nil, nil, errors.New("the arguments changed after the user was asked; call the tool again to ask again")
	case a.now().Unix() > st.Expires:
		return nil, nil, errors.New("the confirmation expired; call the tool again to ask again")
	case st.Confirm != needConfirm:
		return nil, nil, errors.New("this answer belongs to a different question")
	}
	res, ok := req.Params.InputResponses[inputKey].(*mcp.ElicitResult)
	if !ok || res == nil {
		return nil, nil, errors.New("the client did not return the user's answer")
	}
	switch res.Action {
	case "accept":
	case "cancel":
		return nil, nil, ErrCancelled
	default:
		return nil, nil, ErrDeclined
	}
	values := Values(res.Content)
	if values == nil {
		values = Values{}
	}
	return values, nil, nil
}

func requireElicitation(req *mcp.CallToolRequest) error {
	caps := req.ClientCapabilities()
	if caps != nil && caps.Elicitation != nil && (caps.Elicitation.Form != nil || caps.Elicitation.URL == nil) {
		return nil
	}
	const msg = "this action needs the user's confirmation, and the client does not support elicitation"
	if req.ProtocolVersion() >= "2026-07-28" {
		data, _ := json.Marshal(mcp.MissingRequiredClientCapabilityData{
			RequiredCapabilities: &mcp.ClientCapabilities{
				Elicitation: &mcp.ElicitationCapabilities{Form: &mcp.FormElicitationCapabilities{}},
			},
		})
		return &jsonrpc.Error{Code: mcp.CodeMissingRequiredClientCapabilities, Message: msg, Data: data}
	}
	return errors.New(msg + ": use a client that supports elicitation, or an airavata-mcp policy that does not require confirmation for this tool")
}

func schema(fields []Field) *jsonschema.Schema {
	s := &jsonschema.Schema{Type: "object", Properties: map[string]*jsonschema.Schema{}}
	for _, f := range fields {
		p := &jsonschema.Schema{Type: f.Type, Title: f.Title, Description: f.Description, Minimum: f.Minimum, Maximum: f.Maximum}
		if f.Default != nil {
			if b, err := json.Marshal(f.Default); err == nil {
				p.Default = b
			}
		}
		for _, e := range f.Enum {
			p.Enum = append(p.Enum, e)
		}
		s.Properties[f.Name] = p
	}
	return s
}

// hashArgs canonicalises the arguments (sorted keys) before hashing them.
func hashArgs(raw json.RawMessage) (string, error) {
	var v any
	if len(raw) > 0 {
		if err := json.Unmarshal(raw, &v); err != nil {
			return "", fmt.Errorf("arguments are not valid JSON: %w", err)
		}
	}
	canon, err := json.Marshal(v)
	if err != nil {
		return "", err
	}
	sum := sha256.Sum256(canon)
	return hex.EncodeToString(sum[:]), nil
}

func (a *Asker) seal(s sealed) (string, error) {
	plain, err := json.Marshal(s)
	if err != nil {
		return "", err
	}
	nonce := make([]byte, a.aead.NonceSize())
	if _, err := rand.Read(nonce); err != nil {
		return "", err
	}
	return base64.RawURLEncoding.EncodeToString(a.aead.Seal(nonce, nonce, plain, nil)), nil
}

func (a *Asker) open(state string) (*sealed, error) {
	invalid := errors.New("the confirmation state is invalid; call the tool again to ask again")
	raw, err := base64.RawURLEncoding.DecodeString(state)
	if err != nil || len(raw) < a.aead.NonceSize() {
		return nil, invalid
	}
	plain, err := a.aead.Open(nil, raw[:a.aead.NonceSize()], raw[a.aead.NonceSize():], nil)
	if err != nil {
		return nil, invalid
	}
	var s sealed
	if err := json.Unmarshal(plain, &s); err != nil {
		return nil, invalid
	}
	return &s, nil
}
