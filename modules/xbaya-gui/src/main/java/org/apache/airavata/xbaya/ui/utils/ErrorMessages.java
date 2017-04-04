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
package org.apache.airavata.xbaya.ui.utils;

public interface ErrorMessages {

    /**
     * URL_EMPTY
     */
    public static final String URL_EMPTY = "URL cannot be empty.";

    /**
     * URL_WRONG
     */
    public static final String URL_WRONG = "URL is in wrong format.";

    /**
     * GPEL_USERNAME_EMPTY
     */
    public static final String USERNAME_EMPTY = "Username cannot be empty.";

    /**
     * GPEL_PASSPHRASE_EMPTY
     */
    public static final String PASSWORD_EMPTY = "Password cannot be empty.";

    /**
     * Error message when it fails to open the file.
     */
    public static final String OPEN_FILE_ERROR = "Failed to open the file.";

    /**
     * Error message when it fails to write the file.
     */
    public static final String WRITE_FILE_ERROR = "Failed to write the file.";

    /**
     * Error message when it fails to load the graph
     */
    public static final String GRAPH_LOAD_ERROR = "Failed to load the workflow.";

    /**
     * Error message when it fails to save the graph
     */
    public static final String GRAPH_SAVE_ERROR = "Failed to save the workflow.";

    /**
     * Error message when the format of a workflow (graph) is wrong.
     */
    public static final String GRAPH_FORMAT_ERROR = "The workflow file is in wrong format.";

    /**
     * Errro message indicating the workflow is not complete to create a workflow script.
     */
    public static final String GRAPH_NOT_READY_ERROR = "The workflow is not compelete.";

    /**
     * Error message when it fails to load a component list.
     */
    public static final String COMPONENT_LIST_LOAD_ERROR = "Failed to load a component list.";

    /**
     * Error message when it fails to load a component.
     */
    public static final String COMPONENT_LOAD_ERROR = "Failed to load the component.";

    /**
     * Error message when the format of a component is wrong.
     */
    public static final String COMPONENT_FORMAT_ERROR = "The component is in wrong format.";

    /**
     * GPEL_ERROR
     */
    public static final String GPEL_ERROR = "Error occured while communicating with the GPEL Engine.";

    /**
     * GPEL_CONNECTION_ERROR
     */
    public static final String GPEL_CONNECTION_ERROR = "Failed to connect to the GPEL Engine.";

    /**
     * GPEL_URL_EMPTY
     */
    public static final String GPEL_URL_EMPTY = "GPEL Engine URL cannot be empty.";

    /**
     * Error if the format of the URL of the GPEL Engine is wrong.
     */
    public static final String GPEL_WRONG_URL = "The URL of the GPEL Engine is in a wrong format.";

    /**
     * Error while loading a list of workflows from GPEL Engine.
     */
    public static final String GPEL_WORKFLOW_LIST_LOAD_ERROR = "Failed to get list of workflows from GPEL Engine.";

    /**
     * GPEL_WORKFLOW_NOT_FOUND_ERROR
     */
    public static final String GPEL_WORKFLOW_NOT_FOUND_ERROR = "The specified workflow doesn't exist in the GPEL Engine.";

    /**
     * GPEL_MAX_EMPTY
     */
    public static final String GPEL_MAX_EMPTY = "Maximum cannot be empty.";

    /**
     * GPEL_MAX_WRONG
     */
    public static final String GPEL_MAX_WRONG = "Maximum needs to be an integer.";

    /**
     * WORKFLOW_WSDL_ERROR
     */
    public static final String WORKFLOW_WSDL_NOT_EXIST = "You need to generate BPEL script first.";

    /**
     * WORKFLOW_WSDL_ERROR
     */
    public static final String WORKFLOW_WSDL_ERROR = "The workflow WSDL is in wrong format.";

    /**
     * Error while subscribing to notification.
     */
    public static final String MONITOR_SUBSCRIPTION_ERROR = "Failed to subscribe to notification.";

    /**
     * Some other error related to monitoring.
     */
    public static final String MONITOR_ERROR = "Monitor error.";

    /**
     * KARMA_CONNECTION_ERROR
     */
    public static final String KARMA_CONNECTION_ERROR = "Failed to connect to the Karma service.";

    /**
     * TOPIC_EMPTY_ERROR
     */
    public static final String TOPIC_EMPTY_ERROR = "Topic cannot be empty.";

    /**
     * BROKER_URL_NOT_SET_ERROR
     */
    public static final String BROKER_URL_NOT_SET_ERROR = "Broker URL is not set.";

    /**
     * Error while loading a list of workflows from Registry.
     */
    public static final String REGISTRY_WORKFLOW_LIST_LOAD_ERROR = "Failed to get list of workflows from Registry.";

    /**
     * GFAC_URL_WRONG
     */
    public static final String GFAC_URL_WRONG = "GFac URL is in wrong format.";

    /**
     * DSC_URL_WRONG
     */
    public static final String DSC_URL_WRONG = "DSC URL is in wrong format.";

    /**
     * MYPROXY_LOAD_ERROR
     */
    public static final String MYPROXY_LOAD_ERROR = "Failed to load a proxy";

    /**
     * MYPROXY_HOST_EMPTY
     */
    public static final String MYPROXY_HOST_EMPTY = "MyProxy server cannot be empty.";

    /**
     * MYPROXY_PORT_EMPTY
     */
    public static final String MYPROXY_PORT_EMPTY = "MyProxy server port cannot be empty.";

    /**
     * MYPROXY_PORT_WRONG
     */
    public static final String MYPROXY_PORT_WRONG = "MyProxy server port needs to be an integer.";

    /**
     * MYPROXY_LIFETIME_EMPTY
     */
    public static final String MYPROXY_LIFETIME_EMPTY = "Lifetime cannot be empty.";

    /**
     * MYPROXY_LIFETIME_WRONG
     */
    public static final String MYPROXY_LIFETIME_WRONG = "Lifetime needs to be an integer.";

    /**
     * MYPROXY_PASSPHRASE_WRONG
     */
    public static final String MYPROXY_PASSPHRASE_WRONG = "Passphrase must be at least 6 characters long";

    /**
     * Error message for unexpected errors. When you see this, fix it!!
     */
    public static final String UNEXPECTED_ERROR = "Unexpected error.";

    /**
     * Warning message for no component selected.
     */
    public static final String NO_COMPONENT_SELECTED_WARNING = "You need to select a component to add from the Component Selector.";

    /**
     * Warning message when a user tries to set a default value before a parameter node is connected.
     */
    public static final String INPUT_NOT_CONNECTED_WARNING = "You need to connect the input to an input of a service first to configure.";

    /**
     * Warning message when a user tries to set a value before a parameter node is connected.
     */
    public static final String CONSTANT_NOT_CONNECTED_WARNING = "You need to connect the constant to an input of a service first to configure.";

    String CREDENTIALS_WRONG = "Please check credentials";
    String GIVEN_WORKFLOW_NAME_IS_WRONG = "Given Workflow Name is Wrong ";
    String REPOSITORY_CONFIGURATION_IS_WRONG_FAILED_TO_LOAD_THE_WORKFLOW = "Repository Configuration is Wrong, Failed to load the Workflow";
    String WORKFLOW_IS_WRONG = "Workflow is Wrong";
}