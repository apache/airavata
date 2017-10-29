import {Injectable, Inject} from "@angular/core";
import {ApiService} from "./api.service";
import {ComputeResource} from "../models/compute/compute.resource.model";
import 'rxjs/add/operator/map';
/**
 * Created by dimuthu on 10/29/17.
 */

@Injectable()
export class ComputeService {
  constructor(@Inject(ApiService) private apiService:ApiService) {

  }

  getAllComputeResources() {
    return this.apiService.get("compute").map(res => res.json());
  }

  addComputeResource(computeResource: ComputeResource) {
    return this.apiService.post("compute", computeResource);
  }
}
