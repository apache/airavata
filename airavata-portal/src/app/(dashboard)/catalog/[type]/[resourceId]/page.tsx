"use client";

import { useParams } from "next/navigation";
import Link from "next/link";
import { ArrowLeft } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { ResourceDetail } from "@/components/catalog/ResourceDetail";
import { CatalogApplicationDetail } from "@/components/catalog/CatalogApplicationDetail";
import { useCatalogArtifact } from "@/hooks";

export default function ResourceDetailPage() {
  const params = useParams();
  const resourceId = params.resourceId as string;
  const type = params.type as string;

  // If type is APPLICATION, use the application detail component (back + title inline inside component)
  if (type === "APPLICATION") {
    return (
      <div className="space-y-4">
        <CatalogApplicationDetail appId={resourceId} backHref="/catalog" />
      </div>
    );
  }

  // For other types (DATASET, REPOSITORY), use the catalog resource detail with inline header like storage/compute
  const { data: resource, isLoading } = useCatalogArtifact(resourceId);

  if (isLoading) {
    return (
      <div className="space-y-4">
        <Skeleton className="h-10 w-64" />
        <Skeleton className="h-96 w-full" />
      </div>
    );
  }

  if (!resource) {
    return (
      <div className="space-y-4">
        <div className="flex items-center gap-4">
          <Button variant="ghost" size="icon" asChild>
            <Link href="/catalog">
              <ArrowLeft className="h-5 w-5" />
            </Link>
          </Button>
          <div>
            <h1 className="text-3xl font-bold tracking-tight">Catalog Resource</h1>
            <p className="text-muted-foreground">Resource not found</p>
          </div>
        </div>
        <Button asChild>
          <Link href="/catalog">Back to Catalog</Link>
        </Button>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {/* Header: back button inline with title and subtitle (like storage/compute) */}
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" asChild>
          <Link href="/catalog">
            <ArrowLeft className="h-5 w-5" />
          </Link>
        </Button>
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-3xl font-bold tracking-tight">{resource.name}</h1>
          </div>
          <p className="text-muted-foreground">{resource.description || "Catalog resource details"}</p>
        </div>
      </div>

      <ResourceDetail resource={resource} showHeader={false} />
    </div>
  );
}
