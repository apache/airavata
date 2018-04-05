<template>
  <div class="new_app">
    <div class="new_app_header">
      <h4 style="display: inline-block">Add Group Resource Preferences</h4>
      <label v-on:click="newGroupResourcePreference()">New Application <span>+</span></label>
    </div>
    <div class="applications">
      <h6 style="color: #666666;">Group Resource Preferences</h6>
      <div class="container-fluid">
        <div class="row">
          <application-card v-for="preference in preferences" v-bind:app-module="transform(preference)"
                            v-bind:key="preference.groupResourceProfileId" v-on:app-selected="clickHandler(preference)">
          </application-card>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
  import {components as comps} from 'django-airavata-common-ui'

  export default {
    name: "compute-resource-preference",
    components: {
      'application-card': comps.ApplicationCard,
    },
    data: function () {
      return {
        preferences: [
          {
            groupResourceProfileId: 1,
            groupResourceProfileName: "Test1",
            creationTime: 101,
            updatedTime: 201,
            computePreferences: [{
              computeResourceId: 101,
              groupResourceProfileId: 1,
              overridebyAiravata: null,
              loginUserName: "hello",
              preferredJobSubmissionProtocol: null,
              preferredDataMovementProtocol: null,
              preferredBatchQueue: null,
              scratchLocation: null,
              allocationProjectNumber: null,
              resourceSpecificCredentialStoreToken: null,
              usageReportingGatewayId: null,
              qualityOfService: null,
              reservation: null,
              reservationStartTime: null,
              reservationEndTime: null,
              sshAccountProvisiogroupSSHAccountProvisionerConfigsner: null,
              sshAccountProvisionerAdditionalInfo: null,
              batchQueueResourcePolicies: []
            },
            {
              computeResourceId: 101,
              groupResourceProfileId: 2,
              overridebyAiravata: null,
              loginUserName: "hi",
              preferredJobSubmissionProtocol: null,
              preferredDataMovementProtocol: null,
              preferredBatchQueue: null,
              scratchLocation: null,
              allocationProjectNumber: null,
              resourceSpecificCredentialStoreToken: null,
              usageReportingGatewayId: null,
              qualityOfService: null,
              reservation: null,
              reservationStartTime: null,
              reservationEndTime: null,
              sshAccountProvisiogroupSSHAccountProvisionerConfigsner: null,
              sshAccountProvisionerAdditionalInfo: null,
              batchQueueResourcePolicies: []
            }],
            computeResourcePolicies: [{
              resourcePolicyId: 1001,
              computeResourceId: 101,
              groupResourceProfileId: 1,
              allowedBatchQueues: []
            }],
            batchQueueResourcePolicies: [{
              resourcePolicyId: 1001,
              computeResourceId: 101,
              groupResourceProfileId: 1,
              queuename: "cpu",
              maxAllowedNodes: null,
              maxAllowedCores: null,
              maxAllowedWalltime: null,
            }],
            groupResourceProfileName: null,
            creationTime: null,
            updatedTime: null
          }
        ]
      }
    },
    methods: {
      clickHandler: function (preference) {
        for (let computePreference of preference.computePreferences) {
          let groupResourceProfileId = computePreference.groupResourceProfileId;
          let computeResourceId = computePreference.computeResourceId;
          let computeResourcePolicies=[]
          console.log("Group Resource Profile ID, Compute Resource ID",groupResourceProfileId,computeResourceId)
          for (let computeResourcePolicy of preference.computeResourcePolicies) {
            let resourcePolicyId = computeResourcePolicy.resourcePolicyId;
            console.log("policy Group Resource Profile ID, Compute Resource ID Resource Policy",computeResourcePolicy.groupResourceProfileId,computeResourcePolicy.computeResourceId, resourcePolicyId)
            if (groupResourceProfileId == computeResourcePolicy.groupResourceProfileId && computeResourceId == computeResourcePolicy.computeResourceId) {
              let computeResourcePolicyTemp=computeResourcePolicy;
              let batchQueueResourcePolicies = [];
              for (let batchQueueResourcePolicy of preference.batchQueueResourcePolicies) {
                console.log("batch policy Group Resource Profile ID, Compute Resource ID Resource Policy",batchQueueResourcePolicy.groupResourceProfileId,batchQueueResourcePolicy.computeResourceId, batchQueueResourcePolicy.resourcePolicyId)
                if (groupResourceProfileId == batchQueueResourcePolicy.groupResourceProfileId && resourcePolicyId == batchQueueResourcePolicy.resourcePolicyId && computeResourceId == batchQueueResourcePolicy.computeResourceId) {
                  batchQueueResourcePolicies.push(batchQueueResourcePolicy);
                }
              }
              computeResourcePolicyTemp.batchQueueResourcePolicies=batchQueueResourcePolicies;
              computeResourcePolicies.push(computeResourcePolicyTemp);
            }
          }
          computePreference.computeResourcePolicies=computeResourcePolicies;
        }
        this.$router.push({
          name: 'group_resource_preference', params: {
            data: preference
          }
        });
      },
      newGroupResourcePreference: function () {
        this.$router.push({name: 'group_resource_preference'})
      },
      transform: function (preference) {
        return {
          appModuleName: preference.groupResourceProfileName,
          tags: ["creationTime: " + preference.creationTime, " updatedTime " + preference.updatedTime],
          appModuleVersion: null,
          appModuleDescription: null
        }
      }
    }
  }
</script>

<style scoped>
  .new_app {
    margin: 45px;
    width: 100%;
    background-color: white;
  }

  .new_app_header {
    width: 100%;
    display: inline;
  }

  .new_app_header label {
    background-color: #2e73bc;
    color: white;
    border: solid #2e73bc 1px;
    border-radius: 3px;
    float: right;
    padding-right: 15px;
    padding-left: 15px;
    padding-bottom: 8px;
    padding-top: 3px;
    text-align: center;
  }

  .new_app_header label:hover {
    cursor: pointer;
  }

  .new_app_header label span {
    font-weight: 900;
    font-size: 25px;
  }

  .applications {
    margin-top: 50px;
  }

  .ssh, .generate input {
    text-align: center;
  }
</style>
