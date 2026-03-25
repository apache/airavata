"use client";

import { useMemo } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useGateway } from "@/contexts/GatewayContext";
import { usePortalConfig } from "@/contexts/PortalConfigContext";
import { credentialsApi, type SSHCredential, type PasswordCredential } from "@/lib/api/credentials";
import { bindingsApi } from "@/lib/api/bindings";

export function useCredentials() {
  const { effectiveGatewayId } = useGateway();
  const { defaultGatewayId } = usePortalConfig();
  const gatewayId = effectiveGatewayId || defaultGatewayId;

  return useQuery({
    queryKey: ["credentials", gatewayId],
    queryFn: () => credentialsApi.list(gatewayId),
    enabled: !!gatewayId,
    staleTime: 5 * 60 * 1000,
  });
}

export function useCreateSSHCredential() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (credential: SSHCredential) => credentialsApi.createSSH(credential),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["credentials"] });
    },
  });
}

export function useCreatePasswordCredential() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (credential: PasswordCredential) => credentialsApi.createPassword(credential),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["credentials"] });
    },
  });
}

export function useDeleteCredential() {
  const { selectedGatewayId } = useGateway();
  const { defaultGatewayId } = usePortalConfig();
  const gatewayId = selectedGatewayId || defaultGatewayId;
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (token: string) => credentialsApi.delete(token, gatewayId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["credentials"] });
    },
  });
}

/** Returns application deployments that use a given credential token. */
export function useDeploymentsByCredential(credentialToken: string | null) {
  return useQuery({
    queryKey: ["deployments-by-credential", credentialToken],
    queryFn: () => Promise.resolve([]),
    enabled: false, // deployments by credential is no longer supported in new model
  });
}

export type CredentialResourceCounts = Record<string, { compute: number; storage: number }>;

/** Counts how many resource bindings each credential is used in. */
export function useCredentialResourceCounts() {
  const { effectiveGatewayId } = useGateway();
  const { defaultGatewayId } = usePortalConfig();
  const gatewayId = effectiveGatewayId || defaultGatewayId;

  const bindingsQuery = useQuery({
    queryKey: ["bindings", gatewayId],
    queryFn: () => bindingsApi.list({ gatewayId }),
    enabled: !!gatewayId,
    staleTime: 5 * 60 * 1000,
  });

  const counts = useMemo(() => {
    const map: CredentialResourceCounts = {};
    if (bindingsQuery.data) {
      for (const binding of bindingsQuery.data) {
        if (!binding.credentialId) continue;
        if (!map[binding.credentialId]) map[binding.credentialId] = { compute: 0, storage: 0 };
        // Bindings are for compute resources
        map[binding.credentialId].compute++;
      }
    }
    return map;
  }, [bindingsQuery.data]);

  return {
    counts,
    isLoading: bindingsQuery.isLoading,
  };
}
