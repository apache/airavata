import { ProjectType } from "@/interfaces/ProjectType";
import { Input } from "@chakra-ui/react";
import { LuSearch } from "react-icons/lu";
// @ts-expect-error This is fine
import { MOCK_PROJECTS } from "../../data/MOCK_DATA";
import { ProjectCard } from "./ProjectCard";
import { InputGroup } from "../ui/input-group";
import { GridContainer } from "../GridContainer";

export const ProjectsSection = () => {
  // shuffle projects
  // const shuffledProjects = MOCK_PROJECTS.sort(() => 0.5 - Math.random());

  const projects = MOCK_PROJECTS;
  return (
    <>
      <InputGroup mt={2} endElement={<LuSearch />} w="100%">
        <Input placeholder="Search" rounded="md" />
      </InputGroup>

      <GridContainer>
        {projects.map((project: ProjectType) => {
          return <ProjectCard key={project.metadata.slug} project={project} />;
        })}
      </GridContainer>
    </>
  );
};
