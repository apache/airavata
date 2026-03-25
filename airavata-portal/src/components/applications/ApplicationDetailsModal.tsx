"use client";

import { AlertCircle } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { useQuery } from "@tanstack/react-query";
import { apiClient } from "@/lib/api";
import type { Application } from "@/types";

interface ApplicationDetailsModalProps {
  applicationId: string;
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export function ApplicationDetailsModal({
  applicationId,
  open,
  onOpenChange,
}: ApplicationDetailsModalProps) {
  const {
    data: application,
    isLoading,
    error,
  } = useQuery({
    queryKey: ["application", applicationId],
    queryFn: async () => {
      return apiClient.get<Application>(`/api/v1/applications/${applicationId}`);
    },
    enabled: !!applicationId && open,
    retry: 2,
    staleTime: 30000,
  });

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-4xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>{application?.name || "Application Details"}</DialogTitle>
          <DialogDescription>
            View application details, inputs, and outputs
          </DialogDescription>
        </DialogHeader>

        {isLoading ? (
          <div className="space-y-4">
            <Skeleton className="h-64 w-full" />
          </div>
        ) : error || !application ? (
          <Card className="border-destructive">
            <CardContent className="py-8">
              <div className="flex flex-col items-center text-center">
                <AlertCircle className="h-12 w-12 text-destructive mb-4" />
                <h3 className="text-lg font-semibold">Failed to load application</h3>
                <p className="text-muted-foreground mt-1 max-w-md">
                  {error instanceof Error ? error.message : "Application not found"}
                </p>
              </div>
            </CardContent>
          </Card>
        ) : (
          <div className="space-y-6">
            {/* Overview */}
            <Card>
              <CardHeader>
                <CardTitle>Overview</CardTitle>
                <CardDescription>Application configuration details</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="grid gap-4 md:grid-cols-2">
                  <div>
                    <p className="text-sm text-muted-foreground">Name</p>
                    <p className="font-medium">{application.name}</p>
                  </div>
                  {application.version && (
                    <div>
                      <p className="text-sm text-muted-foreground">Version</p>
                      <p className="font-medium">{application.version}</p>
                    </div>
                  )}
                  <div>
                    <p className="text-sm text-muted-foreground">Scope</p>
                    <Badge variant={application.scope === "GATEWAY" ? "default" : "secondary"}>
                      {application.scope}
                    </Badge>
                  </div>
                  <div>
                    <p className="text-sm text-muted-foreground">Description</p>
                    <p className="text-sm">{application.description || "No description"}</p>
                  </div>
                </div>
              </CardContent>
            </Card>

            {/* Inputs */}
            <Card>
              <CardHeader>
                <CardTitle>Inputs ({application.inputs.length})</CardTitle>
                <CardDescription>Input fields required by this application</CardDescription>
              </CardHeader>
              <CardContent>
                {application.inputs.length === 0 ? (
                  <p className="text-sm text-muted-foreground">No inputs defined</p>
                ) : (
                  <div className="border rounded-lg overflow-hidden">
                    <Table>
                      <TableHeader>
                        <TableRow>
                          <TableHead className="h-9 px-3">Name</TableHead>
                          <TableHead className="h-9 px-3">Type</TableHead>
                          <TableHead className="h-9 px-3">Required</TableHead>
                          <TableHead className="h-9 px-3">Description</TableHead>
                          <TableHead className="h-9 px-3">Default</TableHead>
                        </TableRow>
                      </TableHeader>
                      <TableBody>
                        {application.inputs.map((field, idx) => (
                          <TableRow key={idx}>
                            <TableCell className="py-1.5 px-3 font-medium">{field.name}</TableCell>
                            <TableCell className="py-1.5 px-3">
                              <Badge variant="outline" className="text-xs">{field.type}</Badge>
                            </TableCell>
                            <TableCell className="py-1.5 px-3">
                              {field.required ? (
                                <Badge variant="default" className="text-xs bg-orange-500">Required</Badge>
                              ) : (
                                <Badge variant="secondary" className="text-xs">Optional</Badge>
                              )}
                            </TableCell>
                            <TableCell className="py-1.5 px-3 text-muted-foreground text-sm">
                              {field.description || "-"}
                            </TableCell>
                            <TableCell className="py-1.5 px-3 font-mono text-xs text-muted-foreground">
                              {field.defaultValue || "-"}
                            </TableCell>
                          </TableRow>
                        ))}
                      </TableBody>
                    </Table>
                  </div>
                )}
              </CardContent>
            </Card>

            {/* Outputs */}
            <Card>
              <CardHeader>
                <CardTitle>Outputs ({application.outputs.length})</CardTitle>
                <CardDescription>Output fields produced by this application</CardDescription>
              </CardHeader>
              <CardContent>
                {application.outputs.length === 0 ? (
                  <p className="text-sm text-muted-foreground">No outputs defined</p>
                ) : (
                  <div className="border rounded-lg overflow-hidden">
                    <Table>
                      <TableHeader>
                        <TableRow>
                          <TableHead className="h-9 px-3">Name</TableHead>
                          <TableHead className="h-9 px-3">Type</TableHead>
                          <TableHead className="h-9 px-3">Required</TableHead>
                          <TableHead className="h-9 px-3">Description</TableHead>
                        </TableRow>
                      </TableHeader>
                      <TableBody>
                        {application.outputs.map((field, idx) => (
                          <TableRow key={idx}>
                            <TableCell className="py-1.5 px-3 font-medium">{field.name}</TableCell>
                            <TableCell className="py-1.5 px-3">
                              <Badge variant="outline" className="text-xs">{field.type}</Badge>
                            </TableCell>
                            <TableCell className="py-1.5 px-3">
                              {field.required ? (
                                <Badge variant="default" className="text-xs bg-orange-500">Required</Badge>
                              ) : (
                                <Badge variant="secondary" className="text-xs">Optional</Badge>
                              )}
                            </TableCell>
                            <TableCell className="py-1.5 px-3 text-muted-foreground text-sm">
                              {field.description || "-"}
                            </TableCell>
                          </TableRow>
                        ))}
                      </TableBody>
                    </Table>
                  </div>
                )}
              </CardContent>
            </Card>

            {/* Scripts */}
            {(application.installScript || application.runScript) && (
              <Card>
                <CardHeader>
                  <CardTitle>Scripts</CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  {application.installScript && (
                    <div>
                      <p className="text-sm font-medium mb-2">Install Script</p>
                      <pre className="text-xs bg-muted p-3 rounded-lg overflow-x-auto whitespace-pre-wrap">
                        {application.installScript}
                      </pre>
                    </div>
                  )}
                  {application.runScript && (
                    <div>
                      <p className="text-sm font-medium mb-2">Run Script</p>
                      <pre className="text-xs bg-muted p-3 rounded-lg overflow-x-auto whitespace-pre-wrap">
                        {application.runScript}
                      </pre>
                    </div>
                  )}
                </CardContent>
              </Card>
            )}
          </div>
        )}
      </DialogContent>
    </Dialog>
  );
}
