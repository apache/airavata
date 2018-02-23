<template>
    <group-editor :group="newGroup" @saved="handleSaved"></group-editor>
</template>

<script>

import GroupOwnerList from './GroupOwnerList.vue';
import GroupMemberList from './GroupMemberList.vue';
import GroupEditor from './GroupEditor.vue';

import { models, services } from 'django-airavata-api'
import { components as comps } from 'django-airavata-common-ui'

export default {
    name: 'group-create-container',
    data () {
        return {
            newGroup: new models.Group(),
        }
    },
    components: {
        GroupEditor,
    },
    methods: {
        handleSaved: function(group) {
            window.location.assign("/groups/");
        }
    },
    computed: {
    },
    beforeMount: function () {
    },
}
</script>
