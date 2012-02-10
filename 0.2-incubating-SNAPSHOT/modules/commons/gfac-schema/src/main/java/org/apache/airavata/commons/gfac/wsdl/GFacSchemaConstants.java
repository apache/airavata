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

package org.apache.airavata.commons.gfac.wsdl;

public class GFacSchemaConstants {

    public static final String SHUTDOWN = "Shutdown";

    public static final String PING = "Ping";

    public static final String KILL = "Kill";

    public static final String ARRAY_VALUE = "value";

    public static final String _127_0_0_1 = "127.0.0.1";

    public static final String GFAC_NAMESPACE = "http://schemas.airavata.apache.org/gfac/type";

    public static final String TRANSPORT_LEVEL = "TransportLevel";

    public static final String MESSAGE_SIGNATURE = "MessageSignature";

    // Specific to application service
    public static final String SERVICE_URI = "SERVICE_URI";

    public static final String METHOD_NAME = "METHOD_NAME";

    public static final String SERVICE_NAME = "SERVICE_NAME";

    public static final String APP_SERVICE_STRING_PARAM = "AFAC_STRING_PARAM";

    public static final String SERVICE_RESP_MSG_SUFFIX = "_ResponseMessage";

    public static final String SERVICE_INPUT_PARAMS_TYPE_SUFFIX = "_InputParamsType";

    public static final String SERVICE_OUTPUT_PARAMS_TYPE_SUFFIX = "_OutputParamsType";

    public static final String SERVICE_REQ_MSG_SUFFIX = "_RequestMessage";

    public static final String SERVICE_IN_PARAMS_SUFFIX = "_InputParams";

    public static final String SERVICE_OUT_PARAMS_SUFFIX = "_OutputParams";

    public static final String SERVICE_TMP_DIR = "AFAC_TMP_DIR";

    public static final String SERVICE_INPUT_MESSAGE_NAME = "AFAC_INPUT_MESSAGE_NAME";

    public static final String HOST = "host";

    public static final String UTF8 = "UTF-8";

    public static final String LOCALHOST = "localhost";

    public static interface InbuitOperations {
        public static final String OP_KILL = "Kill";

        public static final String OP_PING = "Ping";

        public static final String OP_SHUTDOWN = "Shutdown";
    }

    public static class Types {
        public static final String TYPE_STRING = "String";

        public static final String TYPE_INT = "Integer";

        public static final String TYPE_FLOAT = "Float";

        public static final String TYPE_DOUBLE = "Double";

        public static final String TYPE_BOOLEAN = "Boolean";

        public static final String TYPE_QNAME = "QName";

        public static final String TYPE_URI = "URI";

        public static final String TYPE_STRING_ARRAY = "StringArray";

        public static final String TYPE_INT_ARRAY = "IntegerArray";

        public static final String TYPE_FLAOT_ARRAY = "FloatArray";

        public static final String TYPE_DOUBLE_ARRAY = "DoubleArray";

        public static final String TYPE_BOOLEAN_ARRAY = "BooleanArray";

        public static final String TYPE_QNAME_ARRAY = "QNameArray";

        public static final String TYPE_URI_ARRAY = "URIArray";

        public static final String TYPE_STDOUT = "StdOut";

        public static final String TYPE_STDERRORs = "StdErr";

        public static final String TYPE_DATAID = "DataID";

        public static final String TYPE_DATAID_ARRAY = "DataIDArray";
    }

}