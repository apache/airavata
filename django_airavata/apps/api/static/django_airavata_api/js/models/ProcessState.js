import BaseEnum from "./BaseEnum";

export default class ProcessState extends BaseEnum {}
ProcessState.init([
  "CREATED",
  "VALIDATED",
  "STARTED",
  "PRE_PROCESSING",
  "CONFIGURING_WORKSPACE",
  "INPUT_DATA_STAGING",
  "EXECUTING",
  "MONITORING",
  "OUTPUT_DATA_STAGING",
  "POST_PROCESSING",
  "COMPLETED",
  "FAILED",
  "CANCELLING",
  "CANCELED"
]);
