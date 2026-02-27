"use client";

import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { InputEditorFactory } from "../input-editors/InputEditorFactory";
import type { InputDataObjectType } from "@/types";

interface Props {
  data: any;
  onUpdate: (data: any) => void;
  onNext: () => void;
  onBack: () => void;
}

export function InputConfigurationStep({ data, onUpdate, onNext, onBack }: Props) {
  const handleInputChange = (name: string, value: string | undefined) => {
    const updatedInputs = data.inputs.map((input: InputDataObjectType) =>
      input.name === name ? { ...input, value } : input
    );
    onUpdate({ inputs: updatedInputs });
  };

  const validateInputs = () => {
    // Check required inputs
    const missingRequired = data.inputs.some(
      (input: InputDataObjectType) => input.isRequired && !input.value
    );
    if (missingRequired) {
      alert("Please fill in all required inputs");
      return false;
    }
    return true;
  };

  const handleNext = () => {
    if (validateInputs()) {
      onNext();
    }
  };

  return (
    <div className="space-y-6">
      <div className="space-y-4">
        {data.inputs && data.inputs.length > 0 ? (
          data.inputs.map((input: InputDataObjectType) => (
            <div key={input.name} className="space-y-2">
              <Label>
                {input.userFriendlyDescription || input.name}
                {input.isRequired && <span className="text-destructive ml-1">*</span>}
              </Label>
              {input.metaData && (
                <p className="text-sm text-muted-foreground">{input.metaData}</p>
              )}
              <InputEditorFactory
                input={input}
                value={input.value}
                onChange={(value) => handleInputChange(input.name, value)}
              />
            </div>
          ))
        ) : (
          <div className="text-center text-muted-foreground py-8">
            No inputs required for this application
          </div>
        )}
      </div>

      <div className="flex justify-between">
        <Button variant="outline" onClick={onBack}>
          Back
        </Button>
        <Button onClick={handleNext}>Next</Button>
      </div>
    </div>
  );
}
