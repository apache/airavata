import { Box, Divider, ListItem, Stack, Text, Button, Badge, UnorderedList, useToast, Link } from "@chakra-ui/react";
import { useEffect, useState } from "react";
import { TextWithBoldKey } from "./TextWithBoldKey";
import { getColorScheme } from "../lib/utilityFuncs";

const ExperimentModal = ({ activeExperiment, onOpen, onClose, accessToken }) => {
  const toast = useToast();
  const [experimentData, setExperimentData] = useState(null);
  const [loading, setLoading] = useState(false);
  const experimentId = activeExperiment.experimentId;
  const experimentStatus = activeExperiment.experimentStatus;
  const [experimentOutputs, setExperimentOutputs] = useState([]);
  const [experimentInputList, setExperimentInputList] = useState([]);

  /*
  {
    name: "Coordinate_Files",
    shouldDisplayText: true/false depending on metadata is null or not,
    output: the text to display // only present if shouldDisplay is true,
    uris
  }
  */

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
        duration: 3000,
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
        duration: 3000,
        isClosable: true
      });
      onClose();
    }

    setLoading(false);
  }

  useEffect(() => {
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

        console.log(objToAdd);

        setExperimentInputList((prev) => {
          return [
            ...prev,
            objToAdd
          ];
        });
      }
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
        await fetchExperimentOutputFiles(data.experimentOutputs);
        await fetchExperimentInputs(data.experimentInputs);
      } catch (e) {
        console.log(e);
      }

      setExperimentData(data);
    }

    async function fetchDownloadFromUri(uri) {
      const resp = await fetch(`https://md.cybershuttle.org/sdk/download-file/?data-product-uri=${uri}`, {
        headers: {
          Authorization: `Bearer ${accessToken}`
        }
      });

      return resp;
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
            if (!dataUri) {
              continue;
            }

            const resp = await fetchDownloadFromUri(dataUri);
            const fileName = getFileNameFromHeader(resp.headers.get('Content-Disposition'));
            const text = await resp.text();

            setExperimentOutputs((prev) => {
              return [
                ...prev,
                {
                  outputTypeName: outputTypeName,
                  name: fileName,
                  shouldDisplayText: true,
                  output: text,
                  dataUri: dataUri
                }
              ];
            });
          } else {
            let fileNames = [];
            let uris = [];
            if (!experimentOutputs[i].value.includes(",")) {
              // continue;
            } else {
              uris = experimentOutputs[i].value.split(',');
              // only need to grab filename from header

              for (let j = 0; j < uris.length; j++) {
                const resp = await fetchDownloadFromUri(uris[j]);
                const fileName = getFileNameFromHeader(resp.headers.get('Content-Disposition'));
                fileNames.push(fileName);
              }
            }


            setExperimentOutputs((prev) => {
              return ([
                ...prev,
                {
                  outputTypeName: outputTypeName,
                  name: fileNames,
                  shouldDisplayText: false,
                  output: null,
                  dataUri: uris
                }
              ]);
            });
          }
        } catch (e) {
          console.log(e);
        }
      }
    }

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
                colorScheme='blue'
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

        <Box>
          <Text fontWeight='bold'>Outputs</Text>
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
        </Box>

        <TextWithBoldKey keyName="Name" text={experimentData.experimentName} />

        <TextWithBoldKey keyName="Status" text={<Badge colorScheme={getColorScheme(experimentStatus)}>{experimentStatus}</Badge>} />

        <TextWithBoldKey keyName="Description" text={experimentData.description} />

        <TextWithBoldKey keyName="ExperimentID" text={experimentData.experimentId} />

        <TextWithBoldKey keyName="GatewayID" text={experimentData.gatewayId} />

        <TextWithBoldKey keyName="Creation Time" text={new Date(experimentData.creationTime).toLocaleString()} />

        <TextWithBoldKey keyName="Owner" text={experimentData.userName} />

        <TextWithBoldKey keyName="Application" text={experimentData.userConfigurationData.computationalResourceScheduling.resourceHostId} />

        <TextWithBoldKey keyName="Queue" text={experimentData.userConfigurationData.computationalResourceScheduling.queueName} />

        <TextWithBoldKey keyName="Node Count" text={experimentData.userConfigurationData.computationalResourceScheduling.nodeCount} />

        <TextWithBoldKey keyName="CPU Count" text={experimentData.userConfigurationData.computationalResourceScheduling.totalCPUCount} />

        <TextWithBoldKey keyName="Wall Time Limit" text={experimentData.userConfigurationData.computationalResourceScheduling.wallTimeLimit} />


        <Box>
          <Text fontWeight='bold'>Experiment Inputs</Text>
          <UnorderedList>
            {
              experimentInputList.map((input, index) => {
                return (

                  <ListItem key={index}>
                    <Text>{input.inputName}:</Text>
                    {
                      input.isList ? (
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
                      ) : (
                        <Text>{input.inputValue}</Text>
                      )
                    }

                  </ListItem>
                  // <Box key={index}>

                  // </Box>
                );
              })
            }
          </UnorderedList>


          {/* {
            experimentData.processes.map((process, index) => {
              return (
                <Box key={index}>
                  <Text>{process.processId}</Text>
                  <UnorderedList>
                    {
                      process.processInputs.map((input, index) => {
                        return (
                          <ListItem key={index}>
                            <TextWithBoldKey keyName={input.name} text={input.value} />
                          </ListItem>
                        );
                      })
                    }
                  </UnorderedList>
                </Box>

              );
            })
          } */}
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

      </Stack>


    </Box>
  );
};

export default ExperimentModal;