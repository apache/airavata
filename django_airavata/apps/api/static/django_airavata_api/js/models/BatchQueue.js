import BaseModel from "./BaseModel";

const FIELDS = [
  "queueName",
  "queueDescription",
  "maxRunTime",
  "maxNodes",
  "maxProcessors",
  "maxJobsInQueue",
  "maxMemory",
  "cpuPerNode",
  "defaultNodeCount",
  "defaultCPUCount",
  "defaultWalltime",
  "queueSpecificMacros",
  "isDefaultQueue",
];

export default class BatchQueue extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
  }
}
