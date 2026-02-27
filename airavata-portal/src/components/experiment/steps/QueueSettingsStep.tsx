"use client";

import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import type { Resource } from "@/types";

interface Props {
  data: any;
  onUpdate: (data: any) => void;
  onNext: () => void;
  onBack: () => void;
}

export function QueueSettingsStep({ data, onUpdate, onNext, onBack }: Props) {
  const resource: Resource | undefined = data.selectedResource;
  const batchQueues = resource?.capabilities?.compute?.batchQueues ?? [];
  const isSLURM = resource?.capabilities?.compute?.type === "SLURM";

  const scheduling = data.scheduling ?? {};

  const updateScheduling = (field: string, value: unknown) => {
    onUpdate({
      scheduling: {
        ...scheduling,
        [field]: value,
      },
    });
  };

  const handleNext = () => {
    if (isSLURM && !scheduling.queueName) {
      alert("Please select a queue");
      return;
    }
    onNext();
  };

  return (
    <div className="space-y-6">
      {isSLURM ? (
        <>
          <div className="space-y-2">
            <Label>Queue / Partition <span className="text-destructive">*</span></Label>
            <Select
              value={scheduling.queueName || ""}
              onValueChange={(value) => updateScheduling("queueName", value)}
            >
              <SelectTrigger>
                <SelectValue placeholder="Select a queue" />
              </SelectTrigger>
              <SelectContent>
                {batchQueues.length > 0 ? (
                  batchQueues.map((queue) => (
                    <SelectItem key={queue.queueName} value={queue.queueName}>
                      {queue.queueName}
                      {queue.maxNodes ? ` (${queue.maxNodes} max nodes)` : ""}
                    </SelectItem>
                  ))
                ) : (
                  <SelectItem value="" disabled>
                    No queues configured for this resource
                  </SelectItem>
                )}
              </SelectContent>
            </Select>
          </div>

          <div className="grid gap-4 md:grid-cols-2">
            <div className="space-y-2">
              <Label>Node Count</Label>
              <Input
                type="number"
                min="1"
                value={scheduling.nodeCount ?? 1}
                onChange={(e) => updateScheduling("nodeCount", parseInt(e.target.value) || 1)}
                placeholder="Number of nodes"
              />
            </div>

            <div className="space-y-2">
              <Label>CPU Count</Label>
              <Input
                type="number"
                min="1"
                value={scheduling.cpuCount ?? 1}
                onChange={(e) => updateScheduling("cpuCount", parseInt(e.target.value) || 1)}
                placeholder="Total CPUs"
              />
            </div>

            <div className="space-y-2">
              <Label>Wall Time (minutes)</Label>
              <Input
                type="number"
                min="1"
                value={scheduling.walltime ?? 30}
                onChange={(e) => updateScheduling("walltime", parseInt(e.target.value) || 30)}
                placeholder="Wall time in minutes"
              />
            </div>

            <div className="space-y-2">
              <Label>Allocation Project</Label>
              <Input
                value={scheduling.allocationProject ?? ""}
                onChange={(e) => updateScheduling("allocationProject", e.target.value)}
                placeholder="Project allocation number (optional)"
              />
            </div>
          </div>
        </>
      ) : (
        <div className="py-8 text-center text-muted-foreground">
          <p className="font-medium text-foreground mb-1">No scheduling required</p>
          <p className="text-sm">
            This resource uses{" "}
            {resource?.capabilities?.compute?.type ?? "FORK"} execution — no queue settings
            are needed.
          </p>
        </div>
      )}

      <div className="flex justify-between">
        <Button variant="outline" onClick={onBack}>
          Back
        </Button>
        <Button onClick={handleNext}>Next</Button>
      </div>
    </div>
  );
}
