import BaseModel from './BaseModel';

const FIELDS = [
    'projectID',
    'name',
    'description',
    'owner',
    'gatewayId',
    {
        name: 'creationTime',
        type: 'date'
    },
];

export default class Project extends BaseModel {
    constructor(data = {}) {
        super(FIELDS, data);
    }

    validate() {
        if (this.name === null || this.name.trim() === "") {
            return {
                name: ["Please provide a name."]
            }
        }
        return null;
    }
}