import BaseModel from './BaseModel'
import ParallelismType from './ParallelismType';
import CommandObject from './CommandObject';
import SetEnvPath from './SetEnvPath';


const FIELDS = [
  'appDeploymentId',
  'appModuleId',
  'computeHostId',
  'executablePath',
  {
    name: 'parallelism',
    type: ParallelismType,
    default: ParallelismType.SERIAL,
  },
  'appDeploymentDescription',
  {
    name: 'moduleLoadCmds',
    type: CommandObject,
    list: true,
  },
  {
    name: 'libPrependPaths',
    type: SetEnvPath,
    list: true,
  },
  {
    name: 'libAppendPaths',
    type: SetEnvPath,
    list: true,
  },
  {
    name: 'setEnvironment',
    type: SetEnvPath,
    list: true,
  },
  {
    name: 'preJobCommands',
    type: CommandObject,
    list: true,
  },
  {
    name: 'postJobCommands',
    type: CommandObject,
    list: true,
  },
  'defaultQueueName',
  'defaultNodeCount',
  'defaultCPUCount',
  'defaultWalltime',
  'editableByUser',
  'userHasWriteAccess'
];

export default class ApplicationDeploymentDescription extends BaseModel {

  constructor(data = {}) {
    super(FIELDS, data);
  }
}
