<template>
  <list-layout
    @add-new-item="addNewStoragePreference"
    :items="decoratedStoragePreferences"
    title="Storage Preferences"
    new-item-button-text="New Storage Preference"
    :new-button-disabled="readonly"
  >
    <template slot="new-item-editor">
      <b-card v-if="showNewItemEditor" title="New Storage Preference">
        <b-form-group label="Storage Resource" label-for="storage-resource">
          <b-form-select
            id="storage-resource"
            v-model="newStoragePreference.storageResourceId"
            :options="storageResourceOptions"
          />
        </b-form-group>
        <storage-preference-editor
          v-model="newStoragePreference"
          :default-credential-store-token="defaultCredentialStoreToken"
        />
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
      <b-table
        striped
        hover
        :fields="fields"
        :items="slotProps.items"
        sort-by="storageResourceId"
      >
        <template slot="cell(resourceSpecificCredentialStoreToken)" slot-scope="data">
          {{ data.value }}
          <b-badge
            v-if="
              defaultCredentialStoreToken &&
              !data.item.resourceSpecificCredentialStoreToken
            "
          >
            Default
          </b-badge>
        </template>
        <template slot="cell(action)" slot-scope="data">
          <b-link
            v-if="!readonly"
            class="action-link"
            @click="toggleDetails(data)"
          >
            Edit
            <i class="fa fa-edit" aria-hidden="true"></i>
          </b-link>
          <delete-link
            v-if="!readonly"
            class="action-link"
            @delete="deleteStoragePreference(data.item.storageResourceId)"
          >
            Are you sure you want to delete the storage preference for
            <strong>{{
              getStorageResourceName(data.item.storageResourceId)
            }}</strong
            >?
          </delete-link>
        </template>
        <template slot="row-details" slot-scope="row">
          <b-card>
            <storage-preference-editor
              :value="row.item"
              @input="updatedStoragePreference"
              :default-credential-store-token="defaultCredentialStoreToken"
            />
            <b-button size="sm" @click="toggleDetails(row)">Close</b-button>
          </b-card>
        </template>
      </b-table>
    </template>
  </list-layout>
</template>

<script>
import { models, services, utils } from "django-airavata-api";
import { components, layouts } from "django-airavata-common-ui";
import StoragePreferenceEditor from "./StoragePreferenceEditor.vue";

export default {
  name: "storage-preference-list",
  components: {
    "delete-link": components.DeleteLink,
    "list-layout": layouts.ListLayout,
    StoragePreferenceEditor,
  },
  props: {
    storagePreferences: {
      type: Array,
      required: true,
    },
    defaultCredentialStoreToken: {
      type: String,
    },
    readonly: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      showingDetails: {},
      showNewItemEditor: false,
      newStoragePreference: null,
      storageResourceNames: null,
      credentials: null,
    };
  },
  computed: {
    fields() {
      return [
        {
          label: "Name",
          key: "storageResourceId",
          sortable: true,
          formatter: (value) => this.getStorageResourceName(value),
        },
        {
          label: "Username",
          key: "loginUserName",
        },
        {
          label: "SSH Credential",
          key: "resourceSpecificCredentialStoreToken",
          formatter: (value) => this.getCredentialName(value),
        },
        {
          label: "File System Location",
          key: "fileSystemRootLocation",
        },
        {
          label: "Action",
          key: "action",
        },
      ];
    },
    decoratedStoragePreferences() {
      return this.storagePreferences.map((sp) => {
        const spClone = sp.clone();
        spClone._showDetails = this.showingDetails[spClone.storageResourceId];
        return spClone;
      });
    },
    currentStoragePreferenceIds() {
      return this.storagePreferences.map((sp) => sp.storageResourceId);
    },
    storageResourceOptions() {
      const options = [];
      for (const key in this.storageResourceNames) {
        if (
          this.storageResourceNames.hasOwnProperty(key) &&
          this.currentStoragePreferenceIds.indexOf(key) < 0
        ) {
          const name = this.storageResourceNames[key];
          options.push({
            value: key,
            text: name,
          });
        }
      }
      return utils.StringUtils.sortIgnoreCase(options, (a) => a.text);
    },
    defaultCredentialSummary() {
      if (this.defaultCredentialStoreToken && this.credentials) {
        return this.credentials.find(
          (cred) => cred.token === this.defaultCredentialStoreToken
        );
      } else {
        return null;
      }
    },
  },
  created() {
    services.StorageResourceService.names().then((names) => {
      this.storageResourceNames = names;
    });
    services.CredentialSummaryService.allSSHCredentials().then(
      (creds) => (this.credentials = creds)
    );
  },
  methods: {
    getStorageResourceName(storageResourceId) {
      if (
        this.storageResourceNames &&
        storageResourceId in this.storageResourceNames
      ) {
        return this.storageResourceNames[storageResourceId];
      } else {
        return storageResourceId.substring(0, 10) + "...";
      }
    },
    getCredentialName(token) {
      if (token === null && this.defaultCredentialSummary) {
        return this.defaultCredentialSummary.description;
      } else if (this.credentials) {
        const cred = this.credentials.find((cred) => cred.token === token);
        if (cred) {
          return cred.description;
        }
      }
      return "...";
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
    deleteStoragePreference(storageResourceId) {
      this.$emit("delete", storageResourceId);
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
    },
  },
};
</script>
