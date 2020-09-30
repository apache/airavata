import BaseModel from "./BaseModel";
import ComputationalResourceSchedulingModel from "./ComputationalResourceSchedulingModel";

const FIELDS = [
  {
    name: "airavataAutoSchedule",
    type: "boolean",
    default: false,
  },
  {
    name: "overrideManualScheduledParams",
    type: "boolean",
    default: false,
  },
  {
    name: "shareExperimentPublicly",
    type: "boolean",
    default: false,
  },
  {
    name: "computationalResourceScheduling",
    type: ComputationalResourceSchedulingModel,
    default: BaseModel.defaultNewInstance(ComputationalResourceSchedulingModel),
  },
  {
    name: "throttleResources",
    type: "boolean",
    default: false,
  },
  "userDN",
  {
    name: "generateCert",
    type: "boolean",
    default: false,
  },
  "storageId",
  "experimentDataDir",
  {
    name: "useUserCRPref",
    type: "boolean",
    default: false,
  },
  "groupResourceProfileId",
];

export default class UserConfigurationData extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
  }

  validate() {
    const validationResults = {};
    const computationalResourceSchedulingValidation = this.computationalResourceScheduling.validate();
    if (Object.keys(computationalResourceSchedulingValidation).length > 0) {
      validationResults[
        "computationalResourceScheduling"
      ] = computationalResourceSchedulingValidation;
    }
    return validationResults;
  }
}
