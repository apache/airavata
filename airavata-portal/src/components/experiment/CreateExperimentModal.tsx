"use client";

import { useEffect, useState } from "react";
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { CreateExperimentWizard } from "./CreateExperimentWizard";
import type { Application } from "@/types";

interface CreateExperimentModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  initialApplication?: Application;
  initialProjectId?: string;
}

export function CreateExperimentModal({
  open,
  onOpenChange,
  initialApplication,
  initialProjectId,
}: CreateExperimentModalProps) {
  const [key, setKey] = useState(0);

  // Reset wizard when modal closes to ensure fresh state on next open
  useEffect(() => {
    if (!open) {
      const timer = setTimeout(() => {
        setKey((prev) => prev + 1);
      }, 300);
      return () => clearTimeout(timer);
    }
  }, [open]);

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-5xl max-h-[90vh]">
        <DialogHeader>
          <DialogTitle>Create Experiment</DialogTitle>
          <DialogDescription>
            {initialApplication
              ? `Create a new experiment using ${initialApplication.name}`
              : "Set up a new computational experiment by selecting an application and configuring its parameters"}
          </DialogDescription>
        </DialogHeader>
        <div className="overflow-y-auto max-h-[calc(90vh-180px)]">
          <CreateExperimentWizard
            key={key}
            initialApplication={initialApplication}
            initialProjectId={initialProjectId}
            onClose={() => onOpenChange(false)}
          />
        </div>
      </DialogContent>
    </Dialog>
  );
}
