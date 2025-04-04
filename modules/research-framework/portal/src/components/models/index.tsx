import NavBar from "../NavBar";
import { Container, HStack, Input, SimpleGrid } from "@chakra-ui/react";
import { PageHeader } from "../PageHeader";
import { LuSearch } from "react-icons/lu";
import { InputGroup } from "../ui/input-group";
import { ButtonWithIcon } from "../home/ButtonWithIcon";
import { FaPlus } from "react-icons/fa";
import api from "@/lib/api";
import { ModelResource } from "@/interfaces/ResourceType";
import { useEffect, useState } from "react";
import { ResourceCard } from "../home/ResourceCard";
import { CONTROLLER } from "@/lib/controller";
import { ResourceTypeEnum } from "@/interfaces/ResourceTypeEnum";

const getModels = async () => {
  try {
    const response = await api.get(`${CONTROLLER.resources}/`, {
      params: {
        type: ResourceTypeEnum.MODEL,
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

export const Models = () => {
  const [models, setModels] = useState<ModelResource[]>([]);

  useEffect(() => {
    async function init() {
      const projects = await getModels();
      setModels(projects.content);
    }

    init();
  }, []);

  return (
    <>
      <NavBar />

      <Container maxW="container.lg" mt={8}>
        <HStack alignItems="flex-end" justify="space-between">
          <PageHeader
            title="Models"
            description="Public and Private Scientific models that can be run with varying inputs."
          />
          <ButtonWithIcon colorPalette="teal" icon={FaPlus}>
            Model
          </ButtonWithIcon>
        </HStack>
        <InputGroup mt={4} endElement={<LuSearch />} w="100%">
          <Input placeholder="Search" rounded="md" />
        </InputGroup>

        <SimpleGrid columns={{ base: 1, md: 2, lg: 3 }} gap={4} mt={4}>
          {models.map((model: ModelResource) => {
            return <ResourceCard resource={model} key={model.id} />;
          })}
        </SimpleGrid>
      </Container>
    </>
  );
};
