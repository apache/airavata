<template>
  <div class="has-fixed-footer">
    <div class="row">
      <div class="col">
        <h1 class="h4 mb-4">{{ title }}</h1>
      </div>
    </div>
    <div class="row">
      <div class="col">
        <div class="card">
          <div class="card-body">
            <b-form-group label="Name" label-for="profile-name">
              <b-form-input
                id="profile-name"
                type="text"
                v-model="data.groupResourceProfileName"
                required
                placeholder="Name of this Group Resource Profile"
              >
              </b-form-input>
            </b-form-group>
            <b-form-group
              label="Default SSH Credential"
              label-for="default-credential-store-token"
            >
              <ssh-credential-selector
                id="default-credential-store-token"
                v-model="data.defaultCredentialStoreToken"
              >
              </ssh-credential-selector>
            </b-form-group>
            <share-button ref="shareButton" :entity-id="id" />
          </div>
        </div>
      </div>
    </div>
    <list-layout
      :items="data.computePreferences"
      title="Compute Preferences"
      new-item-button-text="New Compute Preference"
      @add-new-item="createComputePreference"
    >
      <template slot="item-list" slot-scope="slotProps">
        <b-table
          hover
          :fields="computePreferencesFields"
          :items="slotProps.items"
          sort-by="computeResourceId"
        >
          <template slot="policy" slot-scope="row">
            <compute-resource-policy-summary
              :compute-resource-id="row.item.computeResourceId"
              :group-resource-profile="data"
            />
          </template>
          <template slot="reservations" slot-scope="row">
            <compute-resource-reservations-summary :reservations="row.value" />
          </template>
          <template slot="action" slot-scope="row">
            <router-link
              class="action-link"
              :to="{
                name: 'compute_preference',
                params: {
                  value: row.item,
                  id: id,
                  host_id: row.item.computeResourceId,
                  groupResourceProfile: data,
                  computeResourcePolicy: data.getComputeResourcePolicy(
                    row.item.computeResourceId
                  ),
                  batchQueueResourcePolicies: data.getBatchQueueResourcePolicies(
                    row.item.computeResourceId
                  )
                }
              }"
            >
              Edit
              <i class="fa fa-edit" aria-hidden="true"></i>
            </router-link>
            <a
              href="#"
              class="action-link text-danger"
              @click.prevent="
                removeComputePreference(row.item.computeResourceId)
              "
            >
              Delete
              <i class="fa fa-trash" aria-hidden="true"></i>
            </a>
          </template>
        </b-table>
      </template>
    </list-layout>
    <div class="fixed-footer">
      <b-button variant="primary" @click="saveGroupResourceProfile"
        >Save</b-button
      >
      <b-button
        v-if="id"
        class="ml-2"
        variant="danger"
        @click="removeGroupResourceProfile"
        >Delete</b-button
      >
      <b-button class="ml-2" variant="secondary" @click="cancel"
        >Cancel</b-button
      >
    </div>
    <compute-resources-modal
      ref="modalSelectComputeResource"
      @selected="onSelectComputeResource"
      :excluded-resource-ids="excludedComputeResourceIds"
    />
  </div>
</template>

<script>
import { components as comps, layouts } from "django-airavata-common-ui";
import { models, services } from "django-airavata-api";
import ComputeResourcePolicySummary from "./ComputeResourcePolicySummary.vue";
import ComputeResourceReservationsSummary from "./ComputeResourceReservationsSummary.vue";
import ComputeResourcesModal from "../ComputeResourcesModal.vue";
import SSHCredentialSelector from "../../credentials/SSHCredentialSelector.vue";

export default {
  name: "group-compute-resource-preference",
  props: {
    value: {
      type: models.GroupResourceProfile,
      default: function() {
        return new models.GroupResourceProfile();
      }
    },
    id: {
      type: String
    }
  },
  mounted: function() {
    if (this.id) {
      if (!this.value.groupResourceProfileId) {
        services.GroupResourceProfileService.retrieve({ lookup: this.id }).then(
          grp => (this.data = grp)
        );
      }
    }
  },
  data: function() {
    let data = this.value.clone();
    return {
      data: data,
      service: services.GroupResourceProfileService,
      computePreferencesFields: [
        {
          label: "Name",
          key: "computeResourceId",
          sortable: true,
          formatter: value => this.getComputeResourceName(value)
        },
        {
          label: "Username",
          key: "loginUserName"
        },
        {
          label: "Allocation",
          key: "allocationProjectNumber"
        },
        {
          label: "Policy",
          key: "policy" // custom rendering
        },
        {
          label: "Reservations",
          key: "reservations" // custom rendering
        },
        {
          label: "Action",
          key: "action"
        }
      ]
    };
  },

  components: {
    "share-button": comps.ShareButton,
    "list-layout": layouts.ListLayout,
    ComputeResourcePolicySummary,
    ComputeResourcesModal,
    "ssh-credential-selector": SSHCredentialSelector,
    ComputeResourceReservationsSummary
  },
  computed: {
    excludedComputeResourceIds() {
      const currentPrefs = this.data.computePreferences
        ? this.data.computePreferences.map(
            computePreference => computePreference.computeResourceId
          )
        : [];
      return currentPrefs;
    },
    title: function() {
      return this.id
        ? this.data.groupResourceProfileName
        : "New Group Resource Profile";
    }
  },
  methods: {
    saveGroupResourceProfile: function() {
      var persist = null;
      if (this.id) {
        persist = this.service.update({ data: this.data, lookup: this.id });
      } else {
        persist = this.service.create({ data: this.data }).then(data => {
          // Merge sharing settings with default sharing settings created when
          // Group Resource Profile was created
          const groupResourceProfileId = data.groupResourceProfileId;
          return this.$refs.shareButton.mergeAndSave(groupResourceProfileId);
        });
      }
      persist.then(() => {
        this.$router.push("/group-resource-profiles");
      });
    },
    getComputeResourceName: function(computeResourceId) {
      // TODO: load compute resources to get the real name
      return computeResourceId && computeResourceId.indexOf("_") > 0
        ? computeResourceId.split("_")[0]
        : computeResourceId;
    },
    cancel: function() {
      this.$router.push("/group-resource-profiles");
    },
    createComputePreference: function() {
      this.$refs.modalSelectComputeResource.show();
    },
    onSelectComputeResource: function(computeResourceId) {
      const computeResourcePreference = new models.GroupComputeResourcePreference();
      computeResourcePreference.computeResourceId = computeResourceId;
      this.$router.push({
        name: "compute_preference_for_new_group_resource_profile",
        params: {
          value: computeResourcePreference,
          id: this.id,
          host_id: computeResourcePreference.computeResourceId,
          groupResourceProfile: this.data
        }
      });
    },
    removeComputePreference: function(computeResourceId) {
      let groupResourceProfile = this.data.clone();
      groupResourceProfile.removeComputeResource(computeResourceId);
      this.service
        .update({ data: groupResourceProfile, lookup: this.id })
        .then(groupResourceProfile => (this.data = groupResourceProfile));
    },
    removeGroupResourceProfile: function() {
      if (this.id) {
        this.service.delete({ lookup: this.id }).then(() => {
          this.$router.push("/group-resource-profiles");
        });
      } else {
        // Nothing to delete so just treat like a cancel
        this.cancel();
      }
    }
  }
};
</script>
