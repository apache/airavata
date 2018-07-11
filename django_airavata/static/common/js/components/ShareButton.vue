<template>
    <div id="share-button">
        <b-button v-b-modal.modal-share-settings :variant="'outline-primary'" :title="title">
            Share
            <b-badge>{{ totalCount }}</b-badge>
        </b-button>
        <b-modal id="modal-share-settings" title="Sharing Settings">
            <b-form-group label="Search for users/groups" labelFor="user-groups-autocomplete">
                <autocomplete-text-input id="user-groups-autocomplete"
                    :suggestions="usersAndGroupsSuggestions"
                    @selected="suggestionSelected">
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
            <b-table v-if="usersCount > 0" id="modal-user-table" hover :items="sharedEntity.userPermissions" :fields="userFields">
                <template slot="name" slot-scope="data">
                    <span :title="data.item.user.userId">{{data.item.user.firstName}} {{data.item.user.lastName}}</span>
                </template>
                <template slot="email" slot-scope="data">
                    {{data.item.user.email}}
                </template>
                <template slot="permission" slot-scope="data">
                    <b-form-select v-model="data.item.permissionType" :options="permissionOptions"/>
                </template>
                <template slot="remove" slot-scope="data">
                    <a href="#" @click.prevent="removeUser(data.item.user)"><span class="fa fa-trash"></span></a>
                </template>
            </b-table>
            <b-table v-if="groupsCount > 0" id="modal-group-table" hover :items="sharedEntity.groupPermissions" :fields="groupFields">
                <template slot="name" slot-scope="data">
                    {{data.item.group.name}}
                </template>
                <template slot="permission" slot-scope="data">
                    <b-form-select v-model="data.item.permissionType" :options="permissionOptions"/>
                </template>
                <template slot="remove" slot-scope="data">
                    <a href="#" @click.prevent="removeGroup(data.item.group)"><span class="fa fa-trash"></span></a>
                </template>
            </b-table>
        </b-modal>
    </div>
</template>

<script>
import { models, services } from 'django-airavata-api';
import AutocompleteTextInput from './AutocompleteTextInput.vue';

export default {
    name: "share-button",
    props: {
        value: models.SharedEntity,
        // TODO: add gatewayGroups
    },
    components: {
        AutocompleteTextInput,
    },
    data: function() {
        return {
            sharedEntity: this.value ? this.value.clone() : new models.SharedEntity(),
            userFields: [
                {key: 'name', label: 'User Name'},
                {key: 'email', label: 'Email'},
                {key: 'permission', label: 'Permission'},
                {key: 'remove', label: 'Remove'},
            ],
            groupFields: [
                {key: 'name', label: 'Group Name'},
                {key: 'permission', label: 'Permission'},
                {key: 'remove', label: 'Remove'},
            ],
            users: [],
            groups: [],
        }
    },
    computed: {
        title: function() {
            return "Shared with " + this.groupsCount + " groups"
                + (this.groupsCount > 0 ? " (" + this.groupNames.join(", ") + ")" : "")
                + " and " + this.usersCount + " users"
                + (this.usersCount > 0 ? " (" + this.userNames.join(", ") + ")" : "");
        },
        usersCount: function() {
            return this.sharedEntity ? this.sharedEntity.userPermissions.length : 0;
        },
        userNames: function() {
            return this.sharedEntity
                ? this.sharedEntity.userPermissions.map(userPerm => userPerm.user.firstName + " " + userPerm.user.lastName)
                : null;
        },
        groupNames: function() {
            return this.sharedEntity
                ? this.sharedEntity.groupPermissions.map(groupPerm => groupPerm.group.name)
                : null;
        },
        groupsCount: function() {
            return this.sharedEntity ? this.sharedEntity.groupPermissions.length: 0;
        },
        totalCount: function() {
            return this.usersCount + this.groupsCount;
        },
        permissionOptions: function() {
            return [models.ResourcePermissionType.READ,
                    models.ResourcePermissionType.WRITE].map(perm => {
                return {
                    value: perm,
                    text: perm.name,
                }
            })
        },
        groupSuggestions: function() {
            // filter out already selected groups
            const currentGroupIds = this.sharedEntity.groupPermissions.map(groupPerm => groupPerm.group.id);
            return this.groups
                .filter(group => currentGroupIds.indexOf(group.id) < 0)
                .map(group => {
                    return {
                        id: group.id,
                        name: group.name,
                        type: 'group',
                    }
                });
        },
        userSuggestions: function() {
            // filter out already selected users
            const currentUserIds = this.sharedEntity.userPermissions.map(userPerm => userPerm.user.airavataInternalUserId);
            return this.users
                .filter(user => currentUserIds.indexOf(user.airavataInternalUserId) < 0)
                .map(user => {
                    return {
                        id: user.airavataInternalUserId,
                        name: user.firstName + ' ' + user.lastName + ' (' + user.userId + ') ' + user.email,
                        user: user,
                        type: 'user',
                    }
                });
        },
        usersAndGroupsSuggestions: function() {
            return this.userSuggestions.concat(this.groupSuggestions);
        }
    },
    methods: {
        /**
         * Merge the given persisted SharedEntity with the local SharedEntity
         * instance and save it, returning a Promise.
         */
        merge: function(sharedEntity) {
            // TODO: implement this
        },
        removeUser: function(user) {
            this.sharedEntity.userPermissions = this.sharedEntity.userPermissions.filter(
                userPermission => userPermission.user.airavataInternalUserId !== user.airavataInternalUserId);
        },
        removeGroup: function(group) {
            this.sharedEntity.groupPermissions = this.sharedEntity.groupPermissions.filter(
                groupPermission => groupPermission.group.id !== group.id);
        },
        suggestionSelected: function(suggestion) {
            if (suggestion.type === 'group') {
                const group = this.groups.find(group => group.id === suggestion.id);
                this.sharedEntity.groupPermissions.push(new models.GroupPermission({
                    'group': group,
                    'permissionType': models.ResourcePermissionType.READ
                }));
            } else if (suggestion.type === 'user') {
                const user = this.users.find(user => user.airavataInternalUserId === suggestion.id);
                this.sharedEntity.userPermissions.push(new models.UserPermission({
                    'user': user,
                    'permissionType': models.ResourcePermissionType.READ
                }));
            }
        }
    },
    mounted: function() {
        // Load all of the groups and users
        services.ServiceFactory.service("Groups").list({limit: -1}).then(groups => this.groups = groups);
        services.ServiceFactory.service("UserProfiles").list().then(users => this.users = users);
    }
}
</script>

<style scoped>
button {
    background-color: white;
}
#share-button >>> #modal-share-settings .modal-body {
    max-height: 50vh;
    overflow: auto;
}
</style>