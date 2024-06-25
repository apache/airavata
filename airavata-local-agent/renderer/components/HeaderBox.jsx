import { Box, Flex, Spacer, Text, Modal, ModalOverlay, ModalCloseButton, ModalHeader, ModalBody, ModalContent, useDisclosure, Tooltip } from "@chakra-ui/react";
import { UserModal } from './UserModal';
import { useEffect, useState } from "react";

export const HeaderBox = ({ name, email }) => {
  const { isOpen, onOpen, onClose } = useDisclosure();
  const [version, setVersion] = useState('');

  useEffect(() => {
    window.config.getVersionNumber();

    window.config.versionNumber((event, version) => {
      setVersion(version);
    });
  }, []);

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
        <Text>Cybershuttle MD Local Agent {version}</Text>

        <Spacer />

        {
          name && email && (
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