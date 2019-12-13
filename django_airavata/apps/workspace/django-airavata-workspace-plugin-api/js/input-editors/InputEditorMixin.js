// InputEditorMixin: mixin for experiment InputEditors, provides basic v-model
// and validation functionality and defines the basic props interface
// (experimentInput and id).
import {models} from 'django-airavata-api'
export default {
    props: {
        value: {
            type: String
        },
        experimentInput: {
            type: models.InputDataObjectType,
            required: true,
        },
        experiment: {
          type: models.Experiment,
          required: true,
        },
        id: {
            type: String,
            required: true,
        },
    },
    data () {
        return {
            data: this.value,
            inputHasBegun: false,
        }
    },
    computed: {
        validationResults: function() {
            return this.experimentInput.validate(this.data);
        },
        validationMessages: function() {
            return 'value' in this.validationResults ? this.validationResults['value'] : [];
        },
        valid: function() {
            return this.validationMessages.length === 0;
        },
        componentValidState: function() {
            if (this.inputHasBegun) {
              return this.valid ? 'valid' : 'invalid';
            } else {
              return null;
            }
        },
        editorConfig: function() {
            return this.experimentInput.editorConfig;
        }
    },
    methods: {
        valueChanged: function() {
            this.inputHasBegun = true;
            this.$emit('input', this.data);
        },
        checkValidation: function() {
            if (this.valid) {
                this.$emit('valid');
            } else {
                this.$emit('invalid', this.validationMessages);
            }
        }
    },
    created: function() {
        this.checkValidation();
    },
    watch: {
        value(newValue) {
          this.data = newValue;
        },
        valid() {
          this.checkValidation();
        }
    }
}
