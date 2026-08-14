package server_test

import (
	"bytes"
	"context"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"

	"github.com/glebarez/sqlite"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"

	"github.com/apache/airavata/internal/auth"
	"github.com/apache/airavata/internal/config"
	"github.com/apache/airavata/internal/db"
	"github.com/apache/airavata/internal/role"
	"github.com/apache/airavata/internal/server"

	computemodel "github.com/apache/airavata/api/compute/model"
	iammodel "github.com/apache/airavata/api/iam/model"
)

// stubIntrospector maps a bearer token straight to a principal, standing in for
// CILogon so the tests exercise the authorisation model rather than the network.
type stubIntrospector map[string]*auth.Principal

func (s stubIntrospector) Introspect(_ context.Context, token string) (*auth.Principal, error) {
	if p, ok := s[token]; ok {
		return p, nil
	}
	return nil, auth.ErrInvalidToken
}

// Tokens used throughout. Each names a seeded user except "bogus".
const (
	tokenAdmin = "token-admin"
	tokenAlice = "token-alice"
	tokenBob   = "token-bob"
	tokenBogus = "token-bogus"
)

type harness struct {
	t   *testing.T
	srv http.Handler
	db  *gorm.DB
}

func newHarness(t *testing.T) *harness {
	t.Helper()

	gdb, err := gorm.Open(sqlite.Open("file::memory:?_pragma=foreign_keys(1)"), &gorm.Config{
		Logger: logger.Default.LogMode(logger.Silent),
	})
	if err != nil {
		t.Fatalf("open sqlite: %v", err)
	}
	if err := db.AutoMigrate(gdb); err != nil {
		t.Fatalf("automigrate: %v", err)
	}

	// Every principal must have a users row: ownership resolution looks the caller up
	// by principal name, and an unregistered caller cannot own anything.
	for _, u := range []struct {
		id string
		r  role.Role
	}{
		{"admin-user", role.Admin},
		{"alice", role.User},
		{"bob", role.User},
	} {
		if err := gdb.Create(&iammodel.User{ID: u.id, CreatedAt: 1}).Error; err != nil {
			t.Fatalf("seed user %s: %v", u.id, err)
		}
		if err := gdb.Create(&iammodel.UserRole{UserID: u.id, Role: u.r}).Error; err != nil {
			t.Fatalf("seed role for %s: %v", u.id, err)
		}
	}

	introspector := stubIntrospector{
		tokenAdmin: {Name: "admin-user", Authorities: []string{string(role.Admin)}},
		tokenAlice: {Name: "alice", Authorities: []string{string(role.User)}},
		tokenBob:   {Name: "bob", Authorities: []string{string(role.User)}},
	}

	return &harness{
		t:   t,
		db:  gdb,
		srv: server.New(config.Config{CORSAllowedOrigins: []string{"*"}}, gdb, introspector),
	}
}

// do issues a request. An empty token means an anonymous caller.
func (h *harness) do(method, path, token string, body any) *httptest.ResponseRecorder {
	h.t.Helper()

	var reader *bytes.Reader
	if body != nil {
		raw, err := json.Marshal(body)
		if err != nil {
			h.t.Fatalf("marshal body: %v", err)
		}
		reader = bytes.NewReader(raw)
	} else {
		reader = bytes.NewReader(nil)
	}

	req := httptest.NewRequest(method, path, reader)
	if body != nil {
		req.Header.Set("Content-Type", "application/json")
	}
	if token != "" {
		req.Header.Set("Authorization", "Bearer "+token)
	}
	rec := httptest.NewRecorder()
	h.srv.ServeHTTP(rec, req)
	return rec
}

// mustDo issues a request and fails unless the status matches.
func (h *harness) mustDo(method, path, token string, body any, wantStatus int) map[string]any {
	h.t.Helper()

	rec := h.do(method, path, token, body)
	if rec.Code != wantStatus {
		h.t.Fatalf("%s %s: status = %d, want %d\nbody: %s", method, path, rec.Code, wantStatus, rec.Body.String())
	}
	if rec.Body.Len() == 0 {
		return nil
	}
	var out map[string]any
	if err := json.Unmarshal(rec.Body.Bytes(), &out); err != nil {
		return nil // a list response, which callers decode themselves
	}
	return out
}

// seedSSHEndpoint creates an SSH endpoint as admin and returns its id.
func (h *harness) seedSSHEndpoint(name string) string {
	h.t.Helper()
	out := h.mustDo(http.MethodPost, "/api/v1/ssh-endpoints", tokenAdmin, map[string]any{
		"name": name, "hostName": name + ".example.edu",
	}, http.StatusCreated)
	return out["sshEndpointId"].(string)
}

// seedCluster creates an endpoint and a cluster reached through it, returning the
// cluster's id.
func (h *harness) seedCluster(name string) string {
	h.t.Helper()
	out := h.mustDo(http.MethodPost, "/api/v1/clusters", tokenAdmin, map[string]any{
		"clusterName": name, "sshEndpointId": h.seedSSHEndpoint(name), "slurmHome": "/usr/bin",
	}, http.StatusCreated)
	return out["clusterId"].(string)
}

// seedSSHKeyAndCredential creates a key and a credential using it.
func (h *harness) seedSSHKeyAndCredential(name string) (keyID, credentialID string) {
	h.t.Helper()
	key := h.mustDo(http.MethodPost, "/api/v1/ssh-keys", tokenAdmin, map[string]any{
		"sshKeyName": name, "publicKey": "ssh-ed25519 AAAA", "privateKey": "PRIVATE",
	}, http.StatusCreated)
	keyID = key["sshKeyId"].(string)

	cred := h.mustDo(http.MethodPost, "/api/v1/ssh-credentials", tokenAdmin, map[string]any{
		"username": "runner", "sshKeyId": keyID,
	}, http.StatusCreated)
	return keyID, cred["sshCredentialId"].(string)
}

// seedEndpointCredential creates an endpoint, an SSH credential, and a binding between
// them, returning the binding's id — what a deployment's defaultSubmissionCredentialId
// points at.
func (h *harness) seedEndpointCredential(name, token string) (endpointID, bindingID string) {
	h.t.Helper()
	endpointID = h.seedSSHEndpoint(name)
	_, credentialID := h.seedSSHKeyAndCredential(name)
	binding := h.mustDo(http.MethodPost, "/api/v1/ssh-endpoint-credentials", token, map[string]any{
		"sshEndpointId": endpointID, "sshCredentialId": credentialID,
	}, http.StatusCreated)
	return endpointID, binding["sshEndpointCredentialId"].(string)
}

func TestHealthIsOpen(t *testing.T) {
	h := newHarness(t)
	h.mustDo(http.MethodGet, "/health", "", nil, http.StatusOK)
}

// The Java filter chain permits every request and enforces at the method level, so an
// endpoint with no authority requirement stays reachable without a token.
func TestReadsAreOpenToAnonymousCallers(t *testing.T) {
	h := newHarness(t)
	for _, path := range []string{
		"/api/v1/clusters",
		"/api/v1/ssh-endpoints",
		"/api/v1/ssh-keys",
		"/api/v1/ssh-credentials",
		"/api/v1/application-templates",
		"/api/v1/slurm-deployments",
	} {
		if rec := h.do(http.MethodGet, path, "", nil); rec.Code != http.StatusOK {
			t.Errorf("GET %s anonymously: status = %d, want 200", path, rec.Code)
		}
	}
}

// A bad token is rejected outright rather than silently downgraded to anonymous,
// which would turn a mistyped token into a confusing 403 further in.
func TestInvalidTokenIsRejected(t *testing.T) {
	h := newHarness(t)
	rec := h.do(http.MethodGet, "/api/v1/clusters", tokenBogus, nil)
	if rec.Code != http.StatusUnauthorized {
		t.Errorf("status = %d, want 401", rec.Code)
	}
	if got := rec.Header().Get("WWW-Authenticate"); !strings.Contains(got, "invalid_token") {
		t.Errorf("WWW-Authenticate = %q, want it to report an invalid token", got)
	}
}

// Anonymous gets 401 (present a token) while an authenticated non-admin gets 403
// (a token will not help).
func TestWritesRequireAdmin(t *testing.T) {
	h := newHarness(t)
	body := map[string]any{"clusterName": "c", "sshEndpointId": h.seedSSHEndpoint("c"), "slurmHome": "/usr/bin"}

	if rec := h.do(http.MethodPost, "/api/v1/clusters", "", body); rec.Code != http.StatusUnauthorized {
		t.Errorf("anonymous create: status = %d, want 401", rec.Code)
	}
	if rec := h.do(http.MethodPost, "/api/v1/clusters", tokenAlice, body); rec.Code != http.StatusForbidden {
		t.Errorf("non-admin create: status = %d, want 403", rec.Code)
	}
	if rec := h.do(http.MethodPost, "/api/v1/clusters", tokenAdmin, body); rec.Code != http.StatusCreated {
		t.Errorf("admin create: status = %d, want 201\nbody: %s", rec.Code, rec.Body.String())
	}
}

func TestValidationReportsFieldErrors(t *testing.T) {
	h := newHarness(t)
	rec := h.do(http.MethodPost, "/api/v1/clusters", tokenAdmin, map[string]any{
		"clusterName": "  ", "sshEndpointId": "", "slurmHome": "/usr/bin",
	})
	if rec.Code != http.StatusBadRequest {
		t.Fatalf("status = %d, want 400\nbody: %s", rec.Code, rec.Body.String())
	}

	var body struct {
		Fields []struct{ Field, Message string } `json:"fieldErrors"`
	}
	if err := json.Unmarshal(rec.Body.Bytes(), &body); err != nil {
		t.Fatalf("decode: %v", err)
	}
	got := map[string]string{}
	for _, f := range body.Fields {
		got[f.Field] = f.Message
	}
	if got["clusterName"] != "Cluster name cannot be blank" {
		t.Errorf("clusterName error = %q, want the Java message", got["clusterName"])
	}
	if got["sshEndpointId"] != "SSH endpoint id cannot be blank" {
		t.Errorf("sshEndpointId error = %q, want the blank-endpoint message", got["sshEndpointId"])
	}
}

// A partition id from one cluster must not be reachable through another cluster's
// path, which is why the lookup is scoped by both ids.
func TestPartitionLookupIsScopedToItsCluster(t *testing.T) {
	h := newHarness(t)
	clusterA := h.seedCluster("alpha")
	clusterB := h.seedCluster("beta")

	created := h.mustDo(http.MethodPost, "/api/v1/clusters/"+clusterA+"/partitions", tokenAdmin,
		map[string]any{"name": "wholenode", "maxNodes": 16}, http.StatusCreated)
	partitionID := created["partitionId"].(string)

	h.mustDo(http.MethodGet, "/api/v1/clusters/"+clusterA+"/partitions/"+partitionID, "", nil, http.StatusOK)

	if rec := h.do(http.MethodGet, "/api/v1/clusters/"+clusterB+"/partitions/"+partitionID, "", nil); rec.Code != http.StatusNotFound {
		t.Errorf("cross-cluster partition read: status = %d, want 404", rec.Code)
	}
}

func TestDeletingClusterCascadesToPartitions(t *testing.T) {
	h := newHarness(t)
	clusterID := h.seedCluster("gamma")
	h.mustDo(http.MethodPost, "/api/v1/clusters/"+clusterID+"/partitions", tokenAdmin,
		map[string]any{"name": "cpu"}, http.StatusCreated)

	h.mustDo(http.MethodDelete, "/api/v1/clusters/"+clusterID, tokenAdmin, nil, http.StatusNoContent)

	var remaining int64
	h.db.Table("cluster_partitions").Where("cluster_id = ?", clusterID).Count(&remaining)
	if remaining != 0 {
		t.Errorf("%d partitions survived the cluster delete, want 0", remaining)
	}
}

// The private key must never appear in a response, and creation must insist on one.
func TestSSHKeySecretsAreNeverReturnedAndRequiredOnCreate(t *testing.T) {
	h := newHarness(t)

	rec := h.do(http.MethodPost, "/api/v1/ssh-keys", tokenAdmin, map[string]any{
		"sshKeyName": "k", "publicKey": "ssh-ed25519 AAAA",
	})
	if rec.Code != http.StatusBadRequest {
		t.Fatalf("create without a private key: status = %d, want 400", rec.Code)
	}

	created := h.mustDo(http.MethodPost, "/api/v1/ssh-keys", tokenAdmin, map[string]any{
		"sshKeyName": "k", "publicKey": "ssh-ed25519 AAAA", "privateKey": "SUPER-SECRET",
		"passphrase": "hunter2",
	}, http.StatusCreated)

	for _, path := range []string{"/api/v1/ssh-keys", "/api/v1/ssh-keys/" + created["sshKeyId"].(string)} {
		body := h.do(http.MethodGet, path, tokenAdmin, nil).Body.String()
		if strings.Contains(body, "SUPER-SECRET") || strings.Contains(body, "hunter2") {
			t.Errorf("GET %s leaked a secret: %s", path, body)
		}
	}
}

// A client round-tripping a response has no private key to send back, so a blank one
// must mean "leave it alone" rather than "erase it".
func TestUpdatingSSHKeyWithoutSecretsPreservesThem(t *testing.T) {
	h := newHarness(t)
	created := h.mustDo(http.MethodPost, "/api/v1/ssh-keys", tokenAdmin, map[string]any{
		"sshKeyName": "k", "publicKey": "ssh-ed25519 AAAA", "privateKey": "ORIGINAL",
		"passphrase": "original-pass",
	}, http.StatusCreated)
	keyID := created["sshKeyId"].(string)

	h.mustDo(http.MethodPut, "/api/v1/ssh-keys/"+keyID, tokenAdmin, map[string]any{
		"sshKeyName": "renamed", "publicKey": "ssh-ed25519 BBBB", "privateKey": "",
	}, http.StatusOK)

	var stored struct {
		PrivateKey string
		Passphrase *string
		SSHKeyName string
	}
	h.db.Table("ssh_keys").Select("private_key, passphrase, ssh_key_name").
		Where("ssh_key_id = ?", keyID).Scan(&stored)

	if stored.PrivateKey != "ORIGINAL" {
		t.Errorf("private key = %q, want it preserved across an update that omitted it", stored.PrivateKey)
	}
	if stored.Passphrase == nil || *stored.Passphrase != "original-pass" {
		t.Errorf("passphrase = %v, want it preserved", stored.Passphrase)
	}
	if stored.SSHKeyName != "renamed" {
		t.Errorf("name = %q, want the update applied", stored.SSHKeyName)
	}
}

// Nothing in the schema stops a key from being deleted out from under a credential,
// so the service has to.
func TestDeletingSSHKeyInUseConflicts(t *testing.T) {
	h := newHarness(t)
	keyID, _ := h.seedSSHKeyAndCredential("in-use")

	if rec := h.do(http.MethodDelete, "/api/v1/ssh-keys/"+keyID, tokenAdmin, nil); rec.Code != http.StatusConflict {
		t.Errorf("delete of a key in use: status = %d, want 409", rec.Code)
	}
}

func TestDeletingTemplateWithDeploymentsConflicts(t *testing.T) {
	h := newHarness(t)
	_, bindingID := h.seedEndpointCredential("deploy", tokenAdmin)

	tmpl := h.mustDo(http.MethodPost, "/api/v1/application-templates", tokenAdmin, map[string]any{
		"templateName": "gromacs",
		"inputs": []map[string]any{
			{"inputName": "conf", "inputType": "FILE", "required": true},
		},
	}, http.StatusCreated)
	templateID := tmpl["templateId"].(string)

	h.mustDo(http.MethodPost, "/api/v1/slurm-deployments", tokenAdmin, map[string]any{
		"templateId": templateID, "slurmRunSection": "gmx mdrun",
		"defaultSubmissionCredentialId": bindingID,
		"batchJobConfig":                map[string]any{"wallTimeMinutes": 60, "allocation": "TG-1"},
	}, http.StatusCreated)

	if rec := h.do(http.MethodDelete, "/api/v1/application-templates/"+templateID, tokenAdmin, nil); rec.Code != http.StatusConflict {
		t.Errorf("delete of a deployed template: status = %d, want 409", rec.Code)
	}
}

// An update replaces the declaration set wholesale rather than patching it.
func TestUpdatingTemplateReplacesDeclarations(t *testing.T) {
	h := newHarness(t)
	tmpl := h.mustDo(http.MethodPost, "/api/v1/application-templates", tokenAdmin, map[string]any{
		"templateName": "namd",
		"inputs": []map[string]any{
			{"inputName": "first", "inputType": "FILE"},
			{"inputName": "second", "inputType": "STRING"},
		},
	}, http.StatusCreated)
	templateID := tmpl["templateId"].(string)

	updated := h.mustDo(http.MethodPut, "/api/v1/application-templates/"+templateID, tokenAdmin, map[string]any{
		"templateName": "namd",
		"inputs": []map[string]any{
			{"inputName": "only", "inputType": "DIRECTORY"},
		},
	}, http.StatusOK)

	inputs, _ := updated["inputs"].([]any)
	if len(inputs) != 1 {
		t.Fatalf("inputs after update = %d, want 1 (the set is replaced, not merged)", len(inputs))
	}
	if name := inputs[0].(map[string]any)["inputName"]; name != "only" {
		t.Errorf("remaining input = %v, want \"only\"", name)
	}
}

// Duplicate input names within a template are rejected — the constraint the Java
// entity declared against a column that never existed.
func TestDuplicateTemplateInputNamesAreRejected(t *testing.T) {
	h := newHarness(t)
	rec := h.do(http.MethodPost, "/api/v1/application-templates", tokenAdmin, map[string]any{
		"templateName": "dup",
		"inputs": []map[string]any{
			{"inputName": "same", "inputType": "FILE"},
			{"inputName": "same", "inputType": "FILE"},
		},
	})
	// A 400 with a field error, not the 500 a raw constraint violation would produce.
	if rec.Code != http.StatusBadRequest {
		t.Fatalf("duplicate input names: status = %d, want 400\nbody: %s", rec.Code, rec.Body.String())
	}
	if !strings.Contains(rec.Body.String(), "must be unique") {
		t.Errorf("body = %s, want it to name the duplicate", rec.Body.String())
	}
}

// A cluster is optional on a deployment, but a cluster id that is supplied and
// unknown is an error rather than silently ignored.
func TestDeploymentClusterIsOptionalButValidatedWhenPresent(t *testing.T) {
	h := newHarness(t)
	_, bindingID := h.seedEndpointCredential("optional-cluster", tokenAdmin)
	tmpl := h.mustDo(http.MethodPost, "/api/v1/application-templates", tokenAdmin,
		map[string]any{"templateName": "t"}, http.StatusCreated)

	base := map[string]any{
		"templateId": tmpl["templateId"], "slurmRunSection": "run",
		"defaultSubmissionCredentialId": bindingID,
		"batchJobConfig":                map[string]any{"wallTimeMinutes": 30, "allocation": "TG-2"},
	}

	// Absent cluster: accepted.
	h.mustDo(http.MethodPost, "/api/v1/slurm-deployments", tokenAdmin, base, http.StatusCreated)

	// Blank cluster: treated as absent, not as a lookup for "".
	blank := map[string]any{"slurmClusterId": ""}
	for k, v := range base {
		blank[k] = v
	}
	h.mustDo(http.MethodPost, "/api/v1/slurm-deployments", tokenAdmin, blank, http.StatusCreated)

	// Unknown cluster: rejected.
	unknown := map[string]any{"slurmClusterId": "no-such-cluster"}
	for k, v := range base {
		unknown[k] = v
	}
	if rec := h.do(http.MethodPost, "/api/v1/slurm-deployments", tokenAdmin, unknown); rec.Code != http.StatusNotFound {
		t.Errorf("unknown cluster id: status = %d, want 404", rec.Code)
	}
}

// Ownership comes from the token and is enforced on every read of an owned resource.
func TestEndpointCredentialOwnershipIsEnforced(t *testing.T) {
	h := newHarness(t)
	endpointID := h.seedSSHEndpoint("owned")
	_, credentialID := h.seedSSHKeyAndCredential("owned")

	created := h.mustDo(http.MethodPost, "/api/v1/ssh-endpoint-credentials", tokenAlice, map[string]any{
		"sshEndpointId": endpointID, "sshCredentialId": credentialID,
	}, http.StatusCreated)
	bindingID := created["sshEndpointCredentialId"].(string)

	if got := created["userId"]; got != "alice" {
		t.Errorf("owner = %v, want it taken from the token (alice)", got)
	}
	if got := created["permission"]; got != "WRITE" {
		t.Errorf("owner permission = %v, want WRITE", got)
	}

	h.mustDo(http.MethodGet, "/api/v1/ssh-endpoint-credentials/"+bindingID, tokenAlice, nil, http.StatusOK)
	if rec := h.do(http.MethodGet, "/api/v1/ssh-endpoint-credentials/"+bindingID, tokenBob, nil); rec.Code != http.StatusForbidden {
		t.Errorf("read by another user: status = %d, want 403", rec.Code)
	}
	h.mustDo(http.MethodGet, "/api/v1/ssh-endpoint-credentials/"+bindingID, tokenAdmin, nil, http.StatusOK)

	// The unfiltered listing exposes who can reach what, so it is admin only.
	if rec := h.do(http.MethodGet, "/api/v1/ssh-endpoint-credentials", tokenAlice, nil); rec.Code != http.StatusForbidden {
		t.Errorf("listing by a non-admin: status = %d, want 403", rec.Code)
	}

	// /me is scoped to the caller.
	if mine := h.list("/api/v1/ssh-endpoint-credentials/me", tokenBob); len(mine) != 0 {
		t.Errorf("bob's own bindings = %d, want 0", len(mine))
	}
}

// A request body cannot name an owner, so a caller cannot create a binding for
// someone else even by trying.
func TestEndpointCredentialOwnerCannotBeSpoofed(t *testing.T) {
	h := newHarness(t)
	endpointID := h.seedSSHEndpoint("spoof")
	_, credentialID := h.seedSSHKeyAndCredential("spoof")

	created := h.mustDo(http.MethodPost, "/api/v1/ssh-endpoint-credentials", tokenAlice, map[string]any{
		"sshEndpointId": endpointID, "sshCredentialId": credentialID,
		"userId": "bob", "ownerId": "bob",
	}, http.StatusCreated)

	if got := created["userId"]; got != "alice" {
		t.Errorf("owner = %v, want alice — the body must not be able to set it", got)
	}
}

// Ownership is immutable: an admin editing someone's binding must not acquire it.
func TestUpdatingEndpointCredentialKeepsItsOwner(t *testing.T) {
	h := newHarness(t)
	endpointID := h.seedSSHEndpoint("keep-owner")
	_, credentialID := h.seedSSHKeyAndCredential("keep-owner")

	created := h.mustDo(http.MethodPost, "/api/v1/ssh-endpoint-credentials", tokenAlice, map[string]any{
		"sshEndpointId": endpointID, "sshCredentialId": credentialID,
	}, http.StatusCreated)

	updated := h.mustDo(http.MethodPut, "/api/v1/ssh-endpoint-credentials/"+created["sshEndpointCredentialId"].(string),
		tokenAdmin, map[string]any{"sshEndpointId": endpointID, "sshCredentialId": credentialID}, http.StatusOK)

	if got := updated["userId"]; got != "alice" {
		t.Errorf("owner after an admin update = %v, want alice", got)
	}
}

// The host a cluster is reached through is its own record now, and the cluster
// response carries it inline rather than making callers fetch it separately.
func TestClusterCarriesItsSSHEndpoint(t *testing.T) {
	h := newHarness(t)
	endpointID := h.seedSSHEndpoint("expanse")

	created := h.mustDo(http.MethodPost, "/api/v1/clusters", tokenAdmin, map[string]any{
		"clusterName": "expanse", "sshEndpointId": endpointID, "slurmHome": "/usr/bin",
	}, http.StatusCreated)

	if got := created["sshEndpointId"]; got != endpointID {
		t.Errorf("sshEndpointId = %v, want %s", got, endpointID)
	}
	endpoint, ok := created["sshEndpoint"].(map[string]any)
	if !ok {
		t.Fatalf("sshEndpoint = %v, want the endpoint inlined", created["sshEndpoint"])
	}
	if endpoint["hostName"] != "expanse.example.edu" || endpoint["port"].(float64) != 22 {
		t.Errorf("inlined endpoint = %v, want the seeded host on the default port", endpoint)
	}

	// Reads go through a different path than the create response, so check both.
	fetched := h.mustDo(http.MethodGet, "/api/v1/clusters/"+created["clusterId"].(string), "", nil, http.StatusOK)
	if fetched["sshEndpoint"] == nil {
		t.Error("GET cluster did not preload the endpoint")
	}

	// An endpoint that does not resolve is a 404, not a dangling reference.
	if rec := h.do(http.MethodPost, "/api/v1/clusters", tokenAdmin, map[string]any{
		"clusterName": "ghost", "sshEndpointId": "nope", "slurmHome": "/usr/bin",
	}); rec.Code != http.StatusNotFound {
		t.Errorf("unknown endpoint id: status = %d, want 404", rec.Code)
	}
}

// An endpoint in use cannot be deleted: the foreign keys are RESTRICT, and the service
// reports what is still holding it rather than letting the constraint fail opaquely.
func TestSSHEndpointInUseCannotBeDeleted(t *testing.T) {
	h := newHarness(t)

	// Held by a cluster.
	endpointID := h.seedSSHEndpoint("busy")
	h.mustDo(http.MethodPost, "/api/v1/clusters", tokenAdmin, map[string]any{
		"clusterName": "busy", "sshEndpointId": endpointID, "slurmHome": "/usr/bin",
	}, http.StatusCreated)
	if rec := h.do(http.MethodDelete, "/api/v1/ssh-endpoints/"+endpointID, tokenAdmin, nil); rec.Code != http.StatusConflict {
		t.Errorf("delete with a cluster attached: status = %d, want 409", rec.Code)
	}

	// Held by a credential binding.
	boundID, _ := h.seedEndpointCredential("bound", tokenAlice)
	if rec := h.do(http.MethodDelete, "/api/v1/ssh-endpoints/"+boundID, tokenAdmin, nil); rec.Code != http.StatusConflict {
		t.Errorf("delete with a binding attached: status = %d, want 409", rec.Code)
	}

	// Unreferenced, it goes.
	freeID := h.seedSSHEndpoint("free")
	h.mustDo(http.MethodDelete, "/api/v1/ssh-endpoints/"+freeID, tokenAdmin, nil, http.StatusNoContent)
	if rec := h.do(http.MethodGet, "/api/v1/ssh-endpoints/"+freeID, "", nil); rec.Code != http.StatusNotFound {
		t.Errorf("read after delete: status = %d, want 404", rec.Code)
	}
}

// Endpoints are deployment topology, not secrets: reads are open, writes are
// administrative.
func TestSSHEndpointWritesRequireAdmin(t *testing.T) {
	h := newHarness(t)
	body := map[string]any{"name": "login", "hostName": "login.example.edu"}

	if rec := h.do(http.MethodPost, "/api/v1/ssh-endpoints", "", body); rec.Code != http.StatusUnauthorized {
		t.Errorf("anonymous create: status = %d, want 401", rec.Code)
	}
	if rec := h.do(http.MethodPost, "/api/v1/ssh-endpoints", tokenAlice, body); rec.Code != http.StatusForbidden {
		t.Errorf("non-admin create: status = %d, want 403", rec.Code)
	}
	if rec := h.do(http.MethodPost, "/api/v1/ssh-endpoints", tokenAdmin, map[string]any{
		"name": "login", "hostName": "login.example.edu", "port": 70000,
	}); rec.Code != http.StatusBadRequest {
		t.Errorf("out-of-range port: status = %d, want 400", rec.Code)
	}

	created := h.mustDo(http.MethodPost, "/api/v1/ssh-endpoints", tokenAdmin, body, http.StatusCreated)
	if created["port"].(float64) != 22 {
		t.Errorf("port = %v, want the default 22 rather than the zero value", created["port"])
	}
}

// A user share grants exactly what it names: READ lets the grantee read the binding
// and nothing more, and the share itself stays the owner's to see and to revoke.
func TestEndpointCredentialUserSharing(t *testing.T) {
	h := newHarness(t)
	endpointID, bindingID := h.seedEndpointCredential("user-share", tokenAlice)
	shares := "/api/v1/ssh-endpoint-credentials/" + bindingID + "/user-shares"

	// Bob cannot see it at all to begin with.
	if rec := h.do(http.MethodGet, "/api/v1/ssh-endpoint-credentials/"+bindingID, tokenBob, nil); rec.Code != http.StatusForbidden {
		t.Fatalf("read before sharing: status = %d, want 403", rec.Code)
	}

	share := h.mustDo(http.MethodPost, shares, tokenAlice, map[string]any{"userId": "bob"}, http.StatusCreated)
	if share["permission"] != "READ" {
		t.Errorf("permission = %v, want READ by default", share["permission"])
	}

	got := h.mustDo(http.MethodGet, "/api/v1/ssh-endpoint-credentials/"+bindingID, tokenBob, nil, http.StatusOK)
	if got["permission"] != "READ" {
		t.Errorf("reported permission = %v, want READ", got["permission"])
	}
	if shared := h.list("/api/v1/ssh-endpoint-credentials/shared-with-me", tokenBob); len(shared) != 1 {
		t.Errorf("shared-with-me = %d, want the one binding", len(shared))
	}
	// A share is not ownership: /me stays empty for the grantee.
	if mine := h.list("/api/v1/ssh-endpoint-credentials/me", tokenBob); len(mine) != 0 {
		t.Errorf("bob's own bindings = %d, want 0", len(mine))
	}

	// READ is not WRITE, and no share confers control.
	repoint := map[string]any{"sshEndpointId": endpointID, "sshCredentialId": share["x"]}
	repoint["sshCredentialId"] = h.mustDo(http.MethodGet, "/api/v1/ssh-endpoint-credentials/"+bindingID,
		tokenAlice, nil, http.StatusOK)["sshCredentialId"]
	if rec := h.do(http.MethodPut, "/api/v1/ssh-endpoint-credentials/"+bindingID, tokenBob, repoint); rec.Code != http.StatusForbidden {
		t.Errorf("update with READ: status = %d, want 403", rec.Code)
	}
	if rec := h.do(http.MethodGet, shares, tokenBob, nil); rec.Code != http.StatusForbidden {
		t.Errorf("grantee listing the shares: status = %d, want 403", rec.Code)
	}
	if rec := h.do(http.MethodDelete, "/api/v1/ssh-endpoint-credentials/"+bindingID, tokenBob, nil); rec.Code != http.StatusForbidden {
		t.Errorf("grantee deleting the binding: status = %d, want 403", rec.Code)
	}

	// Widened to WRITE, the update goes through — but control still does not.
	sharingID := share["sshEndpointCredentialUserSharingId"].(string)
	h.mustDo(http.MethodPut, shares+"/"+sharingID, tokenAlice, map[string]any{"permission": "WRITE"}, http.StatusOK)
	h.mustDo(http.MethodPut, "/api/v1/ssh-endpoint-credentials/"+bindingID, tokenBob, repoint, http.StatusOK)
	if rec := h.do(http.MethodDelete, "/api/v1/ssh-endpoint-credentials/"+bindingID, tokenBob, nil); rec.Code != http.StatusForbidden {
		t.Errorf("grantee deleting with WRITE: status = %d, want 403", rec.Code)
	}

	// Revoked, the access goes with it.
	h.mustDo(http.MethodDelete, shares+"/"+sharingID, tokenAlice, nil, http.StatusNoContent)
	if rec := h.do(http.MethodGet, "/api/v1/ssh-endpoint-credentials/"+bindingID, tokenBob, nil); rec.Code != http.StatusForbidden {
		t.Errorf("read after revoke: status = %d, want 403", rec.Code)
	}
}

// A group share reaches every active member, and stops reaching them when their
// membership is suspended or the group is left.
func TestEndpointCredentialGroupSharing(t *testing.T) {
	h := newHarness(t)
	_, bindingID := h.seedEndpointCredential("group-share", tokenAlice)
	groupID := h.seedGroup("collab", tokenAlice)
	h.mustDo(http.MethodPost, "/api/v1/groups/"+groupID+"/members", tokenAlice,
		map[string]any{"userId": "bob"}, http.StatusCreated)

	shares := "/api/v1/ssh-endpoint-credentials/" + bindingID + "/group-shares"
	share := h.mustDo(http.MethodPost, shares, tokenAlice,
		map[string]any{"groupId": groupID, "permission": "WRITE"}, http.StatusCreated)

	got := h.mustDo(http.MethodGet, "/api/v1/ssh-endpoint-credentials/"+bindingID, tokenBob, nil, http.StatusOK)
	if got["permission"] != "WRITE" {
		t.Errorf("permission through the group = %v, want WRITE", got["permission"])
	}

	// Suspending the membership withdraws access without touching the share.
	h.mustDo(http.MethodPut, "/api/v1/groups/"+groupID+"/members/bob", tokenAlice,
		map[string]any{"groupMemberStatus": "INACTIVE"}, http.StatusOK)
	if rec := h.do(http.MethodGet, "/api/v1/ssh-endpoint-credentials/"+bindingID, tokenBob, nil); rec.Code != http.StatusForbidden {
		t.Errorf("read by a suspended member: status = %d, want 403", rec.Code)
	}
	if shared := h.list("/api/v1/ssh-endpoint-credentials/shared-with-me", tokenBob); len(shared) != 0 {
		t.Errorf("shared-with-me while suspended = %d, want 0", len(shared))
	}

	// Reinstated, so is the access.
	h.mustDo(http.MethodPut, "/api/v1/groups/"+groupID+"/members/bob", tokenAlice,
		map[string]any{"groupMemberStatus": "ACTIVE"}, http.StatusOK)
	h.mustDo(http.MethodGet, "/api/v1/ssh-endpoint-credentials/"+bindingID, tokenBob, nil, http.StatusOK)

	// Revoking the share ends it for the whole group.
	h.mustDo(http.MethodDelete, shares+"/"+share["sshEndpointCredentialGroupSharingId"].(string),
		tokenAlice, nil, http.StatusNoContent)
	if rec := h.do(http.MethodGet, "/api/v1/ssh-endpoint-credentials/"+bindingID, tokenBob, nil); rec.Code != http.StatusForbidden {
		t.Errorf("read after revoke: status = %d, want 403", rec.Code)
	}
}

// The strongest grant reaching a caller is the one that applies: a READ user share
// does not cap what a WRITE group share gives them.
func TestStrongestShareWins(t *testing.T) {
	h := newHarness(t)
	_, bindingID := h.seedEndpointCredential("strongest", tokenAlice)
	groupID := h.seedGroup("writers", tokenAlice)
	h.mustDo(http.MethodPost, "/api/v1/groups/"+groupID+"/members", tokenAlice,
		map[string]any{"userId": "bob"}, http.StatusCreated)

	base := "/api/v1/ssh-endpoint-credentials/" + bindingID
	h.mustDo(http.MethodPost, base+"/user-shares", tokenAlice,
		map[string]any{"userId": "bob", "permission": "READ"}, http.StatusCreated)
	h.mustDo(http.MethodPost, base+"/group-shares", tokenAlice,
		map[string]any{"groupId": groupID, "permission": "WRITE"}, http.StatusCreated)

	got := h.mustDo(http.MethodGet, base, tokenBob, nil, http.StatusOK)
	if got["permission"] != "WRITE" {
		t.Errorf("permission = %v, want WRITE from the stronger group share", got["permission"])
	}
	shared := h.list("/api/v1/ssh-endpoint-credentials/shared-with-me", tokenBob)
	if len(shared) != 1 || shared[0]["permission"] != "WRITE" {
		t.Errorf("shared-with-me = %v, want one binding at WRITE", shared)
	}
}

func TestEndpointCredentialSharingRejectsBadRequests(t *testing.T) {
	h := newHarness(t)
	_, bindingID := h.seedEndpointCredential("share-validation", tokenAlice)
	base := "/api/v1/ssh-endpoint-credentials/" + bindingID

	if rec := h.do(http.MethodPost, base+"/user-shares", tokenAlice,
		map[string]any{"userId": "nobody"}); rec.Code != http.StatusNotFound {
		t.Errorf("sharing with an unknown user: status = %d, want 404", rec.Code)
	}
	if rec := h.do(http.MethodPost, base+"/group-shares", tokenAlice,
		map[string]any{"groupId": "nope"}); rec.Code != http.StatusNotFound {
		t.Errorf("sharing with an unknown group: status = %d, want 404", rec.Code)
	}
	if rec := h.do(http.MethodPost, base+"/user-shares", tokenAlice,
		map[string]any{"userId": "bob", "permission": "ROOT"}); rec.Code != http.StatusBadRequest {
		t.Errorf("unrecognised permission: status = %d, want 400", rec.Code)
	}
	// Sharing with the owner would grant nothing and could not be revoked meaningfully.
	if rec := h.do(http.MethodPost, base+"/user-shares", tokenAlice,
		map[string]any{"userId": "alice"}); rec.Code != http.StatusConflict {
		t.Errorf("sharing with the owner: status = %d, want 409", rec.Code)
	}
	h.mustDo(http.MethodPost, base+"/user-shares", tokenAlice, map[string]any{"userId": "bob"}, http.StatusCreated)
	if rec := h.do(http.MethodPost, base+"/user-shares", tokenAlice,
		map[string]any{"userId": "bob", "permission": "WRITE"}); rec.Code != http.StatusConflict {
		t.Errorf("duplicate share: status = %d, want 409 — widen the existing one instead", rec.Code)
	}

	// A sharing id from one binding is not reachable through another's path.
	_, otherID := h.seedEndpointCredential("other-binding", tokenAlice)
	shares := h.list(base+"/user-shares", tokenAlice)
	sharingID := shares[0]["sshEndpointCredentialUserSharingId"].(string)
	if rec := h.do(http.MethodDelete,
		"/api/v1/ssh-endpoint-credentials/"+otherID+"/user-shares/"+sharingID, tokenAlice, nil); rec.Code != http.StatusNotFound {
		t.Errorf("cross-binding share delete: status = %d, want 404", rec.Code)
	}
}

// Shares point at the binding with RESTRICT, so deleting one has to take its shares
// with it rather than tripping over them.
func TestDeletingEndpointCredentialRemovesItsShares(t *testing.T) {
	h := newHarness(t)
	_, bindingID := h.seedEndpointCredential("doomed", tokenAlice)
	groupID := h.seedGroup("doomed-group", tokenAlice)
	base := "/api/v1/ssh-endpoint-credentials/" + bindingID

	h.mustDo(http.MethodPost, base+"/user-shares", tokenAlice, map[string]any{"userId": "bob"}, http.StatusCreated)
	h.mustDo(http.MethodPost, base+"/group-shares", tokenAlice, map[string]any{"groupId": groupID}, http.StatusCreated)

	h.mustDo(http.MethodDelete, base, tokenAlice, nil, http.StatusNoContent)

	var remaining int64
	h.db.Model(&computemodel.SSHEndpointCredentialUserSharing{}).
		Where("ssh_endpoint_credential_id = ?", bindingID).Count(&remaining)
	if remaining != 0 {
		t.Errorf("%d user shares survived the delete, want 0", remaining)
	}
	h.db.Model(&computemodel.SSHEndpointCredentialGroupSharing{}).
		Where("ssh_endpoint_credential_id = ?", bindingID).Count(&remaining)
	if remaining != 0 {
		t.Errorf("%d group shares survived the delete, want 0", remaining)
	}
}

// Submitting is self-service: a plain user may create a process for themselves, and
// the resources come from the request rather than the deployment's default.
func TestProcessSubmissionIsSelfServiceWithRequestedResources(t *testing.T) {
	h := newHarness(t)
	_, bindingID := h.seedEndpointCredential("proc", tokenAdmin)
	tmpl := h.mustDo(http.MethodPost, "/api/v1/application-templates", tokenAdmin,
		map[string]any{"templateName": "proc"}, http.StatusCreated)
	deployment := h.mustDo(http.MethodPost, "/api/v1/slurm-deployments", tokenAdmin, map[string]any{
		"templateId": tmpl["templateId"], "slurmRunSection": "run",
		"defaultSubmissionCredentialId": bindingID,
		"batchJobConfig":                map[string]any{"wallTimeMinutes": 60, "allocation": "DEFAULT"},
	}, http.StatusCreated)

	created := h.mustDo(http.MethodPost, "/api/v1/batch-job-processes", tokenAlice, map[string]any{
		"deploymentId":   deployment["deploymentId"],
		"batchJobConfig": map[string]any{"wallTimeMinutes": 120, "allocation": "ALICE-ALLOC", "gpus": 2},
	}, http.StatusCreated)

	if got := created["userId"]; got != "alice" {
		t.Errorf("userId = %v, want it taken from the token", got)
	}
	cfg := created["batchJobConfig"].(map[string]any)
	if cfg["allocation"] != "ALICE-ALLOC" || cfg["wallTimeMinutes"].(float64) != 120 {
		t.Errorf("config = %v, want the values from the request, not the deployment default", cfg)
	}
	// The process owns its own config, distinct from the deployment's.
	if cfg["batchJobConfigId"] == deployment["batchJobConfig"].(map[string]any)["batchJobConfigId"] {
		t.Error("process and deployment share one batch job config; each must own its own")
	}
}

// Deleting a process must take its owned config with it, since the foreign key points
// outward and the database cannot cascade in that direction.
func TestDeletingProcessRemovesItsOwnedConfig(t *testing.T) {
	h := newHarness(t)
	_, bindingID := h.seedEndpointCredential("cleanup", tokenAdmin)
	tmpl := h.mustDo(http.MethodPost, "/api/v1/application-templates", tokenAdmin,
		map[string]any{"templateName": "cleanup"}, http.StatusCreated)
	deployment := h.mustDo(http.MethodPost, "/api/v1/slurm-deployments", tokenAdmin, map[string]any{
		"templateId": tmpl["templateId"], "slurmRunSection": "run",
		"defaultSubmissionCredentialId": bindingID,
		"batchJobConfig":                map[string]any{"wallTimeMinutes": 60, "allocation": "A"},
	}, http.StatusCreated)
	proc := h.mustDo(http.MethodPost, "/api/v1/batch-job-processes", tokenAlice, map[string]any{
		"deploymentId":   deployment["deploymentId"],
		"batchJobConfig": map[string]any{"wallTimeMinutes": 10, "allocation": "B"},
	}, http.StatusCreated)

	configID := proc["batchJobConfig"].(map[string]any)["batchJobConfigId"].(string)
	h.mustDo(http.MethodDelete, "/api/v1/batch-job-processes/"+proc["processId"].(string),
		tokenAdmin, nil, http.StatusNoContent)

	var remaining int64
	h.db.Table("batch_job_configs").Where("batch_job_config_id = ?", configID).Count(&remaining)
	if remaining != 0 {
		t.Error("the process's owned batch job config survived the delete")
	}
}

// The two tables reference each other — a status names its process, and a process
// points at its most recent status — so deleting a process must clear that backward
// reference before removing its statuses, or the RESTRICT constraints deadlock.
func TestDeletingProcessRemovesItsStatuses(t *testing.T) {
	h := newHarness(t)
	_, bindingID := h.seedEndpointCredential("status-cleanup", tokenAdmin)
	tmpl := h.mustDo(http.MethodPost, "/api/v1/application-templates", tokenAdmin,
		map[string]any{"templateName": "status-cleanup"}, http.StatusCreated)
	deployment := h.mustDo(http.MethodPost, "/api/v1/slurm-deployments", tokenAdmin, map[string]any{
		"templateId": tmpl["templateId"], "slurmRunSection": "run",
		"defaultSubmissionCredentialId": bindingID,
		"batchJobConfig":                map[string]any{"wallTimeMinutes": 60, "allocation": "A"},
	}, http.StatusCreated)
	proc := h.mustDo(http.MethodPost, "/api/v1/batch-job-processes", tokenAlice, map[string]any{
		"deploymentId":   deployment["deploymentId"],
		"batchJobConfig": map[string]any{"wallTimeMinutes": 10, "allocation": "B"},
	}, http.StatusCreated)
	processID := proc["processId"].(string)

	h.mustDo(http.MethodDelete, "/api/v1/batch-job-processes/"+processID, tokenAdmin, nil, http.StatusNoContent)

	var remaining int64
	h.db.Table("batch_job_process_statuses").Where("process_id = ?", processID).Count(&remaining)
	if remaining != 0 {
		t.Error("the process's statuses survived the delete")
	}
}

// Submitting a process records an initial CREATED status in the same transaction, so
// a caller never observes a process that exists but has no status history yet.
func TestCreatingProcessRecordsInitialCreatedStatus(t *testing.T) {
	h := newHarness(t)
	_, bindingID := h.seedEndpointCredential("initial-status", tokenAdmin)
	tmpl := h.mustDo(http.MethodPost, "/api/v1/application-templates", tokenAdmin,
		map[string]any{"templateName": "initial-status"}, http.StatusCreated)
	deployment := h.mustDo(http.MethodPost, "/api/v1/slurm-deployments", tokenAdmin, map[string]any{
		"templateId": tmpl["templateId"], "slurmRunSection": "run",
		"defaultSubmissionCredentialId": bindingID,
		"batchJobConfig":                map[string]any{"wallTimeMinutes": 60, "allocation": "A"},
	}, http.StatusCreated)
	proc := h.mustDo(http.MethodPost, "/api/v1/batch-job-processes", tokenAlice, map[string]any{
		"deploymentId":   deployment["deploymentId"],
		"batchJobConfig": map[string]any{"wallTimeMinutes": 10, "allocation": "B"},
	}, http.StatusCreated)
	processID := proc["processId"].(string)

	var statuses []map[string]any
	rec := h.do(http.MethodGet, "/api/v1/batch-job-processes/"+processID+"/statuses", "", nil)
	if rec.Code != http.StatusOK {
		t.Fatalf("list statuses: status = %d, want 200\nbody: %s", rec.Code, rec.Body.String())
	}
	if err := json.Unmarshal(rec.Body.Bytes(), &statuses); err != nil {
		t.Fatalf("decode statuses: %v", err)
	}
	if len(statuses) != 1 {
		t.Fatalf("statuses = %+v, want exactly one (CREATED)", statuses)
	}
	if statuses[0]["status"] != "CREATED" {
		t.Errorf("status = %v, want CREATED", statuses[0]["status"])
	}
	if statuses[0]["processId"] != processID {
		t.Errorf("processId = %v, want %s", statuses[0]["processId"], processID)
	}

	statusID := statuses[0]["processStatusId"].(string)
	h.mustDo(http.MethodGet,
		"/api/v1/batch-job-processes/"+processID+"/statuses/"+statusID, "", nil, http.StatusOK)
}

// There is no way to create or update a status through the REST API — only to read
// it — so the two write methods on the same path must fail, not silently succeed.
func TestProcessStatusWritesAreNotExposed(t *testing.T) {
	h := newHarness(t)
	_, bindingID := h.seedEndpointCredential("status-readonly", tokenAdmin)
	tmpl := h.mustDo(http.MethodPost, "/api/v1/application-templates", tokenAdmin,
		map[string]any{"templateName": "status-readonly"}, http.StatusCreated)
	deployment := h.mustDo(http.MethodPost, "/api/v1/slurm-deployments", tokenAdmin, map[string]any{
		"templateId": tmpl["templateId"], "slurmRunSection": "run",
		"defaultSubmissionCredentialId": bindingID,
		"batchJobConfig":                map[string]any{"wallTimeMinutes": 60, "allocation": "A"},
	}, http.StatusCreated)
	proc := h.mustDo(http.MethodPost, "/api/v1/batch-job-processes", tokenAlice, map[string]any{
		"deploymentId":   deployment["deploymentId"],
		"batchJobConfig": map[string]any{"wallTimeMinutes": 10, "allocation": "B"},
	}, http.StatusCreated)
	processID := proc["processId"].(string)

	body := map[string]any{"status": "RUNNING"}
	if rec := h.do(http.MethodPost, "/api/v1/batch-job-processes/"+processID+"/statuses", tokenAdmin, body); rec.Code != http.StatusMethodNotAllowed {
		t.Errorf("POST statuses: status = %d, want 405", rec.Code)
	}
	if rec := h.do(http.MethodPut, "/api/v1/batch-job-processes/"+processID+"/statuses", tokenAdmin, body); rec.Code != http.StatusMethodNotAllowed {
		t.Errorf("PUT statuses: status = %d, want 405", rec.Code)
	}
}

// A status id from one process must not be reachable through another process's path,
// the same scoping ClusterPartition already applies to its cluster.
func TestGetProcessStatusIsScopedToItsProcess(t *testing.T) {
	h := newHarness(t)
	_, bindingID := h.seedEndpointCredential("status-scope", tokenAdmin)
	tmpl := h.mustDo(http.MethodPost, "/api/v1/application-templates", tokenAdmin,
		map[string]any{"templateName": "status-scope"}, http.StatusCreated)
	deployment := h.mustDo(http.MethodPost, "/api/v1/slurm-deployments", tokenAdmin, map[string]any{
		"templateId": tmpl["templateId"], "slurmRunSection": "run",
		"defaultSubmissionCredentialId": bindingID,
		"batchJobConfig":                map[string]any{"wallTimeMinutes": 60, "allocation": "A"},
	}, http.StatusCreated)

	procA := h.mustDo(http.MethodPost, "/api/v1/batch-job-processes", tokenAlice, map[string]any{
		"deploymentId":   deployment["deploymentId"],
		"batchJobConfig": map[string]any{"wallTimeMinutes": 10, "allocation": "B"},
	}, http.StatusCreated)
	procB := h.mustDo(http.MethodPost, "/api/v1/batch-job-processes", tokenAlice, map[string]any{
		"deploymentId":   deployment["deploymentId"],
		"batchJobConfig": map[string]any{"wallTimeMinutes": 10, "allocation": "B"},
	}, http.StatusCreated)

	var statusesA []map[string]any
	rec := h.do(http.MethodGet, "/api/v1/batch-job-processes/"+procA["processId"].(string)+"/statuses", "", nil)
	if err := json.Unmarshal(rec.Body.Bytes(), &statusesA); err != nil {
		t.Fatalf("decode: %v", err)
	}
	statusID := statusesA[0]["processStatusId"].(string)

	if rec := h.do(http.MethodGet,
		"/api/v1/batch-job-processes/"+procB["processId"].(string)+"/statuses/"+statusID, "", nil,
	); rec.Code != http.StatusNotFound {
		t.Errorf("cross-process status read: status = %d, want 404", rec.Code)
	}
}

// A caller whose token is valid but who has no users row cannot own anything.
func TestUnregisteredPrincipalCannotOwnResources(t *testing.T) {
	h := newHarness(t)
	endpointID := h.seedSSHEndpoint("ghost")
	_, credentialID := h.seedSSHKeyAndCredential("ghost")

	// A token naming a principal with no users row.
	introspector := stubIntrospector{
		"ghost-token": {Name: "ghost", Authorities: []string{string(role.User)}},
	}
	h.srv = server.New(config.Config{CORSAllowedOrigins: []string{"*"}}, h.db, introspector)

	rec := h.do(http.MethodPost, "/api/v1/ssh-endpoint-credentials", "ghost-token", map[string]any{
		"sshEndpointId": endpointID, "sshCredentialId": credentialID,
	})
	if rec.Code != http.StatusNotFound {
		t.Errorf("status = %d, want 404\nbody: %s", rec.Code, rec.Body.String())
	}
	if !strings.Contains(rec.Body.String(), "No user record found") {
		t.Errorf("body = %s, want it to name the missing user record", rec.Body.String())
	}
}

// Users may read themselves; only admins may read anyone.
func TestUserReadIsSelfOrAdmin(t *testing.T) {
	h := newHarness(t)

	h.mustDo(http.MethodGet, "/api/v1/users/alice", tokenAlice, nil, http.StatusOK)
	if rec := h.do(http.MethodGet, "/api/v1/users/bob", tokenAlice, nil); rec.Code != http.StatusForbidden {
		t.Errorf("reading another user: status = %d, want 403", rec.Code)
	}
	h.mustDo(http.MethodGet, "/api/v1/users/bob", tokenAdmin, nil, http.StatusOK)

	if rec := h.do(http.MethodGet, "/api/v1/users", tokenAlice, nil); rec.Code != http.StatusForbidden {
		t.Errorf("listing users as a non-admin: status = %d, want 403", rec.Code)
	}
	h.mustDo(http.MethodGet, "/api/v1/users", tokenAdmin, nil, http.StatusOK)
}

// Registration is Super Admin only — a plain ADMIN is not enough.
func TestUserRegistrationRequiresSuperAdmin(t *testing.T) {
	h := newHarness(t)
	body := map[string]any{"userId": "carol", "firstName": "Carol", "lastName": "X"}

	if rec := h.do(http.MethodPost, "/api/v1/users", tokenAdmin, body); rec.Code != http.StatusForbidden {
		t.Errorf("registration by ADMIN: status = %d, want 403", rec.Code)
	}

	superToken := "token-super"
	h.srv = server.New(config.Config{CORSAllowedOrigins: []string{"*"}}, h.db, stubIntrospector{
		superToken: {Name: "admin-user", Authorities: []string{string(role.SuperAdmin)}},
	})

	created := h.mustDo(http.MethodPost, "/api/v1/users", superToken, body, http.StatusOK)
	if created["status"] != "ACTIVE" {
		t.Errorf("status = %v, want ACTIVE set by the service", created["status"])
	}
	if created["createdAt"].(float64) <= 0 {
		t.Error("createdAt was not stamped")
	}
}

// The user response carries five fields and must not widen to email or roles.
func TestUserResponseDoesNotLeakEmail(t *testing.T) {
	h := newHarness(t)
	if err := h.db.Model(&iammodel.User{}).Where("user_id = ?", "alice").
		Update("email", "alice@example.edu").Error; err != nil {
		t.Fatalf("seed email: %v", err)
	}

	body := h.do(http.MethodGet, "/api/v1/users/alice", tokenAlice, nil).Body.String()
	if strings.Contains(body, "alice@example.edu") {
		t.Errorf("user response leaked an email address: %s", body)
	}
}

// seedGroup creates a group owned by the caller of token and returns its id.
func (h *harness) seedGroup(name, token string) string {
	h.t.Helper()
	out := h.mustDo(http.MethodPost, "/api/v1/groups", token, map[string]any{
		"groupName": name,
	}, http.StatusCreated)
	return out["groupId"].(string)
}

// list issues a GET and decodes the array response.
func (h *harness) list(path, token string) []map[string]any {
	h.t.Helper()
	rec := h.do(http.MethodGet, path, token, nil)
	if rec.Code != http.StatusOK {
		h.t.Fatalf("GET %s: status = %d, want 200\nbody: %s", path, rec.Code, rec.Body.String())
	}
	var out []map[string]any
	if err := json.Unmarshal(rec.Body.Bytes(), &out); err != nil {
		h.t.Fatalf("decode %s: %v", path, err)
	}
	return out
}

// A group belongs to whoever created it, and the owner is admitted as an ADMIN member
// in the same transaction so the group is never left with nobody able to administer it.
func TestGroupCreationAdmitsTheOwnerAsAdmin(t *testing.T) {
	h := newHarness(t)

	created := h.mustDo(http.MethodPost, "/api/v1/groups", tokenAlice, map[string]any{
		"groupName": "molecular-dynamics",
		"ownerId":   "bob", // ignored: ownership comes from the token
	}, http.StatusCreated)

	if got := created["ownerId"]; got != "alice" {
		t.Errorf("ownerId = %v, want it taken from the token (alice)", got)
	}
	if created["createdAt"].(float64) <= 0 {
		t.Error("createdAt was not stamped")
	}

	groupID := created["groupId"].(string)
	members := h.list("/api/v1/groups/"+groupID+"/members", tokenAlice)
	if len(members) != 1 {
		t.Fatalf("members = %d, want the owner's own membership", len(members))
	}
	if members[0]["userId"] != "alice" || members[0]["groupRole"] != "ADMIN" || members[0]["groupMemberStatus"] != "ACTIVE" {
		t.Errorf("owner membership = %v, want alice as an ACTIVE ADMIN", members[0])
	}
}

// Group names say who is working with whom, so an outsider must not be able to confirm
// that a group id exists at all.
func TestGroupIsInvisibleToOutsiders(t *testing.T) {
	h := newHarness(t)
	groupID := h.seedGroup("private", tokenAlice)

	for _, path := range []string{
		"/api/v1/groups/" + groupID,
		"/api/v1/groups/" + groupID + "/members",
		"/api/v1/groups/" + groupID + "/members/alice",
	} {
		if rec := h.do(http.MethodGet, path, tokenBob, nil); rec.Code != http.StatusNotFound {
			t.Errorf("GET %s as an outsider: status = %d, want 404", path, rec.Code)
		}
	}

	if mine := h.list("/api/v1/groups/me", tokenBob); len(mine) != 0 {
		t.Errorf("bob's groups = %d, want 0", len(mine))
	}
	if mine := h.list("/api/v1/groups/me", tokenAlice); len(mine) != 1 {
		t.Errorf("alice's groups = %d, want the one she owns", len(mine))
	}
}

// Plain members may read the group; only the owner, group ADMINs and MODERATORs may
// change who is in it.
func TestGroupMemberManagementFollowsGroupRole(t *testing.T) {
	h := newHarness(t)
	groupID := h.seedGroup("collab", tokenAlice)

	h.mustDo(http.MethodPost, "/api/v1/groups/"+groupID+"/members", tokenAlice,
		map[string]any{"userId": "bob"}, http.StatusCreated)

	// Admitted as a plain member by default: he can read, but not admit anyone else.
	h.mustDo(http.MethodGet, "/api/v1/groups/"+groupID, tokenBob, nil, http.StatusOK)
	if rec := h.do(http.MethodPost, "/api/v1/groups/"+groupID+"/members", tokenBob,
		map[string]any{"userId": "admin-user"}); rec.Code != http.StatusForbidden {
		t.Errorf("member admitting another user: status = %d, want 403", rec.Code)
	}

	// Promoted to MODERATOR, he can.
	updated := h.mustDo(http.MethodPut, "/api/v1/groups/"+groupID+"/members/bob", tokenAlice,
		map[string]any{"groupRole": "MODERATOR"}, http.StatusOK)
	if updated["groupRole"] != "MODERATOR" {
		t.Errorf("groupRole = %v, want MODERATOR", updated["groupRole"])
	}
	if updated["groupMemberStatus"] != "ACTIVE" {
		t.Errorf("groupMemberStatus = %v, want it left as ACTIVE by a role-only update", updated["groupMemberStatus"])
	}
	h.mustDo(http.MethodPost, "/api/v1/groups/"+groupID+"/members", tokenBob,
		map[string]any{"userId": "admin-user", "groupRole": "MEMBER"}, http.StatusCreated)

	// Re-submitting the values a membership already holds is a no-op update, not a
	// failed insert.
	h.mustDo(http.MethodPut, "/api/v1/groups/"+groupID+"/members/bob", tokenAlice,
		map[string]any{"groupRole": "MODERATOR"}, http.StatusOK)

	// Renaming and deleting stay with the owner even so.
	if rec := h.do(http.MethodPut, "/api/v1/groups/"+groupID, tokenBob,
		map[string]any{"groupName": "hijacked"}); rec.Code != http.StatusForbidden {
		t.Errorf("rename by a moderator: status = %d, want 403", rec.Code)
	}
	if rec := h.do(http.MethodDelete, "/api/v1/groups/"+groupID, tokenBob, nil); rec.Code != http.StatusForbidden {
		t.Errorf("delete by a moderator: status = %d, want 403", rec.Code)
	}
}

// A moderator must not be able to suspend the owner out of their own group.
func TestGroupOwnerMembershipIsProtected(t *testing.T) {
	h := newHarness(t)
	groupID := h.seedGroup("protected", tokenAlice)
	h.mustDo(http.MethodPost, "/api/v1/groups/"+groupID+"/members", tokenAlice,
		map[string]any{"userId": "bob", "groupRole": "MODERATOR"}, http.StatusCreated)

	if rec := h.do(http.MethodPut, "/api/v1/groups/"+groupID+"/members/alice", tokenBob,
		map[string]any{"groupMemberStatus": "INACTIVE"}); rec.Code != http.StatusForbidden {
		t.Errorf("moderator suspending the owner: status = %d, want 403", rec.Code)
	}
	if rec := h.do(http.MethodDelete, "/api/v1/groups/"+groupID+"/members/alice", tokenBob, nil); rec.Code != http.StatusConflict {
		t.Errorf("moderator removing the owner: status = %d, want 409", rec.Code)
	}
	// Not even the owner, who must delete the group instead.
	if rec := h.do(http.MethodDelete, "/api/v1/groups/"+groupID+"/members/alice", tokenAlice, nil); rec.Code != http.StatusConflict {
		t.Errorf("owner removing their own membership: status = %d, want 409", rec.Code)
	}
}

// Leaving a group needs nobody's permission, and works from a suspended membership.
func TestMemberMayLeaveGroup(t *testing.T) {
	h := newHarness(t)
	groupID := h.seedGroup("leavers", tokenAlice)
	h.mustDo(http.MethodPost, "/api/v1/groups/"+groupID+"/members", tokenAlice,
		map[string]any{"userId": "bob", "groupMemberStatus": "INACTIVE"}, http.StatusCreated)

	// A suspended member cannot read through the group...
	if rec := h.do(http.MethodGet, "/api/v1/groups/"+groupID, tokenBob, nil); rec.Code != http.StatusNotFound {
		t.Errorf("read by a suspended member: status = %d, want 404", rec.Code)
	}
	// ...but can still walk out.
	h.mustDo(http.MethodDelete, "/api/v1/groups/"+groupID+"/members/bob", tokenBob, nil, http.StatusNoContent)

	if members := h.list("/api/v1/groups/"+groupID+"/members", tokenAlice); len(members) != 1 {
		t.Errorf("members = %d, want only the owner left", len(members))
	}
}

// The unfiltered listing exposes every group in the deployment, so it is admin only;
// /me is what an ordinary caller uses.
func TestGroupListingIsAdminOnly(t *testing.T) {
	h := newHarness(t)
	h.seedGroup("alices", tokenAlice)
	h.seedGroup("bobs", tokenBob)

	if rec := h.do(http.MethodGet, "/api/v1/groups", tokenAlice, nil); rec.Code != http.StatusForbidden {
		t.Errorf("listing by a non-admin: status = %d, want 403", rec.Code)
	}
	if all := h.list("/api/v1/groups", tokenAdmin); len(all) != 2 {
		t.Errorf("admin listing = %d groups, want 2", len(all))
	}
	if mine := h.list("/api/v1/groups/me", tokenAlice); len(mine) != 1 {
		t.Errorf("alice's groups = %d, want 1", len(mine))
	}
}

// Deleting a group takes its membership rows with it rather than leaving rows that
// grant access through a group that no longer exists.
func TestDeletingGroupRemovesMemberships(t *testing.T) {
	h := newHarness(t)
	groupID := h.seedGroup("doomed", tokenAlice)
	h.mustDo(http.MethodPost, "/api/v1/groups/"+groupID+"/members", tokenAlice,
		map[string]any{"userId": "bob"}, http.StatusCreated)

	h.mustDo(http.MethodDelete, "/api/v1/groups/"+groupID, tokenAlice, nil, http.StatusNoContent)

	var remaining int64
	h.db.Model(&iammodel.GroupMember{}).Where("group_id = ?", groupID).Count(&remaining)
	if remaining != 0 {
		t.Errorf("%d membership rows survived the group delete, want 0", remaining)
	}
	if rec := h.do(http.MethodGet, "/api/v1/groups/"+groupID, tokenAlice, nil); rec.Code != http.StatusNotFound {
		t.Errorf("read after delete: status = %d, want 404", rec.Code)
	}
}

func TestGroupMembershipRejectsBadRequests(t *testing.T) {
	h := newHarness(t)
	groupID := h.seedGroup("validation", tokenAlice)

	// An unknown user is a 404 rather than an opaque foreign key failure.
	if rec := h.do(http.MethodPost, "/api/v1/groups/"+groupID+"/members", tokenAlice,
		map[string]any{"userId": "nobody"}); rec.Code != http.StatusNotFound {
		t.Errorf("admitting an unknown user: status = %d, want 404", rec.Code)
	}
	// An unrecognised role is rejected before it reaches the write.
	if rec := h.do(http.MethodPost, "/api/v1/groups/"+groupID+"/members", tokenAlice,
		map[string]any{"userId": "bob", "groupRole": "OVERLORD"}); rec.Code != http.StatusBadRequest {
		t.Errorf("unrecognised role: status = %d, want 400", rec.Code)
	}
	// Admitting the same user twice collides rather than silently rewriting their role.
	h.mustDo(http.MethodPost, "/api/v1/groups/"+groupID+"/members", tokenAlice,
		map[string]any{"userId": "bob", "groupRole": "MEMBER"}, http.StatusCreated)
	if rec := h.do(http.MethodPost, "/api/v1/groups/"+groupID+"/members", tokenAlice,
		map[string]any{"userId": "bob", "groupRole": "ADMIN"}); rec.Code != http.StatusConflict {
		t.Errorf("duplicate membership: status = %d, want 409", rec.Code)
	}
	// A blank name is rejected on the group itself.
	if rec := h.do(http.MethodPost, "/api/v1/groups", tokenAlice,
		map[string]any{"groupName": "   "}); rec.Code != http.StatusBadRequest {
		t.Errorf("blank group name: status = %d, want 400", rec.Code)
	}
}

// Group writes are the one place an ordinary user creates a resource of their own, so
// anonymous callers must still be turned away.
func TestGroupWritesRequireAuthentication(t *testing.T) {
	h := newHarness(t)
	if rec := h.do(http.MethodPost, "/api/v1/groups", "", map[string]any{"groupName": "anon"}); rec.Code != http.StatusUnauthorized {
		t.Errorf("anonymous create: status = %d, want 401", rec.Code)
	}
	if rec := h.do(http.MethodGet, "/api/v1/groups/me", "", nil); rec.Code != http.StatusUnauthorized {
		t.Errorf("anonymous /me: status = %d, want 401", rec.Code)
	}
}

func TestUnknownResourcesReportNotFound(t *testing.T) {
	h := newHarness(t)
	for _, path := range []string{
		"/api/v1/clusters/nope",
		"/api/v1/ssh-endpoints/nope",
		"/api/v1/ssh-keys/nope",
		"/api/v1/application-templates/nope",
		"/api/v1/slurm-deployments/nope",
		"/api/v1/batch-job-processes/nope",
		"/api/v1/groups/nope",
	} {
		rec := h.do(http.MethodGet, path, tokenAdmin, nil)
		if rec.Code != http.StatusNotFound {
			t.Errorf("GET %s: status = %d, want 404", path, rec.Code)
		}
		if ct := rec.Header().Get("Content-Type"); !strings.HasPrefix(ct, "application/json") {
			t.Errorf("GET %s: content type = %q, want JSON", path, ct)
		}
	}
}

// Nullable numbers must round-trip as null rather than collapsing to zero, which is
// the whole reason the entity fields are pointers.
func TestUnsetPartitionLimitsStayNull(t *testing.T) {
	h := newHarness(t)
	clusterID := h.seedCluster("nulls")
	created := h.mustDo(http.MethodPost, "/api/v1/clusters/"+clusterID+"/partitions", tokenAdmin,
		map[string]any{"name": "cpu"}, http.StatusCreated)

	if v, ok := created["maxNodes"]; !ok || v != nil {
		t.Errorf("maxNodes = %v, want null for an undeclared limit", v)
	}
	if v, ok := created["isDefaultQueue"]; !ok || v != nil {
		t.Errorf("isDefaultQueue = %v, want null", v)
	}
}

func TestCORSPreflightIsAnsweredWithoutAuth(t *testing.T) {
	h := newHarness(t)
	req := httptest.NewRequest(http.MethodOptions, "/api/v1/clusters", nil)
	req.Header.Set("Origin", "https://portal.example.edu")
	req.Header.Set("Access-Control-Request-Method", "POST")
	rec := httptest.NewRecorder()
	h.srv.ServeHTTP(rec, req)

	if rec.Code != http.StatusNoContent {
		t.Errorf("preflight status = %d, want 204", rec.Code)
	}
	// The origin is echoed rather than "*", so credentialed requests are usable.
	if got := rec.Header().Get("Access-Control-Allow-Origin"); got != "https://portal.example.edu" {
		t.Errorf("Allow-Origin = %q, want the caller's origin echoed", got)
	}
}

func TestRootTokenAuthenticatesAsSuperAdmin(t *testing.T) {
	provider := auth.NewRootTokenProvider("")
	if provider.Token() == "" {
		t.Fatal("no root token was generated")
	}
	if !provider.Matches(provider.Token()) {
		t.Error("the generated root token did not match itself")
	}
	if provider.Matches("something-else") {
		t.Error("an unrelated token matched the root token")
	}
	p := provider.Principal()
	if p.Name != auth.RootUsername || !p.HasAnyAuthority(string(role.SuperAdmin)) {
		t.Errorf("root principal = %+v, want %q with SUPER_ADMIN", p, auth.RootUsername)
	}
}

func TestNormalizeUsernameCollapsesCILogonURIs(t *testing.T) {
	cases := map[string]string{
		"http://cilogon.org/serverE/users/12345": "cilogon:12345",
		"cilogon:12345":                          "cilogon:12345",
		"admin":                                  "admin",
	}
	for in, want := range cases {
		if got := auth.NormalizeUsername(in); got != want {
			t.Errorf("NormalizeUsername(%q) = %q, want %q", in, got, want)
		}
	}
}
