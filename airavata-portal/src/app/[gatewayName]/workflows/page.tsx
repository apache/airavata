"use client";

import { useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { useSession } from "next-auth/react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Plus, Play, Pencil, Trash2, GitBranch, Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
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
import { Badge } from "@/components/ui/badge";
import { workflowsApi } from "@/lib/api/workflows";
import { useProjects } from "@/hooks";
import { useGateway } from "@/contexts/GatewayContext";
import type { Workflow } from "@/types";
import { WorkflowForm } from "@/components/workflows/WorkflowForm";

export default function WorkflowsPage() {
  const params = useParams();
  const router = useRouter();
  const gatewayName = params?.gatewayName as string;
  const { data: session } = useSession();
  const queryClient = useQueryClient();
  const { effectiveGatewayId } = useGateway();

  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [editingWorkflow, setEditingWorkflow] = useState<Workflow | null>(null);
  const [deletingWorkflow, setDeletingWorkflow] = useState<Workflow | null>(null);

  const { data: projects } = useProjects();
  const defaultProjectId = projects?.[0]?.projectID;

  const { data: workflows, isLoading } = useQuery({
    queryKey: ["workflows", effectiveGatewayId, defaultProjectId],
    queryFn: () => workflowsApi.list(defaultProjectId!, effectiveGatewayId!),
    enabled: !!defaultProjectId && !!effectiveGatewayId,
  });

  const deleteMutation = useMutation({
    mutationFn: (workflowId: string) => workflowsApi.delete(workflowId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["workflows"] });
      setDeletingWorkflow(null);
    },
  });

  const runMutation = useMutation({
    mutationFn: (workflowId: string) =>
      workflowsApi.createRun(workflowId, session?.user?.name || ""),
    onSuccess: (run) => {
      queryClient.invalidateQueries({ queryKey: ["workflows"] });
      router.push(`/${gatewayName}/workflows/runs/${run.runId}`);
    },
  });

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-lg font-semibold">Workflows</h1>
          <p className="text-sm text-muted-foreground">
            Create and manage experiment workflows
          </p>
        </div>
        <Button onClick={() => setIsCreateOpen(true)} size="sm">
          <Plus className="h-4 w-4 mr-1" /> New Workflow
        </Button>
      </div>

      {isLoading ? (
        <div className="flex items-center justify-center py-12">
          <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
        </div>
      ) : !workflows?.length ? (
        <div className="text-center py-12 text-muted-foreground">
          <GitBranch className="h-12 w-12 mx-auto mb-3 opacity-30" />
          <p>No workflows yet</p>
          <p className="text-sm">
            Create a workflow to define a sequence of experiments
          </p>
        </div>
      ) : (
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Name</TableHead>
              <TableHead>Steps</TableHead>
              <TableHead>Created</TableHead>
              <TableHead className="w-[140px]">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {workflows.map((wf) => (
              <TableRow key={wf.workflowId}>
                <TableCell>
                  <div className="font-medium text-sm">{wf.workflowName}</div>
                  {wf.description && (
                    <div className="text-xs text-muted-foreground">
                      {wf.description}
                    </div>
                  )}
                </TableCell>
                <TableCell>
                  <Badge variant="secondary">
                    {wf.steps?.length || 0} steps
                  </Badge>
                </TableCell>
                <TableCell className="text-sm text-muted-foreground">
                  {wf.creationTime
                    ? new Date(wf.creationTime).toLocaleDateString()
                    : "-"}
                </TableCell>
                <TableCell>
                  <div className="flex items-center gap-1">
                    <Button
                      variant="ghost"
                      size="icon"
                      className="h-7 w-7"
                      onClick={() => runMutation.mutate(wf.workflowId)}
                      disabled={runMutation.isPending}
                    >
                      <Play className="h-4 w-4" />
                    </Button>
                    <Button
                      variant="ghost"
                      size="icon"
                      className="h-7 w-7"
                      onClick={() => setEditingWorkflow(wf)}
                    >
                      <Pencil className="h-4 w-4" />
                    </Button>
                    <Button
                      variant="ghost"
                      size="icon"
                      className="h-7 w-7 text-destructive"
                      onClick={() => setDeletingWorkflow(wf)}
                    >
                      <Trash2 className="h-4 w-4" />
                    </Button>
                  </div>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      )}

      {/* Create/Edit Dialog */}
      <Dialog
        open={isCreateOpen || !!editingWorkflow}
        onOpenChange={(open) => {
          if (!open) {
            setIsCreateOpen(false);
            setEditingWorkflow(null);
          }
        }}
      >
        <DialogContent className="max-w-4xl max-h-[85vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>
              {editingWorkflow ? "Edit Workflow" : "New Workflow"}
            </DialogTitle>
            <DialogDescription>
              {editingWorkflow
                ? "Modify the workflow steps and connections"
                : "Define a new experiment workflow"}
            </DialogDescription>
          </DialogHeader>
          <WorkflowForm
            workflow={editingWorkflow}
            gatewayId={effectiveGatewayId || gatewayName}
            projectId={defaultProjectId || ""}
            onSave={() => {
              queryClient.invalidateQueries({ queryKey: ["workflows"] });
              setIsCreateOpen(false);
              setEditingWorkflow(null);
            }}
            onCancel={() => {
              setIsCreateOpen(false);
              setEditingWorkflow(null);
            }}
          />
        </DialogContent>
      </Dialog>

      {/* Delete Confirmation */}
      <AlertDialog
        open={!!deletingWorkflow}
        onOpenChange={(open) => {
          if (!open) setDeletingWorkflow(null);
        }}
      >
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Delete Workflow</AlertDialogTitle>
            <AlertDialogDescription>
              Are you sure you want to delete &quot;
              {deletingWorkflow?.workflowName}&quot;? This cannot be undone.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction
              onClick={() =>
                deletingWorkflow &&
                deleteMutation.mutate(deletingWorkflow.workflowId)
              }
              className="bg-destructive text-destructive-foreground"
            >
              Delete
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
