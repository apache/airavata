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
            <group-list-item v-bind:group="group" v-bind:type="member" v-for="group in groupsForMembers" v-bind:key="group.groupID">
            </group-list-item>
        </tbody>
    </table>
</template>

<script>
import GroupListItem from './GroupListItem.vue'

export default {
    name: 'group-member-list',
    props: ['groupsForMembers'],
    data: function () {
        return {
          member:"member",
        }
    },
    components: {
        GroupListItem
    }
}
</script>
