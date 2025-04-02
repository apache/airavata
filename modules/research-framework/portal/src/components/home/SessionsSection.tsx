import { SessionType } from "@/interfaces/SessionType";
import { SessionCard } from "./SessionCard";
import { GridContainer } from "../GridContainer";
import api from "@/lib/api";
import { useEffect, useState } from "react";

async function getSessions() {
  try {
    const response = await api.get("/hub/sessions");
    const data = response.data;
    return data;
  } catch (error) {
    console.error("Error fetching:", error);
  }
}

export const SessionsSection = () => {
  const [sessions, setSessions] = useState<SessionType[]>([]);

  useEffect(() => {
    let intervalId: NodeJS.Timeout;

    async function fetchData() {
      const sessions = await getSessions();
      setSessions(sessions);
    }

    fetchData(); // Initial fetch

    // eslint-disable-next-line prefer-const
    intervalId = setInterval(fetchData, 2000); // Fetch every 2 seconds

    return () => clearInterval(intervalId); // Cleanup on unmount
  }, []);

  return (
    <>
      <GridContainer>
        {sessions.map((session: SessionType) => {
          return <SessionCard key={session.id} session={session} />;
        })}
      </GridContainer>
    </>
  );
};
