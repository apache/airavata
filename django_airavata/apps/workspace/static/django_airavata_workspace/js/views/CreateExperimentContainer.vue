<template>
    <experiment-editor :experiment="experiment"></experiment-editor>
</template>

<script>

import {models, services} from 'django-airavata-api'
import ExperimentEditor from './ExperimentEditor.vue'

import moment from 'moment';

export default {
    name: 'create-experiment-container',
    props: [
        'app-module-id',
    ],
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
    mounted: function () {
        // TODO: integrate loading spinner
        services.ApplicationModuleService.get(this.appModuleId)
            .then(appModule => {
                this.experiment.experimentName = appModule.appModuleName + ' ' + moment().format('lll');
            });
    }
}
</script>
