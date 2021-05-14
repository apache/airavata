import MaxLengthValidator from "./MaxLengthValidator";
import MinLengthValidator from "./MinLengthValidator";
import RegularExpressionValidator from "./RegularExpressionValidator";

const VALIDATOR_MAPPING = {
  "max-length": MaxLengthValidator,
  "min-length": MinLengthValidator,
  regex: RegularExpressionValidator,
};
export default class ValidatorFactory {
  validate(validationsConfig, value) {
    const errorMessages = [];
    validationsConfig.forEach((validation) => {
      let validatorClass = VALIDATOR_MAPPING[validation.type];
      let validator = new validatorClass(validation);
      let errorMessage = validator.validate(value);
      if (errorMessage !== null) {
        errorMessages.push(Promise.resolve(errorMessage));
      }
    });
    return errorMessages;
  }
}
