import { SessionType } from "@/interfaces/SessionType";
import { Box, Text, Card, Heading, HStack, Badge } from "@chakra-ui/react";
import { SessionCardControls } from "./SessionCardControls";
import { SessionStatusEnum } from "@/interfaces/SessionStatusEnum";

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

const PREVENT_CONTROL_STATUS = [
  SessionStatusEnum.TERMINATED,
  SessionStatusEnum.ERROR,
];
export const SessionCard = ({ session }: { session: SessionType }) => {
  return (
    <Card.Root size="sm" height="fit-content">
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

        {!PREVENT_CONTROL_STATUS.includes(session.status) && (
          <SessionCardControls session={session} />
        )}
      </Card.Body>
    </Card.Root>
  );
};
