"use client";

import { useRef, useState } from "react";
import {
  Alert,
  Card,
  ConfirmButton,
  Field,
  Id,
  Modal,
  numOrNull,
  PageHead,
  strOrNull,
  Table,
  Tabs,
} from "@/components/ui";
import { api, errorText } from "@/lib/api";
import { CreateCredential } from "@/components/CreateCredential";
import { ShareDialog } from "@/components/ShareDialog";
import { useAction, useResource } from "@/lib/hooks";
import { useSession } from "@/lib/session";
import type { ClusterPartition, SlurmCluster, SlurmClusterConfig, SSHCredential } from "@/lib/types";

type Tab = "clusters" | "configs";

export default function ComputePage() {
  const [tab, setTab] = useState<Tab>("clusters");
  return (
    <div className="stack">
      <PageHead
        title="Compute clusters"
        sub="Slurm clusters are the shared catalogue; a cluster config is your login on one of them."
      />
      <Tabs
        value={tab}
        onChange={setTab}
        tabs={[
          { key: "clusters", label: "Clusters" },
          { key: "configs", label: "Cluster configs" },
        ]}
      />
      {tab === "clusters" ? <Clusters /> : <Configs />}
    </div>
  );
}

function Clusters() {
  const { isAdmin } = useSession();
  const clusters = useResource(() => api.get<SlurmCluster[]>("/api/v1/slurm-clusters"));
  const [creating, setCreating] = useState(false);
  const [viewing, setViewing] = useState<SlurmCluster | null>(null);
  const [error, setError] = useState<string | null>(null);

  return (
    <>
      <Alert>{clusters.error || error}</Alert>
      <Card
        title="Slurm clusters"
        flush
        actions={
          isAdmin ? (
            <button className="primary" onClick={() => setCreating(true)}>
              Add cluster
            </button>
          ) : (
            <span className="small muted">Adding clusters requires an admin</span>
          )
        }
      >
        <Table
          rows={clusters.data}
          rowKey={(c) => c.slurmClusterId}
          empty="No clusters registered."
          columns={[
            {
              header: "Name",
              cell: (c) => (
                <button className="link" onClick={() => setViewing(c)}>
                  {c.clusterName}
                </button>
              ),
            },
            { header: "ID", cell: (c) => <Id value={c.slurmClusterId} /> },
            { header: "Head node", cell: (c) => <code>{`${c.headnodeHost}:${c.headnodePort}`}</code> },
            { header: "Data host", cell: (c) => (c.dataHost ? <code>{`${c.dataHost}:${c.dataPort ?? 22}`}</code> : "—") },
            { header: "Partitions", cell: (c) => c.partitions.map((p) => p.name).join(", ") || "—" },
            {
              header: "",
              className: "actions",
              cell: (c) =>
                isAdmin && (
                  <ConfirmButton
                    onConfirm={async () => {
                      setError(null);
                      try {
                        await api.del(`/api/v1/slurm-clusters/${c.slurmClusterId}`);
                        await clusters.reload();
                      } catch (e) {
                        setError(errorText(e));
                      }
                    }}
                  >
                    Delete
                  </ConfirmButton>
                ),
            },
          ]}
        />
      </Card>
      {creating && (
        <CreateCluster
          onClose={() => setCreating(false)}
          onCreated={() => {
            setCreating(false);
            void clusters.reload();
          }}
        />
      )}
      {viewing && <ClusterDetail cluster={viewing} onClose={() => setViewing(null)} />}
    </>
  );
}

function ClusterDetail({ cluster, onClose }: { cluster: SlurmCluster; onClose: () => void }) {
  return (
    <Modal title={cluster.clusterName} onClose={onClose} wide>
      <div className="stack">
        <dl className="kv">
          <dt>ID</dt>
          <dd>
            <code>{cluster.slurmClusterId}</code>
          </dd>
          <dt>Description</dt>
          <dd>{cluster.clusterDescription || "—"}</dd>
          <dt>Head node</dt>
          <dd>
            <code>{`${cluster.headnodeHost}:${cluster.headnodePort}`}</code>
          </dd>
          <dt>Slurm home</dt>
          <dd>{cluster.slurmHome || "—"}</dd>
          <dt>Data host</dt>
          <dd>{cluster.dataHost ? `${cluster.dataHost}:${cluster.dataPort ?? 22}` : "—"}</dd>
        </dl>
        <Card title="Partitions" flush>
          <Table
            rows={cluster.partitions}
            rowKey={(p) => p.partitionId ?? p.name}
            empty="No partitions."
            columns={[
              { header: "Name", cell: (p) => <strong>{p.name}</strong> },
              { header: "Default", cell: (p) => (p.isDefaultQueue ? "yes" : "") },
              { header: "Max run (min)", cell: (p) => p.maxRunTime ?? "—" },
              { header: "Max nodes", cell: (p) => p.maxNodes ?? "—" },
              { header: "CPU / node", cell: (p) => p.cpuPerNode ?? "—" },
              { header: "Max memory", cell: (p) => p.maxMemory ?? "—" },
              { header: "GRES", cell: (p) => p.gres ?? "—" },
            ]}
          />
        </Card>
      </div>
    </Modal>
  );
}

// The partition editor keeps every field as text so an empty box means "not set".
interface PartitionDraft {
  name: string;
  description: string;
  maxRunTime: string;
  defaultWalltime: string;
  maxNodes: string;
  maxProcessors: string;
  cpuPerNode: string;
  maxMemory: string;
  gres: string;
  nodes: string;
  isDefaultQueue: boolean;
}

const emptyPartition = (): PartitionDraft => ({
  name: "",
  description: "",
  maxRunTime: "",
  defaultWalltime: "",
  maxNodes: "",
  maxProcessors: "",
  cpuPerNode: "",
  maxMemory: "",
  gres: "",
  nodes: "",
  isDefaultQueue: false,
});

const text = (v: string | number | null | undefined) => (v === null || v === undefined ? "" : String(v));

function draftFromPartition(p: ClusterPartition): PartitionDraft {
  return {
    name: p.name,
    description: text(p.description),
    maxRunTime: text(p.maxRunTime),
    defaultWalltime: text(p.defaultWalltime),
    maxNodes: text(p.maxNodes),
    maxProcessors: text(p.maxProcessors),
    cpuPerNode: text(p.cpuPerNode),
    maxMemory: text(p.maxMemory),
    gres: text(p.gres),
    nodes: text(p.nodes),
    isDefaultQueue: !!p.isDefaultQueue,
  };
}

function partitionFromDraft(p: PartitionDraft): ClusterPartition {
  return {
    name: p.name,
    description: strOrNull(p.description),
    maxRunTime: numOrNull(p.maxRunTime),
    defaultWalltime: numOrNull(p.defaultWalltime),
    maxNodes: numOrNull(p.maxNodes),
    maxProcessors: numOrNull(p.maxProcessors),
    cpuPerNode: numOrNull(p.cpuPerNode),
    maxMemory: numOrNull(p.maxMemory),
    gres: strOrNull(p.gres),
    nodes: strOrNull(p.nodes),
    isDefaultQueue: p.isDefaultQueue,
  };
}

const PARTITION_FIELDS: { key: Exclude<keyof PartitionDraft, "isDefaultQueue">; label: string; numeric?: boolean; placeholder?: string }[] = [
  { key: "name", label: "Name" },
  { key: "maxRunTime", label: "Max run time (min)", numeric: true },
  { key: "defaultWalltime", label: "Default wall time (min)", numeric: true },
  { key: "maxNodes", label: "Max nodes", numeric: true },
  { key: "maxProcessors", label: "Max processors", numeric: true },
  { key: "cpuPerNode", label: "CPUs per node", numeric: true },
  { key: "maxMemory", label: "Max memory (MB)", numeric: true },
  { key: "gres", label: "GRES", placeholder: "gpu:a100:4" },
  { key: "nodes", label: "Nodes", placeholder: "cn[001-128]" },
];

// CreateCluster registers a cluster. Once the head node is filled in, a login on it —
// a user and an SSH credential — lets the server read the partitions off the cluster
// with scontrol rather than having them typed in, and can be kept as the admin's own
// cluster config.
function CreateCluster({ onClose, onCreated }: { onClose: () => void; onCreated: () => void }) {
  const creds = useResource(() => api.get<SSHCredential[]>("/api/v1/ssh-credentials"));
  const [f, setF] = useState({
    clusterName: "",
    clusterDescription: "",
    headnodeHost: "",
    headnodePort: "22",
    slurmHome: "",
    dataHost: "",
    dataPort: "",
  });
  const [login, setLogin] = useState({ loginUser: "", sshCredentialId: "", saveConfig: true, workRoot: "" });
  const [partitions, setPartitions] = useState<PartitionDraft[]>([]);
  const [registeringKey, setRegisteringKey] = useState(false);
  const [found, setFound] = useState<string | null>(null);
  const set = (patch: Partial<typeof f>) => setF((x) => ({ ...x, ...patch }));
  const setLoginField = (patch: Partial<typeof login>) => setLogin((x) => ({ ...x, ...patch }));
  const setPart = (i: number, patch: Partial<PartitionDraft>) =>
    setPartitions((ps) => ps.map((p, j) => (j === i ? { ...p, ...patch } : p)));

  const hostReady = f.clusterName.trim() !== "" && f.headnodeHost.trim() !== "";
  const loginReady = login.loginUser.trim() !== "" && login.sshCredentialId !== "";
  const credential = creds.data?.find((c) => c.sshCredentialId === login.sshCredentialId);

  const discover = useAction(async () => {
    setFound(null);
    const res = await api.post<{ partitions: ClusterPartition[] }>("/api/v1/slurm-clusters/discover-partitions", {
      headnodeHost: f.headnodeHost,
      headnodePort: numOrNull(f.headnodePort) ?? 22,
      slurmHome: strOrNull(f.slurmHome),
      loginUser: login.loginUser,
      sshCredentialId: login.sshCredentialId,
    });
    setPartitions(res.partitions.map(draftFromPartition));
    setFound(
      `Found ${res.partitions.length} partition${res.partitions.length === 1 ? "" : "s"} on ${f.headnodeHost}. Review them below before adding the cluster.`,
    );
  });

  // Kept across attempts: when the cluster saves but the config does not, a retry
  // saves only the config rather than registering the cluster twice.
  const savedCluster = useRef<SlurmCluster | null>(null);
  const save = useAction(async () => {
    const cluster = (savedCluster.current ??= await api.post<SlurmCluster>("/api/v1/slurm-clusters", {
      clusterName: f.clusterName,
      clusterDescription: strOrNull(f.clusterDescription),
      headnodeHost: f.headnodeHost,
      headnodePort: numOrNull(f.headnodePort) ?? 0,
      slurmHome: strOrNull(f.slurmHome),
      dataHost: strOrNull(f.dataHost),
      dataPort: numOrNull(f.dataPort),
      partitions: partitions.map(partitionFromDraft),
    }));
    if (loginReady && login.saveConfig) {
      try {
        await api.post("/api/v1/slurm-cluster-configs", {
          name: `${f.clusterName} (${login.loginUser})`,
          slurmClusterId: cluster.slurmClusterId,
          loginUser: login.loginUser,
          workRoot: login.workRoot,
          sshCredentialId: login.sshCredentialId,
        });
      } catch (e) {
        throw new Error(`The cluster was added, but saving your cluster config failed: ${errorText(e)}`);
      }
    }
    onCreated();
  });

  // Once the cluster exists, closing still refreshes the list it now belongs in.
  const close = () => (savedCluster.current ? onCreated() : onClose());

  return (
    <Modal
      title="Add Slurm cluster"
      wide
      onClose={close}
      footer={
        <>
          <button onClick={close}>{savedCluster.current ? "Close" : "Cancel"}</button>
          <button
            className="primary"
            disabled={save.busy || discover.busy || !hostReady || (loginReady && login.saveConfig && !login.workRoot.trim())}
            onClick={() => void save.run()}
          >
            {save.busy ? "Saving…" : savedCluster.current ? "Retry saving cluster config" : "Add cluster"}
          </button>
        </>
      }
    >
      <div className="form">
        <fieldset>
          <legend>1. Cluster</legend>
          <div className="form-grid">
            <Field label="Cluster name">
              <input value={f.clusterName} onChange={(e) => set({ clusterName: e.target.value })} />
            </Field>
            <Field label="Head node host">
              <input
                value={f.headnodeHost}
                onChange={(e) => set({ headnodeHost: e.target.value })}
                placeholder="login.cluster.edu"
              />
            </Field>
            <Field label="Head node port">
              <input type="number" value={f.headnodePort} onChange={(e) => set({ headnodePort: e.target.value })} />
            </Field>
            <Field label="Slurm home" hint="optional">
              <input value={f.slurmHome} onChange={(e) => set({ slurmHome: e.target.value })} placeholder="/usr/bin" />
            </Field>
            <Field label="Data host" hint="optional">
              <input value={f.dataHost} onChange={(e) => set({ dataHost: e.target.value })} />
            </Field>
            <Field label="Data port" hint="optional">
              <input type="number" value={f.dataPort} onChange={(e) => set({ dataPort: e.target.value })} />
            </Field>
          </div>
          <div style={{ marginTop: 12 }}>
            <Field label="Description" hint="optional">
              <input value={f.clusterDescription} onChange={(e) => set({ clusterDescription: e.target.value })} />
            </Field>
          </div>
        </fieldset>

        <fieldset disabled={!hostReady}>
          <legend>2. Cluster login</legend>
          {!hostReady ? (
            <span className="small muted">Enter the cluster name and head node host first.</span>
          ) : (
            <div className="form">
              <span className="small muted">
                Log in to {f.headnodeHost} to read its partitions with <code>scontrol</code>. Optional — you can also
                enter partitions by hand.
              </span>
              <div className="form-grid">
                <Field label="Login user">
                  <input value={login.loginUser} onChange={(e) => setLoginField({ loginUser: e.target.value })} />
                </Field>
                <Field label="SSH credential">
                  <div className="row" style={{ flexWrap: "nowrap" }}>
                    <select
                      value={login.sshCredentialId}
                      onChange={(e) => setLoginField({ sshCredentialId: e.target.value })}
                    >
                      <option value="">Select a credential…</option>
                      {creds.data?.map((c) => (
                        <option key={c.sshCredentialId} value={c.sshCredentialId}>
                          {c.sshCredentialName}
                          {c.fullInteractive ? " (interactive)" : ""}
                        </option>
                      ))}
                    </select>
                    <button type="button" className="small" onClick={() => setRegisteringKey(true)}>
                      New…
                    </button>
                  </div>
                </Field>
              </div>
              <div className="row">
                <button
                  type="button"
                  className="primary"
                  disabled={!loginReady || discover.busy}
                  onClick={() => void discover.run()}
                >
                  {discover.busy ? "Reading partitions…" : "Discover partitions"}
                </button>
                {discover.busy && credential?.fullInteractive && (
                  <span className="small muted">Answer the login prompt when it appears.</span>
                )}
              </div>
              <Alert kind="info">{found}</Alert>
              <Alert>{discover.error}</Alert>
              {loginReady && (
                <div className="form-grid" style={{ alignItems: "end" }}>
                  <label className="check">
                    <input
                      type="checkbox"
                      checked={login.saveConfig}
                      onChange={(e) => setLoginField({ saveConfig: e.target.checked })}
                    />
                    Also save this login as my cluster config
                  </label>
                  {login.saveConfig && (
                    <Field label="Work root">
                      <input
                        value={login.workRoot}
                        onChange={(e) => setLoginField({ workRoot: e.target.value })}
                        placeholder={`/scratch/${login.loginUser || "me"}/airavata`}
                      />
                    </Field>
                  )}
                </div>
              )}
            </div>
          )}
        </fieldset>

        <fieldset>
          <legend>3. Partitions</legend>
          <div className="stack">
            {partitions.map((p, i) => (
              <div key={i} className="form-grid" style={{ alignItems: "end", paddingBottom: 12, borderBottom: "1px solid var(--border)" }}>
                {PARTITION_FIELDS.map((pf) => (
                  <Field key={pf.key} label={pf.label}>
                    <input
                      type={pf.numeric ? "number" : "text"}
                      value={p[pf.key]}
                      placeholder={pf.placeholder}
                      onChange={(e) => setPart(i, { [pf.key]: e.target.value })}
                    />
                  </Field>
                ))}
                <label className="check">
                  <input
                    type="checkbox"
                    checked={p.isDefaultQueue}
                    onChange={(e) => setPart(i, { isDefaultQueue: e.target.checked })}
                  />
                  Default queue
                </label>
                <button className="small danger" onClick={() => setPartitions((ps) => ps.filter((_, j) => j !== i))}>
                  Remove
                </button>
              </div>
            ))}
            <div>
              <button className="small" onClick={() => setPartitions((ps) => [...ps, emptyPartition()])}>
                + Add partition
              </button>
            </div>
          </div>
        </fieldset>
        <Alert>{save.error || creds.error}</Alert>
      </div>
      {registeringKey && (
        <CreateCredential
          onClose={() => setRegisteringKey(false)}
          onCreated={(c) => {
            setRegisteringKey(false);
            void creds.reload().then(() => setLoginField({ sshCredentialId: c.sshCredentialId }));
          }}
        />
      )}
    </Modal>
  );
}

function Configs() {
  const mine = useResource(() => api.get<SlurmClusterConfig[]>("/api/v1/slurm-cluster-configs/me"));
  const shared = useResource(() => api.get<SlurmClusterConfig[]>("/api/v1/slurm-cluster-configs/shared-with-me"));
  const [creating, setCreating] = useState(false);
  const [sharing, setSharing] = useState<SlurmClusterConfig | null>(null);
  const [error, setError] = useState<string | null>(null);

  const columns = (owned: boolean) => [
    {
      header: "Name",
      cell: (c: SlurmClusterConfig) => <strong>{c.name || c.slurmCluster?.clusterName || "Unnamed"}</strong>,
    },
    { header: "ID", cell: (c: SlurmClusterConfig) => <Id value={c.slurmClusterConfigId} /> },
    { header: "Cluster", cell: (c: SlurmClusterConfig) => c.slurmCluster?.clusterName ?? <Id value={c.slurmClusterId} /> },
    { header: "Login", cell: (c: SlurmClusterConfig) => <code>{c.loginUser}</code> },
    { header: "Work root", cell: (c: SlurmClusterConfig) => <code>{c.workRoot}</code> },
    { header: "Credential", cell: (c: SlurmClusterConfig) => c.sshCredential?.sshCredentialName ?? <Id value={c.sshCredentialId} /> },
    owned
      ? {
          header: "",
          className: "actions",
          cell: (c: SlurmClusterConfig) => (
            <div className="row" style={{ justifyContent: "flex-end", flexWrap: "nowrap" }}>
              <button className="small" onClick={() => setSharing(c)}>
                Share
              </button>
              <ConfirmButton
                onConfirm={async () => {
                  setError(null);
                  try {
                    await api.del(`/api/v1/slurm-cluster-configs/${c.slurmClusterConfigId}`);
                    await mine.reload();
                  } catch (e) {
                    setError(errorText(e));
                  }
                }}
              >
                Delete
              </ConfirmButton>
            </div>
          ),
        }
      : { header: "Permission", cell: (c: SlurmClusterConfig) => <span className="badge">{c.permission ?? "READ"}</span> },
  ];

  return (
    <>
      <Alert>{mine.error || shared.error || error}</Alert>
      <Card
        title="My cluster configs"
        flush
        actions={
          <button className="primary" onClick={() => setCreating(true)}>
            Add cluster config
          </button>
        }
      >
        <Table rows={mine.data} rowKey={(c) => c.slurmClusterConfigId} empty="No cluster configs yet." columns={columns(true)} />
      </Card>
      <Card title="Shared with me" flush>
        <Table
          rows={shared.data}
          rowKey={(c) => c.slurmClusterConfigId}
          empty="Nothing shared with you."
          columns={columns(false)}
        />
      </Card>
      {creating && (
        <CreateConfig
          onClose={() => setCreating(false)}
          onCreated={() => {
            setCreating(false);
            void mine.reload();
          }}
        />
      )}
      {sharing && (
        <ShareDialog
          title={sharing.name || sharing.slurmCluster?.clusterName || "cluster config"}
          resourcePath={`/api/v1/slurm-cluster-configs/${sharing.slurmClusterConfigId}`}
          permissionHelp={{
            READ: "can run jobs through this config, using its login and SSH credential.",
            WRITE: "can also edit it.",
          }}
          onClose={() => setSharing(null)}
        />
      )}
    </>
  );
}

function CreateConfig({ onClose, onCreated }: { onClose: () => void; onCreated: () => void }) {
  const clusters = useResource(() => api.get<SlurmCluster[]>("/api/v1/slurm-clusters"));
  const creds = useResource(() => api.get<SSHCredential[]>("/api/v1/ssh-credentials"));
  const [f, setF] = useState({
    name: "",
    description: "",
    slurmClusterId: "",
    loginUser: "",
    workRoot: "",
    sshCredentialId: "",
  });
  const set = (patch: Partial<typeof f>) => setF((x) => ({ ...x, ...patch }));
  const save = useAction(async () => {
    await api.post("/api/v1/slurm-cluster-configs", {
      ...f,
      name: strOrNull(f.name),
      description: strOrNull(f.description),
    });
    onCreated();
  });

  return (
    <Modal
      title="Add cluster config"
      onClose={onClose}
      footer={
        <>
          <button onClick={onClose}>Cancel</button>
          <button className="primary" disabled={save.busy} onClick={() => void save.run()}>
            {save.busy ? "Saving…" : "Add config"}
          </button>
        </>
      }
    >
      <div className="form">
        <Field label="Name" hint="optional">
          <input value={f.name} onChange={(e) => set({ name: e.target.value })} />
        </Field>
        <Field label="Cluster">
          <select value={f.slurmClusterId} onChange={(e) => set({ slurmClusterId: e.target.value })}>
            <option value="">Select a cluster…</option>
            {clusters.data?.map((c) => (
              <option key={c.slurmClusterId} value={c.slurmClusterId}>
                {c.clusterName} ({c.headnodeHost})
              </option>
            ))}
          </select>
        </Field>
        <div className="form-grid">
          <Field label="Login user">
            <input value={f.loginUser} onChange={(e) => set({ loginUser: e.target.value })} />
          </Field>
          <Field label="Work root">
            <input
              value={f.workRoot}
              onChange={(e) => set({ workRoot: e.target.value })}
              placeholder="/scratch/me/airavata"
            />
          </Field>
        </div>
        <Field label="SSH credential" hint={creds.data?.length === 0 ? "register one on the SSH credentials page first" : undefined}>
          <select value={f.sshCredentialId} onChange={(e) => set({ sshCredentialId: e.target.value })}>
            <option value="">Select a credential…</option>
            {creds.data?.map((c) => (
              <option key={c.sshCredentialId} value={c.sshCredentialId}>
                {c.sshCredentialName}
                {c.fullInteractive ? " (interactive)" : ""}
              </option>
            ))}
          </select>
        </Field>
        <Field label="Description" hint="optional">
          <input value={f.description} onChange={(e) => set({ description: e.target.value })} />
        </Field>
        <Alert>{save.error || clusters.error || creds.error}</Alert>
      </div>
    </Modal>
  );
}
