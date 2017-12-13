import BaseModel from './BaseModel';
import UserConfigurationData from './UserConfigurationData'
import InputDataObjectType from './InputDataObjectType'
import OutputDataObjectType from './OutputDataObjectType'
import ExperimentStatus from './ExperimentStatus'
import ErrorModel from './ErrorModel'

const FIELDS = [
    'experimentId',
    'projectId',
    'gatewayId',
    'experimentType',
    'userName',
    'experimentName',
    {
        name: 'creationTime',
        type: 'date'
    },
    'description',
    'executionId',
    'enableEmailNotification',
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
    // TODO: map the ProcessModel
    // {
    //     name: 'processes',
    //     type: ProcessModel,
    //     list: true,
    // },
];

export default class Experiment extends BaseModel {
    constructor(data = {}) {
        super(FIELDS, data);
    }
}
