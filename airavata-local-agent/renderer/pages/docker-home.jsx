import { Grid, GridItem, Tabs, useToast, Box, Progress, Text, TabList, TabPanels, Tab, TabPanel, Stack, Heading } from "@chakra-ui/react";
import { HeaderBox } from "../components/HeaderBox";
import { DockerImagesList } from "../components/DockerComponents/DockerImagesList";
import { DockerContainersList } from "../components/DockerComponents/DockerContainersList";
import { useEffect, useState } from "react";
import { AvailablePrograms } from "../components/DockerComponents/AvaliablePrograms";

const CustomTab = ({ icon, children }) => {
  return (
    <Tab
      w='100%'
      rounded='md'
      _hover={{
        bg: 'gray.200',
      }}

      _selected={{
        bg: 'gray.200',
        color: 'blue.500',
      }}

    >
      {icon}
      {children}
    </Tab>
  );
};

const DockerHome = () => {
  const [pullLoading, setPullLoading] = useState(false);
  const toast = useToast();

  useEffect(() => {
    window.ipc.on('docker-pull-progress', (progress) => {
      setPullLoading(progress);
    });

    window.ipc.on('docker-pull-finished', (image) => {
      console.log("Image pulled: ", image);
      setPullLoading(null);
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
          description: "Notebook started successfully",
          status: "success",
          duration: 9000,
          isClosable: true,
        });
      }
    });


    return () => {
      window.ipc.removeAllListeners("container-started");
      window.ipc.removeAllListeners('docker-pull-progress');
      window.ipc.removeAllListeners('docker-pull-finished');
    };
  }, []);

  return (
    <>
      <HeaderBox />

      {
        pullLoading && (
          <>
            {
              pullLoading?.progressDetail?.current ? (
                <>
                  <Progress value={pullLoading.progressDetail.current} max={pullLoading.progressDetail.total} />
                  <Text>{pullLoading.status}</Text>

                </>
              ) : (
                <Progress isIndeterminate />
              )
            }
          </>
        )
      }

      <Tabs isLazy>
        <Grid templateColumns='repeat(11, 1fr)'>
          <GridItem colSpan={2} bg='gray.100' h='100vh' p={4}>
            <Box textAlign='center'>
              <Heading size='md'>Home</Heading>
            </Box>

            <Stack direction='column' spacing={4} mt={4}>
              <CustomTab>Containers</CustomTab>
              <CustomTab>Images</CustomTab>
              <CustomTab>Programs</CustomTab>

            </Stack>
          </GridItem>
          <GridItem colSpan={9} bg='white'>

            <TabPanels>
              <TabPanel>
                <DockerContainersList />
              </TabPanel>
              <TabPanel>
                <DockerImagesList />
              </TabPanel>
              <TabPanel>
                <AvailablePrograms />
              </TabPanel>
            </TabPanels>
          </GridItem>
        </Grid>

      </Tabs>
    </>
  );
};

export default DockerHome;