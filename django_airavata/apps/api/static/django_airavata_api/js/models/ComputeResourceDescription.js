import BaseModel from "./BaseModel";
import BatchQueue from "./BatchQueue";

const FIELDS = [
  "computeResourceId",
  "hostName",
  {
    name: "hostAliases",
    type: "string",
    list: true,
  },
  {
    name: "ipAddresses",
    type: "string",
    list: true,
  },
  "resourceDescription",
  "enabled",
  {
    name: "batchQueues",
    type: BatchQueue,
    list: true,
  },
  // TODO: map these
  // 'fileSystems',
  // 'jobSubmissionInterfaces',
  // 'dataMovementInterfaces',
  "maxMemoryPerNode",
  "gatewayUsageReporting",
  "gatewayUsageModuleLoadCommand",
  "gatewayUsageExecutable",
  "cpusPerNode",
  "defaultNodeCount",
  "defaultCPUCount",
  "defaultWalltime",
];

export default class ComputeResourceDescription extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
  }
}
