import { SessionType } from "@/interfaces/SessionType";
import { API_VERSION, BACKEND_URL } from "@/lib/constants";
import {
  Box,
  Text,
  Card,
  Heading,
  HStack,
  Badge,
  Button,
} from "@chakra-ui/react";

const getColorPalette = (status: string) => {
  switch (status) {
    case "CREATED":
      return "green";
    case "stopped":
      return "red";
    case "pending":
      return "yellow";
    default:
      return "gray";
  }
};
export const SessionCard = ({ session }: { session: SessionType }) => {
  return (
    <Card.Root size="sm">
      <Card.Header>
        <HStack justify="space-between" alignItems="flex-start">
          <Box>
            <Heading size="lg">{session.sessionName}</Heading>
          </Box>
          <Badge size="md" colorPalette={getColorPalette(session.status)}>
            {session.status}
          </Badge>
        </HStack>
      </Card.Header>
      <Card.Body>
        <Text color="fg.muted">
          <Text as="span" fontWeight="bold">
            Created
          </Text>
          : {new Date(session.createdAt).toLocaleString()}
        </Text>

        <HStack alignItems="center" mt={2}>
          <Button
            size="sm"
            colorPalette="red"
            variant="subtle"
            onClick={() => {}}
          >
            Terminate
          </Button>
          <Button
            size="sm"
            colorPalette="green"
            onClick={() => {
              window.open(
                `${BACKEND_URL}/api/${API_VERSION}/rf/hub/sessions/${session.id}/resolve`,
                "_blank"
              );
            }}
          >
            Open Session
          </Button>
        </HStack>
      </Card.Body>
    </Card.Root>
  );
};
