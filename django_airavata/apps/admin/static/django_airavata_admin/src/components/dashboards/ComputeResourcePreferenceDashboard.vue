<template>
  <div class="new_app">
    <div class="new_app_header">
      <h4 style="display: inline-block">Group Resource Profiles</h4>
      <label v-on:click="newGroupResourcePreference()">New Group Resource Profile <span>+</span></label>
    </div>
    <div class="applications">
      <h6 style="color: #666666;">Group Resource Profile</h6>
      <div class="container-fluid">
        <div class="row">
          <application-card v-for="groupResourceProfile in groupResourceProfiles"
                            v-bind:app-module="transform(groupResourceProfile)"
                            v-bind:key="groupResourceProfile.groupResourceProfileId"
                            v-on:app-selected="clickHandler(groupResourceProfile)">
          </application-card>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
  import {components as comps} from 'django-airavata-common-ui'
  import {services} from 'django-airavata-api'

  export default {
    name: "compute-resource-preference",
    components: {
      'application-card': comps.ApplicationCard,
    },
    data: function () {
      return {
        groupResourceProfiles: [],
      }
    },
    methods: {
      clickHandler: function (groupResourceProfile) {
        this.$router.push({
          name: 'group_resource_preference', params: {
            value: groupResourceProfile
          }
        });
      },
      newGroupResourcePreference: function () {
        this.$router.push({
          name: 'group_resource_preference', params: {
            newCreation: true
          }
        })
      },
      transform: function (preference) {
        let tags=["Created On " + new Date(preference.creationTime).toDateString()];
        if(preference.creationTime !==preference.updatedTime){
          tags.push("Updated On " + new Date(preference.updatedTime).toDateString());
        }
        return {
          appModuleName: preference.groupResourceProfileName,
          tags: tags,
          appModuleVersion: null,
          appModuleDescription: null
        }
      },
      loadGroupResourceProfiles: function () {
        services.GroupResourceProfileService.list()
          .then(groupResourceProfiles => {
            this.groupResourceProfiles = groupResourceProfiles;
          });
      },
    },
    mounted: function () {
      this.loadGroupResourceProfiles();
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

  .new_app_header > label {
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
