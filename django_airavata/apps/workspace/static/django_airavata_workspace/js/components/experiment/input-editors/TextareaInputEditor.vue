<template>
    <b-form-group :label="experimentInput.name" :label-for="experimentInput.name"
        :state="validationState">
        <b-form-textarea :id="experimentInput.name" v-model="data"
            :rows="rows"
            :placeholder="experimentInput.userFriendlyDescription"
            :state="validationState"
            @input="valueChanged"/>
        <template slot="invalid-feedback">
            <ul v-if="validationFeedback && validationFeedback.length > 1">
                <li v-for="feedback in validationFeedback">{{ feedback }}</li>
            </ul>
            <div v-else-if="validationFeedback && validationFeedback.length === 1">
                {{ validationFeedback[0] }}
            </div>
        </template>
    </b-form-group>
</template>

<script>
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
    computed: {
        rows: function() {
            return 'rows' in this.experimentInput.editorConfig
                ? this.experimentInput.editorConfig['rows']
                : DEFAULT_ROWS;
        }
    }
}
</script>