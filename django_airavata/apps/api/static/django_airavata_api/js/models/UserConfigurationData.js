import BaseModel from './BaseModel';
import ComputationalResourceSchedulingModel from './ComputationalResourceSchedulingModel'

const FIELDS = [
    'airavataAutoSchedule',
    'overrideManualScheduledParams',
    'shareExperimentPublicly',
    {
        name: 'computationalResourceScheduling',
        type: ComputationalResourceSchedulingModel,
        default: BaseModel.defaultNewInstance(ComputationalResourceSchedulingModel),
    },
    'throttleResources',
    'userDN',
    'generateCert',
    'storageId',
    'experimentDataDir',
    'useUserCRPref',
];

export default class UserConfigurationData extends BaseModel {
    constructor(data = {}) {
        super(FIELDS, data);
    }
}
