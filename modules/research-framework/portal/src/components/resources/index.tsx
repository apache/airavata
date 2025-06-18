import {
  Box,
  Button,
  Code,
  Container,
  Heading,
  HStack,
  Input,
  SimpleGrid,
  Spinner,
  Text,
  VStack,
} from "@chakra-ui/react";
import {useEffect, useState} from "react";
import "./TagInput.css"; // 👈 custom styles
import api from "@/lib/api";
import {CONTROLLER} from "@/lib/controller";
import {ResourceTypeEnum} from "@/interfaces/ResourceTypeEnum";
import {Resource} from "@/interfaces/ResourceType";
import {ResourceCard} from "../home/ResourceCard";
import {FaCheck} from "react-icons/fa";
import {Tag as TagEntity} from "@/interfaces/TagType";
import {useLocation, useNavigate} from "react-router";
import {toaster} from "../ui/toaster";
import {resourceTypeToColor} from "@/lib/util";

const getResources = async (
    types: ResourceTypeEnum[],
    stringTagsArr: string[],
    searchText: string
) => {
  const response = await api.get(`${CONTROLLER.resources}/`, {
    params: {
      type: types.join(","),
      tag: stringTagsArr.join(","),
      nameSearch: searchText,
      pageNumber: 0,
      pageSize: 100,
    },
  });
  return response.data;
};

const getTags = async () => {
  try {
    const response = await api.get(`${CONTROLLER.resources}/tags/all`);
    return response.data;
  } catch (error) {
    console.error("Error fetching:", error);
  }
};

export const Resources = () => {
  const [tags, setTags] = useState<string[]>([]);
  const [suggestions, setSuggestions] = useState<string[]>([]);
  const [resourceTypes, setResourceTypes] = useState<ResourceTypeEnum[]>([]);
  const [resources, setResources] = useState<Resource[]>([]);
  const [loading, setLoading] = useState(false);
  const [hydrated, setHydrated] = useState(false);
  const [searchText, setSearchText] = useState("");
  const location = useLocation();
  const navigate = useNavigate();

  const updateURLWithTags = (updatedTags: string[]) => {
    const params = new URLSearchParams(location.search);

    if (updatedTags.length > 0) {
      params.set("tags", updatedTags.join(","));
    } else {
      params.delete("tags");
    }

    navigate(
        {pathname: location.pathname, search: params.toString()},
        {replace: true}
    );
  };

  const updateURLWithResourceTypes = (
      updatedResourceTypes: ResourceTypeEnum[]
  ) => {
    const params = new URLSearchParams(location.search);

    if (updatedResourceTypes.length > 0) {
      params.set(
          "resourceTypes",
          updatedResourceTypes.map((type) => type).join(",")
      );
    } else {
      params.delete("resourceTypes");
    }

    navigate(
        {pathname: location.pathname, search: params.toString()},
        {replace: true}
    );
  };

  const updateURLWithSearchText = (searchText: string) => {
    const params = new URLSearchParams(location.search);
    if (searchText.length > 0) {
      params.set("searchText", searchText);
    } else {
      params.delete("searchText");
    }

    navigate(
        {pathname: location.pathname, search: params.toString()},
        {replace: true}
    );
  }

  useEffect(() => {
    if (!hydrated) return;
    setLoading(true);

    const handler = setTimeout(() => {
      async function fetchResources() {
        try {
          const resources = await getResources(resourceTypes, tags, searchText);
          setResources(resources.content);
        } catch {
          toaster.create({
            type: "error",
            title: "Error fetching resources",
            description: "An error occurred while fetching resources.",
          });
        } finally {
          setLoading(false);
        }
      }

      fetchResources();
    }, 200);

    return () => clearTimeout(handler);
  }, [resourceTypes, tags, hydrated, searchText]);

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const tagsParam = params.get("tags");
    if (tagsParam) {
      const initialTags = tagsParam.split(",");
      setTags(initialTags);
    } else {
      setTags([]);
    }

    const resourceTypesParam = params.get("resourceTypes");
    if (resourceTypesParam) {
      const initialResourceTypes = resourceTypesParam.split(
          ","
      ) as ResourceTypeEnum[];
      initialResourceTypes.forEach((type) => {
        if (
            !Object.values(ResourceTypeEnum).includes(type as ResourceTypeEnum)
        ) {
          toaster.create({
            type: "error",
            title: "Invalid resource type",
            description: `Invalid resource type: ${type}. Valid types are: ${Object.values(
                ResourceTypeEnum
            ).join(", ")}`,
          });
          return;
        }
      });

      const searchTextParam = params.get("searchText");
      if (searchTextParam) {
        setSearchText(searchTextParam);
      }

      setResourceTypes(initialResourceTypes);
    } else {
      setResourceTypes([]);
    }

    setHydrated(true);
  }, [location.search]);

  useEffect(() => {
    async function fetchTags() {
      const tags: TagEntity[] = await getTags();
      const suggestedTags = tags.map(tag => tag.value);

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
              fontSize={{base: "4xl", md: "5xl"}}
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
            <VStack alignItems={'flex-start'}>
              <Text fontSize="sm" color="gray.500" fontWeight="bold">
                Search by resource title
              </Text>
              <Input
                  rounded={'lg'}
                  placeholder={'Search by resource title'}
                  value={searchText}
                  onChange={(e) => {
                    setSearchText(e.target.value)
                    updateURLWithSearchText(e.target.value)
                  }}
              />
            </VStack>

            <VStack mt={2} alignItems='flex-start'>
              <Text fontSize="sm" color="gray.500" fontWeight="bold">
                Tags Filter
              </Text>

              <HStack wrap={'wrap'}>
                {
                  suggestions.map((tag) => {
                    const isCurrentlyIncluded = tags.includes(tag);
                    return (
                        <Button
                            key={tag}
                            size={'xs'}
                            bg={isCurrentlyIncluded ? 'blue.200' : "transparent"}
                            color={"blue.600"}
                            borderColor={'blue.400'}
                            _hover={{
                              bg: 'blue.400',
                            }}
                            rounded={'lg'}
                            onClick={() => {
                              setTags((prev) => {
                                let newTags = [...prev, tag];
                                if (isCurrentlyIncluded) {
                                  newTags = prev.filter((shouldKeepTag) => tag != shouldKeepTag)
                                }
                                updateURLWithTags(newTags);
                                return newTags;
                              });
                            }}
                        >
                          {tag}
                        </Button>
                    )
                  })
                }
              </HStack>

            </VStack>


            <VStack mt={2} alignItems='flex-start'>
              <Text fontSize="sm" color="gray.500" fontWeight="bold">
                Resource Filter
              </Text>
              <HStack wrap="wrap">
                {labels.map((type) => {
                  const isSelected = resourceTypes.includes(type);
                  const color = resourceTypeToColor(type);
                  return (
                      <Button
                          key={type}
                          variant="outline"
                          color={color + ".600"}
                          bg={isSelected ? color + ".100" : "white"}
                          rounded={'lg'}
                          _hover={{
                            bg: isSelected ? color + ".200" : "gray.100",
                            color: isSelected ? color + ".700" : "black",
                          }}
                          borderColor={color + ".200"}
                          size="sm"
                          onClick={() => {
                            let newResourceTypes = [...resourceTypes, type];

                            if (isSelected) {
                              newResourceTypes = resourceTypes.filter(
                                  (t) => t !== type
                              );
                            }
                            setResourceTypes(newResourceTypes);
                            updateURLWithResourceTypes(newResourceTypes);
                          }}
                      >
                        {type}
                        {isSelected && <FaCheck color={color}/>}
                      </Button>
                  );
                })}
              </HStack>
            </VStack>
          </Box>

          {loading && (
              <Box textAlign="center" mt={2}>
                <Spinner size={'lg'}/>
              </Box>
          )}

          <SimpleGrid
              columns={{base: 1, md: 2, lg: 4}}
              mt={4}
              gap={2}
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
                            <Code key={tag} colorScheme="blue" mr={1}>
                              {tag}
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
