"use client";

import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { gatewayConfigApi } from "@/lib/api/gateway-config";

export function useGatewayConfig(gatewayId: string | undefined) {
  return useQuery({
    queryKey: ["gateway-config", gatewayId],
    queryFn: () => gatewayConfigApi.getConfig(gatewayId!),
    enabled: !!gatewayId,
  });
}

export function useSetGatewayConfig() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({
      gatewayId,
      key,
      value,
    }: {
      gatewayId: string;
      key: string;
      value: string;
    }) => gatewayConfigApi.setConfig(gatewayId, { key, value }),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ["gateway-config", variables.gatewayId] });
    },
  });
}

export function useDeleteGatewayConfig() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ gatewayId, key }: { gatewayId: string; key: string }) =>
      gatewayConfigApi.deleteConfig(gatewayId, key),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ["gateway-config", variables.gatewayId] });
    },
  });
}

export function useGatewayFeatureFlags(gatewayId: string | undefined) {
  return useQuery({
    queryKey: ["gateway-config", gatewayId, "features"],
    queryFn: () => gatewayConfigApi.getFeatureFlags(gatewayId!),
    enabled: !!gatewayId,
  });
}

export function useSetFeatureEnabled() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({
      gatewayId,
      feature,
      enabled,
    }: {
      gatewayId: string;
      feature: string;
      enabled: boolean;
    }) => gatewayConfigApi.setFeatureEnabled(gatewayId, feature, enabled),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({
        queryKey: ["gateway-config", variables.gatewayId, "features"],
      });
      queryClient.invalidateQueries({ queryKey: ["gateway-config", variables.gatewayId] });
    },
  });
}

export function useGatewayMaintenanceMode(gatewayId: string | undefined) {
  return useQuery({
    queryKey: ["gateway-config", gatewayId, "maintenance"],
    queryFn: () => gatewayConfigApi.getMaintenanceMode(gatewayId!),
    enabled: !!gatewayId,
  });
}

export function useSetMaintenanceMode() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({
      gatewayId,
      enabled,
      message,
    }: {
      gatewayId: string;
      enabled: boolean;
      message?: string;
    }) => gatewayConfigApi.setMaintenanceMode(gatewayId, enabled, message),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({
        queryKey: ["gateway-config", variables.gatewayId, "maintenance"],
      });
      queryClient.invalidateQueries({ queryKey: ["gateway-config", variables.gatewayId] });
    },
  });
}
