import { ProjectType } from "@/interfaces/ProjectType";
import api from "@/lib/api";
import { SimpleGrid } from "@chakra-ui/react";
import { useEffect, useState } from "react";
import { ProjectCard } from "./ProjectCard";
import { useAuth } from "react-oidc-context";

export const ProjectsSection = () => {
  const [projects, setProjects] = useState<ProjectType[]>([]);
  const auth = useAuth();

  async function getAllProjects() {
    try {
      const response = await fetch(
        "http://localhost:18889/api/v1/rf/hub/projects",
        {
          method: "GET",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${auth.user?.access_token}`,
            "X-Claims": JSON.stringify({
              userName: auth.user?.profile.email,
              gatewayID: "default",
            }),
          },
        }
      );
      const data = await response.json();
      console.log(data);
      return data;
    } catch (error) {
      console.error("Error fetching:", error);
    }
  }

  useEffect(() => {
    if (auth.isLoading) {
      return;
    }

    console.log("fetching projects");
    console.log(auth.user?.access_token);

    async function init() {
      const projects = await getAllProjects();
      console.log(projects);
      setProjects(projects);
    }

    init();
  }, [auth]);
  return (
    <SimpleGrid mt={2} columns={{ base: 1, md: 2, lg: 2 }}>
      {projects.map((project: ProjectType) => {
        return <ProjectCard project={project} key={project.id} />;
      })}
    </SimpleGrid>
  );
};
