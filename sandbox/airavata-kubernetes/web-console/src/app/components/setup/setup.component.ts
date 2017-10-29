import {Component, ViewEncapsulation} from "@angular/core";
import {ApiService} from "../../services/api.service";
import {Router} from "@angular/router";
/**
 * Created by dimuthu on 10/30/17.
 */

@Component({
  templateUrl: './setup.html',
  encapsulation: ViewEncapsulation.None
})
export class SetupComponent {

   apiServerUrl: string = "http://localhost:8080";
   constructor(private apiService: ApiService, private router: Router) {

   }

   setup() {
     this.apiService.baseUrl = this.apiServerUrl;
     this.router.navigateByUrl("/experiments");
   }
}


