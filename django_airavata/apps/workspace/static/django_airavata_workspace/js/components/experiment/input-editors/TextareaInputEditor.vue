<template>
    <b-form-textarea :id="id" v-model="data"
        :rows="rows"
        :placeholder="experimentInput.userFriendlyDescription"
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
            required: true,
        },
    },
    computed: {
        rows: function() {
            return 'rows' in this.experimentInput.editorConfig
                ? this.experimentInput.editorConfig['rows']
                : DEFAULT_ROWS;
        }
    }
}
</script>