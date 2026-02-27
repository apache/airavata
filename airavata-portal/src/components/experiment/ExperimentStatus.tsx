"use client";

import { CheckCircle, XCircle, Clock, Loader2, Ban, AlertTriangle } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { ExperimentState } from "@/types";
import { cn } from "@/lib/utils";

interface ExperimentStatusProps {
  status: string;
  size?: "sm" | "md" | "lg";
  showIcon?: boolean;
}

const statusConfig: Record<string, { icon: any; color: string; bgColor: string }> = {
  [ExperimentState.CREATED]: {
    icon: Clock,
    color: "text-gray-600",
    bgColor: "bg-gray-100",
  },
  [ExperimentState.VALIDATED]: {
    icon: CheckCircle,
    color: "text-blue-600",
    bgColor: "bg-blue-100",
  },
  [ExperimentState.SCHEDULED]: {
    icon: Clock,
    color: "text-blue-600",
    bgColor: "bg-blue-100",
  },
  [ExperimentState.LAUNCHED]: {
    icon: Loader2,
    color: "text-indigo-600",
    bgColor: "bg-indigo-100",
  },
  [ExperimentState.EXECUTING]: {
    icon: Loader2,
    color: "text-yellow-600",
    bgColor: "bg-yellow-100",
  },
  [ExperimentState.CANCELING]: {
    icon: Ban,
    color: "text-orange-600",
    bgColor: "bg-orange-100",
  },
  [ExperimentState.CANCELED]: {
    icon: Ban,
    color: "text-gray-600",
    bgColor: "bg-gray-100",
  },
  [ExperimentState.COMPLETED]: {
    icon: CheckCircle,
    color: "text-green-600",
    bgColor: "bg-green-100",
  },
  [ExperimentState.FAILED]: {
    icon: XCircle,
    color: "text-red-600",
    bgColor: "bg-red-100",
  },
};

export function ExperimentStatus({ status, size = "md", showIcon = true }: ExperimentStatusProps) {
  const config = statusConfig[status] || {
    icon: AlertTriangle,
    color: "text-gray-600",
    bgColor: "bg-gray-100",
  };
  const Icon = config.icon;
  const isAnimated = status === ExperimentState.EXECUTING || status === ExperimentState.LAUNCHED;

  const sizeClasses = {
    sm: "text-xs px-2 py-0.5",
    md: "text-sm px-2.5 py-0.5",
    lg: "text-base px-3 py-1",
  };

  const iconSizes = {
    sm: "h-3 w-3",
    md: "h-4 w-4",
    lg: "h-5 w-5",
  };

  return (
    <Badge
      variant="secondary"
      className={cn(
        "inline-flex items-center gap-1.5",
        config.bgColor,
        config.color,
        sizeClasses[size]
      )}
    >
      {showIcon && (
        <Icon className={cn(iconSizes[size], isAnimated && "animate-spin")} />
      )}
      {status}
    </Badge>
  );
}
