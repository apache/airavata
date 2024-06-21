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
  Input,
  useToast,
  Tooltip,
  Modal,
  ModalOverlay,
  ModalContent,
  ModalHeader,
  ModalFooter,
  ModalBody,
  ModalCloseButton,
  useDisclosure,
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
import relativeTime from "dayjs/plugin/relativeTime";
import dayjs from "dayjs";
import ExperimentModal from "../components/ExperimentModal";
dayjs.extend(relativeTime);

const getColorScheme = (status) => {
  switch (status) {
    case 'COMPLETED':
      return 'green';
    case 'EXECUTING':
      return 'gray';
    case 'CREATED':
      return 'blue';
    case 'CANCELED':
      return 'yellow';
    case 'FAILED':
      return 'red';
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

const isValidStatusVMD = (status) => {
  let invalidStatus = ["CANCELED"]; // TODO here
  return !invalidStatus.includes(status);
};

// jupyterlab CAN have VMD



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
  const [activeExperimentId, setActiveExperimentId] = useState("");
  const toast = useToast();
  const { isOpen, onOpen, onClose } = useDisclosure();


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

  const makeFetchForExperiments = async (pageSize, offset, token, filterString) => {
    let resp = await fetch(
      `https://md.cybershuttle.org/api/experiment-search/?format=json&limit=${pageSize}&offset=${offset}&${filterString}`, {
      headers: {
        'Authorization': `Bearer ${token}`,
      },
    });

    if (resp.status === 500) {
      toast({
        title: "Your account does not exist in the system yet. Please contact the administrator.",
        status: "error",
        duration: 9000,
        isClosable: true,
      });

      setIsLoading(false);
    }

    return resp;
  };


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
        "application": "VMD",
        "wallTimeLimit": 60, // TODO: get this from the user
      };

      const url = "http://74.235.88.134:9001/api/v1/application/launch";

      if (type === 'VMD') {
        const resp = await fetch(url, {
          method: "POST",
          headers: headers,
          body: JSON.stringify(body)
        });

        if (!resp.ok) {
          setIsLoadingSession(false);
          toast({
            title: "Failed to launch VMD",
            status: "error",
            duration: 3000,
            isClosable: true,
          });
          return;
        }
        const data = await resp.json();
        let hostURL = "18.217.79.150";
        let applicationId = data?.applicationId;
        let port;

        if (data?.allocatedPorts) {
          port = data.allocatedPorts[0];
        }


        component = <VNCViewer applicationId={applicationId} reqHost={hostURL} reqPort={port} experimentId={experimentID} headers={headers} />;
      } else if (type === 'JN') {

        body["application"] = "JUPYTER_LAB";
        const resp = await fetch(url, {
          method: "POST",
          headers: headers,
          body: JSON.stringify(body)
        });

        // url 

        // 18.217.79.150:8888
        // 8888 = port
        // http://18.217.79.150:8888

        component = <iframe src={'https://jupyter.org/try-jupyter/lab/'} width='100%' height='600px'></iframe>;
      }

      const newTabIndex = arrOfTabsInfo.length + 1; // account for List Experiments being 0 index
      associatedIDToIndex[associatedID] = newTabIndex;


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
    setIsLoading(true);
    const urlParams = new URLSearchParams();
    addPropertyToParamsIfNotDefault(urlParams, filterCriteria, "STATUS", "ALL");
    addPropertyToParamsIfNotDefault(urlParams, filterCriteria, "USER_NAME", "");
    addPropertyToParamsIfNotDefault(urlParams, filterCriteria, "EXPERIMENT_NAME", "");
    addPropertyToParamsIfNotDefault(urlParams, filterCriteria, "EXPERIMENT_DESC", "");
    addPropertyToParamsIfNotDefault(urlParams, filterCriteria, "APPLICATION_ID", "");
    addPropertyToParamsIfNotDefault(urlParams, filterCriteria, "PROJECT_ID", "");
    addPropertyToParamsIfNotDefault(urlParams, filterCriteria, "JOB_ID", "");

    let resp = await makeFetchForExperiments(pageSize, offset, accessToken, urlParams.toString());

    // if this fetch request fails, try again after getting a new access token
    if (!resp.ok) {
      let refreshToken = localStorage.getItem('refreshToken');

      const [newAccessToken, newRefreshToken] = await getAccessTokenFromRefreshToken(refreshToken);

      if (!newAccessToken || !newRefreshToken) {
        toast({
          title: "Something went wrong.",
          description: "If you just made your account, please wait a few minutes and try again, as your account is still being set up. If you have been using the app for a while, please log out and log back in as your session may have expired.",
          status: "error",
          duration: 10000,
          isClosable: true,
        });
        setIsLoading(false);
        return;

      }
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
    setExperiments(data);
    setIsLoading(false);
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
    accessToken = localStorage.getItem("accessToken");
    fetchExperiments(pageSize, offset, getFilterObj())
      .catch((error) => {
        console.error("App =>", error);
        // window.location.href = "/login";
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
    if (filterText === "" && filterAttribute !== "") {
      toast({
        title: "Please enter a search text",
        status: "error",
        duration: 3000,
        isClosable: true,
      });
      return;
    } else if (filterText !== "" && filterAttribute === "") {
      toast({
        title: "Please select a search attribute",
        status: "error",
        duration: 3000,
        isClosable: true,
      });
      return;
    } else if (filterText === "" && filterAttribute === "" && filterStatus === "") {
      toast({
        title: "Please enter a search text or select a search attribute or select an experiment status",
        status: "error",
        duration: 3000,
        isClosable: true,
      });
      return;
    }

    setIsLoading(true);
    setCurrentPage(1);
    fetchExperiments(pageSize, 0, getFilterObj())
      .catch((error) => {
        console.error("App =>", error);
        // window.location.href = "/login";
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
              <Spinner mr={2} />
              <Text>
                Currently loading your session, this may take one or two minutes...
              </Text>
            </Alert>
          </>
        )
      }

      <Modal isOpen={isOpen} onClose={onClose} size='full'>
        <ModalOverlay />
        <ModalContent>
          <ModalHeader>Experiment Details</ModalHeader>
          <ModalCloseButton />
          <ModalBody>
            {
              isOpen && (
                <ExperimentModal experimentId={activeExperimentId} />
              )
            }
          </ModalBody>

          <ModalFooter>
            <Button colorScheme='blue' mr={3} onClick={onClose}>
              Close
            </Button>
            <Button variant='ghost'>Secondary Action</Button>
          </ModalFooter>
        </ModalContent>
      </Modal>


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
            <Grid templateColumns='repeat(4, 1fr)' gap={2} mb={4}>
              <GridItem w='100%' h='10'>
                <Input size='sm' placeholder="Search Text" value={filterText} onChange={(e) => setFilterText(e.target.value)} />
              </GridItem>
              <GridItem w='100%' h='10' >
                <Select size='sm' variant='outline' placeholder='Search Attribute' value={filterAttribute} onChange={(e) => setFilterAttribute(e.target.value)}>
                  <option value="USER_NAME">User Name</option>
                  <option value="EXPERIMENT_NAME">Experiment Name</option>
                  <option value="EXPERIMENT_DESC">Experiment Description</option>
                  {/* <option value="APPLICATION_ID">Application ID</option>
                  <option value="PROJECT_ID">Project ID</option> */}
                  <option value="JOB_ID">Job ID</option>
                </Select>

              </GridItem>
              <GridItem w='100%' h='10'>
                <Select size='sm' variant='outline' placeholder='Experiment Status' value={filterStatus} onChange={(e) => setFilterStatus(e.target.value)}>
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
                  <Button size='sm' w='full' onClick={() => {
                    setCurrentPage(1);
                    setFilterAttribute("");
                    setFilterText("");
                    setFilterStatus("");
                    fetchExperiments(pageSize, 0, {});
                  }}>Reset</Button>

                  <Button size='sm' w='full' onClick={handleFilterChange} _hover={{
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
                      <Th>Created</Th>
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
                                <Tooltip label={experiment.experimentId}>
                                  <Text whiteSpace='pre-wrap'
                                    _hover={{
                                      cursor: "pointer",
                                    }} transition='all .2s' onClick={() => {
                                      setActiveExperimentId(experiment.experimentId);
                                      onOpen();
                                    }}
                                  >{experiment.name}
                                  </Text>
                                </Tooltip>
                              </Box>
                            </Td>

                            <Td>
                              <Text>{experiment.userName === email ? "You" : experiment.userName}</Text>
                            </Td>

                            <Td>
                              <Text>{getExperimentApplication(experiment.executionId)}</Text>
                            </Td>

                            <Td>
                              <Tooltip label={new Date(experiment.statusUpdateTime).toLocaleString()}><Text>{dayjs(experiment.statusUpdateTime).fromNow(true)} ago</Text></Tooltip>
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
                                  experiment.executionId?.startsWith('NAMD_') && isValidStatusVMD(experiment.experimentStatus) && (
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
                  // isLoading ? <Spinner /> : <Text>Showing {(currentPage - 1) * pageSize + 1} to {(currentPage) * pageSize - 1 + 1}</Text>}

                  // if there are less than pageSize results, then show the number of results
                  // otherwise, show the range of results
                  isLoading ? <Spinner /> : experiments?.results?.length === 0 ? <Text>No experiments</Text> : experiments?.results?.length < pageSize ? <Text>Showing {(currentPage - 1) * pageSize + 1} to {(currentPage - 1) * pageSize + experiments?.results?.length}</Text> : <Text>Showing {(currentPage - 1) * pageSize + 1} to {(currentPage) * pageSize}</Text>
                }

                <PaginationNext
                  _hover={{
                    bg: "blue.300",
                  }}
                  bg="blue.200"
                  isDisabled={experiments?.next === null}
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