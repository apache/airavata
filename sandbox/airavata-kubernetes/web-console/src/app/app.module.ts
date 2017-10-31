import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppComponent } from './app.component';
import {AppDepListComponent} from "./components/app-dep/list/app.dep.list.component";
import {AppIfaceListComponent} from "./components/app-iface/list/app.iface.list.component";
import {AppModuleListComponent} from "./components/app-module/list/app.module.list.component";
import {ComputeListComponent} from "./components/compute/list/compute.list.component";
import {DashboardComponent} from "./components/dashboard/dashboard.component";
import {ExperimentListComponent} from "./components/experiment/list/experiment.list.component";
import {routing} from "./app.routing";
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {HttpModule} from "@angular/http";
import {FormsModule} from "@angular/forms";
import {ExperimentDetailComponent} from "./components/experiment/detail/experiment.detail";
import {ProcessDetailComponent} from "./components/process/detail/process.detail.component";
import {SetupComponent} from "./components/setup/setup.component";

@NgModule({
  declarations: [
    AppComponent,
    AppDepListComponent,
    AppIfaceListComponent,
    AppModuleListComponent,
    ComputeListComponent,
    DashboardComponent,
    ExperimentListComponent,
    ExperimentDetailComponent,
    ProcessDetailComponent,
    SetupComponent
  ],
  imports: [
    NgbModule.forRoot(),
    BrowserModule,
    routing,
    HttpModule,
    FormsModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
