import {Injectable, Inject} from "@angular/core";
import {ApiService} from "./api.service";
import {Process} from "../models/process/process.model";
/**
 * Created by dimuthu on 10/30/17.
 */
@Injectable()
export class ProcessService {
  constructor(@Inject(ApiService) private apiService:ApiService) {

  }

  getProcessById(id: number) {
    return this.apiService.get("process/" + id).map(res => res.json());
  }

  getAllProcesses() {
    return this.apiService.get("process").map(res => res.json());
  }

  addProcess(process: Process) {
    return this.apiService.post("process", process);
  }

  getAllOutputsForProcess(processId: number) {
    return this.apiService.get("data/process/" + processId).map(res => res.json());
  }
}
