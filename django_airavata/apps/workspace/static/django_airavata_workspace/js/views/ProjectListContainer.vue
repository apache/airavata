<template>
    <project-list v-bind:projects="projects"></project-list>
</template>

<script>
import ProjectList from './ProjectList.vue'

export default {
    props: ['initialProjects'],
    name: 'project-list-container',
    data () {
        return {
            projects: this.initialProjects
        }
    },
    components: {
        ProjectList
    }
}
</script>

<style>
</style>
