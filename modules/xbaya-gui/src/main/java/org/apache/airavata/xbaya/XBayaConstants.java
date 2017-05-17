/**
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
 */
package org.apache.airavata.xbaya;

import java.net.URI;

public class XBayaConstants {

    /**
     * XBaya
     */
    public static final String APPLICATION_SHORT_NAME = "XBaya";

    /**
     * The name of the project
     */
    public static final String PROJECT_NAME = "Apache Airavata";
    
    /**
     * The name of the application
     */
    public static final String APPLICATION_NAME = "XBaya Dashboard";

    /**
     * The URL of the web page of the application
     */
    public static final URI WEB_URL = URI.create("http://airavata.apache.org/");

    // Default values

    /**
     * Default URL of the GPEL Engine
     */
    public static final URI DEFAULT_GPEL_ENGINE_URL = URI.create("https://tyr13.cs.indiana.edu:7443/gpel/");

    /**
     * DEFAULT_GFAC_URL
     */
    public static final URI DEFAULT_GFAC_URL = URI.create("http://localhost:8080/axis2/services/GFacService");

    /**
     * DEFAULT_TOPIC
     */
    public static final String DEFAULT_TOPIC = "xbaya-topic";

    /**
     * Default notification broker URL.
     */
    public static final URI DEFAULT_BROKER_URL = URI.create("http://localhost:8080/axis2/services/EventingService");

    /**
     * Default message box URL.
     */
    public static final URI DEFAULT_MESSAGE_BOX_URL = URI.create("http://localhost:8080/axis2/services/MsgBoxService");

    /**
     * DEFAULT_DSC_URL
     */
    public static final URI DEFAULT_DSC_URL = URI.create("https://silktree.cs.indiana.edu:52520/");

    /**
     * DEFAULT_MYPROXY_SERVER
     */
    public static final String DEFAULT_MYPROXY_SERVER = "myproxy.teragrid.org";

    /**
     * DEFAULT_MYPROXY_PORT
     */
    public static final int DEFAULT_MYPROXY_PORT = 7512;

    /**
     * DEFAULT_MYPROXY_LIFTTIME
     */
    public static final int DEFAULT_MYPROXY_LIFTTIME = 3600;

    /**
     * DEFAULT_WEB_REGISTRY
     */
    public static final URI DEFAULT_WEB_REGISTRY = URI.create("http://www.extreme.indiana.edu/xgws/wsdl/");

    // File suffixes

    /**
     * File suffix for XML
     */
    public static final String XML_SUFFIX = ".xml";

    /**
     * File suffix for WSDL
     */
    public static final String WSDL_SUFFIX = ".wsdl";

    /**
     * File suffix for WSDL
     */
    public static final String WSDL_SUFFIX2 = "-wsdl.xml";

    /**
     * Suffix of a graph file
     */
    public static final String GRAPH_FILE_SUFFIX = ".xgr";

    /**
     * Suffix of a workflow file
     */
    public static final String WORKFLOW_FILE_SUFFIX = ".awf";

    /**
     * File suffix for Jython scripts
     */
    public static final String JYTHON_SCRIPT_SUFFIX = ".py";

    /**
     * File suffix for BPEL
     */
    public static final String BPEL_SUFFIX = ".bpel";

    /**
     * File suffix for SCUFL
     */
    public static final String SCUFL_SCRIPT_SUFFIX = ".xml";

    /**
     * File suffix for PNG
     */
    public static final String PNG_SUFFIX = ".png";

    /**
     * Format name for png image
     */
    public static final String PNG_FORMAT_NAME = "PNG";

    /**
     * ODE URL
     */
    public static final String DEFAULT_ODE_URL = "https://pagodatree.cs.indiana.edu:17443";

    /**
     * WorkflowInterpreter URL
     */
    public static final URI DEFAULT_WORKFLOW_INTERPRETER_URL = URI
            .create("http://localhost:8080/axis2/services/WorkflowInterpretor?wsdl");

    /**
     * 
     * PROXY URL
     */

    public static final URI DEFAULT_PROXY_URI = URI
            .create("http://silktree.cs.indiana.edu:18080/axis2/services/WEPSService?wsdl");

    /**
     * WORKFLOW Namespace
     */
    public static final String LEAD_NS = "http://extreme.indiana.edu/lead/workflow";

    /**
     * OGCE WORKFLOW Namespace
     */
    public static final String OGCE_WORKFLOW_NS = "http://workflow.ogce.org/";

    public static final String STREAM_SERVER = "http://pagodatree.cs.indiana.edu:8081/axis2/services/StreamService?wsdl";

    public static final String STATIC_LABEL = "STATIC";
    public static final URI REGISTRY_URL = URI.create("http://localhost:8080/airavata-registry/api");
    public static final String DEFAULT_GATEWAY = "default";
    public static final String REGISTRY_USERNAME = "admin";
    public static final String REGISTRY_PASSPHRASE = "admin";
    public static final String REGISTRY_TYPE_HOST_DESC = "HostDesc";
    public static final String REGISTRY_TYPE_APPLICATION_DESC = "ApplicationDesc";
    public static final String REGISTRY_TYPE_SERVICE_DESC = "ServiceDesc";
    public static final String REGISTRY_TYPE_WORKFLOW = "workflow";
    public static final String HTTP_SCHEMAS_AIRAVATA_APACHE_ORG_GFAC_TYPE = "http://airavata.apache.org/schemas/gfac/2012/12";

    public static final String XBAYA_REGISTRY_USER = "xbaya.registry.user";
    public static final String XBAYA_REGISTRY_URL = "xbaya.registry.url";
    public static final String XBAYA_DEFAULT_GATEWAY = "xbaya.default.gateway";

    /**
     * XRegistry Resource Types for OGCE Resource
     */
    public static enum XR_Resource_Types {
        Project, Experiment, WorkflowTemplate, WorkflowInstance, WorkflowInput, WorkflowOutput
    };

}