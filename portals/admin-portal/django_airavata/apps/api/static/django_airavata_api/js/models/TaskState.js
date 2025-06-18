import BaseEnum from "./BaseEnum";

export default class TaskState extends BaseEnum {}
TaskState.init(["CREATED", "EXECUTING", "COMPLETED", "FAILED", "CANCELED"]);
