import NavBar from "../NavBar";
// @ts-expect-error This is fine
import { MOCK_DATASETS } from "../../data/MOCK_DATA";
import { Container, HStack, Input, SimpleGrid } from "@chakra-ui/react";
import { PageHeader } from "../PageHeader";
import { LuSearch } from "react-icons/lu";
import { InputGroup } from "../ui/input-group";
import { ButtonWithIcon } from "../home/ButtonWithIcon";
import { FaPlus } from "react-icons/fa";
import { DatasetCard } from "./DatasetCard";
import { DatasetType } from "@/interfaces/DatasetType";

export const Datasets = () => {
  return (
    <>
      <NavBar />

      <Container maxW="container.lg" p={4}>
        <HStack alignItems="flex-end" justify="space-between">
          <PageHeader
            title="Datasets"
            description="Public and Private Scientific Datasets."
          />
          <ButtonWithIcon colorPalette="purple" icon={FaPlus}>
            Dataset
          </ButtonWithIcon>
        </HStack>
        <InputGroup mt={4} endElement={<LuSearch />} w="100%">
          <Input placeholder="Search" rounded="md" />
        </InputGroup>

        <SimpleGrid columns={{ base: 1, md: 2, lg: 3 }} gap={4} mt={4}>
          {MOCK_DATASETS.map((dataset: DatasetType) => {
            return (
              <DatasetCard dataset={dataset} key={dataset.metadata.slug} />
            );
          })}
        </SimpleGrid>
      </Container>
    </>
  );
};
