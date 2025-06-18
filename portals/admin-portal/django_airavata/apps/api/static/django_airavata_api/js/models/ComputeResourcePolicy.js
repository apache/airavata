import BaseModel from "./BaseModel";

const FIELDS = [
  "resourcePolicyId",
  "computeResourceId",
  "groupResourceProfileId",
  {
    name: "allowedBatchQueues",
    type: "string",
    list: true,
  },
];

export default class ComputeResourcePolicy extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
  }

  populateParentIdsOnBatchQueueResourcePolicy(batchQueueResourcePolicy) {
    // For new BatchQueueResourcePolicy instances, set the parent ids
    batchQueueResourcePolicy.groupResourceProfileId = this.groupResourceProfileId;
    batchQueueResourcePolicy.computeResourceId = this.computeResourceId;
    return batchQueueResourcePolicy;
  }

  validate() {
    let validationResults = {};
    if (!this.allowedBatchQueues || this.allowedBatchQueues.length === 0) {
      validationResults["allowedBatchQueues"] =
        "Must select at least one queue.";
    }
    return validationResults;
  }
}
