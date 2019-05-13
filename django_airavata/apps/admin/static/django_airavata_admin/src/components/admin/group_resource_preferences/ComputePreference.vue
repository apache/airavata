<template>
  <div>
    <div class="row">
      <div class="col">
        <h1 class="h4 mb-4">
          <div v-if="localGroupResourceProfile" class="group-resource-profile-name text-muted text-uppercase">
            <i class="fa fa-server" aria-hidden="true"></i> {{ localGroupResourceProfile.groupResourceProfileName }}</div>
          {{ computeResource.hostName }}
        </h1>
      </div>
    </div>
    <div class="row">
      <div class="col">
        <div class="card">
          <div class="card-body">
            <b-form-group label="Login Username" label-for="login-username">
              <b-form-input id="login-username" type="text" v-model="data.loginUserName">
              </b-form-input>
            </b-form-group>
            <b-form-group label="SSH Credential" label-for="credential-store-token">
              <ssh-credential-selector v-model="data.resourceSpecificCredentialStoreToken"
                :null-option-default-credential-token="localGroupResourceProfile.defaultCredentialStoreToken"
                :null-option-disabled="!localGroupResourceProfile.defaultCredentialStoreToken">
                <template slot="null-option-label" slot-scope="nullOptionLabelScope">
                  <span v-if="nullOptionLabelScope.defaultCredentialSummary">
                    Use the default SSH credential for {{ localGroupResourceProfile.groupResourceProfileName }} ({{
                    nullOptionLabelScope.defaultCredentialSummary.description }})
                  </span>
                  <span v-else>
                    Select a SSH credential
                  </span>
                </template>
              </ssh-credential-selector>
            </b-form-group>
            <b-form-group label="Allocation Project Number" label-for="allocation-number">
              <b-form-input id="allocation-number" type="text" v-model="data.allocationProjectNumber">
              </b-form-input>
            </b-form-group>
            <b-form-group label="Scratch Location" label-for="scratch-location">
              <b-form-input id="scratch-location" type="text" v-model="data.scratchLocation">
              </b-form-input>
            </b-form-group>
          </div>
        </div>
      </div>
    </div>
    <div class="row">
      <div class="col">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">Policy</h5>
            <b-form-group label="Allowed Queues">
              <div v-for="batchQueue in computeResource.batchQueues" :key="batchQueue.queueName" v-if="localComputeResourcePolicy">
                <b-form-checkbox :checked="localComputeResourcePolicy.allowedBatchQueues.includes(batchQueue.queueName)"
                  @input="batchQueueChecked(batchQueue, $event)">
                  {{ batchQueue.queueName }}
                </b-form-checkbox>
                <batch-queue-resource-policy v-if="localComputeResourcePolicy.allowedBatchQueues.includes(batchQueue.queueName)"
                  :batch-queue="batchQueue" :value="localBatchQueueResourcePolicies.find(pol => pol.queuename === batchQueue.queueName)"
                  @input="updatedBatchQueueResourcePolicy(batchQueue, $event)" />
              </div>
            </b-form-group>
          </div>
        </div>
      </div>
    </div>
    <div class="row">
      <div class="col d-flex justify-content-end">
        <b-button variant="primary" @click="save">Save</b-button>
        <b-button class="ml-2" variant="danger" @click="remove">Delete</b-button>
        <b-button class="ml-2" variant="secondary" @click="cancel">Cancel</b-button>
      </div>
    </div>
  </div>
</template>

<script>
import DjangoAiravataAPI from "django-airavata-api";
import BatchQueueResourcePolicy from "./BatchQueueResourcePolicy.vue";
import SSHCredentialSelector from "../../credentials/SSHCredentialSelector.vue";

import { models, services } from "django-airavata-api";
import { mixins } from "django-airavata-common-ui";

export default {
  name: "compute-preference",
  components: {
    BatchQueueResourcePolicy,
    "ssh-credential-selector": SSHCredentialSelector
  },
  props: {
    id: {
      type: String
    },
    host_id: {
      type: String,
      required: true
    },
    groupResourceProfile: {
      type: models.GroupResourceProfile
    },
    computeResourcePolicy: {
      type: models.ComputeResourcePolicy
    },
    batchQueueResourcePolicies: {
      type: Array
    }
  },
  mounted: function() {
    const computeResourcePromise = this.fetchComputeResource(this.host_id);
    if (!this.value && this.id && this.host_id) {
      services.GroupResourceProfileService.retrieve({ lookup: this.id }).then(
        groupResourceProfile => {
          this.localGroupResourceProfile = groupResourceProfile;
          const computeResourcePreference = groupResourceProfile.getComputePreference(
            this.host_id
          );
          if (computeResourcePreference) {
            this.data = computeResourcePreference;
          }
          const computeResourcePolicy = groupResourceProfile.getComputeResourcePolicy(
            this.host_id
          );
          if (computeResourcePolicy) {
            this.localComputeResourcePolicy = computeResourcePolicy;
          } else {
            this.createDefaultComputeResourcePolicy(computeResourcePromise);
          }
          this.localBatchQueueResourcePolicies = groupResourceProfile.getBatchQueueResourcePolicies(
            this.host_id
          );
        }
      );
    } else if (!this.computeResourcePolicy) {
      this.createDefaultComputeResourcePolicy(computeResourcePromise);
    }
  },
  data: function() {
    return {
      data: this.value
        ? this.value.clone()
        : new models.GroupComputeResourcePreference({
            computeResourceId: this.host_id
          }),
      localGroupResourceProfile: this.groupResourceProfile
        ? this.groupResourceProfile.clone()
        : null,
      localComputeResourcePolicy: this.computeResourcePolicy
        ? this.computeResourcePolicy.clone()
        : null,
      localBatchQueueResourcePolicies: this.batchQueueResourcePolicies
        ? this.batchQueueResourcePolicies.map(pol => pol.clone())
        : [],
      computeResource: {
        batchQueues: [],
        jobSubmissionInterfaces: []
      }
    };
  },
  computed: {},
  mixins: [mixins.VModelMixin],
  methods: {
    batchQueueChecked: function(batchQueue, checked) {
      if (checked) {
        this.localComputeResourcePolicy.allowedBatchQueues.push(
          batchQueue.queueName
        );
      } else {
        const queueIndex = this.localComputeResourcePolicy.allowedBatchQueues.indexOf(
          batchQueue.queueName
        );
        this.localComputeResourcePolicy.allowedBatchQueues.splice(
          queueIndex,
          1
        );
        // Remove batchQueueResourcePolicy if it exists
        const policyIndex = this.localBatchQueueResourcePolicies.findIndex(
          pol => pol.queuename === batchQueue.queueName
        );
        if (policyIndex >= 0) {
          this.localBatchQueueResourcePolicies.splice(policyIndex, 1);
        }
      }
    },
    updatedBatchQueueResourcePolicy: function(
      batchQueue,
      batchQueueResourcePolicy
    ) {
      const queueName = batchQueue.queueName;
      if (batchQueueResourcePolicy) {
        const existingPolicy = this.localBatchQueueResourcePolicies.find(
          pol => pol.queuename === queueName
        );
        if (existingPolicy) {
          Object.assign(existingPolicy, batchQueueResourcePolicy);
        } else {
          // For new BatchQueueResourcePolicy instances, set the parent ids
          batchQueueResourcePolicy.groupResourceProfileId = this.id;
          batchQueueResourcePolicy.computeResourceId = this.host_id;
          this.localBatchQueueResourcePolicies.push(batchQueueResourcePolicy);
        }
      } else {
        const existingPolicyIndex = this.localBatchQueueResourcePolicies.findIndex(
          pol => pol.queuename === queueName
        );
        if (existingPolicyIndex >= 0) {
          this.localBatchQueueResourcePolicies.splice(existingPolicyIndex, 1);
        }
      }
    },
    fetchComputeResource: function(id) {
      return DjangoAiravataAPI.utils.FetchUtils.get(
        "/api/compute-resources/" + encodeURIComponent(id) + "/"
      ).then(value => {
        return (this.computeResource = value);
      });
    },
    save: function() {
      let groupResourceProfile = this.localGroupResourceProfile.clone();
      groupResourceProfile.mergeComputeResourcePreference(
        this.data,
        this.localComputeResourcePolicy,
        this.localBatchQueueResourcePolicies
      );
      // TODO: success and error handling are the same so we can just combine those
      if (this.id) {
        DjangoAiravataAPI.services.GroupResourceProfileService
          .update({ data: groupResourceProfile, lookup: this.id })
          .then(groupResourceProfile => {
            // Navigate back to GroupResourceProfile with success message
            this.$router.push({
              name: "group_resource_preference",
              params: {
                value: groupResourceProfile,
                id: this.id
              }
            });
          });
      } else {
        DjangoAiravataAPI.services.GroupResourceProfileService
          .create({ data: groupResourceProfile })
          .then(groupResourceProfile => {
            // Navigate back to GroupResourceProfile with success message
            this.$router.push({
              name: "group_resource_preference",
              params: {
                value: groupResourceProfile,
                id: groupResourceProfile.groupResourceProfileId
              }
            });
          });
      }
    },
    remove: function() {
      let groupResourceProfile = this.localGroupResourceProfile.clone();
      const removedChildren = groupResourceProfile.removeComputeResource(
        this.host_id
      );
      if (removedChildren) {
        DjangoAiravataAPI.services.GroupResourceProfileService
          .update({ data: groupResourceProfile, lookup: this.id })
          .then(groupResourceProfile => {
            // Navigate back to GroupResourceProfile with success message
            this.$router.push({
              name: "group_resource_preference",
              params: {
                value: groupResourceProfile,
                id: this.id
              }
            });
          });
      } else {
        // Since nothing was removed, just handle this like a cancel
        this.cancel();
      }
    },
    cancel: function() {
      if (this.id) {
        this.$router.push({
          name: "group_resource_preference",
          params: { id: this.id }
        });
      } else {
        this.$router.push({
          name: "new_group_resource_preference",
          params: { value: this.localGroupResourceProfile }
        });
      }
    },
    createDefaultComputeResourcePolicy: function(computeResourcePromise) {
      computeResourcePromise.then(computeResource => {
        const defaultComputeResourcePolicy = new models.ComputeResourcePolicy();
        defaultComputeResourcePolicy.computeResourceId = this.host_id;
        defaultComputeResourcePolicy.groupResourceProfileId = this.id;
        defaultComputeResourcePolicy.allowedBatchQueues = computeResource.batchQueues.map(
          queue => queue.queueName
        );
        this.localComputeResourcePolicy = defaultComputeResourcePolicy;
      });
    }
  },
  beforeRouteEnter: function(to, from, next) {
    // If we don't have the Group Resource Profile id or instance, then the
    // Group Resource Profile wasn't created and we need to just go back to
    // the dashboard
    if (!to.params.id && !to.params.groupResourceProfile) {
      next({ name: "group_resource_preference_dashboard" });
    } else {
      next();
    }
  }
};
</script>

<style scoped>
.group-resource-profile-name {
  font-size: 12px;
}
</style>
