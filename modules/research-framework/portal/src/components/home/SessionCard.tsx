import { SessionType } from "@/interfaces/SessionType";
import {
  Box,
  Text,
  Card,
  Heading,
  HStack,
  Badge,
  VStack,
  IconButton,
  Dialog,
  useDialog,
  CloseButton,
  Portal,
  Button,
  Input,
} from "@chakra-ui/react";
import { SessionCardControls } from "./SessionCardControls";
import { SessionStatusEnum } from "@/interfaces/SessionStatusEnum";
import { FaTrash } from "react-icons/fa";
import { toaster } from "../ui/toaster";
import { CONTROLLER } from "@/lib/controller";
import { useState } from "react";
import api from "@/lib/api";

const getColorPalette = (status: string) => {
  switch (status) {
    case SessionStatusEnum.CREATED:
      return "green";
    case SessionStatusEnum.RUNNING:
      return "blue";
    case SessionStatusEnum.FINISHED:
      return "purple";
    case SessionStatusEnum.TERMINATED:
      return "gray";
    case SessionStatusEnum.ERROR:
      return "red";
    default:
      return "gray";
  }
};

export const SessionCard = ({ session }: { session: SessionType }) => {
  const dialog = useDialog();
  const [hideCard, setHideCard] = useState(false);
  const [deleteName, setDeleteName] = useState("");
  const [deleteLoading, setDeleteLoading] = useState(false);

  const handleDeleteSession = async () => {
    setDeleteLoading(true);
    try {
      await api.delete(`${CONTROLLER.sessions}/${session.id}`);
      toaster.create({
        title: "Session deleted",
        description: session.sessionName,
        type: "success",
      });

      setHideCard(true);
      dialog.setOpen(false);
    } catch {
      toaster.create({
        title: "Error deleting session",
        type: "error",
      });
    }
    setDeleteLoading(false);
  };

  return (
    <>
      <Dialog.RootProvider size="sm" value={dialog}>
        <Portal>
          <Dialog.Backdrop />
          <Dialog.Positioner>
            <Dialog.Content>
              <Dialog.Header>
                <Dialog.Title>Delete Session</Dialog.Title>
              </Dialog.Header>
              <Dialog.Body>
                <Text color="gray.500">
                  This action is irreversible. To confirm, please type:{" "}
                  <b>{session.sessionName}</b>.
                </Text>

                <Input
                  mt={2}
                  placeholder="Session name"
                  value={deleteName}
                  onChange={(e) => setDeleteName(e.target.value)}
                />
              </Dialog.Body>
              <Dialog.Footer>
                <Button
                  width="100%"
                  colorPalette="red"
                  disabled={deleteName !== session.sessionName || deleteLoading}
                  loading={deleteLoading}
                  onClick={handleDeleteSession}
                >
                  Save
                </Button>
              </Dialog.Footer>
              <Dialog.CloseTrigger asChild>
                <CloseButton size="sm" />
              </Dialog.CloseTrigger>
            </Dialog.Content>
          </Dialog.Positioner>
        </Portal>
      </Dialog.RootProvider>

      <Card.Root size="sm" height="fit-content" hidden={hideCard}>
        <Card.Header>
          <HStack justify="space-between" alignItems="flex-start">
            <Box>
              <Heading size="lg">{session.sessionName}</Heading>
            </Box>
            <VStack alignItems="flex-end">
              <Badge size="md" colorPalette={getColorPalette(session.status)}>
                {session.status}
              </Badge>

              <IconButton
                color="red.600"
                size="xs"
                variant={"ghost"}
                onClick={() => dialog.setOpen(true)}
              >
                <FaTrash />
              </IconButton>
            </VStack>
          </HStack>
        </Card.Header>
        <Card.Body>
          <Text color="fg.muted">
            <Text as="span" fontWeight="bold">
              Created
            </Text>
            : {new Date(session.createdAt).toLocaleString()}
          </Text>

          <SessionCardControls session={session} />
        </Card.Body>
      </Card.Root>
    </>
  );
};
