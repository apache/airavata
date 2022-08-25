import BaseModel from "./BaseModel";

const FIELDS = ["id", "name"];

export default class QueueSettingsCalculator extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
  }
}
