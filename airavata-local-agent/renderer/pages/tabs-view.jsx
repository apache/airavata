import { useEffect, useState } from "react";
import {
  Tabs, TabList, TabPanels, Tab, TabPanel, Button, Box, Spacer, Table,
  Thead,
  Tbody,
  Tfoot,
  Tr,
  Th,
  Td,
  TableCaption,
  TableContainer, Img, Text, Flex, Spinner, Link, HStack, VStack, Stack, Badge, Icon
} from "@chakra-ui/react";
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

const getExperimentApplication = (executionId) => {


  if (executionId.startsWith("AlphaFold2")) {
    return "AlphaFold2";
  } else if (executionId.startsWith("NAMD3_gpu")) {
    return "NAMD3 GPU";
  } else if (executionId.startsWith("NAMD_Diego")) {
    return "NAMD3 Single Node";
  } else if (executionId.startsWith("NAMD3")) {
    return "NAMD3";
  } else if (executionId.startsWith("NAMD")) {
    return "NAMD";
  }


};

const tabSelectedStyles = {
  bg: 'blue.100',
};

let accessToken = '';

const makeFetchForExperiments = async (pageSize, offset) => {
  let resp = await fetch(
    `https://md.cybershuttle.org/api/experiment-search/?format=json&limit=${pageSize}&offset=${offset}`, {
    headers: {
      'Authorization': `Bearer ${accessToken}`,
    },
  });

  return resp;
};
const fetchExperiments = async (pageSize, offset) => {
  if (!accessToken) {
    accessToken = localStorage.getItem('accessToken');
  }

  let resp = await makeFetchForExperiments(pageSize, offset);

  // if this fetch request fails, try again after getting a new access token
  if (!resp.ok) {
    let refreshToken = localStorage.getItem('refreshToken');
    const respForRefresh = await fetch(`http://localhost:3000/get-token-from-refresh-token?refresh_token=${refreshToken}`);

    if (!respForRefresh.ok) {
      console.error('Failed to get new access token');
      return;
    }


    const data = await respForRefresh.json();

    console.log(data);

    localStorage.setItem('accessToken', data.access_token);
    localStorage.setItem('refreshToken', data.refresh_token);

    accessToken = data.access_token;
    resp = await makeFetchForExperiments(pageSize, offset);
    if (!resp.ok) {
      console.error('Failed to fetch experiments');
      return;
    }
  }

  const data = await resp.json();



  return data;
};

const associatedIDToIndex = {}; // 'VMD_adfasdfsdf' => 1

const TabsView = () => {
  const [tabIndex, setTabIndex] = useState(0);
  const [arrOfTabsInfo, setArrOfTabsInfo] = useState([]);
  const [experiments, setExperiments] = useState([]);
  const [isLoading, setIsLoading] = useState(true);

  const {
    pages,
    pagesCount,
    offset,
    currentPage,
    setCurrentPage,
    setIsDisabled,
    isDisabled,
    pageSize,
    setPageSize,
  } = usePagination({
    initialState: {
      pageSize: 10,
      isDisabled: false,
      currentPage: 1,
    },
  });

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
    setIsLoading(true);
    fetchExperiments(pageSize, offset)
      .then((data) => {
        setExperiments(data);
        setIsLoading(false);
      })
      .catch((error) => console.error("App =>", error));
  }, [currentPage, pageSize, offset]);

  const handlePageChange = (nextPage) => {
    // -> request new data using the page number
    setCurrentPage(nextPage);
    console.log("request new data with ->", nextPage);
  };

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
        </Flex>

        <TabPanels>
          <TabPanel>
            <Pagination
              currentPage={currentPage}
              isDisabled={isDisabled}
              onPageChange={handlePageChange}
            >
              <TableContainer>
                <Table variant='simple'>
                  <Thead>
                    <Tr>
                      <Th>Name</Th>
                      <Th>User</Th>
                      <Th>Type</Th>
                      <Th>Time</Th>
                      <Th>Status</Th>
                      <Th>Actions</Th>
                    </Tr>
                  </Thead>
                  <Tbody>
                    {
                      experiments?.results?.map((experiment) => {
                        return (
                          <Tr key={experiment.experimentId} fontSize='sm' alignItems='center'>
                            <Td>
                              <Box>
                                <Text whiteSpace='pre-wrap'>{experiment.name}</Text>
                              </Box>
                            </Td>

                            <Td>
                              <Text>{experiment.userName}</Text>
                            </Td>

                            <Td>
                              <Text>{getExperimentApplication(experiment.executionId)}</Text>
                            </Td>

                            <Td>
                              <Text>{dateToAgo(new Date(experiment.statusUpdateTime))} ago</Text>
                            </Td>

                            <Td>
                              <Badge colorScheme={getColorScheme(experiment.experimentStatus)}>{experiment.experimentStatus}</Badge>
                            </Td>

                            <Td>
                              <HStack>
                                <Button colorScheme='orange' size='xs' onClick={() => {
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
                                    <Button colorScheme='blue' size='xs' onClick={() => {
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
                            </Td>

                          </Tr>
                        );
                      })
                    }


                  </Tbody>
                </Table>
              </TableContainer>
              <Flex align='center' gap={4} justify='center' mt={2}>
                <PaginationPrevious
                  _hover={{
                    bg: "blue.300",
                  }}
                  bg="blue.200"
                  onClick={() => console.warn("I'm clicking the previous")}
                >
                  <Text>Previous</Text>
                </PaginationPrevious>

                {
                  isLoading ? <Spinner /> : <Text>Showing {(currentPage - 1) * pageSize} to {(currentPage) * pageSize - 1}</Text>}


                <PaginationNext
                  _hover={{
                    bg: "blue.300",
                  }}
                  bg="blue.200"
                  onClick={() => console.warn("I'm clicking the next")}
                  isDisabled={experiments?.results?.length < pageSize}
                >
                  <Text>Next</Text>
                </PaginationNext>


              </Flex>


            </Pagination>

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
      </Tabs >


      <Link href='/vnc-client'>VNC Client</Link>
    </>
  );
};

export default TabsView;