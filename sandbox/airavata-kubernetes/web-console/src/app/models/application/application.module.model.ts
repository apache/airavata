/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
export class ApplicationModule {

    id: number;
    name: string;
    version: string;
    description: string;


    constructor(id: number = 0, name: string = null, version: string = null, description: string = null) {
      this.id = id;
      this.name = name;
      this.version = version;
      this.description = description;
    }
}
