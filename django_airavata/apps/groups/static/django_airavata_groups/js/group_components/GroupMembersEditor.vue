<template>
  <div>
    <b-form-group label="Add members">
      <autocomplete-text-input
        id="user-autocomplete"
        :suggestions="suggestions"
        @selected="suggestionSelected"
        placeholder="Search for users to add to this group"
      />
    </b-form-group>
    <b-form-group label="Filter members">
      <b-input-group>
        <b-input-group-text slot="prepend">
          <i class="fa fa-filter"></i>
        </b-input-group-text>
        <b-form-input v-model="filter" placeholder="Filter list of members" />
      </b-input-group>
    </b-form-group>
    <b-table
      v-if="membersCount > 0"
      hover
      :items="currentMembers"
      :fields="fields"
      :filter="filter"
      sort-by="name"
      :sort-compare="sortCompare"
    >
      <template slot="HEAD_role">
        <div class="d-flex">
          <div>Role</div>
          <div class="ml-auto mr-2">
            <i
              class="fa fa-info-circle text-info align-text-top"
              v-b-tooltip.hover
              title="Admins can add and remove group members."
            />
          </div>
        </div>
      </template>
      <template
        slot="role"
        slot-scope="data"
      >
        <!-- Can only change role if the user is the group owner but the role of the owner can't be changed -->
        <b-form-select
          v-if="group.isOwner && data.item.role !== 'OWNER'"
          :value="data.item.role"
          @input="changeRole(data.item, $event)"
          :options="groupRoleOptions"
        >
        </b-form-select>
        <span v-else>{{ data.value }}</span>
      </template>
      <template
        slot="remove"
        slot-scope="data"
      >
        <b-link
          v-if="data.item.editable"
          @click="removeMember(data.item)"
        >
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
      userProfiles: null,
      newMembers: [],
      filter: null
    };
  },
  computed: {
    members() {
      return this.group.members ? this.group.members : [];
    },
    admins() {
      return this.group.admins;
    },
    suggestions() {
      if (!this.userProfiles) {
        return [];
      }
      return (
        this.userProfiles
          // Filter out current members
          .filter(
            userProfile =>
              this.group.members.indexOf(userProfile.airavataInternalUserId) < 0
          )
          .map(userProfile => {
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
          })
      );
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
            const isOwner = this.group.ownerId === m;
            // Owners can edit all members and admins can edit non-admin members
            // (except the owners role isn't editable)
            const editable =
              !isOwner &&
              (this.group.isOwner || (this.group.isAdmin && !isAdmin));
            return {
              id: m,
              name: userProfile.firstName + " " + userProfile.lastName,
              username: userProfile.userId,
              email: userProfile.email,
              role: isOwner ? "OWNER" : isAdmin ? "ADMIN" : "MEMBER",
              editable: editable,
              _rowVariant: this.newMembers.indexOf(m) >= 0 ? "success" : null
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
      this.newMembers.push(suggestion.id);
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
    },
    sortCompare(aRow, bRow, key) {
      // Sort new members before all others
      const aNewIndex = this.newMembers.indexOf(aRow.id);
      const bNewIndex = this.newMembers.indexOf(bRow.id);
      if (aNewIndex >= 0 && bNewIndex >= 0) {
        return aNewIndex - bNewIndex;
      } else if (aNewIndex >= 0) {
        return -1;
      } else if (bNewIndex >= 0) {
        return 1;
      }
      const a = aRow[key];
      const b = bRow[key];
      if (
        (typeof a === "number" && typeof b === "number") ||
        (a instanceof Date && b instanceof Date)
      ) {
        // If both compared fields are native numbers or both are dates
        return a < b ? -1 : a > b ? 1 : 0;
      } else {
        // Otherwise stringify the field data and use String.prototype.localeCompare
        return new String(a).localeCompare(new String(b));
      }
    }
  }
};
</script>

