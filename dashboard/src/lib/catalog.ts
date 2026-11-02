"use client";

import { useMemo } from "react";
import { api } from "./api";
import { useResource } from "./hooks";
import type { BatchDeployment, Process, ProcessStatus, Template } from "./types";

// useDeploymentNames resolves a deployment id to the name of the application it runs,
// which is what a person recognises a run by.
export function useDeploymentNames() {
  const deployments = useResource(() => api.get<BatchDeployment[]>("/api/v1/slurm-deployments"));
  const templates = useResource(() => api.get<Template[]>("/api/v1/application-templates"));
  return useMemo(() => {
    const tmpl = new Map(templates.data?.map((t) => [t.templateId, t]));
    const dep = new Map(deployments.data?.map((d) => [d.deploymentId, d]));
    return {
      deployment: (id: string | null | undefined) => (id ? dep.get(id) : undefined),
      template: (id: string | null | undefined) => (id ? tmpl.get(id) : undefined),
      appName: (deploymentId: string | null | undefined) => {
        const d = deploymentId ? dep.get(deploymentId) : undefined;
        const t = d?.templateId ? tmpl.get(d.templateId) : undefined;
        return t?.templateName ?? null;
      },
    };
  }, [deployments.data, templates.data]);
}

export interface ProcessRow {
  process: Process;
  statuses: ProcessStatus[];
}

export const latestStatus = (r: ProcessRow) => r.statuses[r.statuses.length - 1] ?? null;
export const createdAt = (r: ProcessRow) => r.statuses[0]?.timestamp ?? 0;

// withStatuses reads each run's status history beside it: a process carries only the id
// of its newest status, and its first one is the only record of when it was created.
export async function withStatuses(processes: Process[]): Promise<ProcessRow[]> {
  const rows = await Promise.all(
    processes.map(async (process) => ({
      process,
      statuses: await api.get<ProcessStatus[]>(`/api/v1/processes/${process.processId}/statuses`).catch(() => []),
    })),
  );
  return rows.sort((a, b) => createdAt(b) - createdAt(a));
}
