"use client";

import { File, Download, ExternalLink } from "lucide-react";
import { Button } from "@/components/ui/button";
import type { OutputDataObjectType } from "@/types";
import { DataType } from "@/types";

interface DefaultOutputDisplayProps {
  output: OutputDataObjectType;
}

export function DefaultOutputDisplay({ output }: DefaultOutputDisplayProps) {
  const isFile = output.type === DataType.URI || output.type === DataType.URI_COLLECTION;
  const isStdout = output.type === DataType.STDOUT;
  const isStderr = output.type === DataType.STDERR;

  if (!output.value) {
    return (
      <div className="border rounded-lg p-4 bg-muted/50">
        <div className="flex items-center gap-2 mb-2">
          <File className="h-4 w-4 text-muted-foreground" />
          <span className="font-medium">{output.name}</span>
        </div>
        <p className="text-sm text-muted-foreground">No output available yet</p>
      </div>
    );
  }

  return (
    <div className="border rounded-lg p-4">
      <div className="flex items-center justify-between mb-2">
        <div className="flex items-center gap-2">
          <File className="h-4 w-4 text-muted-foreground" />
          <span className="font-medium">{output.name}</span>
        </div>
        {isFile && (
          <div className="flex items-center gap-2">
            <Button variant="outline" size="sm">
              <Download className="h-4 w-4 mr-2" />
              Download
            </Button>
            <Button variant="ghost" size="sm">
              <ExternalLink className="h-4 w-4" />
            </Button>
          </div>
        )}
      </div>
      
      {(isStdout || isStderr) ? (
        <pre className={`text-sm p-3 rounded ${isStderr ? "bg-red-50 text-red-800" : "bg-gray-100"} overflow-x-auto max-h-64 overflow-y-auto`}>
          {output.value}
        </pre>
      ) : isFile ? (
        <p className="text-sm text-muted-foreground break-all">{output.value}</p>
      ) : (
        <div className="text-sm bg-muted p-2 rounded font-mono">
          {output.value}
        </div>
      )}
    </div>
  );
}
