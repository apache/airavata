import {ApplicationInput} from "./application.ipnput.model";
import {ApplicationOutput} from "./application.output.model";
/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
export class ApplicationIface {

    id:number;
    name:string;
    description:string;
    applicationModuleId:number;
    inputs:Array<ApplicationInput> = [];
    outputs:Array<ApplicationOutput> = [];


    constructor(id: number, name: string, description: string, applicationModuleId: number,
                inputs: Array<ApplicationInput>, outputs: Array<ApplicationOutput>) {
      this.id = id;
      this.name = name;
      this.description = description;
      this.applicationModuleId = applicationModuleId;
      this.inputs = inputs;
      this.outputs = outputs;
    }
}
