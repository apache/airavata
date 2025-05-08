import { Box, Heading } from "@chakra-ui/react";
import { AssociatedProjectsSection } from "../projects/AssociatedProejctsSection";
import { DatasetResource } from "@/interfaces/ResourceType";

export const DatasetSpecificDetails = ({
  dataset,
}: {
  dataset: DatasetResource;
}) => {
  return (
    <>
      <Box>
        <Heading fontWeight="bold" size="2xl" mb={2}>
          Associated Projects
        </Heading>
        <AssociatedProjectsSection resourceId={dataset.id} />
      </Box>
    </>
  );
};
