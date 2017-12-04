<template>
    <div>

        <b-button :size="lg" :variant="success">
                Create New Group <i class="fa fa-plus-circle" aria-hidden="true"></i>
            </b-button>
            <br><br>

        <groups-owned></groups-owned>

        <groups-member></groups-member>
    </div>
</template>

<script>

import GroupsOwned from './GroupsOwned.vue';
import GroupsMember from './GroupsMember.vue';

export default {
    name: 'groups-manage-container',

    components: {
        GroupsOwned,
        GroupsMember
    }
}
</script>
