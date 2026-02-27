"use client";

import { useEffect, useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { Button } from "@/components/ui/button";
import { ExternalLink } from "lucide-react";
import type { OutputDataObjectType } from "@/types";

interface Props {
  output: OutputDataObjectType;
}

export function HTMLOutputDisplay({ output }: Props) {
  const [htmlContent, setHtmlContent] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const loadHtml = async () => {
      if (!output.value) {
        setError("No HTML path provided");
        setIsLoading(false);
        return;
      }

      try {
        // Fetch the HTML content from the API
        const response = await fetch(`/api/html-output?path=${encodeURIComponent(output.value)}`);
        if (!response.ok) {
          throw new Error("Failed to load HTML");
        }
        const html = await response.text();
        setHtmlContent(html);
      } catch (err) {
        setError(err instanceof Error ? err.message : "Failed to load HTML");
      } finally {
        setIsLoading(false);
      }
    };

    loadHtml();
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
            <p>Failed to load HTML</p>
            <p className="text-sm mt-2">{error}</p>
          </div>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between">
        <CardTitle>{output.name}</CardTitle>
        {output.value && (
          <Button
            variant="outline"
            size="sm"
            onClick={() => window.open(`/api/html-output?path=${encodeURIComponent(output.value!)}`, "_blank")}
          >
            <ExternalLink className="mr-2 h-4 w-4" />
            Open in New Tab
          </Button>
        )}
      </CardHeader>
      <CardContent>
        {htmlContent ? (
          <div
            className="html-output border rounded p-4 max-h-[600px] overflow-auto"
            dangerouslySetInnerHTML={{ __html: htmlContent }}
          />
        ) : (
          <div className="text-center text-muted-foreground py-8">
            No HTML content available
          </div>
        )}
      </CardContent>
    </Card>
  );
}
