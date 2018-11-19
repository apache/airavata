import BaseModel from "./BaseModel";

const FIELDS = [
  "id",
  "name",
  "ownerId",
  "description",
  {
    name: "members",
    type: "string",
    list: true,
    default: BaseModel.defaultNewInstance(Array)
  },
  {
    name: "admins",
    type: "string",
    list: true,
    default: BaseModel.defaultNewInstance(Array)
  },
  {
    name: "isOwner",
    type: "boolean",
    default: true
  },
  {
    name: "isAdmin",
    type: "boolean",
    default: false
  },
  {
    name: "isMember",
    type: "boolean",
    default: true
  },
  "isGatewayAdminsGroup",
  "isReadOnlyGatewayAdminsGroup",
  "isDefaultGatewayUsersGroup"
];

export default class Group extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
  }

  validate() {
    if (this.isEmpty(this.name.trim())) {
      return {
        name: ["Please provide a name."]
      };
    }
    return null;
  }
}
