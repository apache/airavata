import { Tabs, TabList, TabPanels, Tab, TabPanel, Button, Box, Spacer, Container, Img, Text, Flex, Heading, Link, HStack, VStack, Stack, Badge } from "@chakra-ui/react";
import { NavContainer } from "../components/NavContainer";
import { dateToAgo } from "../lib/utilityFuncs";

const fetchData = async (
  pageSize,
  offset
) =>
{
  return await fetch(
    `https://pokeapi.co/api/v2/pokemon?limit=${pageSize}&offset=${offset}` // TODO: replace this URL with the actual one
  ).then(async (res) => await res.json());
};


const SAMPLE_JSON_RESPONSE = {
  "next": "https://md.cybershuttle.org/api/experiment-search/?limit=10&offset=10",
  "previous": null,
  "results": [
    {
      "experimentId": "Clone_of_Clone_of_Clone_of_NAMD_on_May_9,_2024_3:0_581f67a6-0159-44e1-9a89-09313d19d9e9",
      "projectId": "DimuthuSample_efb0b290-7664-4234-8a48-86f7176c297f",
      "gatewayId": "molecular-dynamics",
      "creationTime": 1715282078000,
      "userName": "dwannipu@iu.edu",
      "name": "Clone of Clone of Clone of NAMD on May 9, 2024 3:07 PM",
      "description": null,
      "executionId": "NAMD_dd041e87-1dde-4e57-8ec4-23af2ffa1ba0",
      "resourceHostId": "NCSADelta_e75b0d04-8b4b-417b-8ab4-da76bbd835f5",
      "experimentStatus": "COMPLETED",
      "statusUpdateTime": 1715282249038,
      "url": "https://md.cybershuttle.org/api/experiments/Clone_of_Clone_of_Clone_of_NAMD_on_May_9%2C_2024_3%3A0_581f67a6-0159-44e1-9a89-09313d19d9e9/",
      "project": "https://md.cybershuttle.org/api/projects/DimuthuSample_efb0b290-7664-4234-8a48-86f7176c297f/",
      "userHasWriteAccess": true
    },
    {
      "experimentId": "Clone_of_Clone_of_NAMD_on_May_9,_2024_3:07_PM_6b2232cc-59bf-4d6c-81a0-7574a19a98ee",
      "projectId": "DimuthuSample_efb0b290-7664-4234-8a48-86f7176c297f",
      "gatewayId": "molecular-dynamics",
      "creationTime": 1715282028000,
      "userName": "dwannipu@iu.edu",
      "name": "Clone of Clone of NAMD on May 9, 2024 3:07 PM",
      "description": null,
      "executionId": "NAMD_dd041e87-1dde-4e57-8ec4-23af2ffa1ba0",
      "resourceHostId": "NCSADelta_e75b0d04-8b4b-417b-8ab4-da76bbd835f5",
      "experimentStatus": "CREATED",
      "statusUpdateTime": 1715282028513,
      "url": "https://md.cybershuttle.org/api/experiments/Clone_of_Clone_of_NAMD_on_May_9%2C_2024_3%3A07_PM_6b2232cc-59bf-4d6c-81a0-7574a19a98ee/",
      "project": "https://md.cybershuttle.org/api/projects/DimuthuSample_efb0b290-7664-4234-8a48-86f7176c297f/",
      "userHasWriteAccess": true
    }
  ]
};

const getColorScheme = (status) =>
{
  switch (status)
  {
    case 'COMPLETED':
      return 'green';
    case 'CREATED':
      return 'blue';
    default:
      return 'red';
  }
};

const ListExperiments = () =>
{
  return (
    <>
      <NavContainer activePage="List Experiments">
        <Stack direction='column' spacing={4}>
          {
            SAMPLE_JSON_RESPONSE.results.map((experiment) =>
            {
              return (
                <Box p={4} bg='gray.100' rounded='md'>
                  <Flex>
                    <Box>
                      <Text fontWeight='bold'>{experiment.name}</Text>
                    </Box>
                    <Spacer />
                    <Box>
                      <Flex gap={2} alignItems='center'>
                        <Text>{dateToAgo(new Date(experiment.statusUpdateTime))} ago</Text>

                        <Badge colorScheme={getColorScheme(experiment.experimentStatus)}>{experiment.experimentStatus}</Badge>

                      </Flex>
                    </Box>
                  </Flex>

                  {experiment.description &&
                    <Box mt={4}>
                      <Text>{experiment.description}</Text>
                    </Box>
                  }

                  <HStack mt={4}>
                    <Button colorScheme='orange' size='sm'>Jupyter</Button>
                    {
                      // only show jupyter button if executionId starts with "NAMD_*".
                      experiment.executionId.startsWith('NAMD_') && (
                        <Button colorScheme='blue' size='sm'>VMD</Button>
                      )
                    }
                  </HStack>



                </Box>
              );
            })
          }
        </Stack>
      </NavContainer>

    </>
  );
};

export default ListExperiments;