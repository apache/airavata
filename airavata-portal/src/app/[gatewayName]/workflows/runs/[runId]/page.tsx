"use client";

import { useParams } from "next/navigation";
import { useQuery } from "@tanstack/react-query";
import { ArrowLeft, CheckCircle, XCircle, Loader2, Clock } from "lucide-react";
import Link from "next/link";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { workflowsApi } from "@/lib/api/workflows";

export default function WorkflowRunPage() {
  const params = useParams();
  const gatewayName = params?.gatewayName as string;
  const runId = params?.runId as string;

  const { data: run, isLoading } = useQuery({
    queryKey: ["workflow-run", runId],
    queryFn: () => workflowsApi.getRun(runId),
    enabled: !!runId,
    refetchInterval: 5000, // Poll every 5s while running
  });

  const getStatusIcon = (status: string) => {
    switch (status) {
      case "COMPLETED":
        return <CheckCircle className="h-4 w-4 text-green-500" />;
      case "FAILED":
        return <XCircle className="h-4 w-4 text-red-500" />;
      case "RUNNING":
        return <Loader2 className="h-4 w-4 text-blue-500 animate-spin" />;
      default:
        return <Clock className="h-4 w-4 text-gray-400" />;
    }
  };

  const getStatusBadge = (status: string) => {
    const colors: Record<string, string> = {
      COMPLETED: "bg-green-100 text-green-800",
      FAILED: "bg-red-100 text-red-800",
      RUNNING: "bg-blue-100 text-blue-800",
      CREATED: "bg-gray-100 text-gray-800",
      PENDING: "bg-gray-100 text-gray-800",
    };
    return (
      <Badge className={colors[status] || "bg-gray-100 text-gray-800"}>
        {status}
      </Badge>
    );
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-12">
        <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
      </div>
    );
  }

  if (!run) {
    return (
      <div className="text-center py-12 text-muted-foreground">
        Run not found
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center gap-3">
        <Button variant="ghost" size="icon" asChild>
          <Link href={`/${gatewayName}/workflows`}>
            <ArrowLeft className="h-4 w-4" />
          </Link>
        </Button>
        <div>
          <h1 className="text-lg font-semibold">Workflow Run</h1>
          <p className="text-sm text-muted-foreground font-mono">{run.runId}</p>
        </div>
        <div className="ml-auto">{getStatusBadge(run.status)}</div>
      </div>

      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Step</TableHead>
            <TableHead>Status</TableHead>
            <TableHead>Experiment</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {Object.entries(run.stepStates || {}).map(([stepId, state]) => (
            <TableRow key={stepId}>
              <TableCell className="font-medium text-sm">{stepId}</TableCell>
              <TableCell>
                <div className="flex items-center gap-2">
                  {getStatusIcon(state.status)}
                  <span className="text-sm">{state.status}</span>
                </div>
              </TableCell>
              <TableCell>
                {state.experimentId ? (
                  <Link
                    href={`/${gatewayName}/experiments/${state.experimentId}`}
                    className="text-sm text-blue-600 hover:underline font-mono"
                  >
                    {state.experimentId}
                  </Link>
                ) : (
                  <span className="text-sm text-muted-foreground">-</span>
                )}
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  );
}
