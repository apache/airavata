import BaseModel from './BaseModel';

export default class GroupMember extends BaseModel {
    constructor(data = {}) {
        super(data);
        this.id = null;
        this.name = null;
        this.ownerId = null;
        this.description = null;
        this.gatewayId = null;
        // TODO: convert to Date object here instead of doing this in views
        this.copyData(data);
    }

    validateForCreate() {
        if (this.name === null || this.name.trim() === "") {
            return {
                name: ["Please provide a name."]
            }
        }
        return null;
    }

    toJSONForCreate() {
        // Remaining fields just get defaulted
        return JSON.stringify(this, ["name", "description"]);
    }

    toJSONForUpdate() {
        return JSON.stringify(this, ["groupID", "name", "description"]);
    }
}
