export default class MaxLengthValidator {
  constructor(config) {
    this.maxLength = config["value"];
    if ("message" in config) {
      this.customErrorMessage = config["message"];
    }
  }

  validate(value) {
    if (value === null || typeof value === "undefined") {
      return null;
    }
    if (typeof value !== "string") {
      value = value.toString();
    }
    if (value.length > this.maxLength) {
      return this.getErrorMessage(value);
    }
    return null;
  }

  getErrorMessage() {
    if (this.customErrorMessage) {
      return this.customErrorMessage;
    } else {
      return (
        "The value must be less than or equal to " +
        this.maxLength +
        " characters in length."
      );
    }
  }
}
