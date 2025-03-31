import { Input } from "@chakra-ui/react";
import { LuSearch } from "react-icons/lu";
import { ResourceCard } from "./ResourceCard";
import { InputGroup } from "../ui/input-group";
import { GridContainer } from "../GridContainer";
import { useEffect, useState } from "react";
import api from "@/lib/api";
import { Resource } from "@/interfaces/ResourceType";

const fetchNotebooksAndRepositories = async () => {
  try {
    const response = await api.get(
      "/project-management/resources?type=NOTEBOOK&type=REPOSITORY"
    );
    const data = response.data;
    return data;
  } catch (error) {
    console.error("Error fetching:", error);
  }
};

export const NotebooksAndRepositoriesSection = () => {
  const [resources, setResources] = useState<Resource[]>([]);

  useEffect(() => {
    async function init() {
      const projects = await fetchNotebooksAndRepositories();
      setResources(projects.content);
    }

    init();
  }, []);

  console.log("Resources:", resources);

  return (
    <>
      <InputGroup mt={2} endElement={<LuSearch />} w="100%">
        <Input placeholder="Search" rounded="md" />
      </InputGroup>

      <GridContainer>
        {resources.map((resource: Resource) => {
          return <ResourceCard key={resource.id} resource={resource} />;
        })}
      </GridContainer>
    </>
  );
};
