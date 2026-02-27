"use client";

import { useState } from "react";
import { Loader2, Plus, Trash2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { cn } from "@/lib/utils";
import { apiClient } from "@/lib/api";
import type { Application, AppField } from "@/types";

interface CreateApplicationWizardProps {
  gatewayId: string;
  onSuccess: (application: Application) => void;
  onCancel: () => void;
}

const FIELD_TYPES = ["STRING", "INTEGER", "FLOAT", "BOOLEAN", "URI", "FILE"];

const emptyField = (): AppField => ({
  name: "",
  type: "STRING",
  required: false,
  description: "",
  defaultValue: "",
});

export function CreateApplicationWizard({
  gatewayId,
  onSuccess,
  onCancel,
}: CreateApplicationWizardProps) {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [formData, setFormData] = useState({
    name: "",
    version: "",
    description: "",
    scope: "GATEWAY" as "GATEWAY" | "PRIVATE",
    installScript: "",
    runScript: "",
    inputs: [] as AppField[],
    outputs: [] as AppField[],
  });

  const [newInput, setNewInput] = useState<AppField>(emptyField());
  const [newOutput, setNewOutput] = useState<AppField>(emptyField());

  const addInput = () => {
    if (!newInput.name.trim()) return;
    setFormData((prev) => ({
      ...prev,
      inputs: [...prev.inputs, { ...newInput, name: newInput.name.trim() }],
    }));
    setNewInput(emptyField());
  };

  const removeInput = (index: number) => {
    setFormData((prev) => ({
      ...prev,
      inputs: prev.inputs.filter((_, i) => i !== index),
    }));
  };

  const addOutput = () => {
    if (!newOutput.name.trim()) return;
    setFormData((prev) => ({
      ...prev,
      outputs: [...prev.outputs, { ...newOutput, name: newOutput.name.trim() }],
    }));
    setNewOutput(emptyField());
  };

  const removeOutput = (index: number) => {
    setFormData((prev) => ({
      ...prev,
      outputs: prev.outputs.filter((_, i) => i !== index),
    }));
  };

  const handleSubmit = async () => {
    if (!formData.name.trim()) {
      setError("Application name is required");
      return;
    }

    setError(null);
    setIsSubmitting(true);

    try {
      const payload = {
        gatewayId,
        name: formData.name.trim(),
        version: formData.version.trim() || undefined,
        description: formData.description.trim() || undefined,
        scope: formData.scope,
        installScript: formData.installScript.trim() || undefined,
        runScript: formData.runScript.trim() || undefined,
        inputs: formData.inputs,
        outputs: formData.outputs,
      };

      const result = await apiClient.post<Application>("/api/v1/applications", payload);
      onSuccess(result);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to create application");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      {error && (
        <div className="p-3 text-sm bg-destructive/10 text-destructive rounded-md">
          {error}
        </div>
      )}

      {/* Basic Details */}
      <Card>
        <CardHeader>
          <CardTitle>Application Details</CardTitle>
          <CardDescription>Define the application name, version, and description</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid gap-4 md:grid-cols-2">
            <div className="space-y-2">
              <Label htmlFor="app-name">Name *</Label>
              <Input
                id="app-name"
                value={formData.name}
                onChange={(e) => setFormData((prev) => ({ ...prev, name: e.target.value }))}
                placeholder="e.g., Gaussian, OpenFOAM, GROMACS"
                disabled={isSubmitting}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="app-version">Version</Label>
              <Input
                id="app-version"
                value={formData.version}
                onChange={(e) => setFormData((prev) => ({ ...prev, version: e.target.value }))}
                placeholder="e.g., 1.0.0, 2024.1"
                disabled={isSubmitting}
              />
            </div>
          </div>
          <div className="space-y-2">
            <Label htmlFor="app-description">Description</Label>
            <Textarea
              id="app-description"
              value={formData.description}
              onChange={(e) => setFormData((prev) => ({ ...prev, description: e.target.value }))}
              placeholder="Describe the application and its purpose"
              rows={3}
              disabled={isSubmitting}
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="app-scope">Scope</Label>
            <Select
              value={formData.scope}
              onValueChange={(value) => setFormData((prev) => ({ ...prev, scope: value as "GATEWAY" | "PRIVATE" }))}
              disabled={isSubmitting}
            >
              <SelectTrigger id="app-scope">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="GATEWAY">Gateway (visible to all users)</SelectItem>
                <SelectItem value="PRIVATE">Private (only you)</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </CardContent>
      </Card>

      {/* Inputs */}
      <Card>
        <CardHeader>
          <CardTitle>Inputs</CardTitle>
          <CardDescription>Define the input fields required by the application</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid gap-3 p-4 border rounded-lg bg-muted/30">
            <div className="grid gap-3 md:grid-cols-4">
              <div className="space-y-1">
                <Label className="text-xs">Name</Label>
                <Input
                  value={newInput.name}
                  onChange={(e) => setNewInput((prev) => ({ ...prev, name: e.target.value }))}
                  placeholder="Input name"
                  className="h-9"
                  disabled={isSubmitting}
                />
              </div>
              <div className="space-y-1">
                <Label className="text-xs">Type</Label>
                <Select
                  value={newInput.type}
                  onValueChange={(value) => setNewInput((prev) => ({ ...prev, type: value }))}
                  disabled={isSubmitting}
                >
                  <SelectTrigger className="h-9">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {FIELD_TYPES.map((t) => (
                      <SelectItem key={t} value={t}>{t}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-1">
                <Label className="text-xs">Description</Label>
                <Input
                  value={newInput.description || ""}
                  onChange={(e) => setNewInput((prev) => ({ ...prev, description: e.target.value }))}
                  placeholder="Optional description"
                  className="h-9"
                  disabled={isSubmitting}
                />
              </div>
              <div className="flex items-end gap-2">
                <Button
                  type="button"
                  variant={newInput.required ? "default" : "outline"}
                  size="sm"
                  className={cn(
                    "h-9 min-w-[5.5rem] text-xs",
                    newInput.required
                      ? "bg-primary text-primary-foreground hover:bg-primary/90"
                      : "bg-background border border-input hover:bg-muted/50"
                  )}
                  onClick={() => setNewInput((prev) => ({ ...prev, required: !prev.required }))}
                  disabled={isSubmitting}
                >
                  {newInput.required ? "Required" : "Optional"}
                </Button>
                <Button
                  type="button"
                  size="sm"
                  onClick={addInput}
                  disabled={!newInput.name.trim() || isSubmitting}
                >
                  <Plus className="h-4 w-4" />
                </Button>
              </div>
            </div>
          </div>

          {formData.inputs.length > 0 ? (
            <div className="space-y-2">
              {formData.inputs.map((field, idx) => (
                <div key={idx} className="flex items-center justify-between p-3 border rounded-lg">
                  <div className="flex-1 grid grid-cols-4 gap-4 text-sm items-center">
                    <span className="font-medium">{field.name}</span>
                    <Badge variant="outline" className="text-xs w-fit">{field.type}</Badge>
                    <span className="text-muted-foreground truncate">{field.description || "-"}</span>
                    <Badge
                      variant={field.required ? "default" : "secondary"}
                      className="text-xs w-fit"
                    >
                      {field.required ? "Required" : "Optional"}
                    </Badge>
                  </div>
                  <Button
                    type="button"
                    variant="ghost"
                    size="sm"
                    onClick={() => removeInput(idx)}
                    disabled={isSubmitting}
                  >
                    <Trash2 className="h-4 w-4 text-destructive" />
                  </Button>
                </div>
              ))}
            </div>
          ) : (
            <p className="text-sm text-muted-foreground text-center py-4">
              No inputs defined yet. Add inputs above.
            </p>
          )}
        </CardContent>
      </Card>

      {/* Outputs */}
      <Card>
        <CardHeader>
          <CardTitle>Outputs</CardTitle>
          <CardDescription>Define the output fields produced by the application</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid gap-3 p-4 border rounded-lg bg-muted/30">
            <div className="grid gap-3 md:grid-cols-4">
              <div className="space-y-1">
                <Label className="text-xs">Name</Label>
                <Input
                  value={newOutput.name}
                  onChange={(e) => setNewOutput((prev) => ({ ...prev, name: e.target.value }))}
                  placeholder="Output name"
                  className="h-9"
                  disabled={isSubmitting}
                />
              </div>
              <div className="space-y-1">
                <Label className="text-xs">Type</Label>
                <Select
                  value={newOutput.type}
                  onValueChange={(value) => setNewOutput((prev) => ({ ...prev, type: value }))}
                  disabled={isSubmitting}
                >
                  <SelectTrigger className="h-9">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {FIELD_TYPES.map((t) => (
                      <SelectItem key={t} value={t}>{t}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-1">
                <Label className="text-xs">Description</Label>
                <Input
                  value={newOutput.description || ""}
                  onChange={(e) => setNewOutput((prev) => ({ ...prev, description: e.target.value }))}
                  placeholder="Optional description"
                  className="h-9"
                  disabled={isSubmitting}
                />
              </div>
              <div className="flex items-end gap-2">
                <Button
                  type="button"
                  variant={newOutput.required ? "default" : "outline"}
                  size="sm"
                  className={cn(
                    "h-9 min-w-[5.5rem] text-xs",
                    newOutput.required
                      ? "bg-primary text-primary-foreground hover:bg-primary/90"
                      : "bg-background border border-input hover:bg-muted/50"
                  )}
                  onClick={() => setNewOutput((prev) => ({ ...prev, required: !prev.required }))}
                  disabled={isSubmitting}
                >
                  {newOutput.required ? "Required" : "Optional"}
                </Button>
                <Button
                  type="button"
                  size="sm"
                  onClick={addOutput}
                  disabled={!newOutput.name.trim() || isSubmitting}
                >
                  <Plus className="h-4 w-4" />
                </Button>
              </div>
            </div>
          </div>

          {formData.outputs.length > 0 ? (
            <div className="space-y-2">
              {formData.outputs.map((field, idx) => (
                <div key={idx} className="flex items-center justify-between p-3 border rounded-lg">
                  <div className="flex-1 grid grid-cols-4 gap-4 text-sm items-center">
                    <span className="font-medium">{field.name}</span>
                    <Badge variant="outline" className="text-xs w-fit">{field.type}</Badge>
                    <span className="text-muted-foreground truncate">{field.description || "-"}</span>
                    <Badge
                      variant={field.required ? "default" : "secondary"}
                      className="text-xs w-fit"
                    >
                      {field.required ? "Required" : "Optional"}
                    </Badge>
                  </div>
                  <Button
                    type="button"
                    variant="ghost"
                    size="sm"
                    onClick={() => removeOutput(idx)}
                    disabled={isSubmitting}
                  >
                    <Trash2 className="h-4 w-4 text-destructive" />
                  </Button>
                </div>
              ))}
            </div>
          ) : (
            <p className="text-sm text-muted-foreground text-center py-4">
              No outputs defined yet. Add outputs above.
            </p>
          )}
        </CardContent>
      </Card>

      {/* Scripts */}
      <Card>
        <CardHeader>
          <CardTitle>Scripts</CardTitle>
          <CardDescription>Optional installation and run scripts</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="install-script">Install Script</Label>
            <Textarea
              id="install-script"
              value={formData.installScript}
              onChange={(e) => setFormData((prev) => ({ ...prev, installScript: e.target.value }))}
              placeholder="#!/bin/bash&#10;# Installation commands here"
              rows={4}
              className="font-mono text-sm"
              disabled={isSubmitting}
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="run-script">Run Script</Label>
            <Textarea
              id="run-script"
              value={formData.runScript}
              onChange={(e) => setFormData((prev) => ({ ...prev, runScript: e.target.value }))}
              placeholder="#!/bin/bash&#10;# Execution commands here"
              rows={4}
              className="font-mono text-sm"
              disabled={isSubmitting}
            />
          </div>
        </CardContent>
      </Card>

      {/* Action Buttons */}
      <div className="flex items-center justify-end gap-2 pt-4 border-t">
        <Button variant="outline" onClick={onCancel} disabled={isSubmitting}>
          Cancel
        </Button>
        <Button
          onClick={handleSubmit}
          disabled={isSubmitting || !formData.name.trim()}
        >
          {isSubmitting ? (
            <>
              <Loader2 className="mr-2 h-4 w-4 animate-spin" />
              Creating...
            </>
          ) : (
            "Create Application"
          )}
        </Button>
      </div>
    </div>
  );
}
