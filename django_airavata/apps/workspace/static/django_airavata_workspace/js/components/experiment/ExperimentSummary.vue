<template>
    <div>
        <div class="row">
            <div class="col">
                <h1 class="h4 mb-4">
                    <slot name="title">Experiment Summary</slot>
                </h1>
            </div>
        </div>
        <div class="row">
            <div class="col">
                <div class="card border-default">
                    <div class="card-body">
                        <table class="table">
                            <tbody>
                                <tr>
                                    <th scope="row">Name</th>
                                    <td><span :title="localExperiment.experimentId">{{ localExperiment.experimentName }}</span></td>
                                </tr>
                                <tr>
                                    <th scope="row">Description</th>
                                    <td>{{ localExperiment.description }}</td>
                                </tr>
                                <tr>
                                    <th scope="row">Project</th>
                                    <td>{{ project && project.name || '' }}</td>
                                </tr>
                                <tr>
                                    <!-- TODO -->
                                    <th scope="row">Outputs</th>
                                    <td></td>
                                </tr>
                                <!-- Going to leave this out for now -->
                                <!-- <tr>
                                    <th scope="row">Storage Directory</th>
                                    <td></td>
                                </tr> -->
                                <tr>
                                    <th scope="row">Owner</th>
                                    <td>{{ localExperiment.userName }}</td>
                                </tr>
                                <tr>
                                    <!-- TODO -->
                                    <th scope="row">Application</th>
                                    <td></td>
                                </tr>
                                <tr>
                                    <!-- TODO -->
                                    <th scope="row">Compute Resource</th>
                                    <td></td>
                                </tr>
                                <tr>
                                    <th scope="row">Experiment Status</th>
                                    <td>{{ experimentStatus.stateName }}</td>
                                </tr>
                                <tr>
                                    <th scope="row">Notification List</th>
                                    <td>{{ localExperiment.emailAddresses
                                            ? localExperiment.emailAddresses.join(", ")
                                            : '' }}</td>
                                </tr>
                                <tr>
                                    <th scope="row">Creation Time</th>
                                    <td><span :title="localExperiment.creationTime.toString()">{{ creationTime }}</span></td>
                                </tr>
                                <tr>
                                    <th scope="row">Last Modified Time</th>
                                    <td><span :title="experimentStatus.timeOfStateChange.toString()">{{ lastModifiedTime }}</span></td>
                                </tr>
                                <tr>
                                    <th scope="row">Wall Time Limit</th>
                                    <td>{{ localExperiment.userConfigurationData.computationalResourceScheduling.wallTimeLimit }} minutes</td>
                                </tr>
                                <tr>
                                    <th scope="row">CPU Count</th>
                                    <td>{{ localExperiment.userConfigurationData.computationalResourceScheduling.totalCPUCount }}</td>
                                </tr>
                                <tr>
                                    <th scope="row">Node Count</th>
                                    <td>{{ localExperiment.userConfigurationData.computationalResourceScheduling.nodeCount }}</td>
                                </tr>
                                <tr>
                                    <th scope="row">Queue</th>
                                    <td>{{ localExperiment.userConfigurationData.computationalResourceScheduling.queueName }}</td>
                                </tr>
                                <tr>
                                    <!-- TODO -->
                                    <th scope="row">Inputs</th>
                                    <td></td>
                                </tr>
                                <tr>
                                    <!-- TODO -->
                                    <th scope="row">Errors</th>
                                    <td></td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>

import {models, services} from 'django-airavata-api'

import moment from 'moment';

export default {
    name: 'experiment-summary',
    props: {
        experiment: {
            type: models.Experiment,
            required: true
        },
    },
    data () {
        return {
            localExperiment: this.experiment.clone(),
            project: null,
        }
    },
    components: {
    },
    computed: {
        creationTime: function() {
            return moment(this.localExperiment.creationTime).fromNow();
        },
        experimentStatus: function() {
            return this.localExperiment.experimentStatus[0];
        },
        lastModifiedTime: function() {
            return moment(this.experimentStatus.timeOfStateChange).fromNow();
        }
    },
    methods: {
        loadProject: function() {
            services.ProjectService.get(this.experiment.projectId)
                .then(proj => this.project = proj);
        },
        loadApplication: function() {

        },
        loadOutputs: function() {

        },
        loadComputeHost: function() {

        },
    },
    watch: {
    },
    mounted: function() {
        this.loadProject();
    }
}
</script>

<style>
</style>
