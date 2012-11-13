/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.airavata.services.registry.rest.utils;

public class ResourcePathConstants {


    public final class ConfigResourcePathConstants {
        public static final String CONFIGURATION_REGISTRY_RESOURCE = "/registry/api/congfigregistry/";
        public static final String GET_CONFIGURATION = "get/configuration";
        public static final String GET_CONFIGURATION_LIST = "get/configurationlist";
        public static final String SAVE_CONFIGURATION = "save/configuration";
        public static final String UPDATE_CONFIGURATION = "update/configuration";
        public static final String DELETE_ALL_CONFIGURATION = "delete/allconfiguration";
        public static final String DELETE_CONFIGURATION ="delete/configuration";
        public static final String GET_GFAC_URI_LIST = "get/gfac/urilist";
        public static final String GET_WFINTERPRETER_URI_LIST = "get/workflowinterpreter/urilist";
        public static final String GET_EVENTING_URI = "get/eventingservice/uri";
        public static final String GET_MESSAGE_BOX_URI = "get/messagebox/uri";
        public static final String ADD_GFAC_URI = "add/gfacuri";
        public static final String ADD_WFINTERPRETER_URI = "add/workflowinterpreteruri";
        public static final String ADD_EVENTING_URI = "add/eventinguri";
        public static final String ADD_MESSAGE_BOX_URI = "add/msgboxuri";
        public static final String ADD_GFAC_URI_DATE = "add/gfacuri/date";
        public static final String ADD_WFINTERPRETER_URI_DATE = "add/workflowinterpreteruri/date";
        public static final String ADD_EVENTING_URI_DATE = "add/eventinguri/date";
        public static final String ADD_MSG_BOX_URI_DATE = "add/msgboxuri/date";
        public static final String DELETE_GFAC_URI = "delete/gfacuri";
        public static final String DELETE_ALL_GFAC_URIS = "delete/allgfacuris";
        public static final String DELETE_WFINTERPRETER_URI = "delete/workflowinterpreteruri";
        public static final String DELETE_ALL_WFINTERPRETER_URIS = "delete/allworkflowinterpreteruris";
        public static final String DELETE_EVENTING_URI = "delete/eventinguri";
        public static final String DELETE_MSG_BOX_URI = "delete/msgboxuri";
    }

    public final class DecResourcePathConstants {
        public static final String DESC_RESOURCE_PATH = "/registry/api/descriptors/";
        public static final String HOST_DESC_EXISTS = "hostdescriptor/exist";
        public static final String HOST_DESC_SAVE = "hostdescriptor/save";
        public static final String HOST_DESC_UPDATE = "hostdescriptor/update";
        public static final String HOST_DESC = "host/description";
        public static final String HOST_DESC_DELETE = "hostdescriptor/delete";
        public static final String GET_HOST_DESCS = "get/hostdescriptors";
        public static final String GET_HOST_DESCS_NAMES = "get/hostdescriptor/names";
        public static final String SERVICE_DESC_EXISTS = "servicedescriptor/exist";
        public static final String SERVICE_DESC_SAVE = "servicedescriptor/save";
        public static final String SERVICE_DESC_UPDATE = "servicedescriptor/update";
        public static final String SERVICE_DESC = "servicedescriptor/description";
        public static final String SERVICE_DESC_DELETE = "servicedescriptor/delete";
        public static final String GET_SERVICE_DESCS = "get/servicedescriptors";
        public static final String APPL_DESC_EXIST = "applicationdescriptor/exist";
        public static final String APP_DESC_BUILD_SAVE = "applicationdescriptor/build/save";
        public static final String APP_DESC_UPDATE = "applicationdescriptor/update";
        public static final String APP_DESC_DESCRIPTION = "applicationdescriptor/description";
        public static final String APP_DESC_PER_HOST_SERVICE = "applicationdescriptors/alldescriptors/host/service";
        public static final String APP_DESC_ALL_DESCS_SERVICE = "applicationdescriptor/alldescriptors/service";
        public static final String APP_DESC_ALL_DESCRIPTORS = "applicationdescriptor/alldescriptors";
        public static final String APP_DESC_NAMES = "applicationdescriptor/names";
        public static final String APP_DESC_DELETE = "applicationdescriptor/delete";
    }

    public final class ExperimentResourcePathConstants {
        public static final String EXP_RESOURCE_PATH  =  "/registry/api/experimentregistry/";
        public static final String DELETE_EXP = "delete/experiment";
        public static final String GET_ALL_EXPS = "get/experiments/all" ;
        public static final String GET_EXPS_BY_PROJECT = "get/experiments/project" ;
        public static final String GET_EXPS_BY_DATE =  "get/experiments/date";
        public static final String GET_EXPS_PER_PROJECT_BY_DATE = "get/experiments/project/date";
        public static final String ADD_EXP = "add/experiment" ;
        public static final String EXP_EXISTS = "experiment/exist" ;
        public static final String EXP_EXISTS_CREATE = "experiment/notexist/create" ;
    }

    public final class ProjectResourcePathConstants {
        public static final String REGISTRY_API_PROJECTREGISTRY = "/registry/api/projectregistry/";
        public static final String PROJECT_EXIST = "project/exist";
        public static final String PROJECT_EXIST_CREATE = "project/exist";
        public static final String ADD_PROJECT = "add/project";
        public static final String UPDATE_PROJECT = "update/project";
        public static final String DELETE_PROJECT = "delete/project";
        public static final String GET_PROJECT = "get/project";
        public static final String GET_PROJECTS = "get/projects";
    }

    public final class ProvenanceResourcePathConstants {

        public static final String REGISTRY_API_PROVENANCEREGISTRY = "/registry/api/provenanceregistry/";
        public static final String UPDATE_EXPERIMENT_EXECUTIONUSER = "update/experiment/executionuser";
        public static final String GET_EXPERIMENT_EXECUTIONUSER = "get/experiment/executionuser";
        public static final String GET_EXPERIMENT_NAME = "get/experiment/name";
        public static final String UPDATE_EXPERIMENTNAME = "update/experimentname";
        public static final String GET_EXPERIMENTMETADATA = "get/experimentmetadata";
        public static final String UPDATE_EXPERIMENTMETADATA = "update/experimentmetadata";
        public static final String GET_WORKFLOWTEMPLATENAME = "get/workflowtemplatename";
        public static final String UPDATE_WORKFLOWINSTANCETEMPLATENAME = "update/workflowinstancetemplatename";
        public static final String GET_EXPERIMENTWORKFLOWINSTANCES = "get/experimentworkflowinstances";
        public static final String WORKFLOWINSTANCE_EXIST_CHECK = "workflowinstance/exist/check";
        public static final String WORKFLOWINSTANCE_EXIST_CREATE = "workflowinstance/exist/create";
        public static final String UPDATE_WORKFLOWINSTANCESTATUS_INSTANCEID = "update/workflowinstancestatus/instanceid";
        public static final String UPDATE_WORKFLOWINSTANCESTATUS = "update/workflowinstancestatus";
        public static final String GET_WORKFLOWINSTANCESTATUS = "get/workflowinstancestatus";
        public static final String UPDATE_WORKFLOWNODEINPUT = "update/workflownodeinput";
        public static final String UPDATE_WORKFLOWNODEOUTPUT = "update/workflownodeoutput";
        public static final String GET_EXPERIMENT = "get/experiment";
        public static final String GET_EXPERIMENT_ID_USER = "get/experimentId/user";
        public static final String GET_EXPERIMENT_USER = "get/experiment/user";
        public static final String UPDATE_WORKFLOWNODE_STATUS = "update/workflownode/status";
        public static final String GET_WORKFLOWNODE_STATUS = "get/workflownode/status";
        public static final String GET_WORKFLOWNODE_STARTTIME = "get/workflownode/starttime";
        public static final String GET_WORKFLOW_STARTTIME = "get/workflow/starttime";
        public static final String UPDATE_WORKFLOWNODE_GRAMDATA = "update/workflownode/gramdata";
        public static final String GET_WORKFLOWINSTANCEDATA = "get/workflowinstancedata";
        public static final String WORKFLOWINSTANCE_NODE_EXIST = "wfnode/exist";
        public static final String WORKFLOWINSTANCE_NODE_EXIST_CREATE = "wfnode/exist/create";
        public static final String WORKFLOWINSTANCE_NODE_DATA = "workflowinstance/nodeData";
        public static final String ADD_WORKFLOWINSTANCE = "add/workflowinstance";
        public static final String UPDATE_WORKFLOWNODETYPE = "update/workflownodetype";
        public static final String ADD_WORKFLOWINSTANCENODE = "add/workflowinstancenode";
        public static final String EXPERIMENTNAME_EXISTS = "experimentname/exists";

        public static final String GET_EXPERIMENT_METAINFORMATION = "get/experiment/metainformation";
        public static final String GET_ALL_EXPERIMENT_METAINFORMATION = "get/all/experiment/metainformation";
        public static final String SEARCH_EXPERIMENTS = "search/experiments";
    }

    public final class PublishedWFConstants {

        public static final String REGISTRY_API_PUBLISHWFREGISTRY = "/registry/api/publishwfregistry/";
        public static final String PUBLISHWF_EXIST = "publishwf/exist";
        public static final String PUBLISH_WORKFLOW = "publish/workflow";
        public static final String PUBLISH_DEFAULT_WORKFLOW = "publish/default/workflow";
        public static final String GET_PUBLISHWORKFLOWGRAPH = "get/publishworkflowgraph";
        public static final String GET_PUBLISHWORKFLOWNAMES = "get/publishworkflownames";
        public static final String GET_PUBLISHWORKFLOWS = "get/publishworkflows";
        public static final String REMOVE_PUBLISHWORKFLOW = "remove/publishworkflow";
    }

    public final class UserWFConstants {

        public static final String REGISTRY_API_USERWFREGISTRY = "/registry/api/userwfregistry/";
        public static final String WORKFLOW_EXIST = "workflow/exist";
        public static final String ADD_WORKFLOW = "add/workflow";
        public static final String UPDATE_WORKFLOW = "update/workflow";
        public static final String GET_WORKFLOWGRAPH = "get/workflowgraph";
        public static final String GET_WORKFLOWS = "get/workflows";
        public static final String REMOVE_WORKFLOW = "remove/workflow";
    }

    }
