<template>
  <div>
    <b-input-group>
      <b-form-select
        v-model="data"
        :options="credentialStoreTokenOptions"
        :disabled="readonly"
      >
        <option
          v-if="nullOption"
          slot="first"
          :value="null"
          :disabled="nullOptionDisabled"
        >
          <slot
            name="null-option-label"
            :defaultCredentialSummary="defaultCredentialSummary"
          >
            <span v-if="defaultCredentialSummary">
              Use the default SSH credential ({{
                createCredentialDescription(defaultCredentialSummary)
              }})
            </span>
            <span v-else> Unset the default SSH credential </span>
          </slot>
        </option>
      </b-form-select>
      <b-input-group-append>
        <clipboard-copy-button variant="secondary" :text="copySSHPublicKeyText">
        </clipboard-copy-button>
        <b-button
          v-if="!readonly"
          variant="secondary"
          @click="showNewSSHCredentialModal"
        >
          <i class="fa fa-plus"></i>
        </b-button>
      </b-input-group-append>
    </b-input-group>
    <new-ssh-credential-modal
      ref="newSSHCredentialModal"
      @new="createSSHCredential"
    />
  </div>
</template>

<script>
import { services } from "django-airavata-api";
import { components, mixins } from "django-airavata-common-ui";
import NewSSHCredentialModal from "../credentials/NewSSHCredentialModal.vue";

export default {
  // TODO: disable if the 'value' is not in the list of loaded credentials?
  // Because it would mean that the user doesn't have access to this credential.
  // Maybe display 'You don't have access to this credential'.
  name: "ssh-credential-selector",
  props: {
    nullOption: {
      type: Boolean,
      default: true,
    },
    // This is the default credential token that will be used if the null option is selected
    nullOptionDefaultCredentialToken: {
      type: String,
    },
    nullOptionDisabled: {
      type: Boolean,
      default: false,
    },
    readonly: {
      type: Boolean,
      default: false,
    },
  },
  mixins: [mixins.VModelMixin],
  components: {
    "clipboard-copy-button": components.ClipboardCopyButton,
    "new-ssh-credential-modal": NewSSHCredentialModal,
  },
  data() {
    return {
      credentials: null,
    };
  },
  computed: {
    credentialStoreTokenOptions() {
      const options = this.credentials
        ? this.credentials.map((summary) => {
            return {
              value: summary.token,
              text: this.createCredentialDescription(summary),
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
        ? this.credentials.find((cred) => cred.token === this.data)
        : null;
    },
    defaultCredentialSummary() {
      return this.nullOptionDefaultCredentialToken && this.credentials
        ? this.credentials.find(
            (cred) => cred.token === this.nullOptionDefaultCredentialToken
          )
        : null;
    },
    copySSHPublicKeyText() {
      return this.selectedCredential
        ? this.selectedCredential.publicKey.trim()
        : this.defaultCredentialSummary
        ? this.defaultCredentialSummary.publicKey.trim()
        : null;
    },
  },
  methods: {
    showNewSSHCredentialModal() {
      this.$refs.newSSHCredentialModal.show();
    },
    createSSHCredential(data) {
      services.CredentialSummaryService.createSSH({ data: data }).then(
        (cred) => {
          this.credentials.push(cred);
          this.data = cred.token;
        }
      );
    },
    createCredentialDescription(summary) {
      return (
        summary.username +
        " - " +
        (summary.description
          ? summary.description
          : `No description (${summary.token})`)
      );
    },
  },
  created() {
    if (!this.credentials) {
      services.CredentialSummaryService.allSSHCredentials().then(
        (creds) => (this.credentials = creds)
      );
    }
  },
};
</script>
