import {ExperimentInput} from "./experiment.input.model";
import {ExperimentOutput} from "./experiment.output.model";
/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
export class Experiment {

    id: number;
    experimentName: string;
    creationTime: number;
    description: string;
    applicationInterfaceId: number;
    applicationDeploymentId: number;

    experimentInputs: Array<ExperimentInput> = [];
    experimentOutputs: Array<ExperimentOutput> = [];
    experimentStatusIds: Array<number> = [];
    errorsIds: Array<number> = [];
    processIds: Array<number> = [];

    constructor(id: number = 0, experimentName: string = null, creationTime: number = null,
                description: string = null, applicationInterfaceId: number = 0, applicationDeploymentId: number = 0,
                experimentInputs: Array<ExperimentInput> = [], experimentOutputs: Array<ExperimentOutput> = [],
                experimentStatusIds: Array<number> = [], errorsIds: Array<number> = [], processIds: Array<number> = []) {

      this.id = id;
      this.experimentName = experimentName;
      this.creationTime = creationTime;
      this.description = description;
      this.applicationInterfaceId = applicationInterfaceId;
      this.applicationDeploymentId = applicationDeploymentId;
      this.experimentInputs = experimentInputs;
      this.experimentOutputs = experimentOutputs;
      this.experimentStatusIds = experimentStatusIds;
      this.errorsIds = errorsIds;
      this.processIds = processIds;
    }
}
