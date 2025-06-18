import BaseModel from "./BaseModel";
import Group from "./Group";
import ResourcePermissionType from "./ResourcePermissionType";

export default class GroupPermission extends BaseModel {
  constructor(data = {}) {
    super(
      [
        {
          name: "group",
          type: Group,
        },
        {
          name: "permissionType",
          type: ResourcePermissionType,
        },
      ],
      data
    );
  }
}
