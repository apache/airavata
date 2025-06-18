import Notification from "./Notification";

class NotificationList {
  constructor() {
    this.notifications = [];
  }

  add(notification) {
    this.notifications.push(notification);
  }

  // Convenience method for adding an error
  addError(error) {
    this.notifications.push(
      new Notification({
        type: "ERROR",
        message: error.message,
      })
    );
  }

  remove(notification) {
    const i = this.notifications.indexOf(notification);
    this.notifications.splice(i, 1);
  }

  get list() {
    return this.notifications;
  }
}

export default new NotificationList();
