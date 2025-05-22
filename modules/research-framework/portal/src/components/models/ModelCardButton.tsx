import { ModelResource } from "@/interfaces/ResourceType";
import { resourceTypeToColor } from "@/lib/util";
import { Button } from "@chakra-ui/react";

export const ModelCardButton = ({ model }: { model: ModelResource }) => {
  return (
    <Button
      colorPalette={resourceTypeToColor("MODEL")}
      as="a"
      // @ts-expect-error This is fine
      target="_blank"
      href={`https://cybershuttle.org/workspace/applications/${model.applicationInterfaceId}/create_experiment`}
    >
      Create
    </Button>
  );
};
