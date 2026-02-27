"use client";

import { Skeleton } from "@/components/ui/skeleton";
import { Card, CardContent } from "@/components/ui/card";
import { ResourceCard } from "./ResourceCard";
import type { CatalogArtifact } from "@/types/catalog";
import { FileQuestion } from "lucide-react";

interface Props {
  resources?: CatalogArtifact[];
  isLoading: boolean;
}

export function ResourceGrid({ resources, isLoading }: Props) {
  if (isLoading) {
    return (
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        {[...Array(6)].map((_, i) => (
          <Skeleton key={i} className="h-80" />
        ))}
      </div>
    );
  }

  if (!resources || resources.length === 0) {
    return (
      <Card>
        <CardContent className="py-16">
          <div className="text-center">
            <FileQuestion className="mx-auto h-12 w-12 text-muted-foreground/50" />
            <h3 className="mt-4 text-lg font-semibold">No resources found</h3>
            <p className="text-muted-foreground mt-2">
              Try adjusting your filters or search terms
            </p>
          </div>
        </CardContent>
      </Card>
    );
  }

  return (
    <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
      {resources.map((resource) => (
        <ResourceCard key={resource.id} resource={resource} />
      ))}
    </div>
  );
}
