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
        <b-nav tabs>
          <b-nav-item exact-active-class="active" exact :to="{name: id ? 'application_module' : 'new_application_module', params: {id: id}}">Details</b-nav-item>
          <b-nav-item exact-active-class="active" exact :to="{name: 'application_interface', params: {id: id}}" :disabled="!id">Interface</b-nav-item>
          <b-nav-item active-class="active" :to="{name: 'application_deployments', params: {id: id}}" :disabled="!id">Deployments</b-nav-item>
        </b-nav>
        <router-view name="module" v-if="module" v-model="module" @save="saveModule" @cancel="cancelModule" />
        <router-view name="interface" v-if="appInterface" v-model="appInterface" @save="saveInterface" @cancel="cancelInterface"
        />
        <router-view name="deployments" />
        <router-view name="deployment" />
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
    id: String
  },
  data: function() {
    return {
      module: null,
      appInterface: null,
      deployments: null,
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
    ...mapState("applications/interfaces", ["currentInterface"])
  },
  created() {
    this.initialize();
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
    initialize() {
      // TODO: move this to applications store?
      if (this.currentModule && this.currentModule.appModuleId === this.id) {
        this.module = this.currentModule.clone();
        this.loadApplicationInterface(this.id).catch(error => {
          notifications.NotificationList.addError(error);
        });
      } else if (this.id) {
        this.loadApplicationModule(this.id);
        this.loadApplicationInterface(this.id).catch(error => {
          notifications.NotificationList.addError(error);
        });
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
    cancelModule() {
      this.$router.push({ path: "/applications" });
    },
    cancelInterface() {
      this.$router.push({ path: "/applications" });
    }
  },
  watch: {
    $route: function(to, from) {
      if (to.params.id !== from.params.id) {
        this.initialize();
      }
    },
    currentModule: function(newModule) {
      // Clone the module from the store so we can modify it locally
      this.module = newModule.clone();
    },
    currentInterface: function(newInterface) {
      this.appInterface = newInterface.clone();
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

