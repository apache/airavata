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
  Link as ChakraLink,
} from "@chakra-ui/react";
// @ts-expect-error This is fine
import { MOCK_PROJECTS } from "../../data/MOCK_DATA";
import { useEffect, useState } from "react";
import { BiArrowBack } from "react-icons/bi";
import { ProjectType } from "@/interfaces/ProjectType";
import { Metadata } from "../Metadata";

async function getProject(slug: string | undefined) {
  return MOCK_PROJECTS.find(
    (project: ProjectType) => project.metadata.slug === slug
  );
}

const ProjectDetails = () => {
  const { slug } = useParams();
  const [project, setProject] = useState<ProjectType | null>(null);

  useEffect(() => {
    if (!slug) return;

    async function getData() {
      const n = await getProject(slug);
      setProject(n);
    }
    getData();
  }, [slug]);

  if (!project) return <Spinner />;

  const isNotebook = project?.notebookViewer !== undefined;

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

        <Box border="2px solid black">
          <Box height="600px" borderRadius="md" overflow="hidden">
            {isNotebook ? (
              <iframe
                title="notebook"
                src={project?.notebookViewer}
                width="100%"
                height="100%"
              />
            ) : (
              <iframe
                width="100%"
                height="100%"
                src={`https://emgithub.com/iframe.html?target=${project.repositoryUrl}%2Fblob%2Fmaster%2FREADME.md&style=default&type=markdown&showBorder=on&showLineNumbers=on&showFileMeta=on&showFullPath=on&showCopy=on`}
              ></iframe>
            )}
          </Box>
        </Box>
      </Container>
    </>
  );
};

export default ProjectDetails;
