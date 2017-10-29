
/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
export class TaskStatus {

    id: number;
    state: number;
    stateStr: string;
    timeOfStateChange: number;
    reason: string;
    taskId: number;

    constructor(id: number, state: number, timeOfStateChange: number, reason: string, taskId: number) {
      this.id = id;
      this.state = state;
      this.timeOfStateChange = timeOfStateChange;
      this.reason = reason;
      this.taskId = taskId;
    }
}
