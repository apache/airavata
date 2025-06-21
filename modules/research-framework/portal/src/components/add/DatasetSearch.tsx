import {CreateProjectRequest} from "@/interfaces/Requests/CreateProjectRequest";
import {Box, Field, HStack, Input, Spinner, Text, VStack,} from "@chakra-ui/react";
import {LuSearch} from "react-icons/lu";
import {InputGroup} from "../ui/input-group";
import {SetStateAction, useEffect, useState} from "react";
import {DatasetResource} from "@/interfaces/ResourceType";
import {ResourceTypeEnum} from "@/interfaces/ResourceTypeEnum";
import {CONTROLLER} from "@/lib/controller";
import api from "@/lib/api";
import {FaCheck} from "react-icons/fa";

export const DatasetSearchInput = ({
                                     setCreateResourceRequest,
                                   }: {
  setCreateResourceRequest: (
      data: SetStateAction<CreateProjectRequest>
  ) => void;
}) => {
  const [datasetSearch, setDatasetSearch] = useState("");

  const [debounceTimeout, setDebounceTimeout] = useState<NodeJS.Timeout | null>(
      null
  );
  const [selectedDatasets, setSelectedDatasets] = useState<DatasetResource[]>(
      []
  );
  const [results, setResults] = useState<DatasetResource[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setLoading(true);
    if (!datasetSearch) {
      setResults([]);
      setLoading(false);
      return;
    }

    if (debounceTimeout) clearTimeout(debounceTimeout);

    const timeout = setTimeout(async () => {
      try {
        const response = await api.get(`${CONTROLLER.resources}/public/search`, {
          params: {
            type: ResourceTypeEnum.DATASET,
            name: datasetSearch,
          },
        });
        setResults(response.data);
      } catch (error) {
        console.error("Search failed:", error);
        setResults([]);
      } finally {
        setLoading(false);
      }
    }, 1500);

    setDebounceTimeout(timeout);

    return () => clearTimeout(timeout);
  }, [datasetSearch]);

  useEffect(() => {
    const selectedDatasetIds = selectedDatasets.map((dataset) => dataset.id);
    setCreateResourceRequest((prev: CreateProjectRequest) => ({
      ...prev,
      datasetIds: selectedDatasetIds,
    }));
  }, [selectedDatasets]);

  return (
      <VStack align="start" width="full" gap={2}>
        <Field.Root>
          <Field.Label>Dataset (select multiple)</Field.Label>
          <InputGroup startElement={<LuSearch/>} w="full">
            <Input
                value={datasetSearch}
                onChange={(e) => setDatasetSearch(e.target.value)}
                placeholder="Enter dataset name"
            />
          </InputGroup>

          {selectedDatasets.length > 0 && (
              <Box mt={2} w="full">
                <HStack gap={1}>
                  {selectedDatasets.map((res) => {
                    return (
                        <Box
                            key={res.id}
                            borderWidth={1}
                            borderRadius="md"
                            p={2}
                            cursor={"pointer"}
                            _hover={{bg: "red.200"}}
                            onClick={() => {
                              setSelectedDatasets((prev) =>
                                  prev.filter((item) => item.id !== res.id)
                              );
                            }}
                        >
                          <HStack>
                            <FaCheck/>
                            <Text key={res.id} fontSize="sm">
                              {res.name}
                            </Text>
                          </HStack>
                        </Box>
                    );
                  })}
                </HStack>
              </Box>
          )}

          {loading && <Spinner size="sm"/>}

          {!loading && results.length > 0 && (
              <Box mt={2} w="full">
                <VStack align="start" gap={1}>
                  {results.map((res) => {
                    const isSelected = selectedDatasets.some(
                        (item) => item.id === res.id
                    );

                    return (
                        <Box
                            key={res.id}
                            bg={isSelected ? "green.200" : "white"}
                            borderWidth={1}
                            borderRadius="md"
                            p={2}
                            w="full"
                            cursor={"pointer"}
                            _hover={{bg: "gray.100"}}
                            onClick={() => {
                              setSelectedDatasets((prev) => {
                                if (isSelected) {
                                  return prev.filter((item) => item.id !== res.id);
                                } else {
                                  return [...prev, res];
                                }
                              });
                            }}
                        >
                          <Text key={res.id} fontSize="sm">
                            {res.name}
                          </Text>
                        </Box>
                    );
                  })}
                </VStack>
              </Box>
          )}
        </Field.Root>
      </VStack>
  );
};
