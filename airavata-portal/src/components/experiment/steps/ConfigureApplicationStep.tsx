"use client";

// This step is superseded by ApplicationSelectStep in the new experiment wizard.
// Kept as a placeholder to avoid breaking any lingering imports.

import { Button } from "@/components/ui/button";

interface Props {
  data: Record<string, unknown>;
  onUpdate: (data: Record<string, unknown>) => void;
  onNext: () => void;
}

export function ConfigureApplicationStep({ onNext }: Props) {
  return (
    <div className="space-y-6">
      <p className="text-muted-foreground text-sm">
        This step is deprecated. Please use the Application Select step instead.
      </p>
      <div className="flex justify-end">
        <Button onClick={onNext}>Next</Button>
      </div>
    </div>
  );
}
