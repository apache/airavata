
export default {
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
    post: function (url, body, mediaType = "application/json") {
        var headers = this.createHeaders(mediaType)
        // Browsers automatically handle content type for FormData request bodies
        if (body instanceof FormData) {
            headers.delete("Content-Type");
        }
        return fetch(url, {
            method: 'post',
            body: body,
            headers: headers,
            credentials: "same-origin"
        }).then((response) => {
            if (response.ok) {
                return Promise.resolve(response.json())
            } else {
                let error = new Error(response.statusText);
                return response.json().then(json => {
                    error.data = json;
                })
                .then(() => Promise.reject(error),() => Promise.reject(error));
            }
        })
    },
    put: function (url, body, mediaType = "application/json") {
        var headers = this.createHeaders(mediaType)
        return fetch(url, {
            method: 'put',
            body: body,
            headers: headers,
            credentials: "same-origin"
        }).then((response) => {
            if (response.ok) {
                return Promise.resolve(response.json())
            } else {
                let error = new Error(response.statusText);
                return response.json().then(json => {
                    error.data = json;
                })
                .then(() => Promise.reject(error),() => Promise.reject(error));
            }
        })
    },
    get: function (url, queryParams = "", mediaType = "application/json") {
        if (queryParams && typeof(queryParams) != "string") {
            queryParams = Object.keys(queryParams).map(key => encodeURIComponent(key) + "=" + encodeURIComponent(queryParams[key])).join("&")
        }
        if (queryParams) {
            url=url+"?"+queryParams
        }
        var headers = this.createHeaders(mediaType)
        return fetch(url, {
            method: 'get',
            headers: headers,
            credentials: "same-origin"
        }).then((response) => {
            if (response.ok) {
                return Promise.resolve(response.json())
            } else {
                let error = new Error(response.statusText);
                return response.json().then(json => {
                    error.data = json;
                })
                .then(() => Promise.reject(error),() => Promise.reject(error));
            }
        })
    },
    delete: function(url) {
        var headers = this.createHeaders()
        return fetch(url, {
            method: 'delete',
            headers: headers,
            credentials: "same-origin"
        }).then((response) => {
            // Not expecting a response body
            if (response.ok && response.status === 204) {
                return Promise.resolve();
            } else {
                let error = new Error(response.statusText);
                return response.json().then(json => {
                    error.data = json;
                })
                .then(() => Promise.reject(error),() => Promise.reject(error));
            }
        })
    }
}