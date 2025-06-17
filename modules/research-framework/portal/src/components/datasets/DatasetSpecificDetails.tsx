import {AssociatedProjectsSection} from "../projects/AssociatedProjectsSection.tsx";
import {DatasetResource} from "@/interfaces/ResourceType";

export const DatasetSpecificDetails = ({
                                         dataset,
                                       }: {
  dataset: DatasetResource;
}) => {
  return (
      <>
        <AssociatedProjectsSection resourceId={dataset.id}/>
      </>
  );
};
