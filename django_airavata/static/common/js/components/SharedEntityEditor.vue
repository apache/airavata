<template>
  <div>
    <b-form-group label="Search for users/groups" labelFor="user-groups-autocomplete">
      <autocomplete-text-input id="user-groups-autocomplete" :suggestions="usersAndGroupsSuggestions" @selected="suggestionSelected">
        <template slot="suggestion" slot-scope="slotProps">
          <span v-if="slotProps.suggestion.type == 'group'">
            <i class="fa fa-users"></i> {{ slotProps.suggestion.name }}
          </span>
          <span v-if="slotProps.suggestion.type == 'user'">
            <i class="fa fa-user"></i>
            {{ slotProps.suggestion.user.firstName }} {{ slotProps.suggestion.user.lastName }}
            ({{ slotProps.suggestion.user.userId }}) - {{ slotProps.suggestion.user.email }}
          </span>
        </template>
      </autocomplete-text-input>
    </b-form-group>
    <h5 v-if="totalCount > 0">Currently Shared With</h5>
    <b-table v-if="usersCount > 0" id="modal-user-table" hover :items="data.userPermissions" :fields="userFields">
      <template slot="name" slot-scope="data">
        <span :title="data.item.user.userId">{{data.item.user.firstName}} {{data.item.user.lastName}}</span>
      </template>
      <template slot="email" slot-scope="data">
        {{data.item.user.email}}
      </template>
      <template slot="permission" slot-scope="data">
        <b-form-select v-model="data.item.permissionType" :options="permissionOptions" />
      </template>
      <template slot="remove" slot-scope="data">
        <b-link @click="removeUser(data.item.user)">
          <span class="fa fa-trash"></span>
        </b-link>
      </template>
    </b-table>
    <b-table v-if="groupsCount > 0" id="modal-group-table" hover :items="filteredGroupPermissions" :fields="groupFields">
      <template slot="name" slot-scope="data">
        {{data.item.group.name}}
      </template>
      <template slot="permission" slot-scope="data">
        <b-form-select v-if="editingAllowed(data.item.group)" v-model="data.item.permissionType" :options="permissionOptions" />
        <span v-else>{{ data.item.permissionType.name }}</span>
      </template>
      <template slot="remove" slot-scope="data">
        <b-link v-if="editingAllowed(data.item.group)" @click="removeGroup(data.item.group)">
          <span class="fa fa-trash"></span>
        </b-link>
      </template>
    </b-table>
  </div>
</template>

<script>
import { models } from "django-airavata-api";
import AutocompleteTextInput from "./AutocompleteTextInput.vue";
import VModelMixin from "../mixins/VModelMixin";

export default {
  name: "shared-entity-editor",
  mixins: [VModelMixin],
  props: {
    value: {
      type: models.SharedEntity
    },
    users: {
      type: Array,
      required: true
    },
    groups: {
      type: Array,
      required: true
    },
    disallowEditingAdminGroups: {
      type: Boolean,
      default: true
    }
  },
  components: {
    AutocompleteTextInput
  },
  computed: {
    userFields: function() {
      return [
        { key: "name", label: "User Name" },
        { key: "email", label: "Email" },
        { key: "permission", label: "Permission" },
        { key: "remove", label: "Remove" }
      ];
    },
    groupFields: function() {
      return [
        { key: "name", label: "Group Name" },
        { key: "permission", label: "Permission" },
        { key: "remove", label: "Remove" }
      ];
    },
    usersCount: function() {
      return this.data && this.data.userPermissions
        ? this.data.userPermissions.length
        : 0;
    },
    filteredGroupPermissions: function() {
      return this.data && this.data.groupPermissions
        ? this.data.groupPermissions
        : [];
    },
    groupsCount: function() {
      return this.filteredGroupPermissions.length;
    },
    totalCount: function() {
      return this.usersCount + this.groupsCount;
    },
    permissionOptions: function() {
      return [
        models.ResourcePermissionType.READ,
        models.ResourcePermissionType.WRITE
      ].map(perm => {
        return {
          value: perm,
          text: perm.name
        };
      });
    },
    groupSuggestions: function() {
      // filter out already selected groups
      const currentGroupIds = this.filteredGroupPermissions.map(
        groupPerm => groupPerm.group.id
      );
      return this.groups
        .filter(group => currentGroupIds.indexOf(group.id) < 0)
        .map(group => {
          return {
            // AIRAVATA-2927 Due to bad data, sometimes a user and a group will
            // have the same id. To prevent issues distinguish them by appending
            // the type of suggestion (-group)
            id: group.id + '-group',
            name: group.name,
            type: "group"
          };
        });
    },
    userSuggestions: function() {
      // filter out already selected users
      const currentUserIds = this.data.userPermissions
        ? this.data.userPermissions.map(
            userPerm => userPerm.user.airavataInternalUserId
          )
        : [];
      return this.users
        .filter(user => currentUserIds.indexOf(user.airavataInternalUserId) < 0)
        .map(user => {
          return {
            // AIRAVATA-2927 Due to bad data, sometimes a user and a group will
            // have the same id. To prevent issues distinguish them by appending
            // the type of suggestion (-user)
            id: user.airavataInternalUserId + '-user',
            name:
              user.firstName +
              " " +
              user.lastName +
              " (" +
              user.userId +
              ") " +
              user.email,
            user: user,
            type: "user"
          };
        });
    },
    usersAndGroupsSuggestions: function() {
      return this.userSuggestions.concat(this.groupSuggestions);
    }
  },
  methods: {
    removeUser: function(user) {
      this.data.removeUser(user);
    },
    removeGroup: function(group) {
      this.data.removeGroup(group);
    },
    suggestionSelected: function(suggestion) {
      if (suggestion.type === "group") {
        const group = this.groups.find(group => group.id === suggestion.id);
        this.data.addGroup(group);
      } else if (suggestion.type === "user") {
        const user = this.users.find(
          user => user.airavataInternalUserId === suggestion.id
        );
        this.data.addUser(user);
      }
    },
    /**
     * For some entity types the backend automatically shares the entity with
     * admin users and doesn't allow editing or removing those admin groups.
     * For that reason the disallowEditingAdminGroups property was added and
     * when it is true editing of the "Admins" and "Read Only Admins" groups
     * should not be allowed.
     */
    editingAllowed(group) {
      return (
        !this.disallowEditingAdminGroups ||
        !(group.isGatewayAdminsGroup || group.isReadOnlyGatewayAdminsGroup)
      );
    }
  }
};
</script>

