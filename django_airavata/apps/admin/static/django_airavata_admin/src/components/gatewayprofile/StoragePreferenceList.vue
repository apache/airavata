<template>
  <list-layout @add-new-item="newStoragePreference" :items="decoratedStoragePreferences" title="Storage Preferences"
    new-item-button-text="New Storage Preference">
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
import { models } from "django-airavata-api";
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
    }
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
    }
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
      this.$emit('updated', newValue);
    },
    toggleDetails(row) {
      row.toggleDetails();
      this.showingDetails[row.item.storageResourceId] = !this.showingDetails[row.item.storageResourceId];
    }
  }
};
</script>

