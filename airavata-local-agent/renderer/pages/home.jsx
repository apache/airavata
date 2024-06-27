import { Button, Container, Img, Text, Flex, Heading, Link, HStack, UnorderedList, ListItem, Alert, AlertIcon } from "@chakra-ui/react";
import { HeaderBox } from "../components/HeaderBox";
import { useEffect, useState } from "react";
import { isNewerVersion } from "../lib/utilityFuncs";
const Home = () => {
  const features = [
    'Connects local machines, lab servers, and workstations with remote HPC resources and cloud services.',
    'Automates the installation and execution of scientific software.',
    'Facilitates collaboration among researchers across different locations.',
    'Supports graphical interfaces, Jupyter Notebooks, and low-code Python annotations.',
    'Manages data transfer and synchronization between local and remote environments.',
  ];

  const [version, setVersion] = useState('');
  const [serverVersion, setServerVersion] = useState('');
  const [showUpdate, setShowUpdate] = useState(false);

  useEffect(() => {
    window.config.getVersionNumber();

    window.config.versionNumber(async (event, version) => {
      // make fetch here to check for updates
      const resp = await fetch("http://20.51.202.251:9001/local-agent/version");
      let respVersion = await resp.text();

      // respVersion starts with v, so we remove it
      if (respVersion.startsWith('v')) {
        respVersion = respVersion.substring(1);
      }


      setShowUpdate(isNewerVersion(respVersion, version));
      setVersion(version);
      setServerVersion(respVersion);
    });
  }, []);
  return (
    <>
      <HeaderBox />

      <Container maxW='container.sm' p={4} mt={4}>
        <Flex alignItems='center' gap={2}>
          <Img src='/images/a-logo.png' maxH='50px' />
          <Text color='blue.600' fontWeight='bold' fontSize='3xl'>Cybershuttle MD Local Agent</Text>
        </Flex>

        {
          showUpdate && (
            <Alert status='info' rounded='md' mt={4}>
              <AlertIcon />
              <Text>
                A new version is available. You're currently using version {version}. Please update to the latest version {serverVersion} by downloading the latest release from <Link href='' target="_blank" color='blue.500' fontWeight='bold'>here</Link>.
              </Text>
            </Alert>
          )
        }

        <Text mt={2}>Cybershuttle MD Local Agent, developed by the Cybershuttle project, empowers researchers by providing seamless access to a comprehensive range of computational resources. The agent bridges the gap between local, institutional, and national-scale computing resources, enhancing productivity and collaboration in scientific research. By integrating diverse computing environments into a unified interface, Apache Airavata eliminates traditional barriers, enabling researchers to focus on innovation and discovery.
        </Text>

        <Heading fontSize='2xl' mt={4} color='blue.500'>Features</Heading>
        <UnorderedList mt={2}>
          {
            features.map((feature, index) => {
              return <ListItem key={index}>{feature}</ListItem>;
            })
          }
        </UnorderedList>

        <HStack spacing={4} mt={2}>
          <Button as='a' href='/login' colorScheme='blue'>Login</Button>
          {/* <Button as='a' target="_blank" href='https://github.com/apache/airavata/pull/435' bg='black' color='white' _hover={{
            'bg': "#404040"
          }}>Contribute on GitHub</Button> */}
        </HStack>
      </Container>
    </>
  );
};

export default Home;