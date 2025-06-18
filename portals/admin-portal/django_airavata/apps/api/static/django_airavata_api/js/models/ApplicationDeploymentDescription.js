import BaseModel from "./BaseModel";
import ParallelismType from "./ParallelismType";
import CommandObject from "./CommandObject";
import SetEnvPaths from "./SetEnvPaths";

const FIELDS = [
  "appDeploymentId",
  "appModuleId",
  "computeHostId",
  "executablePath",
  {
    name: "parallelism",
    type: ParallelismType,
    default: ParallelismType.SERIAL,
  },
  "appDeploymentDescription",
  {
    name: "moduleLoadCmds",
    type: CommandObject,
    list: true,
  },
  {
    name: "libPrependPaths",
    type: SetEnvPaths,
    list: true,
  },
  {
    name: "libAppendPaths",
    type: SetEnvPaths,
    list: true,
  },
  {
    name: "setEnvironment",
    type: SetEnvPaths,
    list: true,
  },
  {
    name: "preJobCommands",
    type: CommandObject,
    list: true,
  },
  {
    name: "postJobCommands",
    type: CommandObject,
    list: true,
  },
  "defaultQueueName",
  "defaultNodeCount",
  "defaultCPUCount",
  "defaultWalltime",
  {
    name: "editableByUser",
    type: "boolean",
    default: false,
  },
  "userHasWriteAccess",
];

export default class ApplicationDeploymentDescription extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
  }
}
