// import StackTrace from 'stacktrace-js'

import UnhandledErrorDispatcher from './UnhandledErrorDispatcher'

class GlobalErrorHandler {

    init() {
        console.log("Initializing GlobalErrorHandler...");
        window.onerror = this.handleGlobalError;
    }

    handleGlobalError(msg, url, lineNo, columnNo, error) {
        UnhandledErrorDispatcher.reportError({
            message: msg,
            error: error,
            details: {
                url,
                lineNo,
                columnNo
            }
        });

        return false;
    }
}

export default new GlobalErrorHandler()
