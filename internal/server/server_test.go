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

// seedCluster creates a cluster as admin and returns its id.
func (h *harness) seedCluster(name string) string {
	h.t.Helper()
	out := h.mustDo(http.MethodPost, "/api/v1/clusters", tokenAdmin, map[string]any{
		"clusterName": name, "hostName": name + ".example.edu", "slurmHome": "/usr/bin",
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
	body := map[string]any{"clusterName": "c", "hostName": "h", "slurmHome": "/usr/bin"}

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
		"clusterName": "  ", "hostName": "", "slurmHome": "/usr/bin",
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
	if got["hostName"] != "Host name cannot be blank" {
		t.Errorf("hostName error = %q, want the Java message", got["hostName"])
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
	_, credentialID := h.seedSSHKeyAndCredential("deploy")

	tmpl := h.mustDo(http.MethodPost, "/api/v1/application-templates", tokenAdmin, map[string]any{
		"templateName": "gromacs",
		"inputs": []map[string]any{
			{"inputName": "conf", "inputType": "FILE", "required": true},
		},
	}, http.StatusCreated)
	templateID := tmpl["templateId"].(string)

	h.mustDo(http.MethodPost, "/api/v1/slurm-deployments", tokenAdmin, map[string]any{
		"templateId": templateID, "slurmRunSection": "gmx mdrun",
		"defaultSubmissionCredentialId": credentialID,
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
	_, credentialID := h.seedSSHKeyAndCredential("optional-cluster")
	tmpl := h.mustDo(http.MethodPost, "/api/v1/application-templates", tokenAdmin,
		map[string]any{"templateName": "t"}, http.StatusCreated)

	base := map[string]any{
		"templateId": tmpl["templateId"], "slurmRunSection": "run",
		"defaultSubmissionCredentialId": credentialID,
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
func TestClusterCredentialOwnershipIsEnforced(t *testing.T) {
	h := newHarness(t)
	clusterID := h.seedCluster("owned")
	_, credentialID := h.seedSSHKeyAndCredential("owned")

	created := h.mustDo(http.MethodPost, "/api/v1/cluster-credentials", tokenAlice, map[string]any{
		"clusterId": clusterID, "sshCredentialId": credentialID,
	}, http.StatusCreated)
	bindingID := created["clusterCredentialId"].(string)

	if got := created["userId"]; got != "alice" {
		t.Errorf("owner = %v, want it taken from the token (alice)", got)
	}

	h.mustDo(http.MethodGet, "/api/v1/cluster-credentials/"+bindingID, tokenAlice, nil, http.StatusOK)
	if rec := h.do(http.MethodGet, "/api/v1/cluster-credentials/"+bindingID, tokenBob, nil); rec.Code != http.StatusForbidden {
		t.Errorf("read by another user: status = %d, want 403", rec.Code)
	}
	h.mustDo(http.MethodGet, "/api/v1/cluster-credentials/"+bindingID, tokenAdmin, nil, http.StatusOK)

	// The unfiltered listing exposes who can reach what, so it is admin only.
	if rec := h.do(http.MethodGet, "/api/v1/cluster-credentials", tokenAlice, nil); rec.Code != http.StatusForbidden {
		t.Errorf("listing by a non-admin: status = %d, want 403", rec.Code)
	}

	// /me is scoped to the caller.
	var mine []map[string]any
	rec := h.do(http.MethodGet, "/api/v1/cluster-credentials/me", tokenBob, nil)
	if err := json.Unmarshal(rec.Body.Bytes(), &mine); err != nil {
		t.Fatalf("decode /me: %v", err)
	}
	if len(mine) != 0 {
		t.Errorf("bob's own bindings = %d, want 0", len(mine))
	}
}

// A request body cannot name an owner, so a caller cannot create a binding for
// someone else even by trying.
func TestClusterCredentialOwnerCannotBeSpoofed(t *testing.T) {
	h := newHarness(t)
	clusterID := h.seedCluster("spoof")
	_, credentialID := h.seedSSHKeyAndCredential("spoof")

	created := h.mustDo(http.MethodPost, "/api/v1/cluster-credentials", tokenAlice, map[string]any{
		"clusterId": clusterID, "sshCredentialId": credentialID,
		"userId": "bob", "ownerId": "bob",
	}, http.StatusCreated)

	if got := created["userId"]; got != "alice" {
		t.Errorf("owner = %v, want alice — the body must not be able to set it", got)
	}
}

// Ownership is immutable: an admin editing someone's binding must not acquire it.
func TestUpdatingClusterCredentialKeepsItsOwner(t *testing.T) {
	h := newHarness(t)
	clusterID := h.seedCluster("keep-owner")
	_, credentialID := h.seedSSHKeyAndCredential("keep-owner")

	created := h.mustDo(http.MethodPost, "/api/v1/cluster-credentials", tokenAlice, map[string]any{
		"clusterId": clusterID, "sshCredentialId": credentialID,
	}, http.StatusCreated)

	updated := h.mustDo(http.MethodPut, "/api/v1/cluster-credentials/"+created["clusterCredentialId"].(string),
		tokenAdmin, map[string]any{"clusterId": clusterID, "sshCredentialId": credentialID}, http.StatusOK)

	if got := updated["userId"]; got != "alice" {
		t.Errorf("owner after an admin update = %v, want alice", got)
	}
}

// Lifecycle state belongs to the service, not the client.
func TestSCPDataProvisionStatusIsForced(t *testing.T) {
	h := newHarness(t)
	clusterID := h.seedCluster("scp")
	_, credentialID := h.seedSSHKeyAndCredential("scp")

	binding := h.mustDo(http.MethodPost, "/api/v1/cluster-credentials", tokenAlice, map[string]any{
		"clusterId": clusterID, "sshCredentialId": credentialID,
	}, http.StatusCreated)

	created := h.mustDo(http.MethodPost, "/api/v1/scp-data", tokenAlice, map[string]any{
		"dataName": "inputs", "isFile": "true", "path": "/scratch/in",
		"slurmClusterCredentialId": binding["clusterCredentialId"],
		"provisionStatus":          "PROVISIONED",
	}, http.StatusCreated)

	if got := created["provisionStatus"]; got != "REGISTERD" {
		t.Errorf("provisionStatus = %v, want REGISTERD regardless of what the client sent", got)
	}
	if got := created["ownerId"]; got != "alice" {
		t.Errorf("ownerId = %v, want it taken from the token", got)
	}

	if rec := h.do(http.MethodGet, "/api/v1/scp-data/"+created["dataId"].(string), tokenBob, nil); rec.Code != http.StatusForbidden {
		t.Errorf("read by another user: status = %d, want 403", rec.Code)
	}
}

// Submitting is self-service: a plain user may create a process for themselves, and
// the resources come from the request rather than the deployment's default.
func TestProcessSubmissionIsSelfServiceWithRequestedResources(t *testing.T) {
	h := newHarness(t)
	_, credentialID := h.seedSSHKeyAndCredential("proc")
	tmpl := h.mustDo(http.MethodPost, "/api/v1/application-templates", tokenAdmin,
		map[string]any{"templateName": "proc"}, http.StatusCreated)
	deployment := h.mustDo(http.MethodPost, "/api/v1/slurm-deployments", tokenAdmin, map[string]any{
		"templateId": tmpl["templateId"], "slurmRunSection": "run",
		"defaultSubmissionCredentialId": credentialID,
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
	_, credentialID := h.seedSSHKeyAndCredential("cleanup")
	tmpl := h.mustDo(http.MethodPost, "/api/v1/application-templates", tokenAdmin,
		map[string]any{"templateName": "cleanup"}, http.StatusCreated)
	deployment := h.mustDo(http.MethodPost, "/api/v1/slurm-deployments", tokenAdmin, map[string]any{
		"templateId": tmpl["templateId"], "slurmRunSection": "run",
		"defaultSubmissionCredentialId": credentialID,
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

// A caller whose token is valid but who has no users row cannot own anything.
func TestUnregisteredPrincipalCannotOwnResources(t *testing.T) {
	h := newHarness(t)
	clusterID := h.seedCluster("ghost")
	_, credentialID := h.seedSSHKeyAndCredential("ghost")

	// A token naming a principal with no users row.
	introspector := stubIntrospector{
		"ghost-token": {Name: "ghost", Authorities: []string{string(role.User)}},
	}
	h.srv = server.New(config.Config{CORSAllowedOrigins: []string{"*"}}, h.db, introspector)

	rec := h.do(http.MethodPost, "/api/v1/cluster-credentials", "ghost-token", map[string]any{
		"clusterId": clusterID, "sshCredentialId": credentialID,
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

func TestUnknownResourcesReportNotFound(t *testing.T) {
	h := newHarness(t)
	for _, path := range []string{
		"/api/v1/clusters/nope",
		"/api/v1/ssh-keys/nope",
		"/api/v1/application-templates/nope",
		"/api/v1/slurm-deployments/nope",
		"/api/v1/batch-job-processes/nope",
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
