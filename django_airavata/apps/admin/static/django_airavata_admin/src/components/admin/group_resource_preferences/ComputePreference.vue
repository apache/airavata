<template>
  <transition name="fade">
    <div class="new_app">
      <div class="new_app_header">
        <h3 style="display: inline-block">Compute Preference</h3>
        <div class="new-application-tab-main">
          <div class="entry" v-if="newCreation">
            <div class="heading">Select Compute Resource</div>
            <select v-model="selectedComputeResourceIndex">
              <option v-bind:value="index" v-for="computeResource,index in computeResources">{{computeResource.host}}
              </option>
            </select>
          </div>
          <div class="entry">
            <div class="heading">Login User Name</div>
            <input v-model="data.loginUserName" type="text"/>
          </div>
          <div class="entry">
            <div class="heading">Preferred Batch Queue</div>
            <select v-model="data.preferredBatchQueue">
              <option v-bind:value="batchQueue.queueName" v-for="batchQueue,index in computeResource.batchQueues"
                      v-bind:key="index">{{batchQueue.queueName}}
              </option>
            </select>
          </div>
          <div class="entry">
            <div class="heading">Scratch Location</div>
            <input v-model="data.scratchLocation" type="text"/>
          </div>
          <div class="entry">
            <div class="heading">Allocation Project Number</div>
            <input v-model="data.allocationProjectNumber" type="text"/>
          </div>
          <div class="entry">
            <div class="heading">Resource Specific Credential Store Token</div>
            <input v-model="data.resourceSpecificCredentialStoreToken" type="text"/>
          </div>
          <div class="entry">
            <div class="heading">Usage Reporting Gateway ID</div>
            <input v-model="data.usageReportingGatewayId" type="text"/>
          </div>
          <div class="entry">
            <div class="heading">Quality of Service</div>
            <input v-model="data.qualityOfService" type="text"/>
          </div>
          <div class="entry">
            <div class="heading">Application argument</div>
            <input v-model="data.reservation" type="text"/>
          </div>
          <div class="entry">
            <div class="heading">SSH Account Provision Group / SSH Account Provisioner</div>
            <input v-model="data.sshAccountProvisiogroupSSHAccountProvisionerConfigsner" type="text"/>
          </div>
          <div class="entry">
            <div class="heading">SSH Account Provisioner Additional Info</div>
            <input v-model="data.sshAccountProvisionerAdditionalInfo" type="text"/>
          </div>
          <div class="entry">
            <div class="heading">Preferred Data Movement Protocol</div>
            <select v-model="data.preferredDataMovementProtocol">
              <option v-bind:value="dataMovementProtocol.value" v-for="dataMovementProtocol in dataMovementProtocols" v-if="dataMovementProtocol.enabled" v-bind:key="index">{{dataMovementProtocol.name}}</option>
            </select>
          </div>
          <div class="entry">
            <div class="heading">Preferred Job Submission Protocol</div>
            <select v-model="data.preferredJobSubmissionProtocol">
              <option v-bind:value="jobSubmissionProtocol.value" v-for="jobSubmissionProtocol in jobSubmissionProtocols" v-if="jobSubmissionProtocol.enabled" v-bind:key="index">{{jobSubmissionProtocol.name}}</option>
            </select>
          </div>
          <div class="sub-section-1">
            <h4>Compute Resource Policies</h4>
            <tab-sub-section v-for="computeResourcePolicy,index in data.computeResourcePolicies" v-bind:key="index"
                             v-bind:enableDeletion="false" v-bind:section-name="'Compute Resource Policy'">
              <compute-resource-policy v-model="data.computeResourcePolicies[index]"></compute-resource-policy>
            </tab-sub-section>
          </div>
        </div>
      </div>
    </div>
  </transition>
</template>

<script>

  import BooleanRadioButton from '../BooleanRadioButton'
  import TabSubSection from '../../tabs/TabSubSection'
  import DjangoAiravataAPI from 'django-airavata-api'

  import ComputeResourcePolicy from "./ComputeResourcePolicy";
  import VModelMixin from '../../commons/vmodel_mixin'

  export default {
    name: "compute-preference",
    components: {
      ComputeResourcePolicy,
      BooleanRadioButton,
      TabSubSection,
    },
    props: {
      newCreation: {
        type: Boolean,
        default: false
      }
    },
    mounted: function () {
      this.fetchComputeResources().then((value) => this.computeResources = value);
    },
    data: function () {
      let data = this.value;
      if(data.computeResourceId){
        this.fetchComputeResource(data.computeResourceId);
      }
      if (!data.computeResourcePolicies || data.computeResourcePolicies.length == 0) {
        data.computeResourcePolicies = [];
        data.computeResourcePolicies.push(this.createComputeResourcePolicy());
      }
      return {
        data: data,
        selected: null,
        computeResources: [],
        selectedComputeResourceIndex: null,
        dataMovementProtocols:[  {
            name:"LOCAL",
            enabled:false,
            value:0
          },  {
            name:"SCP",
            enabled:false,
            value:1
          },  {
            name:"GridFTP",
            enabled:false,
            value:2
          },  {
            name:"UNICORE_STORAGE_SERVICE",
            enabled:false,
            value:3
          },],
        jobSubmissionProtocols:[
          {
            name:"Local",
            enabled:false,
            value:0
          },
          {
            name:"SSH",
            enabled:false,
            value:1
          },
          {
            name:"GLOBUS",
            enabled:false,
            value:2
          },
          {
            name:"UNICORE",
            enabled:false,
            value:3
          },
          {
            name:"Cloud",
            enabled:false,
            value:4
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
      boolValueHandler: function (id, value) {
        this.data.overridebyAiravata = value
      },
      fetchComputeResources: function () {
        return DjangoAiravataAPI.utils.FetchUtils.get('/api/compute/resources');
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
      fetchComputeResource:function (id) {
        if (id && this.computeResources && this.computeResources.length > 0) {
          DjangoAiravataAPI.utils.FetchUtils.get("/api/compute/resource/details", {id: id}).then((value) => {
            this.computeResource = value;
            this.computeResource.jobSubmissionInterfaces.forEach((jobSubmissionInterface)=>{
              this.jobSubmissionProtocols[jobSubmissionInterface.jobSubmissionProtocol].enabled=true;
            });
            this.computeResource.dataMovementInterfaces.forEach((dataMovementInterface)=>{
              this.dataMovementProtocols[dataMovementInterface.dataMovementProtocol].enabled=true;
            });
            this.data.computeResourceId = value.computeResourceId;
            this.data.computeResourcePolicies.forEach((value) => {
              value.computeResourceId = this.data.computeResourceId;
            })
          });
        }
      }
    },
    watch: {
      selectedComputeResourceIndex: function (newValue) {
       if(newValue){
          this.fetchComputeResource(this.computeResources[index].host_id);
       }
      }
    }
  }
</script>

<style scoped>
  .batch-queue {
    margin-top: 10px;
  }
</style>
