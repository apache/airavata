import { Box, Divider, ListItem, Stack, Text, Button, VStack, Badge, UnorderedList, useToast } from "@chakra-ui/react";
import { useEffect, useState } from "react";
import { TextWithBoldKey } from "./TextWithBoldKey";
import { getColorScheme } from "../lib/utilityFuncs";

const ExperimentModal = ({ activeExperiment, onOpen, onClose }) => {
  const toast = useToast();
  const [experimentData, setExperimentData] = useState(null);
  const [loading, setLoading] = useState(false);
  console.log(activeExperiment);
  const experimentId = activeExperiment.experimentId;
  const experimentStatus = activeExperiment.experimentStatus;

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
      setExperimentData(data);
    }

    fetchExperimentData();
  }, []);

  if (!experimentId || !experimentData) {
    return <h1>Loading...</h1>;
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
                  // Refresh the experiment data
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