import { Box, Container, HStack, Input, SimpleGrid } from "@chakra-ui/react";
import NavBar from "../NavBar";
import { PageHeader } from "../PageHeader";
import { FaCode } from "react-icons/fa";
import { MOCK_NOTEBOOKS } from "../../data/MOCK_DATA";
import { NotebookCard } from "./NotebookCard";
import { InputGroup } from "../ui/input-group";
import { LuSearch } from "react-icons/lu";
import { TagsInput } from "react-tag-input-component";
import { useState } from "react";

const Notebooks = () => {
  const [tags, setTags] = useState<string[]>([]);
  let filteredNotebooks = MOCK_NOTEBOOKS;
  if (tags) {
    filteredNotebooks = MOCK_NOTEBOOKS.filter((notebook) => {
      return tags.every((tag) => notebook.tags.includes(tag));
    });
  }
  return (
    <>
      <NavBar />

      <Container maxW="container.lg" mt={8}>
        <PageHeader
          title="Notebooks"
          icon={<FaCode />}
          description="Create and manage your notebooks. From here, you can create new notebooks, view existing ones, and manage them."
        />
        <InputGroup endElement={<LuSearch />} w="100%" mt={16}>
          <Input placeholder="Search" rounded="md" />
        </InputGroup>
        <Box mt={4}>
          <TagsInput
            value={tags}
            onChange={setTags}
            placeHolder="Filter by tags"
          />
        </Box>
        <SimpleGrid
          columns={{ base: 1, md: 2, lg: 3 }}
          mt={4}
          gap={12}
          justifyContent="space-around"
        >
          {filteredNotebooks.map((notebook: any) => {
            return <NotebookCard key={notebook.slug} notebook={notebook} />;
          })}
        </SimpleGrid>
      </Container>
    </>
  );
};

export default Notebooks;
