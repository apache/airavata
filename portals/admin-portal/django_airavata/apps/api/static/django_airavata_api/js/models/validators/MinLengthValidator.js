export default class MinLengthValidator {
  constructor(config) {
    this.minLength = config["value"];
    if ("message" in config) {
      this.customErrorMessage = config["message"];
    }
  }

  validate(value) {
    if (value === null || typeof value === "undefined") {
      return this.getErrorMessage(value);
    }
    if (typeof value !== "string") {
      value = value.toString();
    }
    if (value.length < this.minLength) {
      return this.getErrorMessage(value);
    }
    return null;
  }

  getErrorMessage() {
    if (this.customErrorMessage) {
      return this.customErrorMessage;
    } else {
      return (
        "The value must be at least " +
        this.minLength +
        " characters in length."
      );
    }
  }
}
