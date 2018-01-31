<template>
    <table class="table table-hover">
        <thead>
            <tr>
                <th>Name</th>
                <th>Owner</th>
                <th>Description</th>
                <th>Actions</th>
            </tr>
        </thead>
        <tbody>
            <group-list-item v-bind:group="group" v-bind:type="owner" v-for="group in groupsForOwners" v-bind:key="group.groupID">
            </group-list-item>
        </tbody>
    </table>
</template>

<script>
import GroupListItem from './GroupListItem.vue'

export default {
    name: 'group-owner-list',
    props: ['groupsForOwners'],
    data: function () {
        return {
          owner:"owner",
        }
    },
    components: {
        GroupListItem
    }
}
</script>
