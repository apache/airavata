const post = "post";
const get = "get";
const put = "put";
const del = "delete";

export default {
    "GroupResourcePreference": {
        "list": {
            url: "/api/group-resource-profiles",
            requestType: get
        },
        "instance": {
            url: "/api/group-resource-profiles/<lookup_value:lookUp>",
            requestType: get,
            queryParams:[
                "name1",{
                "name2":"name3"
                }
            ]
        },
        "create": {
            url: "/api/group-resource-profile",
            requestType: post,
            bodyParams: {
                name: "groupResourceProfile"
            }
        },
        "update": {
            url: "",
            requestType: post,
            bodyParams: {
                name: "groupResourceProfile"
            }
        }
    }
}