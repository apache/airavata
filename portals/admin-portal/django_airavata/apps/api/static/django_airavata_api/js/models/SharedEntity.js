import BaseModel from "./BaseModel";
import GroupPermission from "./GroupPermission";
import UserPermission from "./UserPermission";
import UserProfile from "./UserProfile";
import ResourcePermissionType from "./ResourcePermissionType";

const FIELDS = [
  "entityId",
  {
    name: "userPermissions",
    type: UserPermission,
    list: true,
    default: BaseModel.defaultNewInstance(Array),
  },
  {
    name: "groupPermissions",
    type: GroupPermission,
    list: true,
    default: BaseModel.defaultNewInstance(Array),
  },
  {
    name: "owner",
    type: UserProfile,
  },
  "isOwner",
  "hasSharingPermission",
];

export default class SharedEntity extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
  }

  addUser(user) {
    if (!this.userPermissions) {
      this.userPermissions = [];
    }
    if (
      !this.userPermissions.find(
        (up) => up.user.airavataInternalUserId === user.airavataInternalUserId
      )
    ) {
      this.userPermissions.push(
        new UserPermission({
          user: user,
          permissionType: ResourcePermissionType.READ,
        })
      );
    }
  }

  removeUser(user) {
    this.userPermissions = this.userPermissions.filter(
      (userPermission) =>
        userPermission.user.airavataInternalUserId !==
        user.airavataInternalUserId
    );
  }

  addGroup({ group, permissionType = ResourcePermissionType.READ }) {
    if (!this.groupPermissions) {
      this.groupPermissions = [];
    }
    if (!this.groupPermissions.find((gp) => gp.group.id === group.id)) {
      this.groupPermissions.push(
        new GroupPermission({
          group: group,
          permissionType: permissionType,
        })
      );
    }
  }

  removeGroup(group) {
    this.groupPermissions = this.groupPermissions.filter(
      (groupPermission) => groupPermission.group.id !== group.id
    );
  }

  get nonAdminGroupPermissions() {
    if (this.groupPermissions) {
      return this.groupPermissions.filter(
        (groupPermission) => !groupPermission.group.isAdminGroup
      );
    } else {
      return [];
    }
  }
}
