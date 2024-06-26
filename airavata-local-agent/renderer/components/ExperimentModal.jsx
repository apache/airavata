import {
  Box, Divider, ListItem, Stack, Text, Button, Badge, UnorderedList, useToast, Link, Accordion, AccordionItem, AccordionButton, AccordionPanel, Table,
  Thead,
  Tbody,
  Tr,
  Th,
  Td,
  TableContainer,
  AccordionIcon,
} from "@chakra-ui/react";
import { useEffect, useRef, useState } from "react";
import { TextWithBoldKey } from "./TextWithBoldKey";
import { getColorScheme, getExperimentStatusFromNum, getRelativeTime, getResourceFromId } from "../lib/utilityFuncs";

const ExperimentModal = ({ activeExperiment, onOpen, onClose, accessToken }) => {
  const toast = useToast();
  const [experimentData, setExperimentData] = useState(null);
  const [loading, setLoading] = useState(false);
  const experimentId = activeExperiment.experimentId;
  const [experimentStatus, setExperimentStatus] = useState(activeExperiment.experimentStatus);
  const [experimentOutputs, setExperimentOutputs] = useState([]);
  const [experimentInputList, setExperimentInputList] = useState([]);
  const [experimentJobs, setExperimentJobs] = useState([]);
  const timer = useRef(null);
  /*
  {
    name: "Coordinate_Files",
    shouldDisplayText: true/false depending on metadata is null or not,
    output: the text to display // only present if shouldDisplay is true,
    uris
  }
  */

  async function fetchExperimentJobs() {
    const resp = await fetch(`https://md.cybershuttle.org/api/experiments/${experimentId}/jobs/?format=json`, {
      headers: {
        Authorization: `Bearer ${accessToken}`
      }
    });

    if (!resp.ok) {
      console.log("Error fetching experiment jobs");
      return;
    }

    const data = await resp.json();
    setExperimentJobs(data);
  }

  function processExperimentStatus(status) {
    // get last status in list
    if (!status || status.length === 0) {
      return;
    }
    const lastStatus = status[status.length - 1];
    setExperimentStatus(getExperimentStatusFromNum(lastStatus.state));
  }

  async function fetchExperimentData() {
    const resp = await fetch(`https://md.cybershuttle.org/api/experiments/${experimentId}/?format=json`, {
      headers: {
        Authorization: `Bearer ${accessToken}`
      }
    });
    if (!resp.ok) {
      console.log("Error fetching experiment data");
      return;
    }

    const data = await resp.json();
    try {
      // don't load inputs and outputs here because it will be too slow
      await fetchExperimentJobs();
      processExperimentStatus(data.experimentStatus);
    } catch (e) {
      console.log(e);
    }

    setExperimentData(data);
  }


  async function controlExperiment(action) {
    if (!action || (action !== "launch" && action !== "cancel")) {
      console.log("Invalid action");
      return;
    }

    setLoading(true);
    const resp = await fetch(`https://md.cybershuttle.org/api/experiments/${experimentId}/${action}/`, {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${accessToken}`
      }
    });

    if (!resp.ok) {
      toast({
        title: `Error ${action + "ing"} experiment`,
        status: "error",
        duration: 10000,
        isClosable: true
      });
      return;
    }

    const data = await resp.json();

    if (data?.success) {
      toast({
        title: `Experiment ${action + "ed"} successfully`,
        description: "It may take a few seconds for the status to update.",
        status: "success",
        duration: 10000,
        isClosable: true
      });
      onClose();
    } else {
      toast({
        title: `Error ${action + "ing"} experiment`,
        description: "You may not have the necessary permissions to perform this action.",
        status: "error",
        duration: 10000,
        isClosable: true
      });
    }

    setLoading(false);
  }

  async function fetchExperimentInputs(experimentInputs) {
    if (!experimentInputs || experimentInputs.length === 0) {
      return;
    }

    for (let i = 0; i < experimentInputs.length; i++) {
      let objToAdd = {
        inputName: "",
        inputValue: "",
        isList: false,
        listItems: []
      };

      objToAdd.inputName = experimentInputs[i].name;
      objToAdd.inputValue = experimentInputs[i].value;

      if (isValueUri(experimentInputs[i].value)) {
        const uris = experimentInputs[i].value;
        const delimiter = findDelimiter(uris);
        const uriList = uris.split(delimiter);
        objToAdd.isList = true;

        for (let j = 0; j < uriList.length; j++) {
          const resp = await fetchDownloadFromUri(uriList[j]);
          const fileName = getFileNameFromHeader(resp.headers.get('Content-Disposition'));

          objToAdd.listItems.push({
            name: fileName,
            uri: uriList[j]
          });
        }
      }

      setExperimentInputList((prev) => {
        return [
          ...prev,
          objToAdd
        ];
      });
    }
  }
  async function fetchDownloadFromUri(uri) {
    const resp = await fetch(`https://md.cybershuttle.org/sdk/download-file/?data-product-uri=${uri}`, {
      headers: {
        Authorization: `Bearer ${accessToken}`
      }
    });

    return resp;
  }

  function isValueUri(value) {
    return value && value.startsWith("airavata-dp://");
  }

  function findDelimiter(value) {
    if (value.includes(",")) {
      return ",";
    } else if (value.includes(" ")) {
      return " ";
    }
  }

  function getFileNameFromHeader(contentDisposition) {
    return contentDisposition.split('filename=')[1].replaceAll('"', '');
  }

  async function fetchExperimentOutputFiles(experimentOutputs) {

    for (let i = 0; i < experimentOutputs.length; i++) {
      try {
        let outputTypeName = experimentOutputs[i].name;
        let shouldDisplayText = experimentOutputs[i].metaData !== null;


        if (shouldDisplayText) {
          let dataUri = experimentOutputs[i].value;
          let newObj;

          if (!dataUri || !isValueUri(dataUri)) {
            newObj = {
              outputTypeName: outputTypeName,
              name: null,
              shouldDisplayText: true,
              output: null,
              dataUri: dataUri
            };
          } else {
            const resp = await fetchDownloadFromUri(dataUri);
            const fileName = getFileNameFromHeader(resp.headers.get('Content-Disposition'));
            const text = await resp.text();


            newObj = {
              outputTypeName: outputTypeName,
              name: fileName,
              shouldDisplayText: true,
              output: text,
              dataUri: dataUri
            };
          }

          setExperimentOutputs((prev) => {
            return [
              ...prev,
              newObj
            ];
          });
        } else {
          let fileNames = [];
          let uris = [];
          let newObj;

          if (experimentOutputs[i].value === null) {
            newObj = {
              outputTypeName: outputTypeName,
              name: [],
              shouldDisplayText: false,
              output: null,
              dataUri: null
            };
          } else {
            let delimiter = findDelimiter(experimentOutputs[i].value);
            uris = experimentOutputs[i].value.split(delimiter);

            for (let j = 0; j < uris.length; j++) {
              if (!isValueUri(uris[j])) {
                continue;
              }
              const resp = await fetchDownloadFromUri(uris[j]);
              const fileName = getFileNameFromHeader(resp.headers.get('Content-Disposition'));
              fileNames.push(fileName);
            }

            newObj = {
              outputTypeName: outputTypeName,
              name: fileNames,
              shouldDisplayText: false,
              output: null,
              dataUri: uris
            };
          }


          setExperimentOutputs((prev) => {
            return ([
              ...prev,
              newObj
            ]);
          });
        }
      } catch (e) {
        console.log(e);
      }
    }
  }

  const startAutoUpdate = () => {
    timer.current = setInterval(() => {
      fetchExperimentData()
        .catch((error) => {
          console.error("App =>", error);
          // window.location.href = "/login";
        });
    }, 3000);
  };

  const stopAutoUpdate = () => {
    clearInterval(timer.current);
  };

  useEffect(() => {
    startAutoUpdate();

    return () => {
      stopAutoUpdate();
    };
  });

  useEffect(() => {
    fetchExperimentData();
  }, []);

  if (!experimentId || !experimentData) {
    return <Text>Loading (this may take a moment)...</Text>;
  }

  return (
    <Box>
      <Stack spacing={2} direction='column' divider={<Divider />}>
        {
          (experimentStatus === "CREATED" || experimentStatus === "EXECUTING") &&
          <Box>
            <Text fontWeight='bold' mb={2}>Experiment Actions</Text>

            {
              experimentStatus === "CREATED" &&
              <Button
                colorScheme='green'
                size='sm'
                onClick={() => controlExperiment("launch")}
                isDisabled={loading}
              >
                {
                  loading ? "Launching..." : "Launch Experiment"
                }
              </Button>
            }

            {
              experimentStatus === "EXECUTING" &&
              <Button
                colorScheme='red'
                onClick={() => controlExperiment("cancel")}
                isDisabled={loading}
                size='sm'
              >
                {
                  loading ? "Canceling..." : "Cancel Experiment"
                }
              </Button>
            }
          </Box>
        }

        <TextWithBoldKey keyName="Name" text={experimentData.experimentName} />

        <TextWithBoldKey keyName="Status" text={<Badge colorScheme={getColorScheme(experimentStatus)}>{experimentStatus}</Badge>} />

        <Accordion
          onChange={(lst) => {
            let index = lst[0];
            if (index === 0) {
              console.log("fetching experiment outputs");
              setExperimentOutputs([]);
              fetchExperimentOutputFiles(experimentData.experimentOutputs);
            }
          }}
          allowMultiple
        >
          <AccordionItem border='none'>
            <AccordionButton p={0}>
              <AccordionIcon />
              <Text fontWeight='bold'>Outputs{"  "}
                {
                  experimentData.experimentOutputs.length == 0 && (
                    <Text as='span' color='gray.500'>(No outputs available)</Text>
                  )
                }
              </Text>
            </AccordionButton>



            <AccordionPanel>

              {/* TODO: experiment status does not update bc its passed as a prop */}
              {
                experimentOutputs.map((output, index) => {
                  return (
                    <Box key={index} mb={4}>
                      {
                        !output.shouldDisplayText && (
                          <>
                            <Text>
                              {output.outputTypeName}:{" "}
                            </Text>
                            <UnorderedList>
                              {
                                output.name.map((name, index) => {
                                  return (
                                    <ListItem key={index}>
                                      <Link href={`https://md.cybershuttle.org/sdk/download-file/?data-product-uri=${output.dataUri[index]}`} target="_blank" color='blue.400'>
                                        {name}
                                      </Link>
                                    </ListItem>
                                  );
                                })
                              }
                            </UnorderedList>
                          </>
                        )
                      }
                      {
                        output.shouldDisplayText &&
                        (
                          <>
                            <Text>
                              {output.outputTypeName}:{" "}
                              <Link href={`https://md.cybershuttle.org/sdk/download-file/?data-product-uri=${output.dataUri}`} target="_blank" color='blue.400'>
                                {output.name}
                              </Link>
                            </Text>

                            <Box maxH='300px' overflow="scroll" bg='gray.100' p={4} rounded='md' mt={2}>
                              <Text whiteSpace="pre">{output.output}</Text>
                            </Box>
                          </>)

                      }
                    </Box>
                  );
                })
              }
            </AccordionPanel>
          </AccordionItem>
        </Accordion>

        <Accordion
          onChange={(lst) => {
            let index = lst[0];
            if (index === 0) {
              console.log("fetching experiment inputs");
              setExperimentInputList([]);
              fetchExperimentInputs(
                experimentData.experimentInputs);
            }
          }}
          allowMultiple>
          <AccordionItem border='none'>
            <AccordionButton p={0}>
              <AccordionIcon />
              <Text fontWeight='bold'>Inputs</Text>

            </AccordionButton>

            <AccordionPanel>

              <UnorderedList>
                {
                  experimentInputList.map((input, index) => {
                    return (
                      <ListItem key={index}>
                        <Text>{input.inputName}:{" "}
                          {
                            !input.isList && (
                              <Text as='span'>{input.inputValue}</Text>
                            )
                          }
                        </Text>
                        {
                          input.isList && (
                            <UnorderedList>
                              {
                                input.listItems.map((item, index) => {
                                  return (
                                    <ListItem key={index}>
                                      <Link href={`https://md.cybershuttle.org/sdk/download-file/?data-product-uri=${item.uri}`} target="_blank" color='blue.400'>
                                        {item.name}
                                      </Link>
                                    </ListItem>
                                  );
                                })
                              }
                            </UnorderedList>
                          )
                        }

                      </ListItem>
                    );
                  })
                }
              </UnorderedList>
            </AccordionPanel>
          </AccordionItem>
        </Accordion>



        <TextWithBoldKey keyName="Description" text={experimentData.description} />

        <TextWithBoldKey keyName="ExperimentID" text={experimentData.experimentId} />

        <TextWithBoldKey keyName="Creation Time" text={new Date(experimentData.creationTime).toLocaleString()} />

        <TextWithBoldKey keyName="Owner" text={experimentData.userName} />

        <TextWithBoldKey keyName="Compute Resource ID" text={getResourceFromId(experimentData.userConfigurationData.computationalResourceScheduling.resourceHostId)} />

        <TextWithBoldKey keyName="Queue" text={experimentData.userConfigurationData.computationalResourceScheduling.queueName} />

        <TextWithBoldKey keyName="Node Count" text={experimentData.userConfigurationData.computationalResourceScheduling.nodeCount} />

        <TextWithBoldKey keyName="CPU Count" text={experimentData.userConfigurationData.computationalResourceScheduling.totalCPUCount} />

        <TextWithBoldKey keyName="Total Physical Memory" text={experimentData.userConfigurationData.computationalResourceScheduling.totalPhysicalMemory + " MB"} />

        <TextWithBoldKey keyName="Wall Time Limit" text={experimentData.userConfigurationData.computationalResourceScheduling.wallTimeLimit + " minutes"} />

        <Box>
          <Text fontWeight='bold'>Jobs</Text>

          {experimentJobs && experimentJobs.length > 0 && <TableContainer>
            <Table variant='simple'>
              <Thead>
                <Tr>
                  <Th>Name</Th>
                  <Th>ID</Th>
                  <Th>Status</Th>
                  <Th>Creation Time</Th>
                </Tr>
              </Thead>
              <Tbody>
                {
                  experimentJobs.map((job, index) => {
                    return (
                      <Tr key={index}>
                        <Td>{job.jobName}</Td>
                        <Td>{job.jobId}</Td>
                        <Td>{
                          job.jobStatuses && job.jobStatuses.length > 0 &&
                          job.jobStatuses[job.jobStatuses.length - 1].reason
                        }</Td>
                        <Td>{getRelativeTime(job.creationTime)}</Td>
                      </Tr>
                    );
                  })
                }
              </Tbody>
            </Table>
          </TableContainer>}
        </Box>



        <Box>
          <Text fontWeight='bold'>Errors</Text>
          {
            experimentData.errors.map((error, index) => {
              return (
                <Text key={index}>{error.actualErrorMessage}</Text>
              );
            })
          }
        </Box>

      </Stack >


    </Box >
  );
};

export default ExperimentModal;