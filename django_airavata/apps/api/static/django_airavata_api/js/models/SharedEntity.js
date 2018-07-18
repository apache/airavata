import BaseModel from './BaseModel';
import GroupPermission from './GroupPermission';
import UserPermission from './UserPermission';
import UserProfile from './UserProfile';


const FIELDS = [
    'entityId',
    {
        name: 'userPermissions',
        type: UserPermission,
        list: true,
        default: BaseModel.defaultNewInstance(Array),
    },
    {
        name: 'groupPermissions',
        type: GroupPermission,
        list: true,
        default: BaseModel.defaultNewInstance(Array),
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

    /**
     * Merge given sharedEntity with `this`, where the given sharedEntity takes
     * precedence.
     */
    merge(sharedEntity) {
        if (sharedEntity.entityId) {
            this.entityId = sharedEntity.entityId;
        }
        if (sharedEntity.owner) {
            this.owner = sharedEntity.owner;
            this.isOwner = sharedEntity.isOwner;
        }
        // Allow userPermissions entries in sharedEntity to override this userPermissions
        let newUserPermissions = [].concat(this.userPermissions, sharedEntity.userPermissions);
        let newUserPermissionsMap = newUserPermissions.reduce((map, userPerm) => {
            map[userPerm.user.airavataInternalUserId] = userPerm;
            return map;
        }, {});
        this.userPermissions = Object.values(newUserPermissionsMap);
        // Same deal for groupPermissions
        let newGroupPermissions = [].concat(this.groupPermissions, sharedEntity.groupPermissions);
        let newGroupPermissionsMap = newGroupPermissions.reduce((map, groupPerm) => {
            map[groupPerm.group.id] = groupPerm;
            return map;
        }, {});
        this.groupPermissions = Object.values(newGroupPermissionsMap);
    }
}
