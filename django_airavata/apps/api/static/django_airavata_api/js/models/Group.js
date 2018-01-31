import BaseModel from './BaseModel';

const FIELDS = [
    'id',
    'name',
    'ownerId',
    'description',
    'members',
];

export default class Group extends BaseModel {
    constructor(data={}) {
      super(FIELDS,data);
    }

    validate() {
        if (this.isEmpty(this.name.trim())) {
            return {
                name: ["Please provide a name."]
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
