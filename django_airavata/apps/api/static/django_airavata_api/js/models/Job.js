import BaseModel from './BaseModel';
import JobStatus from './JobStatus';

const FIELDS = [
     'jobId',
     'taskId',
     'processId',
     'jobDescription',
     {
         name: 'creationTime',
         type: 'date',
     },
     {
         name: 'jobStatuses',
         type: JobStatus,
         list: true,
     },
     'computeResourceConsumed',
     'jobName',
     'workingDir',
     'stdOut',
     'stdErr',
     'exitCode',
];

export default class Job extends BaseModel {
    constructor(data = {}) {
        super(FIELDS, data);
    }

    // get the first job status
    get jobStatus() {
        return (this.jobStatuses && this.jobStatuses.length > 0) ? this.jobStatuses[0] : null;
    }

    get jobStatusStateName() {
        return this.jobStatus ? this.jobStatus.jobState.name : null;
    }

    get jobStatusTimeOfStateChange() {
        return this.jobStatus ? this.jobStatus.timeOfStateChange : null;
    }

    get jobStatusReason() {
        return this.jobStatus ? this.jobStatus.reason : null;
    }
}
