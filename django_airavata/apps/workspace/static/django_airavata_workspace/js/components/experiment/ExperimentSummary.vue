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
                                    <td><span :title="experiment.experimentId">{{ experiment.experimentName }}</span></td>
                                </tr>
                                <tr>
                                    <th scope="row">Description</th>
                                    <td>{{ experiment.description }}</td>
                                </tr>
                                <tr>
                                    <th scope="row">Project</th>
                                    <td>{{ fullExperiment.projectName }}</td>
                                </tr>
                                <tr>
                                    <th scope="row">Outputs</th>
                                    <td>
                                        <template v-for="output in fullExperiment.outputDataProducts">
                                            {{ output.filename }}
                                        </template>
                                    </td>
                                </tr>
                                <!-- Going to leave this out for now -->
                                <!-- <tr>
                                    <th scope="row">Storage Directory</th>
                                    <td></td>
                                </tr> -->
                                <tr>
                                    <th scope="row">Owner</th>
                                    <td>{{ experiment.userName }}</td>
                                </tr>
                                <tr>
                                    <th scope="row">Application</th>
                                    <td>{{ fullExperiment.applicationName }}</td>
                                </tr>
                                <tr>
                                    <th scope="row">Compute Resource</th>
                                    <td>{{ fullExperiment.computeHostName }}</td>
                                </tr>
                                <tr>
                                    <th scope="row">Experiment Status</th>
                                    <td>{{ fullExperiment.experimentStatusName }}</td>
                                </tr>
                                <!--  TODO: leave this out for now -->
                                <!-- <tr>
                                    <th scope="row">Notification List</th>
                                    <td>{{ fullExperiment.experiment.emailAddresses
                                            ? fullExperiment.experiment.emailAddresses.join(", ")
                                            : '' }}</td>
                                </tr> -->
                                <tr>
                                    <th scope="row">Creation Time</th>
                                    <td><span :title="experiment.creationTime.toString()">{{ creationTime }}</span></td>
                                </tr>
                                <tr>
                                    <th scope="row">Last Modified Time</th>
                                    <td><span :title="fullExperiment.experimentStatus.timeOfStateChange.toString()">{{ lastModifiedTime }}</span></td>
                                </tr>
                                <tr>
                                    <th scope="row">Wall Time Limit</th>
                                    <td>{{ experiment.userConfigurationData.computationalResourceScheduling.wallTimeLimit }} minutes</td>
                                </tr>
                                <tr>
                                    <th scope="row">CPU Count</th>
                                    <td>{{ experiment.userConfigurationData.computationalResourceScheduling.totalCPUCount }}</td>
                                </tr>
                                <tr>
                                    <th scope="row">Node Count</th>
                                    <td>{{ experiment.userConfigurationData.computationalResourceScheduling.nodeCount }}</td>
                                </tr>
                                <tr>
                                    <th scope="row">Queue</th>
                                    <td>{{ experiment.userConfigurationData.computationalResourceScheduling.queueName }}</td>
                                </tr>
                                <tr>
                                    <!-- TODO -->
                                    <th scope="row">Inputs</th>
                                    <td>
                                        <template v-for="input in fullExperiment.inputDataProducts">
                                            {{ input.filename }}
                                        </template>
                                    </td>
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
        fullExperiment: {
            type: models.FullExperiment,
            required: true
        },
    },
    data () {
        return {
            localFullExperiment: this.fullExperiment.clone(),
        }
    },
    components: {
    },
    computed: {
        creationTime: function() {
            return moment(this.localFullExperiment.experiment.creationTime).fromNow();
        },
        lastModifiedTime: function() {
            return moment(this.localFullExperiment.experimentStatus.timeOfStateChange).fromNow();
        },
        experiment: function() {
            return this.localFullExperiment.experiment;
        }
    },
    methods: {
        loadExperiment: function() {
            return services.FullExperimentService.get(this.localFullExperiment.experiment.experimentId)
                .then(exp => this.localFullExperiment = exp);
        },
        initPollingExperiment: function() {
            var pollExperiment = function() {
                this.loadExperiment()
                    .then(exp => {
                        setTimeout(pollExperiment.bind(this), 3000);
                    })
            }.bind(this);
            setTimeout(pollExperiment, 3000);
        }
    },
    watch: {
    },
    mounted: function() {
        this.initPollingExperiment();
    }
}
</script>

<style>
</style>
