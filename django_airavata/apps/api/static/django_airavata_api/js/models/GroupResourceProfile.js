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
}
