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
package org.apache.airavata.xbaya.file;

import java.io.File;

public interface XBayaPathConstants {

    /**
     * Root directory of the server.
     */
    public static final String XBAYA_DIRECTORY = "modules" + File.separator + "xbaya-gui";

    /**
     * The path of the directory that stores component definitions.
     */
    public static final String WSDL_DIRECTORY = XBAYA_DIRECTORY + File.separator + "src" + File.separator + "main"
            + File.separator + "resources" + File.separator + "wsdls";

    /**
     * The path of the directory that stores graphs.
     */
    public static final String WORKFLOW_DIRECTORY = XBAYA_DIRECTORY + File.separator + "workflows";

    /**
     * The path of the directory where the scripts are saved.
     */
    public static final String SCRIPT_DIRECTORY = XBAYA_DIRECTORY + File.separator + "scripts";

    /**
     * The path of the directory where the BPEL scripts are saved.
     */
    public static final String JYTHON_SCRIPT_DIRECTORY = SCRIPT_DIRECTORY + File.separator + "jython";

    /**
     * The path of the directory where the BPEL scripts are saved.
     */
    public static final String BPEL_SCRIPT_DIRECTORY = SCRIPT_DIRECTORY + File.separator + "bpel";

    /**
     * The path of the directory where the scufl scripts are saved.
     */
    public static final String SCUFL_SCRIPT_DIRECTORY = SCRIPT_DIRECTORY + File.separator + "scufl";

}