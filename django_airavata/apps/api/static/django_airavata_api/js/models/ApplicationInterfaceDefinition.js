import BaseModel from "./BaseModel";
import InputDataObjectType from "./InputDataObjectType";
import OutputDataObjectType from "./OutputDataObjectType";
import DataType from "./DataType";
import Experiment from "./Experiment";

const FIELDS = [
  "applicationInterfaceId",
  "applicationName",
  "applicationDescription",
  {
    name: "applicationModules",
    type: "string",
    list: true
  },
  // When saving/updating, the order of the inputs in the applicationInputs
  // array determines the 'inputOrder' that will be applied to each input on the
  // backend. Updating 'inputOrder' will have no effect.
  {
    name: "applicationInputs",
    type: InputDataObjectType,
    list: true,
    default: BaseModel.defaultNewInstance(Array)
  },
  {
    name: "applicationOutputs",
    type: OutputDataObjectType,
    list: true
  },
  {
    name: "archiveWorkingDirectory",
    type: "boolean",
    default: false
  },
  {
    name: "hasOptionalFileInputs",
    type: "boolean",
    default: false
  },
  "userHasWriteAccess"
];

export default class ApplicationInterfaceDefinition extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
  }

  addStandardOutAndStandardErrorOutputs() {
    const stdout = new OutputDataObjectType({
      name: "Standard-Out",
      type: DataType.STDOUT,
      isRequired: true,
      metaData: {
        "file-metadata": {
          "mime-type": "text/plain"
        }
      }
    });
    const stderr = new OutputDataObjectType({
      name: "Standard-Error",
      type: DataType.STDERR,
      isRequired: true,
      metaData: {
        "file-metadata": {
          "mime-type": "text/plain"
        }
      }
    });
    if (!this.applicationOutputs) {
      this.applicationOutputs = [];
    }
    this.applicationOutputs.push(stdout, stderr);
  }

  createExperiment() {
    const experiment = new Experiment();
    experiment.populateInputsOutputsFromApplicationInterface(this);
    experiment.executionId = this.applicationInterfaceId;
    return experiment;
  }
}
