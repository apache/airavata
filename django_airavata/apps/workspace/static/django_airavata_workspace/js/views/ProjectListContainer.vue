<template>
    <div>
        <project-list v-bind:projects="projects"></project-list>
        <div>
            Showing {{ first }} - {{ last }}
        </div>
        <div v-if="hasNext">
            <a href="#" v-on:click.prevent="nextProjects">Next</a>
        </div>
        <div v-if="hasPrevious">
            <a href="#" v-on:click.prevent="previousProjects">Previous</a>
        </div>
    </div>
</template>

<script>
import ProjectList from './ProjectList.vue'

import { services } from 'django-airavata-common'

export default {
    props: ['initialProjectsData'],
    name: 'project-list-container',
    data () {
        return {
            projectsPaginator: null,
        }
    },
    components: {
        ProjectList
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
        hasNext: function() {
            return this.projectsPaginator && this.projectsPaginator.hasNext();
        },
        hasPrevious: function() {
            return this.projectsPaginator && this.projectsPaginator.hasPrevious();
        },
        first: function() {
            return this.projectsPaginator ? this.projectsPaginator.offset + 1 : null;
        },
        last: function() {
            if (this.projectsPaginator) {
                return this.projectsPaginator.offset + this.projectsPaginator.results.length;
            } else {
                return null;
            }
        }
    },
    beforeMount: function () {
        services.ProjectService.list(this.initialProjectsData)
            .then(result => this.projectsPaginator = result);
    }
}
</script>

<style>
</style>
