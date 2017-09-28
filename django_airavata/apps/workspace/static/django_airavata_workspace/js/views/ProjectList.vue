<template>
    <table class="table">
        <thead>
            <tr>
                <th>Name</th>
                <th>Owner</th>
                <th>Creation Time</th>
                <th>Actions</th>
            </tr>
        </thead>
        <tbody>
            <project-list-item v-bind:project="project" v-for="project in projects" v-bind:key="project.projectID">
            </project-list-item>
        </tbody>
    </table>
</template>

<script>
import ProjectListItem from './ProjectListItem.vue'

export default {
    name: 'project-list',
    props: ['projects'],
    data: function () {
        return {
        }
    },
    components: {
        ProjectListItem
    }
}
</script>

<style>
</style>
