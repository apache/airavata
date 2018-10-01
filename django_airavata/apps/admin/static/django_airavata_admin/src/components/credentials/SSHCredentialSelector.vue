<template>
  <div>
    <b-input-group>
      <b-form-select v-model="data" :options="credentialStoreTokenOptions">
        <template v-if="$slots.first" slot="first">
          <slot name="first">
            <option :value="null">
              Use the default SSH credential
            </option>
          </slot>
        </template>
      </b-form-select>
      <b-input-group-append>
        <clipboard-copy-button variant="secondary" :disabled="!selectedCredential" :text="selectedCredential ? selectedCredential.publicKey : null">
        </clipboard-copy-button>
        <b-button variant="secondary" @click="showNewSSHCredentialModal">
          <font-awesome-icon icon="plus" />
        </b-button>
      </b-input-group-append>
    </b-input-group>
    <new-ssh-credential-modal ref="newSSHCredentialModal" @new="createSSHCredential" />
  </div>
</template>

<script>
import vmodel_mixin from "../commons/vmodel_mixin";
import { services } from "django-airavata-api";
import ClipboardCopyButton from "../commons/ClipboardCopyButton.vue";
import NewSSHCredentialModal from "../credentials/NewSSHCredentialModal.vue";

export default {
  name: "ssh-credential-selector",
  props: {},
  mixins: [vmodel_mixin],
  components: {
    ClipboardCopyButton,
    "new-ssh-credential-modal": NewSSHCredentialModal
  },
  data() {
    return {
      credentials: null
    };
  },
  computed: {
    credentialStoreTokenOptions() {
      const options = this.credentials
        ? this.credentials.map(summary => {
            return {
              value: summary.token,
              text: summary.description
            };
          })
        : [];
      options.sort((a, b) =>
        a.text.toLowerCase().localeCompare(b.text.toLowerCase())
      );
      return options;
    },
    selectedCredential() {
      return this.credentials
        ? this.credentials.find(cred => cred.token === this.data)
        : null;
    }
  },
  methods: {
    showNewSSHCredentialModal() {
      this.$refs.newSSHCredentialModal.show();
    },
    createSSHCredential(data) {
      services.CredentialSummaryService.createSSH({ data: data }).then(cred => {
        this.credentials.push(cred);
        this.data = cred.token;
      });
    }
  },
  created() {
    if (!this.credentials) {
      services.CredentialSummaryService.allSSHCredentials().then(
        creds => (this.credentials = creds)
      );
    }
  }
};
</script>

