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
    name: "groups",
    type: Group,
    list: true
  }
];

export default class ManagedUserProfile extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
  }
}
