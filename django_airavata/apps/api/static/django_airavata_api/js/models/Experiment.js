
import BaseModel from './BaseModel';
import ErrorModel from './ErrorModel'
import ExperimentState from './ExperimentState'
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
    'description',
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

    validate() {
        let validationResults = {};
        const experimentInputsValidation = this.experimentInputs
            .map(experimentInput => {
                const validation = experimentInput.validate();
                if (validation && 'value' in validation) {
                    return {[experimentInput.name]: validation};
                } else {
                    return null;
                }
            })
            .reduce((accumulator, currentValue) => Object.assign(accumulator, currentValue), {});
        if (Object.keys(experimentInputsValidation).length > 0) {
            validationResults['experimentInputs'] = experimentInputsValidation;
        }
        const userConfigurationDataValidation = this.userConfigurationData.validate();
        if (Object.keys(userConfigurationDataValidation).length > 0) {
            validationResults['userConfigurationData'] = userConfigurationDataValidation;
        }
        if (this.isEmpty(this.experimentName)) {
            validationResults['experimentName'] = "Please provide a name for this experiment.";
        }
        if (this.isEmpty(this.projectId)) {
            validationResults['projectId'] = "Please select a project.";
        }
        return validationResults;
    }

    get isProgressing() {
        const progressingStates = [ExperimentState.SCHEDULED,
                                   ExperimentState.LAUNCHED,
                                   ExperimentState.EXECUTING,
                                   ExperimentState.CANCELING];
        return this.experimentStatus
            && this.experimentStatus.length > 0
            && progressingStates.indexOf(this.experimentStatus[0].state) >= 0;
    }
}
