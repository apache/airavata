"use client";

import { useParams, useRouter } from "next/navigation";
import Link from "next/link";
import { ArrowLeft, ArrowRight, AppWindow, Trash2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { apiClient, installationsApi } from "@/lib/api";
import { useCreateExperimentModal } from "@/contexts/CreateExperimentModalContext";
import { useUserRole } from "@/contexts/AdvancedFeaturesContext";
import { useGateway } from "@/contexts/GatewayContext";
import { toast } from "@/hooks/useToast";
import { useGatewayRouteGuard } from "@/lib/route-guards";
import type { Application, ApplicationInstallation } from "@/types";

export default function ApplicationDetailPage() {
  const params = useParams();
  const router = useRouter();
  const gatewayName = params.gatewayName as string;
  const appId = params.appId as string;
  const { effectiveGatewayId } = useGateway();
  const { selectedRole } = useUserRole();
  const { openModal } = useCreateExperimentModal();

  useGatewayRouteGuard(gatewayName);

  const isAdmin = selectedRole === "gateway-admin" || selectedRole === "system-admin";
  const [isDeleting, setIsDeleting] = useState(false);
  const [showDeleteDialog, setShowDeleteDialog] = useState(false);

  const {
    data: application,
    isLoading,
    error,
  } = useQuery({
    queryKey: ["application", appId],
    queryFn: () => apiClient.get<Application>(`/api/v1/applications/${appId}`),
    enabled: !!appId,
    retry: 2,
    staleTime: 30000,
  });

  const { data: installations = [], isLoading: isLoadingInstallations } = useQuery({
    queryKey: ["installations", appId],
    queryFn: () => installationsApi.list({ appId }),
    enabled: !!appId,
  });

  const handleDeleteApplication = async () => {
    setIsDeleting(true);
    try {
      await apiClient.delete(`/api/v1/applications/${appId}`);
      toast({
        title: "Application deleted",
        description: "Application deleted successfully.",
      });
      router.push(`/${gatewayName}/catalog`);
    } catch (err) {
      toast({
        title: "Error",
        description: err instanceof Error ? err.message : "Failed to delete application",
        variant: "destructive",
      });
    } finally {
      setIsDeleting(false);
      setShowDeleteDialog(false);
    }
  };

  const handleCreateExperiment = () => {
    if (application) {
      openModal({ application });
    }
  };

  if (isLoading) {
    return (
      <div className="space-y-6 p-6">
        <Skeleton className="h-10 w-full" />
        <Skeleton className="h-96 w-full" />
      </div>
    );
  }

  if (error || !application) {
    return (
      <div className="space-y-6 p-6">
        <div className="flex items-center gap-4">
          <Button variant="ghost" size="icon" asChild>
            <Link href={`/${gatewayName}/catalog`}>
              <ArrowLeft className="h-5 w-5" />
            </Link>
          </Button>
          <div>
            <h1 className="text-3xl font-bold tracking-tight">Application Not Found</h1>
            <p className="text-muted-foreground">
              The application you&apos;re looking for doesn&apos;t exist or you don&apos;t have access to it.
            </p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6 p-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Button variant="ghost" size="icon" asChild>
            <Link href={`/${gatewayName}/catalog`}>
              <ArrowLeft className="h-5 w-5" />
            </Link>
          </Button>
          <div className="flex items-start gap-4">
            <div className="flex h-14 w-14 items-center justify-center rounded-lg bg-gradient-to-br from-blue-500 to-indigo-600">
              <AppWindow className="h-7 w-7 text-white" />
            </div>
            <div>
              <h1 className="text-3xl font-bold tracking-tight">
                {application.name}
              </h1>
              <div className="flex items-center gap-2 mt-1">
                {application.version && (
                  <span className="text-sm text-muted-foreground">v{application.version}</span>
                )}
                <Badge variant={application.scope === "GATEWAY" ? "default" : "secondary"}>
                  {application.scope}
                </Badge>
              </div>
            </div>
          </div>
        </div>
        <div className="flex items-center gap-2">
          {isAdmin && (
            <Button
              variant="outline"
              onClick={() => setShowDeleteDialog(true)}
              className="text-destructive hover:text-destructive"
            >
              <Trash2 className="mr-2 h-4 w-4" />
              Delete
            </Button>
          )}
          <Button onClick={handleCreateExperiment}>
            Create Experiment
            <ArrowRight className="ml-2 h-4 w-4" />
          </Button>
        </div>
      </div>

      <div className="space-y-6">
        {/* Details Card */}
        <Card>
          <CardHeader>
            <CardTitle>Details</CardTitle>
            <CardDescription>Application configuration and scripts</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid gap-4 md:grid-cols-2">
              <div>
                <p className="text-sm text-muted-foreground">Name</p>
                <p className="font-medium">{application.name}</p>
              </div>
              {application.version && (
                <div>
                  <p className="text-sm text-muted-foreground">Version</p>
                  <p className="font-medium">{application.version}</p>
                </div>
              )}
              <div>
                <p className="text-sm text-muted-foreground">Description</p>
                <p className="text-sm">{application.description || "No description"}</p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Scope</p>
                <Badge variant={application.scope === "GATEWAY" ? "default" : "secondary"}>
                  {application.scope}
                </Badge>
              </div>
            </div>

            {application.installScript && (
              <div>
                <p className="text-sm font-medium mb-2">Install Script</p>
                <pre className="text-xs bg-muted p-3 rounded-lg overflow-x-auto whitespace-pre-wrap font-mono">
                  {application.installScript}
                </pre>
              </div>
            )}

            {application.runScript && (
              <div>
                <p className="text-sm font-medium mb-2">Run Script</p>
                <pre className="text-xs bg-muted p-3 rounded-lg overflow-x-auto whitespace-pre-wrap font-mono">
                  {application.runScript}
                </pre>
              </div>
            )}
          </CardContent>
        </Card>

        {/* Inputs */}
        <Card>
          <CardHeader>
            <CardTitle>Inputs ({application.inputs.length})</CardTitle>
            <CardDescription>Input fields required by this application</CardDescription>
          </CardHeader>
          <CardContent>
            {application.inputs.length === 0 ? (
              <p className="text-sm text-muted-foreground text-center py-4">No inputs defined</p>
            ) : (
              <div className="border rounded-lg overflow-hidden">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead className="h-9 px-3">Name</TableHead>
                      <TableHead className="h-9 px-3">Type</TableHead>
                      <TableHead className="h-9 px-3">Required</TableHead>
                      <TableHead className="h-9 px-3">Description</TableHead>
                      <TableHead className="h-9 px-3">Default</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {application.inputs.map((field, idx) => (
                      <TableRow key={idx}>
                        <TableCell className="py-1.5 px-3 font-medium">{field.name}</TableCell>
                        <TableCell className="py-1.5 px-3">
                          <Badge variant="outline" className="text-xs">{field.type}</Badge>
                        </TableCell>
                        <TableCell className="py-1.5 px-3">
                          {field.required ? (
                            <Badge className="text-xs bg-orange-500">Required</Badge>
                          ) : (
                            <Badge variant="secondary" className="text-xs">Optional</Badge>
                          )}
                        </TableCell>
                        <TableCell className="py-1.5 px-3 text-muted-foreground text-sm">
                          {field.description || "-"}
                        </TableCell>
                        <TableCell className="py-1.5 px-3 font-mono text-xs text-muted-foreground">
                          {field.defaultValue || "-"}
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </div>
            )}
          </CardContent>
        </Card>

        {/* Outputs */}
        <Card>
          <CardHeader>
            <CardTitle>Outputs ({application.outputs.length})</CardTitle>
            <CardDescription>Output fields produced by this application</CardDescription>
          </CardHeader>
          <CardContent>
            {application.outputs.length === 0 ? (
              <p className="text-sm text-muted-foreground text-center py-4">No outputs defined</p>
            ) : (
              <div className="border rounded-lg overflow-hidden">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead className="h-9 px-3">Name</TableHead>
                      <TableHead className="h-9 px-3">Type</TableHead>
                      <TableHead className="h-9 px-3">Required</TableHead>
                      <TableHead className="h-9 px-3">Description</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {application.outputs.map((field, idx) => (
                      <TableRow key={idx}>
                        <TableCell className="py-1.5 px-3 font-medium">{field.name}</TableCell>
                        <TableCell className="py-1.5 px-3">
                          <Badge variant="outline" className="text-xs">{field.type}</Badge>
                        </TableCell>
                        <TableCell className="py-1.5 px-3">
                          {field.required ? (
                            <Badge className="text-xs bg-orange-500">Required</Badge>
                          ) : (
                            <Badge variant="secondary" className="text-xs">Optional</Badge>
                          )}
                        </TableCell>
                        <TableCell className="py-1.5 px-3 text-muted-foreground text-sm">
                          {field.description || "-"}
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </div>
            )}
          </CardContent>
        </Card>

        {/* Installations */}
        <Card>
          <CardHeader>
            <CardTitle>Installations</CardTitle>
            <CardDescription>Deployment status on compute resources</CardDescription>
          </CardHeader>
          <CardContent>
            {isLoadingInstallations ? (
              <Skeleton className="h-32 w-full" />
            ) : installations.length === 0 ? (
              <p className="text-sm text-muted-foreground text-center py-8">
                No installations configured
              </p>
            ) : (
              <div className="border rounded-lg overflow-hidden">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead className="h-9 px-3">Resource ID</TableHead>
                      <TableHead className="h-9 px-3">Login Username</TableHead>
                      <TableHead className="h-9 px-3">Install Path</TableHead>
                      <TableHead className="h-9 px-3">Status</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {installations.map((inst: ApplicationInstallation) => (
                      <TableRow key={inst.installationId}>
                        <TableCell className="py-1.5 px-3 font-mono text-xs">
                          {inst.resourceId}
                        </TableCell>
                        <TableCell className="py-1.5 px-3">{inst.loginUsername}</TableCell>
                        <TableCell className="py-1.5 px-3 font-mono text-xs">
                          {inst.installPath || "-"}
                        </TableCell>
                        <TableCell className="py-1.5 px-3">
                          <Badge
                            variant={
                              inst.status === "INSTALLED"
                                ? "default"
                                : inst.status === "FAILED"
                                ? "destructive"
                                : "secondary"
                            }
                            className="text-xs"
                          >
                            {inst.status}
                          </Badge>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      {/* Delete Dialog */}
      <AlertDialog open={showDeleteDialog} onOpenChange={setShowDeleteDialog}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Delete Application</AlertDialogTitle>
            <AlertDialogDescription>
              Are you sure you want to delete &quot;{application.name}&quot;? This action cannot be undone.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel disabled={isDeleting}>Cancel</AlertDialogCancel>
            <AlertDialogAction
              onClick={handleDeleteApplication}
              className="bg-destructive text-destructive-foreground"
              disabled={isDeleting}
            >
              {isDeleting ? "Deleting..." : "Delete"}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
