export default {
  isValidationError(error) {
    return (
      error.details &&
      error.details.status === 400 &&
      error.details.response &&
      !this.isAPIException(error)
    );
  },
  isAPIException(error) {
    // Django REST Framework API exceptions have a 'detail' key
    // https://www.django-rest-framework.org/api-guide/exceptions/
    return (
      error.details &&
      error.details.response &&
      "detail" in error.details.response
    );
  },
  isUnauthorizedError(error) {
    return this.isAPIException(error) && error.details.status === 403;
  },
  isNotFoundError(error) {
    return this.isAPIException(error) && error.details.status === 404;
  },
  isUnauthenticatedError(error) {
    return (
      this.isAPIException(error) &&
      [401, 403].includes(error.details.status) &&
      "is_authenticated" in error.details.response &&
      error.details.response.is_authenticated === false
    );
  },
  buildLoginUrl(includeNextParameter = true) {
    let loginUrl = "/auth/login";
    if (includeNextParameter) {
      let currentURL = window.location.pathname;
      if (window.location.search) {
        currentURL += window.location.search;
      }
      loginUrl += `?next=${encodeURIComponent(currentURL)}`;
    }
    return loginUrl;
  },
};
