<template>
  <div class="has-fixed-footer">
    <unsaved-changes-guard :dirty="isDirty" />
    <confirmation-dialog
      ref="unsavedChangesDialog"
      title="You have unsaved changes"
    >
      You have unsaved changes. Are you sure you want to leave this page?
    </confirmation-dialog>
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
          <b-nav-item
            exact-active-class="active"
            exact
            :to="{
              name: id ? 'application_module' : 'new_application_module',
              params: { id: id },
            }"
            >Details</b-nav-item
          >
          <b-nav-item
            exact-active-class="active"
            exact
            :to="{ name: 'application_interface', params: { id: id } }"
            :disabled="!id"
            >Interface</b-nav-item
          >
          <b-nav-item
            active-class="active"
            :to="{ name: 'application_deployments', params: { id: id } }"
            :disabled="!id"
            >Deployments</b-nav-item
          >
        </b-nav>
        <router-view
          name="module"
          v-if="appModule"
          v-model="appModule"
          @input="appModuleIsDirty = true"
          :readonly="!appModule.userHasWriteAccess"
          :validation-errors="appModuleValidationErrors"
        />
        <router-view
          name="interface"
          v-if="appInterface"
          v-model="appInterface"
          @input="appInterfaceIsDirty = true"
          :readonly="!appInterface.userHasWriteAccess"
        />
        <router-view
          name="deployments"
          v-if="appModule && appDeployments"
          :deployments="appDeployments"
          @new="createNewDeployment"
          @delete="deleteApplicationDeployment"
          :readonly="!appModule.userHasWriteAccess"
        />
        <router-view
          name="deployment"
          v-if="currentDeployment && currentDeploymentSharedEntity"
          v-model="currentDeployment"
          :shared-entity="currentDeploymentSharedEntity"
          @sharing-changed="deploymentSharingChanged"
          @input="currentDeploymentChanged"
        />
      </div>
    </div>
    <div class="fixed-footer">
      <b-button
        class="editor-button"
        variant="primary"
        @click="saveAll"
        :disabled="readonly || !isDirty"
      >
        Save
      </b-button>
      <delete-button
        class="editor-button"
        v-if="id"
        :disabled="readonly"
        @delete="deleteApplication"
      >
        Are you sure you want to delete the
        <strong>{{ appModule ? appModule.appModuleName : "" }}</strong>
        application?
      </delete-button>
      <b-button class="editor-button" variant="secondary" @click="cancel">
        Cancel
      </b-button>
    </div>
  </div>
</template>

<script>
import {
  errors,
  models,
  services,
  utils as apiUtils,
} from "django-airavata-api";
import { components, notifications } from "django-airavata-common-ui";

export default {
  name: "application-editor-container",
  props: {
    id: String,
    deploymentId: String,
    hostId: String,
  },
  components: {
    "unsaved-changes-guard": components.UnsavedChangesGuard,
    "confirmation-dialog": components.ConfirmationDialog,
    "delete-button": components.DeleteButton,
  },
  data: function () {
    return {
      appModule: null,
      appInterface: null,
      appDeployments: [],
      // Map key is computeHostId, value is SharedEntity
      appDeploymentsSharedEntities: {},
      currentDeployment: null,
      currentDeploymentSharedEntity: null,
      appModuleIsDirty: false,
      appInterfaceIsDirty: false,
      dirtyAppDeploymentComputeHostIds: [],
      dirtyAppDeploymentSharedEntityComputeHostIds: [],
      appModuleValidationErrors: null,
    };
  },
  computed: {
    title: function () {
      if (this.id) {
        return this.appModule && this.appModule.appModuleName
          ? this.appModule.appModuleName
          : "";
      } else {
        return "Create a New Application";
      }
    },
    isDirty() {
      return (
        this.appModuleIsDirty ||
        this.appInterfaceIsDirty ||
        this.dirtyAppDeploymentComputeHostIds.length > 0 ||
        this.dirtyAppDeploymentSharedEntityComputeHostIds.length > 0
      );
    },
    readonly() {
      return this.appModule && !this.appModule.userHasWriteAccess;
    },
  },
  created() {
    this.initialize();
  },
  methods: {
    initialize() {
      if (this.id) {
        this.loadApplicationModule(this.id);
        this.loadApplicationInterface(this.id);
        this.loadApplicationDeployments(this.id).then(() => {
          this.initializeDeploymentEditing();
        });
      } else {
        this.appModule = new models.ApplicationModule({
          userHasWriteAccess: true,
        });
      }
    },
    initializeDeploymentEditing() {
      if (this.deploymentId) {
        this.startEditingExistingDeployment(this.deploymentId);
      } else if (this.hostId) {
        this.startEditingNewDeployment(this.hostId);
      }
    },
    startEditingExistingDeployment(deploymentId) {
      this.setCurrentDeploymentFromAppDeploymentId(
        deploymentId
      ).then((appDeployment) =>
        this.setCurrentApplicationDeploymentSharedEntity(appDeployment)
      );
    },
    startEditingNewDeployment(computeHostId) {
      this.setCurrentDeploymentFromComputeHostId(
        computeHostId
      ).then((appDeployment) =>
        this.setCurrentApplicationDeploymentSharedEntity(appDeployment)
      );
    },
    loadApplicationModule(appModuleId) {
      return services.ApplicationModuleService.retrieve({
        lookup: appModuleId,
      }).then((appModule) => {
        this.appModuleIsDirty = false;
        this.appModule = appModule;
      });
    },
    createApplicationModule(appModule) {
      return services.ApplicationModuleService.create(
        { data: appModule },
        { ignoreErrors: true }
      );
    },
    updateApplicationModule(appModule) {
      return services.ApplicationModuleService.update(
        {
          lookup: appModule.appModuleId,
          data: appModule,
        },
        { ignoreErrors: true }
      );
    },
    saveApplicationModule(appModule) {
      return (this.id
        ? this.updateApplicationModule(appModule)
        : this.createApplicationModule(appModule)
      )
        .then((appModule) => {
          this.appModuleValidationErrors = null;
          this.appModuleIsDirty = false;
          this.appModule = appModule;
          return appModule;
        })
        .catch((error) => {
          if (errors.ErrorUtils.isValidationError(error)) {
            this.appModuleValidationErrors = error.details.response;
          } else {
            this.appModuleValidationErrors = null;
            notifications.NotificationList.addError(error);
          }
          return Promise.reject(error);
        });
    },
    deleteApplicationModule() {
      const deleteModule = this.id
        ? services.ApplicationModuleService.delete({
            lookup: this.id,
          })
        : Promise.resolve(null);
      return deleteModule.then(() => {
        this.appModuleIsDirty = false;
        this.appModule = null;
      });
    },
    loadApplicationInterface(appModuleId) {
      return services.ApplicationModuleService.getApplicationInterface(
        { lookup: appModuleId },
        { ignoreErrors: true }
      )
        .then((appInterface) => {
          this.appInterfaceIsDirty = false;
          this.appInterface = appInterface;
          return appInterface;
        })
        .catch((error) => {
          if (error.details.status === 404) {
            // If there is no interface, just create a new instance
            const appInterface = new models.ApplicationInterfaceDefinition({
              userHasWriteAccess: true,
            });
            appInterface.addStandardOutAndStandardErrorOutputs();
            this.appInterface = appInterface;
            this.appInterfaceIsDirty = true;
            return Promise.resolve(null);
          } else {
            throw error;
          }
        })
        .catch(apiUtils.FetchUtils.reportError);
    },
    createApplicationInterface(appInterface) {
      return services.ApplicationInterfaceService.create({
        data: appInterface,
      }).then((appInterface) => {
        this.appInterfaceIsDirty = false;
        this.appInterface = appInterface;
        return appInterface;
      });
    },
    updateApplicationInterface(appInterface) {
      return services.ApplicationInterfaceService.update({
        lookup: appInterface.applicationInterfaceId,
        data: appInterface,
      }).then((appInterface) => {
        this.appInterfaceIsDirty = false;
        this.appInterface = appInterface;
        return appInterface;
      });
    },
    saveApplicationInterface(appInterface) {
      appInterface.applicationName = this.appModule.appModuleName;
      appInterface.applicationModules = [this.id];
      return appInterface.applicationInterfaceId
        ? this.updateApplicationInterface(appInterface)
        : this.createApplicationInterface(appInterface);
    },
    deleteApplicationInterface(appInterface) {
      if (appInterface.applicationInterfaceId) {
        return services.ApplicationInterfaceService.delete({
          lookup: appInterface.applicationInterfaceId,
        }).then(() => (this.appInterfaceIsDirty = false));
      } else {
        this.appInterfaceIsDirty = false;
        this.appInterface = null;
        return Promise.resolve(null);
      }
    },
    loadApplicationDeployments(appModuleId) {
      return services.ApplicationModuleService.getApplicationDeployments({
        lookup: appModuleId,
      }).then((appDeployments) => {
        this.dirtyAppDeploymentComputeHostIds = [];
        this.appDeployments = appDeployments;
        return appDeployments;
      });
    },
    loadApplicationDeployment(appDeploymentId) {
      return services.ApplicationDeploymentService.retrieve({
        lookup: appDeploymentId,
      }).then((appDeployment) => {
        this.currentDeployment = appDeployment;
        return appDeployment;
      });
    },
    createApplicationDeployment(appDeployment) {
      return services.ApplicationDeploymentService.create({
        data: appDeployment,
      }).then((appDeployment) => {
        this.removeDirtyAppDeploymentComputeHostId(appDeployment);
        this.replaceAppDeployment(appDeployment);
        return appDeployment;
      });
    },
    updateApplicationDeployment(appDeployment) {
      return services.ApplicationDeploymentService.update({
        lookup: appDeployment.appDeploymentId,
        data: appDeployment,
      }).then((appDeployment) => {
        this.removeDirtyAppDeploymentComputeHostId(appDeployment);
        this.replaceAppDeployment(appDeployment);
        return appDeployment;
      });
    },
    saveApplicationDeployment(appDeployment) {
      return appDeployment.appDeploymentId
        ? this.updateApplicationDeployment(appDeployment)
        : this.createApplicationDeployment(appDeployment);
    },
    deleteApplicationDeployment(appDeployment) {
      if (appDeployment.appDeploymentId) {
        return services.ApplicationDeploymentService.delete({
          lookup: appDeployment.appDeploymentId,
        }).then(() => {
          this.removeDirtyAppDeploymentComputeHostId(appDeployment);
          return this.loadApplicationDeployments(this.id);
        });
      } else {
        const depIndex = this.appDeployments.findIndex(
          (dep) => dep.computeHostId === appDeployment.computeHostId
        );
        this.appDeployments.splice(depIndex, 1);
        this.removeDirtyAppDeploymentComputeHostId(appDeployment);
        return Promise.resolve(this.appDeployments);
      }
    },
    currentDeploymentChanged(appDeployment) {
      this.replaceAppDeployment(appDeployment);
      this.setApplicationDeploymentDirty(appDeployment);
    },
    replaceAppDeployment(appDeployment) {
      const depIndex = this.appDeployments.findIndex(
        (dep) => dep.computeHostId === appDeployment.computeHostId
      );
      this.appDeployments.splice(depIndex, 1, appDeployment);
    },
    setApplicationDeploymentDirty(appDeployment) {
      if (
        !this.dirtyAppDeploymentComputeHostIds.includes(
          appDeployment.computeHostId
        )
      ) {
        this.dirtyAppDeploymentComputeHostIds.push(appDeployment.computeHostId);
      }
    },
    removeDirtyAppDeploymentComputeHostId(appDeployment) {
      const hostIdIndex = this.dirtyAppDeploymentComputeHostIds.indexOf(
        appDeployment.computeHostId
      );
      if (hostIdIndex >= 0) {
        this.dirtyAppDeploymentComputeHostIds.splice(hostIdIndex, 1);
      }
    },
    createNewDeployment(computeHostId) {
      this.$router.push({
        name: "new_application_deployment",
        params: { id: this.id, hostId: computeHostId },
      });
    },
    loadApplicationDeploymentSharedEntity(appDeployment) {
      return services.SharedEntityService.retrieve({
        lookup: appDeployment.appDeploymentId,
      }).then((sharedEntity) => {
        this.appDeploymentsSharedEntities[
          appDeployment.computeHostId
        ] = sharedEntity;
        this.removeAppDeploymentSharedEntityDirty(sharedEntity, appDeployment);
        return sharedEntity;
      });
    },
    setCurrentApplicationDeploymentSharedEntity(appDeployment) {
      if (appDeployment.computeHostId in this.appDeploymentsSharedEntities) {
        this.currentDeploymentSharedEntity = this.appDeploymentsSharedEntities[
          appDeployment.computeHostId
        ];
        return Promise.resolve(this.currentDeploymentSharedEntity);
      } else if (appDeployment.appDeploymentId) {
        return this.loadApplicationDeploymentSharedEntity(appDeployment).then(
          (sharedEntity) => (this.currentDeploymentSharedEntity = sharedEntity)
        );
      } else {
        throw new Error(
          "Could not find shared entity in local map and cannot fetch"
        );
      }
    },
    deploymentSharingChanged(deploymentSharedEntity, appDeployment, dirty) {
      this.currentDeploymentSharedEntity = deploymentSharedEntity;
      this.replaceAppDeploymentSharedEntity(
        deploymentSharedEntity,
        appDeployment
      );
      if (dirty) {
        this.setApplicationDeploymentSharedEntityDirty(
          deploymentSharedEntity,
          appDeployment
        );
      } else {
        this.removeAppDeploymentSharedEntityDirty(
          deploymentSharedEntity,
          appDeployment
        );
      }
    },
    mergeSharedEntity(sharedEntity, appDeployment) {
      return services.SharedEntityService.merge({
        data: sharedEntity,
        lookup: appDeployment.appDeploymentId,
      }).then((sharedEntity) => {
        this.replaceAppDeploymentSharedEntity(sharedEntity, appDeployment);
        this.removeAppDeploymentSharedEntityDirty(sharedEntity, appDeployment);
        return sharedEntity;
      });
    },
    updateSharedEntity(sharedEntity, appDeployment) {
      return services.SharedEntityService.update({
        data: sharedEntity,
        lookup: appDeployment.appDeploymentId,
      }).then((sharedEntity) => {
        this.replaceAppDeploymentSharedEntity(sharedEntity, appDeployment);
        this.removeAppDeploymentSharedEntityDirty(sharedEntity, appDeployment);
        return sharedEntity;
      });
    },
    saveSharedEntity(sharedEntity, appDeployment) {
      return sharedEntity.entityId
        ? this.updateSharedEntity(sharedEntity, appDeployment)
        : this.mergeSharedEntity(sharedEntity, appDeployment);
    },
    setApplicationDeploymentSharedEntityDirty(sharedEntity, appDeployment) {
      if (
        !this.dirtyAppDeploymentSharedEntityComputeHostIds.includes(
          appDeployment.computeHostId
        )
      ) {
        this.dirtyAppDeploymentSharedEntityComputeHostIds.push(
          appDeployment.computeHostId
        );
      }
    },
    removeAppDeploymentSharedEntityDirty(sharedEntity, appDeployment) {
      const hostIdIndex = this.dirtyAppDeploymentSharedEntityComputeHostIds.indexOf(
        appDeployment.computeHostId
      );
      if (hostIdIndex >= 0) {
        this.dirtyAppDeploymentSharedEntityComputeHostIds.splice(
          hostIdIndex,
          1
        );
      }
    },
    replaceAppDeploymentSharedEntity(sharedEntity, appDeployment) {
      this.appDeploymentsSharedEntities[
        appDeployment.computeHostId
      ] = sharedEntity;
    },
    setCurrentDeploymentFromAppDeploymentId(appDeploymentId) {
      this.currentDeployment = this.appDeployments.find(
        (dep) => dep.appDeploymentId === appDeploymentId
      );
      if (!this.currentDeployment) {
        throw new Error(
          "Unable to find deployment from appDeploymentId=" + appDeploymentId
        );
      }
      return Promise.resolve(this.currentDeployment);
    },
    setCurrentDeploymentFromComputeHostId(computeHostId) {
      this.currentDeployment = this.appDeployments.find(
        (dep) => dep.computeHostId === computeHostId
      );
      if (!this.currentDeployment) {
        // Create a new deployment
        const deployment = new models.ApplicationDeploymentDescription({
          userHasWriteAccess: true,
        });
        deployment.appModuleId = this.id;
        deployment.computeHostId = computeHostId;
        this.currentDeployment = deployment;
        this.appDeployments.push(deployment);
        this.setApplicationDeploymentDirty(deployment);
        this.appDeploymentsSharedEntities[
          computeHostId
        ] = new models.SharedEntity();
      }
      return Promise.resolve(this.currentDeployment);
    },
    saveAll() {
      const moduleSave = this.appModuleIsDirty
        ? this.saveApplicationModule(this.appModule).catch((error) => {
            // Navigate to the route that has the error
            this.$router.push({
              name: this.id ? "application_module" : "new_application_module",
            });
            // Cancel the chain of promises
            return Promise.reject(error);
          })
        : Promise.resolve(this.appModule);
      const interfaceSave = moduleSave.then(() =>
        this.appInterfaceIsDirty
          ? this.saveApplicationInterface(this.appInterface).catch((error) => {
              // Navigate to the route that has the error
              this.$router.push({
                name: "application_interface",
              });
              // Cancel the chain of promises
              return Promise.reject(error);
            })
          : Promise.resolve(this.appInterface)
      );
      interfaceSave
        .then(() => {
          return Promise.all(
            this.dirtyAppDeploymentComputeHostIds.map((computeHostId) => {
              const deployment = this.appDeployments.find(
                (dep) => dep.computeHostId === computeHostId
              );
              return this.saveApplicationDeployment(deployment).catch(
                (error) => {
                  // Navigate to the route that has the error
                  if (deployment.appDeploymentId) {
                    this.$router.push({
                      name: "application_deployment",
                      params: {
                        id: this.id,
                        deploymentId: deployment.appDeploymentId,
                      },
                    });
                  } else {
                    this.$router.push({
                      name: "new_application_deployment",
                      params: { id: this.id, hostId: deployment.computeHostId },
                    });
                  }
                  return Promise.reject(error);
                }
              );
            })
          );
        })
        .then(() => {
          return Promise.all(
            this.dirtyAppDeploymentSharedEntityComputeHostIds.map(
              (computeHostId) => {
                const sharedEntity = this.appDeploymentsSharedEntities[
                  computeHostId
                ];
                const deployment = this.appDeployments.find(
                  (dep) => dep.computeHostId === computeHostId
                );
                return this.saveSharedEntity(sharedEntity, deployment).catch(
                  (error) => {
                    // Navigate to the route that has the error
                    if (deployment.appDeploymentId) {
                      this.$router.push({
                        name: "application_deployment",
                        params: {
                          id: this.id,
                          deploymentId: deployment.appDeploymentId,
                        },
                      });
                    } else {
                      this.$router.push({
                        name: "new_application_deployment",
                        params: {
                          id: this.id,
                          hostId: deployment.computeHostId,
                        },
                      });
                    }
                    return Promise.reject(error);
                  }
                );
              }
            )
          );
        })
        .then(() => {
          notifications.NotificationList.add(
            new notifications.Notification({
              type: "SUCCESS",
              message: "Application saved successfully",
              duration: 5,
            })
          );
          if (!this.id && this.appModule.appModuleId) {
            // if we just create a new module, navigate to app module route now
            // that we have an id
            this.$router.push({
              name: "application_module",
              params: { id: this.appModule.appModuleId },
            });
          }
          if (this.hostId) {
            // If creating a new deployment, navigate to the deployments list
            this.$router.push({
              name: "application_deployments",
              params: { id: this.appModule.appModuleId },
            });
          } else {
            // Reinitialize deployment editing so that deployment being edited is
            // the saved instance
            this.initializeDeploymentEditing();
          }
        });
    },
    cancel() {
      this.$router.push({ path: "/applications" });
    },
    deleteApplication() {
      const deleteAllDeployments = this.appDeployments.map((dep) =>
        this.deleteApplicationDeployment(dep)
      );
      return Promise.all(deleteAllDeployments)
        .then(() => this.deleteApplicationInterface(this.appInterface))
        .then(() => this.deleteApplicationModule(this.appModule))
        .then(() => {
          this.$router.push({ path: "/applications" });
        });
    },
  },
  watch: {
    $route: function (to, from) {
      if (to.params.id !== from.params.id) {
        this.initialize();
      }
      this.initializeDeploymentEditing();
    },
  },
  beforeRouteLeave(to, from, next) {
    if (this.isDirty) {
      this.$refs.unsavedChangesDialog.show();
      this.$refs.unsavedChangesDialog.$on("ok", next);
    } else {
      next();
    }
  },
};
</script>

<style scoped>
/* style the containing div, in base.html template */
/* .main-content {
    background-color: #ffffff;
} */
.editor-button + .editor-button {
  margin-left: 0.25em;
}
</style>
