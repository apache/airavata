import PaginationIterator from '../utils/PaginationIterator'
import FetchUtils from '../utils/FetchUtils'
import serviceConfiguration from '../service_config'

const requestTypeKey = "requestType";
const postKey = "post";
const getKey = "get";
const putKey = "put";
const delKey = "delete";

const parsePathParams = function (url) {
    var pathParamsRegEx = new RegExp('<[a-zA-Z0-9_]+(:[a-zA-Z0-9_]*)?>', 'g');
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
}

const parseServiceMapping = function (serviceConfiguration) {
    let supportedFunctions = [];
    if (serviceConfiguration.viewSet === true) {
        supportedFunctions = ['list', 'create', 'update', 'delete', 'retrieve'];
    } else if (serviceConfiguration.viewSet instanceof Array) {
        supportedFunctions = serviceConfiguration.viewSet;
    }
    if (supportedFunctions) {
        let url = serviceConfiguration.url;
        if (!url.endsWith("/")) {
            url = url + "/";
        }
        let modelClass = serviceConfiguration.modelClass;
        let queryParams = serviceConfiguration.queryParams;
        let defaultPagination = (serviceConfiguration.pagination) ? true : false;
        delete serviceConfiguration.viewSet;
        delete serviceConfiguration.url;
        delete serviceConfiguration.modelClass;
        delete serviceConfiguration.queryParams;
        delete serviceConfiguration.pagination;
        for (let supportedFunction of supportedFunctions) {
            let supportedFunctionName = supportedFunction
            let pagination = defaultPagination;
            if (typeof(supportedFunctionName) !== "string") {
                supportedFunctionName = supportedFunction.name
                if ('pagination' in supportedFunction) {
                  pagination = supportedFunction.pagination
                }
            }
            switch (supportedFunctionName) {
                case "list":
                    serviceConfiguration["list"] = {
                        url: url,
                        requestType: getKey,
                        modelClass: modelClass,
                        queryParams: queryParams,
                        initialDataParam: supportedFunction.initialDataParam,
                    }
                    break;
                case "create":
                    serviceConfiguration["create"] = {
                        url: url,
                        requestType: postKey,
                        bodyParams: {
                            name: "data"
                        },
                        modelClass: modelClass,
                        queryParams: queryParams,
                    }
                    break;
                case "update":
                    serviceConfiguration["update"] = {
                        url: url + "<lookup>/",
                        requestType: putKey,
                        bodyParams: {
                            name: "data"
                        },
                        modelClass: modelClass,
                        queryParams: queryParams,
                    }
                    break;
                case  "retrieve":
                    serviceConfiguration["retrieve"] = {
                        url: url + "<lookup>/",
                        requestType: getKey,
                        modelClass: modelClass,
                        queryParams: queryParams,
                        initialDataParam: supportedFunction.initialDataParam,
                    }
                    break;
                case "delete":
                    serviceConfiguration["delete"] = {
                        url: url + "<lookup>/",
                        requestType: delKey,
                        modelClass: modelClass,
                        queryParams: queryParams,
                    }
                    break;
                default:
                    // Assume all fields have been provided
                    serviceConfiguration[supportedFunctionName] = supportedFunction;
                    break;
            }
            serviceConfiguration[supportedFunctionName].pagination = pagination
        }
    }
}

const parseQueryMapping = function (queryParamsMapping) {
    let newQueryParamMapping = {};
    if (!queryParamsMapping) {
        return newQueryParamMapping;
    }
    for (let queryParam of queryParamsMapping) {
        if (typeof(queryParam) === "string") {
            newQueryParamMapping[queryParam] = null;
        } else {
            newQueryParamMapping[queryParam.value] = queryParam.name;
        }
    }
    return newQueryParamMapping;
}

class ServiceFactory {
    constructor(serviceConfigurations) {
        for (let serviceName of Object.keys(serviceConfigurations)) {
            parseServiceMapping(serviceConfigurations[serviceName]);
        }
        this.serviceConfigurations = serviceConfigurations;
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
                    throw new TypeError("Invalid request type: " + config.requestType + " for the function: " + functionName + " in the service: " + serviceName);
            }
            let pathParamsMapping = parsePathParams(config.url);
            let queryParamsMapping = parseQueryMapping(config.queryParams);
            serviceObj[functionName] = function (params = {}, { ignoreErrors } = { ignoreErrors: false }) {
                let url = config.url;
                let paramKeys = Object.keys(params);
                let queryParams = {};
                let bodyParams = {};
                let initialData = undefined;
                for (let paramKey of paramKeys) {
                    if (paramKey in pathParamsMapping) {
                        if (pathParamsMapping[paramKey] !== null) {
                            url = url.replace('<' + pathParamsMapping[paramKey] + ':' + paramKey + '>', encodeURIComponent(params[paramKey]));
                        } else {
                            url = url.replace('<' + paramKey + '>', encodeURIComponent(params[paramKey]));
                        }
                    } else if (paramKey in queryParamsMapping) {
                        if (queryParamsMapping[paramKey] === null) {
                            queryParams[paramKey] = params[paramKey];
                        } else {
                            queryParams[queryParamsMapping[paramKey]] = params[paramKey];
                        }
                    }
                    else if ((config.requestType == postKey || config.requestType == putKey) && config.bodyParams instanceof Array && paramKey in config.bodyParams) {
                        bodyParams[paramKey] = params[paramKey];
                    } else if ((config.requestType == postKey || config.requestType == putKey) && config.bodyParams !== null && config.bodyParams.name == paramKey) {
                        bodyParams = params[paramKey];
                    } else if (config.initialDataParam && paramKey === config.initialDataParam) {
                        initialData = params[paramKey];
                    }
                }
                let paginationHandler = (data) => {
                    if (config.pagination === true && 'next' in data) {
                        return new PaginationIterator(data, config.modelClass);
                    } else if (data instanceof Array) {
                        return data.map(item => resultHandler(item));
                    } else {
                        return resultHandler(data);
                    }
                };
                let resultHandler = (data) => {
                    return (config.modelClass) ? new config.modelClass(data) : data;
                }
                switch (config.requestType.toLowerCase()) {
                    case postKey:
                        return FetchUtils.post(url, bodyParams, queryParams, { ignoreErrors }).then(resultHandler);
                    case getKey:
                        if (initialData) {
                          return Promise.resolve(paginationHandler(initialData));
                        } else {
                          return FetchUtils.get(url, queryParams, { ignoreErrors }).then(paginationHandler);
                        }
                    case putKey:
                        return FetchUtils.put(url, bodyParams, { ignoreErrors }).then(resultHandler);
                    case delKey:
                        return FetchUtils.delete(url, { ignoreErrors });
                }
            }
        }
        return serviceObj;
    }
}

export default new ServiceFactory(serviceConfiguration);
