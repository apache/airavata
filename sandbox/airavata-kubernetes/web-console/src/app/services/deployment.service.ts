import {Injectable, Inject} from "@angular/core";
import {ApiService} from "./api.service";
import {ApplicationDeployment} from "../models/application/application.deployment.model";
/**
 * Created by dimuthu on 10/29/17.
 */

@Injectable()
export class AppDeploymentService {
  constructor(@Inject(ApiService) private apiService:ApiService) {

  }

  getAllAppDeployments() {
    return this.apiService.get("appdep").map(res => res.json());
  }

  addAppDeployment(appDep: ApplicationDeployment) {
    return this.apiService.post("appdep", appDep);
  }
}
