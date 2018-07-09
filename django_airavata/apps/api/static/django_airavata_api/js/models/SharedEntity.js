import BaseModel from './BaseModel';
import Group from './Group';
import ResourcePermissionType from './ResourcePermissionType';
import UserProfile from './UserProfile';

class UserPermission extends BaseModel {
    constructor(data = {}) {
        super([
            {
                name: 'user',
                type: UserProfile,
            },
            {
                name: 'permissionType',
                type: ResourcePermissionType,
            }
        ], data);
    }
}

class GroupPermission extends BaseModel {
    constructor(data = {}) {
        super([
            {
                name: 'group',
                type: Group,
            },
            {
                name: 'permissionType',
                type: ResourcePermissionType,
            }
        ], data);
    }
}

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
