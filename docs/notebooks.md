# Notebooks on a cluster

The dashboard's **Notebooks** page runs Python in the browser on Pyodide by default. It can also run notebooks on a cluster: on the Jupyter Server that a running process's job starts, with the job's files in the file browser and its kernels running on the compute node.

The job reaches Airavata the way a [CyberShuttle](https://github.com/cyber-shuttle/cs-plane) session reaches cs-plane. The job runs [Linkspan](https://github.com/cyber-shuttle/linkspan), which dials **out** to Airavata over one WebSocket, the *link*, and Airavata reaches the job's Jupyter Server through it. The compute node opens no inbound port, and nothing runs on the login node besides the usual `sbatch`.

```
browser ──HTTPS──▶ Airavata ──link (WebSocket, yamux)──▶ Linkspan ──loopback──▶ Jupyter Server
                   /api/v1/processes/{id}/jupyter/      dialled from the job        on the compute node
```

## What every batch job is given

Each time a `BATCH_JOB` process is submitted, Airavata issues it a fresh link token and Jupyter token and picks two loopback ports from the process id. It writes them to `.airavata-link.env` in the work directory, readable only by the cluster account (mode `0600`), and the job script sources that file before the run section. The tokens never appear in the script itself, which Airavata logs and stores on the job submission task.

| Environment variable | Contents |
|---|---|
| `CS_LINK_URL` | The link URL, `wss://<public URL>/api/v1/processes/<process id>/link`, built by Airavata for this process |
| `LINKSPAN_LINK_TOKEN` | The link token, which Linkspan reads from the environment |
| `CS_CONTROL_PORT` | The loopback port for Linkspan's own API |
| `CS_JUPYTER_PORT` | The loopback port for the Jupyter Server |
| `JUPYTER_TOKEN` | The Jupyter token, which Linkspan passes to the Jupyter Server it starts |

The run section can also read the non-secret values as template variables: `{{ link_url }}`, `{{ link_control_port }}` and `{{ link_jupyter_port }}`.

A resubmission is issued new tokens, so an earlier job of the same process can no longer link. A job that does not run Linkspan ignores all of this.

Compute nodes must be able to reach Airavata at `AIRAVATA_PUBLIC_URL` (see [INSTALL.md](https://github.com/apache/airavata/blob/master/INSTALL.md#server)). Use `https://` in production, so the link is `wss://`.

## A deployment that runs Linkspan

Any [application deployment](application.md#application-deployment) can serve notebooks. Its run section writes a Linkspan workflow that starts a Jupyter Server on `CS_JUPYTER_PORT`, then runs Linkspan with the link transport:

```jinja
cat > workflow.yaml <<EOF
tasks:
  - on: start
    steps:
      - name: Jupyter
        action: jupyter.sessions.start
        params:
          root_dir: {{ work_dir }}
          addr: 127.0.0.1:${CS_JUPYTER_PORT}
EOF

$HOME/.cybershuttle/bin/linkspan --port "$CS_CONTROL_PORT" --workflow workflow.yaml --tunnel-enable --tunnel-mode=link --tunnel-link-args="--url $CS_LINK_URL"
```

This needs Linkspan 0.22.0 or newer, the first release with the link transport, installed on the cluster. See [Linkspan's README](https://github.com/cyber-shuttle/linkspan#installation). The first Jupyter Server on an account builds a Python environment under `~/.cybershuttle`, which takes a few minutes and needs outbound access to `astral.sh`, `github.com` and `pypi.org`. The job lasts as long as the Jupyter Server, or until its walltime.

`root_dir` decides which folder the file browser opens; `{{ work_dir }}` is the run's work directory, and any absolute path the account can read works.

## Opening it

On the Notebooks page, **Run notebooks** lists the signed-in user's processes that are submitted or running. Choosing one waits for its Linkspan to link, then opens the job's Jupyter Server: the file browser shows the job's files, and notebooks run on a `python3` kernel on the compute node. The kernel is shut down when the page is left or another source is chosen. Choosing **In this browser** goes back to Pyodide and the notebooks kept in the browser.

If Airavata restarts, or the link drops, Linkspan redials on its own and the page picks the server up again.

## API

| Route | Authentication | Purpose |
|---|---|---|
| `GET /api/v1/processes/{processId}/link` | The link token, as the WebSocket subprotocol `link.<token>` | Linkspan's link. Airavata selects the subprotocol `cybershuttle.v1`. |
| `GET /api/v1/processes/{processId}/jupyter-server` | Bearer token of the process owner | `{processId, linked, baseUrl, token}`: where to reach the Jupyter Server and its token |
| `/api/v1/processes/{processId}/jupyter/…` | The Jupyter token, as `Authorization: token <token>` or `?token=` | Proxy to the job's Jupyter Server, including kernel WebSockets. `502` while the job is not linked. |

Only the owner of a process can read its Jupyter token, because the token runs code as the owner's cluster account. The proxy compares the token before forwarding, and the Jupyter Server checks it again. Airavata's bearer tokens are never sent to the job. Other users on a shared node can reach the job's loopback ports, but they still need the Jupyter token to use the server ([Linkspan's security model](https://github.com/cyber-shuttle/linkspan/blob/main/SECURITY.md#security-model)).

The link protocol is Linkspan's, as frozen in its [compatibility document](https://github.com/cyber-shuttle/linkspan/blob/main/docs/COMPATIBILITY.md): yamux runs over binary WebSocket frames, with Airavata as the yamux client. For each stream Airavata writes the target port as two big-endian bytes, and Linkspan answers `1` and carries the stream if one of its own servers listens there, or answers `0`.

