import BaseEnum from "./BaseEnum";

export default class ProcessState extends BaseEnum {

  get isFinished() {
    const finishedStates = [
      ProcessState.CANCELED,
      ProcessState.COMPLETED,
      ProcessState.FAILED,
    ];
    return finishedStates.indexOf(this) >= 0;
  }
}
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
  "CANCELED",
]);
