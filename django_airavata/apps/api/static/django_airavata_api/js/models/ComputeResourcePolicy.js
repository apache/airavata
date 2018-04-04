import BaseModel from './BaseModel'


const FIELDS = [
     'resourcePolicyId',
     'computeResourceId',
     'groupResourceProfileId',
     {
         name: 'allowedBatchQueues',
         type: 'string',
         list: true
     }
];

export default class ComputeResourcePolicy extends BaseModel {

    constructor(data = {}) {
        super(FIELDS, data);
    }
}
