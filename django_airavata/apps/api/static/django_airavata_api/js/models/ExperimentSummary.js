
import BaseModel from './BaseModel'

const FIELDS = [
    'experimentId',
    'projectId',
    'gatewayId',
    {
        name: 'creationTime',
        type: 'date'
    },
    'userName',
    'name',
    'description',
    'executionId',
    'resourceHostId',
    'experimentStatus',
    {
        name: 'statusUpdateTime',
        type: 'date',
    },
];

export default class ExperimentSummary extends BaseModel {
    constructor(data = {}) {
        super(FIELDS, data);
    }
}
