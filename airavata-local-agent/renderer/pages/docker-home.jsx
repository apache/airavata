import { Grid, GridItem, Tabs, useToast, Box, Progress, Text, keyframes, TabPanels, Tab, TabPanel, Stack, Heading, IconButton, Icon } from "@chakra-ui/react";
import { HeaderBox } from "../components/HeaderBox";
import { DockerContainersList } from "../components/DockerComponents/DockerContainersList";
import { useEffect, useState } from "react";
import { AvailablePrograms } from "../components/DockerComponents/AvaliablePrograms";
import { LuContainer } from "react-icons/lu";
import { AiOutlineCode } from "react-icons/ai";
import { useInterval } from "usehooks-ts";
import { DEBUG_DOCKER_MODE, API_BASE_URL, AUTH_BASE_URL, TOKEN_FILE } from "../lib/constants";
import { motion } from 'framer-motion';

const ACCESS_FETCH_INTERVAL = 60000;
const PING_DOCKER_INTERVAL = 5000;

const animationKeyframes = keyframes`
  0% { opacity: 1 }
  25% { opacity: 0.5 }
  50% { opacity: 0 }
  75% { opacity: 0.5 }
  100% { opacity: 1 }
`;
const animation = `${animationKeyframes} 2s linear infinite`;

const CustomTab = ({ icon, children }) => {
  return (
    <Tab
      w='100%'
      rounded='md'
      _hover={{
        bg: 'gray.200',
      }}

      _selected={{
        bg: 'blue.500',
        color: 'white',
        fontWeight: 'semibold',
      }}
      gap={2}
      justifyContent='flex-start'
      alignItems={'center'}
    >
      <Icon as={icon} />
      <Text>
        {children}
      </Text>
    </Tab>
  );
};

const DockerHome = () => {
  const [pullLoading, setPullLoading] = useState(null);
  const [dockerUp, setDockerUp] = useState(false);
  const toast = useToast();

  useEffect(() => {
    pingDocker();

    window.ipc.on('docker-pull-progress', (progress) => {
      setPullLoading(progress);
    });

    window.ipc.on('docker-pull-finished', (image) => {
      console.log("Image pulled: ", image);
      setPullLoading(null);
    });

    window.ipc.on('docker-pinged', (data) => {
      if (data) {
        setDockerUp(true);
      } else {
        setDockerUp(false);
      }
    });

    window.ipc.on("notebook-started", (containerId, err) => {
      console.log("notebook started: ", containerId);
      if (err) {
        toast({
          title: "Error",
          description: err,
          status: "error",
          duration: 9000,
          isClosable: true,
        });
      } else {
        toast({
          title: "Success",
          description: "Notebook started successfully. It may take a few seconds for the program to show up.",
          status: "success",
          duration: 9000,
          isClosable: true,
        });
      }
    });


    return () => {
      window.ipc.removeAllListeners("notebook-started");
      window.ipc.removeAllListeners('docker-pull-progress');
      window.ipc.removeAllListeners('docker-pull-finished');
    };
  }, []);

  const pingDocker = () => {
    window.ipc.send("docker-ping");
  };

  async function getAccessTokenFromRefreshToken(refreshToken) {
    const respForRefresh = await fetch(`${AUTH_BASE_URL}/get-token-from-refresh-token?refresh_token=${refreshToken}`);

    if (!respForRefresh.ok) {
      throw new Error("Failed to fetch new access token (refresh token)");
    }

    const data = await respForRefresh.json();
    return data;
  };

  async function checkAccessToken(url, options) {
    let resp = await fetch(url, options);

    if (!resp.ok) {
      let refreshToken = localStorage.getItem('refreshToken');

      const data = await getAccessTokenFromRefreshToken(refreshToken);

      if (!data.access_token || !data.refresh_token) {
        throw new Error("Failed to fetch new access token (refresh token)");
      }

      window.ipc.send('write-file', TOKEN_FILE, JSON.stringify(data));

      localStorage.setItem('accessToken', data.access_token);
      localStorage.setItem('refreshToken', data.refresh_token);

      options.headers['Authorization'] = `Bearer ${data.access_token}`;

      resp = await fetch(url, options); // make sure the new one works
      if (!resp.ok) {
        throw new Error("Failed to fetch new experiments (new access token)");
      }
    };

    return resp;
  }

  function ensureAccessToken() {
    const accessToken = localStorage.getItem('accessToken');
    const url = `${API_BASE_URL}/`;
    const options = {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${accessToken}`
      }
    };

    try {
      checkAccessToken(url, options).catch(err => {
        console.log(err);
        // window.location.href = "/login";
      });
    } catch (err) {

    }
  }

  useInterval(() => {
    if (!DEBUG_DOCKER_MODE) {
      ensureAccessToken();
    }
  }, ACCESS_FETCH_INTERVAL);

  useInterval(() => {
    pingDocker();
  }, PING_DOCKER_INTERVAL);

  return (
    <Box h='100vh' overflow='hidden' bg='gray.100'>
      <HeaderBox />

      {/* {
        pullLoading && (
          <>
            {
              pullLoading?.progressDetail?.current ? (
                <>
                  <Progress value={pullLoading.progressDetail.current} max={pullLoading.progressDetail.total} />
                  <Text textAlign='center'>{pullLoading.status}</Text>
                </>
              ) : (
                <Progress isIndeterminate />
              )
            }
          </>
        )
      } */}

      <Tabs h='100%' isLazy>
        <Grid templateColumns='repeat(11, 1fr)' h='inherit'>
          <GridItem colSpan={2} bg='gray.100' h='inherit'>
            <Stack direction='column' spacing={2} p={4}>
              <CustomTab icon={LuContainer}>Containers</CustomTab>
              {/* <CustomTab icon={SiPaperswithcode}>Images</CustomTab> */}
              <CustomTab icon={AiOutlineCode}>Programs</CustomTab>
            </Stack>

            <Stack direction='row' align='center' p={4}
              position='fixed'
              bottom='0'
            >
              <Box
                w='10px'
                h='10px'
                bg={dockerUp ? 'green.500' : 'red.500'}
                rounded='full'
                as={motion.div}
                animation={animation}
              ></Box>
              <Text>{dockerUp ? "Docker is running" : "Docker is down"}</Text>
            </Stack>

          </GridItem>
          <GridItem colSpan={9} bg='white' roundedTopLeft='md'>
            <TabPanels>
              <TabPanel>
                <DockerContainersList />
              </TabPanel>
              <TabPanel>
                <AvailablePrograms
                  isDisabled={pullLoading !== null}
                  loadingText={pullLoading?.progressDetail?.current ? `${pullLoading?.status}` : "Pulling..."}
                  progress={pullLoading?.progressDetail?.current ? pullLoading?.progress : 'Unknown progress...'}
                />
              </TabPanel>
            </TabPanels>
          </GridItem>
        </Grid>
      </Tabs >
    </Box >
  );
};

export default DockerHome;