import BaseModel from "./BaseModel";

const FIELDS = [
  "resourcePolicyId",
  "computeResourceId",
  "groupResourceProfileId",
  "queuename",
  "maxAllowedNodes",
  "maxAllowedCores",
  "maxAllowedWalltime",
];

export default class BatchQueueResourcePolicy extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
  }

  validate(batchQueue) {
    let validationResults = {};
    if (this.maxAllowedNodes && this.maxAllowedNodes < 1) {
      validationResults["maxAllowedNodes"] = "Must be at least 1.";
    } else if (this.maxAllowedNodes > batchQueue.maxNodes) {
      validationResults[
        "maxAllowedNodes"
      ] = `Must be at most ${batchQueue.maxNodes}.`;
    }
    if (this.maxAllowedCores && this.maxAllowedCores < 1) {
      validationResults["maxAllowedCores"] = "Must be at least 1.";
    } else if (this.maxAllowedCores > batchQueue.maxProcessors) {
      validationResults[
        "maxAllowedCores"
      ] = `Must be at most ${batchQueue.maxProcessors}.`;
    }
    if (this.maxAllowedWalltime && this.maxAllowedWalltime < 1) {
      validationResults["maxAllowedWalltime"] = "Must be at least 1.";
    } else if (this.maxAllowedWalltime > batchQueue.maxRunTime) {
      validationResults[
        "maxAllowedWalltime"
      ] = `Must be at most ${batchQueue.maxRunTime}.`;
    }
    return validationResults;
  }
}
