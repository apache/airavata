import BaseModel from './BaseModel';

const FIELDS = [
    // TODO: state is an enum field
     'state',
     {
         name: 'timeOfStateChange',
         type: 'date',
     },
     'reason',
];

export default class ExperimentStatus extends BaseModel {
    constructor(data = {}) {
        super(FIELDS, data);
    }
}
