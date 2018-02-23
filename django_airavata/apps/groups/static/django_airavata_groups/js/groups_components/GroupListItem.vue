<template>
    <tr>
        <td>{{ group.name }}</td>
        <td>{{ group.ownerId }}</td>
        <td>{{ group.description }}</td>
        <td>
            <a v-if="group.isOwner || group.isAdmin"
                :href="'/groups/edit/' + encodeURIComponent(group.id) + '/'">
                Edit <i class="fa fa-pencil"></i>
            </a>
        </td>
    </tr>
</template>

<script>

export default {
    name: 'group-list-item',
    props: ['group'],
}
</script>

<style>
</style>
