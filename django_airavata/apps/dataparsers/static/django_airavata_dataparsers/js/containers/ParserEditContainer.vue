<template>
    <parser-editor v-if="parser" :parser="parser" @saved="handleSaved" @cancelled="handleCancelled"></parser-editor>
</template>

<script>

import ParserEditor from '../parser-components/ParserEditor.vue';

import { models, services } from 'django-airavata-api'
import { components as comps } from 'django-airavata-common-ui'

export default {
    name: 'parser-edit-container',
    props: {
        parserId: {
            type: String,
            required: true,
        }
    },
    data () {
        return {
            parser: null,
        }
    },
    components: {
        ParserEditor,
    },
    methods: {
        handleSaved: function(parser) {
            window.location.assign("/dataparsers/");
        },
        handleCancelled: function(parser) {
          window.location.assign("/dataparsers/");
        },
    },
    computed: {
    },
    mounted: function () {
        services.ParserService.retrieve({lookup: this.parserId})
            .then(parser => this.parser = parser);
    },
}
</script>
