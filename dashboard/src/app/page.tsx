"use client";

import Link from "next/link";
import { Alert, Card, formatTime, PageHead, StatusBadge, Table } from "@/components/ui";
import { api } from "@/lib/api";
import { createdAt, latestStatus, useDeploymentNames, withStatuses } from "@/lib/catalog";
import { useResource } from "@/lib/hooks";
import { useInteractiveAuth } from "@/lib/interactive-auth";
import type { Process } from "@/lib/types";

// count reads a listing's length, or null when it cannot be read, so one failing
// listing does not blank the whole overview.
const count = (path: string) => () => api.get<unknown[]>(path).then((r) => r.length).catch(() => null);

function Stat({ label, href, value }: { label: string; href: string; value: number | null | undefined }) {
  return (
    <Link href={href} className="card stat" style={{ color: "inherit", textDecoration: "none" }}>
      <div className="label">{label}</div>
      <div className="value">{value ?? "—"}</div>
    </Link>
  );
}

export default function OverviewPage() {
  const { current } = useInteractiveAuth();
  const names = useDeploymentNames();
  const counts = useResource(async () => {
    const [credentials, clusters, configs, storages, templates, products] = await Promise.all([
      count("/api/v1/ssh-credentials")(),
      count("/api/v1/slurm-clusters")(),
      count("/api/v1/slurm-cluster-configs/me")(),
      count("/api/v1/scp-data-storages/me")(),
      count("/api/v1/application-templates")(),
      count("/api/v1/data-products/me")(),
    ]);
    return { credentials, clusters, configs, storages, templates, products };
  });
  const recent = useResource(async () => (await withStatuses(await api.get<Process[]>("/api/v1/processes/me"))).slice(0, 8), [], 10_000);
  const c = counts.data;

  return (
    <div className="stack">
      <PageHead title="Overview" sub="Your resources on this Airavata server.">
        <Link href="/processes/new/" className="btn primary">
          New process
        </Link>
      </PageHead>

      {current && (
        <Alert kind="warn">
          An SSH login to <code>{current.hostName}</code> is waiting for your answer.{" "}
          <Link href="/interactive-auth/">Answer it</Link>
        </Alert>
      )}

      <div className="grid-stats">
        <Stat label="SSH credentials" href="/credentials/" value={c?.credentials} />
        <Stat label="Clusters" href="/compute/" value={c?.clusters} />
        <Stat label="My cluster configs" href="/compute/" value={c?.configs} />
        <Stat label="My storages" href="/storages/" value={c?.storages} />
        <Stat label="Applications" href="/applications/" value={c?.templates} />
        <Stat label="My data products" href="/data/" value={c?.products} />
      </div>

      <Alert>{recent.error}</Alert>
      <Card title="Recent processes" flush actions={<Link href="/processes/">View all</Link>}>
        <Table
          rows={recent.data}
          rowKey={(r) => r.process.processId}
          empty="No processes yet."
          columns={[
            {
              header: "Process",
              cell: (r) => (
                <Link href={`/processes/view/?id=${r.process.processId}`}>
                  <code>{r.process.processId.slice(0, 8)}</code>
                </Link>
              ),
            },
            { header: "Application", cell: (r) => names.appName(r.process.batchProcess?.deploymentId) ?? "—" },
            { header: "Status", cell: (r) => <StatusBadge status={latestStatus(r)?.status} /> },
            { header: "Job", cell: (r) => <StatusBadge status={r.process.batchProcess?.latestBatchJobStatus?.status} /> },
            { header: "Created", cell: (r) => <span className="small">{formatTime(createdAt(r))}</span> },
          ]}
        />
      </Card>

      <Card title="Getting started">
        <ol style={{ margin: 0, paddingLeft: 18, lineHeight: 1.9 }}>
          <li>
            Register an <Link href="/credentials/">SSH credential</Link> — the key your logins present.
          </li>
          <li>
            Add a <Link href="/compute/">cluster config</Link> (your login on a cluster) and a{" "}
            <Link href="/storages/">storage</Link> for your data.
          </li>
          <li>
            Register input files as <Link href="/data/">data products</Link>, uploading their content if needed.
          </li>
          <li>
            <Link href="/processes/new/">Create and launch a process</Link>, then monitor it until it completes and
            download its outputs.
          </li>
        </ol>
      </Card>
    </div>
  );
}
