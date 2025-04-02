import { Box, Center, SimpleGrid, Flex, Text, Button } from "@chakra-ui/react";
import { useAuth } from "react-oidc-context";

export const Login = () => {
  const auth = useAuth();
  return (
    <>
      <Center height="100vh">
        <SimpleGrid
          columns={{
            base: 1,
            lg: 2,
          }}
          gap={32}
          alignItems="center"
        >
          <Box>
            <Flex gap={4} alignItems="center">
              <Box>
                <Text fontWeight="bold">Airavata Data Catalog</Text>
                <Text color="gray.500" fontSize="sm">
                  Login to access the data catalog
                </Text>
              </Box>
            </Flex>

            <Button
              bg="black"
              color="white"
              _hover={{ bg: "gray.800" }}
              _active={{ bg: "gray.900" }}
              rounded="full"
              w="300px"
              onClick={() => {
                console.log("Sign in clicked");
                auth.signinRedirect();
              }}
            >
              Institution Login
            </Button>
          </Box>
        </SimpleGrid>
      </Center>
    </>
  );
};
