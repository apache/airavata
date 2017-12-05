<template>
    <div>

        <b-button :size="size" :variant="variant">
                Create New Group <i class="fa fa-plus-circle" aria-hidden="true"></i>
            </b-button>
            <br><br>

        <groups-owned v-bind:groupsForOwners="groupsOwners"></groups-owned>

        <groups-member v-bind:groupsForMembers="groupsMembers"></groups-member>
    </div>
</template>

<script>

import GroupsOwned from './GroupsOwned.vue';
import GroupsMember from './GroupsMember.vue';

export default {
    name: 'groups-manage-container',
    props: ['groupsDataOwners', 'groupsDataMembers'],
    components: {
        GroupsOwned,
        GroupsMember,
    },
    data () {
        return {
            size: "lg",
            variant: "success"
        }
    },
    computed: {
        groupsOwners: function() {
            return this.groupsDataOwners;
        },
        groupsMembers: function() {
            return this.groupsDataMembers;
        }
    }

}
</script>
