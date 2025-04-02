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

export const getGithubOwnerAndRepo = (url: string) => {
  const match = url.match(/github\.com\/([^/]+)\/([^/]+)/);
  if (match) {
    const owner = match[1];
    const repo = match[2].replace(/\.git$/, "");
    return { owner, repo };
  }
  return null;
}