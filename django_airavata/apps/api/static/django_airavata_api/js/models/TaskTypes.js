import BaseEnum from "./BaseEnum";

export default class TaskTypes extends BaseEnum {}
TaskTypes.init([
  "ENV_SETUP",
  "DATA_STAGING",
  "JOB_SUBMISSION",
  "ENV_CLEANUP",
  "MONITORING",
  "OUTPUT_FETCHING",
]);
