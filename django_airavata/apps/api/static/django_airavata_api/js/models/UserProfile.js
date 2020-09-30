import BaseModel from "./BaseModel";
import UserStatus from "./UserStatus";

const FIELDS = [
  "userModelVersion",
  "airavataInternalUserId",
  "userId",
  "gatewayId",
  "emails",
  "firstName",
  "lastName",
  "middleName",
  "namePrefix",
  "nameSuffix",
  "orcidId",
  "phones",
  "country",
  "nationality",
  "homeOrganization",
  "orginationAffiliation",
  {
    name: "creationTime",
    type: "date",
  },
  {
    name: "lastAccessTime",
    type: "date",
  },
  "validUntil",
  {
    name: "State",
    type: UserStatus,
  },
  "comments",
  "labeledURI",
  "gpgKey",
  "timeZone",
  "nsfDemographics",
  "customDashboard",
];

export default class UserProfile extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
  }

  get email() {
    return this.emails != null && this.emails.length > 0
      ? this.emails[0]
      : null;
  }
}
