import BaseModel from './BaseModel';
import ComputationalResourceSchedulingModel from './ComputationalResourceSchedulingModel'

const FIELDS = [
    {
        name: 'airavataAutoSchedule',
        type: 'boolean',
        default: false,
    },
    {
        name: 'overrideManualScheduledParams',
        type: 'boolean',
        default: false,
    },
    {
        name: 'shareExperimentPublicly',
        type: 'boolean',
        default: false,
    },
    {
        name: 'computationalResourceScheduling',
        type: ComputationalResourceSchedulingModel,
        default: BaseModel.defaultNewInstance(ComputationalResourceSchedulingModel),
    },
    {
        name: 'throttleResources',
        type: 'boolean',
        default: false,
    },
    {
        name: 'userDN',
        type: 'string',
        default: '',
    },
    {
        name: 'generateCert',
        type: 'boolean',
        default: false,
    },
    {
        name: 'storageId',
        type: 'string',
        default: '',
    },
    {
        name: 'experimentDataDir',
        type: 'string',
        default: '',
    },
    {
        name: 'useUserCRPref',
        type: 'boolean',
        default: false,
    }
];

export default class UserConfigurationData extends BaseModel {
    constructor(data = {}) {
        super(FIELDS, data);
    }
}
