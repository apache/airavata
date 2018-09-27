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
            {{ slotProps.suggestion.user.firstName }} {{ slotProps.suggestion.user.lastName }} ({{ slotProps.suggestion.user.userId }})
            - {{ slotProps.suggestion.user.email }}
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
        <a href="#" @click.prevent="removeUser(data.item.user)">
          <span class="fa fa-trash"></span>
        </a>
      </template>
    </b-table>
    <b-table v-if="groupsCount > 0" id="modal-group-table" hover :items="filteredGroupPermissions" :fields="groupFields">
      <template slot="name" slot-scope="data">
        {{data.item.group.name}}
      </template>
      <template slot="permission" slot-scope="data">
        <b-form-select v-model="data.item.permissionType" :options="permissionOptions" />
      </template>
      <template slot="remove" slot-scope="data">
        <a href="#" @click.prevent="removeGroup(data.item.group)">
          <span class="fa fa-trash"></span>
        </a>
      </template>
    </b-table>
  </div>
</template>

<script>
import { models, services } from "django-airavata-api";
import AutocompleteTextInput from "./AutocompleteTextInput.vue";
import VModelMixin from "../mixins/VModelMixin";

export default {
  name: "shared-entity-editor",
  mixins: [VModelMixin],
  props: {
    value: {
      type: models.SharedEntity
    }
  },
  components: {
    AutocompleteTextInput
  },
  data: function() {
    return {
      userFields: [
        { key: "name", label: "User Name" },
        { key: "email", label: "Email" },
        { key: "permission", label: "Permission" },
        { key: "remove", label: "Remove" }
      ],
      groupFields: [
        { key: "name", label: "Group Name" },
        { key: "permission", label: "Permission" },
        { key: "remove", label: "Remove" }
      ],
      users: [],
      groups: []
    };
  },
  computed: {
    usersCount: function() {
      return this.data && this.data.userPermissions
        ? this.data.userPermissions.length
        : 0;
    },
    filteredGroupPermissions: function() {
      return this.data && this.data.groupPermissions
        ? this.data.groupPermissions.filter(
            grp =>
              !grp.group.isGatewayAdminsGroup &&
              !grp.group.isReadOnlyGatewayAdminsGroup
          )
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
        .filter(
          group =>
            currentGroupIds.indexOf(group.id) < 0 &&
            !group.isGatewayAdminsGroup &&
            !group.isReadOnlyGatewayAdminsGroup
        )
        .map(group => {
          return {
            id: group.id,
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
            id: user.airavataInternalUserId,
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
      this.data.userPermissions = this.data.userPermissions.filter(
        userPermission =>
          userPermission.user.airavataInternalUserId !==
          user.airavataInternalUserId
      );
    },
    removeGroup: function(group) {
      this.data.groupPermissions = this.data.groupPermissions.filter(
        groupPermission => groupPermission.group.id !== group.id
      );
    },
    suggestionSelected: function(suggestion) {
      if (suggestion.type === "group") {
        const group = this.groups.find(group => group.id === suggestion.id);
        this.addGroup(group);
      } else if (suggestion.type === "user") {
        const user = this.users.find(
          user => user.airavataInternalUserId === suggestion.id
        );
        if (!this.data.userPermissions) {
          this.data.userPermissions = [];
        }
        this.data.userPermissions.push(
          new models.UserPermission({
            user: user,
            permissionType: models.ResourcePermissionType.READ
          })
        );
      }
    },
    addGroup: function(group) {
      if (!this.data.groupPermissions) {
        this.data.groupPermissions = [];
      }
      if (!this.data.groupPermissions.find(gp => gp.group.id === group.id)) {
        this.data.groupPermissions.push(
          new models.GroupPermission({
            group: group,
            permissionType: models.ResourcePermissionType.READ
          })
        );
      }
    }
  },
  mounted: function() {
    // Load all of the groups and users
    services.ServiceFactory.service("Groups")
      .list({ limit: -1 })
      .then(groups => {
        this.groups = groups;
      });
    services.ServiceFactory.service("UserProfiles")
      .list()
      .then(users => (this.users = users));
  }
};
</script>

