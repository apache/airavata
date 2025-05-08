import { ProjectType } from "@/interfaces/ProjectType";
import api from "@/lib/api";
import { SimpleGrid } from "@chakra-ui/react";
import { useEffect, useState } from "react";
import { ProjectCard } from "./ProjectCard";
import { CONTROLLER } from "@/lib/controller";
import { useAuth } from "react-oidc-context";

export const ProjectsSection = () => {
  const [projects, setProjects] = useState<ProjectType[]>([]);
  const auth = useAuth();

  async function getAllProjects(userName: string) {
    try {
      if (!userName) {
        console.error("No userName provided");
        return [];
      }
      const response = await api.get(`${CONTROLLER.projects}/${userName}`);
      const data = response.data;
      console.log("projects", data);
      return data;
    } catch (error) {
      console.error("Error fetching:", error);
    }
  }

  useEffect(() => {
    if (!auth) {
      return;
    }
    async function init() {
      const projects = await getAllProjects(auth.user?.profile.email || "");
      console.log(projects);
      setProjects(projects);
    }

    init();
  }, []);
  return (
    <SimpleGrid mt={2} columns={{ base: 1, md: 2, lg: 2 }} gap={4}>
      {projects.map((project: ProjectType) => {
        return <ProjectCard project={project} key={project.id} />;
      })}
    </SimpleGrid>
  );
};
