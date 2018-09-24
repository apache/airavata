import UnhandledErrorDispatcher from "../errors/UnhandledErrorDispatcher";

var count = 0;
const parseQueryParams = function(url, queryParams = "") {
  if (queryParams && typeof queryParams != "string") {
    queryParams = Object.keys(queryParams)
      .map(
        key =>
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

const setSpinnerDisplay = function(display) {
  let spinner = document.getElementById("airavata-spinner");
  spinner.style.display = display;
};

const incrementCount = function() {
  count++;
  if (count == 1) {
    setSpinnerDisplay("block");
  }
};
const decrementCount = function() {
  if (count > 0) {
    count--;
    if (count == 0) {
      setSpinnerDisplay("none");
    }
  }
};

export default {
  enableSpinner: function() {},
  disableSpinner: function() {},
  getCSRFToken: function() {
    var csrfToken = document.cookie
      .split(";")
      .map(val => val.trim())
      .filter(val => val.startsWith("csrftoken" + "="))
      .map(val => val.split("=")[1]);
    if (csrfToken) {
      return csrfToken[0];
    } else {
      return null;
    }
  },
  createHeaders: function(
    contentType = "application/json",
    accept = "application/json"
  ) {
    var csrfToken = this.getCSRFToken();
    var headers = new Headers({
      "Content-Type": contentType,
      Accept: accept
    });
    if (csrfToken != null) {
      headers.set("X-CSRFToken", csrfToken);
    }
    return headers;
  },
  post: function(
    url,
    body,
    queryParams = "",
    { mediaType = "application/json", ignoreErrors = false } = {}
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
      ignoreErrors
    });
  },
  put: function(
    url,
    body,
    { mediaType = "application/json", ignoreErrors = false } = {}
  ) {
    var headers = this.createHeaders(mediaType);
    return this.processFetch(url, {
      method: "put",
      body:
        body instanceof FormData || typeof body === "string"
          ? body
          : JSON.stringify(body),
      headers: headers,
      credentials: "same-origin",
      ignoreErrors
    });
  },
  get: function(
    url,
    queryParams = "",
    { mediaType = "application/json", ignoreErrors = false } = {}
  ) {
    if (queryParams && typeof queryParams != "string") {
      queryParams = Object.keys(queryParams)
        .map(
          key =>
            encodeURIComponent(key) + "=" + encodeURIComponent(queryParams[key])
        )
        .join("&");
    }
    if (queryParams) {
      url = url + "?" + queryParams;
    }
    var headers = this.createHeaders(mediaType);
    return this.processFetch(url, {
      method: "get",
      headers: headers,
      credentials: "same-origin",
      ignoreErrors
    });
  },
  delete: function(url, { ignoreErrors = false } = {}) {
    var headers = this.createHeaders();
    return this.processFetch(url, {
      method: "delete",
      headers: headers,
      credentials: "same-origin",
      ignoreErrors
    });
  },
  processFetch: function(
    url,
    {
      method = "get",
      headers,
      credentials = "same-origin",
      body,
      ignoreErrors = false
    }
  ) {
    const fetchConfig = {
      method,
      headers,
      credentials
    };
    if (body) {
      fetchConfig.body = body;
    }
    incrementCount();
    return fetch(url, fetchConfig)
      .then(
        response => {
          decrementCount();
          if (response.ok) {
            // No response body
            if (response.status === 204) {
              return Promise.resolve();
            } else {
              return Promise.resolve(response.json());
            }
          } else {
            return response.json().then(
              json => {
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
                  responseBody: json
                });
                throw error;
              },
              e => {
                // In case JSON parsing fails
                const error = new Error(response.statusText);
                error.details = this.createErrorDetails({
                  url,
                  body,
                  status: response.status
                });
                throw error;
              }
            );
          }
        },
        error => {
          decrementCount();
          error.details = this.createErrorDetails({ url, body });
          throw error;
        }
      )
      .catch(error => {
        if (!ignoreErrors) {
          UnhandledErrorDispatcher.reportError({
            message: error.message,
            error: error,
            details: error.details
          });
        }
        throw error;
      });
  },
  createErrorDetails: function({
    url,
    body,
    status = null,
    responseBody = null
  } = {}) {
    return {
      url,
      body,
      status,
      response: responseBody
    };
  }
};
