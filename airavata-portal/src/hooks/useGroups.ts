"use client";

import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useGateway } from "@/contexts/GatewayContext";
import { groupsApi, type Group } from "@/lib/api/groups";

export function useGroups() {
  const { selectedGatewayId } = useGateway();
  const gatewayId = selectedGatewayId;

  return useQuery({
    queryKey: ["groups", gatewayId],
    queryFn: () => groupsApi.list(gatewayId || ""),
    enabled: !!gatewayId,
  });
}

export function useGroup(groupId: string) {
  return useQuery({
    queryKey: ["group", groupId],
    queryFn: () => groupsApi.get(groupId),
    enabled: !!groupId,
  });
}

export function useCreateGroup() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (group: Partial<Group>) => groupsApi.create(group),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["groups"] });
    },
  });
}

export function useUpdateGroup() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ groupId, group }: { groupId: string; group: Partial<Group> }) =>
      groupsApi.update(groupId, group),
    onSuccess: (_, { groupId }) => {
      queryClient.invalidateQueries({ queryKey: ["groups"] });
      queryClient.invalidateQueries({ queryKey: ["group", groupId] });
    },
  });
}

export function useDeleteGroup() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (groupId: string) => groupsApi.delete(groupId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["groups"] });
    },
  });
}

export function useAddGroupMember() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ groupId, userId }: { groupId: string; userId: string }) =>
      groupsApi.addMember(groupId, userId),
    onSuccess: (_, { groupId }) => {
      queryClient.invalidateQueries({ queryKey: ["group", groupId] });
    },
  });
}

export function useRemoveGroupMember() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ groupId, userId }: { groupId: string; userId: string }) =>
      groupsApi.removeMember(groupId, userId),
    onSuccess: (_, { groupId }) => {
      queryClient.invalidateQueries({ queryKey: ["group", groupId] });
    },
  });
}
