import { ModelResource } from "@/interfaces/ResourceType";
import { resourceTypeToColor } from "@/lib/util";
import { Button } from "@chakra-ui/react";
export const ModelSpecificBox = ({ model }: { model: ModelResource }) => {
  console.log(model);
  return (
    <Button
      colorPalette={resourceTypeToColor("MODEL")}
      as="a"
      w="100%"
      // @ts-expect-error This is fine
      _target="_blank"
      href={`https://cybershuttle.org/workspace/applications/${model.applicationInterfaceId}/create_experiment`}
    >
      Create{" "}
      <b>
        {model.name} ({model.version})
      </b>{" "}
      in Cybershuttle
    </Button>
  );
};
