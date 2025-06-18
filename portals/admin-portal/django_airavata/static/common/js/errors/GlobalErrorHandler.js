import Vue from "vue";
import { errors } from "django-airavata-api";

class GlobalErrorHandler {
  init() {
    console.log("Initializing GlobalErrorHandler..."); // eslint-disable-line no-console
    window.onerror = this.handleGlobalError;
    Vue.config.errorHandler = this.vueGlobalErrorHandler;
  }

  handleGlobalError(msg, url, lineNo, columnNo, error) {
    errors.UnhandledErrorDispatcher.reportError({
      message: msg,
      error: error,
      details: {
        url,
        lineNo,
        columnNo,
      },
    });

    return false;
  }

  vueGlobalErrorHandler(err, vm, info) {
    console.log("Vue Global Error Handler", err, vm, info); // eslint-disable-line no-console
    errors.UnhandledErrorDispatcher.reportError({
      message: err.message,
      error: err,
      details: info,
    });
  }
}

export default new GlobalErrorHandler();
