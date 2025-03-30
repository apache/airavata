import NavBar from "../NavBar";
// @ts-expect-error This is fine
import { MOCK_MODELS } from "../../data/MOCK_DATA";
import { Container, HStack, Input, SimpleGrid } from "@chakra-ui/react";
import { PageHeader } from "../PageHeader";
import { ModelCard } from "./ModelCard";
import { ModelType } from "@/interfaces/ModelType";
import { LuSearch } from "react-icons/lu";
import { InputGroup } from "../ui/input-group";
import { ButtonWithIcon } from "../home/ButtonWithIcon";
import { FaPlus } from "react-icons/fa";

export const Models = () => {
  return (
    <>
      <NavBar />

      <Container maxW="container.lg" p={4}>
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
          {MOCK_MODELS.map((model: ModelType) => {
            return <ModelCard model={model} key={model.appModuleId} />;
          })}
        </SimpleGrid>
      </Container>
    </>
  );
};
