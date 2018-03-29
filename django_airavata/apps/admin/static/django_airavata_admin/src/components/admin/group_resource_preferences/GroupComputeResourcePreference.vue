<template>
  <div class="new_app">
    <div class="new_app_header">
      <h3 style="display: inline-block">Group Resource Profiles</h3>
      <div class="new-application-tab-main">
        <div class="entry">
          <div class="heading">Group Resource Profile Name</div>
          <input type="text" v-model="data.groupResourceProfileName"/>
        </div>
        <div class="entry">
          <div class="heading">Group Resource Profile Name</div>
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
              {{computePreference.groupResourceProfileId}}
            </span>
            <span v-else>
              Un Saved Compute Preference {{index}}
            </span>

            <img v-on:click.stop="data.computePreferences.splice(index,1)" src="/static/images/delete.png"/>
          </a>
        </div>
        <div class="entry">
          <button class="interface-btn" v-on:click="createComputePreferenceClickHandler()">Add Compute <span>Preference</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
  import ComputePreference from './ComputePreference';
  import MultiSelectionDropDown from "../../commons/MultiSelectionDropDown";
  import SingleItemList from '../../commons/SingleItemList'
  import {components as comps} from 'django-airavata-common-ui'

  export default {
    name: "group-compute-resource-preference",
    props: {
      data: {
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
        selectedGroups: ""
      }
    },

    components: {
      MultiSelectionDropDown,
      ComputePreference,
      SingleItemList,
      "auto-complete": comps.AutoComplete,

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
      createComputeResourcePolicies: function () {

      }
      ,
      createBatchResourcePolicies: function () {

      }
      ,
      updateComputePreference: function (index) {
      },
      computePreferenceClickHandler: function (index) {
        this.$router.push({
          name: 'compute_preferences', params: {
            value: this.data,
            index: index
          }
        });
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

</style>
