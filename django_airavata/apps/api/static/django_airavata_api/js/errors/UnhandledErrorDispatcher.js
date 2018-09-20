import ErrorReporter from "./ErrorReporter";
import UnhandledError from "./UnhandledError";
import UnhandledErrorList from "./UnhandledErrorDisplayList";

class UnhandledErrorDispatcher {
  reportError({
    message = null,
    error = null,
    details = null,
    suppressDisplay = false,
    suppressLogging = false
  }) {
    const unhandledError = new UnhandledError({
      message,
      error,
      details,
      suppressDisplay,
      suppressLogging
    });
    this.reportUnhandledError(unhandledError);
  }

  reportUnhandledError(unhandledError) {
    if (!unhandledError.suppressDisplay) {
      UnhandledErrorList.add(unhandledError);
    }
    if (!unhandledError.suppressLogging) {
      ErrorReporter.reportUnhandledError(unhandledError);
    }
  }
}

export default new UnhandledErrorDispatcher();
