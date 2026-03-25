"use client";

import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import type { InputDataObjectType } from "@/types";

interface SelectInputEditorProps {
  input: InputDataObjectType;
  value: string;
  onChange: (value: string) => void;
  disabled?: boolean;
  options?: { value: string; label: string }[];
}

export function SelectInputEditor({ input, value, onChange, disabled, options = [] }: SelectInputEditorProps) {
  // Parse options from metadata if available
  const parsedOptions = options.length > 0 ? options : parseOptionsFromMetadata(input.metaData);

  return (
    <div className="space-y-2">
      <Label>
        {input.name}
        {input.isRequired && <span className="text-red-500 ml-1">*</span>}
      </Label>
      {input.userFriendlyDescription && (
        <p className="text-sm text-muted-foreground">{input.userFriendlyDescription}</p>
      )}
      <Select value={value} onValueChange={onChange} disabled={disabled || input.isReadOnly}>
        <SelectTrigger>
          <SelectValue placeholder={`Select ${input.name}`} />
        </SelectTrigger>
        <SelectContent>
          {parsedOptions.map((option) => (
            <SelectItem key={option.value} value={option.value}>
              {option.label}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>
    </div>
  );
}

function parseOptionsFromMetadata(metadata?: string): { value: string; label: string }[] {
  if (!metadata) return [];
  try {
    // Try to parse as JSON
    const parsed = JSON.parse(metadata);
    if (Array.isArray(parsed)) {
      return parsed.map((item) => ({
        value: String(item.value || item),
        label: String(item.label || item),
      }));
    }
    if (parsed.options) {
      return parsed.options.map((item: any) => ({
        value: String(item.value || item),
        label: String(item.label || item),
      }));
    }
  } catch {
    // If not JSON, try comma-separated values
    return metadata.split(",").map((s) => {
      const trimmed = s.trim();
      return { value: trimmed, label: trimmed };
    });
  }
  return [];
}
