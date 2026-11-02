"use client";

import Link from "next/link";
import { useState } from "react";
import { Alert, Card, ConfirmButton, formatTime, Id, PageHead, StatusBadge, Table, Tabs } from "@/components/ui";
import { api, errorText } from "@/lib/api";
import { createdAt, latestStatus, useDeploymentNames, withStatuses } from "@/lib/catalog";
import { useResource } from "@/lib/hooks";
import { useSession } from "@/lib/session";
import type { Process } from "@/lib/types";

const POLL_MS = 10_000;

export default function ProcessesPage() {
  const { isAdmin } = useSession();
  const [scope, setScope] = useState<"me" | "all">("me");
  const names = useDeploymentNames();
  const rows = useResource(
    async () => withStatuses(await api.get<Process[]>(scope === "me" ? "/api/v1/processes/me" : "/api/v1/processes")),
    [scope],
    POLL_MS,
  );
  const [error, setError] = useState<string | null>(null);
  const [launching, setLaunching] = useState<string | null>(null);

  const launch = async (id: string) => {
    setError(null);
    setLaunching(id);
    try {
      await api.post(`/api/v1/processes/${id}/launch`);
      await rows.reload();
    } catch (e) {
      setError(errorText(e));
    } finally {
      setLaunching(null);
    }
  };

  return (
    <div className="stack">
      <PageHead title="Processes" sub="Runs of deployed applications. This list refreshes every 10 seconds.">
        <Link href="/processes/new/" className="btn primary">
          New process
        </Link>
      </PageHead>
      {isAdmin && (
        <Tabs
          value={scope}
          onChange={setScope}
          tabs={[
            { key: "me", label: "My processes" },
            { key: "all", label: "All processes (admin)" },
          ]}
        />
      )}
      <Alert>{rows.error || error}</Alert>
      <Card flush>
        <Table
          rows={rows.data}
          rowKey={(r) => r.process.processId}
          empty="No processes yet. Create one to run an application."
          columns={[
            {
              header: "Process",
              cell: (r) => (
                <Link href={`/processes/view/?id=${r.process.processId}`}>
                  <code>{r.process.processId.slice(0, 8)}</code>
                </Link>
              ),
            },
            {
              header: "Application",
              cell: (r) => names.appName(r.process.batchProcess?.deploymentId) ?? <Id value={r.process.batchProcess?.deploymentId} />,
            },
            { header: "Status", cell: (r) => <StatusBadge status={latestStatus(r)?.status} /> },
            { header: "Job", cell: (r) => <StatusBadge status={r.process.batchProcess?.latestBatchJobStatus?.status} /> },
            { header: "Job ID", cell: (r) => r.process.batchProcess?.jobId ?? <span className="muted">—</span> },
            ...(scope === "all" ? [{ header: "Owner", cell: (r: { process: Process }) => <code>{r.process.userId}</code> }] : []),
            { header: "Created", cell: (r) => <span className="small">{formatTime(createdAt(r))}</span> },
            {
              header: "",
              className: "actions",
              cell: (r) => (
                <div className="row" style={{ justifyContent: "flex-end" }}>
                  {latestStatus(r)?.status === "CREATED" && (
                    <button
                      className="small primary"
                      disabled={launching === r.process.processId}
                      onClick={() => void launch(r.process.processId)}
                    >
                      {launching === r.process.processId ? "Launching…" : "Launch"}
                    </button>
                  )}
                  <Link href={`/processes/view/?id=${r.process.processId}`} className="btn small">
                    Monitor
                  </Link>
                  {isAdmin && (
                    <ConfirmButton
                      onConfirm={async () => {
                        setError(null);
                        try {
                          await api.del(`/api/v1/processes/${r.process.processId}`);
                          await rows.reload();
                        } catch (e) {
                          setError(errorText(e));
                        }
                      }}
                    >
                      Delete
                    </ConfirmButton>
                  )}
                </div>
              ),
            },
          ]}
        />
      </Card>
    </div>
  );
}
