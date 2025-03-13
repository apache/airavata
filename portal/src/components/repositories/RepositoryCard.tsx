import {
  Card,
  Heading,
  Box,
  Text,
  Link as ChakraLink,
  HStack,
} from "@chakra-ui/react";
export const RepositoryCard = ({ repository }) => {
  return (
    <Card.Root width="100%">
      <Card.Header>
        <HStack justifyContent="space-between" alignItems="flex-start">
          <Box>
            <Heading size="md">{repository.title}</Heading>
            <Text color="gray.500" mt={2}>
              {repository.date}
            </Text>
          </Box>

          <Text>
            <ChakraLink
              href={repository.githubUrl}
              target="_blank"
              bg="black"
              color="white"
              p={2}
              rounded="md"
              fontWeight="bold"
            >
              View on GitHub
            </ChakraLink>
          </Text>
        </HStack>
      </Card.Header>
      <Card.Body color="fg.muted">{repository.description}</Card.Body>
    </Card.Root>
  );
};
