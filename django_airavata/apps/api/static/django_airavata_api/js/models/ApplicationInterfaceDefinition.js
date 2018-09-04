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
  // When saving/updating, the order of the inputs in the applicationInputs
  // array determines the 'inputOrder' that will be applied to each input on the
  // backend. Updating 'inputOrder' will have no effect.
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
  {
    name: 'archiveWorkingDirectory',
    type: 'boolean',
    default: false,
  },
  {
    name: 'hasOptionalFileInputs',
    type: 'boolean',
    default: false,
  },
];

export default class ApplicationInterfaceDefinition extends BaseModel {

  constructor(data = {}) {
    super(FIELDS, data);
    // Order application inputs
    this.applicationInputs = this.getOrderedApplicationInputs();
  }

  getOrderedApplicationInputs() {
    // Use slice() to make a copy and sort that copy
    return this.applicationInputs ? this.applicationInputs.slice().sort((a, b) => a.inputOrder - b.inputOrder) : [];
  }
}
