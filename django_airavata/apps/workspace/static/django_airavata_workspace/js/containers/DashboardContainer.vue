<template>
    <div>
        <div class="row">
            <div class="col">
                <h1 class="h4 mb-4">Dashboard</h1>
            </div>
        </div>
        <div class="row">
            <application-card v-for="item in applicationModules" v-bind:appModule="item"
                v-bind:key="item.appModuleId" @app-selected="handleAppSelected">
            </application-card>
        </div>

    </div>
</template>

<script>

import { services } from 'django-airavata-api'
import { components as comps } from 'django-airavata-common-ui'
import urls from "../utils/urls";

export default {
    name: 'dashboard-container',
    data () {
        return {
            applicationModules: null,
        }
    },
    components: {
        'application-card': comps.ApplicationCard,
    },
    methods: {
        handleAppSelected: function(appModule) {
            urls.navigateToCreateExperiment(appModule);
        },
    },
    beforeMount: function () {
        services.ApplicationModuleService.list()
            .then(result => this.applicationModules = result);
    }
}
</script>
