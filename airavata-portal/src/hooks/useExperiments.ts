"use client";

import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useSession } from "next-auth/react";
import { useGateway } from "@/contexts/GatewayContext";
import { experimentsApi, type ListExperimentsParams } from "@/lib/api/experiments";
import type { ExperimentModel } from "@/types";

export function useExperiments(params?: Partial<ListExperimentsParams>) {
  const { data: session } = useSession();
  const { selectedGatewayId } = useGateway();
  const gatewayId = selectedGatewayId || params?.gatewayId;
  const userName = session?.user?.userName || session?.user?.name || "testuser";

  return useQuery({
    queryKey: ["experiments", { ...params, gatewayId, userName }],
    queryFn: () =>
      experimentsApi.list({
        gatewayId: gatewayId!,
        userName,
        ...params,
      }),
    enabled: !!gatewayId && !!userName,
  });
}

export function useExperiment(experimentId: string) {
  return useQuery({
    queryKey: ["experiment", experimentId],
    queryFn: () => experimentsApi.get(experimentId),
    enabled: !!experimentId,
  });
}

export function useExperimentProcesses(experimentId: string) {
  return useQuery({
    queryKey: ["experiment-processes", experimentId],
    queryFn: () => experimentsApi.getProcesses(experimentId),
    enabled: !!experimentId,
  });
}

export function useExperimentJobs(processId: string) {
  return useQuery({
    queryKey: ["experiment-jobs", processId],
    queryFn: () => experimentsApi.getJobs(processId),
    enabled: !!processId,
  });
}

export function useCreateExperiment() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (experiment: Partial<ExperimentModel>) => experimentsApi.create(experiment),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["experiments"] });
    },
  });
}

export function useUpdateExperiment() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ experimentId, experiment }: { experimentId: string; experiment: Partial<ExperimentModel> }) =>
      experimentsApi.update(experimentId, experiment),
    onSuccess: (_, { experimentId }) => {
      queryClient.invalidateQueries({ queryKey: ["experiments"] });
      queryClient.invalidateQueries({ queryKey: ["experiment", experimentId] });
    },
  });
}

export function useDeleteExperiment() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (experimentId: string) => experimentsApi.delete(experimentId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["experiments"] });
    },
  });
}

export function useLaunchExperiment() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (experimentId: string) => experimentsApi.launch(experimentId),
    onSuccess: (_, experimentId) => {
      queryClient.invalidateQueries({ queryKey: ["experiments"] });
      queryClient.invalidateQueries({ queryKey: ["experiment", experimentId] });
    },
  });
}

export function useCancelExperiment() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (experimentId: string) => experimentsApi.cancel(experimentId),
    onSuccess: (_, experimentId) => {
      queryClient.invalidateQueries({ queryKey: ["experiments"] });
      queryClient.invalidateQueries({ queryKey: ["experiment", experimentId] });
    },
  });
}

export function useCloneExperiment() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (experimentId: string) => experimentsApi.clone(experimentId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["experiments"] });
    },
  });
}
