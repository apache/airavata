"use client";

import Link from "next/link";
import { AppWindow, Play, Download, Upload } from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import type { Application } from "@/types";
import { useCreateExperimentModal } from "@/contexts/CreateExperimentModalContext";
import { cn } from "@/lib/utils";

interface ApplicationCardProps {
  application: Application;
}

export function ApplicationCard({ application }: ApplicationCardProps) {
  const { openModal } = useCreateExperimentModal();
  const appPermalink = `/catalog/APPLICATION/${application.applicationId}`;

  const handleCreateExperiment = (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    openModal({ application });
  };

  return (
    <Link href={appPermalink} className="block">
      <Card className={cn("transition-shadow hover:shadow-md cursor-pointer h-full")}>
        <CardHeader>
          <div className="flex items-start justify-between">
            <div className="flex items-start gap-3 flex-1">
              <div className="flex h-12 w-12 items-center justify-center rounded-lg bg-gradient-to-br from-blue-500 to-indigo-600">
                <AppWindow className="h-6 w-6 text-white" />
              </div>
              <div className="flex-1">
                <CardTitle className="text-lg hover:text-primary">
                  {application.name}
                  {application.version && (
                    <span className="ml-2 text-sm font-normal text-muted-foreground">
                      v{application.version}
                    </span>
                  )}
                </CardTitle>
                <CardDescription className="mt-1 line-clamp-2">
                  {application.description || "No description available"}
                </CardDescription>
              </div>
            </div>
            <Badge variant="secondary">Application</Badge>
          </div>
        </CardHeader>
        <CardContent>
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-4">
              <div
                className="flex items-center gap-1.5 text-muted-foreground"
                title={`${application.inputs.length} input(s)`}
              >
                <Download className="h-4 w-4" />
                <span className="text-sm font-medium">{application.inputs.length}</span>
              </div>
              <div
                className="flex items-center gap-1.5 text-muted-foreground"
                title={`${application.outputs.length} output(s)`}
              >
                <Upload className="h-4 w-4" />
                <span className="text-sm font-medium">{application.outputs.length}</span>
              </div>
            </div>
            <Button onClick={handleCreateExperiment}>
              <Play className="mr-2 h-4 w-4" />
              Run
            </Button>
          </div>
        </CardContent>
      </Card>
    </Link>
  );
}
