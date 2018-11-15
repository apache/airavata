<template>
  <div>
    <b-form-group label="Add members" labelFor="user-autocomplete">
      <autocomplete-text-input id="user-autocomplete" :suggestions="suggestions" @selected="suggestionSelected" />
    </b-form-group>
    <b-table v-if="membersCount > 0" hover :items="currentMembers" :fields="fields">
      <template slot="role" slot-scope="data">
        <b-form-select :value="data.item.role" @input="changeRole(data.item, $event)" :options="groupRoleOptions">
        </b-form-select>
      </template>
      <template slot="remove" slot-scope="data">
        <b-link @click="removeMember(data.item)">
          <span class="fa fa-trash"></span>
        </b-link>
      </template>
    </b-table>
  </div>
</template>

<script>
import { services } from "django-airavata-api";
import { components } from "django-airavata-common-ui";

export default {
  name: "group-members-editor",
  components: {
    "autocomplete-text-input": components.AutocompleteTextInput
  },
  props: {
    members: {
      type: Array,
      required: true
    },
    admins: {
      type: Array,
      required: true
    }
  },
  data() {
    return {
      userProfiles: null
    };
  },
  computed: {
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
        { key: "name", label: "Name" },
        { key: "username", label: "Username" },
        { key: "email", label: "Email" },
        { key: "role", label: "Role" },
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
      return this.members.map(m => {
        const userProfile = this.userProfilesMap[m];
        const isAdmin = this.admins.indexOf(m) >= 0;
        return {
          id: m,
          name: userProfile.firstName + " " + userProfile.lastName,
          username: userProfile.userId,
          email: userProfile.email,
          role: isAdmin ? "ADMIN" : "MEMBER"
        };
      });
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

