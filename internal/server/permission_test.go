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

package server_test

import (
	"net/http"
	"testing"

	"github.com/apache/airavata/internal/permission"
	"github.com/apache/airavata/internal/role"

	iammodel "github.com/apache/airavata/api/iam/model"
)

type permissionProbe struct {
	perm   permission.Permission
	method string
	path   string
	body   any
}

// Every permission gates what it says it gates: a plain USER is refused, granting that
// one permission to USER lets the same request through, and nothing else changes. This is
// what proves the guards check permissions rather than the ADMIN role name.
func TestEachPermissionGatesItsAction(t *testing.T) {
	h := newHarness(t)

	// Records owned by bob, which alice has no standing on.
	bobGroup := h.seedGroup("bobs-group", tokenBob)
	_, bobStorage := h.seedStorage("bobs-storage", tokenBob)
	bobProcess := h.seedProcess("bobs-run", tokenBob)
	aliceKey := h.seedSSHCredential("alices-key", tokenAlice)
	template := h.mustDo("POST", "/api/v1/application-templates", tokenAdmin,
		map[string]any{"templateName": "probe"}, http.StatusCreated)
	clusterID := h.seedCluster("probe-cluster")
	// A body that passes validation, so the request reaches the authorization check. The
	// config is alice's: editing a process also checks the caller's access to the config
	// it names, which is a separate matter from the right to edit the process.
	configID, configClusterID := h.seedClusterConfig(tokenAlice, "probe-config")
	deployment := h.mustDo("POST", "/api/v1/slurm-deployments", tokenAdmin, map[string]any{
		"templateId": template["templateId"], "slurmRunSection": "run", "slurmClusterId": configClusterID,
		"defaultBatchJobConfig": map[string]any{"wallTimeMinutes": 60, "allocation": "DEFAULT"},
	}, http.StatusCreated)
	processUpdate := map[string]any{
		"processType": "BATCH_JOB",
		"batchProcess": map[string]any{
			"deploymentId":         deployment["deploymentId"],
			"slurmClusterConfigId": configID,
			"batchJobConfig":       map[string]any{"wallTimeMinutes": 60, "allocation": "ALLOC"},
		},
	}
	// The delete probe succeeds once granted, so it gets a process of its own.
	doomedProcess := h.seedProcess("bobs-doomed-run", tokenBob)

	probes := []permissionProbe{
		{permission.UserCreate, "POST", "/api/v1/users", map[string]any{"userId": "cilogon:9", "email": "n@example.org", "firstName": "N", "lastName": "N"}},
		{permission.UserList, "GET", "/api/v1/users", nil},
		{permission.UserReadAny, "GET", "/api/v1/users/bob", nil},
		{permission.UserUpdateAny, "POST", "/api/v1/users/bob", map[string]any{"userId": "bob", "email": "b@example.org", "firstName": "B", "lastName": "B"}},
		{permission.UserSetStatus, "POST", "/api/v1/users/bob/status", map[string]any{"status": "ACTIVE"}},
		{permission.AdminManage, "POST", "/api/v1/users/bob/admin", map[string]any{"admin": true}},

		{permission.GroupList, "GET", "/api/v1/groups", nil},
		{permission.GroupReadAny, "GET", "/api/v1/groups/" + bobGroup, nil},
		{permission.GroupManageAny, "PUT", "/api/v1/groups/" + bobGroup, map[string]any{"groupName": "renamed"}},

		{permission.ServiceAccountRead, "GET", "/api/v1/service-accounts", nil},
		{permission.ServiceAccountManage, "POST", "/api/v1/service-accounts", map[string]any{"name": "ci", "description": "d", "expiresInDays": 30}},

		{permission.ClusterManage, "POST", "/api/v1/slurm-clusters", map[string]any{"clusterName": "c", "headnodeHost": "login.hpc.example.edu", "headnodePort": 22}},
		{permission.ClusterDiscover, "POST", "/api/v1/slurm-clusters/discover-partitions", map[string]any{"headnodeHost": "127.0.0.1", "headnodePort": 1, "loginUser": "runner", "sshCredentialId": aliceKey}},
		{permission.ClusterConfigList, "GET", "/api/v1/slurm-cluster-configs", nil},

		{permission.TemplateManage, "POST", "/api/v1/application-templates", map[string]any{"templateName": "t"}},
		{permission.DeploymentManage, "POST", "/api/v1/slurm-deployments", map[string]any{
			"templateId": template["templateId"], "slurmRunSection": "run", "slurmClusterId": clusterID,
			"defaultBatchJobConfig": map[string]any{"wallTimeMinutes": 60, "allocation": "DEFAULT"},
		}},

		{permission.StorageList, "GET", "/api/v1/scp-data-storages", nil},
		{permission.DataProductList, "GET", "/api/v1/data-products", nil},
		{permission.VirtualDirectoryList, "GET", "/api/v1/virtual-data-directories", nil},
		{permission.ResourceManageAny, "GET", "/api/v1/scp-data-storages/" + bobStorage, nil},

		{permission.ProcessList, "GET", "/api/v1/processes", nil},
		{permission.ProcessUpdate, "PUT", "/api/v1/processes/" + bobProcess, processUpdate},
		{permission.ProcessDelete, "DELETE", "/api/v1/processes/" + doomedProcess, nil},
		{permission.ProcessManageAny, "GET", "/api/v1/processes/" + bobProcess + "/workflows", nil},
	}

	covered := map[permission.Permission]bool{permission.AuditRead: true} // see TestAuditAccessFollowsRolePermissions
	for _, p := range probes {
		covered[p.perm] = true
	}
	for _, p := range permission.All() {
		if !covered[p] {
			t.Errorf("permission %s has no probe in this test", p)
		}
	}

	denied := map[int]bool{http.StatusForbidden: true, http.StatusNotFound: true}
	for _, p := range probes {
		t.Run(string(p.perm), func(t *testing.T) {
			if rec := h.do(p.method, p.path, tokenAlice, p.body); !denied[rec.Code] {
				t.Fatalf("%s %s without the grant: status = %d, want 403 or 404\n%s", p.method, p.path, rec.Code, rec.Body.String())
			}

			grant := iammodel.RolePermission{Role: role.User, Permission: p.perm}
			if err := h.db.Create(&grant).Error; err != nil {
				t.Fatal(err)
			}
			h.svcs.Permissions.Invalidate()
			defer func() {
				h.db.Delete(&grant)
				h.svcs.Permissions.Invalidate()
			}()

			rec := h.do(p.method, p.path, tokenAlice, p.body)
			if rec.Code == http.StatusUnauthorized || denied[rec.Code] {
				t.Fatalf("%s %s with only %s granted: status = %d\n%s", p.method, p.path, p.perm, rec.Code, rec.Body.String())
			}
		})
	}
}
