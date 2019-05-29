<template>
  <div>
    <div class="row">
      <div class="col">
        <h1 class="h4 mb-4">Users</h1>
      </div>
    </div>
    <div class="row">
      <div class="col">
        <div class="card">
          <div class="card-body">
            <b-table
              hover
              :fields="fields"
              :items="items"
            >
              <template
                slot="action"
                slot-scope="data"
              >
                <b-button
                  v-if="data.item.airavataUserProfileExists"
                  @click="data.toggleDetails"
                >
                  Edit Groups
                </b-button>
              </template>
              <template
                slot="row-details"
                slot-scope="data"
              >
                <user-group-membership-editor :groups="data.item.groups" :editableGroups="editableGroups" />
              </template>
            </b-table>
            <pager
              v-bind:paginator="usersPaginator"
              v-on:next="next"
              v-on:previous="previous"
            ></pager>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { services } from "django-airavata-api";
import { components } from "django-airavata-common-ui";
import UserGroupMembershipEditor from "./UserGroupMembershipEditor.vue";

export default {
  name: "user-management-container",
  data() {
    return {
      usersPaginator: null,
      allGroups: null
    };
  },
  components: {
    pager: components.Pager,
    UserGroupMembershipEditor
  },
  created() {
    services.ManagedUserProfileService.list({ limit: 10 }).then(
      users => (this.usersPaginator = users)
    );
    services.GroupService.list({ limit: -1 }).then(
      groups => (this.allGroups = groups)
    );
  },
  computed: {
    fields() {
      return [
        {
          label: "First Name",
          key: "firstName"
        },
        {
          label: "Last Name",
          key: "lastName"
        },
        {
          label: "Username",
          key: "userId"
        },
        {
          label: "Email",
          key: "email"
        },
        {
          label: "Enabled",
          key: "enabled"
        },
        {
          label: "Email Verified",
          key: "emailVerified"
        },
        {
          label: "Action",
          key: "action"
        }
      ];
    },
    items() {
      return this.usersPaginator ? this.usersPaginator.results : [];
    },
    editableGroups() {
      return this.allGroups
        ? this.allGroups.filter(g => g.isAdmin || g.isOwner)
        : [];
    }
  },
  methods: {
    next() {
      this.usersPaginator.next();
    },
    previous() {
      this.usersPaginator.previous();
    }
  }
};
</script>

