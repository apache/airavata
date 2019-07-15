<template>
  <div>
    <list-layout @add-new-item="showNewSSHCredentialModal" :items="sshKeys" title="SSH Credentials" new-item-button-text="New SSH Credential">
      <template slot="item-list" slot-scope="slotProps">

        <b-table striped hover :fields="fields" :items="slotProps.items">
          <template slot="sharing" slot-scope="data">
            <share-button :entity-id="data.item.token" :disallow-editing-admin-groups="false" :auto-add-admin-groups="false"/>
          </template>
          <template slot="persistedTime" slot-scope="data">
            <human-date :date="data.value"/>
          </template>
          <template slot="action" slot-scope="data">
            <clipboard-copy-link :text="data.item.publicKey.trim()" class="mr-1" />
            <delete-link v-if="data.item.userHasWriteAccess" @delete="deleteSSHCredential(data.item)">
              Are you sure you want to delete the
              <strong>{{ data.item.description }}</strong> SSH credential?
            </delete-link>
          </template>
        </b-table>
      </template>
    </list-layout>
    <new-ssh-credential-modal ref="newSSHCredentialModal" @new="createNewSSHCredential" />
    <!--
    <list-layout class="mt-4" @add-new-item="showNewPasswordCredentialModal" :items="passwordCredentials" title="Password Credentials"
      new-item-button-text="New Password Credential">
      <template slot="item-list" slot-scope="slotProps">

        <b-table striped hover :fields="fields" :items="slotProps.items">
          <template slot="sharing" slot-scope="data">
            <share-button :entity-id="data.item.token" :disallow-editing-admin-groups="false" :auto-add-admin-groups="false"/>
          </template>
          <template slot="action" slot-scope="data">
            <delete-link v-if="data.item.userHasWriteAccess" @delete="deletePasswordCredential(data.item)">
              Are you sure you want to delete the
              <strong>{{ data.item.description }}</strong> password credential?
            </delete-link>
          </template>
        </b-table>
      </template>
    </list-layout>
    <new-password-credential-modal ref="newPasswordCredentialModal" @new="createNewPasswordCredential" />
    -->
  </div>
</template>

<script>
import { services } from "django-airavata-api";
import { components, layouts } from "django-airavata-common-ui";
import NewSSHCredentialModal from "../credentials/NewSSHCredentialModal.vue";
// import NewPasswordCredentialModal from "../credentials/NewPasswordCredentialModal.vue";

export default {
  components: {
    "delete-link": components.DeleteLink,
    "human-date": components.HumanDate,
    "list-layout": layouts.ListLayout,
    "clipboard-copy-link": components.ClipboardCopyLink,
    // "new-password-credential-modal": NewPasswordCredentialModal,
    "new-ssh-credential-modal": NewSSHCredentialModal,
    "share-button": components.ShareButton
  },
  created: function() {
    this.fetchSSHKeys();
    this.fetchPasswordCredentials();
  },
  data: function() {
    return {
      sshKeys: [],
      passwordCredentials: []
    };
  },
  computed: {
    fields() {
      return [
        {
          label: "Description",
          key: "description"
        },
        {
          label: "User",
          key: "username"
        },
        {
          label: "Created",
          key: "persistedTime",
        },
        {
          label: "Sharing",
          key: "sharing"
        },
        {
          label: "Action",
          key: "action"
        }
      ];
    }
  },
  methods: {
    fetchSSHKeys() {
      services.CredentialSummaryService.allSSHCredentials().then(sshCreds => {
        this.sshKeys = sshCreds;
      });
    },
    fetchPasswordCredentials() {
      services.CredentialSummaryService.allPasswordCredentials().then(
        passwordCreds => (this.passwordCredentials = passwordCreds)
      );
    },
    showNewSSHCredentialModal() {
      this.$refs.newSSHCredentialModal.show();
    },
    createNewSSHCredential(data) {
      services.CredentialSummaryService.createSSH({ data: data }).then(() =>
        this.fetchSSHKeys()
      );
    },
    deleteSSHCredential(cred) {
      services.CredentialSummaryService.delete({ lookup: cred.token }).then(
        () => this.fetchSSHKeys()
      );
    },
    showNewPasswordCredentialModal() {
      this.$refs.newPasswordCredentialModal.show();
    },
    createNewPasswordCredential(data) {
      services.CredentialSummaryService.createPassword({ data: data }).then(
        () => this.fetchPasswordCredentials()
      );
    },
    deletePasswordCredential(cred) {
      services.CredentialSummaryService.delete({ lookup: cred.token }).then(
        () => this.fetchPasswordCredentials()
      );
    }
  }
};
</script>
