import { ProjectType } from "@/interfaces/ProjectType";
import api from "@/lib/api";
import { CONTROLLER } from "@/lib/controller";
import { SimpleGrid } from "@chakra-ui/react";
import { useEffect, useState } from "react";
import { ProjectCard } from "../home/ProjectCard";

async function fetchProjects(id: string) {
  try {
    const resp = await api.get(`${CONTROLLER.resources}/${id}/projects`);
    return resp.data;
  } catch (error) {
    console.error("Error fetching projects:", error);
  }
}

export const AssociatedProjectsSection = ({
  resourceId,
}: {
  resourceId: string;
}) => {
  const [projects, setProjects] = useState<ProjectType[]>([]);
  useEffect(() => {
    async function fetchData() {
      const projects = await fetchProjects(resourceId);
      setProjects(projects);
    }

    fetchData();
  }, []);
  return (
    <>
      <SimpleGrid columns={{ base: 1, md: 2 }} gap={2}>
        {projects.map((project) => (
          <ProjectCard key={project.id} project={project} />
        ))}
      </SimpleGrid>
    </>
  );
};
