import ApplicationDeploymentDescription from "./models/ApplicationDeploymentDescription";
import ApplicationInterfaceDefinition from "./models/ApplicationInterfaceDefinition";
import ApplicationModule from "./models/ApplicationModule";
import BatchQueue from "./models/BatchQueue";
import ComputeResourceDescription from "./models/ComputeResourceDescription";
import CredentialSummary from "./models/CredentialSummary";
import DataProduct from "./models/DataProduct";
import Experiment from "./models/Experiment";
import ExperimentSearchFields from "./models/ExperimentSearchFields";
import ExperimentStatistics from "./models/ExperimentStatistics";
import ExperimentStoragePath from "./models/ExperimentStoragePath";
import ExperimentSummary from "./models/ExperimentSummary";
import ExtendedUserProfileField from "./models/ExtendedUserProfileField";
import ExtendedUserProfileValue from "./models/ExtendedUserProfileValue";
import FullExperiment from "./models/FullExperiment";
import GatewayResourceProfile from "./models/GatewayResourceProfile";
import Group from "./models/Group";
import GroupResourceProfile from "./models/GroupResourceProfile";
import IAMUserProfile from "./models/IAMUserProfile";
import LogRecord from "./models/LogRecord";
import Notification from "./models/Notification";
import Parser from "./models/Parser";
import Project from "./models/Project";
import QueueSettingsCalculator from "./models/QueueSettingsCalculator";
import Settings from "./models/Settings";
import SharedEntity from "./models/SharedEntity";
import StoragePreference from "./models/StoragePreference";
import StorageResourceDescription from "./models/StorageResourceDescription";
import UnverifiedEmailUserProfile from "./models/UnverifiedEmailUserProfile";
import User from "./models/User";
import UserProfile from "./models/UserProfile";
import UserStoragePath from "./models/UserStoragePath";
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
        modelClass: BatchQueue,
      },
    },
    queryParams: ["appModuleId", "groupResourceProfileId"],
    modelClass: ApplicationDeploymentDescription,
  },
  ApplicationInterfaces: {
    url: "/api/application-interfaces",
    viewSet: true,
    modelClass: ApplicationInterfaceDefinition,
  },
  ApplicationModules: {
    url: "/api/applications",
    viewSet: true,
    methods: {
      getApplicationInterface: {
        url: "/api/applications/<lookup>/application_interface/",
        requestType: "get",
        modelClass: ApplicationInterfaceDefinition,
      },
      getApplicationDeployments: {
        url: "/api/applications/<lookup>/application_deployments/",
        requestType: "get",
        modelClass: ApplicationDeploymentDescription,
      },
      listAll: {
        url: "/api/applications/list_all/",
        requestType: "get",
        modelClass: ApplicationModule,
      },
      favorite: {
        url: "/api/applications/<lookup>/favorite/",
        requestType: "post",
      },
      unfavorite: {
        url: "/api/applications/<lookup>/unfavorite/",
        requestType: "post",
      },
    },
    modelClass: ApplicationModule,
  },
  ComputeResources: {
    url: "/api/compute-resources",
    viewSet: ["retrieve"],
    methods: {
      names: {
        url: "/api/compute-resources/all_names/",
        requestType: "get",
      },
      namesList: {
        url: "/api/compute-resources/all_names_list/",
        requestType: "get",
      },
    },
    modelClass: ComputeResourceDescription,
  },
  CredentialSummaries: {
    url: "/api/credential-summaries/",
    viewSet: ["list", "retrieve", "delete"],
    methods: {
      allSSHCredentials: {
        url: "/api/credential-summaries/ssh/",
        requestType: "get",
        modelClass: CredentialSummary,
      },
      allPasswordCredentials: {
        url: "/api/credential-summaries/password/",
        requestType: "get",
        modelClass: CredentialSummary,
      },
      createSSH: {
        url: "/api/credential-summaries/create_ssh/",
        requestType: "post",
        bodyParams: {
          name: "data",
        },
        modelClass: CredentialSummary,
      },
      createPassword: {
        url: "/api/credential-summaries/create_password/",
        requestType: "post",
        bodyParams: {
          name: "data",
        },
        modelClass: CredentialSummary,
      },
    },
    modelClass: CredentialSummary,
  },
  DataProducts: {
    url: "/api/data-products/",
    methods: {
      retrieve: {
        requestType: "get",
        queryParams: {
          lookup: "product-uri",
        },
        modelClass: DataProduct,
      },
    },
  },
  Experiments: {
    url: "/api/experiments/",
    viewSet: true,
    methods: {
      launch: {
        url: "/api/experiments/<lookup>/launch/",
        requestType: "post",
        modelClass: Experiment,
      },
      clone: {
        url: "/api/experiments/<lookup>/clone/",
        requestType: "post",
        modelClass: Experiment,
      },
      cancel: {
        url: "/api/experiments/<lookup>/cancel/",
        requestType: "post",
        modelClass: Experiment,
      },
      fetchIntermediateOutputs: {
        url: "/api/experiments/<lookup>/fetch_intermediate_outputs/",
        requestType: "post",
        bodyParams: {
          name: "data",
        },
      },
    },
    modelClass: Experiment,
  },
  ExperimentSearch: {
    url: "/api/experiment-search",
    viewSet: [
      {
        name: "list",
        initialDataParam: "initialData",
      },
    ],
    modelClass: ExperimentSummary,
    pagination: true,
    queryParams: ["limit", "offset"].concat(
      ExperimentSearchFields.values.map((f) => f.name)
    ),
  },
  ExperimentStatistics: {
    url: "/api/experiment-statistics",
    methods: {
      get: {
        url: "/api/experiment-statistics",
        requestType: "get",
        queryParams: [
          "fromTime",
          "toTime",
          "userName",
          "applicationName",
          "resourceHostName",
          "limit",
          "offset",
        ],
        pagination: true,
        modelClass: ExperimentStatistics,
      },
    },
  },
  ExperimentStoragePaths: {
    url: "/api/experiment-storage",
    methods: {
      get: {
        url: "/api/experiment-storage/<experimentId>/<path>",
        requestType: "get",
        modelClass: ExperimentStoragePath,
        // NOTE: caller needs to explicitly escape experimentId, but path shouldn't be escaped
        encodePathParams: false,
      },
    },
  },
  ExtendedUserProfileFields: {
    url: "/auth/extended-user-profile-fields",
    viewSet: true,
    modelClass: ExtendedUserProfileField,
  },
  ExtendedUserProfileValues: {
    url: "/auth/extended-user-profile-values",
    viewSet: true,
    modelClass: ExtendedUserProfileValue,
    queryParams: ["username"],
    methods: {
      saveAll: {
        url: "/auth/extended-user-profile-values/save-all/",
        requestType: "post",
        modelClass: ExtendedUserProfileValue,
        bodyParams: {
          name: "data",
        },
      },
    },
  },
  FullExperiments: {
    url: "/api/full-experiments",
    viewSet: [
      {
        name: "retrieve",
        initialDataParam: "initialFullExperimentData",
      },
    ],
    modelClass: FullExperiment,
  },
  GatewayResourceProfile: {
    url: "/api/gateway-resource-profile/",
    methods: {
      get: {
        url: "/api/gateway-resource-profile/",
        requestType: "get",
        modelClass: GatewayResourceProfile,
      },
      update: {
        requestType: "put",
        bodyParams: {
          name: "data",
        },
        modelClass: GatewayResourceProfile,
      },
    },
    modelClass: GatewayResourceProfile,
  },
  GroupResourceProfiles: {
    url: "/api/group-resource-profiles/",
    viewSet: true,
    modelClass: GroupResourceProfile,
  },
  Groups: {
    url: "/api/groups",
    viewSet: true,
    pagination: true,
    queryParams: ["limit", "offset"],
    modelClass: Group,
  },
  IAMUserProfiles: {
    url: "/api/iam-user-profiles",
    viewSet: true,
    pagination: true,
    methods: {
      enable: {
        url: "/api/iam-user-profiles/<lookup>/enable/",
        requestType: "post",
        modelClass: IAMUserProfile,
      },
      updateUsername: {
        url: "/api/iam-user-profiles/update_username/",
        bodyParams: {
          name: "data",
        },
        requestType: "put",
        modelClass: IAMUserProfile,
      },
    },
    queryParams: ["limit", "offset", "search"],
    modelClass: IAMUserProfile,
  },
  LogRecords: {
    url: "/api/log",
    methods: {
      send: {
        url: "/api/log",
        requestType: "post",
        bodyParams: {
          name: "data",
        },
        modelClass: LogRecord,
      },
    },
    modelClass: LogRecord,
  },
  Parsers: {
    url: "/api/parsers",
    viewSet: true,
    queryParams: ["limit", "offset"],
    modelClass: Parser,
  },
  Projects: {
    url: "/api/projects",
    viewSet: true,
    pagination: true,
    methods: {
      listAll: {
        url: "/api/projects/list_all/",
        requestType: "get",
        modelClass: Project,
      },
    },
    queryParams: ["limit", "offset"],
    modelClass: Project,
  },
  QueueSettingsCalculators: {
    url: "/api/queue-settings-calculators",
    viewSet: ["retrieve", "list"],
    methods: {
      calculate: {
        url: "/api/queue-settings-calculators/<lookup>/calculate/",
        requestType: "post",
        bodyParams: {
          name: "data",
        },
      },
    },
    modelClass: QueueSettingsCalculator,
  },
  Settings: {
    url: "/api/settings/",
    methods: {
      get: {
        url: "/api/settings/",
        requestType: "get",
        modelClass: Settings,
      },
    },
  },
  SharedEntities: {
    url: "/api/shared-entities",
    viewSet: ["retrieve", "update"],
    methods: {
      merge: {
        url: "/api/shared-entities/<lookup>/merge/",
        bodyParams: {
          name: "data",
        },
        requestType: "put",
        modelClass: SharedEntity,
      },
    },
    modelClass: SharedEntity,
  },
  StoragePreferences: {
    url: "/api/storage-preferences/",
    viewSet: true,
    modelClass: StoragePreference,
  },
  StorageResources: {
    url: "/api/storage-resources",
    viewSet: ["retrieve"],
    methods: {
      names: {
        url: "/api/storage-resources/all_names/",
        requestType: "get",
      },
    },
    modelClass: StorageResourceDescription,
  },
  UnverifiedEmailUsers: {
    url: "/api/unverified-email-users",
    viewSet: true,
    pagination: true,
    queryParams: ["limit", "offset"],
    modelClass: UnverifiedEmailUserProfile,
  },
  Users: {
    url: "/auth/users",
    viewSet: true,
    methods: {
      current: {
        url: "/auth/users/current/",
        requestType: "get",
      },
      resendEmailVerification: {
        url: "/auth/users/<lookup>/resend_email_verification/",
        requestType: "post",
      },
      verifyEmailChange: {
        url: "/auth/users/<lookup>/verify_email_change/",
        requestType: "post",
        bodyParams: {
          name: "data",
        },
        modelClass: User,
      },
    },
    modelClass: User,
  },
  UserProfiles: {
    url: "/api/user-profiles",
    viewSet: ["list", "retrieve"],
    modelClass: UserProfile,
  },
  UserStoragePaths: {
    url: "/api/user-storage",
    methods: {
      get: {
        url: "/api/user-storage/<path>",
        requestType: "get",
        modelClass: UserStoragePath,
        encodePathParams: false,
      },
    },
  },
  WorkspacePreferences: {
    url: "/api/workspace-preferences",
    methods: {
      get: {
        url: "/api/workspace-preferences",
        requestType: "get",
        modelClass: WorkspacePreferences,
      },
    },
  },
  ManageNotifications: {
    url: "/api/manage-notifications/",
    viewSet: true,
    pagination: false,
    modelClass: Notification,
  },
  APIServerStatusCheck: {
    url: "/api/api-status-check",
    methods: {
      get: {
        url: "/api/api-status-check/",
        requestType: "get",
      },
    },
  },
};
