import BaseModel from "./BaseModel";

const FIELDS = [
  "projectID",
  "name",
  "description",
  "owner",
  "gatewayId",
  {
    name: "creationTime",
    type: "date",
  },
  "userHasWriteAccess",
  "isOwner",
];

export default class Project extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
  }

  validate() {
    if (this.isEmpty(this.name)) {
      return {
        name: ["Please provide a name."],
      };
    }
    return null;
  }
}
