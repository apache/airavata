import BaseModel from './BaseModel';

const FIELDS = [
    'id',
    'name',
    'ownerId',
    'description',
    'members',
    'isOwner',
    'isAdmin',
    'isMember',
    'isGatewayAdminsGroup',
    'isReadOnlyGatewayAdminsGroup',
    'isDefaultGatewayUsersGroup',
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
}
