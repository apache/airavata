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
package org.apache.airavata.gfac.core;

public class GFacConstants {
	public static final String XPATH_EXPR_GLOBAL_INFLOW_HANDLERS = "/GFac/GlobalHandlers/InHandlers/Handler";
	public static final String XPATH_EXPR_GLOBAL_OUTFLOW_HANDLERS = "/GFac/GlobalHandlers/OutHandlers/Handler";
    public static final String XPATH_EXPR_DAEMON_HANDLERS = "/GFac/DaemonHandlers/Handler";

	public static final String XPATH_EXPR_APPLICATION_HANDLERS_START = "/GFac/Application[@name='";
	public static final String XPATH_EXPR_APPLICATION_INFLOW_HANDLERS_END = "']/InHandlers/Handler";
	public static final String XPATH_EXPR_APPLICATION_OUTFLOW_HANDLERS_END = "']/OutHandlers/Handler";
    public static final String XPATH_EXPR_APPLICATION_PROVIDER = "']/OutHandlers/Handler";


	public static final String XPATH_EXPR_PROVIDER_HANDLERS_START = "/GFac/Provider[@class='";
    public static final String XPATH_EXPR_PROVIDER_ON_HOST = "/GFac/Provider[@host='";
    public static final String XPATH_EXPR_PROVIDER_ON_SUBMISSION = "/GFac/Provider[@submission='";
	public static final String XPATH_EXPR_PROVIDER_INFLOW_HANDLERS_END = "']/InHandlers/Handler";
	public static final String XPATH_EXPR_PROVIDER_OUTFLOW_HANDLERS_END = "']/OutHandlers/Handler";

	public static final String GFAC_CONFIG_CLASS_ATTRIBUTE = "class";
	public static final String GFAC_CONFIG_SECURITY_ATTRIBUTE = "security";
	public static final String GFAC_CONFIG_SUBMISSION_ATTRIBUTE = "submission";
    public static final String GFAC_CONFIG_EXECUTION_MODE_ATTRIBUTE = "executionMode";
	public static final String GFAC_CONFIG_APPLICATION_NAME_ATTRIBUTE = "class";
	public static final String NEWLINE = System.getProperty("line.separator");
	public static final String INPUT_DATA_DIR_VAR_NAME = "input";
	public static final String OUTPUT_DATA_DIR_VAR_NAME = "output";
	public static final int DEFAULT_GSI_FTP_PORT = 2811;
	public static final String _127_0_0_1 = "127.0.0.1";
	public static final String LOCALHOST = "localhost";

	public static final String MULTIPLE_INPUTS_SPLITTER = ",";

	public static final String PROP_WORKFLOW_INSTANCE_ID = "workflow.instance.id";
	public static final String PROP_WORKFLOW_NODE_ID = "workflow.node.id";
	public static final String PROP_BROKER_URL = "broker.url";
	public static final String PROP_TOPIC = "topic";
	public static final String SPACE = " ";
	public static final int COMMAND_EXECUTION_TIMEOUT = 5;
	public static final String EXECUTABLE_NAME = "run.sh";

	public static final String TRUSTED_CERT_LOCATION = "trusted.cert.location";
    public static final String TRUSTED_CERTIFICATE_SYSTEM_PROPERTY = "X509_CERT_DIR";
    public static final String MYPROXY_SERVER = "myproxy.server";
    public static final String MYPROXY_SERVER_PORT = "myproxy.port";
    public static final String MYPROXY_USER = "myproxy.username";
    public static final String MYPROXY_PASS = "myproxy.password";
    public static final String MYPROXY_LIFE = "myproxy.life";
    /*
     * SSH properties
     */
    public static final String SSH_PRIVATE_KEY = "private.ssh.key";
    public static final String SSH_PUBLIC_KEY = "public.ssh.key";
    public static final String SSH_PRIVATE_KEY_PASS = "ssh.keypass";
    public static final String SSH_USER_NAME = "ssh.username";
    public static final String SSH_PASSWORD = "ssh.password";
    public static final String PROPERTY = "property";
    public static final String NAME = "name";
    public static final String VALUE = "value";
    public static final String OUTPUT_DATA_DIR = "output.location";

    public static final String EXPERIMENT_ERROR = "EXPERIMENT_ERROR";
	public static final String PROCESS_ERROR = "PROCESS_ERROR";
	public static final String TASK_ERROR = "TASK_ERROR";
	public static final String EXPERIMENT_OUTPUT = "EXPERIMENT_OUTPUT";
	public static final String PROCESS_OUTPUT = "PROCESS_OUTPUT";

	public static final String TASK_ID = "taskId";
	public static final String PROCESS_ID = "processId";

}
