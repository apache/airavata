import {useLocation, useNavigate, useParams} from "react-router";
import {
  Avatar,
  Badge,
  Box,
  Button,
  Container,
  Heading,
  HStack,
  Icon,
  Image,
  Separator,
  Spinner,
  Text,
} from "@chakra-ui/react";
import {useEffect, useState} from "react";
import {BiArrowBack} from "react-icons/bi";
import api from "@/lib/api";
import {
  DatasetResource,
  ModelResource,
  NotebookResource,
  RepositoryResource,
  Resource,
} from "@/interfaces/ResourceType";
import {Tag} from "@/interfaces/TagType";
import {isValidImaage, resourceTypeToColor} from "@/lib/util";
import {ResourceTypeBadge} from "./ResourceTypeBadge";
import {ResourceTypeEnum} from "@/interfaces/ResourceTypeEnum";
import {ModelSpecificBox} from "../models/ModelSpecificBox";
import {NotebookSpecificDetails} from "../notebooks/NotebookSpecificDetails";
import {RepositorySpecificDetails} from "../repositories/RepositorySpecificDetails";
import {CONTROLLER} from "@/lib/controller";
import {DatasetSpecificDetails} from "../datasets/DatasetSpecificDetails";
import {DeleteResourceButton} from "@/components/resources/DeleteResourceButton.tsx";

async function getResource(id: string) {
  const response = await api.get(`${CONTROLLER.resources}/${id}`);
  return response.data;
}

const ResourceDetails = () => {
  const {id} = useParams();
  const [resource, setResource] = useState<Resource | null>(null);
  const navigate = useNavigate();
  const {state} = useLocation();

  useEffect(() => {
    if (!id) return;

    async function getData() {
      // @ts-expect-error This is fine
      const r = await getResource(id);

      setResource(r);
    }

    getData();
  }, [id, state]);

  if (!resource) return <Spinner/>;

  const validImage = isValidImaage(resource.headerImage);

  const goToResources = () => {
    navigate(
        "/resources?resourceTypes=REPOSITORY%2CNOTEBOOK%2CDATASET%2CMODEL"
    )
  }


  return (
      <>
        <Container maxW="breakpoint-lg" mx="auto" p={4} mt={16}>
          <Box>
            <Button
                variant="plain"
                p={0}
                onClick={goToResources}
            >
              <HStack
                  alignItems="center"
                  mb={4}
                  display="inline-flex"
                  _hover={{
                    bg: "gray.300",
                  }}
                  p={1}
                  rounded="md"
              >
                <Icon>
                  <BiArrowBack/>
                </Icon>
                Back
              </HStack>
            </Button>
          </Box>

          <HStack
              alignItems={"flex-start"}
              mb={4}
              gap={8}
              justifyContent="space-between"
          >
            <Box w={'full'}>
              <ResourceTypeBadge type={resource.type}/>

              <HStack mt={2} justifyContent={'space-between'} alignItems={'center'} flexWrap={'wrap'}>
                <Heading as="h1" size="4xl">
                  {resource.name}
                </Heading>

                <DeleteResourceButton resource={resource} onSuccess={goToResources}/>
              </HStack>

              <HStack mt={2}>
                {resource.tags.map((tag: Tag) => (
                    <Badge
                        key={tag.id}
                        size="lg"
                        rounded="md"
                        colorPalette={resourceTypeToColor(resource.type)}
                    >
                      {tag.value}
                    </Badge>
                ))}
              </HStack>

              <HStack mt={8}>
                {resource.authors.map((author: string) => {
                  return (
                      <HStack key={author}>
                        <Avatar.Root shape="full" size="xl">
                          <Avatar.Fallback name={author}/>
                          <Avatar.Image src={author}/>
                        </Avatar.Root>

                        <Box>
                          <Text fontWeight="bold">{author}</Text>
                        </Box>
                      </HStack>
                  );
                })}
              </HStack>
            </Box>

            <Box>
              {validImage && (
                  <Image
                      src={resource.headerImage}
                      alt="Notebook Header"
                      rounded="md"
                      maxW="300px"
                  />
              )}
            </Box>
          </HStack>

          <Separator my={6}/>
          <Box>
            <Heading fontWeight="bold" size="2xl">
              About
            </Heading>

            <Text>{resource.description}</Text>
          </Box>

          <Separator my={8}/>

          <Box>
            {(resource.type as ResourceTypeEnum) ===
                ResourceTypeEnum.REPOSITORY && (
                    <RepositorySpecificDetails
                        repository={resource as RepositoryResource}
                    />
                )}

            {(resource.type as ResourceTypeEnum) === ResourceTypeEnum.DATASET && (
                <DatasetSpecificDetails dataset={resource as DatasetResource}/>
            )}

            {(resource.type as ResourceTypeEnum) === ResourceTypeEnum.MODEL && (
                <ModelSpecificBox model={resource as ModelResource}/>
            )}

            {(resource.type as ResourceTypeEnum) ===
                ResourceTypeEnum.NOTEBOOK && (
                    <NotebookSpecificDetails notebook={resource as NotebookResource}/>
                )}
          </Box>
        </Container>
      </>
  );
};

export default ResourceDetails;
