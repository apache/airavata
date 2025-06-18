export function getProperty(obj, props) {
  if (typeof props === "string") {
    return obj[props];
  } else if (typeof props === "object" && props instanceof Array) {
    // Array
    return props.reduce(
      (o, prop) => (o && prop in o ? o[prop] : undefined),
      obj
    );
  }
}
export function sanitizeHTMLId(id) {
  // Replace anything that isn't an HTML safe id character with underscore
  // Here safe means allowable by HTML5 and also safe to use in a jQuery selector
  return id.replace(/[^a-zA-Z0-9_-]/g, "_");
}
export const dateFormatters = {
  dateTimeInMinutesWithTimeZone: new Intl.DateTimeFormat(undefined, {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "numeric",
    minute: "numeric",
    timeZoneName: "short",
  }),
};
