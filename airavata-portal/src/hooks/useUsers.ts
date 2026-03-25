"use client";

import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useSession } from "next-auth/react";
import { useGateway } from "@/contexts/GatewayContext";
import { usePortalConfig } from "@/contexts/PortalConfigContext";
import { usersApi, type User } from "@/lib/api/users";

export function useUsers() {
  const { accessibleGateways, effectiveGatewayId } = useGateway();
  const { defaultGatewayId } = usePortalConfig();
  const gatewayId = effectiveGatewayId || defaultGatewayId;

  return useQuery({
    queryKey: ["users", gatewayId, accessibleGateways.map((g) => g.gatewayId).join(",")],
    queryFn: async () => {
      const users = await usersApi.list(gatewayId, 100, 0);
      return users.map((user) => {
        if (!user.gatewayId) user.gatewayId = gatewayId;
        return user;
      });
    },
    enabled: !!effectiveGatewayId || accessibleGateways.length > 0,
  });
}

export function useUser(userId: string) {
  const { effectiveGatewayId } = useGateway();
  const { defaultGatewayId } = usePortalConfig();
  const gatewayId = effectiveGatewayId || defaultGatewayId;

  return useQuery({
    queryKey: ["user", userId, gatewayId],
    queryFn: () => usersApi.get(userId, gatewayId),
    enabled: !!userId && !!gatewayId,
  });
}

export function useUpdateUser() {
  const queryClient = useQueryClient();
  const { data: session } = useSession();
  const { defaultGatewayId } = usePortalConfig();
  const gatewayId = session?.user?.gatewayId || defaultGatewayId;

  return useMutation({
    mutationFn: ({ userId, user }: { userId: string; user: Partial<User> }) =>
      usersApi.update(userId, gatewayId, user),
    onSuccess: (_, { userId }) => {
      queryClient.invalidateQueries({ queryKey: ["users"] });
      queryClient.invalidateQueries({ queryKey: ["user", userId] });
    },
  });
}

export function useEnableUser() {
  const queryClient = useQueryClient();
  const { data: session } = useSession();
  const { defaultGatewayId } = usePortalConfig();
  const gatewayId = session?.user?.gatewayId || defaultGatewayId;

  return useMutation({
    mutationFn: (userId: string) => usersApi.enable(userId, gatewayId),
    onSuccess: (_, userId) => {
      queryClient.invalidateQueries({ queryKey: ["users"] });
      queryClient.invalidateQueries({ queryKey: ["user", userId] });
    },
  });
}

export function useDisableUser() {
  const queryClient = useQueryClient();
  const { data: session } = useSession();
  const { defaultGatewayId } = usePortalConfig();
  const gatewayId = session?.user?.gatewayId || defaultGatewayId;

  return useMutation({
    mutationFn: (userId: string) => usersApi.disable(userId, gatewayId),
    onSuccess: (_, userId) => {
      queryClient.invalidateQueries({ queryKey: ["users"] });
      queryClient.invalidateQueries({ queryKey: ["user", userId] });
    },
  });
}

export function useDeleteUser() {
  const queryClient = useQueryClient();
  const { data: session } = useSession();
  const { defaultGatewayId } = usePortalConfig();
  const gatewayId = session?.user?.gatewayId || defaultGatewayId;

  return useMutation({
    mutationFn: (userId: string) => usersApi.delete(userId, gatewayId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["users"] });
    },
  });
}
