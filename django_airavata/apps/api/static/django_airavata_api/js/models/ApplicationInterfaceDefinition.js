import BaseModel from './BaseModel'
import InputDataObjectType from './InputDataObjectType'
import OutputDataObjectType from './OutputDataObjectType'


const FIELDS = [
    'applicationInterfaceId',
    'applicationName',
    'applicationDescription',
    {
        name: 'applicationModules',
        type: 'string',
        list: true,
    },
    {
        name: 'applicationInputs',
        type: InputDataObjectType,
        list: true,
    },
    {
        name: 'applicationOutputs',
        type: OutputDataObjectType,
        list: true,
    },
    'archiveWorkingDirectory',
    'hasOptionalFileInputs',
];

export default class ApplicationInterfaceDefinition extends BaseModel {

    constructor(data = {}) {
        super(FIELDS, data);
    }

    getOrderedApplicationInputs() {
        // Use slice() to make a copy and sort that copy
        return this.applicationInputs ? this.applicationInputs.slice().sort((a, b) => a.inputOrder - b.inputOrder) : [];
    }
}
