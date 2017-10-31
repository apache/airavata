import {Injectable, Inject} from "@angular/core";
import {ApiService} from "./api.service";
import {ComputeResource} from "../models/compute/compute.resource.model";
import 'rxjs/add/operator/map';
import {Experiment} from "../models/experiment/experiment.model";
/**
 * Created by dimuthu on 10/29/17.
 */

@Injectable()
export class ExperimentService {
  constructor(@Inject(ApiService) private apiService:ApiService) {

  }

  launchExperiment(id: number) {
    return this.apiService.get("experiment/" + id + "/launch");
  }

  getExperimentById(id: number) {
    return this.apiService.get("experiment/" + id).map(res => res.json());
  }

  getAllExperiments() {
    return this.apiService.get("experiment").map(res => res.json());
  }

  addExperiment(experiment: Experiment) {
    return this.apiService.post("experiment", experiment);
  }
}
