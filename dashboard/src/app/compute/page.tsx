"use client";

import { useState } from "react";
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

interface PartitionDraft {
  name: string;
  description: string;
  maxRunTime: string;
  maxNodes: string;
  cpuPerNode: string;
  maxMemory: string;
  gres: string;
  isDefaultQueue: boolean;
}

const emptyPartition = (): PartitionDraft => ({
  name: "",
  description: "",
  maxRunTime: "",
  maxNodes: "",
  cpuPerNode: "",
  maxMemory: "",
  gres: "",
  isDefaultQueue: false,
});

function CreateCluster({ onClose, onCreated }: { onClose: () => void; onCreated: () => void }) {
  const [f, setF] = useState({
    clusterName: "",
    clusterDescription: "",
    headnodeHost: "",
    headnodePort: "22",
    slurmHome: "",
    dataHost: "",
    dataPort: "",
  });
  const [partitions, setPartitions] = useState<PartitionDraft[]>([]);
  const set = (patch: Partial<typeof f>) => setF((x) => ({ ...x, ...patch }));
  const setPart = (i: number, patch: Partial<PartitionDraft>) =>
    setPartitions((ps) => ps.map((p, j) => (j === i ? { ...p, ...patch } : p)));

  const save = useAction(async () => {
    const body = {
      clusterName: f.clusterName,
      clusterDescription: strOrNull(f.clusterDescription),
      headnodeHost: f.headnodeHost,
      headnodePort: numOrNull(f.headnodePort) ?? 0,
      slurmHome: strOrNull(f.slurmHome),
      dataHost: strOrNull(f.dataHost),
      dataPort: numOrNull(f.dataPort),
      partitions: partitions.map(
        (p): ClusterPartition => ({
          name: p.name,
          description: strOrNull(p.description),
          maxRunTime: numOrNull(p.maxRunTime),
          maxNodes: numOrNull(p.maxNodes),
          cpuPerNode: numOrNull(p.cpuPerNode),
          maxMemory: numOrNull(p.maxMemory),
          gres: strOrNull(p.gres),
          isDefaultQueue: p.isDefaultQueue,
        }),
      ),
    };
    await api.post("/api/v1/slurm-clusters", body);
    onCreated();
  });

  return (
    <Modal
      title="Add Slurm cluster"
      wide
      onClose={onClose}
      footer={
        <>
          <button onClick={onClose}>Cancel</button>
          <button className="primary" disabled={save.busy} onClick={() => void save.run()}>
            {save.busy ? "Saving…" : "Add cluster"}
          </button>
        </>
      }
    >
      <div className="form">
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
        <Field label="Description" hint="optional">
          <input value={f.clusterDescription} onChange={(e) => set({ clusterDescription: e.target.value })} />
        </Field>

        <fieldset>
          <legend>Partitions</legend>
          <div className="stack">
            {partitions.map((p, i) => (
              <div key={i} className="form-grid" style={{ alignItems: "end" }}>
                <Field label="Name">
                  <input value={p.name} onChange={(e) => setPart(i, { name: e.target.value })} />
                </Field>
                <Field label="Max run time (min)">
                  <input type="number" value={p.maxRunTime} onChange={(e) => setPart(i, { maxRunTime: e.target.value })} />
                </Field>
                <Field label="Max nodes">
                  <input type="number" value={p.maxNodes} onChange={(e) => setPart(i, { maxNodes: e.target.value })} />
                </Field>
                <Field label="CPU per node">
                  <input type="number" value={p.cpuPerNode} onChange={(e) => setPart(i, { cpuPerNode: e.target.value })} />
                </Field>
                <Field label="Max memory">
                  <input type="number" value={p.maxMemory} onChange={(e) => setPart(i, { maxMemory: e.target.value })} />
                </Field>
                <Field label="GRES">
                  <input value={p.gres} onChange={(e) => setPart(i, { gres: e.target.value })} placeholder="gpu:4" />
                </Field>
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
        <Alert>{save.error}</Alert>
      </div>
    </Modal>
  );
}

function Configs() {
  const mine = useResource(() => api.get<SlurmClusterConfig[]>("/api/v1/slurm-cluster-configs/me"));
  const shared = useResource(() => api.get<SlurmClusterConfig[]>("/api/v1/slurm-cluster-configs/shared-with-me"));
  const [creating, setCreating] = useState(false);
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
