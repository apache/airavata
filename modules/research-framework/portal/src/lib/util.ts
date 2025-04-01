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