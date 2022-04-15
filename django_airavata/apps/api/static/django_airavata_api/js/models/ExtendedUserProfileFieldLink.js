import BaseModel from "./BaseModel";

const FIELDS = [
  "id",
  "label",
  "url",
  "order",
  "display_link",
  "display_inline",
];

export default class ExtendedUserProfileFieldLink extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
  }
}
