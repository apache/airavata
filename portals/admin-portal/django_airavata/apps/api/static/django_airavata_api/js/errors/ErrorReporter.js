import { services } from "..";
import LogRecord from "../models/LogRecord";

import StackTrace from "stacktrace-js";

class ErrorReporter {
  reportUnhandledError(unhandledError) {
    console.log(JSON.stringify(unhandledError, null, 4)); // eslint-disable-line no-console

    StackTrace.fromError(unhandledError.error)
      .then((stackframes) => {
        const stacktrace = stackframes.map((sf) => sf.toString());
        services.LoggingService.send(
          {
            data: new LogRecord({
              level: "ERROR",
              message: unhandledError.message,
              details: unhandledError.details,
              stacktrace: stacktrace,
            }),
          },
          { ignoreErrors: true }
        ).catch((err) => {
          console.log("Failed to log error", err); // eslint-disable-line no-console
        });
      })
      .catch((err) => {
        console.log("Failed to produce stacktrace", err); // eslint-disable-line no-console
      });
  }
}

export default new ErrorReporter();
