import {ProcessStatus} from "./process.status.model";
import {Task} from "../task/task.model";
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
    processStatuses: Array<ProcessStatus> = [];
    tasks: Array<Task> = [];
    processErrorIds: Array<number> = [];
    taskDag: string;
    experimentDataDir: string;
}
