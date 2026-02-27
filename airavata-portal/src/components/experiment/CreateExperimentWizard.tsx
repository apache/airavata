"use client";

import { useState, useEffect } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { useSession } from "next-auth/react";
import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { ApplicationSelectStep } from "./steps/ApplicationSelectStep";
import { BindingSelectionStep } from "./steps/BindingSelectionStep";
import { ConfigureRuntimeStep } from "./steps/ConfigureRuntimeStep";
import { QueueSettingsStep } from "./steps/QueueSettingsStep";
import { ReviewStep } from "./steps/ReviewStep";
import { ExperimentStepper } from "./ExperimentStepper";
import { apiClient } from "@/lib/api";
import type { Application, ResourceBinding, Resource, AppField } from "@/types";
import { toast } from "@/hooks/useToast";
import { getExperimentPermalink } from "@/lib/permalink";
import { usePortalConfig } from "@/contexts/PortalConfigContext";

export interface WizardData {
  projectId?: string;
  experimentName: string;
  description: string;
  // Step 1: Application
  application?: Application;
  // Step 2: Binding
  bindingId?: string;
  selectedBinding?: ResourceBinding;
  selectedResource?: Resource;
  // Step 3: Inputs
  inputValues: Record<string, string>;
  // Step 4: Scheduling
  scheduling: {
    queueName?: string;
    nodeCount?: number;
    cpuCount?: number;
    walltime?: number;
    allocationProject?: string;
  };
}

const steps = [
  { id: 1, name: "Select Application", description: "Choose the application to run" },
  { id: 2, name: "Select Binding", description: "Choose credential and compute resource" },
  { id: 3, name: "Configure Inputs", description: "Set application input values" },
  { id: 4, name: "Scheduling", description: "Configure queue and resources" },
  { id: 5, name: "Review", description: "Review and launch" },
];

interface CreateExperimentWizardProps {
  initialApplication?: Application;
  initialProjectId?: string;
  onClose?: () => void;
}

export function CreateExperimentWizard({
  initialApplication,
  initialProjectId,
  onClose,
}: CreateExperimentWizardProps = {}) {
  const router = useRouter();
  const searchParams = useSearchParams();
  const { data: session } = useSession();
  const { defaultGatewayId } = usePortalConfig();
  const [currentStep, setCurrentStep] = useState(1);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const [wizardData, setWizardData] = useState<WizardData>({
    experimentName: "",
    description: "",
    inputValues: {},
    scheduling: {},
    projectId: initialProjectId || searchParams?.get("projectId") || undefined,
    application: initialApplication,
  });

  // Sync initial application when it changes (e.g. loaded async)
  useEffect(() => {
    if (initialApplication) {
      setWizardData((prev) => {
        if (prev.application?.applicationId !== initialApplication.applicationId) {
          return {
            ...prev,
            application: initialApplication,
            experimentName: `${initialApplication.name} Experiment`,
            inputValues: {},
          };
        }
        return prev;
      });
    }
  }, [initialApplication]);

  // Sync projectId from URL
  useEffect(() => {
    const projectId = initialProjectId || searchParams?.get("projectId");
    if (projectId) {
      setWizardData((prev) => ({ ...prev, projectId }));
    }
  }, [searchParams, initialProjectId]);

  const updateWizardData = (data: Partial<WizardData>) => {
    setWizardData((prev) => ({ ...prev, ...data }));
  };

  const nextStep = () => {
    if (currentStep < steps.length) {
      setCurrentStep((prev) => prev + 1);
    }
  };

  const prevStep = () => {
    if (currentStep > 1) {
      setCurrentStep((prev) => prev - 1);
    }
  };

  const handleSubmit = async (launchImmediately: boolean) => {
    if (!wizardData.application) {
      toast({ title: "Error", description: "No application selected", variant: "destructive" });
      return;
    }
    if (!wizardData.bindingId) {
      toast({ title: "Error", description: "No resource binding selected", variant: "destructive" });
      return;
    }

    const gatewayId = session?.user?.gatewayId || defaultGatewayId;
    const userName = session?.user?.email || "admin";

    setIsSubmitting(true);
    try {
      // Build inputs array from the application fields
      const inputs: { name: string; value: string; type: string }[] = (
        wizardData.application.inputs ?? []
      ).map((field: AppField) => ({
        name: field.name,
        value: wizardData.inputValues[field.name] ?? field.defaultValue ?? "",
        type: field.type,
      }));

      const payload = {
        experimentName: wizardData.experimentName,
        description: wizardData.description || undefined,
        applicationId: wizardData.application.applicationId,
        bindingId: wizardData.bindingId,
        projectId: wizardData.projectId,
        gatewayId,
        userName,
        inputs,
        scheduling: Object.keys(wizardData.scheduling).length > 0
          ? wizardData.scheduling
          : undefined,
      };

      const result = await apiClient.post<{ experimentId: string }>(
        "/api/v1/experiments",
        payload
      );

      toast({
        title: "Experiment created",
        description: `Experiment created successfully.`,
      });

      if (launchImmediately && result.experimentId) {
        try {
          await apiClient.post(`/api/v1/experiments/${result.experimentId}/launch`);
          toast({
            title: "Experiment launched",
            description: "Your experiment has been submitted for execution.",
          });
        } catch (launchErr) {
          toast({
            title: "Launch failed",
            description: launchErr instanceof Error ? launchErr.message : "Failed to launch experiment",
            variant: "destructive",
          });
        }
      }

      if (onClose) {
        onClose();
        if (result.experimentId) {
          setTimeout(() => {
            router.push(getExperimentPermalink(result.experimentId));
          }, 100);
        }
      } else if (result.experimentId) {
        router.push(getExperimentPermalink(result.experimentId));
      }
    } catch (error) {
      toast({
        title: "Error",
        description: error instanceof Error ? error.message : "Failed to create experiment",
        variant: "destructive",
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  const isModalMode = !!onClose;

  const stepContent = (
    <div className="space-y-6">
      {currentStep === 1 && (
        <ApplicationSelectStep
          data={wizardData}
          onUpdate={updateWizardData}
          onNext={nextStep}
        />
      )}
      {currentStep === 2 && (
        <BindingSelectionStep
          data={wizardData}
          onUpdate={updateWizardData}
          onNext={nextStep}
          onBack={prevStep}
        />
      )}
      {currentStep === 3 && (
        <ConfigureRuntimeStep
          data={wizardData}
          onUpdate={updateWizardData}
          onNext={nextStep}
          onBack={prevStep}
        />
      )}
      {currentStep === 4 && (
        <QueueSettingsStep
          data={wizardData}
          onUpdate={updateWizardData}
          onNext={nextStep}
          onBack={prevStep}
        />
      )}
      {currentStep === 5 && (
        <ReviewStep
          data={wizardData}
          onBack={prevStep}
          onSubmit={handleSubmit}
          isSubmitting={isSubmitting}
        />
      )}
    </div>
  );

  if (isModalMode) {
    return (
      <div className="space-y-6">
        <div>
          <ExperimentStepper steps={steps} currentStep={currentStep} />
        </div>
        {stepContent}
      </div>
    );
  }

  return (
    <Card>
      <CardHeader>
        <ExperimentStepper steps={steps} currentStep={currentStep} />
      </CardHeader>
      <CardContent>{stepContent}</CardContent>
    </Card>
  );
}
