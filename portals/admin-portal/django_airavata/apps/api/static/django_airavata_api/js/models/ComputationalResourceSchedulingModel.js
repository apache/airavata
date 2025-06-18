import BaseModel from "./BaseModel";

const FIELDS = [
  "resourceHostId",
  "totalCPUCount",
  "nodeCount",
  "numberOfThreads",
  "queueName",
  "wallTimeLimit",
  "totalPhysicalMemory",
  "chessisNumber",
  "staticWorkingDir",
  "overrideLoginUserName",
  "overrideScratchLocation",
  "overrideAllocationProjectNumber",
];

export default class ComputationalResourceSchedulingModel extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
  }

  validate(queueInfo = null, batchQueueResourcePolicy = null) {
    const validationResults = {};
    if (this.isEmpty(this.resourceHostId)) {
      validationResults["resourceHostId"] = "Please select a compute resource.";
    }
    if (this.isEmpty(this.queueName)) {
      validationResults["queueName"] = "Please select a queue.";
    }
    if (!(this.nodeCount > 0)) {
      validationResults["nodeCount"] = "Enter a node count greater than 0.";
    } else if (
      batchQueueResourcePolicy &&
      this.nodeCount > batchQueueResourcePolicy.maxAllowedNodes
    ) {
      validationResults[
        "nodeCount"
      ] = `Enter a node count no greater than ${batchQueueResourcePolicy.maxAllowedNodes}.`;
    } else if (
      queueInfo &&
      queueInfo.maxNodes &&
      this.nodeCount > queueInfo.maxNodes
    ) {
      validationResults[
        "nodeCount"
      ] = `Enter a node count no greater than ${queueInfo.maxNodes}.`;
    }
    if (!(this.totalCPUCount > 0)) {
      validationResults["totalCPUCount"] = "Enter a core count greater than 0.";
    } else if (
      batchQueueResourcePolicy &&
      this.totalCPUCount > batchQueueResourcePolicy.maxAllowedCores
    ) {
      validationResults[
        "totalCPUCount"
      ] = `Enter a core count no greater than ${batchQueueResourcePolicy.maxAllowedCores}.`;
    } else if (
      queueInfo &&
      queueInfo.maxProcessors &&
      this.totalCPUCount > queueInfo.maxProcessors
    ) {
      validationResults[
        "totalCPUCount"
      ] = `Enter a core count no greater than ${queueInfo.maxProcessors}.`;
    }
    if (!(this.wallTimeLimit > 0)) {
      validationResults["wallTimeLimit"] =
        "Enter a wall time limit greater than 0.";
    } else if (
      batchQueueResourcePolicy &&
      this.wallTimeLimit > batchQueueResourcePolicy.maxAllowedWalltime
    ) {
      validationResults[
        "wallTimeLimit"
      ] = `Enter a wall time limit no greater than ${batchQueueResourcePolicy.maxAllowedWalltime}.`;
    } else if (
      queueInfo &&
      queueInfo.maxRunTime &&
      this.wallTimeLimit > queueInfo.maxRunTime
    ) {
      validationResults[
        "wallTimeLimit"
      ] = `Enter a wall time limit no greater than ${queueInfo.maxRunTime}.`;
    }
    if (!(this.totalPhysicalMemory >= 0)) {
      validationResults["totalPhysicalMemory"] =
        "Enter a total physical memory greater than or equal to 0.";
    } else if (
      queueInfo &&
      queueInfo.maxMemory &&
      this.totalPhysicalMemory > queueInfo.maxMemory
    ) {
      validationResults[
        "totalPhysicalMemory"
      ] = `Enter a total physical memory no greater than ${queueInfo.maxMemory}.`;
    }
    return validationResults;
  }
}
