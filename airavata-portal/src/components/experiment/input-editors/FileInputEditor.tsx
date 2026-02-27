"use client";

import { useState, useRef } from "react";
import { Upload, X, File } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import type { InputDataObjectType } from "@/types";

interface FileInputEditorProps {
  input: InputDataObjectType;
  value: string;
  onChange: (value: string) => void;
  disabled?: boolean;
}

export function FileInputEditor({ input, value, onChange, disabled }: FileInputEditorProps) {
  const [fileName, setFileName] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setFileName(file.name);
      // In a real implementation, you would upload the file and get a URI
      onChange(file.name); // Placeholder - would be replaced with actual URI
    }
  };

  const handleClear = () => {
    setFileName(null);
    onChange("");
    if (fileInputRef.current) {
      fileInputRef.current.value = "";
    }
  };

  return (
    <div className="space-y-2">
      <Label>
        {input.name}
        {input.isRequired && <span className="text-red-500 ml-1">*</span>}
      </Label>
      {input.userFriendlyDescription && (
        <p className="text-sm text-muted-foreground">{input.userFriendlyDescription}</p>
      )}
      
      <div className="border-2 border-dashed rounded-lg p-4">
        {fileName || value ? (
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <File className="h-4 w-4 text-muted-foreground" />
              <span className="text-sm">{fileName || value}</span>
            </div>
            <Button
              type="button"
              variant="ghost"
              size="icon"
              onClick={handleClear}
              disabled={disabled}
            >
              <X className="h-4 w-4" />
            </Button>
          </div>
        ) : (
          <div className="text-center">
            <Upload className="h-8 w-8 mx-auto text-muted-foreground mb-2" />
            <p className="text-sm text-muted-foreground mb-2">
              Click to upload or drag and drop
            </p>
            <input
              ref={fileInputRef}
              type="file"
              className="hidden"
              onChange={handleFileSelect}
              disabled={disabled || input.isReadOnly}
            />
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={() => fileInputRef.current?.click()}
              disabled={disabled}
            >
              Select File
            </Button>
          </div>
        )}
      </div>
    </div>
  );
}
