# Interactive Authentication

Most HPC systems no longer accept an SSH key on its own. Logging in to a cluster usually also asks for a password, a one-time code or a Duo push. This **multi-factor authentication** is designed to need a person. Tools that reach the cluster for you, such as job submitters, file transfer clients and workflow engines, either have to stop and wait for that person or can't be used at all.

Airavata supports both. Runs remain an automated workflow (stage in, submit, monitor, stage out), and when a cluster asks for a second factor, Airavata pauses that login, asks **you** for the answer, and then continues. You answer once per login, not once per command. Everything else in the run is automated.

---

## How it maps to an SSH credential

Interactive authentication is a property of an [SSH credential](api.md#ssh-credentials). A credential has an SSH key pair and one flag:

| Field | Meaning |
|---|---|
| `privateKey`, `publicKey`, `passphrase` | The key pair. Airavata always offers it first. |
| `password` | Optional, write-only. Answers the host's first lone password prompt, so you are not asked for it. Ignored unless `fullInteractive`. |
| `fullInteractive` | `true` if the host **also** asks keyboard-interactive questions (password, OTP, Duo) after the key. Default `false`. |

In the dashboard, this is the checkbox **"Host also asks keyboard-interactive questions (password, OTP, Duo) — answer them from this dashboard"** when you register a credential.

A credential is not used on its own. It is attached to whatever logs in to a host:

```
SSH credential (fullInteractive) ──▶ Cluster config  ──▶ runs: job submission, monitoring, staging
                                 ──▶ Data storage    ──▶ data product upload/download, staging
                                 ──▶ (direct use)    ──▶ partition discovery, allocation discovery
```

Any login that uses a `fullInteractive` credential can raise an interactive auth. The prompt goes to the **user whose action caused the login**:

| Login caused by | Prompt goes to |
|---|---|
| A run (staging, submission, monitoring) | the owner of the process |
| Uploading or downloading data product content | the user making the request |
| Partition or allocation discovery | the user making the request |

This affects shared cluster configs and storages. If you share a config that uses an interactive credential, a teammate who launches a run under it is the one asked for the password and second factor. Share an interactive login only with people who can answer its prompts.

When the login starts, Airavata:

1. Connects and offers the key.
2. If the host asks keyboard-interactive questions or a password, creates an **interactive auth**. This records the host, login user, credential, the host's banner and the prompts. The login then waits.
3. Waits for that user to answer, sends the answers to the host, and lets the login proceed.

If the host asks again, for example a Duo prompt after the password, that is a **new** interactive auth with a new id. A stored `password` answers the first password prompt itself; every other prompt, such as a Duo passcode, still goes to the user.

### Answer once per login

An authenticated connection is kept and reused by every operation that logs in with the same host, login user and credential on behalf of the same user. For one run, that covers stage-in, submission, monitoring and stage-out. You answer once, and the steps that follow use the same connection. The connection is closed after five minutes without use, and the next login asks again.

---

## What an interactive auth looks like

```json
{
  "authId": "00000000-0000-4000-8000-000000000001",
  "hostName": "login.cluster.example.edu",
  "port": 22,
  "loginUser": "jdoe",
  "sshCredentialId": "00000000-0000-4000-8000-000000000002",
  "banner": "\nExample HPC Cluster\n\nLogin with your password + Duo multi-factor.\n",
  "name": "",
  "instruction": "",
  "prompts": [ { "text": "Password: ", "echo": false } ],
  "origin": "VS Code · my-project",
  "createdAt": "2026-10-30T14:02:11.482Z"
}
```

| Field | Meaning |
|---|---|
| `hostName`, `port`, `loginUser`, `sshCredentialId` | Which login is waiting. Show these so you know what you are answering. |
| `banner` | The host's pre-login message, as a terminal would print it. |
| `name`, `instruction` | Any heading the host sent with the prompts. |
| `prompts[]` | The questions. `echo: false` means the answer is secret (a password or OTP) and should be masked. |
| `origin` | The client that started the login. See [Which client is asking](#which-client-is-asking). |

The answer is one response per prompt, in order: `{"responses": ["<password>"]}`. You can instead send `{"cancel": true}` to fail the login immediately rather than leave it waiting.

Answers go straight to the SSH handshake. They are **not logged or stored**. A credential's `password` is stored, write-only, and never returned; see [api.md](api.md#ssh-credentials).

An auth exists only while its login is waiting. It disappears once it is answered or the login gives up. Only the user it was raised for can see or answer it. Admins cannot see other users' auths either.

### Which client is asking

A client names itself in an `X-Client` header, such as `X-Client: VS Code · my-project`.

The client of the request that starts a login becomes the auth's `origin`. A run keeps the client that last launched or retried it, so the logins of its later steps carry that name. A launch or retry that names no client keeps the previous one. Any other request that names no client leaves `origin` empty.

The name only labels a login. Every client signed in as you sees every waiting login, whatever its `origin`. When one client answers, the others hear which one did.

---

## If you don't answer in time

A run doesn't need you to be watching the moment it starts. If a prompt is not answered before the login gives up, that step fails like any other failed connection. It is retried according to its retry settings (staging steps retry up to three times, and each retry can raise a new prompt). If the run still fails, **retry the process**:

```bash
curl -s -X POST localhost:9095/api/v1/processes/"$PROCESS_ID"/retry \
  -H "Authorization: Bearer $TOKEN"
```

A retry continues from the step that failed. Steps that already completed are skipped, inputs that were already staged are not copied again, and a submitted job is never submitted twice. The retry raises a new prompt, which you answer, and the run carries on. The dashboard's process view has the same retry action. See [Retry a Process](api.md#retry-a-process).

Partition and allocation discovery work the same way, but the request itself waits for up to five minutes for you to answer. If no answer arrives in time, run the discovery again.

---

## Answering prompts

### Dashboard (recommended)

The dashboard keeps a live connection open for you. When any of your logins is waiting:

- a dialog, **"SSH login needs your answer"**, opens on whatever page you have open. It shows the host, the banner and the prompts, with secret prompts masked.
- the sidebar shows a badge with the number of waiting logins.
- **Interactive auth requests** lists every waiting login, so you can answer them in any order.

Click **Later** to hide a prompt until another one arrives. The login keeps waiting, and the badge still shows it.

Keeping a dashboard tab open while runs are active is the simplest way to make sure prompts are answered on time.

### WebSocket API

Clients that want to be notified as soon as a prompt appears should subscribe:

```
GET /api/v1/ssh-interactive-auths/ws
```

The socket sends your oldest waiting auth as a challenge, or `idle` when nothing is waiting. When that auth is answered, here or anywhere else, or abandoned, it sends the next one.

```
◀ {"type":"challenge","auth":{"authId":"3f2c…","prompts":[{"text":"Password: ","echo":false}], …}}
▶ {"authId":"3f2c…","responses":["<password>"]}
◀ {"type":"accepted","authId":"3f2c…"}
◀ {"type":"challenge","auth":{"authId":"9a1b…","prompts":[{"text":"Duo passcode: ","echo":true}], …}}
◀ {"type":"answered","authId":"9a1b…","by":"VS Code · my-project"}
◀ {"type":"idle"}
```

Here another client answered the Duo prompt, so this socket heard `answered`, with that client's name in `by`.

A client that leaves a prompt to another client sends `{"authId":"3f2c…","skip":true}`. The socket then offers the next waiting auth, or `idle`, and never offers the skipped one again. The login keeps waiting for any other client to answer it. The messages are listed in [api.md](api.md#subscribe-to-interactive-ssh-auths).

Authentication works as it does for any other route. Send `Authorization: Bearer $TOKEN` on the upgrade request. A browser, which cannot set that header, offers the token as a subprotocol instead (`base64url.bearer.<token>` alongside `airavata.v1`).

```bash
websocat -H="Authorization: Bearer $TOKEN" "ws://localhost:9095/api/v1/ssh-interactive-auths/ws"
```

To answer one specific auth, use `GET /api/v1/ssh-interactive-auths/{authId}/ws`. It sends that auth's challenge, accepts one answer and closes.

### REST

For clients that can't keep a socket open:

```
GET  /api/v1/ssh-interactive-auths                     # your waiting logins, oldest first
GET  /api/v1/ssh-interactive-auths/{authId}            # one of them
POST /api/v1/ssh-interactive-auths/{authId}/response   # {"responses": [...]} or {"cancel": true}
```

`204` means Airavata passed the answer to the host. It does not mean the host accepted it. A wrong password shows up as the step failing, or as a new prompt if the host asks again. With REST you have to poll the list to find out when a prompt appears, which is why the WebSocket or the dashboard works better for runs.

Full request, response and error details are in [api.md](api.md#list-interactive-ssh-auths).

### MCP

The [Airavata MCP server](mcp.md) supports interactive auth already. When a run, discovery or transfer is waiting on a prompt, the assistant tells you to answer it in a terminal:

```bash
airavata-mcp ssh-auth
```

This command shows each waiting login and reads secret answers without echoing them. Passwords and codes are never typed into the AI conversation, and the assistant is instructed never to ask for them there.

---

## Setting up an interactive credential

1. **Register the credential** with `fullInteractive: true`. Install its public key for your account on the host as usual. Airavata still offers the key first.
2. **Attach it** to a [cluster config](batch-clusters.md#cluster-config) or a [data storage](data-product.md#data-storage).
3. **Keep a way to answer open**, such as a dashboard tab, a WebSocket client or `airavata-mcp ssh-auth`, whenever you launch runs, discover partitions or allocations, or move data through that login.

If a host needs only a key, leave `fullInteractive` off. Logins then never wait on a person, and runs are fully unattended.
