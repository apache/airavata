
export default class MinLengthValidator {

    constructor(config=null) {
        if (typeof config === 'number') {
            this.minLength = config;
        } else if (typeof config === 'object') {
            this.minLength = config['value'];
            if ('message' in config) {
                this.customErrorMessage = config['message'];
            }
        }
    }

    validate(value) {
        if (value === null || typeof value === 'undefined') {
            return this.getErrorMessage(value);
        }
        if (typeof value !== 'string') {
            value = value.toString();
        }
        if (value.length < this.minLength) {
            return this.getErrorMessage(value);
        }
        return null;
    }

    getErrorMessage(value) {
        if (this.customErrorMessage) {
            return this.customErrorMessage;
        } else {
            return "The value must be at least " + this.minLength + " characters in length.";
        }
    }
}