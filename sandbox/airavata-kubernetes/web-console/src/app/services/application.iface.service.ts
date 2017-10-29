import {Injectable, Inject} from "@angular/core";
import {ApiService} from "./api.service";
import {ComputeResource} from "../models/compute/compute.resource.model";
import 'rxjs/add/operator/map';
import {ApplicationIface} from "../models/application/application.iface.model";
/**
 * Created by dimuthu on 10/29/17.
 */

@Injectable()
export class ApplicationIfaceService {
  constructor(@Inject(ApiService) private apiService:ApiService) {

  }

  getAllApplicationIfaces() {
    return this.apiService.get("appiface").map(res => res.json());
  }

  addApplicationIface(appIface: ApplicationIface) {
    return this.apiService.post("appiface", appIface);
  }
}
