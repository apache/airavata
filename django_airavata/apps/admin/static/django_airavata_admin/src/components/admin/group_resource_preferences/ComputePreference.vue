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
            <input v-model="data.preferredBatchQueue" type="text"/>
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
            <div class="heading">Preferred Job Submission Protocol</div>
            <select v-model="data.preferredDataMovementProtocol">
              <option value="0">LOCAL</option>
              <option value="1">SCP</option>
              <option value="3">GridFTP</option>
              <option value="4">UNICORE_STORAGE_SERVICE</option>
            </select>
          </div>
          <div class="entry">
            <div class="heading">Preferred Job Submission Protocol</div>
            <select v-model="data.preferredJobSubmissionProtocol">
              <option value="0">Local</option>
              <option value="1">SSH</option>
              <option value="2">GLOBUS</option>
              <option value="3">UNICORE</option>
              <option value="4">Cloud</option>
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
      if (!data.computeResourcePolicies || data.computeResourcePolicies.length == 0) {
        data.computeResourcePolicies = [];
        data.computeResourcePolicies.push(this.createComputeResourcePolicy());
      }
      return {
        data: data,
        selected: null,
        computeResources: [],
        selectedComputeResourceIndex: null,
        computeResource: null
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
      }
    },
    watch: {
      selectedComputeResourceIndex: function (newValue) {
        DjangoAiravataAPI.utils.FetchUtils.get("/api/compute/resource/details", {id: this.computeResources[newValue].host_id}).then((value)=>{
          this.computeResource =value;
          this.data.computeResourceId=value.computeResourceId;
          this.data.computeResourcePolicies.forEach((value)=>{
            value.computeResourceId=this.data.computeResourceId;
          })
        });
      }
    }
  }
</script>

<style scoped>
  .batch-queue {
    margin-top: 10px;
  }
</style>
