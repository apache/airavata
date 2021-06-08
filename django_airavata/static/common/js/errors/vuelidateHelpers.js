export function validateState(validation) {
  const { $dirty, $error } = validation;
  return $dirty ? !$error : null;
}
