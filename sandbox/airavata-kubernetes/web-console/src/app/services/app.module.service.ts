import {Injectable, Inject} from "@angular/core";
import {ApiService} from "./api.service";
import {ComputeResource} from "../models/compute/compute.resource.model";
import 'rxjs/add/operator/map';
import {ApplicationModule} from "../models/application/application.module.model";
/**
 * Created by dimuthu on 10/29/17.
 */

@Injectable()
export class AppModuleService {
  constructor(@Inject(ApiService) private apiService:ApiService) {

  }

  getAllAppModules() {
    return this.apiService.get("appmodule").map(res => res.json());
  }

  addAppModules(appModule: ApplicationModule) {
    return this.apiService.post("appmodule", appModule);
  }
}
