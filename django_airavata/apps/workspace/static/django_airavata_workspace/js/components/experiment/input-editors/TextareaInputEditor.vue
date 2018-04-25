<template>
    <input-editor-form-group :label="experimentInput.name" :label-for="experimentInput.name"
        :state="validationState" :feedback-messages="validationFeedback">
        <b-form-textarea :id="experimentInput.name" v-model="data"
            :rows="rows"
            :placeholder="experimentInput.userFriendlyDescription"
            :state="validationState"
            @input="valueChanged"/>
    </input-editor-form-group>
</template>

<script>
import InputEditorFormGroup from './InputEditorFormGroup.vue'
import InputEditorMixin from './InputEditorMixin'

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
    components: {
        InputEditorFormGroup,
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