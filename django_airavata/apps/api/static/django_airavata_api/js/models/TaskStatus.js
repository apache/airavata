import BaseModel from "./BaseModel";
import TaskState from "./TaskState";

const FIELDS = [
  {
    name: "state",
    type: TaskState,
  },
  {
    name: "timeOfStateChange",
    type: Date,
  },
  "reason",
  "statusId",
];

export default class TaskStatus extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
  }
}
