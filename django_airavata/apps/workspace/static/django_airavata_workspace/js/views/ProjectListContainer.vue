<template>
    <div>
        <project-list v-bind:projects="projects"></project-list>
        <div v-if="next">
            <a href="#" v-on:click.prevent="nextProjects">Next</a>
        </div>
        <div v-if="previous">
            <a href="#" v-on:click.prevent="previousProjects">Previous</a>
        </div>
    </div>
</template>

<script>
import Project from '../models/Project'
import ProjectList from './ProjectList.vue'
import ProjectService from '../services/ProjectService'

export default {
    props: ['initialProjectsData'],
    name: 'project-list-container',
    data () {
        return {
            projects: null,
            next: null,
            previous: null,
        }
    },
    components: {
        ProjectList
    },
    methods: {
        // TODO: refactor these two methods since they are practically the same
        nextProjects: function(event) {
            fetch(this.next, {
                credentials: 'include'
            })
            .then(response => response.json())
            .then(json => {
                this.next = json.next;
                this.previous = json.previous;
                this.projects = json.results.map(project => new Project(project));
            });
        },
        previousProjects: function(event) {
            fetch(this.previous, {
                credentials: 'include'
            })
            .then(response => response.json())
            .then(json => {
                this.next = json.next;
                this.previous = json.previous;
                this.projects = json.results.map(project => new Project(project));
            });
        },
    },
    beforeMount: function () {
        if (this.initialProjectsData) {
            // TODO: Initialize project list iterator
            ProjectService.list(this.initialProjectsData)
                .then(result => {
                    this.projects = result.projects;
                    this.next = result.next;
                    this.previous = result.previous;
                });
        } else {
            ProjectService.list()
                .then(result => {
                    this.projects = result.projects;
                    this.next = result.next;
                    this.previous = result.previous;
                });
        }
        // TODO: load projects if initialProjectsData is null
    }
}
</script>

<style>
</style>
