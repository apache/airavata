"use client";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { applicationsApi } from "@/lib/api";
import type { Application } from "@/types";

export function useApplications(gatewayId?: string) {
  const { data, error, isLoading, refetch } = useQuery<Application[]>({
    queryKey: ["applications", gatewayId],
    queryFn: () => applicationsApi.list(gatewayId),
    enabled: !!gatewayId,
  });
  const queryClient = useQueryClient();
  const mutate = () => queryClient.invalidateQueries({ queryKey: ["applications", gatewayId] });
  return { applications: data ?? [], error, isLoading, mutate };
}

export function useApplication(applicationId?: string) {
  const { data, error, isLoading, refetch } = useQuery<Application>({
    queryKey: ["application", applicationId],
    queryFn: () => applicationsApi.get(applicationId!),
    enabled: !!applicationId,
  });
  const queryClient = useQueryClient();
  const mutate = () => queryClient.invalidateQueries({ queryKey: ["application", applicationId] });
  return { application: data, error, isLoading, mutate };
}
