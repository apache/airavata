<template>
  <div>
    <div class="row">
      <div class="col">
        <h1 class="h4 mb-4">Compute Preference</h1>
      </div>
    </div>
    <div class="row">
      <div class="col">
        <div class="card">
          <div class="card-body">
            <b-form-group label="Login Username" label-for="login-username">
              <b-form-input id="login-username" type="text"
                v-model="data.loginUserName">
              </b-form-input>
            </b-form-group>
            <b-form-group label="Allocation Project Number" label-for="allocation-number">
              <b-form-input id="allocation-number" type="text"
                v-model="data.allocationProjectNumber">
              </b-form-input>
            </b-form-group>
            <b-form-group label="Scratch Location" label-for="scratch-location">
              <b-form-input id="scratch-location" type="text"
                v-model="data.scratchLocation">
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
              <div v-for="batchQueue in computeResource.batchQueues" :key="batchQueue.queueName"
                  v-if="localComputeResourcePolicy">
                <b-form-checkbox :checked="localComputeResourcePolicy.allowedBatchQueues.includes(batchQueue.queueName)"
                  @input="batchQueueChecked(batchQueue, $event)">
                  {{ batchQueue.queueName }}
                </b-form-checkbox>
                <batch-queue-resource-policy
                  v-if="localComputeResourcePolicy.allowedBatchQueues.includes(batchQueue.queueName)"
                  :batch-queue="batchQueue"
                  :value="localBatchQueueResourcePolicies.find(pol => pol.queuename === batchQueue.queueName)"
                  @input="updatedBatchQueueResourcePolicy(batchQueue, $event)"/>
              </div>
            </b-form-group>
          </div>
        </div>
      </div>
    </div>
    <div class="row">
        <div class="col d-flex justify-content-end">
            <b-button variant="primary" @click="save">Save</b-button>
            <b-button class="ml-2" variant="secondary" @click="cancel">Cancel</b-button>
        </div>
    </div>
  </div>
</template>

<script>

  import DjangoAiravataAPI from 'django-airavata-api'
  import VModelMixin from '../../commons/vmodel_mixin'
  import BatchQueueResourcePolicy from './BatchQueueResourcePolicy.vue'

  import {models} from 'django-airavata-api'

  export default {
    name: "compute-preference",
    components: {
      BatchQueueResourcePolicy,
    },
    props: {
      id: {
        type: String,
      },
      host_id: {
        type: String,
      },
      groupResourceProfile: {
        type: models.GroupResourceProfile,
      },
      computeResourcePolicy: {
        type: models.ComputeResourcePolicy
      },
      batchQueueResourcePolicies: {
        type: Array
      }
    },
    mounted: function () {
      if (!this.value && this.id && this.host_id) {
        // TODO: load the Group Resource Profile and get the compute preferences for this host_id
      }
      if (this.host_id) {
        const computeResourceOperation = this.fetchComputeResource(this.host_id);
        // If no computeResourcePolicy create a new default one that allows all queues
        if (!this.computeResourcePolicy) {
          computeResourceOperation.then(computeResource => {
            const defaultComputeResourcePolicy = new models.ComputeResourcePolicy();
            defaultComputeResourcePolicy.computeResourceId = this.host_id;
            defaultComputeResourcePolicy.groupResourceProfileId = this.id;
            defaultComputeResourcePolicy.allowedBatchQueues = computeResource.batchQueues.map(queue => queue.queueName);
            this.localComputeResourcePolicy = defaultComputeResourcePolicy;
          })
        }
      }
    },
    data: function () {
      return {
        data: this.value.clone(),
        selected: null,
        computeResources: [],
        selectedComputeResourceIndex: null,
        localComputeResourcePolicy: this.computeResourcePolicy ? this.computeResourcePolicy.clone() : null,
        localBatchQueueResourcePolicies: this.batchQueueResourcePolicies ? this.batchQueueResourcePolicies.map(pol => pol.clone()) : [],
        computeResource: {
          batchQueues: [],
          jobSubmissionInterfaces: []
        }
      }
    },
    mixins: [VModelMixin],
    methods: {
      batchQueueChecked: function(batchQueue, checked) {
        if (checked) {
          this.localComputeResourcePolicy.allowedBatchQueues.push(batchQueue.queueName);
        } else {
          const queueIndex = this.localComputeResourcePolicy.allowedBatchQueues.indexOf(batchQueue.queueName);
          this.localComputeResourcePolicy.allowedBatchQueues.splice(queueIndex, 1);
          // Remove batchQueueResourcePolicy if it exists
          const policyIndex = this.localBatchQueueResourcePolicies.findIndex(pol => pol.queuename === batchQueue.queueName);
          if (policyIndex >= 0) {
            this.localBatchQueueResourcePolicies.splice(policyIndex, 1);
          }
        }
      },
      updatedBatchQueueResourcePolicy: function(batchQueue, batchQueueResourcePolicy) {
        const queueName = batchQueue.queueName;
        if (batchQueueResourcePolicy) {
          const existingPolicy = this.localBatchQueueResourcePolicies.find(pol => pol.queuename === queueName);
          if (existingPolicy) {
            Object.assign(existingPolicy, batchQueueResourcePolicy);
          } else {
            // For new BatchQueueResourcePolicy instances, set the parent ids
            batchQueueResourcePolicy.groupResourceProfileId = this.id;
            batchQueueResourcePolicy.computeResourceId = this.host_id;
            this.localBatchQueueResourcePolicies.push(batchQueueResourcePolicy);
          }
        } else {
          const existingPolicyIndex = this.localBatchQueueResourcePolicies.findIndex(pol => pol.queuename === queueName);
          if (existingPolicyIndex >= 0) {
            this.localBatchQueueResourcePolicies.splice(existingPolicyIndex, 1);
          }
        }
      },
      fetchComputeResource: function (id) {
        return DjangoAiravataAPI.utils.FetchUtils.get("/api/compute-resources/" + encodeURIComponent(id) + "/").then(value => {
          return this.computeResource = value;
        });
      },
      save: function() {
        let groupResourceProfile = this.groupResourceProfile.clone();
        groupResourceProfile.mergeComputeResourcePreference(this.data, this.localComputeResourcePolicy, this.localBatchQueueResourcePolicies);
        // TODO: success and error handling are the same so we can just combine those
        if (this.id) {
          DjangoAiravataAPI.services.ServiceFactory.service("GroupResourceProfiles").update({data: groupResourceProfile, lookup: this.id})
            .then(groupResourceProfile => {
              // Navigate back to GroupResourceProfile with success message
              this.$router.push({
                name: 'group_resource_preference', params: {
                  value: groupResourceProfile,
                  id: this.id
                }
              });
            })
            .catch(error => {
              // TODO: handle error
              console.log("Error occurred", error);
            });
        } else {
          DjangoAiravataAPI.services.ServiceFactory.service("GroupResourceProfiles").create({data: groupResourceProfile})
            .then(groupResourceProfile => {
              // Navigate back to GroupResourceProfile with success message
              this.$router.push({
                name: 'group_resource_preference', params: {
                  value: groupResourceProfile,
                  id: groupResourceProfile.groupResourceProfileId
                }
              });
            })
            .catch(error => {
              // TODO: handle error
              console.log("Error occurred", error);
            });
        }
      },
      cancel: function() {
        if (this.id) {
          this.$router.push({ name: 'group_resource_preference', params: {id: this.id}});
        } else {
          this.$router.push({ name: 'new_group_resource_preference', params: {value: this.groupResourceProfile}});
        }
      }
    },
    beforeRouteEnter: function(to, from, next) {
      // If we don't have the Group Resource Profile id or instance, then the
      // Group Resource Profile wasn't created and we need to just go back to
      // the dashboard
      if (!to.params.id && !to.params.groupResourceProfile) {
        next({name: 'group_resource_preference_dashboard'});
      } else {
        next();
      }
    }
  }
</script>