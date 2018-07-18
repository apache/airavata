import BaseModel from './BaseModel'


const FIELDS = [
     'resourcePolicyId',
     'computeResourceId',
     'groupResourceProfileId',
     'queuename',
     'maxAllowedNodes',
     'maxAllowedCores',
     'maxAllowedWalltime',
];

export default class BatchQueueResourcePolicy extends BaseModel {

    constructor(data = {}) {
        super(FIELDS, data);
    }
}
