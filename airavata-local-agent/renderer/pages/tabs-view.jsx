import { useEffect, useState } from "react";
import { Tabs, TabList, TabPanels, Tab, TabPanel, Button, Box, Spacer, Container, Img, Text, Flex, Spinner, Link, HStack, VStack, Stack, Badge, Icon } from "@chakra-ui/react";
import { SAMPLE_JSON_RESPONSE, dateToAgo, truncTextToN } from "../lib/utilityFuncs";
import { FaHome } from "react-icons/fa";
import { IoClose } from "react-icons/io5";
import {
  Pagination,
  usePagination,
  PaginationNext,
  PaginationPage,
  PaginationPrevious,
  PaginationContainer,
  PaginationPageGroup,
} from "@ajna/pagination";

const getColorScheme = (status) => {
  switch (status) {
    case 'COMPLETED':
      return 'green';
    case 'CREATED':
      return 'blue';
    default:
      return 'red';
  }
};

const tabSelectedStyles = {
  bg: 'blue.100',
};



const associatedIDToIndex = {}; // 'VMD_adfasdfsdf' => 1

const TabsView = () => {
  const [tabIndex, setTabIndex] = useState(0);
  const [arrOfTabsInfo, setArrOfTabsInfo] = useState([]);
  const [experiments, setExperiments] = useState([]);

  /*
    arrOfTabsInfo looks like array of: 
    {
      'associatedID': 'VMD' + experimentId,
      'tabName': 'VMD' + name,
      'component': <VMDComponent />
    }
  */

  const handleRemoveTab = (associatedID, index) => {
    /*
    - when you close a tab
    - each tab needs to have an event listener that calls a deleteTab function with the associated experiment ID
        - remove that ID from the tabsList
        - delete the entry from the hashmap
        - go through the hashmap and decrement all indexes greater the one that was deleted
    */

    delete associatedIDToIndex[associatedID];
    setArrOfTabsInfo(oldArr => oldArr.filter((tabInfo, i) => tabInfo.associatedID !== associatedID));

    setTabIndex(oldIndex => {
      if (index == tabIndex) {
        return 0;
      }
      return oldIndex;
    });

    for (const [key, value] of Object.entries(associatedIDToIndex)) {
      if (value > index) {
        associatedIDToIndex[key] = value - 1;
      }
    }
  };

  const handleAddTab = (type, experimentID, name) => {
    const associatedID = type + "_" + experimentID;

    /*
    - each time when clicking on an experiment,
    - you add an entry to the hashmap (index is length of tabs list)
        - if already exists, set index state to the value in the hashmap and DONE
    - add an entry to tabs list with associated info
    - set the index state to be the newly index and DONE
    */

    if (associatedID in associatedIDToIndex) {
      setTabIndex(associatedIDToIndex[associatedID]);
    } else {
      const newTabIndex = arrOfTabsInfo.length + 1; // account for List Experiments being 0 index
      associatedIDToIndex[associatedID] = newTabIndex;

      let component;
      if (type === 'VMD') {
        component = <Box>{type} {experimentID}</Box>;
      } else if (type === 'JN') {
        component = <iframe src={'https://jupyter.org/try-jupyter/lab/'} width='100%' height='600px'></iframe>;
      }

      setArrOfTabsInfo(oldArr => [...oldArr, {
        associatedID: associatedID,
        tabName: type + ' ' + name,
        component: component
      }]);

      setTabIndex(newTabIndex);
    }
  };

  const handleTabsChange = (index) => {
    setTabIndex(index);
  };

  const isOpenTab = (type, experimentID) => {
    return (type + "_" + experimentID) in associatedIDToIndex;
  };

  useEffect(() => {
    // fetch experiments
    async function fetchExperiments() {
      try {
        const accessToken = localStorage.getItem('accessToken');
        console.log(accessToken);

        // access token should be sent as bearer token
        const response = await fetch('https://md.cybershuttle.org/api/experiment-search/?format=json', {
          headers: {
            'Authorization': `Bearer ${accessToken}`,
          },
        });

        const data = await response.json();

        setExperiments(data);
        // const data = await response.json();
        // console.log(data);
      } catch (error) {
        console.error(error);
      }
    }

    fetchExperiments();
  }, []);

  return (
    <>
      <Tabs index={tabIndex} onChange={handleTabsChange}>
        <Flex alignItems='center' gap={2}>
          <TabList flex='11' alignItems='center' direction="column-reverse" overflowX='scroll' overflowY='hidden'>
            <Tab _selected={tabSelectedStyles} minW='200px'>
              <Icon as={FaHome} mr={2} />
              List Experiments</Tab>

            {
              arrOfTabsInfo.map((tabInfo, index) => {
                return (
                  <Tab key={tabInfo.associatedID} _selected={tabSelectedStyles}>
                    <Text whiteSpace='nowrap'>{truncTextToN(tabInfo.tabName, 20)}</Text>
                    <Icon as={IoClose} transition='all .2s' onClick={() => {
                      handleRemoveTab(tabInfo.associatedID, index + 1);
                    }} _hover={{
                      color: 'red.500',
                    }} />
                  </Tab>
                );
              })
            }
          </TabList>

          {/* 
          <Box>
            <Flex alignItems='center' gap={4}>
              <Img src="/images/a-logo.png" maxH='45px' />
            </Flex>
          </Box> */}
        </Flex>

        <TabPanels>
          <TabPanel>

            <Stack direction='column' spacing={4}>
              {
                experiments?.results?.map((experiment) => {
                  return (
                    <Box p={4} bg='gray.100' rounded='md' key={experiment.experimentId}>
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
                        <Button colorScheme='orange' size='sm' onClick={() => {
                          handleAddTab('JN', experiment.experimentId, experiment.name);
                        }}>

                          Jupyter

                          {
                            isOpenTab('JN', experiment.experimentId) &&
                            <Spinner ml={2} />
                          }

                        </Button>
                        {
                          // only show jupyter button if executionId starts with "NAMD_*".
                          experiment.executionId.startsWith('NAMD_') && (
                            <Button colorScheme='blue' size='sm' onClick={() => {
                              handleAddTab('VMD', experiment.experimentId, experiment.name);
                            }}
                            >
                              VMD

                              {
                                isOpenTab('VMD', experiment.experimentId) &&
                                <Spinner ml={2} />
                              }</Button>
                          )
                        }
                      </HStack>
                    </Box>
                  );
                })
              }
            </Stack>

            <Text mt={4} textAlign='center' fontStyle='italic'>{experiments?.results?.length} experiments total</Text>
          </TabPanel>

          {
            arrOfTabsInfo.map((tabInfo) => {
              return (
                <TabPanel key={tabInfo.associatedID}>
                  {tabInfo.component}
                </TabPanel>
              );
            })
          }
        </TabPanels>
      </Tabs>


      <Link href='/vnc-client'>VNC Client</Link>
    </>
  );
};

export default TabsView;