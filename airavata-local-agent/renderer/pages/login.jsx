import { Box, Center, Flex, FormControl, FormLabel, Input, Img, Text, VStack, Button, Alert, AlertIcon, Link, Heading } from "@chakra-ui/react";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";

const SIGN_UP_URL = "https://md.cybershuttle.org/auth/create-account";

const Login = () => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const router = useRouter();

  const handleLogin = async () => {
    if (username === "") {
      setError("Username cannot be blank.");
    } else if (password === "") {
      setError("Password cannot be blank.");
    } else {
      // handle login
      // setError("Invalid username or password.");
      router.push('/tabs-view');
    }
  };

  const handleCiLogin = async () => {
    window.auth.ciLogonLogin();
  };

  useEffect(() => {
    window.auth.ciLogonSuccess((event, accessToken, refreshToken) => {
      if (!accessToken || !refreshToken) {
        console.log("Error logging in with CI logon");
        window.auth.ciLogonLogin();
        setError("Refresh the page and try again, this may sometimes happen.");
      } else {
        window.localStorage.setItem("accessToken", accessToken);
        window.localStorage.setItem("refreshToken", refreshToken);
        router.push('/tabs-view');
      }
    });
  });

  return (
    <Center mt={16} maxW='400px' mx='auto'>
      <Box>
        <Flex alignItems='center' gap={2}>
          <Img src='/images/a-logo.png' maxH='50px' />
          <Text color='blue.600' fontWeight='bold' fontSize='3xl'>Airavata Local Agent</Text>
        </Flex>


        <Box shadow='lg' rounded='md' p={4} mt={8}>
          <Box>
            <Heading size='md' textAlign="left" color='blue.500'>Log in with Molecular Dynamics Gateway</Heading>
            {/* <Link>(create an account)</Link> */}

            <Text mt={2}>If you need to create an account, <Link color='blue.500' href={SIGN_UP_URL} target="_blank">you can sign up here</Link>. You can close the pop-up window after you see "Account request processed successfully...".</Text>
          </Box>

          <VStack mt={4} w='500px' spacing={4}>
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

            <Button colorScheme='blue' onClick={handleLogin} w='full'>Login with Molecular Dynamics Gateway</Button>
          </VStack>
        </Box>


        <Box shadow='lg' rounded='md' p={4} mt={8}>
          <Box>
            <Heading size='md' textAlign="left" color='blue.500'>Log in with your existing organizational login</Heading>

            <Button colorScheme='blue' w='full' mt={2} onClick={handleCiLogin}>Login with Existing Institution Credentials</Button>
          </Box>
        </Box>


        {/* <Flex alignItems='center' w='full'>
          <Box>
            <Text><Link color='blue.500' href='/home'>Back to home</Link></Text>
          </Box>
        </Flex> */}
      </Box>
    </Center>
  );
};

export default Login;