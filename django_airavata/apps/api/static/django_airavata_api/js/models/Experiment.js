import BaseModel from "./BaseModel";
import ErrorModel from "./ErrorModel";
import ExperimentState from "./ExperimentState";
import ExperimentStatus from "./ExperimentStatus";
import InputDataObjectType from "./InputDataObjectType";
import OutputDataObjectType from "./OutputDataObjectType";
import ProcessModel from "./ProcessModel";
import UserConfigurationData from "./UserConfigurationData";

const FIELDS = [
  "experimentId",
  "projectId",
  "gatewayId",
  {
    name: "experimentType",
    type: "number",
    default: 0,
  },
  "userName",
  "experimentName",
  {
    name: "creationTime",
    type: "date",
  },
  "description",
  "executionId",
  {
    name: "enableEmailNotification",
    type: "boolean",
    default: false,
  },
  {
    name: "emailAddresses",
    type: "string",
    list: true,
  },
  {
    name: "userConfigurationData",
    type: UserConfigurationData,
    default: BaseModel.defaultNewInstance(UserConfigurationData),
  },
  {
    name: "experimentInputs",
    type: InputDataObjectType,
    list: true,
    default: BaseModel.defaultNewInstance(Array),
  },
  {
    name: "experimentOutputs",
    type: OutputDataObjectType,
    list: true,
  },
  {
    name: "experimentStatus",
    type: ExperimentStatus,
    list: true,
  },
  {
    name: "errors",
    type: ErrorModel,
    list: true,
  },
  {
    name: "processes",
    type: ProcessModel,
    list: true,
  },
  "workflow",
  {
    name: "userHasWriteAccess",
    type: "boolean",
    default: true,
  },
];

export default class Experiment extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
    this.evaluateInputDependencies();
  }

  validate() {
    let validationResults = {};
    if (this.isEmpty(this.experimentName)) {
      validationResults["experimentName"] =
        "Please provide a name for this experiment.";
    }
    if (this.isEmpty(this.projectId)) {
      validationResults["projectId"] = "Please select a project.";
    }
    return validationResults;
  }

  get latestStatus() {
    if (this.experimentStatus && this.experimentStatus.length > 0) {
      return this.experimentStatus[this.experimentStatus.length - 1];
    } else {
      return null;
    }
  }

  get isProgressing() {
    return this.latestStatus && this.latestStatus.isProgressing;
  }

  get isFinished() {
    return this.latestStatus && this.latestStatus.isFinished;
  }

  get hasLaunched() {
    const hasLaunchedStates = [
      ExperimentState.SCHEDULED,
      ExperimentState.LAUNCHED,
      ExperimentState.EXECUTING,
      ExperimentState.CANCELING,
      ExperimentState.CANCELED,
      ExperimentState.FAILED,
      ExperimentState.COMPLETED,
    ];
    return (
      this.latestStatus &&
      hasLaunchedStates.indexOf(this.latestStatus.state) >= 0
    );
  }

  get isEditable() {
    return (
      (!this.latestStatus ||
        this.latestStatus.state === ExperimentState.CREATED) &&
      this.userHasWriteAccess
    );
  }

  get isCancelable() {
    switch (this.latestStatus.state) {
      case ExperimentState.VALIDATED:
      case ExperimentState.SCHEDULED:
      case ExperimentState.LAUNCHED:
      case ExperimentState.EXECUTING:
        return true;
      default:
        return false;
    }
  }

  get resourceHostId() {
    return this.userConfigurationData &&
      this.userConfigurationData.computationalResourceScheduling
      ? this.userConfigurationData.computationalResourceScheduling
          .resourceHostId
      : null;
  }

  populateInputsOutputsFromApplicationInterface(applicationInterface) {
    // Copy application inputs and outputs to the experiment
    this.experimentInputs = applicationInterface.applicationInputs.map(
      (input) => input.clone()
    );
    this.evaluateInputDependencies();
    this.experimentOutputs = applicationInterface.applicationOutputs.slice();
  }

  evaluateInputDependencies() {
    const inputValues = this._collectInputValues(this.experimentInputs);
    for (const input of this.experimentInputs) {
      input.evaluateDependencies(inputValues);
    }
  }

  getExperimentInput(inputName) {
    return this.experimentInputs.find(inp => inp.name === inputName);
  }

  getExperimentOutput(outputName) {
    return this.experimentOutputs.find(out => out.name === outputName);
  }

  _collectInputValues() {
    const result = {};
    this.experimentInputs.forEach((inp) => {
      result[inp.name] = inp.value;
    });
    return result;
  }
}
