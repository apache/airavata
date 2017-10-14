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
import Project from '../models/Project';
import ProjectList from './ProjectList.vue'

export default {
    props: ['initialProjectsData'],
    name: 'project-list-container',
    data () {
        return {
            projects: this.initialProjectsData.results,
            next: this.initialProjectsData.next,
            previous: this.initialProjectsData.previous,
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
    }
}
</script>

<style>
</style>
