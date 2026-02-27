"use client";

import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useSession } from "next-auth/react";
import { catalogApi } from "@/lib/api/catalog";
import type { CatalogArtifact, ArtifactFilters } from "@/types/catalog";

export function useCatalogArtifacts(filters?: ArtifactFilters) {
  return useQuery({
    queryKey: ["catalog-artifacts", filters],
    queryFn: async () => {
      try {
        const artifacts = await catalogApi.listPublic(filters || {});
        return artifacts || [];
      } catch (error) {
        console.error("Error fetching catalog artifacts:", error);
        throw error;
      }
    },
    enabled: filters !== undefined,
    retry: 1,
    staleTime: 30 * 1000,
    refetchOnWindowFocus: false,
    gcTime: 5 * 60 * 1000,
  });
}

export function useCatalogArtifact(artifactId: string) {
  return useQuery({
    queryKey: ["catalog-artifact", artifactId],
    queryFn: () => catalogApi.getPublic(artifactId),
    enabled: !!artifactId,
  });
}

export function useCatalogTags() {
  return useQuery({
    queryKey: ["catalog-tags"],
    queryFn: async () => {
      try {
        const tags = await catalogApi.getAllTags();
        return tags || [];
      } catch (error) {
        console.error("Error fetching catalog tags:", error);
        return [];
      }
    },
    enabled: true,
    retry: 1,
    staleTime: 5 * 60 * 1000,
  });
}

export function useStarredArtifacts() {
  const { data: session } = useSession();
  const userEmail = session?.user?.email || "";

  return useQuery({
    queryKey: ["starred-artifacts", userEmail],
    queryFn: () => catalogApi.getStarred(userEmail),
    enabled: !!userEmail,
  });
}

export function useStarArtifact() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (artifactId: string) => catalogApi.star(artifactId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["starred-artifacts"] });
      queryClient.invalidateQueries({ queryKey: ["catalog-artifacts"] });
    },
  });
}

export function useCreateCatalogArtifact() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (artifact: Partial<CatalogArtifact>) => catalogApi.create(artifact),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["catalog-artifacts"] });
    },
  });
}

export function useUpdateCatalogArtifact() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ artifactId, artifact }: { artifactId: string; artifact: Partial<CatalogArtifact> }) =>
      catalogApi.update(artifactId, artifact),
    onSuccess: (_, { artifactId }) => {
      queryClient.invalidateQueries({ queryKey: ["catalog-artifacts"] });
      queryClient.invalidateQueries({ queryKey: ["catalog-artifact", artifactId] });
    },
  });
}

export function useDeleteCatalogArtifact() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (artifactId: string) => catalogApi.delete(artifactId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["catalog-artifacts"] });
    },
  });
}
