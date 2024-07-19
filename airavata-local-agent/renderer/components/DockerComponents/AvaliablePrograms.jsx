import {
  Button,
  Box,
  Text,
  Modal,
  ModalOverlay,
  ModalContent,
  ModalBody,
  SimpleGrid,
  Flex,
} from "@chakra-ui/react";
import { JupyterProgram } from "./Programs/JupyterProgram";
import ReactLoading from "react-loading";
import { useState } from "react";

export const AvailablePrograms = ({ isDisabled, loadingText, progress }) => {
  const [showProgress, setShowProgress] = useState(false);


  return (
    <Box p={4}>

      {/* the modal should show only when isDisabled is true */}

      <Modal isOpen={isDisabled}>
        <ModalOverlay />
        <ModalContent>
          {/* <ModalHeader>Loading...</ModalHeader> */}
          <ModalBody>
            <Flex justifyContent='center'>
              <ReactLoading type='bubbles' color="black" />
            </Flex>
            <Text textAlign='center'>Opening Jupyter Notebook</Text>

            <Box mx='auto' textAlign='center' mt={4}>
              <Button
                size='xs'
                onClick={() => {
                  setShowProgress(!showProgress);
                }}>
                {
                  showProgress ? "Hide Progress" : "Show Progress"
                }
              </Button>

              {
                showProgress && (
                  <>
                    <Text>{loadingText}</Text>
                    <Text>{progress}</Text>
                  </>
                )
              }
            </Box>

          </ModalBody>
        </ModalContent>
      </Modal>

      <Text>
        Select the program you want to run.
      </Text>

      <SimpleGrid columns={3} spacing={10}
        mt={4}
        pointerEvents={isDisabled ? "none" : "auto"}
        opacity={isDisabled ? 0.4 : 1}
      >
        <JupyterProgram />
      </SimpleGrid>
    </Box>
  );
};