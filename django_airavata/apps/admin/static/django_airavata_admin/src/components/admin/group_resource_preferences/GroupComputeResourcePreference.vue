<template>
  <div class="new_app">
    <div class="new_app_header">
      <h3 style="display: inline-block">Group Resource Profile</h3>
      <div class="new-application-tab-main">
        <div class="entry">
          <div class="heading">Name</div>
          <input type="text" v-model="data.groupResourceProfileName"/>
        </div>
        <div class="entry">
          <div class="heading">Groups</div>
          <auto-complete v-model="selectedGroups"
                         v-bind:suggestions="[{name:'hello',id:0},{name:'hi',id:1}]"></auto-complete>
        </div>
      </div>
      <div class="new-application-tab-main">
        <h4>Compute Preferences</h4>
        <div>
          <a class="list-item" v-for="computePreference,index in data.computePreferences" v-bind:key="index"
             v-on:click="computePreferenceClickHandler(index)">
            <span v-if="computePreference.groupResourceProfileId">
              {{getComputeResourceName(computePreference.computeResourceId)}}
            </span>
            <span v-else class="un-saved">
              Un Saved Compute Preference {{index}}
            </span>

            <img v-on:click.stop="data.computePreferences.splice(index,1)" src="/static/images/delete.png"/>
          </a>
        </div>
        <div class="entry">
          <button class="interface-btn" v-on:click="createComputePreferenceClickHandler()">Add Compute <span>Preference</span>
          </button>
        </div>
        <tab-action-console v-bind:save="saveGroupResourceProfile" v-bind:enableCancel="false"></tab-action-console>
      </div>
    </div>
  </div>
</template>
<script>
  const ComputePreference= ()=>import('./ComputePreference') ;
  import TabActionConsole from '../TabActionConsole'
  import {components as comps} from 'django-airavata-common-ui'
  import DjangoAiravataAPI from 'django-airavata-api'

  export default {
    name: "group-compute-resource-preference",
    props: {
      value: {
        type: Object,
        default: function () {
          return {
            computePreferences: [],
            computeResourcePolicies: [],
            batchQueueResourcePolicies: [],
            groupResourceProfileName: null,
            creationTime: null,
            updatedTime: null,
          }
        }
      }
    },
    data: function () {
      return {
        selectedGroups: [],
        data:this.value,
        service:DjangoAiravataAPI.services.ServiceFactory.service("GroupResourcePreference")
      }
    },

    components: {
      ComputePreference,
      "auto-complete": comps.Autocomplete,
      TabActionConsole

    },
    methods: {
      createComputePreferences: function () {
        let computeResourcePreference = {
          computeResourceId: null,
          groupResourceProfileId: null,
          overridebyAiravata: null,
          loginUserName: null,
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
          computeResourcePolicies: []
        };
        this.data.computePreferences.push(computeResourcePreference);
      }
      ,
      createComputePreferenceClickHandler:function () {
        this.createComputePreferences();
        this.computePreferenceClickHandler(this.data.computePreferences.length-1);
      },
      saveGroupResourceProfile: function () {
        let groupResourceProfile=Object.assign({},this.data);
        let computePreferences=groupResourceProfile.computePreferences;
        let batchQueueResourcePolicies=[];
        let computeResourcePolicies=[];
        for(let computePreference of computePreferences){
          for(let computeResourcePolicy of computePreferences.computeResourcePolicies){
            for(let batchQueueResourcePolicy of computeResourcePolicy.batchQueueResourcePolicies){
              batchQueueResourcePolicies.push(batchQueueResourcePolicy);
            }
            delete computeResourcePolicy.batchQueueResourcePolicies;
            computeResourcePolicies.push(computeResourcePolicy);
          }
          delete computePreference.computeResourcePolicies;
          delete computePreference.batchQueueResourcePolicies;
        }
        groupResourceProfile.computeResourcePolicies=computeResourcePolicies;
        groupResourceProfile.batchQueueResourcePolicies=batchQueueResourcePolicies;
        if(computePreferences.groupResourceProfileId){
          this.services.update({groupResourceProfile:groupResourceProfile});
        }else{
          this.services.create(groupResourceProfile);
        }
      },
      computePreferenceClickHandler: function (index) {
        this.$router.push({
          name: 'compute_preferences', params: {
            value: this.data,
            index: index
          }
        });
      },
      getComputeResourceName: function (computeResourceId) {
        // TODO: load compute resources to get the real name
        return (computeResourceId && computeResourceId.indexOf("_") > 0) ? computeResourceId.split("_")[0] : computeResourceId;
      }
    }
  }
</script>

<style scoped>
  .list-item {
    color: #007BFF;
    border: solid 1px #007BFF;
    background-color: white;
    border-top: none;
    text-align: center;
    padding-top: 5px;
    padding-bottom: 5px;
    padding-left: 15px;
    padding-right: 15px;
    display: block;

  }

  .list-item:first-child {
    border-top-left-radius: 4px;
    border-top-right-radius: 4px;
    border-top: solid 1px #007BFF;
  }

  .list-item:last-child {
    border-bottom-left-radius: 4px;
    border-bottom-right-radius: 4px;
  }

  .list-item img {
    float: right;
  }

  .list-item:hover {
    color: white;
    background-color: #007BFF;
    cursor: pointer;
  }
  .list-item:hover span{
    color: white;
  }

  .un-saved{
    color: red;
  }

</style>
