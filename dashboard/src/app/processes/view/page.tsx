"use client";

import Link from "next/link";
import { useSearchParams } from "next/navigation";
import { Suspense, useState } from "react";
import { Alert, Card, formatTime, Id, PageHead, StatusBadge, Table } from "@/components/ui";
import { api, errorText, tryGet } from "@/lib/api";
import { useDeploymentNames } from "@/lib/catalog";
import { useResource } from "@/lib/hooks";
import { isFileType, type DataProduct, type Process, type ProcessStatus, type Task } from "@/lib/types";

const TERMINAL = new Set(["COMPLETED", "FAILED"]);
const POLL_MS = 5000;

const TASK_KINDS = [
  { path: "data-staging-tasks", label: "Data staging" },
  { path: "job-submission-tasks", label: "Job submission" },
  { path: "job-monitoring-tasks", label: "Job monitoring" },
  { path: "interactive-command-tasks", label: "Interactive commands" },
] as const;

// Fields every task carries; the rest are kind-specific and shown as they come.
const COMMON_TASK_FIELDS = new Set(["taskId", "processId", "onFailure", "retryCount", "taskOrder"]);

export default function ProcessViewPage() {
  return (
    <Suspense fallback={<div className="muted">Loading…</div>}>
      <ProcessView />
    </Suspense>
  );
}

function ProcessView() {
  const id = useSearchParams().get("id") ?? "";
  const names = useDeploymentNames();
  const [live, setLive] = useState(true);
  const detail = useResource(
    async () => {
      const [process, statuses] = await Promise.all([
        api.get<Process>(`/api/v1/processes/${id}`),
        api.get<ProcessStatus[]>(`/api/v1/processes/${id}/statuses`),
      ]);
      // Tasks are owner-scoped: a viewer who is neither owner nor admin sees none.
      const tasks = await Promise.all(TASK_KINDS.map((k) => tryGet<Task[]>(`/api/v1/processes/${id}/${k.path}`)));
      const done = TERMINAL.has(statuses[statuses.length - 1]?.status ?? "");
      if (done) setLive(false);
      return { process, statuses, tasks };
    },
    [id],
    live ? POLL_MS : undefined,
  );
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  if (!id) return <Alert>No process id given.</Alert>;
  const d = detail.data;
  const bp = d?.process.batchProcess;
  const latest = d?.statuses[d.statuses.length - 1];
  const template = names.template(names.deployment(bp?.deploymentId)?.templateId);

  const launch = async () => {
    setBusy(true);
    setError(null);
    try {
      await api.post(`/api/v1/processes/${id}/launch`);
      setLive(true);
      await detail.reload();
    } catch (e) {
      setError(errorText(e));
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="stack">
      <PageHead
        title={template?.templateName ? `${template.templateName} run` : "Process"}
        sub={
          <span className="row">
            <code>{id}</code>
            <StatusBadge status={latest?.status} />
            {live ? (
              <span className="small muted">
                <span className="dot ok" /> live, every {POLL_MS / 1000}s
              </span>
            ) : (
              <span className="small muted">not refreshing</span>
            )}
          </span>
        }
      >
        <Link href="/processes/" className="btn">
          All processes
        </Link>
        <button onClick={() => void detail.reload()} disabled={detail.loading}>
          Refresh
        </button>
        {!live && (
          <button onClick={() => setLive(true)}>Resume live updates</button>
        )}
        {latest?.status === "CREATED" && (
          <button className="primary" disabled={busy} onClick={() => void launch()}>
            {busy ? "Launching…" : "Launch"}
          </button>
        )}
      </PageHead>
      <Alert>{detail.error || error}</Alert>

      {d && (
        <>
          <div className="grid-stats">
            <div className="card stat">
              <div className="label">Process status</div>
              <div className="value">
                <StatusBadge status={latest?.status} />
              </div>
            </div>
            <div className="card stat">
              <div className="label">Scheduler status</div>
              <div className="value">
                <StatusBadge status={bp?.latestBatchJobStatus?.status} />
              </div>
            </div>
            <div className="card stat">
              <div className="label">Slurm job ID</div>
              <div className="value mono">{bp?.jobId ?? "—"}</div>
            </div>
            <div className="card stat">
              <div className="label">Created</div>
              <div className="value small">{formatTime(d.statuses[0]?.timestamp)}</div>
            </div>
          </div>

          <Card title="Status history">
            {d.statuses.length === 0 ? (
              <span className="muted">No statuses recorded.</span>
            ) : (
              <ul className="timeline">
                {d.statuses.map((s) => (
                  <li key={s.processStatusId}>
                    <span>
                      <StatusBadge status={s.status} />
                    </span>
                    <span className="small muted">{formatTime(s.timestamp)}</span>
                    <span>{s.log ? <pre className="block">{s.log}</pre> : <span className="muted">—</span>}</span>
                  </li>
                ))}
              </ul>
            )}
            {bp && bp.batchJobStatuses.length > 0 && (
              <>
                <h3 style={{ marginTop: 16 }}>Scheduler events</h3>
                <ul className="timeline">
                  {bp.batchJobStatuses.map((s) => (
                    <li key={s.batchProcessStatusId}>
                      <span>
                        <StatusBadge status={s.status} />
                      </span>
                      <span className="small muted">{formatTime(s.updatedAt)}</span>
                      <span />
                    </li>
                  ))}
                </ul>
              </>
            )}
          </Card>

          {bp && (
            <Card title="Run">
              <dl className="kv">
                <dt>Owner</dt>
                <dd>
                  <code>{d.process.userId}</code>
                </dd>
                <dt>Deployment</dt>
                <dd>
                  <Id value={bp.deploymentId} />
                </dd>
                <dt>Cluster config</dt>
                <dd>
                  <Id value={bp.slurmClusterConfigId} />
                </dd>
                <dt>Base work dir</dt>
                <dd>
                  <code>{bp.baseWorkDir ?? "(cluster config work root)"}</code>
                </dd>
                <dt>Resources</dt>
                <dd>
                  <code>
                    {Object.entries(bp.batchJobConfig ?? {})
                      .filter(([k, v]) => k !== "batchJobConfigId" && v !== null && v !== undefined)
                      .map(([k, v]) => `${k}=${v}`)
                      .join("  ")}
                  </code>
                </dd>
              </dl>
            </Card>
          )}

          {bp && (
            <Mappings
              inputs={bp.inputMappings.map((m) => {
                const decl = template?.inputs.find((i) => i.inputId === m.templateInputId);
                return { key: m.templateInputMappingId ?? m.templateInputId!, name: decl?.inputName, type: decl?.inputType, value: m.value };
              })}
              outputs={bp.outputMappings.map((m) => {
                const decl = template?.outputs.find((o) => o.outputId === m.templateOutputId);
                return { key: m.templateOutputMappingId ?? m.templateOutputId!, name: decl?.outputName, type: decl?.outputType, value: m.value };
              })}
            />
          )}

          {TASK_KINDS.map((k, i) => {
            const tasks = d.tasks[i];
            if (!tasks || tasks.length === 0) return null;
            const extra = Array.from(new Set(tasks.flatMap((t) => Object.keys(t)))).filter((f) => !COMMON_TASK_FIELDS.has(f));
            return (
              <Card key={k.path} title={`${k.label} tasks`} flush>
                <Table
                  rows={tasks}
                  rowKey={(t) => t.taskId}
                  empty=""
                  columns={[
                    { header: "Order", cell: (t) => t.taskOrder ?? "—" },
                    ...extra.map((f) => ({
                      header: f,
                      cell: (t: Task) => {
                        const v = t[f];
                        if (v === null || v === undefined || v === "") return <span className="muted">—</span>;
                        const text = String(v);
                        return text.includes("\n") ? <pre className="block">{text}</pre> : <code>{text}</code>;
                      },
                    })),
                    { header: "On failure", cell: (t) => t.onFailure ?? "—" },
                  ]}
                />
              </Card>
            );
          })}
        </>
      )}
    </div>
  );
}

interface MappingRow {
  key: string;
  name: string | undefined;
  type: string | null | undefined;
  value: string | null;
}

// Mappings lists the run's inputs and outputs. A file mapping's value is a data
// product id, shown as that product, with a download for file outputs.
function Mappings({ inputs, outputs }: { inputs: MappingRow[]; outputs: MappingRow[] }) {
  const ids = [...inputs, ...outputs].filter((m) => isFileType(m.type as never) && m.value).map((m) => m.value!);
  const products = useResource(
    async () => {
      const found = await Promise.all(ids.map((id) => tryGet<DataProduct>(`/api/v1/data-products/${id}`).catch(() => null)));
      return new Map(found.filter((p): p is DataProduct => !!p).map((p) => [p.dataId, p]));
    },
    [ids.join(",")],
  );
  const [error, setError] = useState<string | null>(null);

  const render = (m: MappingRow) => {
    if (!m.value) return <span className="muted">—</span>;
    if (!isFileType(m.type as never)) {
      try {
        const doc = JSON.parse(m.value);
        if (doc && typeof doc === "object") return <code>{String(doc.value ?? JSON.stringify(doc.values ?? doc))}</code>;
      } catch {
        // Not JSON; shown as stored.
      }
      return <code>{m.value}</code>;
    }
    const p = products.data?.get(m.value);
    if (!p) return <Id value={m.value} />;
    return (
      <span className="row">
        <span>
          <strong>{p.dataName}</strong> <code className="small">{p.path}</code>
        </span>
        <StatusBadge status={p.provisionStatus} />
        {p.isFile && (
          <button
            className="small"
            onClick={async () => {
              setError(null);
              try {
                await api.download(`/api/v1/data-products/${p.dataId}/content`, p.path?.split("/").pop() || p.dataId);
              } catch (e) {
                setError(errorText(e));
              }
            }}
          >
            Download
          </button>
        )}
      </span>
    );
  };

  const table = (rows: MappingRow[], empty: string) => (
    <Table
      rows={rows}
      rowKey={(m) => m.key}
      empty={empty}
      columns={[
        { header: "Name", cell: (m) => <code>{m.name ?? "?"}</code> },
        { header: "Type", cell: (m) => <span className="badge">{m.type ?? "?"}</span> },
        { header: "Value", cell: render },
      ]}
    />
  );

  return (
    <>
      <Alert>{error}</Alert>
      <Card title="Inputs" flush>
        {table(inputs, "No inputs.")}
      </Card>
      <Card title="Outputs" flush>
        {table(outputs, "No outputs.")}
      </Card>
    </>
  );
}
