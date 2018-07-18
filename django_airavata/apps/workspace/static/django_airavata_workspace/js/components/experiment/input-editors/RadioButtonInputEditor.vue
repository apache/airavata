<template>
    <input-editor-form-group :label="experimentInput.name" :label-for="experimentInput.name"
        :state="validationState" :feedback-messages="validationFeedback">
        <b-form-radio-group :id="experimentInput.name" v-model="data"
            :options="options"
            stacked
            :state="validationState"
            @input="valueChanged"/>
    </input-editor-form-group>
</template>

<script>
import InputEditorFormGroup from './InputEditorFormGroup.vue'
import InputEditorMixin from './InputEditorMixin'

const CONFIG_OPTION_TEXT_KEY = 'text';
const CONFIG_OPTION_VALUE_KEY = 'value';

export default {
    name: 'radio-button-input-editor',
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
        options: function() {
            return 'options' in this.experimentInput.editorConfig
                ? this.experimentInput.editorConfig['options'].map(option => {
                    return {
                        text: option[CONFIG_OPTION_TEXT_KEY],
                        value: option[CONFIG_OPTION_VALUE_KEY],
                    };
                })
                : [];
        }
    }
}
</script>