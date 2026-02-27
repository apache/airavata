"use client";

import { FlaskConical } from "lucide-react";
import { ExperimentCard } from "./ExperimentCard";
import { Skeleton } from "@/components/ui/skeleton";
import type { ExperimentModel } from "@/types";

interface ExperimentListProps {
  experiments?: ExperimentModel[];
  isLoading?: boolean;
  onDelete?: (experiment: ExperimentModel) => void;
  onCancel?: (experiment: ExperimentModel) => void;
}

export function ExperimentList({ experiments = [], isLoading, onDelete, onCancel }: ExperimentListProps) {
  if (isLoading) {
    return (
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        {[...Array(6)].map((_, i) => (
          <div key={i} className="rounded-xl border p-6 space-y-4">
            <div className="flex items-start gap-3">
              <Skeleton className="h-10 w-10 rounded-lg" />
              <div className="space-y-2 flex-1">
                <Skeleton className="h-5 w-full" />
                <Skeleton className="h-4 w-24" />
              </div>
              <Skeleton className="h-6 w-20 rounded-full" />
            </div>
            <Skeleton className="h-12 w-full" />
            <Skeleton className="h-4 w-32" />
          </div>
        ))}
      </div>
    );
  }

  if (experiments.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-16 text-center">
        <FlaskConical className="h-16 w-16 text-muted-foreground mb-4" />
        <h3 className="text-lg font-semibold">No experiments found</h3>
        <p className="text-muted-foreground mt-1">
          Create your first experiment to get started
        </p>
      </div>
    );
  }

  return (
    <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
      {experiments.map((experiment) => (
        <ExperimentCard
          key={experiment.experimentId}
          experiment={experiment}
          onDelete={onDelete}
          onCancel={onCancel}
        />
      ))}
    </div>
  );
}
