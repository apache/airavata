import BaseEnum from "./BaseEnum";

export default class ResourcePermissionType extends BaseEnum {}
ResourcePermissionType.init(["WRITE", "READ", "OWNER", "MANAGE_SHARING"]);
