"use client";

import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { workflowsApi } from "@/lib/api/workflows";
import type { Workflow } from "@/types";

export function useWorkflows(projectId: string | undefined, gatewayId: string | undefined) {
  return useQuery({
    queryKey: ["workflows", projectId, gatewayId],
    queryFn: () => workflowsApi.list(projectId!, gatewayId!),
    enabled: !!projectId && !!gatewayId,
  });
}

export function useWorkflow(workflowId: string | undefined) {
  return useQuery({
    queryKey: ["workflows", workflowId],
    queryFn: () => workflowsApi.get(workflowId!),
    enabled: !!workflowId,
  });
}

export function useCreateWorkflow() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (workflow: Partial<Workflow>) => workflowsApi.create(workflow),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["workflows"] });
    },
  });
}

export function useUpdateWorkflow() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ workflowId, workflow }: { workflowId: string; workflow: Partial<Workflow> }) =>
      workflowsApi.update(workflowId, workflow),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ["workflows"] });
      queryClient.invalidateQueries({ queryKey: ["workflows", variables.workflowId] });
    },
  });
}

export function useDeleteWorkflow() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (workflowId: string) => workflowsApi.delete(workflowId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["workflows"] });
    },
  });
}

export function useWorkflowRuns(workflowId: string | undefined) {
  return useQuery({
    queryKey: ["workflow-runs", workflowId],
    queryFn: () => workflowsApi.listRuns(workflowId!),
    enabled: !!workflowId,
  });
}

export function useWorkflowRun(runId: string | undefined) {
  return useQuery({
    queryKey: ["workflow-run", runId],
    queryFn: () => workflowsApi.getRun(runId!),
    enabled: !!runId,
    refetchInterval: 5000,
  });
}

export function useCreateWorkflowRun() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ workflowId, userName }: { workflowId: string; userName: string }) =>
      workflowsApi.createRun(workflowId, userName),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["workflow-runs"] });
    },
  });
}
