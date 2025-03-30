import { SessionType } from "@/interfaces/SessionType";
import {
  Box,
  Text,
  Card,
  Heading,
  HStack,
  SimpleGrid,
  Badge,
} from "@chakra-ui/react";

const getColorPalette = (status: string) => {
  switch (status) {
    case "running":
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
    <Card.Root
      size="sm"
      bg={
        session.title.toLocaleLowerCase() === "jupyter"
          ? "green.50"
          : "purple.50"
      }
    >
      <Card.Header>
        <HStack justify="space-between" alignItems="flex-start">
          <Box>
            <Heading size="lg">{session.title}</Heading>
            <Text color="fg.muted">{session.started}</Text>
          </Box>
          <Badge size="md" colorPalette={getColorPalette(session.status)}>
            {session.status}
          </Badge>
        </HStack>
      </Card.Header>
      <Card.Body>
        <SimpleGrid columns={2}>
          <KeyValue label="Models" value={session.models.join(", ")} />
          <KeyValue label="Datasets" value={session.datasets.join(", ")} />
          <KeyValue label="Nodes" value={session.nodes.toString()} />
          <KeyValue label="RAM" value={session.ram.toString()} />
          <KeyValue label="Storage" value={session.storage.toString()} />
        </SimpleGrid>
      </Card.Body>
    </Card.Root>
  );
};

export const KeyValue = ({
  label,
  value,
}: {
  label: string;
  value: string;
}) => {
  return (
    <HStack>
      <Text fontWeight="bold">{label}:</Text>
      <Text>{value}</Text>
    </HStack>
  );
};
