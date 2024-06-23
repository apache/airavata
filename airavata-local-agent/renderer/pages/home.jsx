import { Button, Container, Img, Text, Flex, Heading, Link, HStack, UnorderedList, ListItem } from "@chakra-ui/react";
import { HeaderBox } from "../components/HeaderBox";
import { logger } from "../lib/logger";
const Home = () => {
  const features = [
    'Connects local machines, lab servers, and workstations with remote HPC resources and cloud services.',
    'Automates the installation and execution of scientific software.',
    'Facilitates collaboration among researchers across different locations.',
    'Supports graphical interfaces, Jupyter Notebooks, and low-code Python annotations.',
    'Manages data transfer and synchronization between local and remote environments.',
  ];
  return (
    <>
      <HeaderBox />

      <Container maxW='container.sm' p={4} mt={4}>
        <Flex alignItems='center' gap={2}>
          <Img src='/images/a-logo.png' maxH='50px' />
          <Text color='blue.600' fontWeight='bold' fontSize='3xl'>Cybershuttle MD Local Agent</Text>
        </Flex>

        <Heading fontSize='2xl' mt={4} color='blue.500'>Overview</Heading>

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

        <Heading fontSize='2xl' mt={4} color='blue.500'>Quick Links</Heading>

        <HStack spacing={4} mt={2}>
          <Button as='a' href='/login' colorScheme='blue'>Login</Button>

          <Button as='a' target="_blank" href='https://github.com/apache/airavata/pull/435' bg='black' color='white' _hover={{
            'bg': "#404040"
          }}>Contribute on GitHub</Button>


        </HStack>


      </Container >
    </>
  );
};

export default Home;