import { useEffect, useState } from "react";
import {
  Tabs, TabList, TabPanels, Tab, TabPanel, Button, Box, Table,
  Thead,
  Tbody,
  Tr,
  Th,
  Td,
  TableContainer, Text, Flex, Spinner, HStack, Badge, Icon,
  Alert, Select, Grid, GridItem,
  Input
} from "@chakra-ui/react";
import { dateToAgo, truncTextToN } from "../lib/utilityFuncs";
import { FaHome } from "react-icons/fa";
import { IoClose } from "react-icons/io5";
import {
  Pagination,
  usePagination,
  PaginationNext,
  PaginationPrevious,
} from "@ajna/pagination";
import { HeaderBox } from "../components/HeaderBox";
import { Footer } from "../components/Footer";
import { VNCViewer } from "../components/VNCViewer";

const getColorScheme = (status) => {
  switch (status) {
    case 'COMPLETED':
      return 'green';
    case 'EXECUTING':
      return 'yellow';
    case 'CREATED':
      return 'blue';
    default:
      return 'red';
  }
};

const getExperimentApplication = (executionId) => {
  if (!executionId) {
    return ("N/A");
  } else if (executionId.startsWith("AlphaFold2")) {
    return "AlphaFold2";
  } else if (executionId.startsWith("NAMD3_gpu")) {
    return "NAMD3 GPU";
  } else if (executionId.startsWith("NAMD_Diego")) {
    return "NAMD3 Single Node";
  } else if (executionId.startsWith("NAMD3")) {
    return "NAMD3";
  } else if (executionId.startsWith("NAMD")) {
    return "NAMD";
  } else if (executionId.startsWith("VMD")) {
    return "VMD";
  }
};

const tabSelectedStyles = {
  bg: 'blue.100',
};

const makeFetchForExperiments = async (pageSize, offset, token, filterString) => {
  let resp = await fetch(
    `https://md.cybershuttle.org/api/experiment-search/?format=json&limit=${pageSize}&offset=${offset}&${filterString}`, {
    headers: {
      'Authorization': `Bearer ${token}`,
    },
  });

  return resp;
};

const associatedIDToIndex = {}; // 'VMD_adfasdfsdf' => 1
let accessToken = "";
let gatewayId = "";

const TabsView = () => {
  const [tabIndex, setTabIndex] = useState(0);
  const [arrOfTabsInfo, setArrOfTabsInfo] = useState([]);
  /*
   arrOfTabsInfo looks like array of: 
   {
     'associatedID': 'VMD' + experimentId,
     'tabName': 'VMD' + name,
     'component': <VMDComponent />
   }
 */

  const [experiments, setExperiments] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [isLoadingSession, setIsLoadingSession] = useState(false);

  const [filterAttribute, setFilterAttribute] = useState("");
  const [filterStatus, setFilterStatus] = useState("");
  const [filterText, setFilterText] = useState("");

  const {
    offset,
    currentPage,
    setCurrentPage,
    isDisabled,
    pageSize,
  } = usePagination({
    initialState: {
      pageSize: 10,
      isDisabled: false,
      currentPage: 1,
    },
  });


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

  const handleAddTab = async (type, experimentID, name) => {
    setIsLoadingSession(true);
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
      const headers = {
        "Authorization": "Bearer " + accessToken,
        "X-Claims": JSON.stringify({
          "userName": email,
          "gatewayID": gatewayId
        }),
        "Content-Type": "application/json"
      };

      const body = {
        "expId": experimentID,
        "application": "VMD"
      };

      const url = "http://74.235.88.134:9001/api/v1/application/launch";

      if (type === 'VMD') {
        const resp = await fetch(url, {
          method: "POST",
          headers: headers,
          body: JSON.stringify(body)
        });

        const data = await resp.json();
        let hostURL = "74.235.88.134";
        let port = data.allocatedPorts[0];

        component = <VNCViewer reqHost={hostURL} reqPort={port} experimentId={experimentID} />;
      } else if (type === 'JN') {

        body["application"] = "JUPYTER_LAB";
        const resp = await fetch(url, {
          method: "POST",
          headers: headers,
          body: JSON.stringify(body)
        });

        component = <iframe src={'https://jupyter.org/try-jupyter/lab/'} width='100%' height='600px'></iframe>;
      }

      setArrOfTabsInfo(oldArr => [...oldArr, {
        associatedID: associatedID,
        tabName: type + ' ' + name,
        component: component
      }]);

      setTabIndex(newTabIndex);
    }

    setIsLoadingSession(false);
  };

  const handleTabsChange = (index) => {
    setTabIndex(index);
  };

  const isOpenTab = (type, experimentID) => {
    return (type + "_" + experimentID) in associatedIDToIndex;
  };

  const getAccessTokenFromRefreshToken = async (refreshToken) => {
    const respForRefresh = await fetch(`https://md.cybershuttle.org/auth/get-token-from-refresh-token?refresh_token=${refreshToken}`);

    if (!respForRefresh.ok) {
      throw new Error("Failed to fetch new access token (refresh token)");
    }

    const data = await respForRefresh.json();
    return [data.access_token, data.refresh_token];
  };

  const addPropertyToParamsIfNotDefault = (params, obj, key, defaultVal) => {
    if (obj && obj[key] !== undefined && obj[key] !== "" && obj[key] !== defaultVal) {
      params.set(key, obj[key]);
    }
  };

  const fetchExperiments = async (pageSize, offset, filterCriteria) => {
    console.log(filterCriteria);
    const urlParams = new URLSearchParams();
    addPropertyToParamsIfNotDefault(urlParams, filterCriteria, "STATUS", "ALL");
    addPropertyToParamsIfNotDefault(urlParams, filterCriteria, "USER_NAME", "");
    addPropertyToParamsIfNotDefault(urlParams, filterCriteria, "EXPERIMENT_NAME", "");
    addPropertyToParamsIfNotDefault(urlParams, filterCriteria, "EXPERIMENT_DESC", "");
    addPropertyToParamsIfNotDefault(urlParams, filterCriteria, "APPLICATION_ID", "");
    addPropertyToParamsIfNotDefault(urlParams, filterCriteria, "PROJECT_ID", "");
    addPropertyToParamsIfNotDefault(urlParams, filterCriteria, "JOB_ID", "");

    console.log(urlParams.toString());

    let resp = await makeFetchForExperiments(pageSize, offset, accessToken, urlParams.toString());

    // if this fetch request fails, try again after getting a new access token
    if (!resp.ok) {
      let refreshToken = localStorage.getItem('refreshToken');

      const [newAccessToken, newRefreshToken] = await getAccessTokenFromRefreshToken(refreshToken);

      localStorage.setItem('accessToken', newAccessToken);
      localStorage.setItem('refreshToken', newRefreshToken);

      accessToken = newAccessToken;

      setNameAndEmail();
      resp = await makeFetchForExperiments(pageSize, offset, accessToken, urlParams.toString()); // done with the new accessToken
      if (!resp.ok) {
        throw new Error("Failed to fetch new experiments (new access token)");
      }
    }

    const data = await resp.json();
    return data;
  };

  const setNameAndEmail = () => {
    try {
      if (!accessToken) return;
      const obj = JSON.parse(atob(accessToken.split('.')[1]));

      setName(obj.name);
      setEmail(obj.email);
      getGatewayId(obj.email);
    } catch (error) {
      console.error(error);
    }
  };

  useEffect(() => {
    setIsLoading(true);
    accessToken = localStorage.getItem("accessToken");
    fetchExperiments(pageSize, offset, getFilterObj())
      .then((data) => {
        setExperiments(data);
        setIsLoading(false);
      })
      .catch((error) => {
        console.error("App =>", error);
        window.location.href = "/login";
      });
  }, [currentPage, pageSize, offset]);

  const getGatewayId = async (emailAddress) => {
    console.log("email", emailAddress);
    if (!emailAddress) {
      return;
    }
    const resp = await fetch(`https://md.cybershuttle.org/api/user-profiles/${emailAddress}/`, {
      headers: {
        "Authorization": "Bearer " + accessToken
      }
    });

    const data = await resp.json();
    gatewayId = data.gatewayId;
  };

  useEffect(() => {
    setNameAndEmail();
  }, []);

  const getFilterObj = () => {
    const obj = {
      "STATUS": filterStatus.toUpperCase(),
    };

    obj[filterAttribute] = filterText;

    return obj;
  };

  const handleFilterChange = () => {
    setIsLoading(true);
    setCurrentPage(1);
    fetchExperiments(pageSize, 0, getFilterObj()).then((data) => {
      setExperiments(data);
      setIsLoading(false);
    })
      .catch((error) => {
        console.error("App =>", error);
        window.location.href = "/login";
      });
  };

  const handlePageChange = (nextPage) => {
    setCurrentPage(nextPage);
  };

  return (
    <>
      <HeaderBox name={name} email={email} />
      {
        isLoadingSession && (
          <>
            <Alert status='info' rounded='md' mb={2}>
              {/* <AlertIcon /> */}
              <Spinner mr={2} />
              <Text>
                Currently loading your session, this may take one or two minutes...
              </Text>
            </Alert>
          </>
        )
      }

      <Tabs index={tabIndex} onChange={handleTabsChange}>
        <Flex alignItems='center'>
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
            <Grid templateColumns='repeat(4, 1fr)' gap={6}>
              <GridItem w='100%' h='10'>
                <Input placeholder="Search Text" value={filterText} onChange={(e) => setFilterText(e.target.value)} />
              </GridItem>
              <GridItem w='100%' h='10' >
                <Select variant='outline' placeholder='Search Attribute' value={filterAttribute} onChange={(e) => setFilterAttribute(e.target.value)}>
                  <option value="USER_NAME">User Name</option>
                  <option value="EXPERIMENT_NAME">Experiment Name</option>
                  <option value="EXPERIMENT_DESC">Experiment Description</option>
                  <option value="APPLICATION_ID">Application ID</option>
                  <option value="PROJECT_ID">Project ID</option>
                  <option value="JOB_ID">Job ID</option>
                </Select>

              </GridItem>
              <GridItem w='100%' h='10'>
                <Select variant='outline' placeholder='Experiment Status' value={filterStatus} onChange={(e) => setFilterStatus(e.target.value)}>
                  <option value="CREATED">Created</option>
                  <option value="VALIDATED">Validated</option>
                  <option value="SCHEDULED">Scheduled</option>
                  <option value="LAUNCHED">Launched</option>
                  <option value="EXECUTING">Executing</option>
                  <option value="CANCELED">Canceled</option>
                  <option value="COMPLETED">Completed</option>
                  <option value="FAILED">Failed</option>
                </Select>
              </GridItem>
              <GridItem w='100%' h='10'>

                <HStack>
                  <Button w='full' onClick={() => {
                    setCurrentPage(1);
                    setFilterAttribute("");
                    setFilterText("");
                    setFilterStatus("");
                  }}>Reset</Button>

                  <Button w='full' onClick={handleFilterChange} _hover={{
                    bg: "blue.300",
                  }}
                    bg="blue.200">Search</Button>
                </HStack>
              </GridItem>
            </Grid>
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
                              <Text>{experiment.userName === email ? "You" : experiment.userName}</Text>
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
                                  experiment.executionId?.startsWith('NAMD_') && (
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
                {
                  currentPage > 1 && (
                    <Button onClick={() => {
                      handlePageChange(1);
                    }} _hover={{
                      bg: "blue.300",
                    }}
                      bg="blue.100" size='sm'>
                      Back to first
                    </Button>
                  )
                }

                <PaginationPrevious
                  _hover={{
                    bg: "blue.300",
                  }}
                  bg="blue.200"
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
      <Footer />
    </>
  );
};

export default TabsView;