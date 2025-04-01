import { ProjectType } from "@/interfaces/ProjectType";
import api from "@/lib/api";
import { API_VERSION, BACKEND_URL } from "@/lib/constants";
import { Button, Card, HStack, Text } from "@chakra-ui/react";
import { useEffect, useState } from "react";

async function checkIfSessionWithProjectExists(project: ProjectType) {
  try {
    await api.get(`/hub/project/${project.id}/exists`);

    return true;
  } catch (error: any) {
    return false;
  }
}

export const ProjectCard = ({ project }: { project: ProjectType }) => {
  const [exists, setExists] = useState(false);
  useEffect(() => {
    let intervalId: NodeJS.Timeout;

    async function fetchData() {
      const exists = await checkIfSessionWithProjectExists(project);
      setExists(exists);
    }

    fetchData(); // Initial fetch

    intervalId = setInterval(fetchData, 2000); // Fetch every 5 seconds

    return () => clearInterval(intervalId); // Cleanup on unmount
  }, [project]); // Depend on project so it updates correctly

  return (
    <Card.Root overflow="hidden" size="md">
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

        {exists ? (
          <Text color="green.600">Session already started</Text>
        ) : (
          <Button
            colorPalette="black"
            size="sm"
            onClick={() => {
              window.open(
                `${BACKEND_URL}/api/${API_VERSION}/rf/hub/project/${project.id}?sessionName=${project.name}`,
                "_blank"
              );
            }}
          >
            Open Project
          </Button>
        )}
      </Card.Body>
    </Card.Root>
  );
};
