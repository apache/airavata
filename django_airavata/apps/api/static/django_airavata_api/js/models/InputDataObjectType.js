
import BaseModel from './BaseModel';

const FIELDS = [
    'name',
    'value',
    'type',
    'applicationArgument',
    'standardInput',
    'userFriendlyDescription',
    {
        name: 'metaData',
        type: 'string',
        default: '',
    },
    'inputOrder',
    'isRequired',
    'requiredToAddedToCommandLine',
    'dataStaged',
    {
        name: 'storageResourceId',
        type: 'string',
        default: '',
    },
    'isReadOnly',
];

export default class InputDataObjectType extends BaseModel {
    constructor(data = {}) {
        super(FIELDS, data);
        // TODO: move into BaseModel
        // Convert null strings into empty strings
        if ('metaData' in this && this.metaData === null) {
            this.metaData = '';
        }
        if ('storageResourceId' in this && this.storageResourceId === null) {
            this.storageResourceId = '';
        }
    }

    validate() {
        let results = {};
        if (this.isRequired && this.isEmpty(this.value)) {
            results['value'] = 'This field is required.';
        }
        return results;
    }
}
