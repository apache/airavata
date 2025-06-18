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
  /**
   * Return true if the error is an unauthenticated error, i.e., the user needs
   * to log in again.
   *
   * @param {Error} error
   * @returns
   * @see {@link buildLoginUrl} for utility to build re-login url
   */
  isUnauthenticatedError(error) {
    return (
      this.isAPIException(error) &&
      [401, 403].includes(error.details.status) &&
      "is_authenticated" in error.details.response &&
      error.details.response.is_authenticated === false
    );
  },
  /**
   * Build a url that takes the user to the login page.
   *
   * @param {boolean} includeNextParameter - Add a 'next' url to the login url
   *   that will take the user back to this page after login
   * @returns
   */
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
