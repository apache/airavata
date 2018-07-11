import Group from './models/Group'
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
    "GroupResourcePreference": {
        url: "/api/group-resource-profiles/",
        viewSet: true
    },
    "SharedEntitiesGroups": {
        url: "/api/shared/group/entities",
        viewSet: true
    },
    "Entities": {
        url: "/api/entities",
        viewSet: [{
            name:"create",
            pagination:true
        }]
    },
    "SharedEntities": {
        url: "/api/shared-entities",
        viewSet: true,
        modelClass: SharedEntity,
    },
    "Groups": {
        url: "/api/groups",
        viewSet: true,
        pagination: true,
        queryParams: ['limit', 'offset'],
        modelClass: Group,
    },
    "UserProfiles": {
        url: "/api/user-profiles",
        viewSet: [{
            name: "list"
        }],
        modelClass: UserProfile,
    },
}