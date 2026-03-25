"use client";

import { useMemo, useState } from "react";
import { Briefcase, ChevronDown, ChevronRight } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { cn } from "@/lib/utils";
import {
  ProcessState,
  ExperimentState,
  type ProcessModel,
  type ProcessStatus,
  type JobModel,
  type ExperimentStatus as ExperimentStatusType,
} from "@/types";
import { formatDuration } from "@/lib/utils";
import { Skeleton } from "@/components/ui/skeleton";
import { useExperimentJobs } from "@/hooks";
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from "@/components/ui/tooltip";

// ─── Canonical orderings ────────────────────────────────────────────────────

const EXPERIMENT_STATE_ORDER: string[] = [
  ExperimentState.CREATED,
  ExperimentState.VALIDATED,
  ExperimentState.SCHEDULED,
  ExperimentState.LAUNCHED,
  ExperimentState.EXECUTING,
  ExperimentState.CANCELING,
  ExperimentState.CANCELED,
  ExperimentState.COMPLETED,
  ExperimentState.FAILED,
];

const PROCESS_STATE_ORDER: string[] = [
  ProcessState.CREATED,
  ProcessState.VALIDATED,
  ProcessState.STARTED,
  ProcessState.PRE_PROCESSING,
  ProcessState.CONFIGURING_WORKSPACE,
  ProcessState.INPUT_DATA_STAGING,
  ProcessState.EXECUTING,
  ProcessState.MONITORING,
  ProcessState.OUTPUT_DATA_STAGING,
  ProcessState.POST_PROCESSING,
  ProcessState.COMPLETED,
  ProcessState.FAILED,
  ProcessState.CANCELLING,
  ProcessState.CANCELED,
];

// ─── Colors ─────────────────────────────────────────────────────────────────

const dotColor: Record<string, string> = {
  CREATED: "bg-slate-400",
  VALIDATED: "bg-blue-400",
  SCHEDULED: "bg-blue-500",
  LAUNCHED: "bg-indigo-500",
  EXECUTING: "bg-amber-500",
  CANCELING: "bg-orange-500",
  CANCELED: "bg-slate-500",
  COMPLETED: "bg-emerald-500",
  FAILED: "bg-red-500",
  STARTED: "bg-blue-500",
  PRE_PROCESSING: "bg-indigo-400",
  CONFIGURING_WORKSPACE: "bg-indigo-500",
  INPUT_DATA_STAGING: "bg-violet-500",
  MONITORING: "bg-amber-500",
  OUTPUT_DATA_STAGING: "bg-cyan-500",
  POST_PROCESSING: "bg-cyan-500",
  CANCELLING: "bg-orange-500",
};

const segmentBg: Record<string, string> = {
  CREATED: "bg-slate-200",
  VALIDATED: "bg-blue-200",
  SCHEDULED: "bg-blue-300",
  LAUNCHED: "bg-indigo-200",
  STARTED: "bg-blue-300",
  PRE_PROCESSING: "bg-indigo-200",
  CONFIGURING_WORKSPACE: "bg-indigo-300",
  INPUT_DATA_STAGING: "bg-violet-300",
  EXECUTING: "bg-amber-300",
  MONITORING: "bg-amber-200",
  OUTPUT_DATA_STAGING: "bg-cyan-300",
  POST_PROCESSING: "bg-cyan-200",
  COMPLETED: "bg-emerald-300",
  FAILED: "bg-red-300",
  CANCELLING: "bg-orange-200",
  CANCELED: "bg-slate-300",
  CANCELING: "bg-orange-200",
};

const textColor: Record<string, string> = {
  CREATED: "text-slate-600",
  VALIDATED: "text-blue-600",
  SCHEDULED: "text-blue-600",
  LAUNCHED: "text-indigo-600",
  EXECUTING: "text-amber-700",
  CANCELING: "text-orange-600",
  CANCELED: "text-slate-600",
  COMPLETED: "text-emerald-700",
  FAILED: "text-red-600",
};

// ─── Helpers ────────────────────────────────────────────────────────────────

function formatTime(ts: number | undefined): string {
  if (!ts) return "";
  const d = new Date(ts);
  return d.toLocaleTimeString("en-US", { hour: "2-digit", minute: "2-digit", second: "2-digit" });
}

function formatStateLabel(state: string): string {
  return state
    .replace(/_/g, " ")
    .toLowerCase()
    .replace(/\b\w/g, (c) => c.toUpperCase());
}

function dedup<T extends { state: string; timeOfStateChange?: number }>(
  items: T[],
  canonicalOrder: string[],
): T[] {
  const seen = new Map<string, T>();
  const sorted = [...items].sort((a, b) => (a.timeOfStateChange || 0) - (b.timeOfStateChange || 0));
  for (const s of sorted) {
    if (!seen.has(s.state)) {
      seen.set(s.state, s);
    }
  }
  const orderMap = new Map(canonicalOrder.map((s, i) => [s, i]));
  const entries = Array.from(seen.values());
  entries.sort((a, b) => {
    const aO = orderMap.get(a.state) ?? 999;
    const bO = orderMap.get(b.state) ?? 999;
    if (aO !== bO) return aO - bO;
    return (a.timeOfStateChange || 0) - (b.timeOfStateChange || 0);
  });
  return entries;
}

// ─── ExecutionTimeline ──────────────────────────────────────────────────────

interface ExecutionTimelineProps {
  experimentStatuses: ExperimentStatusType[];
  processes: ProcessModel[];
  isLoading?: boolean;
}

export function ExecutionTimeline({ experimentStatuses, processes, isLoading }: ExecutionTimelineProps) {
  const expStatuses = useMemo(
    () => dedup(experimentStatuses, EXPERIMENT_STATE_ORDER),
    [experimentStatuses],
  );

  if (isLoading) {
    return <Skeleton className="h-32 w-full" />;
  }

  if (expStatuses.length === 0) {
    return <div className="text-center py-8 text-muted-foreground">No status data yet</div>;
  }

  const latestState = expStatuses[expStatuses.length - 1].state;
  const isTerminal = ["COMPLETED", "FAILED", "CANCELED"].includes(latestState);
  const isActive = !isTerminal;

  const sortedProcesses = [...processes].sort((a, b) => (a.creationTime || 0) - (b.creationTime || 0));

  return (
    <TooltipProvider delayDuration={150}>
      <div className="space-y-5">
        {/* ── Experiment milestone row ── */}
        <div className="flex items-start">
          {expStatuses.map((s, i) => {
            const isLast = i === expStatuses.length - 1;
            const dot = dotColor[s.state] || "bg-slate-400";
            const txt = textColor[s.state] || "text-slate-600";
            const pulsing = isLast && isActive;

            return (
              <div key={s.statusId || i} className="flex items-start flex-1 min-w-0 last:flex-none">
                {/* Node + label */}
                <Tooltip>
                  <TooltipTrigger asChild>
                    <div className="flex flex-col items-center gap-1 cursor-default">
                      <div className={cn(
                        "rounded-full shrink-0 border-2 border-background shadow-sm",
                        pulsing ? "h-4 w-4 ring-2 ring-offset-1 ring-amber-300 animate-pulse" : "h-3 w-3",
                        dot,
                      )} />
                      <span className={cn("text-[10px] font-medium whitespace-nowrap leading-tight", txt)}>
                        {formatStateLabel(s.state)}
                      </span>
                    </div>
                  </TooltipTrigger>
                  <TooltipContent side="top" className="text-xs">
                    <p className="font-medium">{formatStateLabel(s.state)}</p>
                    {s.timeOfStateChange && <p className="text-muted-foreground">{formatTime(s.timeOfStateChange)}</p>}
                    {s.reason && <p className="text-muted-foreground">{s.reason}</p>}
                  </TooltipContent>
                </Tooltip>

                {/* Connector line (not after last node) */}
                {!isLast && (
                  <div className="flex-1 flex items-center px-1 mt-[5px]">
                    <div className={cn("h-0.5 w-full rounded-full", i < expStatuses.length - 2 ? "bg-slate-300" : (isActive ? "bg-amber-200" : "bg-slate-300"))} />
                  </div>
                )}
              </div>
            );
          })}
        </div>

        {/* ── Process Gantt bars ── */}
        {sortedProcesses.map((proc) => (
          <ProcessBarRow key={proc.processId} process={proc} />
        ))}
      </div>
    </TooltipProvider>
  );
}

// ─── Process bar ────────────────────────────────────────────────────────────

function ProcessBarRow({ process }: { process: ProcessModel }) {
  const [expanded, setExpanded] = useState(false);
  const statuses = useMemo(
    () => dedup(process.processStatuses || [], PROCESS_STATE_ORDER),
    [process.processStatuses],
  );

  if (statuses.length === 0) return null;

  const latestState = statuses[statuses.length - 1].state;
  const isFailed = ["FAILED", "CANCELED"].includes(latestState);
  const isActive = !["COMPLETED", "FAILED", "CANCELED"].includes(latestState);

  // Compute time-weighted segment widths. If all timestamps are the same, use equal widths.
  const segments = useMemo(() => {
    if (statuses.length <= 1) {
      return statuses.map((s) => ({ ...s, weight: 1 }));
    }
    const startT = statuses[0].timeOfStateChange || 0;
    const endT = statuses[statuses.length - 1].timeOfStateChange || startT;
    const totalTime = endT - startT;

    return statuses.map((s, i) => {
      if (totalTime <= 0) {
        // All at same time: equal widths
        return { ...s, weight: 1 };
      }
      const segStart = s.timeOfStateChange || startT;
      const segEnd = i < statuses.length - 1
        ? (statuses[i + 1].timeOfStateChange || segStart)
        : endT;
      const duration = segEnd - segStart;
      // Give each segment a minimum weight so tiny phases are visible
      return { ...s, weight: Math.max(duration / totalTime, 0.08) };
    });
  }, [statuses]);

  const totalWeight = segments.reduce((sum, s) => sum + s.weight, 0);

  // Duration
  const startT = statuses[0].timeOfStateChange || process.creationTime;
  const endT = statuses[statuses.length - 1].timeOfStateChange || startT;
  const durationSecs = startT && endT ? Math.round((endT - startT) / 1000) : 0;

  const label = process.processType === "APPLICATION_RUN" ? "Application" : (process.processType || "Execution");

  return (
    <div>
      {/* Label + bar */}
      <button
        type="button"
        onClick={() => setExpanded(!expanded)}
        className="w-full text-left group"
      >
        <div className="flex items-center gap-2 mb-1">
          {expanded
            ? <ChevronDown className="h-3 w-3 text-muted-foreground shrink-0" />
            : <ChevronRight className="h-3 w-3 text-muted-foreground shrink-0" />
          }
          <span className="text-xs text-muted-foreground font-medium">{label}</span>
          {durationSecs > 0 && (
            <span className="text-[10px] text-muted-foreground/70">{formatDuration(durationSecs)}</span>
          )}
          {isFailed && (
            <Badge variant="secondary" className="text-[10px] px-1.5 py-0 bg-red-100 text-red-700">
              {formatStateLabel(latestState)}
            </Badge>
          )}
          {isActive && (
            <Badge variant="secondary" className="text-[10px] px-1.5 py-0 bg-amber-100 text-amber-700">
              {formatStateLabel(latestState)}
            </Badge>
          )}
        </div>

        {/* Segmented bar */}
        <div className={cn(
          "flex h-5 rounded overflow-hidden group-hover:opacity-90 transition-opacity",
          isFailed ? "ring-1 ring-red-200" : "ring-1 ring-slate-200",
        )}>
          {segments.map((seg, i) => {
            const bg = segmentBg[seg.state] || "bg-slate-200";
            const widthPct = (seg.weight / totalWeight) * 100;
            return (
              <Tooltip key={i}>
                <TooltipTrigger asChild>
                  <div
                    className={cn(
                      "h-full flex items-center justify-center overflow-hidden border-r last:border-r-0 border-white/40",
                      bg,
                    )}
                    style={{ width: `${widthPct}%` }}
                  >
                    {widthPct > 12 && (
                      <span className="text-[9px] font-medium text-slate-700/70 truncate px-1">
                        {formatStateLabel(seg.state)}
                      </span>
                    )}
                  </div>
                </TooltipTrigger>
                <TooltipContent side="top" className="text-xs">
                  <p className="font-medium">{formatStateLabel(seg.state)}</p>
                  {seg.timeOfStateChange && <p className="text-muted-foreground">{formatTime(seg.timeOfStateChange)}</p>}
                  {seg.reason && <p className="text-muted-foreground">{seg.reason}</p>}
                </TooltipContent>
              </Tooltip>
            );
          })}
        </div>
      </button>

      {/* Expanded details */}
      {expanded && (
        <div className="mt-2 mb-1 border rounded-lg p-3 space-y-3 bg-muted/30">
          {/* Status table */}
          <table className="w-full text-xs">
            <thead>
              <tr className="border-b text-muted-foreground">
                <th className="text-left py-1 font-medium">State</th>
                <th className="text-left py-1 font-medium">Time</th>
                <th className="text-left py-1 font-medium">Reason</th>
              </tr>
            </thead>
            <tbody>
              {statuses.map((s, i) => {
                const bg = segmentBg[s.state] || "bg-slate-200";
                return (
                  <tr key={s.statusId || i} className="border-b last:border-0">
                    <td className="py-1">
                      <div className="flex items-center gap-1.5">
                        <div className={cn("h-2 w-2 rounded-sm shrink-0", bg)} />
                        <span className="font-medium">{formatStateLabel(s.state)}</span>
                      </div>
                    </td>
                    <td className="py-1 text-muted-foreground font-mono">{formatTime(s.timeOfStateChange)}</td>
                    <td className="py-1 text-muted-foreground">{s.reason || "-"}</td>
                  </tr>
                );
              })}
            </tbody>
          </table>

          {/* Jobs */}
          <JobsSection processId={process.processId} />

          {/* Errors */}
          {process.processErrors && process.processErrors.length > 0 && (
            <div className="space-y-1">
              <h4 className="text-xs font-medium text-red-600 uppercase tracking-wide">Errors</h4>
              {process.processErrors.map((err, i) => (
                <div key={i} className="text-xs border border-red-200 bg-red-50 rounded p-2">
                  <p className="font-medium text-red-800">{err.userFriendlyMessage || "Error"}</p>
                  {err.actualErrorMessage && (
                    <pre className="mt-1 text-red-700 whitespace-pre-wrap">{err.actualErrorMessage}</pre>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
}

// ─── Jobs ───────────────────────────────────────────────────────────────────

function JobsSection({ processId }: { processId: string }) {
  const { data: jobs, isLoading } = useExperimentJobs(processId);

  if (isLoading) return <Skeleton className="h-10 w-full" />;
  if (!jobs || jobs.length === 0) return null;

  return (
    <div className="space-y-1">
      <h4 className="text-xs font-medium text-muted-foreground uppercase tracking-wide">Jobs</h4>
      {jobs.map((job) => (
        <JobRow key={job.jobId} job={job} />
      ))}
    </div>
  );
}

function JobRow({ job }: { job: JobModel }) {
  const [showDetails, setShowDetails] = useState(false);
  const latestJobState = job.jobStatuses?.[0]?.jobState || "UNKNOWN";

  const jobStateColors: Record<string, string> = {
    SUBMITTED: "bg-blue-100 text-blue-700",
    QUEUED: "bg-amber-100 text-amber-700",
    ACTIVE: "bg-indigo-100 text-indigo-700",
    COMPLETE: "bg-emerald-100 text-emerald-700",
    CANCELED: "bg-slate-100 text-slate-700",
    FAILED: "bg-red-100 text-red-700",
    SUSPENDED: "bg-orange-100 text-orange-700",
    UNKNOWN: "bg-slate-100 text-slate-700",
  };

  return (
    <div className="border rounded p-2 text-xs bg-background">
      <button
        type="button"
        onClick={() => setShowDetails(!showDetails)}
        className="w-full flex items-center justify-between"
      >
        <div className="flex items-center gap-2">
          <Briefcase className="h-3 w-3 text-muted-foreground" />
          <span className="font-medium">{job.jobName || job.jobId.substring(0, 16)}</span>
        </div>
        <Badge variant="secondary" className={cn("text-xs", jobStateColors[latestJobState] || "")}>
          {latestJobState}
        </Badge>
      </button>

      {showDetails && (
        <div className="mt-2 space-y-2 pl-5">
          {job.stdOut && (
            <div>
              <span className="text-muted-foreground">stdout:</span>
              <pre className="mt-0.5 p-1.5 bg-muted rounded text-xs font-mono whitespace-pre-wrap">{job.stdOut}</pre>
            </div>
          )}
          {job.stdErr && (
            <div>
              <span className="text-red-600">stderr:</span>
              <pre className="mt-0.5 p-1.5 bg-red-50 rounded text-xs font-mono whitespace-pre-wrap text-red-700">{job.stdErr}</pre>
            </div>
          )}
          {job.exitCode !== undefined && job.exitCode !== null && (
            <div className="text-muted-foreground">
              Exit code: <span className={cn("font-mono font-medium", job.exitCode === 0 ? "text-emerald-600" : "text-red-600")}>{job.exitCode}</span>
            </div>
          )}
          {job.workingDir && (
            <div className="text-muted-foreground">
              Working dir: <span className="font-mono">{job.workingDir}</span>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
