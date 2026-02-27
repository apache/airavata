"use client";

import { useParams } from "next/navigation";
import Link from "next/link";
import { ArrowLeft, Pencil, FlaskConical, Calendar, User } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { useProject, useExperiments } from "@/hooks";
import { formatDate } from "@/lib/utils";
import { Badge } from "@/components/ui/badge";
import { getExperimentStatusColor } from "@/lib/utils";
import { getExperimentPermalink } from "@/lib/permalink";
import { useGateway } from "@/contexts/GatewayContext";

export default function ProjectDetailPage() {
  const params = useParams();
  const projectId = params.projectId as string;
  const { selectedGatewayId, getGatewayName, accessibleGateways } = useGateway();
  const dashboardHref =
    selectedGatewayId && accessibleGateways?.length
      ? `/${getGatewayName(selectedGatewayId)}`
      : "/default";

  const { data: project, isLoading: projectLoading } = useProject(projectId);
  const { data: experiments, isLoading: experimentsLoading } = useExperiments({ projectId });

  if (projectLoading) {
    return (
      <div className="space-y-4">
        <div className="flex items-center gap-4">
          <Skeleton className="h-10 w-10" />
          <div className="space-y-2">
            <Skeleton className="h-8 w-64" />
            <Skeleton className="h-4 w-32" />
          </div>
        </div>
        <Skeleton className="h-48 w-full" />
      </div>
    );
  }

  if (!project) {
    return (
      <div className="flex flex-col items-center justify-center py-16">
        <h2 className="text-xl font-semibold">Project not found</h2>
        <p className="text-muted-foreground mt-2">The requested project could not be found.</p>
        <Button asChild className="mt-4">
          <Link href={dashboardHref}>Back to Dashboard</Link>
        </Button>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Button variant="ghost" size="icon" asChild>
            <Link href={dashboardHref}>
              <ArrowLeft className="h-5 w-5" />
            </Link>
          </Button>
          <div>
            <h1 className="text-3xl font-bold tracking-tight">{project.name}</h1>
            <p className="text-muted-foreground">{project.description || "No description"}</p>
          </div>
        </div>
        <Button asChild>
          <Link href={`/projects/${projectId}/edit`}>
            <Pencil className="mr-2 h-4 w-4" />
            Edit Project
          </Link>
        </Button>
      </div>

      <div className="grid gap-6 md:grid-cols-3">
        <Card>
          <CardHeader className="flex flex-row items-center gap-2">
            <User className="h-4 w-4 text-muted-foreground" />
            <CardTitle className="text-sm font-medium">Owner</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-lg font-semibold">{project.owner}</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center gap-2">
            <Calendar className="h-4 w-4 text-muted-foreground" />
            <CardTitle className="text-sm font-medium">Created</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-lg font-semibold">{formatDate(project.creationTime)}</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center gap-2">
            <FlaskConical className="h-4 w-4 text-muted-foreground" />
            <CardTitle className="text-sm font-medium">Experiments</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-lg font-semibold">{experiments?.length || 0}</p>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Experiments</CardTitle>
          <CardDescription>Experiments in this project</CardDescription>
        </CardHeader>
        <CardContent>
          {experimentsLoading ? (
            <div className="space-y-4">
              {[...Array(3)].map((_, i) => (
                <Skeleton key={i} className="h-16 w-full" />
              ))}
            </div>
          ) : experiments?.length === 0 ? (
            <div className="text-center py-8">
              <FlaskConical className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
              <p className="text-muted-foreground">No experiments in this project</p>
              <Button asChild className="mt-4">
                <Link href="/catalog">Create Experiment</Link>
              </Button>
            </div>
          ) : (
            <div className="space-y-2">
              {experiments?.map((experiment) => {
                const status = experiment.experimentStatus?.[0]?.state || "UNKNOWN";
                return (
                  <Link
                    key={experiment.experimentId}
                    href={getExperimentPermalink(experiment.experimentId)}
                    className="flex items-center justify-between p-4 rounded-lg border hover:bg-muted/50 transition-colors"
                  >
                    <div>
                      <p className="font-medium">{experiment.experimentName}</p>
                      <p className="text-sm text-muted-foreground">
                        {formatDate(experiment.creationTime)}
                      </p>
                    </div>
                    <Badge variant="outline" className={`${getExperimentStatusColor(status)}`}>
                      {status}
                    </Badge>
                  </Link>
                );
              })}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
