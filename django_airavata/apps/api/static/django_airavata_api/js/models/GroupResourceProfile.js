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
         default: BaseModel.defaultNewInstance(Array),
     },
     {
         name: 'computeResourcePolicies',
         type: ComputeResourcePolicy,
         list: true,
         default: BaseModel.defaultNewInstance(Array),
     },
     {
         name: 'batchQueueResourcePolicies',
         type: BatchQueueResourcePolicy,
         list: true,
         default: BaseModel.defaultNewInstance(Array),
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

    getBatchQueueResourcePolicies(computeResourceId) {
        return this.batchQueueResourcePolicies.filter(pol => pol.computeResourceId === computeResourceId);
    }

    mergeComputeResourcePreference(computeResourcePreference, computeResourcePolicy, batchQueueResourcePolicies) {
        // merge/add computeResourcePreference and computeResourcePolicy
        const existingComputeResourcePreference = this.computePreferences.find(pref => pref.computeResourceId === computeResourcePreference.computeResourceId);
        if (existingComputeResourcePreference) {
            Object.assign(existingComputeResourcePreference, computeResourcePreference);
        } else {
            this.computePreferences.push(computeResourcePreference);
        }
        const existingComputeResourcePolicy = this.computeResourcePolicies.find(pol => pol.computeResourceId === computeResourcePolicy.computeResourceId);
        if (existingComputeResourcePolicy) {
            Object.assign(existingComputeResourcePolicy, computeResourcePolicy);
        } else {
            this.computeResourcePolicies.push(computeResourcePolicy);
        }
        // merge/add/remove batchQueueResourcePolicies
        const existingBatchQueueResourcePolicies = this.batchQueueResourcePolicies.filter(pol => pol.computeResourceId === computeResourcePreference.computeResourceId);
        for (const batchQueueResourcePolicy of batchQueueResourcePolicies) {
            const existingBatchQueueResourcePolicy = existingBatchQueueResourcePolicies.find(pol => pol.queuename === batchQueueResourcePolicy.queuename);
            if (existingBatchQueueResourcePolicy) {
                Object.assign(existingBatchQueueResourcePolicy, batchQueueResourcePolicy);
                const existingBatchQueueResourcePolicyIndex = existingBatchQueueResourcePolicies.findIndex(pol => pol.queuename === batchQueueResourcePolicy.queuename);
                if (existingBatchQueueResourcePolicyIndex >= 0) {
                    existingBatchQueueResourcePolicies.splice(existingBatchQueueResourcePolicyIndex, 1);
                }
            } else {
                this.batchQueueResourcePolicies.push(batchQueueResourcePolicy);
            }
        }
        for (const existingBatchQueueResourcePolicy of existingBatchQueueResourcePolicies) {
            const existingBatchQueueResourcePolicyIndex = this.batchQueueResourcePolicies.findIndex(
                pol => pol.computeResourceId === existingBatchQueueResourcePolicy.computeResourceId && pol.queuename === existingBatchQueueResourcePolicy.queuename);
            if (existingBatchQueueResourcePolicyIndex >= 0) {
                this.batchQueueResourcePolicies.splice(existingBatchQueueResourcePolicyIndex, 1);
            }
        }
    }
}
