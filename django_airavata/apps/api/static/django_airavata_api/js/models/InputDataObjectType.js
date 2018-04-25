
import BaseModel from './BaseModel';
import DataType from './DataType';
import ValidatorFactory from './validators/ValidatorFactory';

const FIELDS = [
    'name',
    'value',
    {
        name: 'type',
        type: DataType,
    },
    'applicationArgument',
    'standardInput',
    'userFriendlyDescription',
    'metaData',
    'inputOrder',
    'isRequired',
    'requiredToAddedToCommandLine',
    'dataStaged',
    'storageResourceId',
    'isReadOnly',
];

export default class InputDataObjectType extends BaseModel {
    constructor(data = {}) {
        super(FIELDS, data);
    }

    get editorUIComponentId() {
        const metadata = this._getMetadata();
        if (metadata && 'editor' in metadata && 'ui-component-id' in metadata['editor']) {
            return metadata['editor']['ui-component-id'];
        } else {
            return null;
        }
    }

    get editorConfig() {
        const metadata = this._getMetadata();
        if (metadata && 'editor' in metadata && 'config' in metadata['editor']) {
            return metadata['editor']['config'];
        } else {
            return {};
        }
    }

    get editorValidations() {
        const metadata = this._getMetadata();
        if (metadata && 'editor' in metadata && 'validations' in metadata['editor']) {
            return metadata['editor']['validations'];
        } else {
            return {};
        }
    }

    _getMetadata() {
        // metaData could really be anything, here we expect it to be an object
        // so safely check if it is first
        if (this.metaData && typeof this.metaData === 'object') {
            return this.metaData;
        } else {
            return null;
        }
    }

    validate(experiment, value = undefined) {
        let inputValue = typeof value != 'undefined' ? value : this.value;
        let results = {};
        let valueErrorMessages = [];
        if (this.isRequired && this.isEmpty(inputValue)) {
            valueErrorMessages.push('This field is required.');
        }
        // Run through any validations if configured
        if (Object.keys(this.editorValidations).length > 0) {
            const validatorFactory = new ValidatorFactory();
            valueErrorMessages = valueErrorMessages.concat(validatorFactory.validate(this.editorValidations, inputValue));
        }
        if (valueErrorMessages.length > 0) {
            results['value'] = valueErrorMessages;
        }
        return results;
    }
}
