import BaseModel from './BaseModel';

const FIELDS = [
    'resourceHostId',
    'totalCPUCount',
    'nodeCount',
    'numberOfThreads',
    'queueName',
    'wallTimeLimit',
    'totalPhysicalMemory',
    {
        name: 'chessisNumber',
        type: 'string',
        default: '',
    },
    {
        name: 'staticWorkingDir',
        type: 'string',
        default: '',
    },
    {
        name: 'overrideLoginUserName',
        type: 'string',
        default: '',
    },
    {
        name: 'overrideScratchLocation',
        type: 'string',
        default: '',
    },
    {
        name: 'overrideAllocationProjectNumber',
        type: 'string',
        default: '',
    },
];

export default class ComputationalResourceSchedulingModel extends BaseModel {
    constructor(data = {}) {
        super(FIELDS, data);
    }
}
