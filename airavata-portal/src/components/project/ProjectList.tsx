"use client";

import { FolderKanban } from "lucide-react";
import { ProjectCard } from "./ProjectCard";
import { Skeleton } from "@/components/ui/skeleton";
import type { Project } from "@/types";

interface ProjectListProps {
  projects?: Project[];
  isLoading?: boolean;
  onEdit?: (project: Project) => void;
  onDelete?: (project: Project) => void;
}

export function ProjectList({ projects = [], isLoading, onEdit, onDelete }: ProjectListProps) {
  if (isLoading) {
    return (
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        {[...Array(6)].map((_, i) => (
          <div key={i} className="rounded-xl border p-6 space-y-4">
            <div className="flex items-start gap-3">
              <Skeleton className="h-10 w-10 rounded-lg" />
              <div className="space-y-2">
                <Skeleton className="h-5 w-32" />
                <Skeleton className="h-4 w-24" />
              </div>
            </div>
            <Skeleton className="h-12 w-full" />
            <Skeleton className="h-4 w-20" />
          </div>
        ))}
      </div>
    );
  }

  if (projects.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-16 text-center">
        <FolderKanban className="h-16 w-16 text-muted-foreground mb-4" />
        <h3 className="text-lg font-semibold">No projects found</h3>
        <p className="text-muted-foreground mt-1">
          Create your first project to start organizing experiments
        </p>
      </div>
    );
  }

  return (
    <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
      {projects.map((project) => (
        <ProjectCard
          key={project.projectID}
          project={project}
          onEdit={onEdit}
          onDelete={onDelete}
        />
      ))}
    </div>
  );
}
