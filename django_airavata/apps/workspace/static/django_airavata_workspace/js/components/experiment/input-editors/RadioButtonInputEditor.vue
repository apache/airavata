<template>
    <b-form-radio-group :id="id" v-model="data"
        :options="options"
        stacked
        :state="componentValidState"
        @input="valueChanged"/>
</template>

<script>
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