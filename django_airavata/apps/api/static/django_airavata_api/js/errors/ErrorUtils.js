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
  }
};
