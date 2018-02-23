import BaseModel from './BaseModel'


const FIELDS = [
     'computeResourceId',
     'groupResourceProfileId',
     'overridebyAiravata',
     'loginUserName',
     'preferredJobSubmissionProtocol',
     'preferredDataMovementProtocol',
     'preferredBatchQueue',
     'scratchLocation',
     'allocationProjectNumber',
     'resourceSpecificCredentialStoreToken',
     'usageReportingGatewayId',
     'qualityOfService',
     'reservation',
     'reservationStartTime',
     'reservationEndTime',
     'sshAccountProvisioner',
     'groupSSHAccountProvisionerConfigs',
     'sshAccountProvisionerAdditionalInfo',
];

export default class GroupComputeResourcePreference extends BaseModel {

    constructor(data = {}) {
        super(FIELDS, data);
    }
}
