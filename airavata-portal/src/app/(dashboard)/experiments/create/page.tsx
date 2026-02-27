"use client";

import { Suspense } from "react";
import { useSearchParams } from "next/navigation";
import { CreateExperimentWizard } from "@/components/experiment/CreateExperimentWizard";
import { Skeleton } from "@/components/ui/skeleton";
import { Button } from "@/components/ui/button";
import Link from "next/link";
import { ArrowLeft } from "lucide-react";
import { useGateway } from "@/contexts/GatewayContext";

function CreateExperimentContent() {
  const searchParams = useSearchParams();
  const projectId = searchParams?.get("projectId");
  const { selectedGatewayId, getGatewayName, accessibleGateways } = useGateway();
  const dashboardHref =
    selectedGatewayId && accessibleGateways?.length
      ? `/${getGatewayName(selectedGatewayId)}`
      : "/default";

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" asChild>
          <Link href={dashboardHref}>
            <ArrowLeft className="h-4 w-4" />
          </Link>
        </Button>
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Create Experiment</h1>
          <p className="text-muted-foreground">
            {projectId 
              ? "Set up a new computational experiment in this project"
              : "Set up a new computational experiment (project required)"}
          </p>
        </div>
      </div>

      {!projectId && (
        <div className="border rounded-lg p-4 bg-yellow-50 dark:bg-yellow-900/20">
          <p className="text-sm text-yellow-800 dark:text-yellow-200">
            <strong>Note:</strong> Experiments must be created within a project. Please select a project in the first step.
          </p>
        </div>
      )}

      <CreateExperimentWizard />
    </div>
  );
}

export default function CreateExperimentPage() {
  return (
    <Suspense
      fallback={
        <div className="space-y-6">
          <Skeleton className="h-10 w-64" />
          <Skeleton className="h-[400px] w-full" />
        </div>
      }
    >
      <CreateExperimentContent />
    </Suspense>
  );
}
