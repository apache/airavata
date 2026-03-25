"use client";

import { Fragment, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { Trash2, Plus, AlertCircle, MoreVertical, AppWindow, ArrowRightToLine, ArrowLeftFromLine, ArrowLeft, ChevronRight } from "lucide-react";
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
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { useQuery } from "@tanstack/react-query";
import { apiClient, installationsApi } from "@/lib/api";
import { toast } from "@/hooks/useToast";
import { useUserRole } from "@/contexts/AdvancedFeaturesContext";
import { useCreateExperimentModal } from "@/contexts/CreateExperimentModalContext";
import { cn } from "@/lib/utils";
import type { Application, ApplicationInstallation } from "@/types";

interface CatalogApplicationDetailProps {
  appId: string;
  backHref?: string;
}

export function CatalogApplicationDetail({ appId, backHref }: CatalogApplicationDetailProps) {
  const router = useRouter();
  const { selectedRole } = useUserRole();
  const { openModal } = useCreateExperimentModal();
  const isAdmin = selectedRole === "gateway-admin" || selectedRole === "system-admin";

  const [isDeletingApp, setIsDeletingApp] = useState(false);
  const [showDeleteAppDialog, setShowDeleteAppDialog] = useState(false);
  const [collapsedFieldGroups, setCollapsedFieldGroups] = useState<Set<string>>(new Set());

  const toggleFieldGroup = (group: string) => {
    setCollapsedFieldGroups((prev) => {
      const next = new Set(prev);
      if (next.has(group)) next.delete(group);
      else next.add(group);
      return next;
    });
  };

  const {
    data: application,
    isLoading,
    error,
  } = useQuery({
    queryKey: ["application", appId],
    queryFn: async () => apiClient.get<Application>(`/api/v1/applications/${appId}`),
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
    setIsDeletingApp(true);
    try {
      await apiClient.delete(`/api/v1/applications/${appId}`);
      toast({
        title: "Application deleted",
        description: "Application deleted successfully.",
      });
      setShowDeleteAppDialog(false);
      router.push("/catalog");
    } catch (err) {
      toast({
        title: "Error",
        description: err instanceof Error ? err.message : "Failed to delete application",
        variant: "destructive",
      });
    } finally {
      setIsDeletingApp(false);
    }
  };

  const handleCreateExperiment = () => {
    if (application) {
      openModal({ application });
    }
  };

  if (isLoading) {
    return (
      <div className="space-y-6">
        <Skeleton className="h-10 w-full" />
        <Skeleton className="h-96 w-full" />
      </div>
    );
  }

  if (error || !application) {
    return (
      <Card className="border-destructive">
        <CardContent className="py-8">
          <div className="flex flex-col items-center text-center">
            <AlertCircle className="h-12 w-12 text-destructive mb-4" />
            <h3 className="text-lg font-semibold">Failed to load application</h3>
            <p className="text-muted-foreground mt-1 max-w-md">
              {error instanceof Error ? error.message : "Application not found"}
            </p>
          </div>
        </CardContent>
      </Card>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          {backHref && (
            <Button variant="ghost" size="icon" asChild>
              <Link href={backHref}>
                <ArrowLeft className="h-5 w-5" />
              </Link>
            </Button>
          )}
          <div>
            <div className="flex items-center gap-2">
              <h1 className="text-3xl font-bold tracking-tight">{application.name}</h1>
              <Badge>Application</Badge>
              {application.version && (
                <Badge variant="outline">v{application.version}</Badge>
              )}
            </div>
            <p className="text-muted-foreground">{application.description || "No description"}</p>
          </div>
        </div>
        <div className="flex items-center gap-2">
          <Button onClick={handleCreateExperiment}>
            Create Experiment
          </Button>
          {isAdmin && (
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant="outline" size="icon">
                  <MoreVertical className="h-4 w-4" />
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end">
                <DropdownMenuItem
                  className="text-destructive focus:text-destructive"
                  onClick={() => setShowDeleteAppDialog(true)}
                >
                  <Trash2 className="h-4 w-4 mr-2" />
                  Delete Application
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          )}
        </div>
      </div>

      {/* Overview */}
      <Card>
        <CardHeader>
          <CardTitle>Application Details</CardTitle>
          <CardDescription>Application configuration and metadata</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid gap-4 md:grid-cols-2">
            <div>
              <p className="text-sm text-muted-foreground mb-1">Name</p>
              <p className="text-sm font-medium">{application.name}</p>
            </div>
            {application.version && (
              <div>
                <p className="text-sm text-muted-foreground mb-1">Version</p>
                <p className="text-sm">{application.version}</p>
              </div>
            )}
            <div>
              <p className="text-sm text-muted-foreground mb-1">Description</p>
              <p className="text-sm">{application.description || "No description"}</p>
            </div>
            <div>
              <p className="text-sm text-muted-foreground mb-1">Scope</p>
              <Badge variant={application.scope === "GATEWAY" ? "default" : "secondary"}>
                {application.scope}
              </Badge>
            </div>
          </div>

          {/* Fields: grouped collapsible table */}
          <div>
            <p className="text-sm text-muted-foreground mb-2">Fields</p>
            <div className="border rounded-lg overflow-hidden">
              <Table className="[&_th]:h-9 [&_th]:py-1.5 [&_th]:px-3 [&_td]:py-1.5 [&_td]:px-3">
                <TableHeader>
                  <TableRow>
                    <TableHead>Name</TableHead>
                    <TableHead>Type</TableHead>
                    <TableHead>Required</TableHead>
                    <TableHead>Description</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {[
                    { key: "inputs", label: "Inputs", icon: ArrowRightToLine, items: application.inputs, emptyMsg: "No inputs defined" },
                    { key: "outputs", label: "Outputs", icon: ArrowLeftFromLine, items: application.outputs, emptyMsg: "No outputs defined" },
                  ].map((group) => {
                    const isCollapsed = collapsedFieldGroups.has(group.key);
                    const Icon = group.icon;
                    return (
                      <Fragment key={group.key}>
                        <TableRow
                          className="bg-muted hover:bg-muted cursor-pointer"
                          onClick={() => toggleFieldGroup(group.key)}
                        >
                          <TableCell className="py-1.5 px-3" colSpan={4}>
                            <div className="flex items-center gap-2">
                              <ChevronRight className={cn(
                                "h-3.5 w-3.5 text-muted-foreground transition-transform",
                                !isCollapsed && "rotate-90"
                              )} />
                              <Icon className="h-4 w-4 text-muted-foreground" />
                              <span className="text-xs font-semibold uppercase tracking-wide">
                                {group.label}
                              </span>
                              <Badge variant="secondary" className="text-xs ml-1">
                                {group.items.length}
                              </Badge>
                            </div>
                          </TableCell>
                        </TableRow>
                        {!isCollapsed && (group.items.length > 0 ? (
                          group.items.map((field, idx) => (
                            <TableRow key={`${group.key}-${idx}`} className="hover:bg-muted/50">
                              <TableCell className="py-1.5 pr-3 pl-9 font-medium">
                                {field.name}
                              </TableCell>
                              <TableCell className="text-muted-foreground py-1.5">
                                <Badge variant="outline" className="text-xs">{field.type}</Badge>
                              </TableCell>
                              <TableCell className="py-1.5">
                                {field.required ? (
                                  <Badge className="text-xs bg-orange-500">Required</Badge>
                                ) : (
                                  <Badge variant="secondary" className="text-xs">Optional</Badge>
                                )}
                              </TableCell>
                              <TableCell className="text-muted-foreground text-sm py-1.5">
                                {field.description || "-"}
                              </TableCell>
                            </TableRow>
                          ))
                        ) : (
                          <TableRow>
                            <TableCell colSpan={4} className="py-4 pl-9 text-muted-foreground text-sm">
                              {group.emptyMsg}
                            </TableCell>
                          </TableRow>
                        ))}
                      </Fragment>
                    );
                  })}
                </TableBody>
              </Table>
            </div>
          </div>

          {/* Scripts */}
          {(application.installScript || application.runScript) && (
            <div className="space-y-3">
              {application.installScript && (
                <div>
                  <p className="text-sm font-medium mb-1">Install Script</p>
                  <pre className="text-xs bg-muted p-3 rounded-lg overflow-x-auto whitespace-pre-wrap font-mono">
                    {application.installScript}
                  </pre>
                </div>
              )}
              {application.runScript && (
                <div>
                  <p className="text-sm font-medium mb-1">Run Script</p>
                  <pre className="text-xs bg-muted p-3 rounded-lg overflow-x-auto whitespace-pre-wrap font-mono">
                    {application.runScript}
                  </pre>
                </div>
              )}
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
            <div className="text-center py-8 text-muted-foreground">
              No installations configured
            </div>
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

      {/* Delete Application Dialog */}
      <AlertDialog open={showDeleteAppDialog} onOpenChange={setShowDeleteAppDialog}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Delete Application</AlertDialogTitle>
            <AlertDialogDescription>
              Are you sure you want to delete &quot;{application.name}&quot;? This action cannot be undone.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel disabled={isDeletingApp}>Cancel</AlertDialogCancel>
            <AlertDialogAction
              onClick={handleDeleteApplication}
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
              disabled={isDeletingApp}
            >
              {isDeletingApp ? "Deleting..." : "Delete"}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
