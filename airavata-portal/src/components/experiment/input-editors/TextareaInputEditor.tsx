"use client";

import { Textarea } from "@/components/ui/textarea";
import { Label } from "@/components/ui/label";
import type { InputDataObjectType } from "@/types";

interface TextareaInputEditorProps {
  input: InputDataObjectType;
  value: string;
  onChange: (value: string) => void;
  disabled?: boolean;
}

export function TextareaInputEditor({ input, value, onChange, disabled }: TextareaInputEditorProps) {
  return (
    <div className="space-y-2">
      <Label htmlFor={input.name}>
        {input.name}
        {input.isRequired && <span className="text-red-500 ml-1">*</span>}
      </Label>
      {input.userFriendlyDescription && (
        <p className="text-sm text-muted-foreground">{input.userFriendlyDescription}</p>
      )}
      <Textarea
        id={input.name}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder={`Enter ${input.name}`}
        disabled={disabled || input.isReadOnly}
        required={input.isRequired}
        rows={4}
      />
    </div>
  );
}
