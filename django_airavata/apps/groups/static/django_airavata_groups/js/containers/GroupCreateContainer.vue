<template>
    <group-editor :group="newGroup" @saved="handleSaved"></group-editor>
</template>

<script>

import GroupEditor from '../group_components/GroupEditor.vue';

import { models } from 'django-airavata-api'

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
        handleSaved: function() {
            window.location.assign("/groups/");
        }
    },
    computed: {
    },
    beforeMount: function () {
    },
}
</script>
