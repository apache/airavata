<template>
  <div>
    <div class="row">
      <div class="col">
        <div class="card">
          <div class="card-body">
            <b-table hover :fields="fields" :items="items" :fixed="true">
              <template slot="cell(creationTime)" slot-scope="data">
                <human-date :date="data.value" />
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
                <enable-user-panel
                  v-if="!data.item.enabled && !data.item.emailVerified"
                  :username="data.item.userId"
                  :email="data.item.email"
                  @enable-user="enableUser"
                />
                <delete-user-panel
                  v-if="!data.item.enabled && !data.item.emailVerified"
                  :username="data.item.userId"
                  @delete-user="deleteUser"
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
import { components } from "django-airavata-common-ui";
import { services } from "django-airavata-api";
import EnableUserPanel from "./EnableUserPanel";
import DeleteUserPanel from "./DeleteUserPanel";

export default {
  name: "unverified-email-user-management-container",
  data() {
    return {
      usersPaginator: null,
      showingDetails: {},
    };
  },
  components: {
    pager: components.Pager,
    "human-date": components.HumanDate,
    EnableUserPanel,
    DeleteUserPanel,
  },
  created() {
    services.UnverifiedEmailUserProfileService.list({ limit: 10 }).then(
      (users) => (this.usersPaginator = users)
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
          label: "Email Verified",
          key: "emailVerified",
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
      return this.usersPaginator ? this.usersPaginator.results : [];
    },
  },
  methods: {
    next() {
      this.usersPaginator.next();
    },
    previous() {
      this.usersPaginator.previous();
    },
    enableUser(username) {
      services.IAMUserProfileService.enable({ lookup: username }).finally(() =>
        this.loadUnverifiedEmailUsers()
      );
    },
    deleteUser(username) {
      services.IAMUserProfileService.delete({ lookup: username }).finally(() =>
        this.loadUnverifiedEmailUsers()
      );
    },
    loadUnverifiedEmailUsers() {
      return services.UnverifiedEmailUserProfileService.list({
        limit: 10,
      }).then((users) => (this.usersPaginator = users));
    },
    toggleDetails(row) {
      row.toggleDetails();
      this.showingDetails[row.item.userId] = !this.showingDetails[
        row.item.userId
      ];
    },
  },
};
</script>
