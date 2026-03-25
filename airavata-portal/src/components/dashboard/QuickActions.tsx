"use client";

import Link from "next/link";
import { Plus, FolderPlus, Upload, Settings } from "lucide-react";
import { cn } from "@/lib/utils";
import { useCreateExperimentModal } from "@/contexts/CreateExperimentModalContext";

interface QuickActionsProps {
  gatewayName: string;
  onCreateProject?: () => void;
}

export function QuickActions({ gatewayName, onCreateProject }: QuickActionsProps) {
  const prefix = `/${gatewayName}`;
  const { openModal } = useCreateExperimentModal();

  const handleCreateExperiment = (e: React.MouseEvent) => {
    e.preventDefault();
    openModal();
  };

  const handleCreateProject = (e: React.MouseEvent) => {
    e.preventDefault();
    if (onCreateProject) {
      onCreateProject();
    } else {
      window.location.href = `${prefix}?action=new`;
    }
  };

  const actions = [
    {
      title: "Create Experiment",
      icon: Plus,
      href: `${prefix}/catalog`,
      onClick: handleCreateExperiment,
    },
    {
      title: "New Project",
      icon: FolderPlus,
      href: `${prefix}?action=new`,
      onClick: handleCreateProject,
    },
    {
      title: "Upload Files",
      icon: Upload,
      href: `${prefix}/storage`,
    },
    {
      title: "Account",
      icon: Settings,
      href: "/account",
    },
  ];

  return (
    <div className="flex items-center justify-center gap-2 flex-wrap">
      {actions.map((action) => (
        <Link
          key={action.title}
          href={action.href}
          onClick={action.onClick}
          className={cn(
            "inline-flex items-center gap-2 px-4 py-2 rounded-full text-sm font-medium transition-colors",
            "bg-muted text-foreground hover:bg-muted/70"
          )}
        >
          <action.icon className="h-4 w-4" />
          {action.title}
        </Link>
      ))}
    </div>
  );
}
