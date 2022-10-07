import ErrorReporter from "./ErrorReporter";
import UnhandledError from "./UnhandledError";
import UnhandledErrorList from "./UnhandledErrorDisplayList";

class UnhandledErrorDispatcher {
  reportError({
    message = null,
    error = null,
    details = null,
    suppressDisplay = false,
    suppressLogging = false,
  }) {
    const unhandledError = new UnhandledError({
      message,
      error,
      details,
      suppressDisplay,
      suppressLogging,
    });
    this.reportUnhandledError(unhandledError);
  }

  reportUnhandledError(unhandledError) {
    // Ignore unauthenticated errors that have already been displayed
    if (
      unhandledError.isUnauthenticatedError &&
      UnhandledErrorList.list.some((e) => e.isUnauthenticatedError)
    ) {
      return;
    }

    if (!unhandledError.suppressDisplay) {
      UnhandledErrorList.add(unhandledError);
    }
    if (
      !unhandledError.suppressLogging &&
      // Don't log unauthenticated errors
      !unhandledError.isUnauthenticatedError
    ) {
      ErrorReporter.reportUnhandledError(unhandledError);
    }
  }
}

export default new UnhandledErrorDispatcher();
