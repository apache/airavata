/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
export class ExperimentInput {

    id: number;
    name: string;
    type: number;
    value: string;
    arguments: string;

    constructor(id: number, name: string, type: number, value: string, args: string) {
      this.id = id;
      this.name = name;
      this.type = type;
      this.value = value;
      this.arguments = args;
    }
}
