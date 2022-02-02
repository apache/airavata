import BaseModel from "./BaseModel";
import ProcessState from "./ProcessState";

const FIELDS = [
  {
    name: "state",
    type: ProcessState,
  },
  {
    name: "timeOfStateChange",
    type: Date,
  },
  "reason",
  "statusId",
];

export default class ProcessStatus extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
  }

  get isFinished() {
    return this.state && this.state.isFinished;
  }
}
