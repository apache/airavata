# Virtual Data

A **virtual dataset** is a folder tree you build out of [data products](data-product.md). It gives scattered data one browsable layout. For example, files on a lab server, a directory on a cluster's scratch space and the results of several runs can all appear together under `my-study/`, without any of them being moved or copied.

The tree holds **references only**. Each entry points at a data product, which still says where the data actually lives. Creating, renaming, moving or deleting an entry never touches a storage host.

```
my-study/                         virtual data directory (root)    ── owned by you
├── inputs/                       virtual data directory
│   ├── T1050.fasta               virtual data file  ──▶ file product on lab-sequences
│   └── T1051.fasta               virtual data file  ──▶ file product on lab-sequences
├── reference-db/                 virtual data directory ──▶ directory product on expanse-scratch
└── results/
    └── run-1/
        └── confidence.json       virtual data file  ──▶ file product written by a run
```

The same product can appear in several trees, or several times in one tree, under different names.

---

## Building blocks

### Virtual data directory

A directory node is one of two kinds:

| Kind | `dataProductId` | Holds |
|---|---|---|
| **Container** | not set | other directories and files, like an ordinary folder |
| **Product-backed** | a **directory** data product | nothing. It stands for that product's whole directory and cannot have children. |

| Field | Required | Meaning |
|---|---|---|
| `directoryName` | yes | One path segment. It cannot be `.` or `..` or contain `/`, `\` or a NUL character. |
| `parentDirectoryId` | no | The directory it sits in. If this is left out, the node is a **root**, which is the top of a new dataset. |
| `dataProductId` | no | Set this to make the node product-backed. |

### Virtual data file

A file is a leaf that stands for one **file** data product.

| Field | Required | Meaning |
|---|---|---|
| `fileName` | yes | One path segment, with the same rules as `directoryName`. It need not match the product's real file name. |
| `parentDirectoryId` | yes | The directory it sits in. Every file has one, so a file cannot be a root. |
| `dataProductId` | yes | A **file** data product. |

### Naming rules

- Names are unique within a directory across both kinds. A file and a directory in the same directory cannot share a name.
- Root names are unique per owner. You cannot have two datasets called `my-study`, but another user can.
- A product's `isFile` must match the node that uses it. A file node takes a file product and a product-backed directory takes a directory product. A mismatch is `400 Bad Request`.
- A name collision is `409 Conflict`.

---

## Building a dataset

Register the data products first (see [data-product.md](data-product.md)), then lay out the tree.

```bash
# A root: a new dataset owned by the caller
ROOT_ID=$(curl -s -X POST localhost:9095/api/v1/virtual-data-directories \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{ "directoryName": "my-study" }' | jq -r '.virtualDataDirectoryId')

# A container inside it
INPUTS_ID=$(curl -s -X POST localhost:9095/api/v1/virtual-data-directories \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{ "directoryName": "inputs", "parentDirectoryId": "'"$ROOT_ID"'" }' | jq -r '.virtualDataDirectoryId')

# A file in the container, standing for a file product
curl -s -X POST localhost:9095/api/v1/virtual-data-files \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{ "fileName": "T1050.fasta", "parentDirectoryId": "'"$INPUTS_ID"'", "dataProductId": "'"$FASTA_DATA_ID"'" }'

# A product-backed directory, standing for a directory product
curl -s -X POST localhost:9095/api/v1/virtual-data-directories \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{ "directoryName": "reference-db", "parentDirectoryId": "'"$ROOT_ID"'", "dataProductId": "'"$DB_DATA_ID"'" }'
```

To place a product in a tree, you need `READ` on the product and `WRITE` on the directory you are adding to.

### Browsing

```
GET /api/v1/virtual-data-directories/me                          # roots of your own datasets
GET /api/v1/virtual-data-directories/shared-with-me              # directories shared with you
GET /api/v1/virtual-data-directories/{id}                        # one directory, without contents
GET /api/v1/virtual-data-directories/{id}/contents               # one directory plus its direct children
GET /api/v1/virtual-data-directories/{id}/files                  # just the files directly under it
GET /api/v1/virtual-data-files/{id}                              # one file
```

`/contents` returns **one level** (`virtualDataFiles` and `virtualDataDirectories`). To walk a deeper tree, call it again on each child directory. Every response includes a `permission` field saying what you hold on that node.

### Renaming and moving

`PUT` replaces a node, and the same call renames it, moves it (by changing `parentDirectoryId`) or points it at a different product.

- An update replaces the whole node. To rename a nested directory, send its `parentDirectoryId` back too. Leaving it out moves the directory out to become a root.
- A move needs `WRITE` on both the old and the new parent. A directory cannot be moved into its own subtree.
- A container that still has entries cannot be turned into a product-backed directory (`409 Conflict`).

### Deleting

| Delete | Removes | Needs |
|---|---|---|
| A file (`DELETE /api/v1/virtual-data-files/{id}`) | that entry | `WRITE` on its directory |
| A directory (`DELETE /api/v1/virtual-data-directories/{id}`) | the directory, **everything beneath it**, and every share made anywhere in that subtree | ownership (yours or an ancestor's), or admin |

Deleting virtual entries never deletes data products or data. The products stay registered and their data stays where it is. In the other direction, a product that a virtual dataset still uses cannot be deleted until those entries are removed.

---

## Ownership and sharing

A dataset belongs to whoever created its root. Ownership covers the whole tree. A directory that someone else adds to your dataset, using a `WRITE` share, still belongs to you, not to them.

Directories are shared like other records ([sharing.md](sharing.md)), with one difference: **a share applies to the whole subtree** below the directory it is made on.

| Standing on a directory | May do |
|---|---|
| `READ` | browse it and everything below it |
| `WRITE` | the above, and add, rename, move and remove entries inside it |
| Owner (or admin) | the above, delete it, and manage its shares |

- A user can share part of a dataset by sharing an interior directory. The grantee sees that directory as their top level in `/shared-with-me` and cannot see anything above it.
- The strongest grant anywhere in a node's ancestry applies. `WRITE` on the root and `READ` on a nested directory still means `WRITE` there.
- Files carry no shares of their own. Access to a file comes from its directory.
- `GET .../{id}/shares` lists only the grants made **at that node**, not the ones it inherits from above.

**Sharing a dataset does not share its data.** A virtual share lets someone see the tree: names, layout and which product each entry uses. Downloading a product's content, or using it in a run, is still governed by the product's own shares. To let a collaborator use the data, share the products (or their storage) as well. See [data-product.md](data-product.md#ownership-and-sharing).

---

## Endpoints

```
GET    /api/v1/virtual-data-directories                          (admin)
GET    /api/v1/virtual-data-directories/me
GET    /api/v1/virtual-data-directories/shared-with-me
POST   /api/v1/virtual-data-directories
GET    /api/v1/virtual-data-directories/{id}                     (READ)
GET    /api/v1/virtual-data-directories/{id}/contents            (READ)
GET    /api/v1/virtual-data-directories/{id}/files               (READ)
PUT    /api/v1/virtual-data-directories/{id}                     (WRITE)
DELETE /api/v1/virtual-data-directories/{id}                     (owner)

POST   /api/v1/virtual-data-files                                (WRITE on parent)
GET    /api/v1/virtual-data-files/{id}                           (READ on parent)
PUT    /api/v1/virtual-data-files/{id}                           (WRITE on parent)
DELETE /api/v1/virtual-data-files/{id}                           (WRITE on parent)

GET|POST|PUT|DELETE /api/v1/virtual-data-directories/{id}/shares[/{sharingId}]   (owner)
```

Files are addressed at the top level, not under their directory, so a file keeps its URL when it is moved.
