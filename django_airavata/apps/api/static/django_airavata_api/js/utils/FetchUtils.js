var count = 0;
const parseQueryParams = function (url, queryParams = "") {
    if (queryParams && typeof(queryParams) != "string") {
        queryParams = Object.keys(queryParams).map(key => encodeURIComponent(key) + "=" + encodeURIComponent(queryParams[key])).join("&");
    }
    if (queryParams && queryParams !== "") {
        return url + "?" + queryParams;
    } else {
        return url;
    }
}

const setSpinnerDisplay = function (display) {
    let spinner = document.getElementById("airavata-spinner");
    spinner.style.display = display;
}

const incrementCount = function () {
    count++;
    if (count == 1) {
        setSpinnerDisplay("block");
    }
}
const decrementCount = function () {
    if (count > 0) {
        count--;
        if (count == 0) {
            setSpinnerDisplay("none");
        }
    }
}

export default {
    enableSpinner: function () {

    },
    disableSpinner: function () {

    },
    getCSRFToken: function () {
        var csrfToken = document.cookie.split(';').map(val => val.trim()).filter(val => val.startsWith("csrftoken" + '=')).map(val => val.split("=")[1]);
        if (csrfToken) {
            return csrfToken[0];
        } else {
            return null;
        }
    },
    createHeaders: function (contentType = "application/json", accept = "application/json") {
        var csrfToken = this.getCSRFToken();
        var headers = new Headers({
            "Content-Type": contentType,
            "Accept": accept,
        });
        if (csrfToken != null) {
            headers.set("X-CSRFToken", csrfToken)
        }
        return headers;
    },
    post: function (url, body, queryParams = "", mediaType = "application/json") {
        var headers = this.createHeaders(mediaType)
        // Browsers automatically handle content type for FormData request bodies
        if (body instanceof FormData) {
            headers.delete("Content-Type");
        }
        console.log("post body", body);
        url = parseQueryParams(url, queryParams);
        incrementCount();
        return fetch(url, {
            method: 'post',
            body: (body instanceof FormData || typeof body === 'string') ? body : JSON.stringify(body),
            headers: headers,
            credentials: "same-origin"
        }).then((response) => {
            decrementCount();
            if (response.ok) {
                return Promise.resolve(response.json())
            } else {
                let error = new Error(response.statusText);
                return response.json().then(json => {
                    error.data = json;
                })
                    .then(() => Promise.reject(error), () => Promise.reject(error));
            }
        }, (response) => {
            decrementCount();
            return Promise.reject(response);
        })
    },
    put: function (url, body, mediaType = "application/json") {
        var headers = this.createHeaders(mediaType);
        incrementCount();
        return fetch(url, {
            method: 'put',
            body: (body instanceof FormData || typeof body === 'string') ? body : JSON.stringify(body),
            headers: headers,
            credentials: "same-origin"
        }).then((response) => {
            decrementCount();
            if (response.ok) {
                return Promise.resolve(response.json())
            } else {
                let error = new Error(response.statusText);
                return response.json().then(json => {
                    error.data = json;
                })
                    .then(() => Promise.reject(error), () => Promise.reject(error));
            }
        }, (response) => {
            decrementCount();
            return Promise.reject(response);
        })
    },
    get: function (url, queryParams = "", mediaType = "application/json") {
        if (queryParams && typeof(queryParams) != "string") {
            queryParams = Object.keys(queryParams).map(key => encodeURIComponent(key) + "=" + encodeURIComponent(queryParams[key])).join("&")
        }
        if (queryParams) {
            url = url + "?" + queryParams
        }
        var headers = this.createHeaders(mediaType);
        incrementCount();
        return fetch(url, {
            method: 'get',
            headers: headers,
            credentials: "same-origin"
        }).then((response) => {
            decrementCount();
            if (response.ok) {
                return Promise.resolve(response.json())
            } else {
                let error = new Error(response.statusText);
                return response.json().then(json => {
                    error.data = json;
                })
                    .then(() => Promise.reject(error), () => Promise.reject(error));
            }
        }, (response) => {
            decrementCount();
            return Promise.reject(response);
        })
    },
    delete: function (url) {
        var headers = this.createHeaders();
        incrementCount();
        return fetch(url, {
            method: 'delete',
            headers: headers,
            credentials: "same-origin"
        }).then((response) => {
            decrementCount();
            // Not expecting a response body
            if (response.ok && response.status === 204) {
                return Promise.resolve();
            } else {
                let error = new Error(response.statusText);
                return response.json().then(json => {
                    error.data = json;
                })
                    .then(() => Promise.reject(error), () => Promise.reject(error));
            }
        })
    }
}