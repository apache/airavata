import BaseModel from './BaseModel';

const FIELDS = [
    'experimentId',
    'projectId',
    'gatewayId',
    'experimentType',
    'userName',
    'experimentName',
    'description',
];

export default class Experiment extends BaseModel {
    constructor(data = {}) {
        super(FIELDS, data);
    }
}
