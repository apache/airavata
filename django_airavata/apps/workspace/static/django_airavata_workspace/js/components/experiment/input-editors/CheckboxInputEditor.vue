<template>
    <b-form-checkbox-group :id="id" :checked="selectedOptions"
        :options="options"
        stacked
        :state="componentValidState"
        @input="selectionsChanged"/>
</template>

<script>
import {InputEditorMixin} from 'django-airavata-workspace-plugin-api'

const CONFIG_OPTION_TEXT_KEY = 'text';
const CONFIG_OPTION_VALUE_KEY = 'value';

export default {
    name: 'checkbox-input-editor',
    mixins: [InputEditorMixin],
    props: {
        value: {
            type: String,
        },
    },
    computed: {
        options: function() {
            return 'options' in this.editorConfig
                ? this.editorConfig['options'].map(option => {
                    return {
                        text: option[CONFIG_OPTION_TEXT_KEY],
                        value: option[CONFIG_OPTION_VALUE_KEY],
                    };
                })
                : [];
        },
        selectedOptions() {
          return this.data ? this.data.split(",") : [];
        }
    },
    methods: {
      selectionsChanged(values) {
        this.data = values && values.length > 0 ? values.join(",") : null;
        this.valueChanged();
      }
    }
}
</script>
