# Data Products

A **data product** is a dataset registered with Airavata: one file or one directory, at a known path on a known storage host. It is how Airavata refers to data. An [application](application.md) run names its data inputs and outputs by data product id rather than by path, and Airavata uses the product to know where to copy the data from or to.

A product records *where* the data is, not the data itself. Registering one does not contact the host, and an output product's path need not exist yet.

```
SSH credential  ◀──  Data storage  ◀──  Data product  ◀──  Process input / output mapping
 (key pair)          host, port,        path, file or
                     login user         directory
```

Setting up data takes two steps:

1. Register a **data storage**. This is the host and account the data is reached through. You need one per host and account, and you can reuse it for many products.
2. Register a **data product** on that storage for each file or directory.

---

## Data storage

A data storage says how to reach a host. The supported storage type is **SCP**: files are copied over SSH.

| Field | Required | Meaning |
|---|---|---|
| `dataName` | yes | A name for the storage, e.g. `lab-sequences` or `expanse-scratch`. |
| `hostName` | yes | The host the data sits on. |
| `port` | no | The SSH port (default `22`). |
| `loginUser` | yes | The account to log in as. |
| `sshCredentialId` | yes | An [SSH credential](api.md#ssh-credentials) **you own**, whose public key is authorised for `loginUser` on the host. |

A product names no credential of its own. The host, the account and the key all come from its storage, so many products can be registered on one storage without repeating any of them. One SSH credential can be reused by several storages when its public key is installed for several accounts.

```bash
STORAGE_ID=$(curl -s -X POST localhost:9095/api/v1/scp-data-storages \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "dataName": "lab-sequences",
    "hostName": "data.lab.example.edu",
    "port": 22,
    "loginUser": "labuser",
    "sshCredentialId": "'"$SSH_CREDENTIAL_ID"'"
  }' | jq -r '.dataId')
```

A storage cannot be deleted while products are still registered on it (`409 Conflict`).

---

## Data product

| Field | Required | Meaning |
|---|---|---|
| `dataName` | yes | A name for the dataset. |
| `dataDescription` | no | Free text. |
| `isFile` | yes | `true` for a single file, `false` for a directory. |
| `path` | yes | The absolute path of the file or directory on the storage host. |
| `dataStorageId` | yes | The storage it lives on. You must be able to read that storage. |
| `dataStorageType` | no | `SCP` (the default). |

The server sets the remaining fields: `dataId`, `ownerId` (taken from your token), `provisionStatus` and `createdAt`.

```bash
DATA_ID=$(curl -s -X POST localhost:9095/api/v1/data-products \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "dataName": "T1050-sequence",
    "dataDescription": "Target protein sequence in FASTA format",
    "isFile": true,
    "path": "/data/sequences/T1050.fasta",
    "dataStorageId": "'"$STORAGE_ID"'"
  }' | jq -r '.dataId')
```

### Files and directories

`isFile` decides which application inputs and outputs a product can be used for:

| Product | Can be used as |
|---|---|
| file (`isFile: true`) | a `FILE` input, an element of a `FILE_LIST` input, or a `FILE` output |
| directory (`isFile: false`) | a `DIRECTORY` input, a `DIRECTORY` output, or a `FILE_LIST` output (the files are collected into it) |

### Provision status

`provisionStatus` tracks whether the data the product describes is actually at its path:

| Status | Meaning |
|---|---|
| `REGISTERD` | Registered. The data may or may not be there yet. This is the initial state. |
| `PROVISIONING` | The data is being written. |
| `PROVISIONED` | The data is at its path, e.g. after a successful upload. |
| `DEPROVISIONING` / `DEPROVISIONED` | The data is being removed or has been removed. |
| `FAILED` | Provisioning failed. |

The server moves this status. A request cannot set it.

---

## Moving content

A data product's content gets to its path in one of three ways:

- **It is already there.** Register the product against the existing path.
- **Upload it through Airavata.** `PUT /api/v1/data-products/{dataId}/content` sends the bytes to the product's path on its storage. You can send a raw body or a multipart part named `file`. Missing parent directories are created, and the limit is 5 GiB. A successful upload marks the product `PROVISIONED`. `GET .../content` downloads it. Both work for file products only.
- **A run writes it.** When a product is mapped as an application output, Airavata copies the job's result to the product's path after the job finishes.

Transfers use the storage's credential on the user's behalf. A user who has been shared a product can download it without having access to the storage or seeing the key.

---

## Ownership and sharing

A product belongs to whoever registered it. Others reach it through a share, granted to a user or a [group](api.md#groups):

| Standing | May do |
|---|---|
| `READ` | read the product and download its content |
| `WRITE` | the above, edit the product and upload content |
| Owner (or admin) | the above, delete it and manage its shares |

Storages are shared the same way. `READ` on a storage lets another user register products on it. See [sharing.md](sharing.md) for the request format.

Listings never expose products you cannot reach. `/api/v1/data-products/me` lists what you own, and `/shared-with-me` lists what has been shared with you.

---

## Using data products in an application run

A run's `inputMappings` and `outputMappings` carry product ids for data-typed declarations:

```json
"inputMappings": [
  { "templateInputId": "<FILE input>",      "value": "<file dataId>" },
  { "templateInputId": "<FILE_LIST input>", "value": "{\"values\":[\"<file dataId>\",\"<file dataId>\"]}" },
  { "templateInputId": "<DIRECTORY input>", "value": "<directory dataId>" }
],
"outputMappings": [
  { "templateOutputId": "<FILE output>",      "value": "<file dataId>" },
  { "templateOutputId": "<DIRECTORY output>", "value": "<directory dataId>" }
]
```

At launch, Airavata copies each input product from its storage into the run's work directory on the cluster. After the job finishes, it copies each output from the work directory to its product's storage and path. The input and output products can be on different storages. For example, inputs can come from a lab data server and results can go to the cluster's scratch filesystem. See [application.md](application.md) for how the run section sees these paths.

To arrange products from different storages into one browsable folder tree, see [virtual-data.md](virtual-data.md).

Full request and response bodies are in [api.md](api.md#data-products) and [api.md](api.md#scp-data-storages).
