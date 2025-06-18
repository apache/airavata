import BaseModel from "./BaseModel";
import ComputeResourceReservation from "./ComputeResourceReservation";

const FIELDS = [
  "computeResourceId",
  "groupResourceProfileId",
  {
    name: "overridebyAiravata",
    type: "boolean",
    default: true,
  },
  "loginUserName",
  "preferredJobSubmissionProtocol",
  "preferredDataMovementProtocol",
  "preferredBatchQueue",
  "scratchLocation",
  "allocationProjectNumber",
  "resourceSpecificCredentialStoreToken",
  "usageReportingGatewayId",
  "qualityOfService",
  "sshAccountProvisioner",
  "groupSSHAccountProvisionerConfigs",
  "sshAccountProvisionerAdditionalInfo",
  {
    name: "reservations",
    type: ComputeResourceReservation,
    list: true,
    default: BaseModel.defaultNewInstance(Array),
  },
  
];

export default class GroupComputeResourcePreference extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
  }

  validate() {
    let validationResults = {};
    if (this.isEmpty(this.loginUserName)) {
      validationResults["loginUserName"] = "Please provide a login username.";
    }
    if (this.isEmpty(this.scratchLocation)) {
      validationResults["scratchLocation"] =
        "Please provide a scratch location.";
    }
    return validationResults;
  }
}
