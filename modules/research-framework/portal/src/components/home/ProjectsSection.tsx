import { ProjectType } from "@/interfaces/ProjectType";
import api from "@/lib/api";
import { SimpleGrid } from "@chakra-ui/react";
import { useEffect, useState } from "react";
import { ProjectCard } from "./ProjectCard";

async function getAllProjects() {
  try {
    const response = await api.get("/hub/projects");
    const data = response.data;
    return data;
  } catch (error) {
    console.error("Error fetching:", error);
  }
}
export const ProjectsSection = () => {
  const [projects, setProjects] = useState<ProjectType[]>([]);

  useEffect(() => {
    async function init() {
      const projects = await getAllProjects();
      console.log(projects);
      setProjects(projects);
    }

    init();
  }, []);
  return (
    <SimpleGrid mt={2} columns={{ base: 1, md: 2, lg: 2 }}>
      {projects.map((project: ProjectType) => {
        return <ProjectCard project={project} key={project.id} />;
      })}
    </SimpleGrid>
  );
};
