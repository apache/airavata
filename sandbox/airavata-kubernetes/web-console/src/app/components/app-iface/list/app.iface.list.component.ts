import {Component, ViewEncapsulation} from "@angular/core";
import {ApplicationIfaceService} from "../../../services/application.iface.service";
import {NgbModal, NgbModalRef, ModalDismissReasons} from "@ng-bootstrap/ng-bootstrap";
import {ApplicationIface} from "../../../models/application/application.iface.model";
import {ApplicationModule} from "../../../models/application/application.module.model";
import {AppModuleService} from "../../../services/app.module.service";
import {ApplicationInput} from "../../../models/application/application.ipnput.model";
import {ApplicationOutput} from "../../../models/application/application.output.model";
/**
 * Created by dimuthu on 10/29/17.
 */

@Component({
  templateUrl: './list.html',
  encapsulation: ViewEncapsulation.None,
  providers: [ApplicationIfaceService, AppModuleService]
})
export class AppIfaceListComponent {
  closeResult: string;
  appIfaces: Array<ApplicationIface> = [];
  newAppIface = new ApplicationIface();
  createModalRef: NgbModalRef;
  appModules: Array<ApplicationModule> = [];


  constructor(private modalService: NgbModal, private appIfaceService: ApplicationIfaceService,
              private appModuleService: AppModuleService) {
    this.getAllAppIfaces();
    this.getAllAppModules();
  }

  addNewInput() {
    let input: ApplicationInput = new ApplicationInput();
    this.newAppIface.inputs.push(input);
  }

  addNewOutput() {
    let output: ApplicationOutput = new ApplicationOutput();
    this.newAppIface.outputs.push(output)
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

  addNewAppIface() {
    this.appIfaceService.addApplicationIface(this.newAppIface).subscribe(data => {
        console.log("Created application interface " + data);
        this.createModalRef.close();
        this.getAllAppIfaces();
      },
      err => {
        console.log(err);
        this.createModalRef.close();
      });
  }

  getAllAppIfaces() {
    this.appIfaceService.getAllApplicationIfaces().subscribe(data => {
      this.appIfaces = data;
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
