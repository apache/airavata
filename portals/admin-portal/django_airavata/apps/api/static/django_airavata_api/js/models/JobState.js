import BaseEnum from "./BaseEnum";

export default class JobState extends BaseEnum {}
JobState.init([
  "SUBMITTED",
  "QUEUED",
  "ACTIVE",
  "COMPLETE",
  "CANCELED",
  "FAILED",
  "SUSPENDED",
  "UNKNOWN",
  "NON_CRITICAL_FAIL",
]);
