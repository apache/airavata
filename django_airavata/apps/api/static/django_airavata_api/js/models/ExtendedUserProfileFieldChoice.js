import BaseModel from "./BaseModel";

const FIELDS = [
  "id",
  "display_text",
  "order",
];

export default class ExtendedUserProfileFieldChoice extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
  }
}
