<template>
    <ul id="project-list">
        <project-list-item v-bind:project="project" v-for="project in projects" v-bind:key="project.projectID">
        </project-list-item>
    </ul>
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
