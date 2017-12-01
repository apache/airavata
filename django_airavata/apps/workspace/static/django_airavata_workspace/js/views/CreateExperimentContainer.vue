<template>
    <experiment-editor :experiment="experiment"></experiment-editor>
</template>

<script>

import {models, services} from 'django-airavata-api'
import ExperimentEditor from './ExperimentEditor.vue'

export default {
    name: 'create-experiment-container',
    data () {
        return {
            'experiment': new models.Experiment(),
        }
    },
    components: {
        'experiment-editor': ExperimentEditor,
    },
    methods: {
    },
    computed: {
    },
    beforeMount: function () {
        this.experiment.experimentName = 'XX Exp Name';
    }
}
</script>
