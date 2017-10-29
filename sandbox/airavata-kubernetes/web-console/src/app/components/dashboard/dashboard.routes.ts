import {DashboardComponent} from "./dashboard.component";
import {ComputeListComponent} from "../compute/list/compute.list.component";
import {ExperimentListComponent} from "../experiment/list/experiment.list.component";
import {AppIfaceListComponent} from "../app-iface/list/app.iface.list.component";
import {AppDepListComponent} from "../app-dep/list/app.dep.list.component";
import {AppModuleListComponent} from "../app-module/list/app.module.list.component";
import {Routes} from "@angular/router";
import {ExperimentDetailComponent} from "../experiment/detail/experiment.detail";
import {ProcessDetailComponent} from "../process/detail/process.detail.component";
import {SetupComponent} from "../setup/setup.component";

/**
 * Created by dimuthu on 10/29/17.
 */

export const DASHBOARD_ROUTES: Routes = [

  {
    path: 'setup',
    component: SetupComponent,
  },
  {
    path: 'compute',
    component: ComputeListComponent,
  },
  {
    path: 'process/detail/:id',
    component: ProcessDetailComponent,
  },
  {
    path: 'experiment/detail/:id',
    component: ExperimentDetailComponent,
  },
  {
    path: 'experiment',
    component: ExperimentListComponent,
  },
  {
    path: 'iface',
    component: AppIfaceListComponent,
  },
  {
    path: 'dep',
    component: AppDepListComponent,
  },
  {
    path: 'module',
    component: AppModuleListComponent,
  }
];
