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
    default: BaseModel.defaultNewInstance(Array)
  },
  {
    name: "groupPermissions",
    type: GroupPermission,
    list: true,
    default: BaseModel.defaultNewInstance(Array)
  },
  {
    name: "owner",
    type: UserProfile
  },
  "isOwner"
];

export default class SharedEntity extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
  }

  addGroup(group) {
    if (!this.groupPermissions) {
      this.groupPermissions = [];
    }
    if (!this.groupPermissions.find(gp => gp.group.id === group.id)) {
      this.groupPermissions.push(
        new GroupPermission({
          group: group,
          permissionType: ResourcePermissionType.READ
        })
      );
    }
  }
}
