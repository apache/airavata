
import BaseModel from './BaseModel';

const FIELDS = [
    'name',
    'value',
    'type',
    'applicationArgument',
    'isRequired',
    'requiredToAddedToCommandLine',
    'dataMovement',
    'location',
    'searchQuery',
    'outputStreaming',
    'storageResourceId',
];

export default class OutputDataObjectType extends BaseModel {
    constructor(data = {}) {
        super(FIELDS, data);
    }
}
