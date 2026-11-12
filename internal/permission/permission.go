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

// Package permission defines what a caller may be allowed to do.
//
// A permission names one action on one kind of record, written "resource:action". The
// handlers and services ask for permissions, never for roles: which roles grant a
// permission is data (the role_permissions table), so changing who may do something
// does not mean changing the code that does it.
//
// It is a leaf package like internal/role, so the model, the auth guards and the
// resolver can all share the definitions without importing one another.
package permission

import (
	"sort"

	"github.com/apache/airavata/internal/role"
)

// Permission is one grantable action.
//
// Naming: "resource:action". An action ending in "_any" is an override that reaches
// records the caller does not own; without it the caller can still act on their own.
// "list" returns every user's records rather than the caller's.
type Permission string

const (
	// Audit log.
	AuditRead Permission = "audit:read" // read the audit log

	// Users.
	UserCreate    Permission = "user:create"     // create a user record ahead of the user's first sign-in
	UserList      Permission = "user:list"       // list every user
	UserReadAny   Permission = "user:read_any"   // read another user's record
	UserUpdateAny Permission = "user:update_any" // edit another user's profile
	UserSetStatus Permission = "user:set_status" // activate, suspend or deactivate a user
	// AdminManage is the authority over administrators themselves: appointing and
	// removing ADMINs, and changing an administrator's status.
	AdminManage Permission = "admin:manage"

	// Groups.
	GroupList      Permission = "group:list"       // list every group
	GroupReadAny   Permission = "group:read_any"   // read a group the caller is not in
	GroupManageAny Permission = "group:manage_any" // rename, delete and manage members of any group

	// Service accounts.
	ServiceAccountRead   Permission = "serviceaccount:read"   // list and read service accounts
	ServiceAccountManage Permission = "serviceaccount:manage" // create, edit, delete and re-issue tokens

	// Compute.
	ClusterManage     Permission = "cluster:manage"     // create, edit and delete clusters and their partitions
	ClusterDiscover   Permission = "cluster:discover"   // discover a cluster's partitions over SSH
	ClusterConfigList Permission = "clusterconfig:list" // list every cluster config

	// Applications.
	TemplateManage   Permission = "template:manage"   // create, edit and delete application templates
	DeploymentManage Permission = "deployment:manage" // create, edit and delete batch deployments

	// Data.
	StorageList          Permission = "storage:list"          // list every data storage
	DataProductList      Permission = "dataproduct:list"      // list every data product
	VirtualDirectoryList Permission = "virtualdirectory:list" // list every virtual data directory

	// ResourceManageAny reaches any shareable record — cluster configs, data storages,
	// data products and virtual data directories — as if the caller owned it. SSH
	// credentials are not among them: they are visible to their owner only.
	ResourceManageAny Permission = "resource:manage_any"

	// Processes.
	ProcessList      Permission = "process:list"       // list every process
	ProcessUpdate    Permission = "process:update"     // edit any process directly
	ProcessDelete    Permission = "process:delete"     // delete any process
	ProcessManageAny Permission = "process:manage_any" // read, launch and edit the tasks, statuses and workflows of any process
)

// all is the catalogue, in the order it is documented.
var all = []Permission{
	AuditRead,
	UserCreate, UserList, UserReadAny, UserUpdateAny, UserSetStatus, AdminManage,
	GroupList, GroupReadAny, GroupManageAny,
	ServiceAccountRead, ServiceAccountManage,
	ClusterManage, ClusterDiscover, ClusterConfigList,
	TemplateManage, DeploymentManage,
	StorageList, DataProductList, VirtualDirectoryList, ResourceManageAny,
	ProcessList, ProcessUpdate, ProcessDelete, ProcessManageAny,
}

// superAdminOnly are the permissions ADMIN does not hold by default, because before
// permissions existed only SUPER_ADMIN could do them.
var superAdminOnly = map[Permission]bool{
	UserCreate:    true,
	UserUpdateAny: true,
	AdminManage:   true,
}

// descriptions say, in a line, what each permission allows. They are what an
// administrator sees when choosing what to grant, so they are written for that reader.
var descriptions = map[Permission]string{
	Permission("audit:read"):            "read the audit log",
	Permission("user:create"):           "create a user record ahead of the user's first sign-in",
	Permission("user:list"):             "list every user",
	Permission("user:read_any"):         "read another user's record",
	Permission("user:update_any"):       "edit another user's profile",
	Permission("user:set_status"):       "activate, suspend or deactivate a user",
	Permission("admin:manage"):          "appoint and remove administrators, and change an administrator's status",
	Permission("group:list"):            "list every group",
	Permission("group:read_any"):        "read a group the caller is not in",
	Permission("group:manage_any"):      "rename, delete and manage members of any group",
	Permission("serviceaccount:read"):   "list and read service accounts",
	Permission("serviceaccount:manage"): "create, edit, delete and re-issue tokens",
	Permission("cluster:manage"):        "create, edit and delete clusters and their partitions",
	Permission("cluster:discover"):      "discover a cluster's partitions over SSH",
	Permission("clusterconfig:list"):    "list every cluster config",
	Permission("template:manage"):       "create, edit and delete application templates",
	Permission("deployment:manage"):     "create, edit and delete batch deployments",
	Permission("storage:list"):          "list every data storage",
	Permission("dataproduct:list"):      "list every data product",
	Permission("virtualdirectory:list"): "list every virtual data directory",
	Permission("resource:manage_any"):   "act as the owner of any cluster config, data storage, data product or virtual data directory",
	Permission("process:list"):          "list every process",
	Permission("process:update"):        "edit any process directly",
	Permission("process:delete"):        "delete any process",
	Permission("process:manage_any"):    "read, launch and edit the tasks, statuses and workflows of any process",
}

// Description returns what p allows, or "" for a permission that is not defined.
func (p Permission) Description() string { return descriptions[p] }

// Info describes one permission for a client that lets an administrator choose them.
type Info struct {
	Permission                Permission
	Description               string
	GrantableToServiceAccount bool
}

// Catalog returns every defined permission with its description, sorted.
func Catalog() []Info {
	out := make([]Info, 0, len(all))
	for _, p := range All() {
		out = append(out, Info{Permission: p, Description: p.Description(), GrantableToServiceAccount: p.GrantableToServiceAccount()})
	}
	return out
}

// notForServiceAccounts are the permissions a service account can never hold, however
// an administrator asks. They are the ones that let a token widen access beyond what it
// was given: appointing administrators, rewriting another user's profile, and creating
// or administering service accounts. The guarantee that a service account cannot become
// an administrator or mint another service account depends on them staying out of
// reach.
//
// user:create and user:set_status are deliberately not here. They let an account
// register users and activate or suspend them, which an integration such as an
// onboarding system legitimately needs, and neither reaches an administrator: changing
// an administrator's status still needs admin:manage.
var notForServiceAccounts = map[Permission]bool{
	AdminManage:          true,
	UserUpdateAny:        true,
	ServiceAccountManage: true,
}

// GrantableToServiceAccount reports whether p may be granted to a service account.
func (p Permission) GrantableToServiceAccount() bool {
	return p.Valid() && !notForServiceAccounts[p]
}

// All returns every defined permission, sorted.
func All() []Permission {
	out := append([]Permission(nil), all...)
	sort.Slice(out, func(i, j int) bool { return out[i] < out[j] })
	return out
}

// Valid reports whether p is a defined permission.
func (p Permission) Valid() bool {
	for _, known := range all {
		if p == known {
			return true
		}
	}
	return false
}

// Set is a collection of permissions with constant-time membership.
type Set map[Permission]struct{}

// SetOf returns a Set holding ps.
func SetOf(ps ...Permission) Set {
	s := make(Set, len(ps))
	for _, p := range ps {
		s[p] = struct{}{}
	}
	return s
}

// Has reports whether s contains p. It is safe on a nil Set, which contains nothing.
func (s Set) Has(p Permission) bool {
	_, ok := s[p]
	return ok
}

// Sorted lists the members in a stable order, for responses and logs.
func (s Set) Sorted() []string {
	out := make([]string, 0, len(s))
	for p := range s {
		out = append(out, string(p))
	}
	sort.Strings(out)
	return out
}

// Defaults is what each role is granted on a fresh install, before an administrator
// changes anything. It reproduces what the role checks allowed before permissions
// existed: ADMIN could do every administrative thing except the few reserved to
// SUPER_ADMIN, and USER none.
//
// SUPER_ADMIN is deliberately absent. It holds every permission by definition and is
// never looked up in role_permissions, so no edit to that table can lock the last
// administrator out.
//
// The same rows are inserted by the versioned migrations (0015, 0016);
// TestDefaultsMatchMigrations keeps them in step.
func Defaults() map[role.Role][]Permission {
	var admin []Permission
	for _, p := range All() {
		if !superAdminOnly[p] {
			admin = append(admin, p)
		}
	}
	return map[role.Role][]Permission{
		role.Admin: admin,
		role.User:  {},
	}
}
