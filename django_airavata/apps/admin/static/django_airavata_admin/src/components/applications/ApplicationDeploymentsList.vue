<template>
  <div>
    <list-layout
      @add-new-item="newApplicationDeployment"
      :items="deployments"
      title="Application Deployments"
      new-item-button-text="New Deployment"
      :new-button-disabled="readonly"
    >
      <template slot="item-list" slot-scope="slotProps">
        <b-table
          striped
          hover
          :fields="fields"
          :items="slotProps.items"
          sort-by="computeHostId"
        >
          <template slot="cell(action)" slot-scope="data">
            <router-link
              class="action-link"
              v-if="!data.item.userHasWriteAccess"
              :to="{
                name: 'application_deployment',
                params: {
                  id: id,
                  deploymentId: data.item.appDeploymentId,
                  readonly: true,
                },
              }"
            >
              View
              <i class="fa fa-eye" aria-hidden="true"></i>
            </router-link>
            <router-link
              class="action-link"
              v-if="data.item.userHasWriteAccess && data.item.appDeploymentId"
              :to="{
                name: 'application_deployment',
                params: {
                  id: id,
                  deploymentId: data.item.appDeploymentId,
                  readonly: false,
                },
              }"
            >
              Edit
              <i class="fa fa-edit" aria-hidden="true"></i>
            </router-link>
            <router-link
              class="action-link"
              v-if="data.item.userHasWriteAccess && !data.item.appDeploymentId"
              :to="{
                name: 'new_application_deployment',
                params: {
                  id: id,
                  hostId: data.item.computeHostId,
                  readonly: false,
                },
              }"
            >
              Edit
              <i class="fa fa-edit" aria-hidden="true"></i>
            </router-link>
            <delete-link
              v-if="data.item.userHasWriteAccess"
              @delete="removeApplicationDeployment(data.item)"
              class="action-link"
            >
              Are you sure you want to remove the
              <strong>{{
                getComputeResourceName(data.item.computeHostId)
              }}</strong>
              deployment?
            </delete-link>
          </template>
        </b-table>
      </template>
    </list-layout>
    <compute-resources-modal
      ref="modalSelectComputeResource"
      @selected="onSelectComputeResource"
      :compute-resource-names="selectableComputeResourceNames"
      :excluded-resource-ids="excludedComputeResourceIds"
    />
  </div>
</template>

<script>
import { services } from "django-airavata-api";
import { components, layouts } from "django-airavata-common-ui";
import ComputeResourcesModal from "../admin/ComputeResourcesModal.vue";

export default {
  name: "application-deployments-list",
  components: {
    "list-layout": layouts.ListLayout,
    ComputeResourcesModal,
    "delete-link": components.DeleteLink,
  },
  props: {
    deployments: {
      type: Array,
      required: true,
    },
    id: {
      // app module id
      type: String,
      required: true,
    },
    readonly: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      computeResourceNames: null,
      groupResourceProfiles: null,
    };
  },
  computed: {
    fields() {
      return [
        {
          label: "Compute Resource",
          key: "computeHostId",
          sortable: true,
          formatter: (value) => this.getComputeResourceName(value),
        },
        {
          label: "Description",
          key: "appDeploymentDescription",
        },
        {
          label: "Action",
          key: "action",
        },
      ];
    },
    selectableComputeResourceNames() {
      // Only allow selecting a compute resource for a new deployment if that
      // compute resource exists in a GroupResourceProfile
      if (this.computeResourceNames && this.groupResourceProfiles) {
        // Create a set of all computeResourceIds in GroupResourceProfiles
        const groupResourceProfileCompResources = {};
        for (const groupResourceProfile of this.groupResourceProfiles) {
          for (const computePreference of groupResourceProfile.computePreferences) {
            groupResourceProfileCompResources[
              computePreference.computeResourceId
            ] = null;
          }
        }
        const result = [];
        // Filter compute resources based on existence in groupResourceProfileCompResources
        for (const computeResourceId in this.computeResourceNames) {
          if (
            this.computeResourceNames.hasOwnProperty(computeResourceId) &&
            groupResourceProfileCompResources.hasOwnProperty(computeResourceId)
          ) {
            const computeResourceName = this.computeResourceNames[
              computeResourceId
            ];
            result.push({
              host_id: computeResourceId,
              host: computeResourceName,
            });
          }
        }
        return result;
      } else {
        return [];
      }
    },
    excludedComputeResourceIds() {
      return this.deployments.map((dep) => dep.computeHostId);
    },
  },
  mounted() {
    services.ComputeResourceService.names().then(
      (names) => (this.computeResourceNames = names)
    );
    services.GroupResourceProfileService.list().then(
      (groupResourceProfiles) =>
        (this.groupResourceProfiles = groupResourceProfiles)
    );
  },
  methods: {
    getComputeResourceName(computeResourceId) {
      if (
        this.computeResourceNames &&
        computeResourceId in this.computeResourceNames
      ) {
        return this.computeResourceNames[computeResourceId];
      } else {
        return computeResourceId.substring(0, 10) + "...";
      }
    },
    onSelectComputeResource(computeResourceId) {
      this.$emit("new", computeResourceId);
    },
    newApplicationDeployment() {
      this.$refs.modalSelectComputeResource.show();
    },
    removeApplicationDeployment(deployment) {
      this.$emit("delete", deployment);
    },
  },
};
</script>
