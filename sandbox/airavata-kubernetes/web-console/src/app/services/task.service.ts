import {Injectable, Inject} from "@angular/core";
import {ApiService} from "./api.service";
import {} from "../models/process/process.model";
import {Task} from "../models/task/task.model";
/**
 * Created by dimuthu on 10/30/17.
 */
@Injectable()
export class TaskService {
  constructor(@Inject(ApiService) private apiService:ApiService) {

  }

  getTaskById(id: number) {
    return this.apiService.get("task/" + id).map(res => res.json());
  }

  getAllTasks() {
    return this.apiService.get("task").map(res => res.json());
  }

  addTask(task: Task) {
    return this.apiService.post("task", task);
  }
}
