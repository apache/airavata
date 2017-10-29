import {Component, ViewEncapsulation} from "@angular/core";
import {ProcessService} from "../../../services/process.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {ActivatedRoute} from "@angular/router";
import {TaskService} from "../../../services/task.service";
import {Process} from "../../../models/process/process.model";
import {Task} from "../../../models/task/task.model";
import {TaskStatus} from "../../../models/task/task.status.model";

@Component({
  templateUrl: './detail.html',
  encapsulation: ViewEncapsulation.None,
  providers: [ProcessService, TaskService]
})
export class ProcessDetailComponent {

  selectedProcess: Process = new Process();
  taskDagFroProcess: Array<Task> = [];
  taskEvents: Array<TaskStatus> = [];

  constructor(private modalService: NgbModal, private activatedRoute: ActivatedRoute,
              private processService: ProcessService, private taskService: TaskService){

    let processId = this.activatedRoute.snapshot.params["id"];
    processService.getProcessById(processId).subscribe(data => {
      this.selectedProcess = data;
      this.taskDagFroProcess = this.selectedProcess.tasks;
    }, err => console.log(err));
  }

  openAsModel(content) {
    this.modalService.open(content, {size: "lg"}).result.then((result) => {}, (reason) => {});
  }
}
