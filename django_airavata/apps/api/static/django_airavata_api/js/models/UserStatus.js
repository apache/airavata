import BaseEnum from "./BaseEnum";

export default class UserStatus extends BaseEnum {}
UserStatus.init([
  "ACTIVE",
  "CONFIRMED",
  "APPROVED",
  "DELETED",
  "DUPLICATE",
  "GRACE_PERIOD",
  "INVITED",
  "DENIED",
  "PENDING",
  "PENDING_APPROVAL",
  "PENDING_CONFIRMATION",
  "SUSPENDED",
  "DECLINED",
  "EXPIRED",
]);
