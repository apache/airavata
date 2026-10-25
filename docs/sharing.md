# Sharing

Every shareable record in Airavata is reached the same way: you own it, an administrator overrides it, or someone granted you access to it. This page describes that one mechanism and the one set of endpoints that drives it. For everything else about a given record — what its fields mean, how to create it — see [`api.md`](api.md).

Base URL: `http://localhost:9095` (default `SERVER_PORT` is `9095`).

## What can be shared

Four kinds of record, each reached under its own collection:

| Resource | `resourceType` | Shares live under |
|---|---|---|
| [SCP data storage](api.md#scp-data-storages) | `SCP_DATA_STORAGE` | `/api/v1/scp-data-storages/{dataStorageId}/shares` |
| [Data product](api.md#data-products) | `DATA_PRODUCT` | `/api/v1/data-products/{dataProductId}/shares` |
| Virtual data directory | `VIRTUAL_DATA_DIRECTORY` | `/api/v1/virtual-data-directories/{virtualDataDirectoryId}/shares` |
| [Slurm cluster config](api.md#slurm-cluster-configs) | `SLURM_CLUSTER_CONFIG` | `/api/v1/slurm-cluster-configs/{slurmClusterConfigId}/shares` |

[SSH keys](api.md#ssh-keys) are deliberately **not** shareable. A key is the credential itself rather than something reached with one, so lending it out is what sharing a cluster config or a data storage is *for* — the key stays with its owner while other people submit and stage under it.

## The model

A share is one row saying *this principal may do this much with this record*. Both ends are a `(type, id)` pair:

```
resourceType   + resourceId    →  what is being opened up
principalType  + principalId   →  who it is being opened up to
permission                     →  how much
```

`principalType` is `USER` or `GROUP`. That field is the only difference between granting to a person and granting to a team — there is no separate endpoint for each.

`permission` is `READ` or `WRITE`. **`WRITE` implies `READ`**; there is nothing above `WRITE`.

### What `WRITE` does not include

Two things stay with the owner and cannot be reached through any share:

- **Deleting the record.**
- **Reading or changing its share list.** The list names who holds the record, which is more than a grantee needs to know — so a grantee gets `403` even on `GET .../shares`.

Platform admins (`ADMIN`, `SUPER_ADMIN`) are treated as owners throughout.

### How access is resolved

For a given caller and record, the answer is the **strongest** of:

1. **Ownership** — the owner always holds `WRITE`, plus the two owner-only powers above.
2. **A share naming the caller** (`principalType: USER`).
3. **A share naming a group the caller is an active member of** (`principalType: GROUP`).

"Strongest" matters when more than one applies. A `READ` grant to you personally does not cap a `WRITE` grant reaching you through a group — you hold `WRITE`.

A group share reaches someone only through an **`ACTIVE`** membership. An inactive member keeps their place in the group without keeping access through it.

Reads that return a record include a `permission` field saying what the calling principal holds on it, so a client does not have to work this out for itself.

### Virtual data directories inherit down the tree

A virtual dataset is a tree, and a share on a directory opens **the whole subtree beneath it**. Sharing an interior node hands out part of a dataset without handing out all of it.

Ownership inherits the same way: owning the root of a dataset is owning all of it, so the owner of a root controls every node below without a share anywhere.

Resolution still takes the strongest grant reaching the caller *anywhere in the node's ancestry*. A `WRITE` at the root and a `READ` on a nested node means `WRITE` on that node — the more specific grant does not override the broader one.

Two consequences worth knowing:

- `GET /api/v1/virtual-data-directories/{virtualDataDirectoryId}/shares` lists the grants made **at that node**, not the ones it inherits. That is the list you can edit there.
- `/shared-with-me` returns the nodes the shares actually name, which may be interior directories rather than roots. That node is as far up as the grantee can see.

## Endpoints

The same four routes under every resource. `{resourceId}` below stands for whichever path parameter that collection uses.

```
GET    /api/v1/{collection}/{resourceId}/shares                 (owner)
POST   /api/v1/{collection}/{resourceId}/shares                 (owner)
PUT    /api/v1/{collection}/{resourceId}/shares/{sharingId}     (owner)
DELETE /api/v1/{collection}/{resourceId}/shares/{sharingId}     (owner)
```

All four require `Authorization: Bearer <token>` for the record's owner or a platform admin.

A `sharingId` is scoped to the record it belongs to. Presenting a product's sharing id under a storage's path is `404`, not a cross-resource edit.

### Grant access

```
POST /api/v1/{collection}/{resourceId}/shares
```

**Request body**

| Field | Type | Notes |
|---|---|---|
| `principalType` | `USER` \| `GROUP` | required |
| `principalId` | string | required, cannot be blank; a user id for `USER`, a group id for `GROUP`. Must reference an existing record |
| `permission` | `READ` \| `WRITE` \| null | optional, defaults to `READ` — widening is a deliberate act |

```bash
# Share a data product with one user, read-only.
curl -s -X POST localhost:9095/api/v1/data-products/"$DATA_PRODUCT_ID"/shares \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{ "principalType": "USER", "principalId": "cilogon:67890", "permission": "READ" }'
```

**Response — `201 Created`**

```json
{
  "resourceSharingId": "2c3d4e5f-6a7b-4c8d-9e0f-1a2b3c4d5e6f",
  "resourceType": "DATA_PRODUCT",
  "resourceId": "9a8b7c6d-5e4f-4a3b-9c2d-1e0f9a8b7c6d",
  "principalType": "USER",
  "principalId": "cilogon:67890",
  "permission": "READ"
}
```

The same call against a group differs only in the two principal fields:

```bash
# Let a whole team launch under a cluster config.
curl -s -X POST localhost:9095/api/v1/slurm-cluster-configs/"$CLUSTER_CONFIG_ID"/shares \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{ "principalType": "GROUP", "principalId": "'"$GROUP_ID"'", "permission": "WRITE" }'
```

### List who has access

```
GET /api/v1/{collection}/{resourceId}/shares
```

Returns both kinds of grant together, in no guaranteed order:

```bash
curl -s localhost:9095/api/v1/scp-data-storages/"$DATA_STORAGE_ID"/shares \
  -H "Authorization: Bearer $TOKEN"
```

```json
[
  {
    "resourceSharingId": "2c3d4e5f-6a7b-4c8d-9e0f-1a2b3c4d5e6f",
    "resourceType": "SCP_DATA_STORAGE",
    "resourceId": "1f2e3d4c-5b6a-4987-8765-43210fedcba9",
    "principalType": "USER",
    "principalId": "cilogon:67890",
    "permission": "READ"
  },
  {
    "resourceSharingId": "7a8b9c0d-1e2f-4a3b-8c4d-5e6f7a8b9c0d",
    "resourceType": "SCP_DATA_STORAGE",
    "resourceId": "1f2e3d4c-5b6a-4987-8765-43210fedcba9",
    "principalType": "GROUP",
    "principalId": "3b4c5d6e-7f80-4912-a3b4-c5d6e7f80912",
    "permission": "WRITE"
  }
]
```

### Widen or narrow a grant

```
PUT /api/v1/{collection}/{resourceId}/shares/{sharingId}
```

Only `permission` is editable — the subject is fixed when the share is created, because moving a grant to a different principal would be a revoke and a grant wearing one id. `permission` is required here; it does not default.

```bash
curl -s -X PUT localhost:9095/api/v1/data-products/"$DATA_PRODUCT_ID"/shares/"$SHARING_ID" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{ "permission": "WRITE" }'
```

Returns `200 OK` with the updated share.

To change who a grant names, revoke it and create a new one.

### Revoke

```
DELETE /api/v1/{collection}/{resourceId}/shares/{sharingId}
```

```bash
curl -s -X DELETE localhost:9095/api/v1/data-products/"$DATA_PRODUCT_ID"/shares/"$SHARING_ID" \
  -H "Authorization: Bearer $TOKEN"
```

Returns `204 No Content`. Access ends immediately.

### Find what has been shared with you

Each shareable collection carries a `/shared-with-me` listing, returning the records others have opened up to the caller — directly or through a group — each with the `permission` it grants:

```bash
curl -s localhost:9095/api/v1/data-products/shared-with-me \
  -H "Authorization: Bearer $TOKEN"
```

Records you **own** never appear there: ownership is not a share, and `/me` already returns your own.

## A worked example

Alice registers a dataset and opens it to Bob's team for writing, then narrows one person back to read-only.

```bash
ALICE="Bearer $ALICE_TOKEN"

# 1. Alice owns the product; permission comes back as WRITE.
curl -s localhost:9095/api/v1/data-products/"$PRODUCT" -H "Authorization: $ALICE" | jq .permission
# "WRITE"

# 2. Bob sees nothing yet.
curl -s -o /dev/null -w '%{http_code}\n' \
  localhost:9095/api/v1/data-products/"$PRODUCT" -H "Authorization: Bearer $BOB_TOKEN"
# 403

# 3. Alice shares with the team, WRITE. Bob is an ACTIVE member.
curl -s -X POST localhost:9095/api/v1/data-products/"$PRODUCT"/shares \
  -H "Authorization: $ALICE" -H "Content-Type: application/json" \
  -d '{ "principalType": "GROUP", "principalId": "'"$TEAM"'", "permission": "WRITE" }'

# 4. Bob now holds WRITE, through the group.
curl -s localhost:9095/api/v1/data-products/"$PRODUCT" -H "Authorization: Bearer $BOB_TOKEN" | jq .permission
# "WRITE"

# 5. A READ grant naming Bob directly does NOT cap the group's WRITE.
curl -s -X POST localhost:9095/api/v1/data-products/"$PRODUCT"/shares \
  -H "Authorization: $ALICE" -H "Content-Type: application/json" \
  -d '{ "principalType": "USER", "principalId": "bob", "permission": "READ" }'

curl -s localhost:9095/api/v1/data-products/"$PRODUCT" -H "Authorization: Bearer $BOB_TOKEN" | jq .permission
# "WRITE"  — the strongest grant reaching him still applies

# 6. To actually narrow Bob, narrow the group grant he holds it through.
SHARE=$(curl -s localhost:9095/api/v1/data-products/"$PRODUCT"/shares -H "Authorization: $ALICE" \
  | jq -r '.[] | select(.principalType == "GROUP") | .resourceSharingId')

curl -s -X PUT localhost:9095/api/v1/data-products/"$PRODUCT"/shares/"$SHARE" \
  -H "Authorization: $ALICE" -H "Content-Type: application/json" \
  -d '{ "permission": "READ" }'

# 7. Bob is read-only. He still cannot see the share list, or delete the product.
curl -s -o /dev/null -w '%{http_code}\n' \
  localhost:9095/api/v1/data-products/"$PRODUCT"/shares -H "Authorization: Bearer $BOB_TOKEN"
# 403
```

Step 5 is the one that catches people out. Grants accumulate; they do not override each other. To take access away, remove or narrow **every** grant that reaches the person.

## When shares disappear on their own

| What you delete | What happens to its shares |
|---|---|
| A shareable record | Its shares are deleted with it, in the same transaction |
| A virtual data directory | Its shares **and every share in the subtree below it** go too |
| A group | Every grant made to that group is withdrawn, across all four resource kinds |
| A group membership | Nothing is deleted — but the share stops reaching that person, since it only reaches active members |

Users are never deleted through this API, so a user grant outlives everything except an explicit revoke.

## Errors

| Status | When |
|---|---|
| `400 Bad Request` | `principalType` missing or not `USER`/`GROUP`; `principalId` blank; an unrecognised `permission` |
| `401 Unauthorized` | no token, or a token that is present but unusable |
| `403 Forbidden` | the caller is not the owner — a grantee cannot read or change the share list, even one they appear in |
| `404 Not Found` | no such record, user or group; or a `sharingId` belonging to a different record |
| `409 Conflict` | already shared with that principal — widen the existing grant instead; or shared with the record's owner, which would grant nothing |

Note the distinction on `409`: for a virtual data directory, "the owner" means the owner of that node **or of any directory above it**, since ownership inherits down the tree.

## Notes for implementers

The sharing rows for all four resource kinds live in one table, `resource_sharings`, keyed by `(resourceType, resourceId, principalType, principalId)` — which is unique, and is what makes a duplicate grant a `409` rather than a second row.

Because the resource and the principal are each a `(type, id)` pair, **neither carries a database foreign key**: a column cannot reference four resource tables, nor users and groups at once. Nothing in the database removes a share when the thing it names goes away. That cleanup is done by the services instead, which is what the table above describes — and it is why a new shareable record must delete its own shares when it is deleted.

Adding a fifth shareable record means adding a `resourceType` constant and implementing one small interface (`ResourceGuard`: what the type tag is, what to call the record in an error, and whether this caller may manage its shares). It does not mean new tables, a new service or new routes.
