import { Container, Input, SimpleGrid } from "@chakra-ui/react";
import { PageHeader } from "../PageHeader";
import { InputGroup } from "../ui/input-group";
import { LuSearch } from "react-icons/lu";
import { useEffect, useState } from "react";
import api from "@/lib/api";
import { NotebookResource } from "@/interfaces/ResourceType";
import { ResourceCard } from "../home/ResourceCard";
import { CONTROLLER } from "@/lib/controller";
import { ResourceTypeEnum } from "@/interfaces/ResourceTypeEnum";

const getNotebooks = async () => {
  try {
    const response = await api.get(`${CONTROLLER.resources}/`, {
      params: {
        type: ResourceTypeEnum.NOTEBOOK,
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

const Notebooks = () => {
  const [notebooks, setNotebooks] = useState<NotebookResource[]>([]);

  useEffect(() => {
    async function init() {
      const notebooks = await getNotebooks();
      setNotebooks(notebooks.content);
    }

    init();
  }, []);

  return (
    <>
      <Container maxW="container.lg" mt={8}>
        <PageHeader
          title="Notebooks"
          description="Create and manage your notebooks. From here, you can create new notebooks, view existing ones, and manage them."
        />
        <InputGroup endElement={<LuSearch />} w="100%" mt={4}>
          <Input placeholder="Search" rounded="md" />
        </InputGroup>
        {/* <Box mt={4}>
          <TagsInput
            value={tags}
            onChange={setTags}
            placeHolder="Filter by tags"
          />
        </Box> */}
        <SimpleGrid
          columns={{ base: 1, md: 2, lg: 3 }}
          mt={4}
          gap={12}
          justifyContent="space-around"
        >
          {notebooks.map((notebook: NotebookResource) => {
            return <ResourceCard resource={notebook} key={notebook.id} />;
          })}
        </SimpleGrid>
      </Container>
    </>
  );
};

export default Notebooks;
