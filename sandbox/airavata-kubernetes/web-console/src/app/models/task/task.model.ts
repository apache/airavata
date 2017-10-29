import {TaskParam} from "./task.param.model";
import {TaskStatus} from "./task.status.model";
/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
export class Task {

    id: number;
    taskType: number;
    taskTypeStr: string;
    parentProcessId: number;
    creationTime: number;
    lastUpdateTime: number;
    taskStatus: Array<TaskStatus> = [];
    taskDetail: string;
    taskErrorIds: Array<number> = [];
    taskParams: Array<TaskParam> = [];
    jobIds: Array<number> = [];
    order: number;


  constructor(id: number, taskType: number, parentProcessId: number, creationTime: number,
              lastUpdateTime: number, taskStatus: Array<TaskStatus>, taskDetail: string,
              taskErrorIds: Array<number>, taskParams: Array<TaskParam>, jobIds: Array<number>,
              order: number) {
    this.id = id;
    this.taskType = taskType;
    this.parentProcessId = parentProcessId;
    this.creationTime = creationTime;
    this.lastUpdateTime = lastUpdateTime;
    this.taskStatus = taskStatus;
    this.taskDetail = taskDetail;
    this.taskErrorIds = taskErrorIds;
    this.taskParams = taskParams;
    this.jobIds = jobIds;
    this.order = order;
  }
}
