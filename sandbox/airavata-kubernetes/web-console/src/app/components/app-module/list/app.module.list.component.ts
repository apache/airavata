import {Component, ViewEncapsulation} from "@angular/core";
import {NgbModal, ModalDismissReasons, NgbModalRef} from '@ng-bootstrap/ng-bootstrap';
import {ApiService} from "../../../services/api.service";
import {AppModule} from "../../../app.module";
import {AppModuleService} from "../../../services/app.module.service";
import {ApplicationModule} from "../../../models/application/application.module.model";
/**
 * Created by dimuthu on 10/29/17.
 */

@Component({
  templateUrl: './list.html',
  encapsulation: ViewEncapsulation.None,
  providers: [AppModuleService]
})
export class AppModuleListComponent {
  closeResult: string;
  appModules: Array<ApplicationModule> = [];
  newAppModule = new ApplicationModule();
  createModalRef: NgbModalRef;

  constructor(private modalService: NgbModal, private appModuleService: AppModuleService) {
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

  addNewAppModule() {
    this.appModuleService.addAppModules(this.newAppModule).subscribe(data => {
        console.log("Created app module " + data);
        this.createModalRef.close();
        this.getAllAppModules();
      },
      err => {
        console.log(err);
        this.createModalRef.close();
      });
  }

  getAllAppModules() {
    this.appModuleService.getAllAppModules().subscribe(data => {
      this.appModules = data;
    }, err => {
      console.log(err);
    })
  }
}
