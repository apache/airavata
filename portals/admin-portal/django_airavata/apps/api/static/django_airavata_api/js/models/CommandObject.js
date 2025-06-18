import BaseModel from "./BaseModel";
import uuidv4 from "uuid/v4";

const FIELDS = ["command", "commandOrder"];

export default class CommandObject extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
    this._key = data.key ? data.key : uuidv4();
  }

  get key() {
    return this._key;
  }
}
