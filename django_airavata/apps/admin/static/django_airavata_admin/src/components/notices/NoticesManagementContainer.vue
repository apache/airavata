<template>
  <div>
    <div class="row">
      <div class="col">
        <h1 class="h4 mb-4">Manage Notices</h1>
      </div>
    </div>
    <div class="row">
      <div class="col">
        <div class="card">
          <div class="card-body">


            <list-layout @add-new-item="addNewNotice" :items="decoratedStoragePreferences" title="Notice"
              new-item-button-text="New Notice">
              <template slot="new-item-editor">
                <b-card v-if="showNewItemEditor" title="New Storage Preference">
                  <storage-preference-editor v-model="newStoragePreference" :default-credential-store-token="defaultCredentialStoreToken" />
                  <div class="row">
                    <div class="col">
                      <b-button variant="primary" @click="saveNewNotice">
                        Save
                      </b-button>
                      <b-button variant="secondary" @click="cancelNewNotice">
                        Cancel
                      </b-button>
                    </div>
                  </div>
                </b-card>
              </template>
              <template slot="item-list" slot-scope="slotProps">

                <b-table hover :fields="fields" :items="items">
                  <template slot="publishedTime" slot-scope="data">
                    <human-date :date="data.value"/>
                  </template>
                  <template slot="expirationTime" slot-scope="data">
                    <human-date :date="data.value"/>
                  </template>
                  <template slot="action" slot-scope="data">
                    <b-link class="action-link" @click="toggleDetails(data)">
                      Edit
                      <i class="fa fa-edit" aria-hidden="true"></i>
                    </b-link>
                    <delete-link @delete="deleteNotice(data.item.notificationId)">
                      Are you sure you want to delete the notice?
                  </delete-link>
                  </template>
                  <template slot="row-details" slot-scope="data">
                  </template>
                </b-table>
              </template>
            </list-layout >
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { models, services } from "django-airavata-api";
import { components, layouts } from "django-airavata-common-ui";
import NoticesDetailsContainer from "./NoticesDetailsContainer.vue";

export default {
  name: "notice-management-container",
  data() {
    return {
      notices: null,
      showNewItemEditor: false,
      newNotice: null,
      allGroups: null,
      showingDetails: {}
    };
  },
  components: {
    'human-date': components.HumanDate,
    NoticesDetailsContainer,
    "delete-link": components.DeleteLink,
    "list-layout": layouts.ListLayout,
  },
  created() {
    services.ManageNotificationService.list().then(
      notices => (this.notices = notices)
    );
    services.GroupService.list({ limit: -1 }).then(
      groups => (this.allGroups = groups)
    );
  },
  computed: {
    fields() {
      return [
        {
          label: "Notice",
          key: "title"
        },
        {
          label: "Message",
          key: "notificationMessage"
        },
        {
          label: "Publish Date",
          key: "publishedTime"
        },
        {
          label: "Expiry Date",
          key: "expirationTime"
        },
        {
          label: "Priority",
          key: "priority.name"
        },
        {
          label: "Action",
          key: "action"
        }

      ];
    },
    items() {
      return this.notices
        ? this.notices
        : [];
    },
    editableGroups() {
      return this.allGroups
        ? this.allGroups.filter(g => g.isAdmin || g.isOwner)
        : [];
    }
  },
  methods: {
    addNewNotice() {
      this.newNotice = new models.Notification();
      this.showNewItemEditor = true;
    },
    groupsUpdated(managedUserProfile) {
      services.ManagedUserProfileService.update({
        lookup: managedUserProfile.userId,
        data: managedUserProfile
      }).finally(() => {
        this.reloadUserProfiles();
      });
    },
    deleteNotice(notificationId) {
      services.ManageNotificationService.delete({
        lookup: notificationId
      }).then(() => {
        const index = this.notices.findIndex(
          sp => sp.notificationId === notificationId
        );
        this.notices.splice(index, 1);
      });
    },
    reloadUserProfiles() {
      const params = {
        limit: 10,
        offset: this.currentOffset
      };
      if (this.search) {
        params["search"] = this.search;
      }
      services.ManagedUserProfileService.list(params).then(
        users => (this.usersPaginator = users)
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
    }
  }
};
</script>
