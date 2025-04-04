import NavBar from "../NavBar";
import { Container, HStack, Input, SimpleGrid } from "@chakra-ui/react";
import { PageHeader } from "../PageHeader";
import { LuSearch } from "react-icons/lu";
import { InputGroup } from "../ui/input-group";
import { ButtonWithIcon } from "../home/ButtonWithIcon";
import { FaPlus } from "react-icons/fa";
import api from "@/lib/api";
import { DatasetResource } from "@/interfaces/ResourceType";
import { useEffect, useState } from "react";
import { ResourceCard } from "../home/ResourceCard";
import { resourceTypeToColor } from "@/lib/util";
import { ResourceTypeEnum } from "@/interfaces/ResourceTypeEnum";
import { CONTROLLER } from "@/lib/controller";

const getDatasets = async () => {
  try {
    const response = await api.get(`${CONTROLLER.resources}/`, {
      params: {
        type: ResourceTypeEnum.DATASET,
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

      <Container maxW="container.lg" mt={8}>
        <HStack alignItems="flex-end" justify="space-between">
          <PageHeader
            title="Datasets"
            description="Public and Private Scientific Datasets."
          />
          <ButtonWithIcon
            colorPalette={resourceTypeToColor("DATASET")}
            icon={FaPlus}
          >
            Dataset
          </ButtonWithIcon>
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
