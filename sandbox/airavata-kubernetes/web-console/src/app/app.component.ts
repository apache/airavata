import { Component } from '@angular/core';
import {ApiService} from "./services/api.service";
import {Http, ConnectionBackend} from "@angular/http";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  template: `<router-outlet></router-outlet>`,
  providers: [ApiService]
})
export class AppComponent {
  title = 'app';
}
