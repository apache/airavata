"use client";

import { useParams } from "next/navigation";
import { useSession } from "next-auth/react";
import { useQuery } from "@tanstack/react-query";
import { artifactsApi } from "@/lib/api/artifacts";
import { ResourceDetail } from "@/components/catalog/ResourceDetail";
import { Skeleton } from "@/components/ui/skeleton";
import { Button } from "@/components/ui/button";
import Link from "next/link";
import { ArrowLeft } from "lucide-react";
import { NoPermissions } from "@/components/errors/NoPermissions";
import { NotFound } from "@/components/errors/NotFound";
import type { CatalogArtifact } from "@/types/catalog";
import { ArtifactType } from "@/types/catalog";

export default function DatasetPermalinkPage() {
  const params = useParams();
  const { data: session } = useSession();
  const datasetId = params.datasetId as string;

  const { data: resource, isLoading, error } = useQuery<CatalogArtifact | null>({
    queryKey: ["artifact-dataset", datasetId],
    queryFn: async () => {
      try {
        const dp = await artifactsApi.get(datasetId);
        return {
          id: dp.artifactUri,
          type: ArtifactType.DATASET,
          name: dp.name ?? "",
          description: dp.description ?? "",
          authors: dp.authors ?? [],
          tags: (dp.tags ?? []).map((t) => ({ id: t.id ?? t.name ?? "", name: t.name ?? t.id ?? "" })),
          headerImage: dp.headerImage,
          datasetUrl: dp.primaryStorageResourceId && dp.primaryFilePath
            ? `${dp.primaryStorageResourceId}:${dp.primaryFilePath}`
            : undefined,
          size: dp.size,
          format: dp.format,
        } as CatalogArtifact;
      } catch (err: any) {
        if (err?.response?.status === 404) {
          return null;
        }
        if (err?.response?.status === 403 || err?.response?.status === 401) {
          throw new Error("NO_PERMISSIONS");
        }
        throw err;
      }
    },
    enabled: !!datasetId && !!session?.accessToken,
  });

  if (isLoading) {
    return (
      <div className="space-y-6 p-6">
        <Skeleton className="h-10 w-full" />
        <Skeleton className="h-96 w-full" />
      </div>
    );
  }

  if (error) {
    if (error.message === "NO_PERMISSIONS") {
      return <NoPermissions resourceType="dataset" resourceId={datasetId} />;
    }
    return <NotFound resourceType="dataset" resourceId={datasetId} />;
  }

  if (!resource) {
    return <NotFound resourceType="dataset" resourceId={datasetId} />;
  }

  return (
    <div className="space-y-6 p-6">
      <Button variant="ghost" size="icon" asChild>
        <Link href="/catalog">
          <ArrowLeft className="h-5 w-5" />
        </Link>
      </Button>
      <ResourceDetail resource={resource} />
    </div>
  );
}
