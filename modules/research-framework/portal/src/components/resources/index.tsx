import {
  Text,
  Container,
  Heading,
  Box,
  SimpleGrid,
  Button,
  HStack,
  Code,
  Spinner,
} from "@chakra-ui/react";
import { useEffect, useState } from "react";
import { SEPARATORS, WithContext as ReactTags, Tag } from "react-tag-input";
import "./TagInput.css"; // 👈 custom styles
import api from "@/lib/api";
import { CONTROLLER } from "@/lib/controller";
import { ResourceTypeEnum } from "@/interfaces/ResourceTypeEnum";
import { Resource } from "@/interfaces/ResourceType";
import { ResourceCard } from "../home/ResourceCard";
import { FaCheck } from "react-icons/fa";
import { Tag as TagEntity } from "@/interfaces/TagType";

const getResources = async (
  types: ResourceTypeEnum[],
  stringTagsArr: string[]
) => {
  try {
    const response = await api.get(`${CONTROLLER.resources}/`, {
      params: {
        type: types.join(","),
        tag: stringTagsArr.join(","),
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

const getTags = async () => {
  try {
    const response = await api.get(`${CONTROLLER.resources}/tags/all`);
    const data = response.data;
    return data;
  } catch (error) {
    console.error("Error fetching:", error);
  }
};

export const Resources = () => {
  const [tags, setTags] = useState<Tag[]>([]);
  const [suggestions, setSuggestions] = useState<Tag[]>([]);
  const [resourceTypes, setResourceTypes] = useState<ResourceTypeEnum[]>([
    ResourceTypeEnum.REPOSITORY,
    ResourceTypeEnum.NOTEBOOK,
  ]);
  const [resources, setResources] = useState<Resource[]>([]);
  const [loading, setLoading] = useState(false);

  const handleDelete = (i: number) => {
    setTags(tags.filter((_, index) => index !== i));
  };

  const handleAddition = (tag: Tag) => {
    console.log(tag);
    setTags([...tags, tag]);
  };

  useEffect(() => {
    async function fetchResources() {
      setLoading(true);
      const stringTagsArr = tags.map((tag) => tag.text);
      const resources = await getResources(resourceTypes, stringTagsArr);
      setResources(resources.content);
      setLoading(false);
    }

    fetchResources();
  }, [resourceTypes, tags]);

  useEffect(() => {
    async function fetchTags() {
      const tags: TagEntity[] = await getTags();
      const suggestedTags = tags.map((tag: TagEntity) => ({
        id: tag.value,
        text: tag.value,
        className: "",
      }));

      setSuggestions(suggestedTags);
    }

    fetchTags();
  }, []);

  const labels = [
    ResourceTypeEnum.REPOSITORY,
    ResourceTypeEnum.NOTEBOOK,
    ResourceTypeEnum.DATASET,
    ResourceTypeEnum.MODEL,
  ];

  return (
    <>
      <Container maxW="container.lg" mt={8}>
        <Heading
          textAlign="center"
          fontSize={{ base: "4xl", md: "5xl" }}
          fontWeight="black"
          lineHeight={1.2}
        >
          Research Catalog
        </Heading>
        <Text mt={2} textAlign="center">
          Browse models, notebooks, repositories, and datasets. Created by
          scientists and prepared for
          <Text as="span" color="blue.600" fontWeight="bold">
            {" "}
            execution in local and remote machines
          </Text>
          .
        </Text>

        <Box mt={4} maxWidth="1000px" mx="auto">
          <ReactTags
            tags={tags}
            handleDelete={handleDelete}
            handleAddition={handleAddition}
            suggestions={suggestions}
            separators={[
              SEPARATORS.TAB,
              SEPARATORS.COMMA,
              SEPARATORS.ENTER,
              SEPARATORS.SEMICOLON,
            ]}
            allowDragDrop={true}
            placeholder="Filter resources by tags"
            renderSuggestion={(item) => {
              return <span>{item.text}</span>;
            }}
          />

          <HStack alignItems="center" mt={2}>
            <Text fontSize="sm" color="gray.500" fontWeight="bold">
              Showing
            </Text>
            <HStack wrap="wrap">
              {labels.map((type) => {
                const isSelected = resourceTypes.includes(type);
                return (
                  <Button
                    key={type}
                    variant="outline"
                    color={isSelected ? "blue.600" : "black"}
                    bg={isSelected ? "blue.100" : "white"}
                    _hover={{
                      bg: isSelected ? "blue.200" : "gray.100",
                      color: isSelected ? "blue.700" : "black",
                    }}
                    size="sm"
                    onClick={() => {
                      if (isSelected) {
                        setResourceTypes(
                          resourceTypes.filter((t) => t !== type)
                        );
                      } else {
                        setResourceTypes([...resourceTypes, type]);
                      }
                    }}
                  >
                    {type}
                    {isSelected && <FaCheck color="blue" />}
                  </Button>
                );
              })}
            </HStack>
          </HStack>
        </Box>

        <SimpleGrid
          columns={{ base: 1, md: 2, lg: 3 }}
          mt={4}
          gap={4}
          justifyContent="space-around"
        >
          {resources.map((resource: Resource) => {
            return (
              <ResourceCard
                resource={resource}
                key={resource.id}
                appendTypeToUrl={true}
              />
            );
          })}
        </SimpleGrid>
        {loading && (
          <Box textAlign="center">
            <Spinner />
          </Box>
        )}

        {resources.length === 0 && (
          <Box textAlign="center" color="gray.500">
            <Text textAlign="center" mt={8} mb={4}>
              No resources found with the following criteria:
            </Text>
            <Text>
              Tags:{" "}
              {tags.length > 0 ? (
                <>
                  {tags.map((tag) => (
                    <Code key={tag.id} colorScheme="blue" mr={1}>
                      {tag.text}
                    </Code>
                  ))}
                </>
              ) : (
                <Text as="span">None</Text>
              )}
            </Text>

            <Text>
              Resource Types:{" "}
              {resourceTypes.length > 0 ? (
                <>
                  {resourceTypes.map((type) => (
                    <Code key={type} colorScheme="blue" mr={1}>
                      {type}
                    </Code>
                  ))}
                </>
              ) : (
                <Text as="span">None</Text>
              )}
            </Text>
          </Box>
        )}
      </Container>
    </>
  );
};
