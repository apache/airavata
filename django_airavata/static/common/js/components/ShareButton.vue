<template>
    <b-button :variant="'outline-primary'" :title="title">
        Share
        <b-badge>{{ totalCount }}</b-badge>
    </b-button>
</template>

<script>
import { models } from 'django-airavata-api';

export default {
    name: "share-button",
    props: {
        value: models.SharedEntity,
        // TODO: add gatewayGroups
    },
    computed: {
        title: function() {
            return "Shared with " + this.groupsCount + " groups"
                + (this.groupsCount > 0 ? " (" + this.groupNames.join(", ") + ")" : "")
                + " and " + this.usersCount + " users"
                + (this.usersCount > 0 ? " (" + this.userNames.join(", ") + ")" : "");
        },
        usersCount: function() {
            return this.value ? this.value.userPermissions.length : 0;
        },
        userNames: function() {
            return this.value
                ? this.value.userPermissions.map(userPerm => userPerm.user.firstName + " " + userPerm.user.lastName)
                : null;
        },
        groupNames: function() {
            return this.value
                ? this.value.groupPermissions.map(groupPerm => groupPerm.group.name)
                : null;
        },
        groupsCount: function() {
            return this.value ? this.value.groupPermissions.length: 0;
        },
        totalCount: function() {
            return this.usersCount + this.groupsCount;
        },
    }
}
</script>

<style scoped>
button {
    background-color: white;
}
</style>