import BaseModel from './BaseModel';

const FIELDS = [
    'resourceHostId',
    'totalCPUCount',
    'nodeCount',
    'numberOfThreads',
    'queueName',
    'wallTimeLimit',
    'totalPhysicalMemory',
    'chessisNumber',
    'staticWorkingDir',
    'overrideLoginUserName',
    'overrideScratchLocation',
    'overrideAllocationProjectNumber',
];

export default class ComputationalResourceSchedulingModel extends BaseModel {
    constructor(data = {}) {
        super(FIELDS, data);
    }

    validate() {
        const validationResults = {};
        if (this.isEmpty(this.resourceHostId)) {
            validationResults['resourceHostId'] = "Please select a compute resource.";
        }
        if (!(this.nodeCount > 0)) {
            validationResults['nodeCount'] = "Enter a node count greater than 0.";
        }
        if (!(this.totalCPUCount > 0)) {
            validationResults['totalCPUCount'] = "Enter a core count greater than 0.";
        }
        if (!(this.wallTimeLimit > 0)) {
            validationResults['wallTimeLimit'] = "Enter a wall time limit greater than 0.";
        }
        return validationResults;
    }
}
