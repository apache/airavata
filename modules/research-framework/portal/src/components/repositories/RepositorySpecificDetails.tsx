import {RepositoryResource} from "@/interfaces/ResourceType";
import {Box} from "@chakra-ui/react";
import {AssociatedProjectsSection} from "../projects/AssociatedProjectsSection.tsx";
import {GitHubFileTree} from "./GitHubFileTree";

export const RepositorySpecificDetails = ({
                                            repository,
                                          }: {
  repository: RepositoryResource;
}) => {
  return (
      <Box>
        <AssociatedProjectsSection resourceId={repository.id}/>

        <GitHubFileTree repository={repository}/>
      </Box>
  );
};
