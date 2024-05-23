import { Button, Container, Img, Text, Flex, Heading, Link, HStack } from "@chakra-ui/react";

const Home = () =>
{
  return (
    <>
      <Container maxW='container.sm' p={4} mt={4}>
        <Flex alignItems='center' gap={2}>
          <Img src='/images/a-logo.png' maxH='50px' />
          <Text color='blue.600' fontWeight='bold' fontSize='3xl'>Airavata UI</Text>
        </Flex>

        <Heading fontSize='2xl' mt={4} color='blue.500'>Overview</Heading>

        <Text mt={2}>Airavata User Interface (UI) was developed to aid scientists in lorem ipsum.  developed to aid scientists in lorem ipsum  developed to aid scientists in lorem ipsum  developed to aid scientists in lorem ipsum developed to aid scientists in lorem ipsum developed to aid scientists in lorem ipsum developed to aid scientists in lorem ipsum developed to aid scientists in lorem ipsum. To continue and access the dashboard, please <Link href='/login' color='blue.500'>login to Airavata UI</Link>.
        </Text>

        <Text mt={2}><em>Airavata UI was developed by Ganning Xu, with mentors from the Apache Software Foundation, as part of Google Summer of Code 2024.</em></Text>

        <Heading fontSize='2xl' mt={4} color='blue.500'>Quick Links</Heading>

        <HStack spacing={4} mt={2}>
          <Button as='a' href='/login' colorScheme='blue'>Login to Airavata UI</Button>

          <Button as='a' href='/login' bg='black' color='white' _hover={{
            'bg': "#404040"
          }}>Contribute on GitHub</Button>


        </HStack>


      </Container >
    </>
  );
};

export default Home;