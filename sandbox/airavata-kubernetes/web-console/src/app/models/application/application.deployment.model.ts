/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
export class ApplicationDeployment {

    id: number;
    name: string;
    applicationModuleId: number;
    computeResourceId: number;
    executablePath: string;
    preJobCommand: string;
    postJobCommand: string;

    constructor(id: number = 0, name: string = null, applicationModuleId: number = 0, computeResourceId: number = 0,
                executablePath: string = null, preJobCommand: string = null, postJobCommand: string = null) {
      this.name = name;
      this.id = id;
      this.applicationModuleId = applicationModuleId;
      this.computeResourceId = computeResourceId;
      this.executablePath = executablePath;
      this.preJobCommand = preJobCommand;
      this.postJobCommand = postJobCommand;
    }
}
