<template>
  <div>
    <div class="row">
      <div class="col">
        <div class="card">
          <div class="card-body">
            <b-input-group>
              <b-form-input
                v-model="search"
                placeholder="Search by name, email or username"
                @keydown.native.enter="searchUsers"
              />
              <b-input-group-append>
                <b-button @click="resetSearch">Reset</b-button>
                <b-button variant="primary" @click="searchUsers"
                  >Search</b-button
                >
              </b-input-group-append>
            </b-input-group>
          </div>
        </div>
      </div>
    </div>
    <div class="row">
      <div class="col">
        <div class="card">
          <div class="card-body">
            <b-table hover :fields="fields" :items="items" :fixed="true">
              <template slot="cell(creationTime)" slot-scope="data">
                <human-date :date="data.value" />
              </template>
              <template slot="cell(groups)" slot-scope="data">
                <group-membership-display :groups="data.item.groups" />
              </template>
              <template slot="cell(action)" slot-scope="data">
                <b-button
                  v-if="data.item.userHasWriteAccess"
                  @click="toggleDetails(data)"
                >
                  Edit
                </b-button>
              </template>
              <template slot="row-details" slot-scope="data">
                <user-details-container
                  :iam-user-profile="data.item"
                  :editable-groups="editableGroups"
                  @groups-updated="groupsUpdated"
                  @enable-user="enableUser"
                  @delete-user="deleteUser"
                  @update-username="updateUsername(data.item, ...$event)"
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
import GroupMembershipDisplay from "./GroupMembershipDisplay";

export default {
  name: "user-management-container",
  data() {
    return {
      usersPaginator: null,
      allGroups: null,
      showingDetails: {},
      search: null,
    };
  },
  components: {
    pager: components.Pager,
    "human-date": components.HumanDate,
    UserDetailsContainer,
    GroupMembershipDisplay,
  },
  created() {
    services.IAMUserProfileService.list({ limit: 10 }).then(
      (users) => (this.usersPaginator = users)
    );
    services.GroupService.list({ limit: -1 }).then(
      (groups) => (this.allGroups = groups)
    );
  },
  computed: {
    fields() {
      return [
        {
          label: "First Name",
          key: "firstName",
        },
        {
          label: "Last Name",
          key: "lastName",
        },
        {
          label: "Username",
          key: "userId",
        },
        {
          label: "Email",
          key: "email",
        },
        {
          label: "Enabled",
          key: "enabled",
        },
        {
          label: "Email Verified",
          key: "emailVerified",
        },
        {
          label: "Groups",
          key: "groups",
        },
        {
          label: "Created",
          key: "creationTime",
        },
        {
          label: "Action",
          key: "action",
        },
      ];
    },
    items() {
      return this.usersPaginator
        ? this.usersPaginator.results.map((u) => {
            const user = u.clone();
            user._showDetails =
              this.showingDetails[u.airavataInternalUserId] || false;
            return user;
          })
        : [];
    },
    editableGroups() {
      return this.allGroups
        ? this.allGroups.filter((g) => g.isAdmin || g.isOwner)
        : [];
    },
    currentOffset() {
      return this.usersPaginator ? this.usersPaginator.offset : 0;
    },
  },
  methods: {
    next() {
      this.usersPaginator.next();
    },
    previous() {
      this.usersPaginator.previous();
    },
    groupsUpdated(user) {
      services.IAMUserProfileService.update({
        lookup: user.userId,
        data: user,
      }).finally(() => {
        this.reloadUserProfiles();
      });
    },
    reloadUserProfiles() {
      const params = {
        limit: 10,
        offset: this.currentOffset,
      };
      if (this.search) {
        params["search"] = this.search;
      }
      services.IAMUserProfileService.list(params).then(
        (users) => (this.usersPaginator = users)
      );
    },
    toggleDetails(row) {
      row.toggleDetails();
      this.showingDetails[row.item.airavataInternalUserId] = !this
        .showingDetails[row.item.airavataInternalUserId];
    },
    searchUsers() {
      // Reset paginator when starting a search
      this.usersPaginator = null;
      this.reloadUserProfiles();
    },
    resetSearch() {
      this.usersPaginator = null;
      this.search = null;
      this.reloadUserProfiles();
    },
    enableUser(username) {
      services.IAMUserProfileService.enable({ lookup: username }).finally(() =>
        this.reloadUserProfiles()
      );
    },
    deleteUser(username) {
      services.IAMUserProfileService.delete({ lookup: username }).finally(() =>
        this.reloadUserProfiles()
      );
    },
    updateUsername(userProfile, username, newUsername) {
      const updatedUserProfile = userProfile.clone();
      updatedUserProfile.newUsername = newUsername;
      services.IAMUserProfileService.updateUsername({
        data: updatedUserProfile,
      }).finally(() => this.reloadUserProfiles());
    },
  },
};
</script>
