export default {
  isValidationError(error) {
    return (
      error.details &&
      error.details.status === 400 &&
      error.details.response &&
      !("detail" in error.details.response)
    );
  }
};
