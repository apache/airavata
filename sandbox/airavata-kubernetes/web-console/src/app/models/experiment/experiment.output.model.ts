/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
export class ExperimentOutput {

    id: number;
    name: string;
    value: string;
    type: number;

    constructor(id: number, name: string, value: string, type: number) {
      this.id = id;
      this.name = name;
      this.value = value;
      this.type = type;
    }
}
