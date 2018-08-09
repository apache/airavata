import BaseModel from './BaseModel'
import BatchQueueResourcePolicy from './BatchQueueResourcePolicy'
import ComputeResourcePolicy from './ComputeResourcePolicy'
import GroupComputeResourcePreference from './GroupComputeResourcePreference'

const FIELDS = [
     'gatewayId',
     'groupResourceProfileId',
     'groupResourceProfileName',
     {
         name: 'computePreferences',
         type: GroupComputeResourcePreference,
         list: true,
     },
     {
         name: 'computeResourcePolicies',
         type: ComputeResourcePolicy,
         list: true,
     },
     {
         name: 'batchQueueResourcePolicies',
         type: BatchQueueResourcePolicy,
         list: true
     },
     {
         name: 'creationTime',
         type: 'date',
     },
     {
         name: 'updatedTime',
         type: 'date',
     },
     'userHasWriteAccess', // true if current user has write access
];

export default class GroupResourceProfile extends BaseModel {

    constructor(data = {}) {
        super(FIELDS, data);
    }

    getComputeResourcePolicy(computeResourceId) {
        return this.computeResourcePolicies.find(pol => pol.computeResourceId === computeResourceId);
    }

    setComputeResourcePolicy(computeResourcePolicy) {
        const currentPolicy = this.getComputeResourcePolicy(computeResourcePolicy.computeResourceId);
        if (currentPolicy) {
            Object.assign(currentPolicy, computeResourcePolicy);
        } else {
            this.computeResourcePolicies.push(computeResourcePolicy);
        }
    }

    getBatchQueueResourcePolicies(computeResourceId) {
        return this.batchQueueResourcePolicies.filter(pol => pol.computeResourceId === computeResourceId);
    }

    setComputeResourcePolicy(batchQueueResourcePolicies) {
        for (let newPolicy of batchQueueResourcePolicies) {
            const currentPolicy = this.batchQueueResourcePolicies.find(pol => pol.resourcePolicyId === newPolicy.resourcePolicyId);
            if (currentPolicy) {
                Object.assign(currentPolicy, newPolicy);
            } else {
                this.batchQueueResourcePolicies.push(newPolicy);
            }
        }
    }
}
