<template>
    <table class="table table-hover">
        <thead>
            <tr>
                <th>Name</th>
                <th>Actions</th>
            </tr>
        </thead>
        <tbody>
            <group-list-item v-bind:project="group" v-for="group in groupsForOwners" v-bind:key="group.groupID">
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
        }
    },
    components: {
        GroupListItem
    }
}
</script>
