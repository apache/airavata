import {
  Button, Tooltip, Box, HStack, Table,
  Thead,
  Tbody,
  Text,
  Tr,
  Th,
  Td,
  useDisclosure,
  TableContainer,
  useToast, Modal,
  ModalOverlay,
  ModalContent,
  ModalHeader,
  ModalFooter,
  ModalBody,
  ModalCloseButton,
  Input,
  FormControl,
  FormLabel,
  Stack,
  IconButton,
  FormHelperText,
  Heading,
  SimpleGrid,
  Flex,
  Img,
} from "@chakra-ui/react";
import { useEffect, useState } from "react";
import { JupyterProgram } from "./Programs/JupyterProgram";

const DEFAULT_CONFIG = {
  port: "6080",
  name: "",
  mountLocation: ""
};

export const AvailablePrograms = ({ isDisabled }) => {
  return (
    <>
      <SimpleGrid columns={3} spacing={10} p={4}
        pointerEvents={isDisabled ? "none" : "auto"}
        opacity={isDisabled ? 0.4 : 1}
      >
        <JupyterProgram />
      </SimpleGrid>
    </>
  );
};