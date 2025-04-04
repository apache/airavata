import { Container, Input, SimpleGrid } from "@chakra-ui/react";
import NavBar from "../NavBar";
import { PageHeader } from "../PageHeader";
import api from "@/lib/api";
import { useEffect, useState } from "react";
import { RepositoryResource } from "@/interfaces/ResourceType";
import { ResourceCard } from "../home/ResourceCard";
import { LuSearch } from "react-icons/lu";
import { InputGroup } from "../ui/input-group";
import { CONTROLLER } from "@/lib/controller";
import { ResourceTypeEnum } from "@/interfaces/ResourceTypeEnum";

const getRepositories = async () => {
  try {
    const response = await api.get(`${CONTROLLER.resources}/`, {
      params: {
        type: ResourceTypeEnum.REPOSITORY,
        pageNumber: 0,
        pageSize: 100,
      },
    });
    const data = response.data;
    return data;
  } catch (error) {
    console.error("Error fetching:", error);
  }
};

const Repositories = () => {
  const [repositories, setRepositories] = useState<RepositoryResource[]>([]);

  useEffect(() => {
    async function init() {
      const repositories = await getRepositories();
      setRepositories(repositories.content);
    }

    init();
  }, []);

  return (
    <>
      <NavBar />

      <Container maxW="container.lg" mt={8}>
        <PageHeader
          title="Repositories"
          description="View the most viewed repositories on GitHub, all central to this page."
        />
        <InputGroup mt={4} endElement={<LuSearch />} w="100%">
          <Input placeholder="Search" rounded="md" />
        </InputGroup>
        <SimpleGrid columns={{ base: 1, md: 2, lg: 3 }} gap={4} mt={4}>
          {repositories.map((repo: RepositoryResource) => {
            return <ResourceCard resource={repo} key={repo.id} />;
          })}
        </SimpleGrid>
      </Container>
    </>
  );
};

export default Repositories;
