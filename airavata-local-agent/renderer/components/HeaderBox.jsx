import { Box, Flex, Spacer, Text, Modal, ModalOverlay, ModalCloseButton, ModalHeader, ModalBody, ModalContent, useDisclosure, Tooltip, Img } from "@chakra-ui/react";
import { UserModal } from './UserModal';
import { useEffect, useState } from "react";

export const HeaderBox = ({ name, email }) => {
  const { isOpen, onOpen, onClose } = useDisclosure();
  const [version, setVersion] = useState('');
  const [userObj, setUserObj] = useState({
    name: null,
    email: null
  });

  useEffect(() => {
    window.config.getVersionNumber();

    window.config.versionNumber((event, version) => {
      setVersion(version);
    });

    if (!name) {
      const accessToken = localStorage.getItem("accessToken");

      try {
        if (accessToken) {
          const obj = JSON.parse(atob(accessToken.split('.')[1]));

          setUserObj({
            name: obj.name,
            email: obj.email
          });
        }
      } catch (e) {
        console.log("Error parsing access token", e);
      }
    }
  }, []);

  email = email || userObj.email;
  name = name || userObj.name;

  return (
    <Box py={1} px={2} bg='gray.100'>

      <Modal isOpen={isOpen} onClose={onClose} size='xl'>
        <ModalOverlay />
        <ModalContent>
          <ModalHeader>User Information</ModalHeader>
          <ModalCloseButton />
          <ModalBody pb={8}>
            {
              isOpen && (
                <UserModal email={email} accessToken={localStorage.getItem("accessToken")} />
              )
            }
          </ModalBody>
        </ModalContent>
      </Modal>

      <Flex>
        <Flex align='center' gap={1}>
          <Img src='/images/a-logo.png' maxH='20px' />
          <Text>Cybershuttle Local Agent v{version}</Text>
        </Flex>

        <Spacer />

        {
          (name) && (email) && (
            <Text>
              <Tooltip label="View user information">
                <Text as='span'
                  _hover={{
                    textDecoration: "underline",
                    cursor: "pointer"
                  }}
                  onClick={() => {
                    onOpen();
                  }}
                >
                  {name} ({email}),
                </Text>
              </Tooltip>
              {" "}
              <Text color='blue.400' _hover={{ textDecoration: "underline", cursor: "pointer" }} as='span' onClick={() => {
                // delete the access token and refresh token
                // console.log("Running this code...");
                localStorage.removeItem('accessToken');
                localStorage.removeItem('refreshToken');

                window.auth.ciLogonLogout();
                // redirect to login page
                window.location.href = '/login';
              }}>Log Out</Text>
            </Text>
          )
        }
      </Flex>
    </Box>

  );
};