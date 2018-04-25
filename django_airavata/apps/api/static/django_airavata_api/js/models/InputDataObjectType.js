
import BaseModel from './BaseModel';
import DataType from './DataType'

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

    get uiComponentId() {
        const metadata = this._getMetadata();
        if (metadata && 'editor' in metadata && 'ui-component-id' in metadata['editor']) {
            return metadata['editor']['ui-component-id'];
        } else {
            return null;
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
        if (this.isRequired && this.isEmpty(inputValue)) {
            results['value'] = 'This field is required.';
        }
        return results;
    }
}
