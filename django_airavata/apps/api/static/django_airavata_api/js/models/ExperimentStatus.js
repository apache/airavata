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

const STATE_NAMES = {
    0: "CREATED",
    1: "VALIDATED",
    2: "SCHEDULED",
    3: "LAUNCHED",
    4: "EXECUTING",
    5: "CANCELING",
    6: "CANCELED",
    7: "COMPLETED",
    8: "FAILED",
}

export default class ExperimentStatus extends BaseModel {
    constructor(data = {}) {
        super(FIELDS, data);
    }

    get stateName() {
        return STATE_NAMES[this.state];
    }
}
