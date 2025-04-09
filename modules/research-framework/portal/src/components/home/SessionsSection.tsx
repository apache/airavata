import { SessionType } from "@/interfaces/SessionType";
import { SessionCard } from "./SessionCard";
import api from "@/lib/api";
import { useEffect, useState } from "react";
import { CONTROLLER } from "@/lib/controller";
import { Button, HStack, SimpleGrid, Text } from "@chakra-ui/react";
import { SessionStatusEnum } from "@/interfaces/SessionStatusEnum";

async function getSessions(status: SessionStatusEnum | null = null) {
  try {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const params = {} as any;
    if (status) {
      params.status = status;
    }
    const response = await api.get(`${CONTROLLER.sessions}/`, {
      params,
    });
    const data = response.data;
    return data;
  } catch (error) {
    console.error("Error fetching:", error);
  }
}

export const SessionsSection = () => {
  const [sessionStatusFilter, setSessionStatusFilter] =
    useState<SessionStatusEnum | null>(null);
  const [sessions, setSessions] = useState<SessionType[]>([]);

  useEffect(() => {
    let intervalId: NodeJS.Timeout;

    async function fetchData() {
      const allSessions = await getSessions(sessionStatusFilter);

      setSessions(allSessions);
    }

    fetchData(); // Initial fetch
    // eslint-disable-next-line prefer-const
    intervalId = setInterval(fetchData, 2000); // Refetch every 2s

    return () => clearInterval(intervalId); // Cleanup
  }, [sessionStatusFilter]); // <== Important dependency

  const sessionStatusFilterOptions = [
    SessionStatusEnum.CREATED,
    SessionStatusEnum.TERMINATED,
  ];

  return (
    <>
      <HStack flexWrap={"wrap"} gap={2} mt={4}>
        <Button
          variant={sessionStatusFilter === null ? "solid" : "outline"}
          size="sm"
          onClick={() => {
            setSessionStatusFilter(null);
          }}
        >
          All
        </Button>
        {/* {Object.values(SessionStatusEnum).map((status) => ( */}
        {sessionStatusFilterOptions.map((status) => (
          <Button
            key={status}
            variant={sessionStatusFilter === status ? "solid" : "outline"}
            size="sm"
            onClick={() => {
              setSessionStatusFilter(status);
            }}
          >
            {status}
          </Button>
        ))}
      </HStack>
      <SimpleGrid mt={4} columns={{ base: 1, md: 2, lg: 3 }} gap={4}>
        {sessions.map((session: SessionType) => {
          return <SessionCard key={session.id} session={session} />;
        })}
      </SimpleGrid>

      {sessions.length === 0 && (
        <Text mt={4} color="gray.500">
          No sessions found.
        </Text>
      )}
    </>
  );
};
