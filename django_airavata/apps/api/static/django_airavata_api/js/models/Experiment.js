
import BaseModel from './BaseModel';
import ErrorModel from './ErrorModel'
import ExperimentStatus from './ExperimentStatus'
import InputDataObjectType from './InputDataObjectType'
import OutputDataObjectType from './OutputDataObjectType'
import ProcessModel from './ProcessModel'
import UserConfigurationData from './UserConfigurationData'

const FIELDS = [
    'experimentId',
    'projectId',
    'gatewayId',
    {
        name: 'experimentType',
        type: 'number',
        default: 0,
    },
    'userName',
    'experimentName',
    {
        name: 'creationTime',
        type: 'date'
    },
    {
        name: 'description',
        type: 'string',
        default: '',
    },
    'executionId',
    {
        name: 'enableEmailNotification',
        type: 'boolean',
        default: false,
    },
    {
        name: 'emailAddresses',
        type: 'string',
        list: true,
    },
    {
        name: 'userConfigurationData',
        type: UserConfigurationData,
        default: BaseModel.defaultNewInstance(UserConfigurationData),
    },
    {
        name: 'experimentInputs',
        type: InputDataObjectType,
        list: true,
    },
    {
        name: 'experimentOutputs',
        type: OutputDataObjectType,
        list: true,
    },
    {
        name: 'experimentStatus',
        type: ExperimentStatus,
        list: true,
    },
    {
        name: 'errors',
        type: ErrorModel,
        list: true,
    },
    {
        name: 'processes',
        type: ProcessModel,
        list: true,
    },
];

export default class Experiment extends BaseModel {
    constructor(data = {}) {
        super(FIELDS, data);
    }
}
