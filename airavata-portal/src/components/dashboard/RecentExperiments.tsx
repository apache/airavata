"use client";

import { Fragment, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { FlaskConical, FolderKanban, MoreVertical, Eye, Pencil, Trash2, StopCircle, ChevronRight, Loader2, CheckCircle, XCircle } from "lucide-react";
import { Skeleton } from "@/components/ui/skeleton";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import type { ExperimentModel, Project } from "@/types";
import { ExperimentState } from "@/types";
import { getExperimentStatusColor, isTerminalState } from "@/lib/utils";
import { getExperimentPermalink } from "@/lib/permalink";
import { cn } from "@/lib/utils";

interface RecentExperimentsProps {
  experiments?: ExperimentModel[];
  projects?: Project[];
  isLoading?: boolean;
  onDelete?: (experiment: ExperimentModel) => void;
}

interface ProjectGroup {
  project: Project | null;
  projectId: string;
  experiments: ExperimentModel[];
}

export function RecentExperiments({ experiments = [], projects = [], isLoading, onDelete }: RecentExperimentsProps) {
  const router = useRouter();
  const [collapsedProjects, setCollapsedProjects] = useState<Set<string>>(new Set());

  const projectMap = useMemo(() => {
    const map: Record<string, Project> = {};
    projects.forEach((p) => { map[p.projectID] = p; });
    return map;
  }, [projects]);

  const projectGroups = useMemo<ProjectGroup[]>(() => {
    if (!experiments.length) return [];

    const grouped: Record<string, ExperimentModel[]> = {};
    experiments.forEach((exp) => {
      const projectId = exp.projectId || "unassigned";
      if (!grouped[projectId]) grouped[projectId] = [];
      grouped[projectId].push(exp);
    });

    Object.values(grouped).forEach((exps) => {
      exps.sort((a, b) => (b.creationTime || 0) - (a.creationTime || 0));
    });

    const sortedGroupIds = Object.keys(grouped).sort((a, b) => {
      const aTime = grouped[a][0]?.creationTime || 0;
      const bTime = grouped[b][0]?.creationTime || 0;
      return bTime - aTime;
    });

    return sortedGroupIds.map((projectId) => ({
      project: projectMap[projectId] || null,
      projectId,
      experiments: grouped[projectId],
    }));
  }, [experiments, projectMap]);

  const toggleProject = (projectId: string) => {
    setCollapsedProjects((prev) => {
      const next = new Set(prev);
      if (next.has(projectId)) next.delete(projectId);
      else next.add(projectId);
      return next;
    });
  };

  if (isLoading) {
    return (
      <div className="space-y-2">
        {[...Array(5)].map((_, i) => (
          <Skeleton key={i} className="h-10 w-full" />
        ))}
      </div>
    );
  }

  return (
    <div className="border rounded-lg overflow-hidden">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead className="h-9 px-3">Name</TableHead>
            <TableHead className="h-9 px-3">Status</TableHead>
            <TableHead className="h-9 px-3 text-right">Actions</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {projectGroups.length === 0 ? (
            <TableRow>
              <TableCell colSpan={3} className="text-center py-8">
                <div className="flex flex-col items-center gap-3">
                  <FlaskConical className="h-10 w-10 text-muted-foreground" />
                  <p className="text-sm text-muted-foreground">No experiments found</p>
                </div>
              </TableCell>
            </TableRow>
          ) : (
            projectGroups.map((group) => {
              const isCollapsed = collapsedProjects.has(group.projectId);
              const hasProject = group.project !== null;

              return (
                <Fragment key={group.projectId}>
                  {hasProject && (
                    <TableRow
                      key={`project-${group.projectId}`}
                      className="bg-muted hover:bg-muted cursor-pointer"
                      onClick={() => toggleProject(group.projectId)}
                    >
                      <TableCell className="py-1.5 px-3">
                        <div className="flex items-center gap-2">
                          <ChevronRight className={cn(
                            "h-3.5 w-3.5 text-muted-foreground transition-transform",
                            !isCollapsed && "rotate-90"
                          )} />
                          <FolderKanban className="h-4 w-4 text-muted-foreground" />
                          <span className="text-xs font-semibold uppercase tracking-wide">
                            {group.project!.name}
                          </span>
                        </div>
                      </TableCell>
                      <TableCell className="py-1.5 px-3" />
                      <TableCell className="py-1.5 px-3 text-right">
                        <div className="flex items-center justify-end gap-2 text-xs">
                          {(() => {
                            const running = group.experiments.filter((e) => e.experimentStatus?.[0]?.state === ExperimentState.EXECUTING).length;
                            const completed = group.experiments.filter((e) => e.experimentStatus?.[0]?.state === ExperimentState.COMPLETED).length;
                            const failed = group.experiments.filter((e) => e.experimentStatus?.[0]?.state === ExperimentState.FAILED).length;
                            return (
                              <>
                                <span className="inline-flex items-center gap-0.5 rounded-full px-2 py-0.5 bg-amber-50 text-amber-700 border border-amber-200">
                                  <Loader2 className="h-3 w-3" />{running}
                                </span>
                                <span className="inline-flex items-center gap-0.5 rounded-full px-2 py-0.5 bg-emerald-50 text-emerald-700 border border-emerald-200">
                                  <CheckCircle className="h-3 w-3" />{completed}
                                </span>
                                <span className="inline-flex items-center gap-0.5 rounded-full px-2 py-0.5 bg-red-50 text-red-700 border border-red-200">
                                  <XCircle className="h-3 w-3" />{failed}
                                </span>
                              </>
                            );
                          })()}
                        </div>
                      </TableCell>
                    </TableRow>
                  )}
                  {!isCollapsed && group.experiments.map((experiment) => {
                    const status = experiment.experimentStatus?.[0]?.state || "UNKNOWN";
                    const canEdit = status === ExperimentState.CREATED;
                    const canCancel = [ExperimentState.EXECUTING, ExperimentState.LAUNCHED, ExperimentState.SCHEDULED].includes(status as ExperimentState);
                    const experimentPermalink = getExperimentPermalink(experiment.experimentId);

                    return (
                      <TableRow
                        key={experiment.experimentId}
                        className="hover:bg-muted/50 cursor-pointer"
                        onClick={() => router.push(experimentPermalink)}
                      >
                        <TableCell className="py-1.5 pr-3 pl-9">
                          <div className="font-medium text-sm">
                            {experiment.experimentName}
                          </div>
                        </TableCell>
                        <TableCell className="py-1.5 px-3">
                          <Badge variant="outline" className={`${getExperimentStatusColor(status)} text-xs`}>
                            {status}
                          </Badge>
                        </TableCell>
                        <TableCell className="py-1.5 px-3 text-right">
                          <DropdownMenu>
                            <DropdownMenuTrigger asChild onClick={(e) => e.stopPropagation()}>
                              <Button variant="ghost" size="icon" className="h-8 w-8">
                                <MoreVertical className="h-4 w-4" />
                              </Button>
                            </DropdownMenuTrigger>
                            <DropdownMenuContent align="end">
                              <DropdownMenuItem onClick={() => router.push(experimentPermalink)}>
                                <Eye className="mr-2 h-4 w-4" />
                                View Details
                              </DropdownMenuItem>
                              {canEdit && (
                                <DropdownMenuItem onClick={() => router.push(`${experimentPermalink}/edit`)}>
                                  <Pencil className="mr-2 h-4 w-4" />
                                  Edit
                                </DropdownMenuItem>
                              )}
                              {canCancel && (
                                <DropdownMenuItem onClick={(e) => {
                                  e.preventDefault();
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
                        </TableCell>
                      </TableRow>
                    );
                  })}
                </Fragment>
              );
            })
          )}
        </TableBody>
      </Table>
    </div>
  );
}
