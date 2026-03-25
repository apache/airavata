"use client";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { ExternalLink } from "lucide-react";
import type { OutputDataObjectType } from "@/types";

interface Props {
  output: OutputDataObjectType;
}

export function LinkOutputDisplay({ output }: Props) {
  if (!output.value) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>{output.name}</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="text-center text-muted-foreground py-8">
            No link available
          </div>
        </CardContent>
      </Card>
    );
  }

  const isValidUrl = (url: string) => {
    try {
      new URL(url);
      return true;
    } catch {
      return false;
    }
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle>{output.name}</CardTitle>
      </CardHeader>
      <CardContent>
        <div className="space-y-4">
          <p className="text-sm text-muted-foreground">
            {output.metaData || "Output link"}
          </p>
          <div className="flex items-center gap-4 p-4 border rounded-lg bg-muted/50">
            <div className="flex-1 font-mono text-sm break-all">
              {output.value}
            </div>
            {isValidUrl(output.value) && (
              <Button
                onClick={() => window.open(output.value, "_blank")}
                size="sm"
              >
                <ExternalLink className="mr-2 h-4 w-4" />
                Open
              </Button>
            )}
          </div>
        </div>
      </CardContent>
    </Card>
  );
}
