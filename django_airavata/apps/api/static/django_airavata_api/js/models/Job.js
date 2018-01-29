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
}
