import { resourceTypeToColor } from "@/lib/util";
import { Badge } from "@chakra-ui/react";

interface ResourceTypeBadgeProps {
  type: string;
  [key: string]: string | number | boolean; // Specify a more specific type for additional props
}

export const ResourceTypeBadge = ({
  type,
  ...props
}: ResourceTypeBadgeProps) => {
  return (
    <Badge
      colorPalette={resourceTypeToColor(type)}
      fontWeight="bold"
      size="sm"
      px="2"
      py="1"
      borderRadius="md"
      {...props}
    >
      {type}
    </Badge>
  );
};
