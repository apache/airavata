import BaseEnum from './BaseEnum'

export default class ExperimentState extends BaseEnum {
}
ExperimentState.init([
    'CREATED',
    'VALIDATED',
    'SCHEDULED',
    'LAUNCHED',
    'EXECUTING',
    'CANCELING',
    'CANCELED',
    'COMPLETED',
    'FAILED'
]);
