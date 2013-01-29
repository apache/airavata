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
    public static final String XPATH_EXPR_MYPROXY_SERVER = "/GFac/MyProxy/Server/text()";
    public static final String XPATH_EXPR_MYPROXY_USER = "/GFac/MyProxy/User/text()";
    public static final String XPATH_EXPR_MYPROXY_PASSPHRASE = "/GFac/MyProxy/Passphrase/text()";
    public static final String XPATH_EXPR_MYPROXY_LIFECYCLE = "/GFac/MyProxy/LifeCycle/text()";
    public static final String XPATH_EXPR_INFLOW_HANDLERS = "/GFac/Handlers/InFlow/Handler";
    public static final String XPATH_EXPR_OUTFLOW_HANDLERS = "/GFac/Handlers/OutFlow/Handler";

    public static final String GFAC_CONFIG_HANDLER_CLASS_ATTRIBUTE = "class";
    public static final String NEWLINE = System.getProperty("line.separator");
    public static final String INPUT_DATA_DIR_VAR_NAME = "inputData";
    public static final String OUTPUT_DATA_DIR_VAR_NAME = "outputData";
    public static final int DEFAULT_GSI_FTP_PORT = 2811;
    public static final String _127_0_0_1 = "127.0.0.1";
    public static final String LOCALHOST = "localhost";

    public static final String PROP_WORKFLOW_INSTANCE_ID = "workflow.instance.id";
    public static final String PROP_WORKFLOW_NODE_ID = "workflow.node.id";
    public static final String PROP_BROKER_URL = "broker.url";
    public static final String PROP_TOPIC = "topic";}
