import BaseModel from "./BaseModel";

const FIELDS = [
  "id",
  "value_type",
  "ext_user_profile_field",
  "text_value",
  "choices",
  "other_value",
  "agreement_value",
  "valid",
  "value_display",
];

export default class ExtendedUserProfileValue extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
  }

  toJSON() {
    const copy = Object.assign({}, this);
    // Remove unnecessary properties
    switch (this.value_type) {
      case "text":
        delete copy["other_value"];
        delete copy["choices"];
        delete copy["agreement_value"];
        break;
      case "single_choice":
      case "multi_choice":
        delete copy["text_value"];
        delete copy["agreement_value"];
        break;
      case "user_agreement":
        delete copy["text_value"];
        delete copy["other_value"];
        delete copy["choices"];
        break;
      default:
        // eslint-disable-next-line no-console
        console.error("Unrecognized value type", this.value_type);
        break;
    }
    return copy;
  }
}
