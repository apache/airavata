import {TaskParam} from "./task.param.model";
/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
export class Task {

    id: number;
    taskType: number;
    parentProcessId: number;
    creationTime: number;
    lastUpdateTime: number;
    taskStatusIds: Array<number> = [];
    taskDetail: string;
    taskErrorIds: Array<number> = [];
    taskParams: Array<TaskParam> = [];
    jobIds: Array<number> = [];
    order: number;


  constructor(id: number, taskType: number, parentProcessId: number, creationTime: number,
              lastUpdateTime: number, taskStatusIds: Array<number>, taskDetail: string,
              taskErrorIds: Array<number>, taskParams: Array<TaskParam>, jobIds: Array<number>,
              order: number) {
    this.id = id;
    this.taskType = taskType;
    this.parentProcessId = parentProcessId;
    this.creationTime = creationTime;
    this.lastUpdateTime = lastUpdateTime;
    this.taskStatusIds = taskStatusIds;
    this.taskDetail = taskDetail;
    this.taskErrorIds = taskErrorIds;
    this.taskParams = taskParams;
    this.jobIds = jobIds;
    this.order = order;
  }
}
