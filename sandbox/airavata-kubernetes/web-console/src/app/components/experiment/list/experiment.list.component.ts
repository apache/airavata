import {Component, ViewEncapsulation} from "@angular/core";
import {NgbModal, ModalDismissReasons, NgbModalRef} from '@ng-bootstrap/ng-bootstrap';
import {ApiService} from "../../../services/api.service";
import {ExperimentService} from "../../../services/experiment.service";
import {Experiment} from "../../../models/experiment/experiment.model";
import {ApplicationIface} from "../../../models/application/application.iface.model";
import {ApplicationIfaceService} from "../../../services/application.iface.service";
import {ExperimentInput} from "../../../models/experiment/experiment.input.model";
import {ExperimentOutput} from "../../../models/experiment/experiment.output.model";
import {ApplicationDeployment} from "../../../models/application/application.deployment.model";
import {AppDeploymentService} from "../../../services/deployment.service";
import {ComputeResource} from "../../../models/compute/compute.resource.model";
import {ComputeService} from "../../../services/compute.service";
import {Router} from "@angular/router";

/**
 * Created by dimuthu on 10/29/17.
 */

@Component({
  templateUrl: './list.html',
  encapsulation: ViewEncapsulation.None,
  providers: [ExperimentService, ApplicationIfaceService, AppDeploymentService, ComputeService]
})
export class ExperimentListComponent {

  closeResult: string;
  experiments: Array<Experiment> = [];
  newExperiment = new Experiment();
  createModalRef: NgbModalRef;
  appIfaces: Array<ApplicationIface> = [];
  selectedAppDeployments: Array<ApplicationDeployment> = [];
  allAppDeployments: Array<ApplicationDeployment> = [];
  computeResources: Array<ComputeResource> = [];

  constructor(private modalService: NgbModal, private experimentSerive: ExperimentService,
              private appIfaceService: ApplicationIfaceService, private depService: AppDeploymentService,
              private computeService: ComputeService, private router: Router) {
    this.getAllExperiments();
    this.getAllAppIfaces();
    this.getAllDeployments();
    this.getAllComputes();
  }

  routeToDetailPage(id: number) {
    this.router.navigateByUrl("/experiment/detail/"+id);
  }

  openAsModel(content) {
    this.createModalRef = this.modalService.open(content, {size: "lg"});
    this.createModalRef.result.then((result) => {
      this.closeResult = `Closed with: ${result}`;
    }, (reason) => {
      this.closeResult = `Dismissed ${this.getDismissReason(reason)}`;
    });
  }

  private getDismissReason(reason: any): string {
    if (reason === ModalDismissReasons.ESC) {
      return 'by pressing ESC';
    } else if (reason === ModalDismissReasons.BACKDROP_CLICK) {
      return 'by clicking on a backdrop';
    } else {
      return  `with: ${reason}`;
    }
  }

  addNewExperiment() {
    this.experimentSerive.addExperiment(this.newExperiment).subscribe(data => {
        console.log("Created experiment " + data);
        this.createModalRef.close();
        this.getAllExperiments();
      },
      err => {
        console.log(err);
        this.createModalRef.close();
      });
  }

  getAllExperiments() {
    this.experimentSerive.getAllExperiments().subscribe(data => {
      this.experiments = data;
    }, err => {
      console.log(err);
    })
  }

  getAllAppIfaces() {
    this.appIfaceService.getAllApplicationIfaces().subscribe(data => {
      this.appIfaces = data;
    }, err => {
      console.log(err);
    })
  }

  getAllDeployments() {
    this.depService.getAllAppDeployments().subscribe(data => {
      this.allAppDeployments = data;
    }, err => {
      console.log(err);
    })
  }

  getAllComputes() {
    this.computeService.getAllComputeResources().subscribe(data => {
      this.computeResources = data;
    }, err => {
      console.log(err);
    })
  }

  getComputeById(id: number): ComputeResource {
    return this.computeResources.filter(compute => compute.id == id)[0];
  }

  ifaceOnChange(ifaceId: number) {

    console.log("Selected deployments before");
    console.log(this.selectedAppDeployments);

    let selectedIface: ApplicationIface = this.appIfaces.filter((iface) => iface.id == ifaceId)[0];

    this.newExperiment.experimentInputs = [];
    selectedIface.inputs.forEach(input => {
      let expInput: ExperimentInput = new ExperimentInput(0, input.name, input.type, input.value, input.arguments);
      this.newExperiment.experimentInputs.push(expInput);
    });

    this.newExperiment.experimentOutputs = [];

    selectedIface.outputs.forEach(output => {
      let expOutput: ExperimentOutput = new ExperimentOutput(0, output.name, output.value, output.type);
      this.newExperiment.experimentOutputs.push(expOutput);
    });

    this.selectedAppDeployments = [];
    this.selectedAppDeployments = this.allAppDeployments.filter(appDep => appDep.applicationModuleId == selectedIface.applicationModuleId);
    console.log("Selected deployments");
    console.log(this.selectedAppDeployments);
  }
}
