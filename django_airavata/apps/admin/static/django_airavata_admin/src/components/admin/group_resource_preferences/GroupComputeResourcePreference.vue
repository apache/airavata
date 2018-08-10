<template>
  <div>
    <div class="row">
      <div class="col">
        <h1 class="h4 mb-4">Group Resource Profile</h1>
      </div>
    </div>
    <div class="row">
      <div class="col">
        <div class="card">
          <div class="card-body">
            <b-form-group label="Name" label-for="profile-name">
              <b-form-input id="profile-name" type="text"
                v-model="data.groupResourceProfileName"
                required placeholder="Name of this Group Resource Profile">
              </b-form-input>
            </b-form-group>
            <share-button ref="shareButton" v-model="sharedEntity"/>
          </div>
        </div>
      </div>
    </div>
    <list-layout :items="data.computePreferences" title="Compute Preferences"
      new-item-button-text="New Compute Preference"
      @add-new-item="createComputePreferences">
      <template slot="item-list" slot-scope="slotProps">

        <b-table hover :fields="computePreferencesFields" :items="slotProps.items"
          sort-by="computeResourceId">
          <template slot="action" slot-scope="data">
            <a href="#" @click.prevent="computePreferenceClickHandler(data.item.computeResourceId)">
              Edit
              <i class="fa fa-edit" aria-hidden="true"></i>
            </a>
          </template>
        </b-table> 
      </template>
    </list-layout>
  </div>
</template>
<script>
  import {components as comps, layouts} from 'django-airavata-common-ui'
  import DjangoAiravataAPI from 'django-airavata-api'

  export default {
    name: "group-compute-resource-preference",
    props: {
      value: {
        type: DjangoAiravataAPI.models.GroupResourceProfile,
        default: function () {
          return new DjangoAiravataAPI.models.GroupResourceProfile()
        }
      },
    },
    mounted: function () {
      if (this.value.groupResourceProfileId) {
        DjangoAiravataAPI.services.ServiceFactory.service("SharedEntities").retrieve({lookup: this.value.groupResourceProfileId})
          .then(sharedEntity => this.sharedEntity = sharedEntity);
      } else if (this.$route.params.id) {
        // TODO: switch to using props to get the id param
        DjangoAiravataAPI.services.ServiceFactory.service("GroupResourceProfiles").retrieve({lookup: this.$route.params.id})
          .then(grp => this.data = grp);
        DjangoAiravataAPI.services.ServiceFactory.service("SharedEntities").retrieve({lookup: this.$route.params.id})
          .then(sharedEntity => this.sharedEntity = sharedEntity);
      }
    },
    data: function () {
      let data = this.value.clone();
      return {
        data: data,
        service: DjangoAiravataAPI.services.ServiceFactory.service("GroupResourceProfiles"),
        sharedEntity: null,
        computePreferencesFields: [
          {
            label: 'Name',
            key: 'computeResourceId',
            sortable: true,
            formatter: (value) => this.getComputeResourceName(value),
          },
          {
            label: 'Action',
            key: 'action',
          },
        ],
      }
    },

    components: {
      "share-button": comps.ShareButton,
      "list-layout": layouts.ListLayout,
    },
    methods: {
      saveGroupResourceProfile: function () {
        if (this.data.groupResourceProfileId) {
          DjangoAiravataAPI.utils.FetchUtils.put('/api/group-resource-profiles/' + this.data.groupResourceProfileId + '/', this.data)
            .then((data) => this.data = data);
        } else {
          this.service.create({data: this.data})
            .then((data) => {
              this.data = data;
              // Save sharing settings too
              return this.$refs.shareButton.mergeAndSave(data.groupResourceProfileId);
            });
        }
      },
      computePreferenceClickHandler: function (computeResourceId) {
        let computeResourcePreference = this.data.computePreferences.find(pref => pref.computeResourceId === computeResourceId);
        const computeResourcePolicy = this.data.getComputeResourcePolicy(computeResourceId);
        const batchQueueResourcePolicies = this.data.getBatchQueueResourcePolicies(computeResourceId);
        this.$router.push({
          name: 'compute_preference', params: {
            value: computeResourcePreference,
            id: this.data.groupResourceProfileId,
            host_id: computeResourceId,
            computeResourcePolicy: computeResourcePolicy,
            batchQueueResourcePolicies: batchQueueResourcePolicies,
          }
        });
      },
      getComputeResourceName: function (computeResourceId) {
        // TODO: load compute resources to get the real name
        return (computeResourceId && computeResourceId.indexOf("_") > 0) ? computeResourceId.split("_")[0] : computeResourceId;
      },
    },
  }
</script>