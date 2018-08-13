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
      @add-new-item="createComputePreference">
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
    <div class="row">
        <div class="col d-flex justify-content-end">
            <b-button variant="primary" @click="saveGroupResourceProfile">Save</b-button>
            <b-button class="ml-2" variant="secondary" @click="cancel">Cancel</b-button>
        </div>
    </div>
    <b-modal id="modal-select-compute-resource" ref="modalSelectComputeResource" title="Select Compute Resource"
      @ok="onSelectComputeResource" :ok-disabled="modalSelectComputeResourceOkDisabled">
      <b-form-select v-model="selectedComputeResource" :options="computeResourceOptions">
        <template slot="first">
          <option :value="null">Please select compute resource</option>
        </template>
      </b-form-select>
    </b-modal>
  </div>
</template>
<script>
  import {components as comps, layouts} from 'django-airavata-common-ui'
  import {models, services} from 'django-airavata-api'

  export default {
    name: "group-compute-resource-preference",
    props: {
      value: {
        type: models.GroupResourceProfile,
        default: function () {
          return new models.GroupResourceProfile()
        }
      },
      id: {
        type: String,
      },
    },
    mounted: function () {
      if (this.value.groupResourceProfileId) {
        services.ServiceFactory.service("SharedEntities").retrieve({lookup: this.value.groupResourceProfileId})
          .then(sharedEntity => this.sharedEntity = sharedEntity);
      } else if (this.id) {
        services.ServiceFactory.service("GroupResourceProfiles").retrieve({lookup: this.id})
          .then(grp => this.data = grp);
        services.ServiceFactory.service("SharedEntities").retrieve({lookup: this.id})
          .then(sharedEntity => this.sharedEntity = sharedEntity);
      }
      services.ComputeResourceService.namesList()
        .then(names => this.computeResources = names);
    },
    data: function () {
      let data = this.value.clone();
      return {
        data: data,
        service: services.ServiceFactory.service("GroupResourceProfiles"),
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
        computeResources: null,
        selectedComputeResource: null,
      }
    },

    components: {
      "share-button": comps.ShareButton,
      "list-layout": layouts.ListLayout,
    },
    computed: {
      modalSelectComputeResourceOkDisabled: function() {
        return this.selectedComputeResource == null;
      },
      computeResourceOptions: function() {
        const currentPrefs = this.data.computePreferences ? this.data.computePreferences.map(computePreference => computePreference.computeResourceId) : [];
        const options = this.computeResources ? this.computeResources
          .filter(comp => !currentPrefs.includes(comp.host_id))
          .map(comp => {
            return {
              value: comp.host_id,
              text: comp.host 
            }
          }) : [];
        options.sort((a, b) => a.text.toLowerCase().localeCompare(b.text.toLowerCase()));
        return options;
      },
    },
    methods: {
      saveGroupResourceProfile: function () {
        var persist = null;
        if (this.data.groupResourceProfileId) {
          persist = this.service.update({data: this.data, lookup: this.data.groupResourceProfileId});
        } else {
          persist = this.service.create({data: this.data})
            .then((data) => {
              // Save sharing settings too
              return this.$refs.shareButton.mergeAndSave(data.groupResourceProfileId);
            });
        }
        // TODO: handle errors
        persist.then(data => {
          this.$router.push('/group-resource-profiles');
        });
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
            groupResourceProfile: this.data,
            computeResourcePolicy: computeResourcePolicy,
            batchQueueResourcePolicies: batchQueueResourcePolicies,
          }
        });
      },
      getComputeResourceName: function (computeResourceId) {
        // TODO: load compute resources to get the real name
        return (computeResourceId && computeResourceId.indexOf("_") > 0) ? computeResourceId.split("_")[0] : computeResourceId;
      },
      cancel: function() {
        this.$router.push('/group-resource-profiles');
      },
      createComputePreference: function() {
        this.$refs.modalSelectComputeResource.show();
      },
      onSelectComputeResource: function() {
        const computeResourcePreference = new models.GroupComputeResourcePreference();
        const computeResourceId = this.selectedComputeResource;
        computeResourcePreference.computeResourceId = computeResourceId;
        this.$router.push({
          name: 'compute_preference', params: {
            value: computeResourcePreference,
            id: this.data.groupResourceProfileId,
            host_id: computeResourceId,
            groupResourceProfile: this.data,
            computeResourcePolicy: null,
            batchQueueResourcePolicies: null,
          }
        });
      }
    },
  }
</script>