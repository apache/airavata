import BaseModel from "./BaseModel";
import uuidv4 from "uuid/v4";

const FIELDS = [
  "reservationId",
  "reservationName",
  {
    name: "queueNames",
    type: "string",
    list: true
  },
  {
    name: "startTime",
    type: Date,
    default: () => new Date()
  },
  {
    name: "endTime",
    type: Date,
    default: () => new Date()
  }
];

export default class ComputeResourceReservation extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
    this._key = data.key ? data.key : uuidv4();
  }
  get key() {
    return this._key;
  }
  validate() {
    let validationResults = {};
    if (this.isEmpty(this.reservationName)) {
      validationResults["reservationName"] =
        "Please provide the name of this reservation.";
    }
    if (this.startTime > this.endTime) {
      validationResults["endTime"] = "End time must be later than start time.";
    }
    if (this.isEmpty(this.queueNames)) {
      validationResults["queueNames"] = "Please select at least one queue.";
    }
    return validationResults;
  }
  get isExpired() {
    const now = new Date();
    return now > this.endTime;
  }
}
