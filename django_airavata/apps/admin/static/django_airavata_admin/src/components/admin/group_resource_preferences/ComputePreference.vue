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
              <div v-for="batchQueue in computeResource.batchQueues" :key="batchQueue.queueName">
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
        this.fetchComputeResource(this.host_id);
      } else {
        this.fetchComputeResources();
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
        dataMovementProtocols: [{
          name: "LOCAL",
          enabled: false,
          value: 0
        }, {
          name: "SCP",
          enabled: false,
          value: 1
        }, {
          name: "GridFTP",
          enabled: false,
          value: 2
        }, {
          name: "UNICORE_STORAGE_SERVICE",
          enabled: false,
          value: 3
        },],
        jobSubmissionProtocols: [
          {
            name: "Local",
            enabled: false,
            value: 0
          },
          {
            name: "SSH",
            enabled: false,
            value: 1
          },
          {
            name: "GLOBUS",
            enabled: false,
            value: 2
          },
          {
            name: "UNICORE",
            enabled: false,
            value: 3
          },
          {
            name: "Cloud",
            enabled: false,
            value: 4
          },
        ],
        computeResource: {
          batchQueues: [],
          jobSubmissionInterfaces: []
        }
      }
    },
    mixins: [VModelMixin],
    methods: {
      fetchComputeResources: function () {
        return DjangoAiravataAPI.utils.FetchUtils.get('/api/compute-resources/all_names_list').then((value) => this.computeResources = value);
      },
      createComputeResourcePolicy: function () {
        return {
          allowedBatchQueues: [],
          batchQueueResourcePolicies: [],
          computeResourceId: null,
          groupResourceProfileId: null,
          resourcePolicyId: null
        }
      },
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
            this.localBatchQueueResourcePolicies.push(batchQueueResourcePolicy);
          }
        } else {
          const existingPolicyIndex = this.localBatchQueueResourcePolicies.findIndex(pol => pol.queuename === queueName);
          if (existingPolicyIndex >= 0) {
            this.localBatchQueueResourcePolicies.splice(existingPolicyIndex, 1);
          }
        }
      },
      createGroupSSHAccountProvisionerConfigs: function () {
        this.data.groupSSHAccountProvisionerConfigs.push({
          resourceId: this.data.computeResourceId,
          groupResourceProfileId: this.data.groupResourceProfileId,
          configName: null,
          configValue: null
        });
      },
      fetchComputeResource: function (id) {
        console.log("Fetching compute Resource", id);
        if (id) {
          DjangoAiravataAPI.utils.FetchUtils.get("/api/compute-resources/" + encodeURIComponent(id) + "/").then(value => {
            console.log("Compute  Resource", value);
            this.computeResource = value;
            this.computeResource.jobSubmissionInterfaces.forEach((jobSubmissionInterface) => {
              this.jobSubmissionProtocols[jobSubmissionInterface.jobSubmissionProtocol].enabled = true;
            });
            this.computeResource.dataMovementInterfaces.forEach((dataMovementInterface) => {
              this.dataMovementProtocols[dataMovementInterface.dataMovementProtocol].enabled = true;
            });
          });
        }
      },
      continueHandler: function () {
        this.enablePopup = false;
        if (this.computeResources && this.computeResources.length > 0) {
          this.fetchComputeResource(this.computeResources[this.selectedComputeResourceIndex].host_id);
        }
      }
    },
    watch: {}
  }
</script>

<style scoped>
  .batch-queue {
    margin-top: 10px;
  }

  .popup-select {
    height: 100%;
  }
</style>
