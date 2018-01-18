<template>
    <experiment-summary v-if="experiment" :experiment="experiment">
    </experiment-summary>
</template>

<script>

import {models, services} from 'django-airavata-api'
import ExperimentSummary from '../components/experiment/ExperimentSummary.vue'

export default {
    name: 'view-experiment-container',
    props: {
        initialExperimentData: {
            required: true
        },
    },
    data () {
        return {
            experiment: null,
        }
    },
    components: {
        ExperimentSummary,
    },
    methods: {
    },
    computed: {
    },
    beforeMount: function () {
        services.ExperimentService.get(this.initialExperimentData.experimentId, this.initialExperimentData)
            .then(exp => this.experiment = exp);
    }
}
</script>
<style>
</style>
