import {ProjectType} from "@/interfaces/ProjectType";
import {Card, HStack, Text} from "@chakra-ui/react";
import {StartSessionFromProjectButton} from "./StartSessionFromProjectButton";
import {DeleteProjectButton} from "@/components/projects/DeleteProjectButton.tsx";
import {useState} from "react";

export const ProjectCard = ({project}: { project: ProjectType }) => {
  const [hideCard, setHideCard] = useState(false);

  const onDeleteSuccess = () => {
    setHideCard(true);
  }

  return (
      <Card.Root overflow="hidden" size="md" height="fit-content" hidden={hideCard}>
        <Card.Body gap="2">
          {/* Card Content */}
          <HStack alignItems="flex-start" justifyContent="space-between">
            <Card.Title>
              {project.name}
              <Text color="gray.500" fontSize={'sm'}>
                {new Date(project.createdAt).toLocaleDateString()}
              </Text>
            </Card.Title>

            <DeleteProjectButton project={project} onSuccess={onDeleteSuccess}/>
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

          <StartSessionFromProjectButton project={project}/>
        </Card.Body>
      </Card.Root>
  );
};
