import BaseModel from "./BaseModel";
import StoragePreference from "./StoragePreference";

const FIELDS = [
  "gatewayID",
  "credentialStoreToken",
  "computeResourcePreferences",
  {
    name: "storagePreferences",
    type: StoragePreference,
    list: true,
  },
  "identityServerTenant",
  "identityServerPwdCredToken",
  "userHasWriteAccess",
];

export default class GatewayResourceProfile extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
  }
}
