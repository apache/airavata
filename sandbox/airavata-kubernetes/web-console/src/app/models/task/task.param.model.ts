/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
export class TaskParam {

    id: number;
    key: string;
    value: string;

    constructor(id: number, key: string, value: string) {
      this.id = id;
      this.key = key;
      this.value = value;
    }
}
