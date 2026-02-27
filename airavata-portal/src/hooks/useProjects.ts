"use client";

import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useGateway } from "@/contexts/GatewayContext";
import { usePortalConfig } from "@/contexts/PortalConfigContext";
import { projectsApi, type ListProjectsParams } from "@/lib/api/projects";
import type { Project } from "@/types";

export function useProjects(params?: ListProjectsParams) {
  const { effectiveGatewayId } = useGateway();
  const gatewayId = effectiveGatewayId || params?.gatewayId;

  return useQuery({
    queryKey: ["projects", { ...params, gatewayId }],
    queryFn: () => projectsApi.list({ gatewayId, ...params }),
    enabled: !!gatewayId,
  });
}

export function useProject(projectId: string) {
  return useQuery({
    queryKey: ["project", projectId],
    queryFn: () => projectsApi.get(projectId),
    enabled: !!projectId,
  });
}

export function useCreateProject() {
  const queryClient = useQueryClient();
  const { selectedGatewayId, accessibleGateways, isLoading: gatewaysLoading } = useGateway();
  const { defaultGatewayId } = usePortalConfig();

  return useMutation({
    mutationFn: (project: Partial<Project>) => {
      // Wait for gateways to load if still loading
      if (gatewaysLoading) {
        throw new Error("Please wait for gateways to load before creating a project.");
      }
      
      let gatewayId: string | undefined = selectedGatewayId ?? undefined;
      if (!gatewayId && accessibleGateways.length > 0) {
        gatewayId = accessibleGateways[0].gatewayId;
      }
      if (!gatewayId) {
        gatewayId = defaultGatewayId;
      }
      
      if (!gatewayId) {
        throw new Error("Gateway must be selected. Please select a gateway before creating a project.");
      }
      
      return projectsApi.create(project, gatewayId);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["projects"] });
    },
  });
}

export function useUpdateProject() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ projectId, project }: { projectId: string; project: Partial<Project> }) =>
      projectsApi.update(projectId, project),
    onSuccess: (_, { projectId }) => {
      queryClient.invalidateQueries({ queryKey: ["projects"] });
      queryClient.invalidateQueries({ queryKey: ["project", projectId] });
    },
  });
}

export function useDeleteProject() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (projectId: string) => projectsApi.delete(projectId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["projects"] });
    },
  });
}
