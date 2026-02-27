"use client";

import { useCallback } from "react";
import ReactFlow, {
  Node,
  Edge,
  Background,
  Controls,
  MiniMap,
  useNodesState,
  useEdgesState,
} from "reactflow";
import "reactflow/dist/style.css";
import type { ProcessModel, TaskModel } from "@/types";
import { TaskTypes, TaskState } from "@/types";

interface WorkflowViewerProps {
  processes?: ProcessModel[];
}

function createNodesFromProcess(process: ProcessModel): { nodes: Node[]; edges: Edge[] } {
  const nodes: Node[] = [];
  const edges: Edge[] = [];

  // Add process node
  const processNode: Node = {
    id: process.processId,
    type: "default",
    position: { x: 250, y: 0 },
    data: {
      label: `Process: ${process.processId.substring(0, 8)}...`,
    },
    style: {
      background: "#3b82f6",
      color: "white",
      borderRadius: 8,
      padding: 10,
    },
  };
  nodes.push(processNode);

  // Add task nodes
  process.tasks?.forEach((task, idx) => {
    const taskNode: Node = {
      id: task.taskId,
      type: "default",
      position: { x: 250, y: 100 + idx * 80 },
      data: {
        label: getTaskLabel(task),
      },
      style: {
        background: getTaskColor(task),
        color: "white",
        borderRadius: 8,
        padding: 8,
        fontSize: 12,
      },
    };
    nodes.push(taskNode);

    // Connect to previous
    if (idx === 0) {
      edges.push({
        id: `${process.processId}-${task.taskId}`,
        source: process.processId,
        target: task.taskId,
        animated: true,
      });
    } else if (process.tasks && process.tasks[idx - 1]) {
      edges.push({
        id: `${process.tasks[idx - 1].taskId}-${task.taskId}`,
        source: process.tasks[idx - 1].taskId,
        target: task.taskId,
        animated: true,
      });
    }
  });

  return { nodes, edges };
}

function getTaskLabel(task: TaskModel): string {
  const typeLabels: Record<string, string> = {
    [TaskTypes.ENV_SETUP]: "Environment Setup",
    [TaskTypes.DATA_STAGING]: "Data Staging",
    [TaskTypes.JOB_SUBMISSION]: "Job Submission",
    [TaskTypes.ENV_CLEANUP]: "Cleanup",
    [TaskTypes.MONITORING]: "Monitoring",
    [TaskTypes.OUTPUT_FETCHING]: "Output Fetching",
  };
  const status = task.taskStatuses?.[0]?.state || "UNKNOWN";
  return `${typeLabels[task.taskType] || task.taskType}\n(${status})`;
}

function getTaskColor(task: TaskModel): string {
  const status = task.taskStatuses?.[0]?.state;
  switch (status) {
    case TaskState.COMPLETED:
      return "#22c55e";
    case TaskState.EXECUTING:
      return "#eab308";
    case TaskState.FAILED:
      return "#ef4444";
    case TaskState.CANCELED:
      return "#6b7280";
    default:
      return "#3b82f6";
  }
}

export function WorkflowViewer({ processes }: WorkflowViewerProps) {
  const allNodes: Node[] = [];
  const allEdges: Edge[] = [];

  // Build nodes from processes
  processes?.forEach((process, idx) => {
    const { nodes, edges } = createNodesFromProcess(process);
    // Offset for multiple processes
    nodes.forEach((node) => {
      node.position.x += idx * 400;
    });
    allNodes.push(...nodes);
    allEdges.push(...edges);
  });

  const [nodes, setNodes, onNodesChange] = useNodesState(allNodes);
  const [edges, setEdges, onEdgesChange] = useEdgesState(allEdges);

  if (!processes || processes.length === 0) {
    return (
      <div className="flex items-center justify-center h-64 bg-muted rounded-lg">
        <p className="text-muted-foreground">No workflow data available</p>
      </div>
    );
  }

  return (
    <div className="h-96 border rounded-lg overflow-hidden">
      <ReactFlow
        nodes={nodes}
        edges={edges}
        onNodesChange={onNodesChange}
        onEdgesChange={onEdgesChange}
        fitView
        attributionPosition="bottom-left"
      >
        <Background />
        <Controls />
        <MiniMap />
      </ReactFlow>
    </div>
  );
}
