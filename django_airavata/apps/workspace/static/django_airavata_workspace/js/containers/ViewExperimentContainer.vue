<template>
    <experiment-summary v-if="fullExperiment" :fullExperiment="fullExperiment"
    :launching="launch" @Launched="handleLaunchedExperiment"
    :key="componentKey">
    </experiment-summary>
</template>

<script>

import {services} from 'django-airavata-api'
import ExperimentSummary from '../components/experiment/ExperimentSummary.vue'

export default {
    name: 'view-experiment-container',
    props: {
        initialFullExperimentData: {
            required: true
        },
        launching: {
            type: Boolean,
            default: false,
        }
    },
    data () {
        return {
            fullExperiment: null,
            componentKey: 0,
            launch: this.launching,
        }
    },
    components: {
        ExperimentSummary,
    },
    methods: {
      handleLaunchedExperiment: function() {
        this.launch = true;
        this.componentKey = 1;
      }
    },
    computed: {
    },
    beforeMount: function () {
        services.FullExperimentService.retrieve({lookup: this.initialFullExperimentData.experimentId, initialFullExperimentData: this.initialFullExperimentData})
            .then(exp => this.fullExperiment = exp);
    }
}
</script>
<style>
</style>
