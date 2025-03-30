export interface SessionType {
  sessionId: string;
  title: string;
  started: string;
  models: string[];
  datasets: string[];
  nodes: number;
  ram: number;
  storage: number;
  status: "running" | "stopped";
}
