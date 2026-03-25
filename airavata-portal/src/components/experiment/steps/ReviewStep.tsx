"use client";

import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Label } from "@/components/ui/label";
import { Badge } from "@/components/ui/badge";
import { Separator } from "@/components/ui/separator";
import type { Application, ResourceBinding, Resource, AppField } from "@/types";

interface Props {
  data: any;
  onBack: () => void;
  onSubmit: (launchImmediately: boolean) => Promise<void>;
  isSubmitting: boolean;
}

export function ReviewStep({ data, onBack, onSubmit, isSubmitting }: Props) {
  const application: Application | undefined = data.application;
  const binding: ResourceBinding | undefined = data.selectedBinding;
  const resource: Resource | undefined = data.selectedResource;
  const inputValues: Record<string, string> = data.inputValues ?? {};
  const scheduling = data.scheduling ?? {};
  const inputs: AppField[] = application?.inputs ?? [];

  return (
    <div className="space-y-6">
      <Card className="p-6 space-y-4">
        {/* Experiment basics */}
        <div>
          <h3 className="text-lg font-semibold mb-4">Experiment Details</h3>
          <div className="grid gap-3">
            <div>
              <Label className="text-muted-foreground">Experiment Name</Label>
              <p className="font-medium">{data.experimentName}</p>
            </div>
            {data.description && (
              <div>
                <Label className="text-muted-foreground">Description</Label>
                <p className="font-medium">{data.description}</p>
              </div>
            )}
            {data.projectId && (
              <div>
                <Label className="text-muted-foreground">Project</Label>
                <p className="font-medium">{data.projectId}</p>
              </div>
            )}
          </div>
        </div>

        <Separator />

        {/* Application */}
        <div>
          <h3 className="text-lg font-semibold mb-4">Application</h3>
          <div className="grid gap-3">
            <div>
              <Label className="text-muted-foreground">Name</Label>
              <p className="font-medium">
                {application?.name}
                {application?.version && (
                  <span className="ml-2 text-sm font-normal text-muted-foreground">
                    v{application.version}
                  </span>
                )}
              </p>
            </div>
            {application?.description && (
              <div>
                <Label className="text-muted-foreground">Description</Label>
                <p className="text-sm text-muted-foreground">{application.description}</p>
              </div>
            )}
          </div>
        </div>

        <Separator />

        {/* Binding / Resource */}
        <div>
          <h3 className="text-lg font-semibold mb-4">Compute Resource</h3>
          <div className="grid gap-3">
            {resource && (
              <div>
                <Label className="text-muted-foreground">Resource</Label>
                <p className="font-medium">
                  {resource.name}
                  {resource.capabilities?.compute && (
                    <Badge variant="outline" className="ml-2 text-xs">
                      {resource.capabilities.compute.type}
                    </Badge>
                  )}
                </p>
                {resource.hostName && (
                  <p className="text-sm text-muted-foreground">{resource.hostName}</p>
                )}
              </div>
            )}
            {binding && (
              <div>
                <Label className="text-muted-foreground">Login Username</Label>
                <p className="font-mono text-sm">{binding.loginUsername}</p>
              </div>
            )}
          </div>
        </div>

        <Separator />

        {/* Inputs */}
        <div>
          <h3 className="text-lg font-semibold mb-4">Inputs</h3>
          {inputs.length === 0 ? (
            <p className="text-muted-foreground text-sm">No inputs required</p>
          ) : (
            <div className="grid gap-3">
              {inputs.map((field) => (
                <div key={field.name}>
                  <Label className="text-muted-foreground">{field.name}</Label>
                  <p className="font-medium break-all">
                    {inputValues[field.name] || field.defaultValue || (
                      <span className="text-muted-foreground">(not set)</span>
                    )}
                  </p>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Scheduling (only for SLURM) */}
        {resource?.capabilities?.compute?.type === "SLURM" && (
          <>
            <Separator />
            <div>
              <h3 className="text-lg font-semibold mb-4">Scheduling</h3>
              <div className="grid gap-3">
                {scheduling.queueName && (
                  <div>
                    <Label className="text-muted-foreground">Queue</Label>
                    <p className="font-medium">{scheduling.queueName}</p>
                  </div>
                )}
                <div className="grid grid-cols-2 gap-3">
                  {scheduling.nodeCount && (
                    <div>
                      <Label className="text-muted-foreground">Nodes</Label>
                      <p className="font-medium">{scheduling.nodeCount}</p>
                    </div>
                  )}
                  {scheduling.cpuCount && (
                    <div>
                      <Label className="text-muted-foreground">CPUs</Label>
                      <p className="font-medium">{scheduling.cpuCount}</p>
                    </div>
                  )}
                  {scheduling.walltime && (
                    <div>
                      <Label className="text-muted-foreground">Wall Time</Label>
                      <p className="font-medium">{scheduling.walltime} min</p>
                    </div>
                  )}
                  {scheduling.allocationProject && (
                    <div>
                      <Label className="text-muted-foreground">Allocation Project</Label>
                      <p className="font-medium">{scheduling.allocationProject}</p>
                    </div>
                  )}
                </div>
              </div>
            </div>
          </>
        )}
      </Card>

      <div className="flex justify-between">
        <Button variant="outline" onClick={onBack} disabled={isSubmitting}>
          Back
        </Button>
        <div className="space-x-2">
          <Button
            variant="outline"
            onClick={() => onSubmit(false)}
            disabled={isSubmitting}
          >
            Save Draft
          </Button>
          <Button onClick={() => onSubmit(true)} disabled={isSubmitting}>
            {isSubmitting ? "Creating..." : "Create & Launch"}
          </Button>
        </div>
      </div>
    </div>
  );
}
