import { Tabs, TabList, TabPanels, Tab, TabPanel, Button, Box, Spacer, Container, Img, Text, Flex, Heading, Link, HStack, VStack, Stack, Badge } from "@chakra-ui/react";
import { NavContainer } from "../components/NavContainer";
import { dateToAgo, SAMPLE_JSON_RESPONSE } from "../lib/utilityFuncs";

const fetchData = async (
  pageSize,
  offset
) =>
{
  return await fetch(
    `https://pokeapi.co/api/v2/pokemon?limit=${pageSize}&offset=${offset}` // TODO: replace this URL with the actual one
  ).then(async (res) => await res.json());
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