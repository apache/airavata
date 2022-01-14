export default {
  createValidationFeedback(data, validationErrors) {
    const validationFeedback = {};
    if (!data) {
      return validationFeedback;
    }
    for (const fieldName in data) {
      if (data.hasOwnProperty(fieldName)) {
        const errorMessages = validationErrors
          ? validationErrors[fieldName]
          : null;
        if (errorMessages) {
          validationFeedback[fieldName] = {
            invalidFeedback: errorMessages,
            state: false,
          };
        } else {
          validationFeedback[fieldName] = {
            invalidFeedback: null,
            state: null,
          };
        }
      }
    }
    return validationFeedback;
  },
};
