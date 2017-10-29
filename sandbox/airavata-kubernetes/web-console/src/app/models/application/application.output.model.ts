
/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
export class ApplicationOutput {

    id: number;
    name: string;
    type: number;
    value: string;

    constructor(id: number = 0, name: string = null, type: number = 0, value: string = null) {
      this.id = id;
      this.name = name;
      this.type = type;
      this.value = value;
    }
}
