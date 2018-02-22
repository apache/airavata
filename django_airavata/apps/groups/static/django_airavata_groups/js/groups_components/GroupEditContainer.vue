<template>
    <group-editor v-if="group" :group="group"></group-editor>
</template>

<script>

import GroupEditor from './GroupEditor.vue';

import { models, services } from 'django-airavata-api'
import { components as comps } from 'django-airavata-common-ui'

export default {
    name: 'group-edit-container',
    props: {
        groupId: {
            type: String,
            required: true,
        }
    },
    data () {
        return {
            group: null,
        }
    },
    components: {
        GroupEditor,
    },
    methods: {
    },
    computed: {
    },
    mounted: function () {
        services.GroupService.get(this.groupId)
            .then(group => this.group = group);
    },
}
</script>
