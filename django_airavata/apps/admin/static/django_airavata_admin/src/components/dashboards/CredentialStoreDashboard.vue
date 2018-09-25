<template>
  <list-layout @add-new-item="addNewSSHCredential" :items="sshKeys" title="SSH Credentials" new-item-button-text="New SSH Credential">
    <template slot="item-list" slot-scope="slotProps">

      <b-table striped hover :fields="fields" :items="slotProps.items">
        <template slot="action" slot-scope="data">
          <clipboard-copy-link :text="data.item.publicKey" class="mr-1" />
          <delete-link v-if="data.item.userHasWriteAccess" @delete="deleteSSHCredential(data.item)">
            Are you sure you want to delete this SSH credential?
          </delete-link>
        </template>
      </b-table>
    </template>
  </list-layout>
</template>

<script>
import { services } from "django-airavata-api";
import { components, layouts } from "django-airavata-common-ui";
import moment from "moment";
import ClipboardCopyLink from "../commons/ClipboardCopyLink.vue";

export default {
  components: {
    "delete-link": components.DeleteLink,
    "list-layout": layouts.ListLayout,
    ClipboardCopyLink
  },
  created: function() {
    this.fetchSSHKeys();
  },
  data: function() {
    return {
      sshKeys: []
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
          formatter: value => moment(new Date(value)).fromNow()
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
        console.log(
          "loaded SSH Credentials",
          JSON.stringify(sshCreds, null, 4)
        );
        this.sshKeys = sshCreds;
      });
    },
    addNewSSHCredentials() {},
    deleteSSHCredential() {}
  }
};
</script>
