import BaseModel from "./BaseModel";
import BatchQueueResourcePolicy from "./BatchQueueResourcePolicy";
import ComputeResourcePolicy from "./ComputeResourcePolicy";
import GroupComputeResourcePreference from "./GroupComputeResourcePreference";

const FIELDS = [
  "gatewayId",
  "groupResourceProfileId",
  "groupResourceProfileName",
  {
    name: "computePreferences",
    type: GroupComputeResourcePreference,
    list: true,
    default: BaseModel.defaultNewInstance(Array),
  },
  {
    name: "computeResourcePolicies",
    type: ComputeResourcePolicy,
    list: true,
    default: BaseModel.defaultNewInstance(Array),
  },
  {
    name: "batchQueueResourcePolicies",
    type: BatchQueueResourcePolicy,
    list: true,
    default: BaseModel.defaultNewInstance(Array),
  },
  {
    name: "creationTime",
    type: "date",
  },
  {
    name: "updatedTime",
    type: "date",
  },
  "defaultCredentialStoreToken",
  "userHasWriteAccess", // true if current user has write access
];

export default class GroupResourceProfile extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
  }

  getComputePreference(computeResourceId) {
    return this.computePreferences.find(
      (pref) => pref.computeResourceId === computeResourceId
    );
  }

  getComputeResourcePolicy(computeResourceId) {
    return this.computeResourcePolicies.find(
      (pol) => pol.computeResourceId === computeResourceId
    );
  }

  getBatchQueueResourcePolicies(computeResourceId) {
    return this.batchQueueResourcePolicies.filter(
      (pol) => pol.computeResourceId === computeResourceId
    );
  }

  mergeComputeResourcePreference(
    computeResourcePreference,
    computeResourcePolicy,
    batchQueueResourcePolicies
  ) {
    // merge/add computeResourcePreference and computeResourcePolicy
    const existingComputeResourcePreference = this.computePreferences.find(
      (pref) =>
        pref.computeResourceId === computeResourcePreference.computeResourceId
    );
    if (existingComputeResourcePreference) {
      Object.assign(
        existingComputeResourcePreference,
        computeResourcePreference
      );
    } else {
      this.computePreferences.push(computeResourcePreference);
    }
    const existingComputeResourcePolicy = this.computeResourcePolicies.find(
      (pol) => pol.computeResourceId === computeResourcePolicy.computeResourceId
    );
    if (existingComputeResourcePolicy) {
      Object.assign(existingComputeResourcePolicy, computeResourcePolicy);
    } else {
      this.computeResourcePolicies.push(computeResourcePolicy);
    }
    // merge/add/remove batchQueueResourcePolicies
    const existingBatchQueueResourcePolicies = this.batchQueueResourcePolicies.filter(
      (pol) =>
        pol.computeResourceId === computeResourcePreference.computeResourceId
    );
    for (const batchQueueResourcePolicy of batchQueueResourcePolicies) {
      const existingBatchQueueResourcePolicy = existingBatchQueueResourcePolicies.find(
        (pol) => pol.queuename === batchQueueResourcePolicy.queuename
      );
      if (existingBatchQueueResourcePolicy) {
        Object.assign(
          existingBatchQueueResourcePolicy,
          batchQueueResourcePolicy
        );
        const existingBatchQueueResourcePolicyIndex = existingBatchQueueResourcePolicies.findIndex(
          (pol) => pol.queuename === batchQueueResourcePolicy.queuename
        );
        if (existingBatchQueueResourcePolicyIndex >= 0) {
          existingBatchQueueResourcePolicies.splice(
            existingBatchQueueResourcePolicyIndex,
            1
          );
        }
      } else {
        this.batchQueueResourcePolicies.push(batchQueueResourcePolicy);
      }
    }
    for (const existingBatchQueueResourcePolicy of existingBatchQueueResourcePolicies) {
      const existingBatchQueueResourcePolicyIndex = this.batchQueueResourcePolicies.findIndex(
        (pol) =>
          pol.computeResourceId ===
            existingBatchQueueResourcePolicy.computeResourceId &&
          pol.queuename === existingBatchQueueResourcePolicy.queuename
      );
      if (existingBatchQueueResourcePolicyIndex >= 0) {
        this.batchQueueResourcePolicies.splice(
          existingBatchQueueResourcePolicyIndex,
          1
        );
      }
    }
  }

  /**
   * Remove compute resource preference, compute resource policy and batch queue policies.
   * @param {string} computeResourceId
   * @returns {boolean} true if this GroupResourceProfile was changed
   */
  removeComputeResource(computeResourceId) {
    let removedChildren = false;
    const existingComputeResourcePreferenceIndex = this.computePreferences.findIndex(
      (pref) => pref.computeResourceId === computeResourceId
    );
    if (existingComputeResourcePreferenceIndex >= 0) {
      this.computePreferences.splice(existingComputeResourcePreferenceIndex, 1);
      removedChildren = true;
    }
    const existingComputeResourcePolicyIndex = this.computeResourcePolicies.findIndex(
      (pol) => pol.computeResourceId === computeResourceId
    );
    if (existingComputeResourcePolicyIndex >= 0) {
      this.computeResourcePolicies.splice(
        existingComputeResourcePolicyIndex,
        1
      );
      removedChildren = true;
    }
    const existingBatchQueueResourcePolicies = this.batchQueueResourcePolicies.filter(
      (pol) => pol.computeResourceId === computeResourceId
    );
    for (const existingBatchQueueResourcePolicy of existingBatchQueueResourcePolicies) {
      const existingBatchQueueResourcePolicyIndex = this.batchQueueResourcePolicies.indexOf(
        existingBatchQueueResourcePolicy
      );
      this.batchQueueResourcePolicies.splice(
        existingBatchQueueResourcePolicyIndex,
        1
      );
      removedChildren = true;
    }

    return removedChildren;
  }
}
