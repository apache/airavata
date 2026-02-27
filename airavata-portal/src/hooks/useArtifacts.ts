"use client";

import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useSession } from "next-auth/react";
import { usePortalConfig } from "@/contexts/PortalConfigContext";
import { artifactsApi, type SearchArtifactsParams } from "@/lib/api/artifacts";
import type { ArtifactModel } from "@/types";

export function usePublicArtifacts(nameSearch?: string, pageNumber = 0, pageSize = 20) {
  return useQuery({
    queryKey: ["artifacts", "public", nameSearch, pageNumber, pageSize],
    queryFn: async () => {
      try {
        return await artifactsApi.getPublic(nameSearch, pageNumber, pageSize);
      } catch (error) {
        console.error("Error fetching public artifacts:", error);
        throw error;
      }
    },
    enabled: true,
    staleTime: 30 * 1000,
    retry: 1,
    refetchOnWindowFocus: false,
  });
}

export function useAccessibleArtifacts(
  userId: string,
  gatewayId: string,
  options?: { groupIds?: string[]; nameSearch?: string; pageNumber?: number; pageSize?: number }
) {
  return useQuery({
    queryKey: ["artifacts", "accessible", userId, gatewayId, options?.nameSearch, options?.pageNumber, options?.pageSize],
    queryFn: () =>
      artifactsApi.getAccessible({
        userId,
        gatewayId,
        groupIds: options?.groupIds,
        nameSearch: options?.nameSearch,
        pageNumber: options?.pageNumber ?? 0,
        pageSize: options?.pageSize ?? 50,
      }),
    enabled: !!userId && !!gatewayId,
    staleTime: 30 * 1000,
  });
}

export function useArtifacts(searchParams?: Partial<SearchArtifactsParams>) {
  const { data: session } = useSession();
  const { defaultGatewayId } = usePortalConfig();
  const gatewayId = session?.user?.gatewayId || defaultGatewayId;
  const userId = session?.user?.id || "";

  return useQuery({
    queryKey: ["artifacts", searchParams],
    queryFn: () =>
      artifactsApi.search({
        gatewayId,
        userId,
        name: searchParams?.name || "",
        limit: searchParams?.limit || 100,
        offset: searchParams?.offset || 0,
      }),
    enabled: !!userId,
  });
}

export function useArtifact(artifactUri: string | undefined) {
  return useQuery({
    queryKey: ["artifacts", artifactUri],
    queryFn: () => artifactsApi.get(artifactUri!),
    enabled: !!artifactUri,
  });
}

export function useArtifactParent(artifactUri: string | undefined) {
  return useQuery({
    queryKey: ["artifacts", artifactUri, "parent"],
    queryFn: () => artifactsApi.getParent(artifactUri!),
    enabled: !!artifactUri,
  });
}

export function useArtifactChildren(artifactUri: string | undefined) {
  return useQuery({
    queryKey: ["artifacts", artifactUri, "children"],
    queryFn: () => artifactsApi.getChildren(artifactUri!),
    enabled: !!artifactUri,
  });
}

export function useCreateArtifact() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (artifact: Partial<ArtifactModel>) => artifactsApi.create(artifact),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["artifacts"] });
    },
  });
}

export function useUpdateArtifact() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ artifactUri, artifact }: { artifactUri: string; artifact: Partial<ArtifactModel> }) =>
      artifactsApi.update(artifactUri, artifact),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ["artifacts"] });
      queryClient.invalidateQueries({ queryKey: ["artifacts", variables.artifactUri] });
    },
  });
}

export function useDeleteArtifact() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (artifactUri: string) => artifactsApi.delete(artifactUri),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["artifacts"] });
    },
  });
}
