import BaseModel from './BaseModel'


const FIELDS = [
  'appDeploymentId',
  'appModuleId',
  'computeHostId',
  'executablePath',
  'parallelism',
  'appDeploymentDescription',
  // TODO: map these
  // 'moduleLoadCmds',
  // 'libPrependPaths',
  // 'libAppendPaths',
  // 'setEnvironment',
  // 'preJobCommands',
  // 'postJobCommands',
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
