import { Center, Text, Button, Image, VStack, Stack } from "@chakra-ui/react";
import { useAuth } from "react-oidc-context";
import AirvataLogo from "../../assets/airavata-logo.png";
import { useEffect, useState } from "react";

export const Login = () => {
  const [loading, setLoading] = useState(false);
  const auth = useAuth();

  // Automatically login the user if they are authenticated
  useEffect(() => {
    if (auth.isAuthenticated && !auth.isLoading) {
      auth.signinRedirect();
    }
  }, [auth]);

  const redirect =
    new URLSearchParams(window.location.search).get("redirect") || "/";

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
              console.log("Login button clicked");
              setLoading(true);
              auth.signinRedirect({
                redirect_uri: `${window.location.origin}${redirect}`,
                extraQueryParams: {
                  prompt: "login",
                  kc_idp_hint: "oidc",
                },
              });
              // no need to set loading to false here, as the redirect will happen
            }}
            loading={loading}
          >
            Institution Login
          </Button>
        </VStack>
      </Stack>
    </Center>
  );
};
