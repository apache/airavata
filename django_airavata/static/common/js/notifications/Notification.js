let idSequence = 0;
class Notification {
  constructor({
    type = "SUCCESS",
    message = null,
    details = null,
    dismissable = true,
    duration = 0,
    createdDate = null,
  }) {
    this.id = idSequence++;
    this.type = type;
    this.message = message;
    this.details = details;
    this.dismissable = dismissable;
    this.duration = duration;
    this.createdDate = createdDate ? createdDate : new Date();
  }
}

export default Notification;
