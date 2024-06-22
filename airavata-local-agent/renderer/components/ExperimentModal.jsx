import { Box, Divider, ListItem, Stack, Text, Button, VStack, Badge, UnorderedList, useToast, Link } from "@chakra-ui/react";
import { useEffect, useState } from "react";
import { TextWithBoldKey } from "./TextWithBoldKey";
import { getColorScheme } from "../lib/utilityFuncs";

const ExperimentModal = ({ activeExperiment, onOpen, onClose }) => {
  const toast = useToast();
  const [experimentData, setExperimentData] = useState(null);
  const [loading, setLoading] = useState(false);
  const experimentId = activeExperiment.experimentId;
  const experimentStatus = activeExperiment.experimentStatus;
  const [experimentOutputs, setExperimentOutputs] = useState([]);

  /*
  {
    name: "Coordinate_Files",
    shouldDisplayText: true/false depending on metadata is null or not,
    output: the text to display // only present if shouldDisplay is true,
    uris

  }
  */

  useEffect(() => {
    // Fetch data here
    let accessToken = localStorage.getItem("accessToken");

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
      } catch (e) {
        console.log(e);
      }

      setExperimentData(data);
    }

    async function fetchExperimentOutputFiles(experimentOutputs) {
      for (let i = 0; i < experimentOutputs.length; i++) {
        try {
          // console.log(experimentOutputs[i]);
          let outputTypeName = experimentOutputs[i].name;
          // read experimentOutputs[i] as a comman separated string into a list

          let shouldDisplayText = experimentOutputs[i].metaData !== null;

          if (shouldDisplayText) {
            console.log(experimentOutputs[i]);
            // only need to grab the URI
            let dataUri = experimentOutputs[i].value;

            let resp = await fetch(`https://md.cybershuttle.org/sdk/download-file/?data-product-uri=${dataUri}`, {
              headers: {
                Authorization: `Bearer ${accessToken}`
              }
            });

            const fileName = await resp.headers.get('Content-Disposition').split('filename=')[1].replaceAll('"', '');
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
                let resp = await fetch(`https://md.cybershuttle.org/sdk/download-file/?data-product-uri=${uris[j]}`, {
                  headers: {
                    Authorization: `Bearer ${accessToken}`
                  }
                });

                const fileName = await resp.headers.get('Content-Disposition').split('filename=')[1].replaceAll('"', '');
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
              <Button colorScheme='blue' size='sm' onClick={async () => {
                setLoading(true);
                let accessToken = localStorage.getItem("accessToken");
                const resp = await fetch(`https://md.cybershuttle.org/api/experiments/${experimentId}/launch/`, {
                  method: 'POST',
                  headers: {
                    Authorization: `Bearer ${accessToken}`
                  }
                });
                if (!resp.ok) {
                  console.log("Error launching experiment");
                  toast({
                    title: "Error launching experiment",
                    status: "error",
                    duration: 3000,
                    isClosable: true
                  });
                  return;
                }
                const data = await resp.json();

                if (data?.success) {
                  console.log("Experiment launched successfully");
                  toast({
                    title: "Experiment launched successfully",
                    status: "success",
                    duration: 3000,
                    isClosable: true
                  });

                  setRefreshData(Math.random());
                  onClose();
                }

                setLoading(false);
              }} isDisabled={
                loading
              }>
                {
                  loading ? "Launching..." : "Launch Experiment"
                }
              </Button>
            }

            {
              experimentStatus === "EXECUTING" &&
              <Button colorScheme='red'
                onClick={async () => {
                  setLoading(true);
                  let accessToken = localStorage.getItem("accessToken");
                  const resp = await fetch(`https://md.cybershuttle.org/api/experiments/${experimentId}/cancel/`, {
                    method: 'POST',
                    headers: {
                      Authorization: `Bearer ${accessToken}`
                    }
                  });
                  if (!resp.ok) {
                    console.log("Error canceling experiment");
                    toast({
                      title: "Error canceling experiment",
                      status: "error",
                      duration: 3000,
                      isClosable: true
                    });
                    return;
                  }
                  const data = await resp.json();

                  if (data?.success) {
                    console.log("Experiment canceled successfully");
                    toast({
                      title: "Experiment canceled successfully",
                      status: "success",
                      duration: 3000,
                      isClosable: true
                    });
                    onClose();
                  }
                }} isDisabled={
                  loading
                }
                size='sm'
              >{
                  loading ? "Canceling..." : "Cancel Experiment"
                }</Button>
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
          <Text fontWeight='bold'>Inputs ({experimentData.processes.length} total processes)</Text>

          {
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
          }
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