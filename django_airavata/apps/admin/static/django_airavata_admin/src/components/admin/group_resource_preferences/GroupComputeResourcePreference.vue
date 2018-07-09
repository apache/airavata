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
                         v-bind:suggestions="groups"></auto-complete>
        </div>
        <share-button v-if="sharedEntity" v-model="sharedEntity"/>
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
          <button class="interface-btn" v-on:click="createComputePreferenceClickHandler()">Add Compute
            <span>Preference</span>
          </button>
        </div>
        <tab-action-console v-bind:sectionName="'Group Resource Profile'" v-bind:save="saveGroupResourceProfile"
                            v-bind:enableCancel="false"></tab-action-console>
      </div>
    </div>
  </div>
</template>
<script>
  const ComputePreference = () => import('./ComputePreference');
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
            groupResourceProfileId: null
          }
        }
      },
      newCreation: {
        type: Boolean,
        default: false
      },
      transform: {
        type: Boolean,
        default: true
      }
    },
    mounted: function () {
      this.fetchGroups().then((value => {
        this.groups = value.results;
      }));
      this.fetchGroup(this.value.groupResourceProfileId);
      if (this.value.groupResourceProfileId) {
        DjangoAiravataAPI.services.ServiceFactory.service("SharedEntities").retrieve({lookup: this.value.groupResourceProfileId})
          .then(sharedEntity => this.sharedEntity = sharedEntity);
      }
    },
    data: function () {
      let data = Object.assign({},this.value);
      if (this.transform) {
        data = this.transformData(data);
      }
      return {
        selectedGroups: [],
        data: data,
        service: DjangoAiravataAPI.services.ServiceFactory.service("GroupResourcePreference"),
        groups: [],
        sharedEntity: null,

      }
    },

    components: {
      ComputePreference,
      "auto-complete": comps.Autocomplete,
      TabActionConsole,
      "share-button": comps.ShareButton,
    },
    methods: {
      transformData: function (groupResourceProfile) {
        let computePreferences=groupResourceProfile.computePreferences;
        console.log("Transform Compute prefernces",computePreferences.length,groupResourceProfile);
        for (let computePreference of computePreferences) {
          let groupResourceProfileId = computePreference.groupResourceProfileId;
          let computeResourceId = computePreference.computeResourceId;
          let computeResourcePolicies = []
          console.log("Transforming   Group Resource Profile ID, Compute Resource ID", groupResourceProfileId, computeResourceId)
          for (let computeResourcePolicy of groupResourceProfile.computeResourcePolicies) {
            let resourcePolicyId = computeResourcePolicy.resourcePolicyId;
            console.log("policy Group Resource Profile ID, Compute Resource ID Resource Policy", computeResourcePolicy.groupResourceProfileId, computeResourcePolicy.computeResourceId, resourcePolicyId)
            if (groupResourceProfileId == computeResourcePolicy.groupResourceProfileId && computeResourceId == computeResourcePolicy.computeResourceId) {
              let computeResourcePolicyTemp = computeResourcePolicy;
              let batchQueueResourcePolicies = [];
              for (let batchQueueResourcePolicy of groupResourceProfile.batchQueueResourcePolicies) {
                console.log("batch policy Group Resource Profile ID, Compute Resource ID Resource Policy", batchQueueResourcePolicy.groupResourceProfileId, batchQueueResourcePolicy.computeResourceId, batchQueueResourcePolicy.resourcePolicyId)
                if (groupResourceProfileId == batchQueueResourcePolicy.groupResourceProfileId && computeResourceId == batchQueueResourcePolicy.computeResourceId) {
                  batchQueueResourcePolicies.push(batchQueueResourcePolicy);
                }
              }
              console.log("Batch Queue Rsource Policies for", batchQueueResourcePolicies.length, computeResourcePolicy.computeResourceId);
              computeResourcePolicyTemp.batchQueueResourcePolicies = batchQueueResourcePolicies;
              computeResourcePolicies.push(computeResourcePolicyTemp);
            }
          }
          computePreference.computeResourcePolicies = computeResourcePolicies;
        }
        groupResourceProfile.computePreferences=computePreferences;
        return groupResourceProfile;
      },
      createComputePreferences: function () {
        let computeResourcePreference = {
          computeResourceId: null,
          groupResourceProfileId: null,
          overridebyAiravata: true,
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
          groupSSHAccountProvisionerConfigs: [],
          computeResourcePolicies: []
        };
        this.data.computePreferences.push(computeResourcePreference);
      }
      ,
      createComputePreferenceClickHandler: function () {
        this.createComputePreferences();
        this.computePreferenceClickHandler(this.data.computePreferences.length - 1);
      },
      saveGroupResourceProfile: function (callback) {
        let groupResourceProfile = Object.assign({}, this.data);
        let computePreferences = groupResourceProfile.computePreferences;
        let batchQueueResourcePolicies = [];
        let computeResourcePolicies = [];
        if (computePreferences) {
          for (let computePreference of computePreferences) {
            computePreference.groupResourceProfileId = groupResourceProfile.groupResourceProfileId;
            if (!computePreference.computeResourcePolicies) {
              console.log("Compute Resource Policies empty", computePreference);
            }
            for (let computeResourcePolicy of computePreference.computeResourcePolicies) {
              if (!computeResourcePolicy.batchQueueResourcePolicies) {batchQueueResourcePolicies
                console.log("batchQueueResourcePolicies empty", computePreference);
              }
              computeResourcePolicy.groupResourceProfileId=groupResourceProfile.groupResourceProfileId;
              for (let batchQueueResourcePolicy of computeResourcePolicy.batchQueueResourcePolicies) {
                batchQueueResourcePolicy.computeResourceId = computePreference.computeResourceId;
                batchQueueResourcePolicy.groupResourceProfileId=groupResourceProfile.groupResourceProfileId;
                batchQueueResourcePolicies.push(batchQueueResourcePolicy);
              }
              delete computeResourcePolicy.batchQueueResourcePolicies;
              computeResourcePolicies.push(computeResourcePolicy);
            }
            delete computePreference.computeResourcePolicies;
            delete computePreference.batchQueueResourcePolicies;
          }
        } else {
          groupResourceProfile.computePreferences = [];
        }
        groupResourceProfile.computeResourcePolicies = computeResourcePolicies;
        groupResourceProfile.batchQueueResourcePolicies = batchQueueResourcePolicies;
        console.log("Saving..", groupResourceProfile);
        if (this.data.groupResourceProfileId) {
          DjangoAiravataAPI.utils.FetchUtils.put('/api/group-resource-profiles/' + this.data.groupResourceProfileId + '/', groupResourceProfile)
            .then(callback.failure).then((data) => {
            console.log("Completed")
            if (data) {
              this.data = this.transformData(data);
              this.allowGroups();
            }
          });
        } else {
          this.service.create({data: groupResourceProfile}).then(callback.success, callback.failure).then((data) => {
            console.log("Completed")
            if (data) {
              this.data = this.transformData(data);
              this.allowGroups();
            }
          });
        }
      },
      fetchGroups: function () {
        return DjangoAiravataAPI.services.GroupService.list()
      },
      fetchGroup: function (groupResourceProfileId) {
        if (groupResourceProfileId) {
          DjangoAiravataAPI.services.ServiceFactory.service('SharedEntitiesGroups').retrieve({lookup: groupResourceProfileId}).then((groups) => {
            console.log("Selected Groups", groups);
            this.selectedGroups = groups.groupList;
          });
        }
      },
      computePreferenceClickHandler: function (index) {
        this.$router.push({
          name: 'compute_preferences', params: {
            value: this.data,
            index: index,
            newCreation: this.newCreation
          }
        });
      },
      getComputeResourceName: function (computeResourceId) {
        // TODO: load compute resources to get the real name
        return (computeResourceId && computeResourceId.indexOf("_") > 0) ? computeResourceId.split("_")[0] : computeResourceId;
      },
      allowGroups: function () {
        return DjangoAiravataAPI.services.ServiceFactory.service('SharedEntitiesGroups').update({
          lookup: this.data.groupResourceProfileId,
          data: {
            groupList: this.selectedGroups,
            entityId: this.data.groupResourceProfileId
          }
        }).then((groups) => {
          console.log("Selected Groups", groups);
            this.selectedGroups = groups.groupList;
            return
        }, (response) => console.log("Failed Resp ", response));
      },
      fetchAllowedGroups: function () {
      }
    },
    watch: {
      'data.groupResourceProfileId': function (newValue) {
        let computePreferences = groupResourceProfile.computePreferences;
        for (let computePreference of computePreferences) {
          computePreference.groupResourceProfileId = newValue;
          for (let groupSSHAccountProvisionerConfig of computePreference.groupSSHAccountProvisionerConfigs) {
            groupSSHAccountProvisionerConfig.groupResourceProfileId = newValue;
          }
        }
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

  .list-item:hover span {
    color: white;
  }

  .un-saved {
    color: red;
  }

</style>
