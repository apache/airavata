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

package sharing

import (
	"context"
	"errors"
	"fmt"
	"strings"

	"github.com/modelcontextprotocol/go-sdk/mcp"

	"github.com/apache/airavata/internal/mcp/client"
	"github.com/apache/airavata/internal/mcp/confirm"
	tk "github.com/apache/airavata/internal/mcp/tools/toolkit"
)

// Register adds the sharing toolset.
func Register(s *mcp.Server, d *tk.Deps) {
	tk.Read(s, d, tk.Def{Name: "list_shares", Title: "List who a resource is shared with",
		Description: "List the users and groups a cluster config, data storage or data product is shared with, and their permission. Only the owner can see this."},
		func(ctx context.Context, _ *mcp.CallToolRequest, in resourceIn) (*mcp.CallToolResult, sharesOut, error) {
			shares, err := list(ctx, d, in)
			if err != nil {
				return nil, sharesOut{}, err
			}
			return nil, sharesOut{Shares: shares}, nil
		})
	tk.Write(s, d, tk.Def{Name: "share_resource", Title: "Share a resource",
		Description: "Share a cluster config, data storage or data product with a user or a group, READ (default) or WRITE. WRITE never includes deleting or re-sharing. A group share reaches every active member. Asks the user to confirm, showing exactly who gains what."},
		prepareShare(d))
	tk.Write(s, d, tk.Def{Name: "revoke_share", Title: "Revoke a share", Destructive: true,
		Description: "Stop sharing a cluster config, data storage or data product with a user or group. Asks the user to confirm."},
		prepareRevoke(d))
}

type resourceIn struct {
	ResourceType string `json:"resource_type" jsonschema:"cluster_config, data_storage or data_product"`
	ResourceID   string `json:"resource_id" jsonschema:"id from list_cluster_configs, list_data_storages or list_data_products"`
}

type shareOut struct {
	PrincipalType string `json:"principal_type"`
	PrincipalID   string `json:"principal_id"`
	Permission    string `json:"permission"`
}

type sharesOut struct {
	Shares []shareOut `json:"shares"`
}

type resource struct {
	label string
	list  func(ctx context.Context) ([]client.ShareResponse, error)
	add   func(ctx context.Context, body client.ShareRequest) (*client.ShareResponse, error)
	del   func(ctx context.Context, sharingID client.SharingId) error
}

func resolve(d *tk.Deps, in resourceIn) (*resource, error) {
	id, err := client.ParseUUID("resource_id", in.ResourceID)
	if err != nil {
		return nil, err
	}
	api := d.API
	unwrap := func(r *[]client.ShareResponse, err error) ([]client.ShareResponse, error) {
		if err != nil {
			return nil, err
		}
		return *r, nil
	}
	switch in.ResourceType {
	case "cluster_config":
		return &resource{label: "cluster config",
			list: func(ctx context.Context) ([]client.ShareResponse, error) {
				return unwrap(client.JSON[[]client.ShareResponse](api.GetApiV1SlurmClusterConfigsSlurmClusterConfigIdSharesWithResponse(ctx, id)))
			},
			add: func(ctx context.Context, b client.ShareRequest) (*client.ShareResponse, error) {
				return client.JSON[client.ShareResponse](api.PostApiV1SlurmClusterConfigsSlurmClusterConfigIdSharesWithResponse(ctx, id, b))
			},
			del: func(ctx context.Context, sid client.SharingId) error {
				return client.NoContent(api.DeleteApiV1SlurmClusterConfigsSlurmClusterConfigIdSharesSharingIdWithResponse(ctx, id, sid))
			}}, nil
	case "data_storage":
		return &resource{label: "data storage",
			list: func(ctx context.Context) ([]client.ShareResponse, error) {
				return unwrap(client.JSON[[]client.ShareResponse](api.GetApiV1ScpDataStoragesDataStorageIdSharesWithResponse(ctx, id)))
			},
			add: func(ctx context.Context, b client.ShareRequest) (*client.ShareResponse, error) {
				return client.JSON[client.ShareResponse](api.PostApiV1ScpDataStoragesDataStorageIdSharesWithResponse(ctx, id, b))
			},
			del: func(ctx context.Context, sid client.SharingId) error {
				return client.NoContent(api.DeleteApiV1ScpDataStoragesDataStorageIdSharesSharingIdWithResponse(ctx, id, sid))
			}}, nil
	case "data_product":
		return &resource{label: "data product",
			list: func(ctx context.Context) ([]client.ShareResponse, error) {
				return unwrap(client.JSON[[]client.ShareResponse](api.GetApiV1DataProductsDataProductIdSharesWithResponse(ctx, id)))
			},
			add: func(ctx context.Context, b client.ShareRequest) (*client.ShareResponse, error) {
				return client.JSON[client.ShareResponse](api.PostApiV1DataProductsDataProductIdSharesWithResponse(ctx, id, b))
			},
			del: func(ctx context.Context, sid client.SharingId) error {
				return client.NoContent(api.DeleteApiV1DataProductsDataProductIdSharesSharingIdWithResponse(ctx, id, sid))
			}}, nil
	}
	return nil, fmt.Errorf("resource_type %q is not shareable here; use cluster_config, data_storage or data_product", in.ResourceType)
}

func list(ctx context.Context, d *tk.Deps, in resourceIn) ([]shareOut, error) {
	r, err := resolve(d, in)
	if err != nil {
		return nil, err
	}
	shares, err := r.list(ctx)
	if err != nil {
		if client.IsStatus(err, 403) || client.IsStatus(err, 404) {
			return nil, fmt.Errorf("only the owner of %s %s can see its shares", r.label, in.ResourceID)
		}
		return nil, err
	}
	out := []shareOut{}
	for _, s := range shares {
		out = append(out, shareOut{PrincipalType: deref(s.PrincipalType), PrincipalID: tk.Str(s.PrincipalId), Permission: deref(s.Permission)})
	}
	return out, nil
}

func deref[T ~string](p *T) string {
	if p == nil {
		return ""
	}
	return string(*p)
}

type shareIn struct {
	resourceIn
	PrincipalType string `json:"principal_type" jsonschema:"user or group"`
	PrincipalID   string `json:"principal_id" jsonschema:"a user id (cilogon:<id>) or a group id"`
	Permission    string `json:"permission,omitempty" jsonschema:"READ (default) or WRITE"`
}

type doneOut struct {
	Message string `json:"message"`
}

// describePrincipal says who a grant reaches, in words.
func describePrincipal(ctx context.Context, d *tk.Deps, kind, id string) (client.ShareRequestPrincipalType, string, error) {
	switch strings.ToLower(kind) {
	case "user":
		return client.ShareRequestPrincipalTypeUSER, "user " + id, nil
	case "group":
		g, err := client.JSON[client.GroupResponse](d.API.GetGroupWithResponse(ctx, id))
		if err != nil {
			return "", "", tk.NotFound(err, "group", id)
		}
		who := fmt.Sprintf("group %q (%s)", tk.Str(g.GroupName), id)
		if members, err := client.JSON[[]client.GroupMemberResponse](d.API.ListGroupMembersWithResponse(ctx, id)); err == nil {
			active := 0
			for _, m := range *members {
				if m.GroupMemberStatus == client.GroupMemberStatusACTIVE {
					active++
				}
			}
			who += fmt.Sprintf(", currently %d active member(s), and anyone who joins it later", active)
		}
		return client.ShareRequestPrincipalTypeGROUP, who, nil
	}
	return "", "", errors.New("principal_type must be user or group")
}

func prepareShare(d *tk.Deps) tk.Prepare[shareIn, doneOut] {
	return func(ctx context.Context, _ string, in shareIn) (*tk.Proposal[doneOut], error) {
		r, err := resolve(d, in.resourceIn)
		if err != nil {
			return nil, err
		}
		perm := client.ShareRequestPermissionREAD
		switch strings.ToUpper(in.Permission) {
		case "", "READ":
		case "WRITE":
			perm = client.ShareRequestPermissionWRITE
		default:
			return nil, errors.New("permission must be READ or WRITE")
		}
		ptype, who, err := describePrincipal(ctx, d, in.PrincipalType, in.PrincipalID)
		if err != nil {
			return nil, err
		}
		what := "read it"
		if perm == client.ShareRequestPermissionWRITE {
			what = "read and modify it (not delete it or change who it is shared with)"
		}
		if in.ResourceType == "cluster_config" {
			what += "; a cluster config lets them submit jobs as its login user with its credential"
		}
		msg := fmt.Sprintf("Share %s %s with %s. They will be able to %s.", r.label, in.ResourceID, who, what)
		body := client.ShareRequest{PrincipalType: ptype, PrincipalId: in.PrincipalID, Permission: &perm}
		return &tk.Proposal[doneOut]{Message: msg, Run: func(ctx context.Context, _ confirm.Values) (doneOut, []string, error) {
			s, err := r.add(ctx, body)
			if err != nil {
				return doneOut{}, nil, err
			}
			return doneOut{Message: "Shared."}, []string{tk.Str(s.ResourceSharingId)}, nil
		}}, nil
	}
}

type revokeIn struct {
	resourceIn
	PrincipalType string `json:"principal_type" jsonschema:"user or group"`
	PrincipalID   string `json:"principal_id"`
}

func prepareRevoke(d *tk.Deps) tk.Prepare[revokeIn, doneOut] {
	return func(ctx context.Context, _ string, in revokeIn) (*tk.Proposal[doneOut], error) {
		r, err := resolve(d, in.resourceIn)
		if err != nil {
			return nil, err
		}
		shares, err := r.list(ctx)
		if err != nil {
			return nil, err
		}
		var target *client.ShareResponse
		for i := range shares {
			if strings.EqualFold(deref(shares[i].PrincipalType), in.PrincipalType) && tk.Str(shares[i].PrincipalId) == in.PrincipalID {
				target = &shares[i]
			}
		}
		if target == nil || target.ResourceSharingId == nil {
			return nil, fmt.Errorf("%s %s is not shared with %s %s", r.label, in.ResourceID, in.PrincipalType, in.PrincipalID)
		}
		sid, err := client.ParseUUID("sharing id", *target.ResourceSharingId)
		if err != nil {
			return nil, err
		}
		msg := fmt.Sprintf("Stop sharing %s %s with %s %s (currently %s). They lose access immediately.",
			r.label, in.ResourceID, strings.ToLower(in.PrincipalType), in.PrincipalID, deref(target.Permission))
		return &tk.Proposal[doneOut]{Message: msg, Run: func(ctx context.Context, _ confirm.Values) (doneOut, []string, error) {
			if err := r.del(ctx, sid); err != nil {
				return doneOut{}, nil, err
			}
			return doneOut{Message: "Share revoked."}, []string{sid.String()}, nil
		}}, nil
	}
}
