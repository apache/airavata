import BaseModel from "./BaseModel";
import JobState from "./JobState";

const FIELDS = [
  {
    name: "jobState",
    type: JobState,
  },
  {
    name: "timeOfStateChange",
    type: "date",
  },
  "reason",
  "statusId",
];

export default class JobStatus extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
  }
}
