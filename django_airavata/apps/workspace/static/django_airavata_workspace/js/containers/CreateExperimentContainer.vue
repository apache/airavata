<template>
    <experiment-editor v-if="appModule && appInterface" :experiment="experiment" :app-module="appModule" :app-interface="appInterface">
        <span slot="title">Create a New Experiment</span>
    </experiment-editor>
</template>

<script>

import {models, services} from 'django-airavata-api'
import ExperimentEditor from '../components/experiment/ExperimentEditor.vue'

import moment from 'moment';

export default {
    name: 'create-experiment-container',
    props: [
        'app-module-id',
    ],
    data () {
        return {
            'experiment': new models.Experiment(),
            'appModule': null,
            'appInterface': null,
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
                this.experiment.experimentName = appModule.appModuleName + ' on ' + moment().format('lll');
                this.appModule = appModule;
            });
        services.ApplicationInterfaceService.getForAppModuleId(this.appModuleId)
            .then(appInterface => {
                this.experiment.experimentInputs = appInterface.getOrderedApplicationInputs().map(input => input.clone());
                this.appInterface = appInterface;
                this.experiment.executionId = this.appInterface.applicationInterfaceId;
            });
    }
}
</script>
