import { Link, useParams } from "react-router";
import NavBar from "../NavBar";
import {
  Container,
  Spinner,
  HStack,
  Box,
  Separator,
  Icon,
  Text,
  Image,
  Badge,
  Heading,
  Avatar,
} from "@chakra-ui/react";
import { useEffect, useState } from "react";
import { BiArrowBack } from "react-icons/bi";
import api from "@/lib/api";
import {
  ModelResource,
  NotebookResource,
  RepositoryResource,
  Resource,
} from "@/interfaces/ResourceType";
import { Tag } from "@/interfaces/TagType";
import { User } from "@/interfaces/UserType";
import { isValidImaage, resourceTypeToColor } from "@/lib/util";
import { ResourceTypeBadge } from "./ResourceTypeBadge";
import { ResourceTypeEnum } from "@/interfaces/ResourceTypeEnum";
import { ModelSpecificBox } from "../models/ModelSpecificBox";
import { NotebookSpecificDetails } from "../notebooks/NotebookSpecificDetails";
import { RepositorySpecificDetails } from "../repositories/RepositorySpecificDetails";

async function getResource(id: string) {
  const response = await api.get(`/project-management/resources/${id}`);
  return response.data;
}

const ResourceDetails = () => {
  const { id } = useParams();
  const [resource, setResource] = useState<Resource | null>(null);

  useEffect(() => {
    if (!id) return;

    async function getData() {
      // @ts-expect-error This is fine
      const r = await getResource(id);

      setResource(r);
    }
    getData();
  }, [id]);

  if (!resource) return <Spinner />;

  const validImage = isValidImaage(resource.headerImage);

  return (
    <>
      <NavBar />
      <Container maxW="breakpoint-lg" mx="auto" p={4} mt={16}>
        <Box>
          <Link to="/">
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
                <BiArrowBack />
              </Icon>
              Back
            </HStack>
          </Link>
        </Box>

        <HStack
          alignItems={"center"}
          mb={4}
          gap={8}
          justifyContent="space-between"
        >
          <Box>
            <ResourceTypeBadge type={resource.type} />
            <Heading as="h1" size="4xl" mt={2}>
              {resource.name}
            </Heading>

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
              {resource.authors.map((author: User) => {
                return (
                  <HStack key={author.id}>
                    <Avatar.Root shape="full" size="xl">
                      <Avatar.Fallback
                        name={author.firstName + " " + author.lastName}
                      />
                      <Avatar.Image src={author.avatar} />
                    </Avatar.Root>

                    <Box>
                      <Text fontWeight="bold">
                        {author.firstName + " " + author.lastName}
                      </Text>
                    </Box>
                  </HStack>
                );
              })}
            </HStack>
          </Box>

          {validImage && (
            <Image
              src={resource.headerImage}
              alt="Notebook Header"
              rounded="md"
              maxW="300px"
            />
          )}
        </HStack>

        <Separator my={6} />
        <Box>
          <Heading fontWeight="bold" size="2xl">
            About
          </Heading>

          <Text>{resource.description}</Text>
        </Box>

        <Separator my={8} />

        <Box>
          {(resource.type as ResourceTypeEnum) ===
            ResourceTypeEnum.REPOSITORY && (
            <RepositorySpecificDetails
              dataset={resource as RepositoryResource}
            />
          )}

          {(resource.type as ResourceTypeEnum) === ResourceTypeEnum.MODEL && (
            <ModelSpecificBox model={resource as ModelResource} />
          )}

          {(resource.type as ResourceTypeEnum) ===
            ResourceTypeEnum.NOTEBOOK && (
            <NotebookSpecificDetails notebook={resource as NotebookResource} />
          )}
        </Box>
      </Container>
    </>
  );
};

export default ResourceDetails;
