import {Component, ViewEncapsulation} from "@angular/core";
import {ApplicationDeployment} from "../../../models/application/application.deployment.model";
import {NgbModalRef, NgbModal, ModalDismissReasons} from "@ng-bootstrap/ng-bootstrap";
import {AppDeploymentService} from "../../../services/deployment.service";
import {ComputeService} from "../../../services/compute.service";
import {ComputeResource} from "../../../models/compute/compute.resource.model";
import {AppModuleService} from "../../../services/app.module.service";
import {ApplicationModule} from "../../../models/application/application.module.model";
/**
 * Created by dimuthu on 10/29/17.
 */

@Component({
  templateUrl: './list.html',
  encapsulation: ViewEncapsulation.None,
  providers: [AppDeploymentService, ComputeService, AppModuleService]
})
export class AppDepListComponent {

  closeResult: string;
  deployments: Array<ApplicationDeployment> = [];
  newAppDep = new ApplicationDeployment();
  createModalRef: NgbModalRef;
  computeResources: Array<ComputeResource> = [];
  appModules: Array<ApplicationModule> = [];

  constructor(private modalService: NgbModal, private depService: AppDeploymentService,
              private computeService: ComputeService, private appModuleService: AppModuleService) {
    this.getAllDeployments();
    this.getAllComputes();
    this.getAllAppModules();
  }

  openAsModel(content) {
    this.createModalRef = this.modalService.open(content);
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

  addNewAppDeployment() {
    this.depService.addAppDeployment(this.newAppDep).subscribe(data => {
        console.log("Created app deployment" + data);
        this.createModalRef.close();
        this.getAllDeployments();
      },
      err => {
        console.log(err);
        this.createModalRef.close();
      });
  }

  getAllDeployments() {
    this.depService.getAllAppDeployments().subscribe(data => {
      this.deployments = data;
    }, err => {
      console.log(err);
    })
  }

  getAllComputes() {
    this.computeService.getAllComputeResources().subscribe(data => {
      console.log("All compute data2");
      this.computeResources = data;
      console.log(this.computeResources);
    }, err => {
      console.log(err);
    })
  }

  getAllAppModules() {
    this.appModuleService.getAllAppModules().subscribe(data => {
      this.appModules = data;
    }, err => {
      console.log(err);
    })
  }
}
