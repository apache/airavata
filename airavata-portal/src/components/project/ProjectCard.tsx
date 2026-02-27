"use client";

import Link from "next/link";
import { FolderKanban, MoreVertical, Pencil, Trash2, User } from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { useSession } from "next-auth/react";
import { GatewayBadge } from "@/components/gateway/GatewayBadge";
import type { Project } from "@/types";
import { formatDate } from "@/lib/utils";

interface ProjectCardProps {
  project: Project;
  onEdit?: (project: Project) => void;
  onDelete?: (project: Project) => void;
}

export function ProjectCard({ project, onEdit, onDelete }: ProjectCardProps) {
  const { data: session } = useSession();
  
  // Get owner from project or fallback to session user
  const owner = project.owner || session?.user?.email || session?.user?.name || "Unknown";
  
  return (
    <Link href={`/projects/${project.projectID}`}>
      <Card className="cursor-pointer transition-shadow hover:shadow-md h-full">
        <CardHeader className="flex flex-row items-start justify-between space-y-0">
          <div className="flex items-start gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-purple-100">
              <FolderKanban className="h-5 w-5 text-purple-600" />
            </div>
            <div>
              <CardTitle className="text-lg">
                {project.name}
              </CardTitle>
              <CardDescription className="mt-1">
                Created {formatDate(project.creationTime)}
              </CardDescription>
            </div>
          </div>
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" size="icon" onClick={(e) => e.preventDefault()}>
                <MoreVertical className="h-4 w-4" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuItem onClick={(e) => {
                e.preventDefault();
                onEdit?.(project);
              }}>
                <Pencil className="mr-2 h-4 w-4" />
                Edit
              </DropdownMenuItem>
              <DropdownMenuItem
                className="text-red-600"
                onClick={(e) => {
                  e.preventDefault();
                  onDelete?.(project);
                }}
              >
                <Trash2 className="mr-2 h-4 w-4" />
                Delete
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </CardHeader>
        <CardContent>
          {/* Show gateway badge */}
          {project.gatewayId && <GatewayBadge gatewayId={project.gatewayId} className="mb-2" />}
          <p className="text-sm text-muted-foreground line-clamp-2">
            {project.description || "No description provided"}
          </p>
          <div className="mt-4 flex items-center gap-2 text-sm text-muted-foreground">
            <User className="h-4 w-4" />
            <span>Owner: {owner}</span>
          </div>
        </CardContent>
      </Card>
    </Link>
  );
}
