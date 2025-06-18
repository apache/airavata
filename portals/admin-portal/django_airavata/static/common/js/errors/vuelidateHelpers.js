export function validateState(validation) {
  const { $dirty, $error } = validation;
  return $dirty ? !$error : null;
}

/**
 * Return false if there is a validation error, null otherwise.
 *
 * This is just like validateState except it doesn't return true when valid
 * which is useful if you only want to show invalid feedback.
 *
 * @param {*} validation
 * @returns
 */
export function validateStateErrorOnly(validation) {
  const { $dirty, $error } = validation;
  return $dirty && $error ? false : null;
}
