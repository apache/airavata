import { ProjectType } from "./ProjectType";
import { SessionStatusEnum } from "./SessionStatusEnum";
import { User } from "./UserType";

export interface SessionType {
  id: string;
  sessionName: string;
  user: User;
  project: ProjectType;
  createdAt: string;
  updatedAt: string;
  status: SessionStatusEnum;
}
