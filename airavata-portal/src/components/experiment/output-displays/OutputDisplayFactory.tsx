"use client";

import { DefaultOutputDisplay } from "./DefaultOutputDisplay";
import { ImageOutputDisplay } from "./ImageOutputDisplay";
import type { OutputDataObjectType } from "@/types";
import { DataType } from "@/types";

interface OutputDisplayFactoryProps {
  output: OutputDataObjectType;
}

export function OutputDisplayFactory({ output }: OutputDisplayFactoryProps) {
  // Determine display type based on output type and metadata
  const displayType = getDisplayType(output);

  switch (displayType) {
    case "image":
      return <ImageOutputDisplay output={output} />;
    default:
      return <DefaultOutputDisplay output={output} />;
  }
}

function getDisplayType(output: OutputDataObjectType): string {
  // Check metadata for display hints
  if (output.metaData) {
    try {
      const metadata = JSON.parse(output.metaData);
      if (metadata.display) {
        return metadata.display;
      }
    } catch {
      // Not JSON
    }
  }

  // Check if value looks like an image URL
  if (output.value) {
    const lowerValue = output.value.toLowerCase();
    if (
      lowerValue.endsWith(".png") ||
      lowerValue.endsWith(".jpg") ||
      lowerValue.endsWith(".jpeg") ||
      lowerValue.endsWith(".gif") ||
      lowerValue.endsWith(".webp") ||
      lowerValue.startsWith("data:image/")
    ) {
      return "image";
    }
  }

  // Check output name hints
  const lowerName = output.name.toLowerCase();
  if (
    lowerName.includes("image") ||
    lowerName.includes("plot") ||
    lowerName.includes("figure") ||
    lowerName.includes("graph")
  ) {
    return "image";
  }

  return "default";
}
