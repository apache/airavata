import ErrorUtils from "./ErrorUtils";

let idSequence = 0;
class UnhandledError {
  constructor({
    message = null,
    error = null,
    details = null,
    suppressDisplay = false,
    suppressLogging = false,
  }) {
    this.id = idSequence++;
    this.message = message;
    this.error = error;
    this.details = details;
    this.suppressDisplay = suppressDisplay;
    this.suppressLogging = suppressLogging;
    this.createdDate = new Date();
  }

  get isUnauthenticatedError() {
    return ErrorUtils.isUnauthenticatedError(this.error);
  }
}

export default UnhandledError;
