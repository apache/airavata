import BaseModel from "./BaseModel";
import DataReplicaLocation from "./DataReplicaLocation";

import URL from "url-parse";

const FIELDS = [
  "productUri",
  "gatewayId",
  "parentProductUri",
  "productName",
  "productDescription",
  "ownerName",
  "dataProductType",
  "productSize",
  {
    name: "creationTime",
    type: "date",
  },
  {
    name: "lastModifiedTime",
    type: "date",
  },
  "productMetadata",
  {
    name: "replicaLocations",
    type: DataReplicaLocation,
    list: true,
  },
  "downloadURL",
  "isInputFileUpload",
  "filesize",
];

const FILENAME_REGEX = /[^/]+$/;
const TEXT_MIME_TYPE_REGEX = /^text\/.+/;
const IMAGE_MIME_TYPE_REGEX = /^image\/.+/;

export default class DataProduct extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
  }

  get filename() {
    if (this.replicaLocations && this.replicaLocations.length > 0) {
      const firstReplicaLocation = this.replicaLocations[0];
      const fileURL = new URL(firstReplicaLocation.filePath);
      const filenameMatch = FILENAME_REGEX.exec(fileURL.pathname);
      if (filenameMatch) {
        return filenameMatch[0];
      }
    }
    return null;
  }

  get isText() {
    return this.mimeType && TEXT_MIME_TYPE_REGEX.test(this.mimeType);
  }

  get isImage() {
    return this.mimeType && IMAGE_MIME_TYPE_REGEX.test(this.mimeType);
  }

  get mimeType() {
    return this.productMetadata && this.productMetadata["mime-type"]
      ? this.productMetadata["mime-type"]
      : null;
  }
}
