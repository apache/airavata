
class ErrorReporter {
    reportUnhandledError(unhandledError) {
        // TODO: send to the server so it can be logged there
        console.log(JSON.stringify(unhandledError, null, 4)); // eslint-disable-line no-console
    }
}

export default new ErrorReporter();
