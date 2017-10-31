import {Component, ViewEncapsulation} from "@angular/core";
import {NgbModal, ModalDismissReasons, NgbModalRef} from '@ng-bootstrap/ng-bootstrap';
import {ComputeService} from "../../../services/compute.service";
import {ComputeResource} from "../../../models/compute/compute.resource.model";
import {ApiService} from "../../../services/api.service";

/**
 * Created by dimuthu on 10/29/17.
 */

@Component({
  templateUrl: './list.html',
  encapsulation: ViewEncapsulation.None,
  providers: [ComputeService]
})
export class ComputeListComponent {

  closeResult: string;
  computeResources: Array<ComputeResource> = [];
  newComputeResource = new ComputeResource();
  createModalRef: NgbModalRef;


  constructor(private modalService: NgbModal, private computeService: ComputeService) {
    this.getAllComputes();
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

  addNewCompute() {
    this.computeService.addComputeResource(this.newComputeResource).subscribe(data => {
      console.log("Created compute resource " + data);
      this.createModalRef.close();
      this.getAllComputes();
    },
    err => {
      console.log(err);
      this.createModalRef.close();
    });
  }

  getAllComputes() {
    this.computeService.getAllComputeResources().subscribe(data => {
      this.computeResources = data;
    }, err => {
      console.log(err);
    })
  }
}
