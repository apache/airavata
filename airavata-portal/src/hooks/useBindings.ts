"use client";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { bindingsApi } from "@/lib/api";
import type { ResourceBinding } from "@/types";

export function useBindings(params?: { gatewayId?: string; resourceId?: string; credentialId?: string }) {
  const key = params ? ["bindings", JSON.stringify(params)] : (["bindings"] as const);
  const { data, error, isLoading } = useQuery<ResourceBinding[]>({
    queryKey: key,
    queryFn: () => bindingsApi.list(params),
    enabled: true,
  });
  const queryClient = useQueryClient();
  const mutate = () => queryClient.invalidateQueries({ queryKey: ["bindings"] });
  return { bindings: data ?? [], error, isLoading, mutate };
}

export function useBinding(bindingId?: string) {
  const { data, error, isLoading } = useQuery<ResourceBinding>({
    queryKey: ["binding", bindingId],
    queryFn: () => bindingsApi.get(bindingId!),
    enabled: !!bindingId,
  });
  const queryClient = useQueryClient();
  const mutate = () => queryClient.invalidateQueries({ queryKey: ["binding", bindingId] });
  return { binding: data, error, isLoading, mutate };
}
