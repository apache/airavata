import BaseModel from './BaseModel';

export default class Group extends BaseModel {
    constructor(data = {}) {
        super(data);
        this.id = null;
        this.name = null;
        this.ownerId = null;
        this.description = null;
        this.members = null;
        this.copyData(data);
    }

    validateForCreate() {
        if (this.name === null || this.name.trim() === "") {
            return {
                name: ["Please provide a name."]
            }
        }
        if (this.description === null || this.description.trim() === "") {
            return {
                name: ["Please provide a description."]
            }
        }
        if (this.members === null || this.members.trim() === "") {
            return {
                name: ["Please provide some members."]
            }
        }
        return null;
    }

    toJSONForCreate() {
        // Remaining fields just get defaulted
        return JSON.stringify(this, ["name", "description", "members"]);
    }

    toJSONForUpdate() {
        return JSON.stringify(this, ["id", "name", "description"]);
    }
}
