"use client";

import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import type { InputDataObjectType } from "@/types";

interface StringInputEditorProps {
  input: InputDataObjectType;
  value: string;
  onChange: (value: string) => void;
  disabled?: boolean;
}

export function StringInputEditor({ input, value, onChange, disabled }: StringInputEditorProps) {
  return (
    <div className="space-y-2">
      <Label htmlFor={input.name}>
        {input.name}
        {input.isRequired && <span className="text-red-500 ml-1">*</span>}
      </Label>
      {input.userFriendlyDescription && (
        <p className="text-sm text-muted-foreground">{input.userFriendlyDescription}</p>
      )}
      <Input
        id={input.name}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder={`Enter ${input.name}`}
        disabled={disabled || input.isReadOnly}
        required={input.isRequired}
      />
    </div>
  );
}
