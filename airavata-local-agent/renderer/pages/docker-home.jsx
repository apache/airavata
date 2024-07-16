import { Grid, GridItem, Tabs, Box, TabList, TabPanels, Tab, TabPanel, Stack, Heading } from "@chakra-ui/react";
import { HeaderBox } from "../components/HeaderBox";
import { DockerImagesList } from "../components/DockerComponents/DockerImagesList";

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



  return (
    <>
      <HeaderBox />
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
                <p>one!</p>
              </TabPanel>
              <TabPanel>
                <DockerImagesList />
              </TabPanel>
              <TabPanel>
                <p>three!</p>
              </TabPanel>
            </TabPanels>
          </GridItem>
        </Grid>

      </Tabs>
    </>
  );
};

export default DockerHome;