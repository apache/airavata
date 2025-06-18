import BaseModel from "./BaseModel";
import ErrorModel from "./ErrorModel";
import Job from "./Job";
import TaskTypes from "./TaskTypes";
import TaskStatus from "./TaskStatus";

const FIELDS = [
  "taskId",
  {
    name: "taskType",
    type: TaskTypes,
  },
  "parentProcessId",
  {
    name: "creationTime",
    type: Date,
  },
  {
    name: "lastUpdateTime",
    type: Date,
  },
  {
    name: "taskStatuses",
    type: TaskStatus,
    list: true,
  },
  "taskDetail",
  "subTaskModel",
  {
    name: "taskErrors",
    type: ErrorModel,
    list: true,
  },
  {
    name: "jobs",
    type: Job,
    list: true,
  },
  "maxRetry",
  "currentRetry",
];

export default class Task extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
  }

  get latestStatus() {
    if (this.taskStatuses && this.taskStatuses.length > 0) {
      return this.taskStatuses[this.taskStatuses.length - 1];
    } else {
      return null;
    }
  }
}
