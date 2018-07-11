import BaseModel from './BaseModel';
import GroupPermission from './GroupPermission';
import UserPermission from './UserPermission';
import UserProfile from './UserProfile';


const FIELDS = [
    'entityId',
    {
        name: 'userPermissions',
        type: UserPermission,
        list: true
    },
    {
        name: 'groupPermissions',
        type: GroupPermission,
        list: true
    },
    {
        name: 'owner',
        type: UserProfile,
    },
    'isOwner',
];

export default class SharedEntity extends BaseModel {
    constructor(data = {}) {
        super(FIELDS, data);
    }
}
