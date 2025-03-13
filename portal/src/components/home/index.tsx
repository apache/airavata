import {
  Box,
  Input,
  Heading,
  Text,
  VStack,
  HStack,
  Icon,
  Container,
} from "@chakra-ui/react";

import NavBar from "../NavBar";
import { InputGroup } from "../ui/input-group";
import { LuSearch } from "react-icons/lu";
import { FaMap, FaProjectDiagram, FaSearch } from "react-icons/fa";

const Home = () => {
  return (
    <Box>
      <NavBar />

      <Container maxW="container.xl">
        <Box textAlign="center" p={10} minH="100vh">
          <Heading as="h1" size="5xl" fontWeight="black" color="#f58b7d">
            Apache{" "}
            <Text as="span" color="#2bb8df">
              Airavata
            </Text>{" "}
          </Heading>
          <Text mt={2} color="gray.600">
            Empowering Science with Distributed Computing and Data Management
          </Text>

          <InputGroup
            mt={12}
            mx="auto"
            borderRadius="md"
            w="70%"
            bg="white"
            boxShadow="md"
            endElement={<LuSearch />}
          >
            <Input
              type="text"
              placeholder="Start your exploration..."
              borderRadius="md"
            />
          </InputGroup>

          <HStack mt={10} gap={24} justify="center">
            <VStack>
              <Icon as={FaMap} w={10} h={10} color="blue.400" />
              <Text fontWeight="bold">Map</Text>
              <Text fontSize="sm" color="gray.500">
                Transform data into insights
              </Text>
            </VStack>

            <VStack>
              <Icon as={FaProjectDiagram} w={10} h={10} color="green.400" />
              <Text fontWeight="bold">Connect</Text>
              <Text fontSize="sm" color="gray.500">
                Understand linked knowledge
              </Text>
            </VStack>

            <VStack>
              <Icon as={FaSearch} w={10} h={10} color="purple.400" />
              <Text fontWeight="bold">Discover</Text>
              <Text fontSize="sm" color="gray.500">
                Explore new knowledge intelligently
              </Text>
            </VStack>
          </HStack>
        </Box>
      </Container>
    </Box>
  );
};

export default Home;
