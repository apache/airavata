"use client";

import { useParams, useRouter } from "next/navigation";
import Link from "next/link";
import { ArrowLeft } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { ProjectForm } from "@/components/project";
import { useProject, useUpdateProject } from "@/hooks";
import { useGateway } from "@/contexts/GatewayContext";

export default function EditProjectPage() {
  const params = useParams();
  const router = useRouter();
  const projectId = params.projectId as string;
  const { selectedGatewayId, getGatewayName, accessibleGateways } = useGateway();
  const dashboardHref =
    selectedGatewayId && accessibleGateways?.length
      ? `/${getGatewayName(selectedGatewayId)}`
      : "/default";
  
  const { data: project, isLoading } = useProject(projectId);
  const updateProject = useUpdateProject();

  const handleSubmit = async (data: { name: string; description?: string }) => {
    await updateProject.mutateAsync({
      projectId,
      project: {
        name: data.name,
        description: data.description,
      },
    });
    router.push(`/projects/${projectId}`);
  };

  if (isLoading) {
    return (
      <div className="space-y-4">
        <div className="flex items-center gap-4">
          <Skeleton className="h-10 w-10" />
          <Skeleton className="h-8 w-48" />
        </div>
        <Skeleton className="h-64 w-full max-w-2xl" />
      </div>
    );
  }

  if (!project) {
    return (
      <div className="flex flex-col items-center justify-center py-16">
        <h2 className="text-xl font-semibold">Project not found</h2>
        <Button asChild className="mt-4">
          <Link href={dashboardHref}>Back to Dashboard</Link>
        </Button>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" asChild>
          <Link href={`/projects/${projectId}`}>
            <ArrowLeft className="h-5 w-5" />
          </Link>
        </Button>
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Edit Project</h1>
          <p className="text-muted-foreground">Update project details</p>
        </div>
      </div>

      <Card className="max-w-2xl">
        <CardHeader>
          <CardTitle>Project Details</CardTitle>
          <CardDescription>
            Update the name and description of your project
          </CardDescription>
        </CardHeader>
        <CardContent>
          <ProjectForm
            project={project}
            onSubmit={handleSubmit}
            onCancel={() => router.push(`/projects/${projectId}`)}
            isLoading={updateProject.isPending}
          />
        </CardContent>
      </Card>
    </div>
  );
}
