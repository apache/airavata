import { Box, Center, Flex, FormControl, FormLabel, Input, Img, Text, VStack, Button, Alert, AlertIcon, Link, Heading, Spinner } from "@chakra-ui/react";
import { useContext, useEffect, useState } from "react";
import { HeaderBox } from "../components/HeaderBox";
import { AuthContext, useAuth } from "../lib/Contexts";
import { useRouter } from "next/router";
import { TOKEN_FILE } from "../lib/constants";

const Login = () => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [verifyURL, setVerifyURL] = useState("");
  const [isProd, setIsProd] = useState(false);
  const [authInfo, setAuthInfo] = useAuth();
  const router = useRouter();

  // general a random string
  const randomString = (length) => {
    const chars = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ';

    let result = '';
    for (let i = length; i > 0; --i) {
      result += chars[Math.floor(Math.random() * chars.length)];
    }

    return result;
  };

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
    setLoading(true);
    window.auth.ciLogonLogin();
  };

  useEffect(() => {

    window.ipc.send('is-prod');

    if (localStorage.getItem("ciLoginAuto") === "true") {
      setLoading(true);
      localStorage.removeItem("ciLoginAuto");
      window.auth.ciLogonLogin();
    }

    window.ipc.on('file-written', (data) => {
      console.log("File written", data);
      window.ipc.send('read-file', TOKEN_FILE);
    });

    window.ipc.on('file-read', (data) => {
      console.log("File read", data);
    });

    window.auth.ciLogonSuccess((event, data) => {
      const accessToken = data.access_token;
      const refreshToken = data.refresh_token;

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
          setError("Refresh the page and try logging in again.");
          setLoading(false);
          return;
        }

        localStorage.setItem("ciLoginAuto", "true");
        location.reload();
      } else {
        window.localStorage.setItem("accessToken", data.access_token);
        window.localStorage.setItem("refreshToken", data.refresh_token);

        localStorage.removeItem("ciLoginAuto");
        localStorage.setItem('numTries', 0);
        setLoading(false);
        window.ipc.send('write-file', TOKEN_FILE, JSON.stringify(data));
        router.push('/docker-home');
      }
    });

    window.ipc.on('is-prod-reply', (isProdResult) => {
      setIsProd(isProdResult);
    });

    return () => {
      window.ipc.removeAllListeners('file-written');
      window.ipc.removeAllListeners('file-read');
      window.ipc.removeAllListeners('is-prod-reply');
    };
  }, []);

  return (
    <>
      <HeaderBox />
      <Center mt={16} maxW='500px' mx='auto'>
        <Box>
          <Flex alignItems='center' gap={2}>
            <Img src='/images/a-logo.png' maxH='50px' />
            <Text color='blue.600' fontWeight='bold' fontSize='3xl'>Cybershuttle Local Agent</Text>
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

          {
            verifyURL !== "" && (
              <Alert status='info' rounded='md' mt={2}>
                <AlertIcon />
                <Text>
                  <Text as='span' color='blue.800' fontWeight='bold'>Please verify your login</Text>. Open the following URL in your browser if it has not automatically opened: <Link color='blue.500' href={verifyURL} target="_blank">{verifyURL}</Link>
                </Text>
              </Alert>
            )
          }

          <Box shadow='md' rounded='md' p={4} mt={4}>
            <Heading size='md' textAlign="left" color='blue.500'>

              Log in with your existing organizational login</Heading>

            {
              isProd ? (
                <Button colorScheme='blue' w='full' mt={4}
                  as='a'
                  href={`https://iam.scigap.org/auth/realms/molecular-dynamics/protocol/openid-connect/auth?response_type=code&client_id=pga&redirect_uri=csagent%3A%2F%2Flogin-callback&scope=openid&state=${randomString(15)}&kc_idp_hint=cilogon&idp_alias=cilogon`}
                  target="_blank"
                  isDisabled={loading}
                > {
                    loading && <Spinner mr={2} />
                  }Login with CILogon</Button>
              ) : (
                <Button colorScheme='blue' w='full' mt={4}
                  onClick={handleCiLogin}
                  isDisabled={loading}
                > {
                    loading && <Spinner mr={2} />
                  }Login with CILogon</Button>
              )
            }
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