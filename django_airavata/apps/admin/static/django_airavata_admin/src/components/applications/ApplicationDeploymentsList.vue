<template>
  <div>
    <list-layout @add-new-item="newApplicationDeployment" :items="deployments" title="Application Deployments" new-item-button-text="New Deployment">
      <template slot="item-list" slot-scope="slotProps">

        <b-table striped hover :fields="fields" :items="slotProps.items" sort-by="computeHostId">
          <template slot="action" slot-scope="data">
            <router-link v-if="!data.item.userHasWriteAccess" :to="{name: 'application_deployment', params: {id: id, deployment_id: data.item.appDeploymentId}}">
              View
              <i class="fa fa-eye" aria-hidden="true"></i>
            </router-link>
            <router-link v-if="data.item.userHasWriteAccess" :to="{name: 'application_deployment', params: {id: id, deployment_id: data.item.appDeploymentId}}">
              Edit
              <i class="fa fa-edit" aria-hidden="true"></i>
            </router-link>
            <a href="#" class="text-danger" @click.prevent="removeApplicationDeployment(data.item)" v-if="data.item.userHasWriteAccess">
              Delete
              <i class="fa fa-trash" aria-hidden="true"></i>
            </a>
          </template>
        </b-table>
      </template>
    </list-layout>
    <compute-resources-modal ref="modalSelectComputeResource" @selected="onSelectComputeResource" :excluded-resource-ids="excludedComputeResourceIds"
    />
  </div>
</template>

<script>
import { models, services } from "django-airavata-api";
import { layouts } from "django-airavata-common-ui";
import ComputeResourcesModal from "../admin/ComputeResourcesModal.vue";

export default {
  name: "application-deployments-list",
  components: {
    "list-layout": layouts.ListLayout,
    ComputeResourcesModal
  },
  props: {
    deployments: {
      type: Array,
      required: true
    },
    id: {
      // app module id
      type: String,
      required: true
    }
  },
  data() {
    return {
      computeResourceNames: null
    };
  },
  computed: {
    fields() {
      return [
        {
          label: "Compute Resource",
          key: "computeHostId",
          sortable: true,
          formatter: value => this.getComputeResourceName(value)
        },
        {
          label: "Description",
          key: "appDeploymentDescription"
        },
        {
          label: "Action",
          key: "action"
        }
      ];
    },
    excludedComputeResourceIds() {
      return this.deployments.map(dep => dep.computeHostId);
    }
  },
  mounted() {
    services.ComputeResourceService.names().then(
      names => (this.computeResourceNames = names)
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
    }
  }
};
</script>

