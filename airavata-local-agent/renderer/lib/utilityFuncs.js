export function titleCase(str) {
  return str.toLowerCase().replace(/\b\w/g, s => s.toUpperCase());
};

export function dateToAgo(date) {
  let seconds = Math.floor((new Date() - date) / 1000);

  let interval = Math.floor(seconds / 31536000);

  if (interval > 1) {
    return interval + " years";
  }
  interval = Math.floor(seconds / 2592000);
  if (interval > 1) {
    return interval + " months";
  }
  interval = Math.floor(seconds / 86400);
  if (interval > 1) {
    return interval + " days";
  }
  interval = Math.floor(seconds / 3600);
  if (interval > 1) {
    return interval + " hours";
  }
  interval = Math.floor(seconds / 60);
  if (interval > 1) {
    return interval + " minutes";
  }
  return Math.floor(seconds) + " seconds";
};

export const getColorScheme = (status) => {
  switch (status) {
    case 'COMPLETED':
      return 'green';
    case 'EXECUTING':
      return 'gray';
    case 'CREATED':
      return 'blue';
    case 'CANCELED':
      return 'yellow';
    case 'FAILED':
      return 'red';
    default:
      return 'red';
  }
};

export const truncTextToN = (str, n) => {
  return (str.length > n) ? str.substr(0, n - 1) + '...' : str;
};

export const SAMPLE_JSON_RESPONSE = {
  "next": "https://md.cybershuttle.org/api/experiment-search/?limit=10&offset=10",
  "previous": null,
  "results": [
    {
      "experimentId": "Clone_of_Clone_of_Clone_of_NAMD_on_May_9,_2024_3:0_581f67a6-0159-44e1-9a89-09313d19d9e9",
      "projectId": "DimuthuSample_efb0b290-7664-4234-8a48-86f7176c297f",
      "gatewayId": "molecular-dynamics",
      "creationTime": 1715282078000,
      "userName": "dwannipu@iu.edu",
      "name": "Clone of Clone of Clone of NAMD on May 9, 2024 3:07 PM",
      "description": null,
      "executionId": "NAMD_dd041e87-1dde-4e57-8ec4-23af2ffa1ba0",
      "resourceHostId": "NCSADelta_e75b0d04-8b4b-417b-8ab4-da76bbd835f5",
      "experimentStatus": "COMPLETED",
      "statusUpdateTime": 1715282249038,
      "url": "https://md.cybershuttle.org/api/experiments/Clone_of_Clone_of_Clone_of_NAMD_on_May_9%2C_2024_3%3A0_581f67a6-0159-44e1-9a89-09313d19d9e9/",
      "project": "https://md.cybershuttle.org/api/projects/DimuthuSample_efb0b290-7664-4234-8a48-86f7176c297f/",
      "userHasWriteAccess": true
    },
    {
      "experimentId": "Clone_of_Clone_of_NAMD_on_May_9,_2024_3:07_PM_6b2232cc-59bf-4d6c-81a0-7574a19a98ee",
      "projectId": "DimuthuSample_efb0b290-7664-4234-8a48-86f7176c297f",
      "gatewayId": "molecular-dynamics",
      "creationTime": 1715282028000,
      "userName": "dwannipu@iu.edu",
      "name": "Clone of Clone of NAMD on May 9, 2024 3:07 PM",
      "description": null,
      "executionId": "NAMD_dd041e87-1dde-4e57-8ec4-23af2ffa1ba0",
      "resourceHostId": "NCSADelta_e75b0d04-8b4b-417b-8ab4-da76bbd835f5",
      "experimentStatus": "CREATED",
      "statusUpdateTime": 1715282028513,
      "url": "https://md.cybershuttle.org/api/experiments/Clone_of_Clone_of_NAMD_on_May_9%2C_2024_3%3A07_PM_6b2232cc-59bf-4d6c-81a0-7574a19a98ee/",
      "project": "https://md.cybershuttle.org/api/projects/DimuthuSample_efb0b290-7664-4234-8a48-86f7176c297f/",
      "userHasWriteAccess": true
    }
  ]
};
