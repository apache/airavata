import { Link, useParams } from "react-router";
import NavBar from "../NavBar";
import {
  Container,
  Avatar,
  Image,
  Spinner,
  HStack,
  Text,
  Box,
  Heading,
  Separator,
  Button,
  Icon,
  Link as ChakraLink,
} from "@chakra-ui/react";
import { MOCK_NOTEBOOKS } from "../../data/MOCK_DATA";
import { useEffect, useState } from "react";
import { BiArrowBack } from "react-icons/bi";

async function getNotebook(notebookSlug: string | undefined) {
  return MOCK_NOTEBOOKS.find((notebook) => notebook.slug === notebookSlug);
}

const NotebookDetails = () => {
  const { slug } = useParams();
  const [notebook, setNotebook] = useState(null);

  useEffect(() => {
    if (!slug) return;

    async function getData() {
      const n = await getNotebook(slug);
      console.log(n);
      setNotebook(n);
    }
    getData();
  }, [slug]);

  if (!notebook) return <Spinner />;

  return (
    <>
      <NavBar />

      <Container maxW="breakpoint-lg" mx="auto" p={4} mt={16}>
        <Box>
          <HStack alignItems="center" mb={4}>
            <Icon>
              <BiArrowBack />
            </Icon>
            <Link to="/notebooks">Back to Notebooks</Link>
          </HStack>
        </Box>
        <HStack alignContent={"center"} mb={4} gap={8}>
          <Box>
            <Heading as="h1" size="4xl" mb={4}>
              {notebook.title}
            </Heading>

            <HStack mb={4}>
              {notebook.tags.map((tag) => (
                <Text
                  key={tag}
                  color="gray.500"
                  bg="gray.200"
                  p={2}
                  rounded="md"
                >
                  {tag}
                </Text>
              ))}
            </HStack>

            <HStack>
              <Avatar.Root shape="full" size="xl">
                <Avatar.Fallback name={notebook.author.name} />
                <Avatar.Image src={notebook.author.avatar} />
              </Avatar.Root>

              <Box>
                <Text fontWeight="bold">{notebook.author.name}</Text>
                <Text color="gray.500">{notebook.author.role}</Text>
              </Box>
            </HStack>
          </Box>

          <Image
            src={notebook.images.headerImage}
            alt="Notebook Header"
            rounded="md"
            maxW="300px"
          />
        </HStack>

        <Separator my={8} />
        <Box>
          <Text fontWeight="bold" fontSize="2xl" mb={4}>
            About
          </Text>
          <Text color="gray.600">{notebook.description}</Text>
        </Box>

        <Separator my={8} />

        <Box>
          <Text fontWeight="bold" fontSize="2xl" mb={4}>
            Notebook Viewer
          </Text>

          <Button colorPalette="teal" mb={4}>
            <ChakraLink
              target="_blank"
              href={notebook.notebookViewer}
              color="white"
            >
              Run Notebook
            </ChakraLink>
          </Button>

          <Box height="600px" borderRadius="md" overflow="hidden">
            <iframe
              title="notebook"
              src={notebook.notebookViewer}
              width="100%"
              height="100%"
            ></iframe>
          </Box>
        </Box>
      </Container>
    </>
  );
};

export default NotebookDetails;
