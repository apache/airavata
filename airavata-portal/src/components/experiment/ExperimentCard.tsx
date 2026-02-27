"use client";

import Link from "next/link";
import { FlaskConical, MoreVertical, Eye, Pencil, Trash2, StopCircle } from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import type { ExperimentModel } from "@/types";
import { ExperimentState } from "@/types";
import { formatDate, getExperimentStatusColor, isTerminalState } from "@/lib/utils";
import { GatewayBadge } from "@/components/gateway/GatewayBadge";
import { getExperimentPermalink } from "@/lib/permalink";

interface ExperimentCardProps {
  experiment: ExperimentModel;
  onDelete?: (experiment: ExperimentModel) => void;
  onCancel?: (experiment: ExperimentModel) => void;
}

export function ExperimentCard({ experiment, onDelete, onCancel }: ExperimentCardProps) {
  const status = experiment.experimentStatus?.[0]?.state || "UNKNOWN";
  const canEdit = status === ExperimentState.CREATED;
  const canCancel = [ExperimentState.EXECUTING, ExperimentState.LAUNCHED, ExperimentState.SCHEDULED].includes(status as ExperimentState);
  const experimentPermalink = getExperimentPermalink(experiment.experimentId);

  return (
    <Link href={experimentPermalink}>
      <Card className="cursor-pointer transition-shadow hover:shadow-md h-full">
        <CardHeader className="flex flex-row items-start justify-between space-y-0">
          <div className="flex items-start gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-blue-100">
              <FlaskConical className="h-5 w-5 text-blue-600" />
            </div>
            <div className="min-w-0 flex-1">
              <CardTitle className="text-lg truncate">
                {experiment.experimentName}
              </CardTitle>
              <CardDescription className="mt-1">
                Created {formatDate(experiment.creationTime)}
              </CardDescription>
            </div>
          </div>
          <div className="flex items-center gap-2">
            <Badge variant="outline" className={`${getExperimentStatusColor(status)}`}>
              {status}
            </Badge>
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant="ghost" size="icon" onClick={(e) => e.preventDefault()}>
                  <MoreVertical className="h-4 w-4" />
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end">
                <DropdownMenuItem asChild>
                  <Link href={experimentPermalink}>
                    <Eye className="mr-2 h-4 w-4" />
                    View Details
                  </Link>
                </DropdownMenuItem>
                {canEdit && (
                  <DropdownMenuItem asChild>
                    <Link href={`${experimentPermalink}/edit`}>
                      <Pencil className="mr-2 h-4 w-4" />
                      Edit
                    </Link>
                  </DropdownMenuItem>
                )}
                {canCancel && (
                  <DropdownMenuItem onClick={(e) => {
                    e.preventDefault();
                    onCancel?.(experiment);
                  }}>
                    <StopCircle className="mr-2 h-4 w-4" />
                    Cancel
                  </DropdownMenuItem>
                )}
                {isTerminalState(status) && (
                  <DropdownMenuItem
                    className="text-red-600"
                    onClick={(e) => {
                      e.preventDefault();
                      onDelete?.(experiment);
                    }}
                  >
                    <Trash2 className="mr-2 h-4 w-4" />
                    Delete
                  </DropdownMenuItem>
                )}
              </DropdownMenuContent>
            </DropdownMenu>
          </div>
        </CardHeader>
        <CardContent>
          {experiment.gatewayId && <GatewayBadge gatewayId={experiment.gatewayId} className="mb-2" />}
          <p className="text-sm text-muted-foreground line-clamp-2">
            {experiment.description || "No description provided"}
          </p>
          <div className="mt-4 flex items-center gap-4 text-sm text-muted-foreground">
            <span>User: {experiment.userName}</span>
            {experiment.projectId && (
              <span>Project: {experiment.projectId.substring(0, 8)}...</span>
            )}
          </div>
        </CardContent>
      </Card>
    </Link>
  );
}
