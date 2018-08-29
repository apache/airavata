import ApplicationDeploymentDescription from './models/ApplicationDeploymentDescription'
import ApplicationModule from './models/ApplicationModule';
import CredentialSummary from './models/CredentialSummary'
import Group from './models/Group'
import GroupResourceProfile from './models/GroupResourceProfile'
import SharedEntity from './models/SharedEntity'
import UserProfile from './models/UserProfile'

const post = "post";
const get = "get";
const put = "put";
const del = "delete";
/*
examples:

Generating Services based on the API view set
{
serviceName:{
url:'/example/api',
viewSet:true,
pagination: true/false,
modelClass: ModelClass,
}
}
Normal service configuration:
{
serviceName:{
serviceAction1:{
url:'/example/api/<look_up>',  # the <look_up> implies a path parameter lok_up
requestType:'post',
bodyParams:[...] # body parameter names for json parameter if body params id=s a list of array else an object with the param name for the body object
queryParams:[] # list query param names/ query param name to param name mapping
pagination:true # whether to treat the response as a paginated response


}
}
}
 */

export default {
    "ApplicationDeployments": {
        url: "/api/application-deployments",
        viewSet: true,
        queryParams: ['appModuleId', 'groupResourceProfileId'],
        modelClass: ApplicationDeploymentDescription,
    },
    "ApplicationModules": {
        url: "/api/applications",
        viewSet: true,
        modelClass: ApplicationModule,
    },
    "ComputeResources": {
        url: "/api/compute-resources",
        viewSet: [{
            name: "list"
        }, {
            name: "names",
            url: "/api/compute-resources/all_names/",
            requestType: 'get',
        }, {
            name: "namesList",
            url: "/api/compute-resources/all_names_list/",
            requestType: 'get',
        }],
        modelClass: ApplicationDeploymentDescription,
    },
    "CredentialSummaries": {
        url: "/api/credential-summaries/",
        viewSet: [{
            name: "list"
        }],
        modelClass: CredentialSummary,
    },
    "GroupResourceProfiles": {
        url: "/api/group-resource-profiles/",
        viewSet: true,
        modelClass: GroupResourceProfile,
    },
    "Groups": {
        url: "/api/groups",
        viewSet: true,
        pagination: true,
        queryParams: ['limit', 'offset'],
        modelClass: Group,
    },
    "SharedEntities": {
        url: "/api/shared-entities",
        viewSet: true,
        modelClass: SharedEntity,
    },
    "UserProfiles": {
        url: "/api/user-profiles",
        viewSet: [{
            name: "list"
        }],
        modelClass: UserProfile,
    },
}
