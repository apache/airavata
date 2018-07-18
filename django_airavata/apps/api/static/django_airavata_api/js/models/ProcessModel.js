import BaseModel from './BaseModel';

const FIELDS = [
    'processId',
    'experimentId',
    // TODO: finish mapping fields
];

export default class ProcessModel extends BaseModel {
    constructor(data = {}) {
        super(FIELDS, data);
    }
}
