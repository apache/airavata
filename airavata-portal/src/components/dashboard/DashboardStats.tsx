"use client";

import { FlaskConical, FolderKanban, CheckCircle, XCircle, Loader2 } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import type { ExperimentModel, Project } from "@/types";
import { ExperimentState } from "@/types";

interface DashboardStatsProps {
  experiments?: ExperimentModel[];
  projects?: Project[];
  isLoading?: boolean;
}

export function DashboardStats({ experiments = [], projects = [], isLoading }: DashboardStatsProps) {
  const running = experiments.filter(
    (e) => e.experimentStatus?.[0]?.state === ExperimentState.EXECUTING
  ).length;
  const completed = experiments.filter(
    (e) => e.experimentStatus?.[0]?.state === ExperimentState.COMPLETED
  ).length;
  const failed = experiments.filter(
    (e) => e.experimentStatus?.[0]?.state === ExperimentState.FAILED
  ).length;

  if (isLoading) {
    return (
      <div className="grid gap-4 grid-cols-2">
        {[...Array(2)].map((_, i) => (
          <Card key={i}>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <Skeleton className="h-4 w-24" />
              <Skeleton className="h-8 w-8 rounded-full" />
            </CardHeader>
            <CardContent>
              <Skeleton className="h-8 w-16" />
            </CardContent>
          </Card>
        ))}
      </div>
    );
  }

  return (
    <div className="grid gap-4 grid-cols-2">
      <Card>
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle className="text-sm font-medium">Experiments</CardTitle>
          <div className="rounded-full p-2 bg-blue-100">
            <FlaskConical className="h-4 w-4 text-blue-600" />
          </div>
        </CardHeader>
        <CardContent>
          <div className="text-2xl font-bold">{experiments.length}</div>
          <div className="flex items-center gap-3 mt-2 text-xs text-muted-foreground">
            <span className="inline-flex items-center gap-1">
              <Loader2 className="h-3 w-3 text-yellow-600" />
              {running} running
            </span>
            <span className="inline-flex items-center gap-1">
              <CheckCircle className="h-3 w-3 text-green-600" />
              {completed} completed
            </span>
            <span className="inline-flex items-center gap-1">
              <XCircle className="h-3 w-3 text-red-600" />
              {failed} failed
            </span>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle className="text-sm font-medium">Projects</CardTitle>
          <div className="rounded-full p-2 bg-purple-100">
            <FolderKanban className="h-4 w-4 text-purple-600" />
          </div>
        </CardHeader>
        <CardContent>
          <div className="text-2xl font-bold">{projects.length}</div>
        </CardContent>
      </Card>
    </div>
  );
}
