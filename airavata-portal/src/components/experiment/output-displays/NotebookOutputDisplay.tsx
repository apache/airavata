"use client";

import { useEffect, useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import type { OutputDataObjectType } from "@/types";

interface Props {
  output: OutputDataObjectType;
}

export function NotebookOutputDisplay({ output }: Props) {
  const [notebookHtml, setNotebookHtml] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const loadNotebook = async () => {
      if (!output.value) {
        setError("No notebook path provided");
        setIsLoading(false);
        return;
      }

      try {
        // Fetch the notebook HTML from the API
        const response = await fetch(`/api/notebook-output?path=${encodeURIComponent(output.value)}`);
        if (!response.ok) {
          throw new Error("Failed to load notebook");
        }
        const html = await response.text();
        setNotebookHtml(html);
      } catch (err) {
        setError(err instanceof Error ? err.message : "Failed to load notebook");
      } finally {
        setIsLoading(false);
      }
    };

    loadNotebook();
  }, [output.value]);

  if (isLoading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>{output.name}</CardTitle>
        </CardHeader>
        <CardContent>
          <Skeleton className="h-[400px] w-full" />
        </CardContent>
      </Card>
    );
  }

  if (error) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>{output.name}</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="text-center text-destructive py-8">
            <p>Failed to load notebook</p>
            <p className="text-sm mt-2">{error}</p>
          </div>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>{output.name}</CardTitle>
      </CardHeader>
      <CardContent>
        {notebookHtml ? (
          <div
            className="notebook-output"
            dangerouslySetInnerHTML={{ __html: notebookHtml }}
          />
        ) : (
          <div className="text-center text-muted-foreground py-8">
            No notebook content available
          </div>
        )}
      </CardContent>
    </Card>
  );
}
