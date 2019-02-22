
import BaseModel from './BaseModel';
import DataType from './DataType';
import uuidv4 from 'uuid/v4';

const FIELDS = [
  'name',
  'value',
  {
    name: 'type',
    type: DataType,
    default: DataType.URI,
  },
  'applicationArgument',
  {
    name: 'isRequired',
    type: 'boolean',
    default: false
  },
  {
    name: 'requiredToAddedToCommandLine',
    type: 'boolean',
    default: false,
  },
  {
    name: 'dataMovement',
    type: 'boolean',
    default: false
  },
  'location',
  'searchQuery',
  {
    name: 'outputStreaming',
    type: 'boolean',
    default: false
  },
  'storageResourceId',
];

export default class OutputDataObjectType extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
    // Copy key when cloning a model
    this._key = data.key ? data.key : uuidv4();
  }

  get key() {
    return this._key;
  }
}

OutputDataObjectType.VALID_DATA_TYPES = DataType.values
