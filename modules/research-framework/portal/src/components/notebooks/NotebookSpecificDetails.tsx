import { NotebookResource } from "@/interfaces/ResourceType";
import { Box } from "@chakra-ui/react";

export const NotebookSpecificDetails = ({
  notebook,
}: {
  notebook: NotebookResource;
}) => {
  return (
    <Box height="800px">
      <iframe
        title="notebook"
        src={notebook.notebookPath}
        width="100%"
        height="100%"
      />
    </Box>
  );
};
