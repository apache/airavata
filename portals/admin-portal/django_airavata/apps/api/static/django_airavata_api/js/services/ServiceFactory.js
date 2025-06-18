import PaginationIterator from "../utils/PaginationIterator";
import FetchUtils from "../utils/FetchUtils";
import serviceConfiguration from "../service_config";

const postKey = "post";
const getKey = "get";
const putKey = "put";
const delKey = "delete";

const parsePathParams = function (url) {
  var pathParamsRegEx = new RegExp("<[a-zA-Z0-9_]+(:[a-zA-Z0-9_]*)?>", "g");
  let pathParamsMatch = url.match(pathParamsRegEx);
  let pathParams = {};
  if (!pathParamsMatch) {
    return pathParams;
  }
  for (let pathParamMatch of pathParamsMatch) {
    let pathParam = pathParamMatch.split(":");
    if (pathParam.length == 2) {
      pathParams[pathParam[1]] = pathParam[0].replace(/<|>/gi, "");
    } else {
      pathParams[pathParam[0].replace(/<|>/gi, "")] = null;
    }
  }
  return pathParams;
};

const parseServiceMapping = function (serviceConfiguration) {
  const mappedFunctions = {};
  let viewSetFunctions = [];
  if (serviceConfiguration.viewSet === true) {
    viewSetFunctions = ["list", "create", "update", "delete", "retrieve"];
  } else if (serviceConfiguration.viewSet instanceof Array) {
    viewSetFunctions = serviceConfiguration.viewSet;
  }
  let url = serviceConfiguration.url;
  if (!url.endsWith("/")) {
    url = url + "/";
  }
  let modelClass = serviceConfiguration.modelClass;
  let queryParams = serviceConfiguration.queryParams;
  let defaultPagination = serviceConfiguration.pagination ? true : false;
  let encodePathParams =
    "encodePathParams" in serviceConfiguration
      ? serviceConfiguration.encodePathParams
      : true;
  for (let viewSetFunction of viewSetFunctions) {
    let viewSetFunctionName = viewSetFunction;
    let pagination = defaultPagination;
    if (typeof viewSetFunctionName !== "string") {
      viewSetFunctionName = viewSetFunction.name;
      if ("pagination" in viewSetFunction) {
        pagination = viewSetFunction.pagination;
      }
    }
    switch (viewSetFunctionName) {
      case "list":
        mappedFunctions["list"] = {
          url: url,
          requestType: getKey,
          modelClass: modelClass,
          queryParams: queryParams,
          initialDataParam: viewSetFunction.initialDataParam,
          encodePathParams: encodePathParams,
        };
        break;
      case "create":
        mappedFunctions["create"] = {
          url: url,
          requestType: postKey,
          bodyParams: {
            name: "data",
          },
          modelClass: modelClass,
          queryParams: queryParams,
          encodePathParams: encodePathParams,
        };
        break;
      case "update":
        mappedFunctions["update"] = {
          url: url + "<lookup>/",
          requestType: putKey,
          bodyParams: {
            name: "data",
          },
          modelClass: modelClass,
          queryParams: queryParams,
          encodePathParams: encodePathParams,
        };
        break;
      case "retrieve":
        mappedFunctions["retrieve"] = {
          url: url + "<lookup>/",
          requestType: getKey,
          modelClass: modelClass,
          queryParams: queryParams,
          initialDataParam: viewSetFunction.initialDataParam,
          encodePathParams: encodePathParams,
        };
        break;
      case "delete":
        mappedFunctions["delete"] = {
          url: url + "<lookup>/",
          requestType: delKey,
          modelClass: modelClass,
          queryParams: queryParams,
          encodePathParams: encodePathParams,
        };
        break;
      default:
        // Assume all fields have been provided
        mappedFunctions[viewSetFunctionName] = viewSetFunction;
        break;
    }
    mappedFunctions[viewSetFunctionName].pagination = pagination;
  }
  if ("methods" in serviceConfiguration) {
    for (let methodName of Object.keys(serviceConfiguration["methods"])) {
      let methodConfig = serviceConfiguration["methods"][methodName];
      mappedFunctions[methodName] = {
        url: methodConfig.url || url,
        requestType: methodConfig.requestType || getKey,
        queryParams: methodConfig.queryParams || queryParams,
        pagination:
          "pagination" in methodConfig
            ? methodConfig.pagination
            : defaultPagination,
        encodePathParams:
          "encodePathParams" in methodConfig
            ? methodConfig.encodePathParams
            : true,
      };
      if ("modelClass" in methodConfig) {
        mappedFunctions[methodName]["modelClass"] = methodConfig.modelClass;
      }
      if ("bodyParams" in methodConfig) {
        mappedFunctions[methodName]["bodyParams"] = methodConfig.bodyParams;
      }
      if ("pathParams" in methodConfig) {
        mappedFunctions[methodName]["pathParams"] = methodConfig.pathParams;
      }
    }
  }
  return mappedFunctions;
};

const parseQueryMapping = function (queryParamsMapping) {
  let newQueryParamMapping = {};
  if (!queryParamsMapping) {
    return newQueryParamMapping;
  }
  // names of query parameters as an array of strings
  if (queryParamsMapping instanceof Array) {
    for (let queryParam of queryParamsMapping) {
      if (typeof queryParam === "string") {
        newQueryParamMapping[queryParam] = null;
      }
    }
  }
  // mapping of query parameters as an object of names/values
  else {
    for (let queryParam of Object.keys(queryParamsMapping)) {
      newQueryParamMapping[queryParam] = queryParamsMapping[queryParam];
    }
  }
  return newQueryParamMapping;
};

class ServiceFactory {
  constructor(serviceConfigurations) {
    const parsedConfigurations = {};
    for (let serviceName of Object.keys(serviceConfigurations)) {
      parsedConfigurations[serviceName] = parseServiceMapping(
        serviceConfigurations[serviceName]
      );
    }
    this.serviceConfigurations = parsedConfigurations;
  }

  /*
    Creates service object based serviceName in service configuration
     */
  service(serviceName) {
    if (!serviceName) {
      throw new TypeError("Invalid Service Name");
    } else if (!(serviceName in this.serviceConfigurations)) {
      throw new Error("Service :" + serviceName + " could not be found");
    }
    let serviceConfiguration = this.serviceConfigurations[serviceName];
    let serviceObj = {};
    let functionNames = Object.keys(serviceConfiguration);
    for (let functionName of functionNames) {
      let config = serviceConfiguration[functionName];
      switch (config.requestType.toLowerCase()) {
        case postKey:
        case getKey:
        case putKey:
        case delKey:
          break;
        default:
          throw new TypeError(
            "Invalid request type: " +
              config.requestType +
              " for the function: " +
              functionName +
              " in the service: " +
              serviceName
          );
      }
      let pathParamsMapping = parsePathParams(config.url);
      let queryParamsMapping = parseQueryMapping(config.queryParams);
      serviceObj[functionName] = function (
        params = {},
        { ignoreErrors, showSpinner, cache } = {
          ignoreErrors: false,
          showSpinner: true,
          cache: false,
        }
      ) {
        let url = config.url;
        let paramKeys = Object.keys(params);
        let queryParams = {};
        let bodyParams = {};
        let initialData = undefined;
        for (let paramKey of paramKeys) {
          if (paramKey in pathParamsMapping) {
            if (pathParamsMapping[paramKey] !== null) {
              url = url.replace(
                "<" + pathParamsMapping[paramKey] + ":" + paramKey + ">",
                config.encodePathParams
                  ? encodeURIComponent(params[paramKey])
                  : params[paramKey]
              );
            } else {
              url = url.replace(
                "<" + paramKey + ">",
                config.encodePathParams
                  ? encodeURIComponent(params[paramKey])
                  : params[paramKey]
              );
            }
          } else if (paramKey in queryParamsMapping) {
            if (queryParamsMapping[paramKey] === null) {
              queryParams[paramKey] = params[paramKey];
            } else {
              queryParams[queryParamsMapping[paramKey]] = params[paramKey];
            }
          } else if (
            (config.requestType == postKey || config.requestType == putKey) &&
            config.bodyParams instanceof Array &&
            paramKey in config.bodyParams
          ) {
            bodyParams[paramKey] = params[paramKey];
          } else if (
            (config.requestType == postKey || config.requestType == putKey) &&
            config.bodyParams !== null &&
            config.bodyParams.name == paramKey
          ) {
            bodyParams = params[paramKey];
          } else if (
            config.initialDataParam &&
            paramKey === config.initialDataParam
          ) {
            initialData = params[paramKey];
          }
        }
        let paginationHandler = (data) => {
          if (config.pagination === true && "next" in data) {
            return new PaginationIterator(data, config.modelClass);
          } else if (data instanceof Array) {
            return data.map((item) => resultHandler(item));
          } else {
            return resultHandler(data);
          }
        };
        let resultHandler = (data) => {
          if (Array.isArray(data)) {
            return data.map((item) => resultHandler(item));
          }
          return config.modelClass ? new config.modelClass(data) : data;
        };
        switch (config.requestType.toLowerCase()) {
          case postKey:
            return FetchUtils.post(url, bodyParams, queryParams, {
              ignoreErrors,
              showSpinner,
            }).then(resultHandler);
          case getKey:
            if (initialData) {
              return Promise.resolve(paginationHandler(initialData));
            } else {
              return FetchUtils.get(url, queryParams, {
                ignoreErrors,
                showSpinner,
                cache,
              }).then(paginationHandler);
            }
          case putKey:
            return FetchUtils.put(url, bodyParams, {
              ignoreErrors,
              showSpinner,
            }).then(resultHandler);
          case delKey:
            return FetchUtils.delete(url, { ignoreErrors, showSpinner });
        }
      };
    }
    return serviceObj;
  }
}

export default new ServiceFactory(serviceConfiguration);
