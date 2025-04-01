export const resourceTypeToColor = (type: string) => {
  if (type === "NOTEBOOK") {
    return "blue";
  } else if (type === "REPOSITORY") {
    return "red";
  } else if (type === "DATASET") {
    return "green";
  } else if (type === "MODEL") {
    return "purple";
  } else {
    return "gray";
  }
}

export const isValidImaage = (url: string) => {
  // should start with http or https 

  if (url.startsWith("http")) {
    return true;
  }
  return false
}