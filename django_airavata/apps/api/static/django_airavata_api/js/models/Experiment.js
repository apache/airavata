import BaseModel from './BaseModel';

export default class Experiment extends BaseModel {
    constructor(data = {}) {
        super(data);
        this.experimentId = null;
        this.projectId = null;
        this.gatewayId = null;
        this.experimentType = null;
        this.userName = null;
        this.experimentName = null;
        this.description = null;
        this.copyData(data);
    }

    validateForCreate() {
    }

    validateForUpdate() {
    }

    toJSONForCreate() {
    }

    toJSONForUpdate() {
    }
}
