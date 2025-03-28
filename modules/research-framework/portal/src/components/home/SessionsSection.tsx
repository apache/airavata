// @ts-expect-error This is fine
import { MOCK_SESSIONS } from "../../data/MOCK_DATA";
import { SessionType } from "@/interfaces/SessionType";
import { SessionCard } from "./SessionCard";
import { GridContainer } from "../GridContainer";

export const SessionsSection = () => {
  return (
    <>
      <GridContainer>
        {MOCK_SESSIONS.map((session: SessionType) => {
          return <SessionCard key={session.sessionId} session={session} />;
        })}
      </GridContainer>
    </>
  );
};
