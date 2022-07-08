import ApplicationDeploymentEditor from "./components/applications/ApplicationDeploymentEditor.vue";
import ApplicationDeploymentsList from "./components/applications/ApplicationDeploymentsList.vue";
import ApplicationEditorContainer from "./components/applications/ApplicationEditorContainer.vue";
import ApplicationInterfaceEditor from "./components/applications/ApplicationInterfaceEditor.vue";
import ApplicationModuleEditor from "./components/applications/ApplicationModuleEditor.vue";
import ApplicationsDashboard from "./components/dashboards/ApplicationsDashboard.vue";
import ComputePreference from "./components/admin/group_resource_preferences/ComputePreference";
import ComputeResourcePreferenceDashboard from "./components/dashboards/ComputeResourcePreferenceDashboard";
import CredentialStoreDashboard from "./components/dashboards/CredentialStoreDashboard";
import DevelopersContainer from "./components/developers//DevelopersContainer.vue";
import ExperimentStatisticsContainer from "./components/statistics/ExperimentStatisticsContainer";
import ExtendedUserProfileContainer from "./components/users/ExtendedUserProfileContainer";
import GatewayResourceProfileEditorContainer from "./components/gatewayprofile/GatewayResourceProfileEditorContainer.vue";
import GroupComputeResourcePreference from "./components/admin/group_resource_preferences/GroupComputeResourcePreference";
import IdentityServiceUserManagementContainer from "./components/users/IdentityServiceUserManagementContainer.vue";
import UnverifiedEmailUserManagementContainer from "./components/users/UnverifiedEmailUserManagementContainer.vue";
import UserManagementContainer from "./components/users/UserManagementContainer.vue";
import NoticesManagementContainer from "./components/notices/NoticesManagementContainer.vue";
import VueRouter from "vue-router";

const routes = [
  {
    path: "/applications/new",
    component: ApplicationEditorContainer,
    name: "new_application",
    children: [
      // Only the module route for a new application, save it and then replace
      // the URL with the module id
      {
        path: "",
        components: {
          module: ApplicationModuleEditor,
        },
        name: "new_application_module",
      },
    ],
  },
  {
    path: "/applications/:id",
    component: ApplicationEditorContainer,
    name: "application",
    props: true,
    children: [
      {
        path: "",
        components: {
          module: ApplicationModuleEditor,
        },
        name: "application_module",
      },
      {
        path: "interface",
        components: {
          interface: ApplicationInterfaceEditor,
        },
        name: "application_interface",
      },
      {
        path: "deployments",
        components: {
          deployments: ApplicationDeploymentsList,
        },
        name: "application_deployments",
        props: {
          deployments: true,
        },
      },
      {
        path: "deployments/new/:hostId",
        components: {
          deployment: ApplicationDeploymentEditor,
        },
        name: "new_application_deployment",
      },
      {
        path: "deployments/:deploymentId",
        components: {
          deployment: ApplicationDeploymentEditor,
        },
        name: "application_deployment",
        props: {
          deployment: true,
        },
      },
    ],
  },
  { path: "/applications", component: ApplicationsDashboard },
  {
    path: "/group-resource-profiles/new",
    component: GroupComputeResourcePreference,
    name: "new_group_resource_preference",
    props: true,
  },
  {
    path: "/group-resource-profiles/:id",
    component: GroupComputeResourcePreference,
    name: "group_resource_preference",
    props: true,
  },
  {
    path: "/group-resource-profiles/new/compute-preferences/:host_id",
    component: ComputePreference,
    name: "compute_preference_for_new_group_resource_profile",
    props: true,
  },
  {
    path: "/group-resource-profiles/:id/compute-preferences/:host_id",
    component: ComputePreference,
    name: "compute_preference",
    props: true,
  },
  {
    path: "/group-resource-profiles",
    component: ComputeResourcePreferenceDashboard,
    name: "group_resource_preference_dashboard",
  },
  {
    path: "/credentials",
    component: CredentialStoreDashboard,
    name: "credential_store",
  },
  {
    path: "/gateway-resource-profile",
    component: GatewayResourceProfileEditorContainer,
    name: "gateway-resource-profile",
  },
  {
    path: "/users",
    component: UserManagementContainer,
    name: "users",
    children: [
      {
        path: "",
        component: IdentityServiceUserManagementContainer,
        name: "identity-service-users",
      },
      {
        path: "unverified-email",
        component: UnverifiedEmailUserManagementContainer,
        name: "unverified-email-users",
      },
    ],
  },
  {
    path: "/extended-user-profile",
    component: ExtendedUserProfileContainer,
    name: "extended-user-profile",
  },
  {
    path: "/notices",
    component: NoticesManagementContainer,
    name: "notices",
  },
  {
    path: "/experiment-statistics",
    component: ExperimentStatisticsContainer,
    name: "experiment-statistics",
  },
  {
    path: "/developers",
    component: DevelopersContainer,
    name: "developers",
  }
];
export default new VueRouter({
  mode: "history",
  base: "/admin/",
  routes: routes,
});
