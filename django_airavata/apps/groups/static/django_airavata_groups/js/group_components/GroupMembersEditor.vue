<template>
  <div>
    <b-form-group>
      <autocomplete-text-input id="user-autocomplete" :suggestions="suggestions" @selected="suggestionSelected"
        placeholder="Search for users to add to this group" />
    </b-form-group>
    <b-table v-if="membersCount > 0" hover :items="currentMembers" :fields="fields" sort-by="name">
      <template slot="HEAD_role" slot-scope="data">
        <div class="d-flex">
          <div>Role</div>
          <div class="ml-auto mr-2">
            <i class="fa fa-info-circle text-info align-text-top" v-b-tooltip.hover title="Admins can add and remove group members." />
          </div>
        </div>
      </template>
      <template slot="role" slot-scope="data">
        <b-form-select v-if="group.isOwner" :value="data.item.role" @input="changeRole(data.item, $event)" :options="groupRoleOptions">
        </b-form-select>
        <span v-else>{{ data.value }}</span>
      </template>
      <template slot="remove" slot-scope="data">
        <b-link v-if="data.item.editable" @click="removeMember(data.item)">
          <span class="fa fa-trash"></span>
        </b-link>
      </template>
    </b-table>
  </div>
</template>

<script>
import { models, services } from "django-airavata-api";
import { components } from "django-airavata-common-ui";

export default {
  name: "group-members-editor",
  components: {
    "autocomplete-text-input": components.AutocompleteTextInput
  },
  props: {
    group: {
      type: models.Group,
      required: true
    }
  },
  data() {
    return {
      userProfiles: null
    };
  },
  computed: {
    members() {
      return this.group.members;
    },
    admins() {
      return this.group.admins;
    },
    suggestions() {
      if (!this.userProfiles) {
        return [];
      }
      // TODO: filter out current members
      return this.userProfiles.map(userProfile => {
        return {
          id: userProfile.airavataInternalUserId,
          name:
            userProfile.firstName +
            " " +
            userProfile.lastName +
            " (" +
            userProfile.userId +
            ")"
        };
      });
    },
    fields() {
      return [
        { key: "name", label: "Name", sortable: true },
        { key: "username", label: "Username", sortable: true },
        { key: "email", label: "Email", sortable: true },
        { key: "role", label: "Role", sortable: true },
        { key: "remove", label: "Remove" }
      ];
    },
    userProfilesMap() {
      if (!this.userProfiles) {
        return null;
      }
      const result = {};
      this.userProfiles.forEach(up => {
        result[up.airavataInternalUserId] = up;
      });
      return result;
    },
    currentMembers() {
      if (!this.userProfilesMap) {
        return [];
      }
      return (
        this.members
          // Filter out users that are missing profiles
          .filter(m => m in this.userProfilesMap)
          .map(m => {
            const userProfile = this.userProfilesMap[m];
            const isAdmin = this.admins.indexOf(m) >= 0;
            // Owners can edit all members and admins can edit non-admin members
            const editable =
              this.group.isOwner || (this.group.isAdmin && !isAdmin);
            return {
              id: m,
              name: userProfile.firstName + " " + userProfile.lastName,
              username: userProfile.userId,
              email: userProfile.email,
              role: isAdmin ? "ADMIN" : "MEMBER",
              editable: editable
            };
          })
      );
    },
    membersCount() {
      return this.members.length;
    },
    groupRoleOptions() {
      return [
        {
          value: "MEMBER",
          text: "MEMBER"
        },
        {
          value: "ADMIN",
          text: "ADMIN"
        }
      ];
    }
  },
  created() {
    services.UserProfileService.list().then(userProfiles => {
      this.userProfiles = userProfiles;
    });
  },
  methods: {
    suggestionSelected(suggestion) {
      this.$emit("add-member", suggestion.id);
    },
    removeMember(item) {
      this.$emit("remove-member", item.id);
    },
    changeRole(item, role) {
      if (role === "ADMIN") {
        this.$emit("change-role-to-admin", item.id);
      } else {
        this.$emit("change-role-to-member", item.id);
      }
    }
  }
};
</script>

