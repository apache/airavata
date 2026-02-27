"use client";

import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { gatewaysApi } from "@/lib/api/gateways";
import type { Gateway } from "@/types";

export function useGateways() {
  return useQuery({
    queryKey: ["gateways"],
    queryFn: () => gatewaysApi.list(),
    // Gateways rarely change, so use longer stale time
    staleTime: 10 * 60 * 1000, // 10 minutes
  });
}

export function useGateway(gatewayId: string | undefined) {
  return useQuery({
    queryKey: ["gateways", gatewayId],
    queryFn: () => gatewaysApi.get(gatewayId!),
    enabled: !!gatewayId,
    // Individual gateway data rarely changes
    staleTime: 10 * 60 * 1000, // 10 minutes
  });
}

export function useCreateGateway() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (gateway: Partial<Gateway>) => gatewaysApi.create(gateway),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["gateways"] });
    },
  });
}

export function useUpdateGateway() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ gatewayId, gateway }: { gatewayId: string; gateway: Partial<Gateway> }) =>
      gatewaysApi.update(gatewayId, gateway),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ["gateways"] });
      queryClient.invalidateQueries({ queryKey: ["gateways", variables.gatewayId] });
    },
  });
}

export function useDeleteGateway() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (gatewayId: string) => gatewaysApi.delete(gatewayId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["gateways"] });
    },
  });
}
