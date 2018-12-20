<template>
    <input-editor-form-group :label="experimentInput.name" :label-for="inputEditorComponentId"
        :state="validationState" :feedback-messages="validationFeedback">
        <component :is="inputEditorComponentName"
            :id="inputEditorComponentId"
            :experiment-input="experimentInput"
            v-model="data"
            @invalid="recordInvalidInputEditorValue"
            @valid="recordValidInputEditorValue"
            @input="valueChanged"/>
    </input-editor-form-group>
</template>

<script>
import FileInputEditor from './FileInputEditor.vue'
import InputEditorFormGroup from './InputEditorFormGroup.vue'
import RadioButtonInputEditor from './RadioButtonInputEditor.vue'
import StringInputEditor from './StringInputEditor.vue'
import TextareaInputEditor from './TextareaInputEditor.vue'

import {models} from 'django-airavata-api'

export default {
    name: 'input-editor-container',
    props: {
        value: {
            required: true,
        },
        experimentInput: {
            type: models.InputDataObjectType,
            required: true,
        },
    },
    components: {
        FileInputEditor,
        InputEditorFormGroup,
        RadioButtonInputEditor,
        StringInputEditor,
        TextareaInputEditor,
    },
    data: function() {
        return {
            data: this.value,
            state: null,
            feedbackMessages: [],
            inputHasBegun: false,
        }
    },
    computed: {
        inputEditorComponentName: function() {
            // If input specifices an editor UI component, use that
            if (this.experimentInput.editorUIComponentId) {
                return this.experimentInput.editorUIComponentId;
            }
            // Default UI components based on input type
            if (this.experimentInput.type === models.DataType.STRING) {
                return 'string-input-editor';
            } else if (this.experimentInput.type === models.DataType.URI) {
                return 'file-input-editor';
            }
            // Default
            return 'string-input-editor';
        },
        inputEditorComponentId: function() {
            return this.experimentInput.name;
        },
        validationFeedback: function() {
            // Only display validation feedback after the user has provided
            // input so that missing required value errors are only displayed
            // after interacting with the input editor
            return this.inputHasBegun ? this.feedbackMessages : null;
        },
        validationState: function() {
            return this.inputHasBegun ? this.state : null;
        },
    },
    methods: {
        recordValidInputEditorValue: function() {
            this.state = null;
            this.$emit('valid');
        },
        recordInvalidInputEditorValue: function(feedbackMessages) {
            this.feedbackMessages = feedbackMessages;
            this.state = 'invalid';
            this.$emit('invalid', feedbackMessages);
        },
        valueChanged: function() {
            this.inputHasBegun = true;
            this.$emit('input', this.data);
        },
    }
}
</script>
