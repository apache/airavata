"use client";

import Link from "next/link";
import { useSearchParams } from "next/navigation";
import { Suspense, useState } from "react";
import { Alert, Card, formatTime, Id, PageHead, StatusBadge, statusTone, Table, Tabs } from "@/components/ui";
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
  type SCPDataStorage,
  type SlurmClusterConfig,
  type Task,
  type TaskStatus,
  type TaskType,
} from "@/lib/types";

const TERMINAL = new Set(["COMPLETED", "FAILED", "STOPPED"]);
const POLL_MS = 5000;

const TASK_KINDS: readonly { path: string; label: string; type: TaskType }[] = [
  { path: "data-staging-tasks", label: "Data staging", type: "DATA_STAGING" },
  { path: "job-submission-tasks", label: "Job submission", type: "JOB_SUBMISSION" },
  { path: "job-monitoring-tasks", label: "Job monitoring", type: "JOB_MONITORING" },
  { path: "interactive-command-tasks", label: "Interactive commands", type: "INTERACTIVE_COMMAND" },
];

const WORKFLOW_STEPS: readonly { type: ProcessWorkflowType; label: string }[] = [
  { type: "BATCH_JOB_SUBMISSION", label: "Submission" },
  { type: "JOB_MONITORING", label: "Monitoring" },
  { type: "BATCH_JOB_COMPLETION", label: "Completion" },
];

const WORKFLOW_LABELS: Record<ProcessWorkflowType, string> = {
  BATCH_JOB_SUBMISSION: "Job submission",
  JOB_MONITORING: "Job monitoring",
  BATCH_JOB_COMPLETION: "Job completion",
};

const RESOURCE_LABELS: Record<string, string> = {
  wallTimeMinutes: "Wall time (min)",
  allocation: "Allocation",
  cpus: "CPUs",
  mem: "Memory",
  memPerCpu: "Mem / CPU",
  ntasksPerNode: "Tasks / node",
  cpusPerTask: "CPUs / task",
  nodes: "Nodes",
  ntasks: "Tasks",
  gres: "GRES",
  gpus: "GPUs",
  memPerGpu: "Mem / GPU",
  cpusPerGpu: "CPUs / GPU",
};

// Whether a retry would be accepted, by the same rule the server applies: no workflow
// is running, and the latest submission failed, or it succeeded and the latest
// completion failed. The server decides; this only chooses whether to offer it.
function canRetry(workflows: ProcessWorkflow[] | null | undefined): boolean {
  const latest = new Map<string, string | null>();
  for (const w of workflows ?? []) if (w.workflowType) latest.set(w.workflowType, w.status);
  if ([...latest.values()].includes("RUNNING")) return false;
  const submission = latest.get("BATCH_JOB_SUBMISSION");
  if (submission === undefined) return false;
  return submission === "FAILED" || latest.get("BATCH_JOB_COMPLETION") === "FAILED";
}

// Fields every task carries; the rest are kind-specific and shown as they come.
// Index of data staging tasks in TASK_KINDS, and so in the fetched task lists.
const STAGING = TASK_KINDS.findIndex((k) => k.type === "DATA_STAGING");

// script is left out too: a submission task's batch script gets a block of its own.
const COMMON_TASK_FIELDS = new Set(["taskId", "processId", "onFailure", "retryCount", "taskOrder", "script"]);

// shortTime drops the date for today's timestamps; the full time is in the tooltip.
function shortTime(ms: number | null | undefined): string {
  if (!ms) return "—";
  const d = new Date(ms);
  if (d.toDateString() === new Date().toDateString()) return d.toLocaleTimeString();
  return d.toLocaleString(undefined, { month: "short", day: "numeric", hour: "2-digit", minute: "2-digit" });
}

function Time({ ms }: { ms: number | null | undefined }) {
  return (
    <span className="small muted nowrap" title={formatTime(ms)}>
      {shortTime(ms)}
    </span>
  );
}

function duration(ms: number): string {
  const s = Math.max(0, Math.round(ms / 1000));
  if (s < 60) return `${s}s`;
  const m = Math.floor(s / 60);
  if (m < 60) return `${m}m ${s % 60}s`;
  const h = Math.floor(m / 60);
  if (h < 24) return `${h}h ${m % 60}m`;
  return `${Math.floor(h / 24)}d ${h % 24}h`;
}

// Log shows a short one-line log inline and folds anything longer, open when it
// explains a failure.
function Log({ text, open }: { text: string | null | undefined; open?: boolean }) {
  if (!text) return null;
  if (!text.includes("\n") && text.length <= 140) return <div className="small muted log-line">{text}</div>;
  return (
    <details className="log" open={open}>
      <summary>Log</summary>
      <pre className="block">{text}</pre>
    </details>
  );
}

export default function ProcessViewPage() {
  return (
    <Suspense fallback={<div className="muted">Loading…</div>}>
      <ProcessView />
    </Suspense>
  );
}

type TabKey = "overview" | "activity" | "tasks" | "details";

interface FeedEvent {
  key: string;
  ts: number | null;
  source: string;
  detail?: string;
  status: string | null;
  log?: string | null;
}

function ProcessView() {
  const id = useSearchParams().get("id") ?? "";
  const names = useDeploymentNames();
  const [live, setLive] = useState(true);
  const [tab, setTab] = useState<TabKey>("overview");
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
  const endpoints = useEndpoints(
    detail.data?.tasks[STAGING] ?? null,
    detail.data?.process.batchProcess?.slurmClusterConfigId,
  );

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
  const allTasks = (d?.tasks ?? [])
    .flatMap((list, i) => (list ?? []).map((t) => ({ task: t, kind: TASK_KINDS[i] })))
    .sort((a, b) => (a.task.taskOrder ?? Infinity) - (b.task.taskOrder ?? Infinity));

  const feed: FeedEvent[] = d
    ? [
        ...d.statuses.map((s) => ({
          key: `p-${s.processStatusId}`,
          ts: s.timestamp,
          source: "Process",
          status: s.status,
          log: s.log,
        })),
        ...(bp?.batchJobStatuses ?? []).map((s) => ({
          key: `b-${s.batchProcessStatusId}`,
          ts: s.updatedAt,
          source: "Scheduler",
          status: s.status,
        })),
        ...d.taskStatuses.map((s) => {
          const order = taskOrder.get(s.taskId);
          return {
            key: `t-${s.taskStatusId}`,
            ts: s.timestamp,
            source: TASK_KINDS.find((k) => k.type === s.taskType)?.label ?? s.taskType,
            detail: order !== undefined && order !== null ? `step ${order}` : undefined,
            status: s.status,
            log: s.log,
          };
        }),
      ].sort((a, b) => (a.ts ?? 0) - (b.ts ?? 0))
    : [];

  const started = d?.statuses[0]?.timestamp;
  const ended = latest && TERMINAL.has(latest.status ?? "") ? latest.timestamp : null;
  const elapsed = started ? duration((ended ?? Date.now()) - started) : null;

  const act = async (action: "launch" | "retry") => {
    setBusy(true);
    setError(null);
    try {
      await api.post(`/api/v1/processes/${id}/${action}`);
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
      <div>
        <Link href="/processes/" className="small muted">
          ← All processes
        </Link>
      </div>
      <PageHead
        title={template?.templateName ? `${template.templateName} run` : "Process"}
        sub={
          <span className="row">
            <StatusBadge status={latest?.status} />
            <Id value={id} />
            {live ? (
              <span className="small muted row live">
                <span className="dot ok pulse" /> Live
              </span>
            ) : (
              <button className="link small" onClick={() => setLive(true)} title="Poll for changes again">
                Resume live updates
              </button>
            )}
          </span>
        }
      >
        <button onClick={() => void detail.reload()} disabled={detail.loading} title="Refresh now">
          ↻ Refresh
        </button>
        {latest?.status === "CREATED" && (
          <button className="primary" disabled={busy} onClick={() => void act("launch")}>
            {busy ? "Launching…" : "Launch"}
          </button>
        )}
        {canRetry(d?.workflows) && (
          <button
            className="primary"
            disabled={busy}
            title="Run the failed workflow again, skipping tasks that already completed"
            onClick={() => void act("retry")}
          >
            {busy ? "Retrying…" : "Retry"}
          </button>
        )}
      </PageHead>
      <Alert>{detail.error || error}</Alert>

      {d && (
        <>
          <section className="card summary">
            <dl className="facts">
              <div>
                <dt>Scheduler</dt>
                <dd>
                  <StatusBadge status={bp?.latestBatchJobStatus?.status} />
                </dd>
              </div>
              <div>
                <dt>Job ID</dt>
                <dd className="mono">{bp?.jobId ?? <span className="muted">—</span>}</dd>
              </div>
              <div>
                <dt>Started</dt>
                <dd>
                  <span title={formatTime(started)}>{shortTime(started)}</span>
                </dd>
              </div>
              <div>
                <dt>{ended ? "Duration" : "Elapsed"}</dt>
                <dd>{elapsed ?? <span className="muted">—</span>}</dd>
              </div>
              <div>
                <dt>Owner</dt>
                <dd className="mono">{d.process.userId ?? <span className="muted">—</span>}</dd>
              </div>
            </dl>
            {d.workflows && d.workflows.length > 0 && (
              <Stepper
                workflows={d.workflows}
                monitoring={[...latestTaskStatus.values()].filter((s) => s.taskType === "JOB_MONITORING").pop()}
              />
            )}
          </section>

          <Tabs
            value={tab}
            onChange={setTab}
            tabs={[
              { key: "overview", label: "Overview" },
              { key: "activity", label: <>Activity <span className="count">{feed.length}</span></> },
              ...(allTasks.length > 0
                ? [{ key: "tasks" as const, label: <>Tasks <span className="count">{allTasks.length}</span></> }]
                : []),
              { key: "details", label: "Details" },
            ]}
          />

          {tab === "overview" && (
            <>
              {latest?.status === "FAILED" && latest.log && (
                <div className="alert error">
                  <strong>This run failed.</strong>
                  <pre className="block failure">{latest.log}</pre>
                </div>
              )}
              {bp ? (
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
              ) : (
                <div className="empty">This process has no batch job.</div>
              )}
            </>
          )}

          {tab === "activity" && (
            <Card flush>
              {feed.length === 0 ? (
                <div className="empty">Nothing recorded yet.</div>
              ) : (
                <ol className="feed">
                  {feed.map((e) => (
                    <li key={e.key}>
                      <span className={`feed-dot ${statusTone(e.status)}`} />
                      <div className="feed-body">
                        <div className="row">
                          <strong>{e.source}</strong>
                          {e.detail && <span className="small muted">{e.detail}</span>}
                          <StatusBadge status={e.status} />
                          <span className="spacer" />
                          <Time ms={e.ts} />
                        </div>
                        <Log text={e.log} open={e.status === "FAILED"} />
                      </div>
                    </li>
                  ))}
                </ol>
              )}
            </Card>
          )}

          {tab === "tasks" && (
            <Card flush>
              <div className="task-list">
                {allTasks.map(({ task, kind }) => {
                  const st = latestTaskStatus.get(task.taskId);
                  const staging = kind.type === "DATA_STAGING";
                  const submission = kind.type === "JOB_SUBMISSION";
                  const shown = staging ? STAGING_FIELDS : submission ? SUBMISSION_FIELDS : null;
                  const extra = Object.keys(task).filter((f) => !COMMON_TASK_FIELDS.has(f) && !shown?.has(f));
                  const cluster = bp?.slurmClusterConfigId;
                  return (
                    <details key={task.taskId} className="task" open={st?.status === "FAILED"}>
                      <summary>
                        <span className="step">{task.taskOrder ?? "·"}</span>
                        <strong>{kind.label}</strong>
                        <StatusBadge status={st?.status} />
                        {staging && <span className="small muted route">{routeLabel(task, endpoints)}</span>}
                        {submission && cluster && (
                          <span className="small muted route">
                            → {endpoints.get(endpointKey("HPC", cluster))?.name ?? "cluster"}
                            {typeof task.jobId === "string" && task.jobId && ` · job ${task.jobId}`}
                          </span>
                        )}
                        <span className="spacer" />
                        <Time ms={st?.timestamp} />
                      </summary>
                      <div className="task-body">
                        {staging && <Transfer task={task} endpoints={endpoints} />}
                        {submission && (
                          <SubmittedTo
                            task={task}
                            clusterConfigId={cluster}
                            endpoint={cluster ? endpoints.get(endpointKey("HPC", cluster)) : undefined}
                            allocation={bp?.batchJobConfig?.allocation}
                          />
                        )}
                        <dl className="kv">
                          {extra.map((f) => (
                            <FieldRow key={f} name={f} value={task[f]} />
                          ))}
                          <dt>On failure</dt>
                          <dd>{task.onFailure ?? <span className="muted">—</span>}</dd>
                          {task.retryCount ? (
                            <>
                              <dt>Retries</dt>
                              <dd>{task.retryCount}</dd>
                            </>
                          ) : null}
                          <dt>Task ID</dt>
                          <dd>
                            <code>{task.taskId}</code>
                          </dd>
                        </dl>
                        {kind.type === "JOB_SUBMISSION" && <JobScript script={task.script} />}
                        <Log text={st?.log} open={st?.status === "FAILED"} />
                      </div>
                    </details>
                  );
                })}
              </div>
            </Card>
          )}

          {tab === "details" && (
            <>
              {bp && (
                <Card title="Run configuration">
                  <dl className="kv">
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
                      {bp.baseWorkDir ? <code>{bp.baseWorkDir}</code> : <span className="muted">Cluster config work root</span>}
                    </dd>
                    <dt>Resources</dt>
                    <dd className="chips">
                      {Object.entries(bp.batchJobConfig ?? {})
                        .filter(([k, v]) => k !== "batchJobConfigId" && v !== null && v !== undefined && v !== "")
                        .map(([k, v]) => (
                          <span key={k} className="chip">
                            <span className="muted">{RESOURCE_LABELS[k] ?? k}</span> {String(v)}
                          </span>
                        ))}
                    </dd>
                  </dl>
                </Card>
              )}
              {d.workflows && d.workflows.length > 0 && (
                <Card title="Workflow runs" flush>
                  <Table
                    rows={d.workflows}
                    rowKey={(w) => w.processWorkflowId}
                    empty=""
                    columns={[
                      {
                        header: "Workflow",
                        cell: (w) => (
                          <span className="row">
                            {w.workflowType ? WORKFLOW_LABELS[w.workflowType] : "—"}
                            {w.processWorkflowId === d.process.lastWorkflowId && <span className="badge">latest</span>}
                          </span>
                        ),
                      },
                      { header: "Status", cell: (w) => <StatusBadge status={w.status} /> },
                      { header: "Started", cell: (w) => <Time ms={w.timestamp} /> },
                      { header: "Workflow ID", cell: (w) => <Id value={w.workflowId} /> },
                    ]}
                  />
                </Card>
              )}
            </>
          )}
        </>
      )}
    </div>
  );
}

// A data staging endpoint, resolved from the storage id and type a task names. An SCP
// endpoint is a registered storage; an HPC endpoint is a cluster config, reached
// through the cluster's head node.
interface Endpoint {
  kind: "SCP" | "HPC";
  name: string;
  address: string;
}

// The staging fields Transfer shows; they are kept out of the generic field list.
const STAGING_FIELDS = new Set([
  "sourceDataStorageId",
  "sourceDataStorageType",
  "sourcePath",
  "destinationDataStorageId",
  "destinationDataStorageType",
  "destinationPath",
]);

// The submission fields SubmittedTo shows (script has its own block).
const SUBMISSION_FIELDS = new Set(["jobId", "workingDir"]);

const endpointKey = (type: unknown, id: unknown) => `${String(type)}:${String(id)}`;

// EndpointName names an endpoint, or says why it cannot: undefined while it loads or
// when there is no id, null when the viewer may not read it.
function EndpointName({ endpoint, id }: { endpoint: Endpoint | null | undefined; id: unknown }) {
  if (endpoint)
    return (
      <>
        <strong>{endpoint.name}</strong>
        {endpoint.address && <code className="small muted">{endpoint.address}</code>}
      </>
    );
  if (endpoint === null)
    return (
      <span className="small muted">
        Not visible to you · <Id value={String(id)} />
      </span>
    );
  return id ? <Id value={String(id)} /> : <span className="muted">—</span>;
}

// SubmittedTo shows where a submission task sends its job: the run's cluster, reached
// through its head node, the directory the job is submitted from, and the job id the
// scheduler gave it.
function SubmittedTo({
  task,
  clusterConfigId,
  endpoint,
  allocation,
}: {
  task: Task;
  clusterConfigId: string | null | undefined;
  endpoint: Endpoint | null | undefined;
  allocation: string | null | undefined;
}) {
  const jobId = typeof task.jobId === "string" && task.jobId ? task.jobId : null;
  const workingDir = typeof task.workingDir === "string" && task.workingDir ? task.workingDir : null;
  return (
    <div className="endpoint">
      <div className="endpoint-label">Submitted to</div>
      <div className="endpoint-body">
        <div className="row">
          <span className="badge">Cluster</span>
          <EndpointName endpoint={endpoint} id={clusterConfigId} />
        </div>
        <dl className="kv small">
          <dt>Working dir</dt>
          <dd>{workingDir ? <code>{workingDir}</code> : <span className="muted">—</span>}</dd>
          {allocation && (
            <>
              <dt>Allocation</dt>
              <dd>
                <code>{allocation}</code>
              </dd>
            </>
          )}
          <dt>Job ID</dt>
          <dd>{jobId ? <code>{jobId}</code> : <span className="muted">Not submitted yet</span>}</dd>
        </dl>
      </div>
    </div>
  );
}

function address(user: string | null | undefined, host: string | null | undefined, port: number | null | undefined) {
  if (!host) return "";
  return `${user ? `${user}@` : ""}${host}${port && port !== 22 ? `:${port}` : ""}`;
}

// useEndpoints looks up every endpoint the staging tasks name. One the viewer cannot
// read (it belongs to someone else and is not shared) resolves to null.
// clusterConfigId is the cluster the job is submitted to, looked up alongside them.
function useEndpoints(tasks: Task[] | null, clusterConfigId: string | null | undefined): Map<string, Endpoint | null> {
  const refs = new Map<string, { type: string; id: string }>();
  if (clusterConfigId) refs.set(endpointKey("HPC", clusterConfigId), { type: "HPC", id: clusterConfigId });
  for (const t of tasks ?? []) {
    for (const [type, id] of [
      [t.sourceDataStorageType, t.sourceDataStorageId],
      [t.destinationDataStorageType, t.destinationDataStorageId],
    ]) {
      if (typeof type === "string" && typeof id === "string") refs.set(endpointKey(type, id), { type, id });
    }
  }
  const keys = [...refs.keys()].sort().join(",");
  const res = useResource(async () => {
    const out = new Map<string, Endpoint | null>();
    await Promise.all(
      [...refs.entries()].map(async ([key, { type, id }]) => {
        try {
          if (type === "HPC") {
            const c = await tryGet<SlurmClusterConfig>(`/api/v1/slurm-cluster-configs/${id}`);
            out.set(
              key,
              c && {
                kind: "HPC",
                name: c.name || c.slurmCluster?.clusterName || "Cluster",
                address: address(c.loginUser, c.slurmCluster?.headnodeHost, c.slurmCluster?.headnodePort),
              },
            );
          } else {
            const s = await tryGet<SCPDataStorage>(`/api/v1/scp-data-storages/${id}`);
            out.set(key, s && { kind: "SCP", name: s.dataName || "Storage", address: address(s.loginUser, s.hostName, s.port) });
          }
        } catch {
          out.set(key, null);
        }
      }),
    );
    return out;
  }, [keys]);
  return res.data ?? new Map();
}

// Paths are a single path or a JSON array of them.
function paths(v: unknown): string[] {
  if (typeof v !== "string" || v === "") return [];
  if (v.trim().startsWith("[")) {
    try {
      const list = JSON.parse(v);
      if (Array.isArray(list)) return list.map(String);
    } catch {
      // Not JSON; a single path.
    }
  }
  return [v];
}

function routeLabel(task: Task, endpoints: Map<string, Endpoint | null>): string {
  const name = (type: unknown, id: unknown) =>
    id ? (endpoints.get(endpointKey(type, id))?.name ?? (type === "HPC" ? "cluster" : "storage")) : "?";
  return `${name(task.sourceDataStorageType, task.sourceDataStorageId)} → ${name(task.destinationDataStorageType, task.destinationDataStorageId)}`;
}

function Transfer({ task, endpoints }: { task: Task; endpoints: Map<string, Endpoint | null> }) {
  const side = (label: string, type: unknown, id: unknown, path: unknown) => {
    const ep = id ? endpoints.get(endpointKey(type, id)) : undefined;
    const list = paths(path);
    return (
      <div className="endpoint">
        <div className="endpoint-label">{label}</div>
        <div className="endpoint-body">
          <div className="row">
            <span className="badge">{type === "HPC" ? "Cluster" : String(type ?? "?")}</span>
            <EndpointName endpoint={ep} id={id} />
          </div>
          {list.length === 0 ? (
            <span className="small muted">No path</span>
          ) : (
            list.map((p) => (
              <code key={p} className="endpoint-path">
                {p}
              </code>
            ))
          )}
        </div>
      </div>
    );
  };
  return (
    <div className="transfer">
      {side("From", task.sourceDataStorageType, task.sourceDataStorageId, task.sourcePath)}
      <div className="transfer-arrow" aria-hidden>
        →
      </div>
      {side("To", task.destinationDataStorageType, task.destinationDataStorageId, task.destinationPath)}
    </div>
  );
}

// JobScript shows the batch script a submission task built, which the server records
// when the task runs.
function JobScript({ script }: { script: unknown }) {
  const [copied, setCopied] = useState(false);
  if (typeof script !== "string" || script === "") {
    return <div className="small muted">The job script is recorded when this task runs.</div>;
  }
  return (
    <div className="script">
      <div className="script-head">
        <span className="small">
          <strong>Job script</strong> <span className="muted">· script.slurm</span>
        </span>
        <span className="spacer" />
        <button
          className="small"
          onClick={async () => {
            try {
              await navigator.clipboard.writeText(script);
              setCopied(true);
              setTimeout(() => setCopied(false), 1500);
            } catch {
              // Clipboard unavailable (e.g. insecure context); the text is selectable.
            }
          }}
        >
          {copied ? "Copied" : "Copy"}
        </button>
      </div>
      <pre className="block">{script}</pre>
    </div>
  );
}

function FieldRow({ name, value }: { name: string; value: unknown }) {
  const empty = value === null || value === undefined || value === "";
  const text = empty ? "" : typeof value === "object" ? JSON.stringify(value, null, 2) : String(value);
  return (
    <>
      <dt>{name}</dt>
      <dd>
        {empty ? (
          <span className="muted">—</span>
        ) : text.includes("\n") ? (
          <pre className="block">{text}</pre>
        ) : (
          <code>{text}</code>
        )}
      </dd>
    </>
  );
}

type StepState = "done" | "running" | "failed" | "stopped" | "pending";

const WORKFLOW_STATE: Record<string, StepState> = { SUCCEEDED: "done", RUNNING: "running", FAILED: "failed", STOPPED: "stopped" };
const TASK_STATE: Record<string, StepState> = {
  COMPLETED: "done",
  EXECUTING: "running",
  PAUSED: "running",
  FAILED: "failed",
  STOPPED: "stopped",
};

// Stepper shows where the run is: submission and completion by the latest run of
// their workflows, and monitoring by its task, since the server runs job monitoring
// as a task inside the completion workflow rather than as a workflow of its own.
function Stepper({ workflows, monitoring }: { workflows: ProcessWorkflow[]; monitoring: TaskStatus | undefined }) {
  const latest = new Map<ProcessWorkflowType, { w: ProcessWorkflow; attempts: number }>();
  for (const w of workflows) {
    if (!w.workflowType) continue;
    latest.set(w.workflowType, { w, attempts: (latest.get(w.workflowType)?.attempts ?? 0) + 1 });
  }
  return (
    <ol className="stepper">
      {WORKFLOW_STEPS.map((s, i) => {
        let state: StepState;
        let text: string;
        let attempts = 0;
        if (s.type === "JOB_MONITORING") {
          state = TASK_STATE[monitoring?.status ?? ""] ?? "pending";
          text = state === "done" ? "completed" : state === "running" ? "watching job" : (monitoring?.status ?? "pending").toLowerCase();
        } else {
          const run = latest.get(s.type);
          state = WORKFLOW_STATE[run?.w.status ?? ""] ?? "pending";
          text = run ? (run.w.status ?? "—").toLowerCase() : "pending";
          attempts = run?.attempts ?? 0;
        }
        if (state === "pending") text = "pending";
        return (
          <li key={s.type} className={state}>
            <span className="marker">{state === "done" ? "✓" : state === "failed" ? "!" : i + 1}</span>
            <span className="step-text">
              <span className="step-label">{s.label}</span>
              <span className="small muted">
                {text}
                {attempts > 1 && ` · attempt ${attempts}`}
              </span>
            </span>
          </li>
        );
      })}
    </ol>
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
      <div className="product">
        <div className="row">
          <strong>{p.dataName}</strong>
          {p.provisionStatus !== "PROVISIONED" && <StatusBadge status={p.provisionStatus} />}
        </div>
        {p.path && (
          <code className="small muted truncate" title={p.path}>
            {p.path}
          </code>
        )}
      </div>
    );
  };

  const download = (m: MappingRow) => {
    const p = m.value ? products.data?.get(m.value) : undefined;
    if (!p?.isFile) return null;
    return (
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
    );
  };

  const table = (rows: MappingRow[], empty: string) => (
    <Table
      rows={rows}
      rowKey={(m) => m.key}
      empty={empty}
      columns={[
        {
          header: "Name",
          cell: (m) => (
            <span className="row">
              <strong>{m.name ?? "?"}</strong>
              <span className="small muted">{(m.type ?? "?").toLowerCase()}</span>
            </span>
          ),
        },
        { header: "Value", cell: render },
        { header: "", className: "actions", cell: download },
      ]}
    />
  );

  return (
    <>
      <Alert>{error}</Alert>
      <div className="io-grid">
        <Card title="Inputs" flush>
          {table(inputs, "No inputs.")}
        </Card>
        <Card title="Outputs" flush>
          {table(outputs, "No outputs.")}
        </Card>
      </div>
    </>
  );
}
