import {RouterModule, Routes} from "@angular/router";
import {AppComponent} from "./app.component";
import {DashboardComponent} from "./components/dashboard/dashboard.component";
import {DASHBOARD_ROUTES} from "./components/dashboard/dashboard.routes";

const APP_ROUTES: Routes = [
    { path: '', component: DashboardComponent, children: DASHBOARD_ROUTES },
    { path: '', component: DashboardComponent },
    { path: '**', redirectTo: ''}
];

export const routing = RouterModule.forRoot(APP_ROUTES);


