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
                                    <th scope="row">Application</th>
                                    <td></td>
                                </tr>
                                <tr>
                                    <th scope="row">Compute Resource</th>
                                    <td></td>
                                </tr>
                                <tr>
                                    <th scope="row">Experiment Status</th>
                                    <td>{{ localExperiment.experimentStatus[0].stateName }}</td>
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
        console.log(JSON.stringify(this.experiment.experimentOutputs))
        this.loadProject();
    }
}
</script>

<style>
</style>
