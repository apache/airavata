<template>
  <list-layout @add-new-item="addNewStoragePreference" :items="decoratedStoragePreferences" title="Storage Preferences"
    new-item-button-text="New Storage Preference">
    <template slot="new-item-editor">
      <b-card v-if="showNewItemEditor" title="New Storage Preference">
        <b-form-group label="Storage Resource" label-for="storage-resource">
          <b-form-select id="storage-resource" v-model="newStoragePreference.storageResourceId" :options="storageResourceOptions" />
        </b-form-group>
        <storage-preference-editor v-model="newStoragePreference" />
        <div class="row">
          <div class="col">
            <b-button variant="primary" @click="saveNewStoragePreference">
              Save
            </b-button>
            <b-button variant="secondary" @click="cancelNewStoragePreference">
              Cancel
            </b-button>
          </div>
        </div>
      </b-card>
    </template>
    <template slot="item-list" slot-scope="slotProps">

      <b-table striped hover :fields="fields" :items="slotProps.items" sort-by="storageResourceId">
        <template slot="action" slot-scope="data">
          <b-link @click="toggleDetails(data)">
            Edit
            <i class="fa fa-edit" aria-hidden="true"></i>
          </b-link>
        </template>
        <template slot="row-details" slot-scope="row">
          <b-card>
            <storage-preference-editor :value="row.item" @input="updatedStoragePreference" />
            <b-button size="sm" @click="toggleDetails(row)">Close</b-button>
          </b-card>
        </template>
      </b-table>
    </template>
  </list-layout>
</template>

<script>
import { models, services, utils } from "django-airavata-api";
import { layouts } from "django-airavata-common-ui";
import StoragePreferenceEditor from "./StoragePreferenceEditor.vue";

export default {
  name: "storage-preference-list",
  components: {
    "list-layout": layouts.ListLayout,
    StoragePreferenceEditor
  },
  props: {
    storagePreferences: {
      type: Array,
      required: true
    }
  },
  data() {
    return {
      showingDetails: {},
      showNewItemEditor: false,
      newStoragePreference: null,
      storageResourceNames: null
    };
  },
  computed: {
    fields() {
      return [
        {
          label: "Name",
          key: "storageResourceId",
          sortable: true,
          formatter: value => this.getStorageResourceName(value)
        },
        {
          label: "Username",
          key: "loginUserName"
        },
        {
          label: "SSH Credential",
          key: "resourceSpecificCredentialStoreToken",
          formatter: value => this.getCredentialName(value)
        },
        {
          label: "File System Location",
          key: "fileSystemRootLocation",
          formatter: value => this.formatFileSystemLocation(value)
        },
        {
          label: "Action",
          key: "action"
        }
      ];
    },
    decoratedStoragePreferences() {
      return this.storagePreferences.map(sp => {
        const spClone = sp.clone();
        spClone._showDetails = this.showingDetails[spClone.storageResourceId];
        return spClone;
      });
    },
    storageResourceOptions() {
      const options = [];
      for (const key in this.storageResourceNames) {
        if (this.storageResourceNames.hasOwnProperty(key)) {
          const name = this.storageResourceNames[key];
          options.push({
            value: key,
            text: name
          });
        }
      }
      return utils.StringUtils.sortIgnoreCase(options, a => a.text);
    }
  },
  created() {
    services.StorageResourceService.names().then(names => {
      this.storageResourceNames = names;
    });
  },
  methods: {
    getStorageResourceName(storageResourceId) {
      // TODO: fetch storage resources
      return storageResourceId;
    },
    getCredentialName(token) {
      // TODO: fetch credential name
      return token;
    },
    formatFileSystemLocation(fileSystemRootLocation) {
      // TODO: truncate to fit
      return fileSystemRootLocation;
    },
    updatedStoragePreference(newValue) {
      this.$emit("updated", newValue);
    },
    toggleDetails(row) {
      row.toggleDetails();
      this.showingDetails[row.item.storageResourceId] = !this.showingDetails[
        row.item.storageResourceId
      ];
    },
    addNewStoragePreference() {
      this.newStoragePreference = new models.StoragePreference();
      this.showNewItemEditor = true;
    },
    saveNewStoragePreference() {
      this.$emit("added", this.newStoragePreference);
      this.showNewItemEditor = false;
    },
    cancelNewStoragePreference() {
      this.showNewItemEditor = false;
    }
  }
};
</script>

