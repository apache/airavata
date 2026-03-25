"use client";

import { useState, useCallback, useMemo } from "react";
import { useSession } from "next-auth/react";
import { useMutation, useQuery } from "@tanstack/react-query";
import ReactFlow, {
  addEdge,
  Background,
  Controls,
  useNodesState,
  useEdgesState,
  Handle,
  Position,
  type Node,
  type Edge,
  type Connection,
  type NodeTypes,
} from "reactflow";
import "reactflow/dist/style.css";
import { Plus, Save, X } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { applicationsApi } from "@/lib/api/applications";
import { workflowsApi } from "@/lib/api/workflows";
import type { Workflow, WorkflowStep, WorkflowEdge } from "@/types";

interface WorkflowFormProps {
  workflow?: Workflow | null;
  gatewayId: string;
  projectId: string;
  onSave: () => void;
  onCancel: () => void;
}

// Custom node for workflow steps
function StepNode({ data }: { data: { label: string; applicationId: string } }) {
  return (
    <div className="px-4 py-2 rounded-lg border-2 border-blue-300 bg-white shadow-sm min-w-[150px]">
      <Handle type="target" position={Position.Top} className="w-3 h-3" />
      <div className="text-sm font-medium">{data.label}</div>
      <div className="text-xs text-muted-foreground truncate">
        {data.applicationId}
      </div>
      <Handle type="source" position={Position.Bottom} className="w-3 h-3" />
    </div>
  );
}

const nodeTypes: NodeTypes = { step: StepNode };

export function WorkflowForm({
  workflow,
  gatewayId,
  projectId,
  onSave,
  onCancel,
}: WorkflowFormProps) {
  const { data: session } = useSession();
  const [name, setName] = useState(workflow?.workflowName || "");
  const [description, setDescription] = useState(workflow?.description || "");

  // Convert existing workflow steps/edges to ReactFlow format
  const initialNodes: Node[] = useMemo(
    () =>
      (workflow?.steps || []).map((step) => ({
        id: step.stepId,
        type: "step",
        position: { x: step.x, y: step.y },
        data: { label: step.label, applicationId: step.applicationId },
      })),
    [workflow]
  );

  const initialEdges: Edge[] = useMemo(
    () =>
      (workflow?.edges || []).map((edge) => ({
        id: `e-${edge.fromStepId}-${edge.toStepId}`,
        source: edge.fromStepId,
        target: edge.toStepId,
        animated: true,
        style: { stroke: "#3b82f6" },
      })),
    [workflow]
  );

  const [nodes, setNodes, onNodesChange] = useNodesState(initialNodes);
  const [edges, setEdges, onEdgesChange] = useEdgesState(initialEdges);

  // Application interfaces list for adding steps
  const { data: applications } = useQuery({
    queryKey: ["application-interfaces", gatewayId],
    queryFn: () => applicationsApi.list(gatewayId),
    enabled: !!gatewayId,
  });

  const [selectedAppId, setSelectedAppId] = useState("");
  const [stepLabel, setStepLabel] = useState("");

  const onConnect = useCallback(
    (connection: Connection) =>
      setEdges((eds) =>
        addEdge(
          { ...connection, animated: true, style: { stroke: "#3b82f6" } },
          eds
        )
      ),
    [setEdges]
  );

  const addStep = useCallback(() => {
    if (!selectedAppId || !stepLabel) return;
    const newNode: Node = {
      id: `step-${Date.now()}`,
      type: "step",
      position: { x: 250, y: nodes.length * 120 + 50 },
      data: { label: stepLabel, applicationId: selectedAppId },
    };
    setNodes((nds) => [...nds, newNode]);
    setStepLabel("");
    setSelectedAppId("");
  }, [selectedAppId, stepLabel, nodes.length, setNodes]);

  const removeSelectedNodes = useCallback(() => {
    const selectedNodeIds = new Set(
      nodes.filter((n) => n.selected).map((n) => n.id)
    );
    setNodes((nds) => nds.filter((n) => !n.selected));
    setEdges((eds) =>
      eds.filter(
        (e) => !selectedNodeIds.has(e.source) && !selectedNodeIds.has(e.target)
      )
    );
  }, [nodes, setNodes, setEdges]);

  const saveMutation = useMutation({
    mutationFn: async () => {
      const steps: WorkflowStep[] = nodes.map((n) => ({
        stepId: n.id,
        applicationId: n.data.applicationId,
        label: n.data.label,
        x: Math.round(n.position.x),
        y: Math.round(n.position.y),
      }));
      const workflowEdges: WorkflowEdge[] = edges.map((e) => ({
        fromStepId: e.source,
        toStepId: e.target,
        mappings: [],
      }));

      const payload: Partial<Workflow> = {
        workflowName: name,
        description,
        steps,
        edges: workflowEdges,
        projectId,
        gatewayId,
        userName: session?.user?.name || "",
      };

      if (workflow?.workflowId) {
        return workflowsApi.update(workflow.workflowId, payload);
      } else {
        return workflowsApi.create(payload);
      }
    },
    onSuccess: () => onSave(),
  });

  return (
    <div className="space-y-4">
      <div className="grid grid-cols-2 gap-4">
        <div>
          <Label htmlFor="wf-name">Name</Label>
          <Input
            id="wf-name"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="My Workflow"
          />
        </div>
        <div>
          <Label htmlFor="wf-desc">Description</Label>
          <Input
            id="wf-desc"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="Optional description"
          />
        </div>
      </div>

      {/* Add step controls */}
      <div className="flex items-end gap-2 p-3 bg-muted/50 rounded-lg">
        <div className="flex-1">
          <Label className="text-xs">Application</Label>
          <Select value={selectedAppId} onValueChange={setSelectedAppId}>
            <SelectTrigger className="h-8">
              <SelectValue placeholder="Select app..." />
            </SelectTrigger>
            <SelectContent>
              {applications?.map((app) => (
                <SelectItem
                  key={app.applicationId}
                  value={app.applicationId}
                >
                  {app.name}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
        <div className="flex-1">
          <Label className="text-xs">Step Label</Label>
          <Input
            className="h-8"
            value={stepLabel}
            onChange={(e) => setStepLabel(e.target.value)}
            placeholder="Step name"
          />
        </div>
        <Button
          size="sm"
          variant="outline"
          onClick={addStep}
          disabled={!selectedAppId || !stepLabel}
        >
          <Plus className="h-4 w-4 mr-1" /> Add Step
        </Button>
      </div>

      {/* ReactFlow Canvas */}
      <div className="h-[400px] border rounded-lg overflow-hidden">
        <ReactFlow
          nodes={nodes}
          edges={edges}
          onNodesChange={onNodesChange}
          onEdgesChange={onEdgesChange}
          onConnect={onConnect}
          nodeTypes={nodeTypes}
          fitView
          deleteKeyCode="Delete"
        >
          <Background />
          <Controls />
        </ReactFlow>
      </div>

      {/* Actions */}
      <div className="flex justify-between">
        <Button variant="outline" size="sm" onClick={removeSelectedNodes}>
          <X className="h-4 w-4 mr-1" /> Remove Selected
        </Button>
        <div className="flex gap-2">
          <Button variant="outline" size="sm" onClick={onCancel}>
            Cancel
          </Button>
          <Button
            size="sm"
            onClick={() => saveMutation.mutate()}
            disabled={!name || saveMutation.isPending}
          >
            <Save className="h-4 w-4 mr-1" />
            {saveMutation.isPending ? "Saving..." : "Save"}
          </Button>
        </div>
      </div>
    </div>
  );
}
