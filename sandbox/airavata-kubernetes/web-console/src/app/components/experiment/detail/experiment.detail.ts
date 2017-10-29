import {ViewEncapsulation, Component} from "@angular/core";
import {Experiment} from "../../../models/experiment/experiment.model";
import {ActivatedRoute, Router} from "@angular/router";
import {ExperimentService} from "../../../services/experiment.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {Process} from "../../../models/process/process.model";
import {ProcessService} from "../../../services/process.service";
import {ProcessStatus} from "../../../models/process/process.status.model";

/**
 * Created by dimuthu on 10/29/17.
 */

@Component({
  templateUrl: './detail.html',
  encapsulation: ViewEncapsulation.None,
  providers: [ExperimentService, ProcessService]
})
export class ExperimentDetailComponent {

  selectedExperiment: Experiment = new Experiment();
  processes: Array<Process> = [];
  processLastState: ProcessStatus = new ProcessStatus();

  constructor(private modalService: NgbModal,private activatedRoute: ActivatedRoute,
              private experimentService: ExperimentService, private processService: ProcessService,
              private router: Router) {

    let expId = this.activatedRoute.snapshot.params["id"];
    this.experimentService.getExperimentById(expId)
      .subscribe(data => {this.selectedExperiment = data}, err => {console.log(err)});
  }

  launchExperiment() {
    this.experimentService.launchExperiment(this.selectedExperiment.id).subscribe(data => {
      alert("Experiment successfully launched");
    },
      err => {
        console.log(err);
        alert("Experiment launch failed");
      }
    )
  }

  routeToProcessPage(id: number) {
    this.router.navigateByUrl("/process/detail/" + id);
  }

  openAsModel(content) {
    this.modalService.open(content, {size: "lg"}).result.then((result) => {}, (reason) => {});
  }

  openProcessesAsModel(content) {
    this.processes = [];
    this.selectedExperiment.processIds.forEach(id => {
      this.processService.getProcessById(id).subscribe(data => {
        this.processes.push(data);
      }, err => {
        console.log(err);
      });
    });
    this.modalService.open(content, {size: "lg"}).result.then((result) => {}, (reason) => {});
  }
}
