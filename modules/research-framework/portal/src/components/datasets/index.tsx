import NavBar from "../NavBar";
import { Container, HStack, Input, SimpleGrid } from "@chakra-ui/react";
import { PageHeader } from "../PageHeader";
import { LuSearch } from "react-icons/lu";
import { InputGroup } from "../ui/input-group";
import api from "@/lib/api";
import { DatasetResource } from "@/interfaces/ResourceType";
import { useEffect, useState } from "react";
import { ResourceCard } from "../home/ResourceCard";

const getDatasets = async () => {
  try {
    const response = await api.get(
      "/project-management/resources?type=DATASET"
    );
    const data = response.data;
    return data;
  } catch (error) {
    console.error("Error fetching:", error);
  }
};

export const Datasets = () => {
  const [datasets, setDatasets] = useState<DatasetResource[]>([]);

  useEffect(() => {
    async function init() {
      const projects = await getDatasets();
      setDatasets(projects.content);
    }

    init();
  }, []);

  return (
    <>
      <NavBar />

      <Container maxW="container.lg" p={4}>
        <HStack alignItems="flex-end" justify="space-between">
          <PageHeader
            title="Datasets"
            description="Public and Private Scientific Datasets."
          />
        </HStack>
        <InputGroup mt={4} endElement={<LuSearch />} w="100%">
          <Input placeholder="Search" rounded="md" />
        </InputGroup>

        <SimpleGrid columns={{ base: 1, md: 2, lg: 3 }} gap={4} mt={4}>
          {datasets.map((dataset: DatasetResource) => {
            return <ResourceCard resource={dataset} key={dataset.id} />;
          })}
        </SimpleGrid>
      </Container>
    </>
  );
};
