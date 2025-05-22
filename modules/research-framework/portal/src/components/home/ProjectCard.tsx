import { ProjectType } from "@/interfaces/ProjectType";
import { Card, HStack, Text } from "@chakra-ui/react";
import { StartSessionFromProjectButton } from "./StartSessionFromProjectButton";

export const ProjectCard = ({ project }: { project: ProjectType }) => {
  return (
    <Card.Root overflow="hidden" size="md" height="fit-content">
      <Card.Body gap="2">
        {/* Card Content */}
        <HStack alignItems="center" justifyContent="space-between">
          <Card.Title>{project.name}</Card.Title>
          <Text color="gray.500">
            {new Date(project.createdAt).toLocaleDateString()}
          </Text>
        </HStack>

        <Text fontWeight="bold">
          Repository:{" "}
          <Text as="span" fontWeight="normal">
            {project.repositoryResource.name}
          </Text>
        </Text>
        <Text fontWeight="bold">
          Datasets:{" "}
          <Text as="span" fontWeight="normal">
            {project.datasetResources.map((dataset) => dataset.name).join(", ")}
          </Text>
        </Text>

        <StartSessionFromProjectButton project={project} />
      </Card.Body>
    </Card.Root>
  );
};
