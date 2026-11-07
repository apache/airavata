"use client";

import Link from "next/link";
import { useSearchParams } from "next/navigation";
import { Suspense, useState } from "react";
import { Alert, Card, formatTime, Id, PageHead, StatusBadge, Table } from "@/components/ui";
import { api, errorText, tryGet } from "@/lib/api";
import { useDeploymentNames } from "@/lib/catalog";
import { useResource } from "@/lib/hooks";
import {
  isFileType,
  type DataProduct,
  type Process,
  type ProcessStatus,
  type ProcessWorkflow,
  type ProcessWorkflowType,
  type Task,
  type TaskStatus,
  type TaskType,
} from "@/lib/types";

const TERMINAL = new Set(["COMPLETED", "FAILED"]);
const POLL_MS = 5000;

const TASK_KINDS: readonly { path: string; label: string; type: TaskType }[] = [
  { path: "data-staging-tasks", label: "Data staging", type: "DATA_STAGING" },
  { path: "job-submission-tasks", label: "Job submission", type: "JOB_SUBMISSION" },
  { path: "job-monitoring-tasks", label: "Job monitoring", type: "JOB_MONITORING" },
  { path: "interactive-command-tasks", label: "Interactive commands", type: "INTERACTIVE_COMMAND" },
];

const WORKFLOW_LABELS: Record<ProcessWorkflowType, string> = {
  BATCH_JOB_SUBMISSION: "Job submission",
  JOB_MONITORING: "Job monitoring",
  BATCH_JOB_COMPLETION: "Job completion",
};

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
      // So are workflows.
      const [tasks, taskStatuses, workflows] = await Promise.all([
        Promise.all(TASK_KINDS.map((k) => tryGet<Task[]>(`/api/v1/processes/${id}/${k.path}`))),
        tryGet<TaskStatus[]>(`/api/v1/processes/${id}/task-statuses`),
        tryGet<ProcessWorkflow[]>(`/api/v1/processes/${id}/workflows`),
      ]);
      // A process is marked COMPLETED from inside its completion workflow, just before
      // that workflow records its own outcome, so keep polling until it has.
      const done =
        TERMINAL.has(statuses[statuses.length - 1]?.status ?? "") &&
        !(workflows ?? []).some((w) => w.status === "RUNNING");
      if (done) setLive(false);
      return { process, statuses, tasks, taskStatuses: taskStatuses ?? [], workflows };
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
  // Statuses arrive oldest first, so the last one seen for a task is its current one.
  const latestTaskStatus = new Map<string, TaskStatus>();
  for (const s of d?.taskStatuses ?? []) latestTaskStatus.set(s.taskId, s);
  const taskOrder = new Map<string, number | null>();
  d?.tasks.forEach((list) => list?.forEach((t) => taskOrder.set(t.taskId, t.taskOrder)));

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

          {d.workflows && d.workflows.length > 0 && (
            <Card title="Workflows" flush>
              <Table
                rows={d.workflows}
                rowKey={(w) => w.processWorkflowId}
                empty=""
                columns={[
                  {
                    header: "Workflow",
                    cell: (w) => (
                      <span className="row">
                        <strong>{w.workflowType ? WORKFLOW_LABELS[w.workflowType] : "—"}</strong>
                        {w.processWorkflowId === d.process.lastWorkflowId && <span className="small muted">latest</span>}
                      </span>
                    ),
                  },
                  { header: "Status", cell: (w) => <StatusBadge status={w.status} /> },
                  { header: "Started", cell: (w) => <span className="small">{formatTime(w.timestamp)}</span> },
                  { header: "Workflow ID", cell: (w) => <Id value={w.workflowId} /> },
                ]}
              />
            </Card>
          )}

          {d.taskStatuses.length > 0 && (
            <Card title="Task history">
              <ul className="timeline">
                {d.taskStatuses.map((s) => {
                  const order = taskOrder.get(s.taskId);
                  return (
                    <li key={s.taskStatusId}>
                      <span>
                        <StatusBadge status={s.status} />
                      </span>
                      <span className="small muted">{formatTime(s.timestamp)}</span>
                      <span>
                        <span className="row">
                          <strong>{TASK_KINDS.find((k) => k.type === s.taskType)?.label ?? s.taskType}</strong>
                          {order !== undefined && order !== null && <span className="small muted">step {order}</span>}
                          <Id value={s.taskId} />
                        </span>
                        {s.log && <pre className="block">{s.log}</pre>}
                      </span>
                    </li>
                  );
                })}
              </ul>
            </Card>
          )}

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
                    { header: "Status", cell: (t) => <StatusBadge status={latestTaskStatus.get(t.taskId)?.status} /> },
                    {
                      header: "Updated",
                      cell: (t) => <span className="small">{formatTime(latestTaskStatus.get(t.taskId)?.timestamp)}</span>,
                    },
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
