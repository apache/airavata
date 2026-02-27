"use client";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { resourcesApi } from "@/lib/api";
import type { Resource } from "@/types";

export function useResources(gatewayId?: string) {
  const { data, error, isLoading } = useQuery<Resource[]>({
    queryKey: ["resources", gatewayId],
    queryFn: () => resourcesApi.list(gatewayId),
    enabled: !!gatewayId,
  });
  const queryClient = useQueryClient();
  const mutate = () => queryClient.invalidateQueries({ queryKey: ["resources", gatewayId] });
  return { resources: data ?? [], error, isLoading, mutate };
}

export function useResource(resourceId?: string) {
  const { data, error, isLoading } = useQuery<Resource>({
    queryKey: ["resource", resourceId],
    queryFn: () => resourcesApi.get(resourceId!),
    enabled: !!resourceId,
  });
  const queryClient = useQueryClient();
  const mutate = () => queryClient.invalidateQueries({ queryKey: ["resource", resourceId] });
  return { resource: data, error, isLoading, mutate };
}
