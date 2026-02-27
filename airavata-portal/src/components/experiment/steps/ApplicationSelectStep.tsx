"use client";

import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { useQuery } from "@tanstack/react-query";
import { apiClient } from "@/lib/api";
import { useProjects } from "@/hooks";
import { useGateway } from "@/contexts/GatewayContext";
import { usePortalConfig } from "@/contexts/PortalConfigContext";
import { ApplicationSearchSelect } from "../ApplicationSearchSelect";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import type { Application } from "@/types";

interface Props {
  data: any;
  onUpdate: (data: any) => void;
  onNext: () => void;
}

export function ApplicationSelectStep({ data, onUpdate, onNext }: Props) {
  const { effectiveGatewayId } = useGateway();
  const { defaultGatewayId } = usePortalConfig();
  const gatewayId = effectiveGatewayId || defaultGatewayId;

  const { data: applications = [], isLoading } = useQuery({
    queryKey: ["applications", gatewayId],
    queryFn: () => apiClient.get<Application[]>(`/api/v1/applications?gatewayId=${gatewayId}`),
    enabled: !!gatewayId,
    staleTime: 30000,
  });

  const { data: projects } = useProjects();

  const handleSelectApplication = (app: Application) => {
    onUpdate({
      application: app,
      experimentName: `${app.name} Experiment`,
      inputValues: {},
    });
  };

  const handleNext = () => {
    if (!data.projectId) {
      alert("Please select a project. Experiments must be created within a project.");
      return;
    }
    if (!data.application) {
      alert("Please select an application");
      return;
    }
    if (!data.experimentName?.trim()) {
      alert("Please enter an experiment name");
      return;
    }
    onNext();
  };

  return (
    <div className="space-y-6">
      <div className="space-y-2">
        <Label>Application <span className="text-destructive">*</span></Label>
        <ApplicationSearchSelect
          applications={applications}
          selectedApplication={data.application}
          onSelect={handleSelectApplication}
          isLoading={isLoading}
          placeholder="Search and select an application..."
        />
      </div>

      <div className="space-y-2">
        <Label htmlFor="project">Project <span className="text-destructive">*</span></Label>
        <Select
          value={data.projectId || ""}
          onValueChange={(value) => onUpdate({ projectId: value })}
        >
          <SelectTrigger>
            <SelectValue placeholder="Select a project (required)" />
          </SelectTrigger>
          <SelectContent>
            {projects && projects.length > 0 ? (
              projects.map((project) => (
                <SelectItem key={project.projectID} value={project.projectID}>
                  {project.name}
                </SelectItem>
              ))
            ) : (
              <div className="p-2 text-sm text-muted-foreground">
                No projects available. Please create a project first.
              </div>
            )}
          </SelectContent>
        </Select>
        {(!projects || projects.length === 0) && (
          <p className="text-xs text-muted-foreground">
            You must create a project before creating experiments.
          </p>
        )}
      </div>

      <div className="space-y-2">
        <Label htmlFor="experiment-name">Experiment Name <span className="text-destructive">*</span></Label>
        <Input
          id="experiment-name"
          value={data.experimentName ?? ""}
          onChange={(e) => onUpdate({ experimentName: e.target.value })}
          placeholder="Enter experiment name"
        />
      </div>

      <div className="space-y-2">
        <Label htmlFor="description">Description</Label>
        <Input
          id="description"
          value={data.description ?? ""}
          onChange={(e) => onUpdate({ description: e.target.value })}
          placeholder="Enter experiment description (optional)"
        />
      </div>

      <div className="flex justify-end">
        <Button onClick={handleNext}>Next</Button>
      </div>
    </div>
  );
}
