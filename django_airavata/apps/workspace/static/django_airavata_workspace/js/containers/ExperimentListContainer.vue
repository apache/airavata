<template>
    <div>
        <div class="row">
            <div class="col">
                <h1 class="h4 mb-4">Browse Experiments</h1>
            </div>
        </div>
        <div class="row">
            <div class="col">
                <div class="card">
                    <div class="card-body">
                        <table class="table table-hover">
                            <thead>
                                <tr>
                                    <th>Name</th>
                                    <th>Creation Time</th>
                                    <th>Status</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr v-for="experiment in experiments" :key="experiment.experimentId">
                                    <td>{{experiment.name}}</td>
                                    <td><span :title="experiment.creationTime">{{ fromNow(experiment.creationTime) }}</span></td>
                                    <td><experiment-status-badge :statusName="experiment.experimentStatus" /></td>
                                    <td>
                                        <a :href="viewLink(experiment)">View <i class="fa fa-bar-chart" aria-hidden="true"></i></a>
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                        <pager v-bind:paginator="experimentsPaginator"
                        v-on:next="nextExperiments" v-on:previous="previousExperiments"></pager>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>

import { models, services } from 'django-airavata-api'
import { components as comps } from 'django-airavata-common-ui'

import ExperimentStatusBadge from '../components/experiment/ExperimentStatusBadge.vue'

import moment from 'moment';

export default {
    props: ['initialExperimentsData'],
    name: 'experiment-list-container',
    data () {
        return {
            experimentsPaginator: null,
        }
    },
    components: {
        'pager': comps.Pager,
        'experiment-status-badge': ExperimentStatusBadge,
    },
    methods: {
        nextExperiments: function(event) {
            this.experimentsPaginator.next();
        },
        previousExperiments: function(event) {
            this.experimentsPaginator.previous();
        },
        fromNow: function(date) {
            return moment(date).fromNow();
        },
        viewLink: function(experiment) {
            return '/workspace/experiments/' + encodeURIComponent(experiment.experimentId) + '/';
        }
    },
    computed: {
        experiments: function() {
            return this.experimentsPaginator ? this.experimentsPaginator.results : null;
        },
    },
    beforeMount: function () {
        services.ExperimentSearchService.list(this.initialExperimentsData)
            .then(result => {
                this.experimentsPaginator = result
                console.log("experiments", result.results);
            });
    }
}
</script>

<style>
</style>
