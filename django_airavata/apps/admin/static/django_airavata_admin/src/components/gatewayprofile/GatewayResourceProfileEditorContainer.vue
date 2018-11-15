<template>
  <div>
    <div class="row">
      <div class="col">
        <div class="card">
          <div class="card-body">
            <gateway-resource-profile-editor v-if="gatewayResourceProfile" v-model="gatewayResourceProfile" />
          </div>
        </div>
      </div>
    </div>
    <div class="row">
      <div class="col">
        <div class="card">
          <div class="card-body">
            <storage-preference-list v-if="gatewayResourceProfile" :storagePreferences="gatewayResourceProfile.storagePreferences"
              @updated="updatedStoragePreference" @added="addedStoragePreference" />
          </div>
        </div>
      </div>
    </div>
    <div class="row">
      <div class="col">
        <b-button variant="primary" @click="save">
          Save
        </b-button>
        <b-button variant="secondary" @click="cancel">
          Cancel
        </b-button>
      </div>
    </div>
  </div>
</template>

<script>
import { services } from "django-airavata-api";
import GatewayResourceProfileEditor from "./GatewayResourceProfileEditor.vue";
import StoragePreferenceEditor from "./StoragePreferenceEditor.vue";
import StoragePreferenceList from "./StoragePreferenceList.vue";

export default {
  name: "gateway-resource-profile-editor-container",
  components: {
    GatewayResourceProfileEditor,
    StoragePreferenceEditor,
    StoragePreferenceList
  },
  data() {
    return {
      gatewayResourceProfile: null,
      gatewayResourceProfileClone: null
    };
  },
  created() {
    services.GatewayResourceProfileService.current().then(gwp => {
      this.gatewayResourceProfile = gwp;
      this.gatewayResourceProfileClone = gwp.clone();
    });
  },
  methods: {
    save() {
      services.GatewayResourceProfileService.update({
        lookup: this.gatewayResourceProfile.gatewayID,
        data: this.gatewayResourceProfile
      }).then(gwp => {
        this.gatewayResourceProfile = gwp;
        this.gatewayResourceProfileClone = gwp.clone();
      });
    },
    cancel() {
      this.gatewayResourceProfile = this.gatewayResourceProfileClone.clone();
    },
    updatedStoragePreference(updatedStoragePreference) {
      const index = this.gatewayResourceProfile.storagePreferences.findIndex(
        sp =>
          sp.storageResourceId === updatedStoragePreference.storageResourceId
      );
      this.gatewayResourceProfile.storagePreferences.splice(
        index,
        1,
        updatedStoragePreference
      );
    },
    addedStoragePreference(newStoragePreference) {
      services.StoragePreferenceService.create({
        data: newStoragePreference
      }).then(sp => {
        this.gatewayResourceProfile.storagePreferences.push(sp);
      });
    }
  }
};
</script>

