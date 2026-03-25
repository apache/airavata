"use client";

import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import type { Application, AppField } from "@/types";

interface Props {
  data: any;
  onUpdate: (data: any) => void;
  onNext: () => void;
  onBack: () => void;
}

export function ConfigureRuntimeStep({ data, onUpdate, onNext, onBack }: Props) {
  const application: Application | undefined = data.application;
  const inputs: AppField[] = application?.inputs ?? [];
  const inputValues: Record<string, string> = data.inputValues ?? {};

  const handleInputChange = (name: string, value: string) => {
    onUpdate({
      inputValues: { ...inputValues, [name]: value },
    });
  };

  const handleNext = () => {
    // Validate required inputs
    const missingRequired = inputs.filter(
      (field) => field.required && !inputValues[field.name]?.trim()
    );
    if (missingRequired.length > 0) {
      alert(`Please fill in required fields: ${missingRequired.map((f) => f.name).join(", ")}`);
      return;
    }
    onNext();
  };

  return (
    <div className="space-y-6">
      {inputs.length === 0 ? (
        <div className="py-8 text-center text-muted-foreground">
          <p className="font-medium text-foreground mb-1">No inputs required</p>
          <p className="text-sm">This application does not require any inputs.</p>
        </div>
      ) : (
        <div className="space-y-4">
          <div>
            <h3 className="text-sm font-semibold">Application Inputs</h3>
            <p className="text-sm text-muted-foreground mt-1">
              Configure input values for {application?.name}
            </p>
          </div>
          {inputs.map((field) => (
            <div key={field.name} className="grid gap-4 md:grid-cols-[140px_1fr] md:items-start">
              <Label className="md:text-right pt-2">
                {field.name}
                {field.required && <span className="text-destructive ml-1">*</span>}
              </Label>
              <div>
                {field.description && (
                  <p className="text-xs text-muted-foreground mb-1">{field.description}</p>
                )}
                {field.type === "STRING" || field.type === "FILE" || field.type === "URI" ? (
                  <Input
                    value={inputValues[field.name] ?? field.defaultValue ?? ""}
                    onChange={(e) => handleInputChange(field.name, e.target.value)}
                    placeholder={field.defaultValue ? `Default: ${field.defaultValue}` : `Enter ${field.name}`}
                  />
                ) : field.type === "INTEGER" || field.type === "FLOAT" ? (
                  <Input
                    type="number"
                    value={inputValues[field.name] ?? field.defaultValue ?? ""}
                    onChange={(e) => handleInputChange(field.name, e.target.value)}
                    placeholder={field.defaultValue ? `Default: ${field.defaultValue}` : `Enter ${field.name}`}
                  />
                ) : (
                  <Textarea
                    value={inputValues[field.name] ?? field.defaultValue ?? ""}
                    onChange={(e) => handleInputChange(field.name, e.target.value)}
                    placeholder={field.defaultValue ? `Default: ${field.defaultValue}` : `Enter ${field.name}`}
                    rows={3}
                  />
                )}
              </div>
            </div>
          ))}
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
