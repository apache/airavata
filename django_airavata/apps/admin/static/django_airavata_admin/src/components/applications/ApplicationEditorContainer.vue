<template>
  <div>
    <div class="row">
      <div class="col">
        <h1 class="h4 mb-4">
          {{ title }}
        </h1>
      </div>
    </div>
    <div class="row">
      <div class="col">
        <b-nav tabs class="mb-3">
          <b-nav-item exact-active-class="active" exact :to="{name: id ? 'application_module' : 'new_application_module', params: {id: id}}">Details</b-nav-item>
          <b-nav-item exact-active-class="active" exact :to="{name: 'application_interface', params: {id: id}}" :disabled="!id">Interface</b-nav-item>
          <b-nav-item active-class="active" :to="{name: 'application_deployments', params: {id: id}}" :disabled="!id">Deployments</b-nav-item>
        </b-nav>
        <router-view name="module" v-if="module" v-model="module" @save="saveModule" @cancel="cancelModule" :readonly="!module.userHasWriteAccess"
        />
        <router-view name="interface" v-if="appInterface" v-model="appInterface" @save="saveInterface" @cancel="cancelInterface"
          :readonly="!appInterface.userHasWriteAccess" />
        <router-view name="deployments" v-if="deployments" :deployments="deployments" @new="createNewDeployment" @delete="deleteDeployment"
        />
        <router-view name="deployment" v-if="deployment" v-model="deployment" @save="saveDeployment" @cancel="cancelDeployment" />
      </div>
    </div>
  </div>
</template>

<script>
import { mapActions, mapState } from "vuex";
import { models } from "django-airavata-api";
import { notifications } from "django-airavata-common-ui";

export default {
  name: "application-editor-container",
  props: {
    id: String,
    deployment_id: String,
    hostId: String
  },
  data: function() {
    return {
      module: null,
      appInterface: null,
      deployment: null
    };
  },
  computed: {
    title: function() {
      if (this.id) {
        return this.module && this.module.appModuleName
          ? this.module.appModuleName
          : "";
      } else {
        return "Create a New Application";
      }
    },
    ...mapState("applications/modules", ["currentModule"]),
    ...mapState("applications/interfaces", ["currentInterface"]),
    ...mapState("applications/deployments", [
      "currentDeployment",
      "deployments"
    ])
  },
  created() {
    this.initialize();
    if (this.deployment_id) {
      this.loadApplicationDeployment(this.deployment_id);
    } else if (this.hostId) {
      this.createNewDeployment(this.hostId);
    }
  },
  methods: {
    ...mapActions("applications/modules", [
      "loadApplicationModule",
      "createApplicationModule",
      "updateApplicationModule"
    ]),
    ...mapActions("applications/interfaces", [
      "loadApplicationInterface",
      "createApplicationInterface",
      "updateApplicationInterface"
    ]),
    ...mapActions("applications/deployments", [
      "loadApplicationDeployments",
      "loadApplicationDeployment",
      "createApplicationDeployment",
      "updateApplicationDeployment",
      "deleteApplicationDeployment"
    ]),
    initialize() {
      // TODO: move this to applications store?
      if (this.currentModule && this.currentModule.appModuleId === this.id) {
        this.module = this.currentModule.clone();
        this.loadApplicationInterface(this.id).catch(error => {
          notifications.NotificationList.addError(error);
        });
        this.loadApplicationDeployments(this.id);
      } else if (this.id) {
        this.loadApplicationModule(this.id);
        this.loadApplicationInterface(this.id).catch(error => {
          notifications.NotificationList.addError(error);
        });
        this.loadApplicationDeployments(this.id);
      } else {
        this.module = new models.ApplicationModule();
      }
    },
    saveModule() {
      if (this.id) {
        this.updateApplicationModule(this.module).then(() => {
          this.$router.push({ path: "/applications" });
        });
      } else {
        this.createApplicationModule(this.module).then(appModule => {
          this.$router.push({
            name: "application_module",
            params: { id: appModule.appModuleId }
          });
        });
      }
    },
    saveInterface() {
      // Copy name and description from module
      this.appInterface.applicationName = this.module.appModuleName;
      this.appInterface.applicationDescription = this.module.appModuleDescription;

      this.updateApplicationModule(this.module)
        .then(appModule => {
          if (this.appInterface.applicationInterfaceId) {
            return this.updateApplicationInterface(this.appInterface).then(
              () => {
                this.$router.push({ path: "/applications" });
              }
            );
          } else {
            this.appInterface.applicationModules = [this.id];
            return this.createApplicationInterface(this.appInterface).then(
              () => {
                this.$router.push({ path: "/applications" });
              }
            );
          }
        })
        .catch(error => notifications.NotificationList.addError(error));
    },
    createNewDeployment(computeHostId) {
      const deployment = new models.ApplicationDeploymentDescription();
      deployment.appModuleId = this.id;
      deployment.computeHostId = computeHostId;
      this.deployment = deployment;
      this.$router.push({
        name: "new_application_deployment",
        params: { id: this.id, hostId: computeHostId }
      });
    },
    saveDeployment() {
      return this.saveAll().then(appDeployment => {
        this.$router.push({
          name: "application_deployments",
          params: { id: this.id }
        });
      });
    },
    saveAll() {
      const moduleSave = this.id
        ? this.updateApplicationModule(this.module)
        : this.createApplicationModule(this.module);
      return moduleSave
        .then(appModule => {
          this.appInterface.applicationName = appModule.appModuleName;
          this.appInterface.applicationDescription =
            appModule.appModuleDescription;

          if (this.appInterface.applicationInterfaceId) {
            return this.updateApplicationInterface(this.appInterface);
          } else {
            this.appInterface.applicationModules = [this.id];
            return this.createApplicationInterface(this.appInterface);
          }
        })
        .then(appInterface => {
          if (this.deployment) {
            if (this.deployment.appDeploymentId) {
              return this.updateApplicationDeployment(this.deployment);
            } else {
              return this.createApplicationDeployment(this.deployment);
            }
          } else {
            return Promise.resolve(null);
          }
        });
    },
    cancelModule() {
      this.$router.push({ path: "/applications" });
    },
    cancelInterface() {
      this.$router.push({ path: "/applications" });
    },
    cancelDeployment() {
      this.$router.push({
        name: "application_deployments",
        params: { id: this.id }
      });
    },
    deleteDeployment(deployment) {
      return this.deleteApplicationDeployment(deployment)
        .then(() => this.loadApplicationDeployments(this.id))
        .then(() => {
          this.$router.push({
            name: "application_deployments",
            params: { id: this.id }
          });
        });
    }
  },
  watch: {
    $route: function(to, from) {
      if (to.params.id !== from.params.id) {
        this.initialize();
      }
      if (this.deployment_id) {
        this.loadApplicationDeployment(this.deployment_id);
      }
    },
    currentModule: function(newModule) {
      // Clone the module from the store so we can modify it locally
      this.module = newModule.clone();
    },
    currentInterface: function(newInterface) {
      this.appInterface = newInterface.clone();
    },
    currentDeployment: function(newDeployment) {
      this.deployment = newDeployment.clone();
    }
  }
};
</script>

<style>
/* style the containing div, in base.html template */
/* .main-content {
    background-color: #ffffff;
} */
</style>

