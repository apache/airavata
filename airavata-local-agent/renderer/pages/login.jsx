import { Box, Center, Flex, FormControl, FormLabel, Input, Img, Text, VStack, Button, Alert, AlertIcon, Link, Heading } from "@chakra-ui/react";
import { useEffect, useState } from "react";
import { HeaderBox } from "../components/HeaderBox";

const SIGN_UP_URL = "https://md.cybershuttle.org/auth/create-account";

const Login = () => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");

  const handleLogin = async () => {
    setError("Molecular Dynamics Gateway login is not yet implemented, please use the organizational login.");
    return;

    // TODO: implement this section
    if (username === "") {
      setError("Username cannot be blank.");
    } else if (password === "") {
      setError("Password cannot be blank.");
    } else {
      // handle login
      // setError("Invalid username or password.");
      window.location.href = "/tabs-view";
    }
  };

  const handleCiLogin = async () => {
    window.auth.ciLogonLogin();
  };

  useEffect(() => {
    if (localStorage.getItem("ciLoginAuto") === "true") {
      localStorage.removeItem("ciLoginAuto");
      window.auth.ciLogonLogin();
    }
    window.auth.ciLogonSuccess((event, accessToken, refreshToken) => {
      if (!accessToken || !refreshToken) {
        console.log("Error logging in with CI logon");

        const numTriesAlready = localStorage.getItem("numTries");
        if (numTriesAlready == null) {
          localStorage.setItem('numTries', 1);
        } else if (parseInt(numTriesAlready) < 4) {
          localStorage.setItem('numTries', parseInt(numTriesAlready) + 1);
        } else {
          localStorage.setItem('numTries', 0);
          localStorage.setItem('ciLoginAuto', "false");
          setError("Refresh the page and try logging in again, this may sometimes happen.");
          return;
        }

        localStorage.setItem("ciLoginAuto", "true");
        location.reload();
      } else {
        window.localStorage.setItem("accessToken", accessToken);
        window.localStorage.setItem("refreshToken", refreshToken);
        localStorage.removeItem("ciLoginAuto");
        localStorage.setItem('numTries', 0);
        window.location.href = "/tabs-view";
      }
    });
  }, []);

  return (
    <>
      <HeaderBox />
      <Center mt={16} maxW='500px' mx='auto'>
        <Box>
          <Flex alignItems='center' gap={2}>
            <Img src='/images/a-logo.png' maxH='50px' />
            <Text color='blue.600' fontWeight='bold' fontSize='3xl'>Airavata Local Agent</Text>
          </Flex>

          {
            error !== "" && (
              <Alert status='error' rounded='md' mt={2}>
                <AlertIcon />
                <Text>
                  <Text as='span' color='red.800' fontWeight='bold'>Login Failed</Text>. {error}
                </Text>
              </Alert>
            )
          }

          <Box shadow='md' rounded='md' p={4} mt={4}>
            <Heading size='md' textAlign="left" color='blue.500'>Log in with your existing organizational login</Heading>
            <Button colorScheme='blue' w='full' mt={4} onClick={handleCiLogin}>Login with Existing Institution Credentials</Button>
          </Box>

          {/* <Box shadow='md' rounded='md' p={4} mt={8}>
            <Heading size='md' textAlign="left" color='blue.500'>Log in with Molecular Dynamics Gateway</Heading>
            <Text mt={2}>If you need to create an account, <Link color='blue.500' href={SIGN_UP_URL} target="_blank">you can sign up here</Link>. You can close the pop-up window after you see "Account request processed successfully...".</Text>

            <VStack mt={4} w='500px' spacing={4}>
              <FormControl>
                <FormLabel>Username</FormLabel>
                <Input type='text' value={username} onChange={(e) => {
                  setUsername(e.target.value);
                }} placeholder='Username' />
              </FormControl>

              <FormControl>
                <FormLabel>Password</FormLabel>
                <Input type='password' value={password} onChange={(e) => {
                  setPassword(e.target.value);
                }} placeholder='Password' />
              </FormControl>

              <Button colorScheme='blue' onClick={handleLogin} w='full' isDisabled>Login with Molecular Dynamics Gateway</Button>
            </VStack>
          </Box> */}

          <Text mt={4} textAlign='center'><Link color='blue.500' href='/home'>Back to home</Link></Text>
        </Box>
      </Center >
    </>
  );
};

export default Login;