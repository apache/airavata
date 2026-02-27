"use client";

import { Suspense } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import Link from "next/link";
import { ArrowLeft } from "lucide-react";
import { Button } from "@/components/ui/button";
import { CatalogResourceForm } from "@/components/catalog/CatalogResourceForm";
import { useCreateCatalogArtifact } from "@/hooks";
import { toast } from "@/hooks/useToast";
import { ArtifactType } from "@/types/catalog";
import type { CatalogArtifact } from "@/types/catalog";
import { Skeleton } from "@/components/ui/skeleton";

function AddCatalogResourceContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const typeParam = searchParams.get("type");
  const createResource = useCreateCatalogArtifact();

  const handleSubmit = async (resource: Partial<CatalogArtifact>) => {
    try {
      const result = await createResource.mutateAsync(resource);
      toast({
        title: "Resource created",
        description: "Your catalog resource has been created successfully.",
      });
      // Use permalink based on resource type
      const permalink = resource.type === "DATASET" 
        ? `/datasets/${result.id}`
        : `/repositories/${result.id}`;
      router.push(permalink);
    } catch (error) {
      toast({
        title: "Error",
        description: error instanceof Error ? error.message : "Failed to create resource",
        variant: "destructive",
      });
    }
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" asChild>
          <Link href="/catalog">
            <ArrowLeft className="h-5 w-5" />
          </Link>
        </Button>
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Add Resource</h1>
          <p className="text-muted-foreground">
            Share your research resource with the community
          </p>
        </div>
      </div>

      <CatalogResourceForm
        resource={typeParam ? { type: typeParam as ArtifactType } as CatalogArtifact : undefined}
        onSubmit={handleSubmit}
        onCancel={() => router.push("/catalog")}
        isLoading={createResource.isPending}
      />
    </div>
  );
}

export default function AddCatalogResourcePage() {
  return (
    <Suspense fallback={
      <div className="space-y-4">
        <Skeleton className="h-10 w-64" />
        <Skeleton className="h-96 w-full" />
      </div>
    }>
      <AddCatalogResourceContent />
    </Suspense>
  );
}
