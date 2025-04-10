import { RepositoryResource } from "@/interfaces/ResourceType";
import { Box, Heading, Separator } from "@chakra-ui/react";
import { AssociatedProjectsSection } from "../projects/AssociatedProejctsSection";
import { Toaster } from "../ui/toaster";
import { GitHubFileTree } from "./GitHubFileTree";

export const RepositorySpecificDetails = ({
  repository,
}: {
  repository: RepositoryResource;
}) => {
  return (
    <Box>
      <Toaster />

      <Box>
        <Heading fontWeight="bold" size="2xl" mb={2}>
          Associated Projects
        </Heading>

        <AssociatedProjectsSection resourceId={repository.id} />
      </Box>

      <Separator my={6} />

      <GitHubFileTree repository={repository} />
    </Box>
  );
};
