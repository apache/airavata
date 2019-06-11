<template>
    <b-form-textarea :id="id" v-model="data"
        :rows="rows"
        :state="componentValidState"
        @input="valueChanged"/>
</template>

<script>
import {InputEditorMixin} from 'django-airavata-workspace-plugin-api'

const DEFAULT_ROWS = 3;

export default {
    name: 'textarea-input-editor',
    mixins: [InputEditorMixin],
    props: {
        value: {
            type: String,
        },
    },
    computed: {
        rows: function() {
            return 'rows' in this.editorConfig
                ? this.editorConfig['rows']
                : DEFAULT_ROWS;
        }
    }
}
</script>
