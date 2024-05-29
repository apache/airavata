import { Box, Center, Flex, FormControl, FormLabel, Input, Img, Text, VStack, Button, Alert, AlertIcon, Link, Spacer } from "@chakra-ui/react";
import { useRouter } from "next/navigation";
import { useState } from "react";

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

  return (
    <Center mt={16}>
      <Box>
        <Flex alignItems='center' gap={2}>
          <Img src='/images/a-logo.png' maxH='50px' />
          <Text color='blue.600' fontWeight='bold' fontSize='3xl'>Airavata UI</Text>
        </Flex>


        <VStack mt={8} w='500px' spacing={4}>
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
            <FormLabel>Airavata Username</FormLabel>
            <Input type='text' value={username} onChange={(e) => {
              setUsername(e.target.value);
            }} placeholder='Username' />
          </FormControl>

          <FormControl>
            <FormLabel>Airavata Password</FormLabel>
            <Input type='password' value={password} onChange={(e) => {
              setPassword(e.target.value);
            }} placeholder='Password' />
          </FormControl>

          <Button colorScheme='blue' onClick={handleLogin} w='full'>Login to Airavata UI</Button>

          <Flex alignItems='center' w='full'>
            <Box>
              <Text><Link color='blue.500' href='/home'>Back to home</Link></Text>
            </Box>

            {/* <Spacer />

                        <Box>
                            <Text><Link>Back to home</Link></Text>
                        </Box> */}
          </Flex>

        </VStack>
      </Box>
    </Center>
  );
};

export default Login;