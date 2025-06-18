import ErrorUtils from "../errors/ErrorUtils";
import UnhandledErrorDispatcher from "../errors/UnhandledErrorDispatcher";
import Cache from "./Cache";

var count = 0;
const parseQueryParams = function (url, queryParams = "") {
  if (queryParams && typeof queryParams != "string") {
    queryParams = Object.keys(queryParams)
      .map(
        (key) =>
          encodeURIComponent(key) + "=" + encodeURIComponent(queryParams[key])
      )
      .join("&");
  }
  if (queryParams && queryParams !== "") {
    return url + "?" + queryParams;
  } else {
    return url;
  }
};

const setSpinnerDisplay = function (display) {
  let spinner = document.getElementById("airavata-spinner");
  spinner.style.display = display;
};

const incrementCount = function () {
  count++;
  if (count == 1) {
    setSpinnerDisplay("block");
  }
};
const decrementCount = function () {
  if (count > 0) {
    count--;
    if (count == 0) {
      setSpinnerDisplay("none");
    }
  }
};

const responseCache = new Cache();

export default {
  showSpinner: function (promise) {
    incrementCount();
    promise.then(decrementCount, decrementCount);
    // return the promise so that it can be chained
    return promise;
  },
  getCSRFToken: function () {
    var csrfToken = document.cookie
      .split(";")
      .map((val) => val.trim())
      .filter((val) => val.startsWith("csrftoken" + "="))
      .map((val) => val.split("=")[1]);
    if (csrfToken) {
      return csrfToken[0];
    } else {
      return null;
    }
  },
  // For POST, PUT, DELETE
  createHeaders: function (
    contentType = "application/json",
    accept = "application/json"
  ) {
    var csrfToken = this.getCSRFToken();
    var headers = new Headers({
      "Content-Type": contentType,
      Accept: accept,
    });
    if (csrfToken != null) {
      headers.set("X-CSRFToken", csrfToken);
    }
    return headers;
  },
  post: function (
    url,
    body,
    queryParams = "",
    {
      mediaType = "application/json",
      ignoreErrors = false,
      showSpinner = true,
      responseType = "json",
    } = {}
  ) {
    var headers = this.createHeaders(mediaType);
    // Browsers automatically handle content type for FormData request bodies
    if (body instanceof FormData) {
      headers.delete("Content-Type");
    }
    url = parseQueryParams(url, queryParams);
    return this.processFetch(url, {
      method: "post",
      body:
        body instanceof FormData || typeof body === "string"
          ? body
          : JSON.stringify(body),
      headers: headers,
      credentials: "same-origin",
      ignoreErrors,
      showSpinner,
      responseType,
    });
  },
  put: function (
    url,
    body,
    {
      mediaType = "application/json",
      ignoreErrors = false,
      showSpinner = true,
      responseType = "json",
    } = {}
  ) {
    var headers = this.createHeaders(mediaType);
    // Browsers automatically handle content type for FormData request bodies
    if (body instanceof FormData) {
      headers.delete("Content-Type");
    }
    return this.processFetch(url, {
      method: "put",
      body:
        body instanceof FormData || typeof body === "string"
          ? body
          : JSON.stringify(body),
      headers: headers,
      credentials: "same-origin",
      ignoreErrors,
      showSpinner,
      responseType,
    });
  },
  get: function (
    url,
    queryParams = "",
    {
      mediaType = "application/json",
      ignoreErrors = false,
      showSpinner = true,
      cache = false,
      responseType = "json",
    } = {}
  ) {
    if (queryParams && typeof queryParams != "string") {
      queryParams = Object.keys(queryParams)
        .map(
          (key) =>
            encodeURIComponent(key) + "=" + encodeURIComponent(queryParams[key])
        )
        .join("&");
    }
    if (queryParams) {
      url = url + "?" + queryParams;
    }
    if (cache) {
      if (responseCache.has(url)) {
        return responseCache.get(url);
      }
    }
    var headers = new Headers({ Accept: mediaType });
    const fetchRequest = this.processFetch(url, {
      method: "get",
      headers: headers,
      credentials: "same-origin",
      ignoreErrors,
      showSpinner,
      responseType,
    });
    if (cache) {
      responseCache.put({ key: url, value: fetchRequest });
    }
    return fetchRequest;
  },
  delete: function (
    url,
    { ignoreErrors = false, showSpinner = true, responseType = "json" } = {}
  ) {
    var headers = this.createHeaders();
    return this.processFetch(url, {
      method: "delete",
      headers: headers,
      credentials: "same-origin",
      ignoreErrors,
      showSpinner,
      responseType,
    });
  },
  processFetch: function (
    url,
    {
      method = "get",
      headers,
      credentials = "same-origin",
      body,
      ignoreErrors = false,
      showSpinner = true,
      responseType = "json",
    }
  ) {
    const fetchConfig = {
      method,
      headers,
      credentials,
    };
    if (body) {
      fetchConfig.body = body;
    }
    if (showSpinner) {
      incrementCount();
    }
    return fetch(url, fetchConfig)
      .then(
        (response) => {
          if (showSpinner) {
            decrementCount();
          }
          if (response.ok) {
            // No response body
            if (response.status === 204) {
              return Promise.resolve();
            } else {
              return Promise.resolve(
                response[responseType]().then((responseData) => {
                  return responseData;
                })
              );
            }
          } else {
            return response.json().then(
              (json) => {
                // if json doesn't have detail key, stringify body
                let errorMessage = json.detail;
                if (!("detail" in json)) {
                  errorMessage = "Error: " + JSON.stringify(json);
                }
                const error = new Error(errorMessage);
                error.details = this.createErrorDetails({
                  url,
                  body,
                  status: response.status,
                  responseBody: json,
                });
                throw error;
              },
              () => {
                // In case JSON parsing fails
                const error = new Error(response.statusText);
                error.details = this.createErrorDetails({
                  url,
                  body,
                  status: response.status,
                });
                throw error;
              }
            );
          }
        },
        (error) => {
          error.details = this.createErrorDetails({ url, body });
          throw error;
        }
      )
      .catch((error) => {
        if (showSpinner) {
          decrementCount();
        }
        // Always report unauthenticated errors so user knows they need to re-authenticate
        if (!ignoreErrors || ErrorUtils.isUnauthenticatedError(error)) {
          this.reportError(error);
        }
        throw error;
      });
  },
  createErrorDetails: function ({
    url,
    body,
    status = null,
    responseBody = null,
  } = {}) {
    return {
      url,
      body,
      status,
      response: responseBody,
    };
  },
  reportError(error) {
    UnhandledErrorDispatcher.reportError({
      message: error.message,
      error: error,
      details: error.details,
    });
  },
};
