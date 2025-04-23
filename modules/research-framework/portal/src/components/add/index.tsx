import { Button, Container, HStack, Heading, Text } from "@chakra-ui/react";
import { Link } from "react-router";

export const Add = () => {
  return (
    <Container maxW="container.lg" mt={8}>
      <Heading
        textAlign="center"
        fontSize={{ base: "4xl", md: "5xl" }}
        fontWeight="black"
        lineHeight={1.2}
      >
        Add
      </Heading>

      <Text fontSize="lg" mt={2} textAlign="center">
        Do you want to add a <b>repository</b> or a <b>project</b>?
      </Text>

      <HStack justify="center">
        <Link to="/add/repo">
          <Button mt={4} colorScheme="blue" size="lg" variant="solid">
            Repository
          </Button>
        </Link>

        <Link to="/add/project">
          <Button mt={4} colorScheme="blue" size="lg" variant="solid">
            Project
          </Button>
        </Link>
      </HStack>
    </Container>
  );
};
