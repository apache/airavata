"use client";

import { ApplicationList } from "@/components/application";

export default function ApplicationsPage() {
  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Applications</h1>
        <p className="text-muted-foreground">
          Browse available applications and create new experiments
        </p>
      </div>

      <ApplicationList />
    </div>
  );
}
