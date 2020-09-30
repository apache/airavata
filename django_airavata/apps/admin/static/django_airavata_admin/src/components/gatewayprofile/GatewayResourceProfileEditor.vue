<template>
  <div>
    <div class="row">
      <div class="col">
        <h1 class="h4 mb-4">Gateway Resource Profile - {{ data.gatewayID }}</h1>
        <b-form-group
          label="Default SSH Credential"
          label-for="default-credential-store-token"
          description="This is the default SSH credential that will be used for storage preferences that don't specify their own SSH credential."
        >
          <ssh-credential-selector
            id="default-credential-store-token"
            v-model="data.credentialStoreToken"
            :readonly="!data.userHasWriteAccess"
          />
        </b-form-group>
      </div>
    </div>
  </div>
</template>

<script>
import { mixins } from "django-airavata-common-ui";
import SSHCredentialSelector from "../credentials/SSHCredentialSelector.vue";

export default {
  name: "gateway-resource-profile-editor",
  mixins: [mixins.VModelMixin],
  components: {
    "ssh-credential-selector": SSHCredentialSelector,
  },
};
</script>
