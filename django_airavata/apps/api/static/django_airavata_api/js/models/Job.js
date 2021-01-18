import BaseModel from "./BaseModel";
import JobStatus from "./JobStatus";

const FIELDS = [
  "jobId",
  "taskId",
  "processId",
  "jobDescription",
  {
    name: "creationTime",
    type: "date",
  },
  {
    name: "jobStatuses",
    type: JobStatus,
    list: true,
  },
  "computeResourceConsumed",
  "jobName",
  "workingDir",
  "stdOut",
  "stdErr",
  "exitCode",
];

export default class Job extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
  }

  // get the first job status
  get latestJobStatus() {
    if (this.jobStatuses && this.jobStatuses.length > 0) {
      return this.jobStatuses[this.jobStatuses.length - 1];
    } else {
      return null;
    }
  }

  get jobStatusStateName() {
    return this.latestJobStatus ? this.latestJobStatus.jobState.name : null;
  }

  get jobStatusTimeOfStateChange() {
    return this.latestJobStatus ? this.latestJobStatus.timeOfStateChange : null;
  }

  get jobStatusReason() {
    return this.latestJobStatus ? this.latestJobStatus.reason : null;
  }
}
