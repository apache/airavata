import ApplicationDeploymentDescription from "./models/ApplicationDeploymentDescription";
import ApplicationInterfaceDefinition from "./models/ApplicationInterfaceDefinition";
import ApplicationModule from "./models/ApplicationModule";
import BatchQueue from "./models/BatchQueue";
import ComputeResourceDescription from "./models/ComputeResourceDescription";
import CredentialSummary from "./models/CredentialSummary";
import DataProduct from "./models/DataProduct";
import Experiment from "./models/Experiment";
import ExperimentSummary from "./models/ExperimentSummary";
import FullExperiment from "./models/FullExperiment";
import GatewayResourceProfile from "./models/GatewayResourceProfile";
import Group from "./models/Group";
import GroupResourceProfile from "./models/GroupResourceProfile";
import Parser from "./models/Parser";
import SharedEntity from "./models/SharedEntity";
import StoragePreference from "./models/StoragePreference";
import StorageResourceDescription from "./models/StorageResourceDescription";
import UserProfile from "./models/UserProfile";
import WorkspacePreferences from "./models/WorkspacePreferences";

/*
examples:

Generating Services based on the API view set
{
  serviceName:{
    url:'/example/api',
    viewSet:true, // or array of supported view set method names
    pagination: true/false,
    modelClass: ModelClass,
  }
}
Normal service configuration:
{
  serviceName:{
    methods: {
      serviceAction1:{
        url:'/example/api/<look_up>',  # the <look_up> implies a path parameter lok_up, defaults to service's url
        requestType:'post', # defaults to "get"
        bodyParams:[...] # body parameter names for json parameter if body
          params id=s a list of array else an object with the param name for the
          body object
        queryParams:[], # list query param names/ query param name to param
          name mapping, defaults to service's queryParams
        pagination:true, # whether to treat the response as a paginated
          response, defaults to service's pagination
        modelClass: ModelClass
      }
    }
  }
}
 */

export default {
  ApplicationDeployments: {
    url: "/api/application-deployments",
    viewSet: true,
    methods: {
      getQueues: {
        url: "/api/application-deployments/<lookup>/queues/",
        requestType: "get",
        modelClass: BatchQueue
      }
    },
    queryParams: ["appModuleId", "groupResourceProfileId"],
    modelClass: ApplicationDeploymentDescription
  },
  ApplicationInterfaces: {
    url: "/api/application-interfaces",
    viewSet: true,
    modelClass: ApplicationInterfaceDefinition
  },
  ApplicationModules: {
    url: "/api/applications",
    viewSet: true,
    methods: {
      getApplicationInterface: {
        url: "/api/applications/<lookup>/application_interface/",
        requestType: "get",
        modelClass: ApplicationInterfaceDefinition
      },
      getApplicationDeployments: {
        url: "/api/applications/<lookup>/application_deployments/",
        requestType: "get",
        modelClass: ApplicationDeploymentDescription
      },
      listAll: {
        url: "/api/applications/list_all/",
        requestType: "get",
        modelClass: ApplicationModule
      }
    },
    modelClass: ApplicationModule
  },
  ComputeResources: {
    url: "/api/compute-resources",
    viewSet: ["retrieve"],
    methods: {
      names: {
        url: "/api/compute-resources/all_names/",
        requestType: "get"
      },
      namesList: {
        url: "/api/compute-resources/all_names_list/",
        requestType: "get"
      }
    },
    modelClass: ComputeResourceDescription
  },
  CredentialSummaries: {
    url: "/api/credential-summaries/",
    viewSet: ["list", "retrieve", "delete"],
    methods: {
      allSSHCredentials: {
        url: "/api/credential-summaries/ssh/",
        requestType: "get",
        modelClass: CredentialSummary
      },
      allPasswordCredentials: {
        url: "/api/credential-summaries/password/",
        requestType: "get",
        modelClass: CredentialSummary
      },
      createSSH: {
        url: "/api/credential-summaries/create_ssh/",
        requestType: "post",
        bodyParams: {
          name: "data"
        },
        modelClass: CredentialSummary
      },
      createPassword: {
        url: "/api/credential-summaries/create_password/",
        requestType: "post",
        bodyParams: {
          name: "data"
        },
        modelClass: CredentialSummary
      }
    },
    modelClass: CredentialSummary
  },
  DataProducts: {
    url: "/api/data-products/",
    methods: {
      retrieve: {
        requestType: "get",
        queryParams: {
          lookup: "product-uri"
        },
        modelClass: DataProduct
      }
    }
  },
  Experiments: {
    url: "/api/experiments/",
    viewSet: true,
    methods: {
      launch: {
        url: "/api/experiments/<lookup>/launch/",
        requestType: "post",
        modelClass: Experiment
      },
      clone: {
        url: "/api/experiments/<lookup>/clone/",
        requestType: "post",
        modelClass: Experiment
      }
    },
    modelClass: Experiment
  },
  ExperimentSearch: {
    url: "/api/experiment-search",
    viewSet: [
      {
        name: "list",
        initialDataParam: "initialData"
      }
    ],
    modelClass: ExperimentSummary,
    pagination: true,
    queryParams: ["limit", "offset"]
  },
  FullExperiments: {
    url: "/api/full-experiments",
    viewSet: [
      {
        name: "retrieve",
        initialDataParam: "initialFullExperimentData"
      }
    ],
    modelClass: FullExperiment
  },
  GatewayResourceProfiles: {
    url: "/api/gateway-resource-profiles/",
    viewSet: true,
    methods: {
      current: {
        url: "/api/gateway-resource-profile/",
        requestType: "get",
        modelClass: GatewayResourceProfile
      }
    },
    modelClass: GatewayResourceProfile
  },
  GroupResourceProfiles: {
    url: "/api/group-resource-profiles/",
    viewSet: true,
    modelClass: GroupResourceProfile
  },
  Groups: {
    url: "/api/groups",
    viewSet: true,
    pagination: true,
    queryParams: ["limit", "offset"],
    modelClass: Group
  },
  Parsers: {
    url: "/api/parsers",
    viewSet: true,
    queryParams: ["limit", "offset"],
    modelClass: Parser
  },
  SharedEntities: {
    url: "/api/shared-entities",
    viewSet: ["retrieve", "update"],
    methods: {
      merge: {
        url: "/api/shared-entities/<lookup>/merge/",
        bodyParams: {
          name: "data"
        },
        requestType: "put",
        modelClass: SharedEntity
      }
    },
    modelClass: SharedEntity
  },
  StoragePreferences: {
    url: "/api/storage-preferences/",
    viewSet: true,
    modelClass: StoragePreference
  },
  StorageResources: {
    url: "/api/storage-resources",
    viewSet: ["retrieve"],
    methods: {
      names: {
        url: "/api/storage-resources/all_names/",
        requestType: "get"
      }
    },
    modelClass: StorageResourceDescription
  },
  UserProfiles: {
    url: "/api/user-profiles",
    viewSet: ["list"],
    modelClass: UserProfile
  },
  WorkspacePreferences: {
    url: "/api/workspace-preferences",
    methods: {
      get: {
        url: "/api/workspace-preferences",
        requestType: "get",
        modelClass: WorkspacePreferences
      }
    }
  }
};
