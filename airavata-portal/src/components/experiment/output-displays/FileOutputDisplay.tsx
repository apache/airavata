"use client";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Download, Eye } from "lucide-react";
import type { OutputDataObjectType } from "@/types";

interface Props {
  output: OutputDataObjectType;
}

export function FileOutputDisplay({ output }: Props) {
  if (!output.value) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>{output.name}</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="text-center text-muted-foreground py-8">
            No file available
          </div>
        </CardContent>
      </Card>
    );
  }

  const fileName = output.value.split("/").pop() || output.value;
  const fileExtension = fileName.split(".").pop()?.toLowerCase();
  const isImage = ["png", "jpg", "jpeg", "gif", "bmp", "svg", "webp"].includes(fileExtension || "");
  const isText = ["txt", "log", "csv", "json", "xml", "md"].includes(fileExtension || "");

  const handleDownload = () => {
    window.open(`/api/download?path=${encodeURIComponent(output.value!)}`, "_blank");
  };

  const handlePreview = () => {
    window.open(`/api/download?path=${encodeURIComponent(output.value!)}&inline=true`, "_blank");
  };

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between">
        <CardTitle>{output.name}</CardTitle>
        <div className="flex gap-2">
          {(isImage || isText) && (
            <Button
              variant="outline"
              size="sm"
              onClick={handlePreview}
            >
              <Eye className="mr-2 h-4 w-4" />
              Preview
            </Button>
          )}
          <Button
            variant="outline"
            size="sm"
            onClick={handleDownload}
          >
            <Download className="mr-2 h-4 w-4" />
            Download
          </Button>
        </div>
      </CardHeader>
      <CardContent>
        <div className="space-y-4">
          <div className="flex items-center gap-4 p-4 border rounded-lg bg-muted/50">
            <div className="flex-1">
              <p className="font-medium">{fileName}</p>
              <p className="text-sm text-muted-foreground mt-1 font-mono break-all">
                {output.value}
              </p>
            </div>
          </div>
          {isImage && (
            <div className="border rounded-lg p-4">
              <img
                src={`/api/download?path=${encodeURIComponent(output.value)}&inline=true`}
                alt={fileName}
                className="max-w-full h-auto"
                onError={(e) => {
                  (e.target as HTMLImageElement).style.display = "none";
                }}
              />
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  );
}
