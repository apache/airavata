/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
export class ProcessStatus {

    id: number;
    state: number;
    timeOfStateChange: number;
    reason: string;
    processId: number;


    constructor(id: number, state: number, timeOfStateChange: number, reason: string, processId: number) {
      this.id = id;
      this.state = state;
      this.timeOfStateChange = timeOfStateChange;
      this.reason = reason;
      this.processId = processId;
    }
}
