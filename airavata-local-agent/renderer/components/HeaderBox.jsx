import { Box, Flex, Spacer, Text } from "@chakra-ui/react";

export const HeaderBox = ({ name, email }) => {
  return (
    <Box py={1} px={2} bg='gray.100'>

      <Flex>
        <Text>Airavata Local Agent v1.0.0</Text>

        <Spacer />

        {
          !name || !email ? (
            <Text color='blue.400' _hover={{ textDecoration: "underline", cursor: "pointer" }} as='span' onClick={() => {
              window.location.href = '/login';
            }}>Log In</Text>
          ) : (
            <Text>{name} ({email}), <Text color='blue.400' _hover={{ textDecoration: "underline", cursor: "pointer" }} as='span' onClick={() => {
              // delete the access token and refresh token
              // console.log("Running this code...");
              localStorage.removeItem('accessToken');
              localStorage.removeItem('refreshToken');
              // redirect to login page
              window.location.href = '/login';
            }}>Log Out</Text></Text>
          )
        }
      </Flex>
    </Box>

  );
};