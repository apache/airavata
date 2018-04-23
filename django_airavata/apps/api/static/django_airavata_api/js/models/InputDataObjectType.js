
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

    validate(experiment, value = undefined) {
        let inputValue = typeof value != 'undefined' ? value : this.value;
        let results = {};
        if (this.isRequired && this.isEmpty(inputValue)) {
            results['value'] = 'This field is required.';
        }
        return results;
    }
}
