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

package org.apache.airavata.gfac;

public class Constants {
	public static final String XPATH_EXPR_GLOBAL_INFLOW_HANDLERS = "/GFac/GlobalHandlers/InHandlers/Handler";
	public static final String XPATH_EXPR_GLOBAL_OUTFLOW_HANDLERS = "/GFac/GlobalHandlers/OutHandlers/Handler";

	public static final String XPATH_EXPR_APPLICATION_HANDLERS_START = "/GFac/Application[@name='";
	public static final String XPATH_EXPR_APPLICATION_INFLOW_HANDLERS_END = "']/InHandlers/Handler";
	public static final String XPATH_EXPR_APPLICATION_OUTFLOW_HANDLERS_END = "']/OutHandlers/Handler";
    public static final String XPATH_EXPR_APPLICATION_PROVIDER = "']/OutHandlers/Handler";


	public static final String XPATH_EXPR_PROVIDER_HANDLERS_START = "/GFac/Provider[@class='";
	public static final String XPATH_EXPR_PROVIDER_INFLOW_HANDLERS_END = "']/InHandlers/Handler";
	public static final String XPATH_EXPR_PROVIDER_OUTFLOW_HANDLERS_END = "']/OutHandlers/Handler";

	public static final String GFAC_CONFIG_HANDLER_CLASS_ATTRIBUTE = "class";
	public static final String GFAC_CONFIG_APPLICATION_NAME_ATTRIBUTE = "class";
	public static final String NEWLINE = System.getProperty("line.separator");
	public static final String INPUT_DATA_DIR_VAR_NAME = "inputData";
	public static final String OUTPUT_DATA_DIR_VAR_NAME = "outputData";
	public static final int DEFAULT_GSI_FTP_PORT = 2811;
	public static final String _127_0_0_1 = "127.0.0.1";
	public static final String LOCALHOST = "localhost";

	public static final String PROP_WORKFLOW_INSTANCE_ID = "workflow.instance.id";
	public static final String PROP_WORKFLOW_NODE_ID = "workflow.node.id";
	public static final String PROP_BROKER_URL = "broker.url";
	public static final String PROP_TOPIC = "topic";
	public static final String SPACE = " ";
	public static final int COMMAND_EXECUTION_TIMEOUT = 5;
	public static final String EXECUTABLE_NAME = "run.sh";

	public static final String TRUSTED_CERT_LOCATION = "trusted.cert.location";
    public static final String MYPROXY_SERVER = "myproxy.server";
    public static final String MYPROXY_USER = "myproxy.user";
    public static final String MYPROXY_PASS = "myproxy.pass";
    public static final String MYPROXY_LIFE = "myproxy.life";
    /*
     * SSH properties
     */
    public static final String SSH_PRIVATE_KEY = "ssh.key";
    public static final String SSH_PRIVATE_KEY_PASS = "ssh.keypass";
    public static final String SSH_USER_NAME = "ssh.username";
}
