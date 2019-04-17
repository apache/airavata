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

    validate(queueInfo, batchQueueResourcePolicy = null) {
        const validationResults = {};
        if (this.isEmpty(this.resourceHostId)) {
            validationResults['resourceHostId'] = "Please select a compute resource.";
        }
        if (!(this.nodeCount > 0)) {
            validationResults['nodeCount'] = "Enter a node count greater than 0.";
        } else if (batchQueueResourcePolicy && this.nodeCount > batchQueueResourcePolicy.maxAllowedNodes) {
            validationResults['nodeCount'] = `Enter a node count no greater than ${batchQueueResourcePolicy.maxAllowedNodes}.`;
        } else if (queueInfo.maxNodes && this.nodeCount > queueInfo.maxNodes) {
            validationResults['nodeCount'] = `Enter a node count no greater than ${queueInfo.maxNodes}.`;
        }
        if (!(this.totalCPUCount > 0)) {
            validationResults['totalCPUCount'] = "Enter a core count greater than 0.";
        } else if (batchQueueResourcePolicy && this.totalCPUCount > batchQueueResourcePolicy.maxAllowedCores) {
            validationResults['totalCPUCount'] = `Enter a core count no greater than ${batchQueueResourcePolicy.maxAllowedCores}.`;
        } else if (queueInfo.maxProcessors && this.totalCPUCount > queueInfo.maxProcessors) {
            validationResults['totalCPUCount'] = `Enter a core count no greater than ${queueInfo.maxProcessors}.`;
        }
        if (!(this.wallTimeLimit > 0)) {
            validationResults['wallTimeLimit'] = "Enter a wall time limit greater than 0.";
        } else if (batchQueueResourcePolicy && this.wallTimeLimit > batchQueueResourcePolicy.maxAllowedWalltime) {
            validationResults['wallTimeLimit'] = `Enter a wall time limit no greater than ${batchQueueResourcePolicy.maxAllowedWalltime}.`;
        } else if (queueInfo.maxRunTime && this.wallTimeLimit > queueInfo.maxRunTime) {
            validationResults['wallTimeLimit'] = `Enter a wall time limit no greater than ${queueInfo.maxRunTime}.`;
        }
        return validationResults;
    }
}
