"use client";

import { Check } from "lucide-react";
import { cn } from "@/lib/utils";

interface Step {
  id: number;
  name: string;
}

interface ExperimentStepperProps {
  steps: Step[];
  currentStep: number;
}

export function ExperimentStepper({ steps, currentStep }: ExperimentStepperProps) {
  return (
    <div className="w-full py-4">
      <div className="flex items-start relative">
        {steps.map((step, index) => {
          const stepNumber = index + 1;
          const isCompleted = stepNumber < currentStep;
          const isCurrent = stepNumber === currentStep;
          const isUpcoming = stepNumber > currentStep;

          return (
            <div key={step.id} className="flex flex-col items-center flex-1 relative z-10">
              {/* Step Circle */}
              <div
                className={cn(
                  "relative flex items-center justify-center w-10 h-10 rounded-full border-2 transition-all",
                  isCompleted && "bg-primary border-primary text-primary-foreground",
                  isCurrent && "bg-primary border-primary text-primary-foreground ring-2 ring-primary ring-offset-2",
                  isUpcoming && "bg-background border-muted-foreground/30 text-muted-foreground"
                )}
              >
                {isCompleted ? (
                  <Check className="h-5 w-5" />
                ) : (
                  <span className="text-sm font-semibold">{stepNumber}</span>
                )}
              </div>
              
              {/* Step Label */}
              <span
                className={cn(
                  "mt-2 text-xs text-center max-w-[80px] leading-tight",
                  isCurrent && "font-medium text-foreground",
                  !isCurrent && "text-muted-foreground"
                )}
              >
                {step.name}
              </span>

              {/* Connector Line */}
              {index < steps.length - 1 && (
                <div
                  className={cn(
                    "absolute left-[calc(50%+1.25rem)] top-5 h-0.5 right-[-calc(50%-1.25rem)]",
                    isCompleted ? "bg-primary" : "bg-muted-foreground/30"
                  )}
                />
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}
