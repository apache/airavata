import BaseModel from "./BaseModel";
import Group from "./Group";

const FIELDS = [
  "userModelVersion",
  "airavataInternalUserId",
  "userId",
  "gatewayId",
  "email",
  "firstName",
  "lastName",
  "enabled",
  "emailVerified",
  "airavataUserProfileExists",
  {
    name: "creationTime",
    type: "date",
  },
  {
    name: "groups",
    type: Group,
    list: true,
  },
  "userHasWriteAccess",
  "externalIDPUserInfo",
  "userProfileInvalidFields",
];

export default class IAMUserProfile extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
  }
  get userProfileComplete() {
    return this.userProfileInvalidFields.length === 0;
  }
}
