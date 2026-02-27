"use client";

import { useParams, useRouter } from "next/navigation";
import { useSession } from "next-auth/react";
import { useQuery } from "@tanstack/react-query";
import { experimentsApi } from "@/lib/api/experiments";
import { Skeleton } from "@/components/ui/skeleton";
import { Button } from "@/components/ui/button";
import Link from "next/link";
import { ArrowLeft, Pencil, RefreshCw, Calendar, User, Server, FolderKanban, Play, StopCircle, Copy } from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Badge } from "@/components/ui/badge";
import { ExperimentStatus } from "@/components/experiment";
import { ExecutionTimeline } from "@/components/experiment/ProcessTimeline";
import { useLaunchExperiment, useCancelExperiment, useCloneExperiment } from "@/hooks";
import { formatDate, isTerminalState } from "@/lib/utils";
import { ExperimentState } from "@/types";
import { useQueryClient } from "@tanstack/react-query";
import { toast } from "@/hooks/useToast";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { NoPermissions } from "@/components/errors/NoPermissions";
import { NotFound } from "@/components/errors/NotFound";
import type { ExperimentModel } from "@/types";
import { useGateway } from "@/contexts/GatewayContext";
import { getExperimentPermalink } from "@/lib/permalink";

export default function ExperimentPermalinkPage() {
  const params = useParams();
  const router = useRouter();
  const { data: session } = useSession();
  const { selectedGatewayId, getGatewayName, accessibleGateways } = useGateway();
  const experimentId = params.experimentId as string;
  const dashboardHref =
    selectedGatewayId && accessibleGateways?.length
      ? `/${getGatewayName(selectedGatewayId)}`
      : "/default";
  const queryClient = useQueryClient();

  const { data: experiment, isLoading: expLoading, refetch, error } = useQuery<ExperimentModel | null>({
    queryKey: ["experiment", experimentId],
    queryFn: async () => {
      try {
        return await experimentsApi.get(experimentId);
      } catch (err: any) {
        if (err?.response?.status === 404) {
          return null;
        }
        if (err?.response?.status === 403 || err?.response?.status === 401) {
          throw new Error("NO_PERMISSIONS");
        }
        throw err;
      }
    },
    enabled: !!experimentId && !!session?.accessToken,
    // Auto-poll every 5 seconds while experiment is in a non-terminal state
    refetchInterval: (query) => {
      const exp = query.state.data;
      if (!exp) return false;
      const currentState = exp.experimentStatus?.[0]?.state;
      return currentState && !isTerminalState(currentState) ? 5000 : false;
    },
  });

  // Processes also auto-refresh while experiment is running
  const isExpRunning = experiment?.experimentStatus?.[0]?.state
    ? !isTerminalState(experiment.experimentStatus[0].state)
    : false;
  const { data: processes, isLoading: processLoading } = useQuery({
    queryKey: ["experiment-processes", experimentId],
    queryFn: () => experimentsApi.getProcesses(experimentId),
    enabled: !!experimentId && !!session?.accessToken,
    refetchInterval: isExpRunning ? 5000 : false,
  });
  const launchExperiment = useLaunchExperiment();
  const cancelExperiment = useCancelExperiment();
  const cloneExperiment = useCloneExperiment();

  if (expLoading) {
    return (
      <div className="space-y-6 p-6">
        <Skeleton className="h-10 w-full" />
        <Skeleton className="h-96 w-full" />
      </div>
    );
  }

  if (error) {
    if (error.message === "NO_PERMISSIONS") {
      return <NoPermissions resourceType="experiment" resourceId={experimentId} />;
    }
    return <NotFound resourceType="experiment" resourceId={experimentId} />;
  }

  if (!experiment) {
    return <NotFound resourceType="experiment" resourceId={experimentId} />;
  }

  const status = experiment?.experimentStatus?.[0]?.state || "UNKNOWN";
  const canEdit = status === ExperimentState.CREATED;
  const canLaunch = status === ExperimentState.CREATED;
  const canCancel = status === ExperimentState.EXECUTING || status === ExperimentState.SCHEDULED || status === ExperimentState.LAUNCHED;
  const isRunning = isExpRunning;

  const handleLaunch = async () => {
    try {
      await launchExperiment.mutateAsync(experimentId);
      toast({
        title: "Experiment launched",
        description: "Your experiment has been submitted for execution.",
      });
    } catch (error) {
      toast({
        title: "Launch failed",
        description: error instanceof Error ? error.message : "Failed to launch experiment",
        variant: "destructive",
      });
    }
  };

  const handleCancel = async () => {
    if (!confirm("Are you sure you want to cancel this experiment?")) {
      return;
    }
    try {
      await cancelExperiment.mutateAsync(experimentId);
      toast({
        title: "Experiment cancelled",
        description: "Your experiment has been cancelled.",
      });
    } catch (error) {
      toast({
        title: "Cancel failed",
        description: error instanceof Error ? error.message : "Failed to cancel experiment",
        variant: "destructive",
      });
    }
  };

  const handleClone = async () => {
    try {
      const result = await cloneExperiment.mutateAsync(experimentId);
      toast({
        title: "Experiment cloned",
        description: "A copy of this experiment has been created.",
      });
      router.push(getExperimentPermalink(result.experimentId));
    } catch (error) {
      toast({
        title: "Clone failed",
        description: error instanceof Error ? error.message : "Failed to clone experiment",
        variant: "destructive",
      });
    }
  };

  return (
    <div className="space-y-6 p-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Button variant="ghost" size="icon" asChild>
            <Link href={dashboardHref}>
              <ArrowLeft className="h-5 w-5" />
            </Link>
          </Button>
          <div>
            <div className="flex items-center gap-3">
              <h1 className="text-3xl font-bold tracking-tight">{experiment.experimentName}</h1>
              <ExperimentStatus status={status} />
            </div>
            <p className="text-muted-foreground">{experiment.description || "No description"}</p>
          </div>
        </div>
        <div className="flex items-center gap-2">
          {isRunning && (
            <div className="flex items-center gap-2">
              <span className="flex items-center gap-1.5 text-sm text-muted-foreground">
                <span className="relative flex h-2 w-2">
                  <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-green-400 opacity-75"></span>
                  <span className="relative inline-flex rounded-full h-2 w-2 bg-green-500"></span>
                </span>
                Auto-refreshing
              </span>
              <Button variant="outline" size="sm" onClick={() => refetch()}>
                <RefreshCw className="mr-2 h-4 w-4" />
                Refresh
              </Button>
            </div>
          )}
          {canLaunch && (
            <Button onClick={handleLaunch} disabled={launchExperiment.isPending}>
              <Play className="mr-2 h-4 w-4" />
              Launch
            </Button>
          )}
          {canCancel && (
            <Button variant="destructive" onClick={handleCancel} disabled={cancelExperiment.isPending}>
              <StopCircle className="mr-2 h-4 w-4" />
              Cancel
            </Button>
          )}
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="outline">Actions</Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent>
              {canEdit && (
                <DropdownMenuItem>
                  <Pencil className="mr-2 h-4 w-4" />
                  Edit
                </DropdownMenuItem>
              )}
              <DropdownMenuItem onClick={handleClone} disabled={cloneExperiment.isPending}>
                <Copy className="mr-2 h-4 w-4" />
                Clone
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </div>

      <div className="grid gap-4 md:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center gap-2 pb-2">
            <User className="h-4 w-4 text-muted-foreground" />
            <CardTitle className="text-sm font-medium">User</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="font-semibold">{experiment.userName}</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center gap-2 pb-2">
            <Calendar className="h-4 w-4 text-muted-foreground" />
            <CardTitle className="text-sm font-medium">Created</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="font-semibold">{formatDate(experiment.creationTime)}</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center gap-2 pb-2">
            <FolderKanban className="h-4 w-4 text-muted-foreground" />
            <CardTitle className="text-sm font-medium">Project</CardTitle>
          </CardHeader>
          <CardContent>
            {experiment.projectId ? (
              <Link href={`/projects/${experiment.projectId}`} className="font-semibold text-primary hover:underline">
                View Project
              </Link>
            ) : (
              <p className="text-muted-foreground">None</p>
            )}
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center gap-2 pb-2">
            <Server className="h-4 w-4 text-muted-foreground" />
            <CardTitle className="text-sm font-medium">Compute Resource</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="font-semibold truncate">
              {experiment.userConfigurationData?.computationalResourceScheduling?.resourceHostId || "Not specified"}
            </p>
          </CardContent>
        </Card>
      </div>

      {/* Execution Timeline (Gantt chart) */}
      <Card>
        <CardHeader className="pb-3">
          <CardTitle className="text-sm font-medium">Execution Timeline</CardTitle>
        </CardHeader>
        <CardContent>
          <ExecutionTimeline
            experimentStatuses={experiment.experimentStatus || []}
            processes={processes || []}
            isLoading={processLoading}
          />
        </CardContent>
      </Card>

      <Tabs defaultValue="inputs">
        <TabsList>
          <TabsTrigger value="inputs">Inputs</TabsTrigger>
          <TabsTrigger value="outputs">Outputs</TabsTrigger>
          <TabsTrigger value="errors">Errors</TabsTrigger>
        </TabsList>

        <TabsContent value="inputs" className="mt-4">
          <Card>
            <CardHeader>
              <CardTitle>Experiment Inputs</CardTitle>
              <CardDescription>Input parameters for this experiment</CardDescription>
            </CardHeader>
            <CardContent>
              {experiment.experimentInputs?.length === 0 ? (
                <p className="text-muted-foreground text-center py-8">No inputs defined</p>
              ) : (
                <div className="space-y-4">
                  {experiment.experimentInputs?.map((input, idx) => (
                    <div key={idx} className="border rounded-lg p-4">
                      <div className="flex items-start justify-between">
                        <div>
                          <p className="font-medium">{input.name}</p>
                          <p className="text-sm text-muted-foreground">{input.userFriendlyDescription}</p>
                        </div>
                        <Badge variant="outline">{input.type}</Badge>
                      </div>
                      <div className="mt-2 p-2 bg-muted rounded text-sm font-mono">
                        {input.value || <span className="text-muted-foreground">No value</span>}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="outputs" className="mt-4">
          <Card>
            <CardHeader>
              <CardTitle>Experiment Outputs</CardTitle>
              <CardDescription>Output files and results</CardDescription>
            </CardHeader>
            <CardContent>
              {experiment.experimentOutputs?.length === 0 ? (
                <p className="text-muted-foreground text-center py-8">No outputs yet</p>
              ) : (
                <div className="space-y-4">
                  {experiment.experimentOutputs?.map((output, idx) => (
                    <div key={idx} className="border rounded-lg p-4">
                      <div className="flex items-start justify-between">
                        <div>
                          <p className="font-medium">{output.name}</p>
                        </div>
                        <Badge variant="outline">{output.type}</Badge>
                      </div>
                      {output.value && (
                        <div className="mt-2 p-2 bg-muted rounded text-sm font-mono break-all">
                          {output.value}
                        </div>
                      )}
                    </div>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="errors" className="mt-4">
          <Card>
            <CardHeader>
              <CardTitle>Errors</CardTitle>
              <CardDescription>Any errors encountered during execution</CardDescription>
            </CardHeader>
            <CardContent>
              {experiment.errors?.length === 0 ? (
                <p className="text-muted-foreground text-center py-8">No errors</p>
              ) : (
                <div className="space-y-4">
                  {experiment.errors?.map((error, idx) => (
                    <div key={idx} className="border border-red-200 bg-red-50 rounded-lg p-4">
                      <p className="font-medium text-red-800">{error.userFriendlyMessage || "Error"}</p>
                      {error.actualErrorMessage && (
                        <pre className="mt-2 text-sm text-red-700 whitespace-pre-wrap">
                          {error.actualErrorMessage}
                        </pre>
                      )}
                      <p className="text-xs text-red-600 mt-2">
                        {formatDate(error.creationTime)}
                      </p>
                    </div>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
}

