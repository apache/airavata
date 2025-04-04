import { Center, Text, Button, Image, VStack, Stack } from "@chakra-ui/react";
import { useAuth } from "react-oidc-context";
import AirvataLogo from "../../assets/airavata-logo.png";

export const Login = () => {
  const auth = useAuth();
  return (
    <Center height="100vh">
      <Stack
        gap={8}
        alignItems="center"
        padding={4}
        direction={{ base: "column", lg: "row" }}
      >
        <Image src={AirvataLogo} alt="Airavata Logo" maxWidth="100px" />
        <VStack gap={1} alignItems="flex-start">
          <Text fontWeight="bold" fontSize="2xl">
            Cybershuttle Data Catalog
          </Text>
          <Text color="gray.500" fontSize="sm" maxWidth="300px">
            Jupyter Notebooks, repositories, datasets, and models, for
            scientists, by scientists.
          </Text>

          <Button
            colorPalette="black"
            _active={{ bg: "gray.900" }}
            w="300px"
            onClick={() => {
              console.log("Sign in clicked");
              auth.signinRedirect();
            }}
          >
            Institution Login
          </Button>
        </VStack>
      </Stack>
    </Center>
  );
};
