import BaseModel from "./BaseModel";
import SummaryType from "./SummaryType";

const FIELDS = [
  {
    name: "type",
    type: SummaryType,
  },
  "gatewayId",
  "username",
  "publicKey",
  {
    name: "persistedTime",
    type: Date,
  },
  "token",
  "description",
  "userHasWriteAccess",
];

export default class CredentialSummary extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
  }
}
