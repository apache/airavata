import { Link, useParams } from "react-router";
import NavBar from "../NavBar";
import {
  Container,
  Spinner,
  HStack,
  Box,
  Separator,
  Button,
  Icon,
  Link as ChakraLink, Heading, ListItem, Center, ListRoot,
} from "@chakra-ui/react";
// @ts-expect-error This is fine
import { MOCK_PROJECTS } from "../../data/MOCK_DATA";
import { useEffect, useState } from "react";
import { BiArrowBack } from "react-icons/bi";
import { ProjectType } from "@/interfaces/ProjectType";
import { Metadata } from "../Metadata";
import {FiFile, FiFolder} from "react-icons/fi";

interface FileTreeItem {
  name: string;
  type: string;
  sha: string;
}

async function getProject(slug: string | undefined) {
  return MOCK_PROJECTS.find(
      (project: ProjectType) => project.metadata.slug === slug
  );
}

const ProjectDetails = () => {
  const { slug } = useParams();
  const [project, setProject] = useState<ProjectType | null>(null);
  const [fileTree, setFileTree] = useState<FileTreeItem[]>([]);
  const [fileTreeLoading, setFileTreeLoading] = useState(false);

  useEffect(() => {
    if (!slug) return;

    async function getData() {
      const n = await getProject(slug);
      setProject(n);
    }
    getData();
  }, [slug]);

  const isNotebook = project?.notebookViewer !== undefined;

  useEffect(() => {
    if (project && !isNotebook) {
      const repoUrl = project.repositoryUrl;
      // @ts-expect-error This is fine
      const match = repoUrl.match(/github\.com\/([^/]+)\/([^/]+)/);
      if (match) {
        const owner = match[1];
        const repo = match[2].replace(/\.git$/, "");
        setFileTreeLoading(true);
        fetch(`https://api.github.com/repos/${owner}/${repo}/contents`)
            .then((res) => res.json())
            .then((data) => {
              setFileTree(data);
              setFileTreeLoading(false);
            })
            .catch((error) => {
              console.error("Error fetching file tree:", error);
              setFileTreeLoading(false);
            });
      }
    }
  }, [project, isNotebook]);

  if (!project) return <Spinner />;

  return (
      <>
        <NavBar />

        <Container maxW="breakpoint-lg" mx="auto" p={4} mt={16}>
          <Box>
            <HStack alignItems="center" mb={4}>
              <Icon>
                <BiArrowBack />
              </Icon>
              <Link to="/">Back to Projects</Link>
            </HStack>
          </Box>
          <Metadata metadata={project.metadata} />

          <Separator my={8} />

          <Button colorPalette="teal" mb={4} w="full">
            <ChakraLink
                target="_blank"
                href={project?.notebookViewer || project?.repositoryUrl}
                color="white"
                fontWeight="bold"
                display="block"
                width="100%"
                textAlign="center"
            >
              Open
            </ChakraLink>
          </Button>

          <Box border="1px" borderColor="gray.200" borderRadius="md" overflow="hidden">
            <Box height="600px" bg="gray.50" p={4} overflow="auto">
              {isNotebook ? (
                  <iframe
                      title="notebook"
                      src={project?.notebookViewer}
                      width="100%"
                      height="100%"
                      style={{ border: "none" }}
                  />
              ) : fileTreeLoading ? (
                  <Center height="100%">
                    <Spinner size="xl" />
                  </Center>
              ) : (
                  <Box
                      bg="white"
                      p={4}
                      borderRadius="md"
                      shadow="md"
                      overflow="auto"
                      height="full"
                  >
                    <Heading as="h2" size="lg" mb={4}>
                      {project.metadata.title}
                    </Heading>
                    <ListRoot>
                      {fileTree &&
                          fileTree.map((file) => (
                              <ListItem
                                  key={file.sha}
                                  display="flex"
                                  alignItems="center"
                                  p={2}
                                  borderRadius="md"
                                  _hover={{ bg: "gray.100" }}
                              >
                                <Icon
                                    as={file.type === "dir" ? FiFolder : FiFile}
                                    color={file.type === "dir" ? "blue.500" : "gray.500"}
                                    mr={2}
                                />

                                <p>{file.name}</p>
                              </ListItem>
                          ))}
                    </ListRoot>
                  </Box>
              )}
            </Box>
          </Box>
        </Container>
      </>
  );
};

export default ProjectDetails;
