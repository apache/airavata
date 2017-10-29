/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
export class Process {

    id: number;
    experimentId: number;
    creationTime: number;
    lastUpdateTime: number;
    processStatuses: Array<number> = [];
    tasks: Array<number> = [];
    processErrorIds: Array<number> = [];
    taskDag: string;
    experimentDataDir: string;

    constructor(id: number, experimentId: number, creationTime: number, lastUpdateTime: number, processStatuses: Array<number>, tasks: Array<number>, processErrorIds: Array<number>, taskDag: string, experimentDataDir: string) {
      this.id = id;
      this.experimentId = experimentId;
      this.creationTime = creationTime;
      this.lastUpdateTime = lastUpdateTime;
      this.processStatuses = processStatuses;
      this.tasks = tasks;
      this.processErrorIds = processErrorIds;
      this.taskDag = taskDag;
      this.experimentDataDir = experimentDataDir;
    }
}
