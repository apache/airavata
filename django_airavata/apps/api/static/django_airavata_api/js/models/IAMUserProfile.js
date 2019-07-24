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
    type: "date"
  },
  {
    name: "groups",
    type: Group,
    list: true
  },
  "userHasWriteAccess"
];

export default class IAMUserProfile extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
  }
}
