import { Box, Divider, ListItem, Stack, Text, UnorderedList, VStack } from "@chakra-ui/react";
import { useEffect, useState } from "react";
import { TextWithBoldKey } from "./TextWithBoldKey";

const ExperimentModal = ({ experimentId }) => {
  const [experimentData, setExperimentData] = useState(null);


  // Rest of your code here

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

      console.log(data);
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
        <TextWithBoldKey keyName="Name" text={experimentData.experimentName} />

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