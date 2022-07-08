import BaseModel from "./BaseModel";
import uuidv4 from "uuid/v4";

const FIELDS = ["id", "display_text", "order"];

export default class ExtendedUserProfileFieldChoice extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
    this._key = data.key ? data.key : uuidv4();
  }

  get key() {
    return this._key;
  }

  toJSON() {
    const copy = Object.assign({}, this);
    // id must either have a value or be missing, it can't be null
    if (!copy.id) {
      delete copy.id;
    }
    return copy;
  }
}
