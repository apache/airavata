<template>
    <div id="share-button">
        <b-button v-b-modal.modal-share-settings :variant="'outline-primary'" :title="title">
            Share
            <b-badge>{{ totalCount }}</b-badge>
        </b-button>
        <b-modal id="modal-share-settings" title="Sharing Settings">
            <h5>Currently Shared With</h5>
            <b-table id="modal-group-table" hover :items="sharedEntity.groupPermissions" :fields="groupFields">
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
import { models } from 'django-airavata-api';

export default {
    name: "share-button",
    props: {
        value: models.SharedEntity,
        // TODO: add gatewayGroups
    },
    data: function() {
        return {
            sharedEntity: this.value ? this.value.clone() : new models.SharedEntity(),
            groupFields: [
                {key: 'name', label: 'Group Name'},
                {key: 'permission', label: 'Permission Settings'},
                {key: 'remove', label: 'Remove'},
            ],
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
        removeGroup: function(group) {
            this.sharedEntity.groupPermissions = this.sharedEntity.groupPermissions.filter(
                groupPermission => groupPermission.group.id !== group.id);
        }
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