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
}
