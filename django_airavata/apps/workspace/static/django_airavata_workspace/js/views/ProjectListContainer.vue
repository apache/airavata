<template>
    <div class="card">
        <div class="card-body">
            <project-list v-bind:projects="projects"></project-list>
            <pager v-bind:paginator="projectsPaginator"
                v-on:next="nextProjects" v-on:previous="previousProjects"></pager>
        </div>
    </div>
</template>

<script>
import ProjectList from './ProjectList.vue'

import { services } from 'django-airavata-api'
import { components as comps } from 'django-airavata-common'

export default {
    props: ['initialProjectsData'],
    name: 'project-list-container',
    data () {
        return {
            projectsPaginator: null,
        }
    },
    components: {
        'project-list': ProjectList,
        'pager': comps.Pager
    },
    methods: {
        nextProjects: function(event) {
            this.projectsPaginator.next();
        },
        previousProjects: function(event) {
            this.projectsPaginator.previous();
        },
    },
    computed: {
        projects: function() {
            return this.projectsPaginator ? this.projectsPaginator.results : null;
        },
    },
    beforeMount: function () {
        services.ProjectService.list(this.initialProjectsData)
            .then(result => this.projectsPaginator = result);
    }
}
</script>

<style>
</style>
