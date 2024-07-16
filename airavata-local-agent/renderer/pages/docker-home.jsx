import { Grid, GridItem, Tabs, useToast, Box, Progress, Text, TabList, TabPanels, Tab, TabPanel, Stack, Heading, IconButton, Icon } from "@chakra-ui/react";
import { HeaderBox } from "../components/HeaderBox";
import { DockerImagesList } from "../components/DockerComponents/DockerImagesList";
import { DockerContainersList } from "../components/DockerComponents/DockerContainersList";
import { useEffect, useState } from "react";
import { AvailablePrograms } from "../components/DockerComponents/AvaliablePrograms";
import { LuContainer } from "react-icons/lu";
import { SiPaperswithcode } from "react-icons/si";
import { AiOutlineCode } from "react-icons/ai";

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
      gap={2}
      textAlign='left'
    >
      <Icon as={icon} />
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
    <Box h='100vh' overflow='hidden'>
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

      <Tabs h='100%' isLazy>
        <Grid templateColumns='repeat(11, 1fr)' h='inherit'>
          <GridItem colSpan={2} bg='gray.100' h='inherit'>
            <Stack direction='column' spacing={4}>
              <CustomTab icon={LuContainer}>Containers</CustomTab>
              <CustomTab icon={SiPaperswithcode}>Images</CustomTab>
              <CustomTab icon={AiOutlineCode}>Programs</CustomTab>

            </Stack>
          </GridItem>
          <GridItem colSpan={9} bg='white' borderRadius={'md'}>
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

      </Tabs >
    </Box>
  );
};

export default DockerHome;