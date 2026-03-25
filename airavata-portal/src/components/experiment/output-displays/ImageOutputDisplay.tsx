"use client";

import { useState } from "react";
import { Image as ImageIcon, Download, Maximize2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import type { OutputDataObjectType } from "@/types";

interface ImageOutputDisplayProps {
  output: OutputDataObjectType;
}

export function ImageOutputDisplay({ output }: ImageOutputDisplayProps) {
  const [isFullscreen, setIsFullscreen] = useState(false);

  if (!output.value) {
    return (
      <div className="border rounded-lg p-4 bg-muted/50">
        <div className="flex items-center gap-2 mb-2">
          <ImageIcon className="h-4 w-4 text-muted-foreground" />
          <span className="font-medium">{output.name}</span>
        </div>
        <p className="text-sm text-muted-foreground">No image available</p>
      </div>
    );
  }

  return (
    <>
      <div className="border rounded-lg p-4">
        <div className="flex items-center justify-between mb-2">
          <div className="flex items-center gap-2">
            <ImageIcon className="h-4 w-4 text-muted-foreground" />
            <span className="font-medium">{output.name}</span>
          </div>
          <div className="flex items-center gap-2">
            <Button variant="outline" size="sm">
              <Download className="h-4 w-4 mr-2" />
              Download
            </Button>
            <Button variant="ghost" size="sm" onClick={() => setIsFullscreen(true)}>
              <Maximize2 className="h-4 w-4" />
            </Button>
          </div>
        </div>
        <div className="mt-2 bg-muted rounded-lg overflow-hidden">
          <img
            src={output.value}
            alt={output.name}
            className="max-w-full h-auto max-h-64 object-contain mx-auto"
          />
        </div>
      </div>

      <Dialog open={isFullscreen} onOpenChange={setIsFullscreen}>
        <DialogContent className="max-w-4xl">
          <DialogHeader>
            <DialogTitle>{output.name}</DialogTitle>
          </DialogHeader>
          <div className="flex items-center justify-center p-4">
            <img
              src={output.value}
              alt={output.name}
              className="max-w-full max-h-[70vh] object-contain"
            />
          </div>
        </DialogContent>
      </Dialog>
    </>
  );
}
