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
                  @click="toggleDetails(data)"
                >
                  Edit Groups
                </b-button>
              </template>
              <template
                slot="row-details"
                slot-scope="data"
              >
                <user-details-container
                  :managed-user-profile="data.item"
                  :editable-groups="editableGroups"
                  @groups-updated="groupsUpdated"
                />
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
import UserDetailsContainer from "./UserDetailsContainer.vue";

export default {
  name: "user-management-container",
  data() {
    return {
      usersPaginator: null,
      allGroups: null,
      showingDetails: {}
    };
  },
  components: {
    pager: components.Pager,
    UserDetailsContainer
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
      return this.usersPaginator
        ? this.usersPaginator.results.map(u => {
            const user = u.clone();
            user._showDetails = this.showingDetails[u.airavataInternalUserId] || false;
            return user;
          })
        : [];
    },
    editableGroups() {
      return this.allGroups
        ? this.allGroups.filter(g => g.isAdmin || g.isOwner)
        : [];
    },
    currentOffset() {
      return this.usersPaginator ? this.usersPaginator.offset : 0;
    }
  },
  methods: {
    next() {
      this.usersPaginator.next();
    },
    previous() {
      this.usersPaginator.previous();
    },
    groupsUpdated(managedUserProfile) {
      services.ManagedUserProfileService.update({
        lookup: managedUserProfile.userId,
        data: managedUserProfile
      }).finally(() => {
        this.reloadUserProfiles();
      });
    },
    reloadUserProfiles() {
      services.ManagedUserProfileService.list({
        limit: 10,
        offset: this.currentOffset
      }).then(users => (this.usersPaginator = users));
    },
    toggleDetails(row) {
      row.toggleDetails();
      this.showingDetails[row.item.airavataInternalUserId] = !this
        .showingDetails[row.item.airavataInternalUserId];
    }
  }
};
</script>

