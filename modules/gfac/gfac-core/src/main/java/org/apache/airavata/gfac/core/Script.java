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
package org.apache.airavata.gfac.core;/*
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

public enum Script {

    SHELL_NAME("shellName"),
    QUEUE_NAME("queueName"),
    NODES("nodes"),
    CPU_COUNT("cpuCount"),
    MAIL_ADDRESS("mailAddress"),
    ACCOUNT_STRING("accountString"),
    MAX_WALL_TIME("maxWallTime"),
    JOB_NAME("jobName"),
    STANDARD_OUT_FILE("standardOutFile"),
    STANDARD_ERROR_FILE("standardErrorFile"),
    QUALITY_OF_SERVICE("qualityOfService"),
    RESERVATION("reservation"),
    EXPORTS("exports"),
    MODULE_COMMANDS("moduleCommands"),
    SCRATCH_LOCATION("scratchLocation"),
    WORKING_DIR("workingDirectory"),
    PRE_JOB_COMMANDS("preJobCommands"),
    JOB_SUBMITTER_COMMAND("jobSubmitterCommand"),
    EXECUTABLE_PATH("executablePath"),
    INPUTS("inputs"),
    INPUTS_ALL("inputsAll"),
    POST_JOB_COMMANDS("postJobCommands"),
    USED_MEM("usedMem"),
    PROCESS_PER_NODE("processPerNode"),
    CHASSIS_NAME("chassisName"),
    INPUT_DIR("inputDir"),
    OUTPUT_DIR("outputDir"),
    USER_NAME("userName"),
    GATEWAY_ID("gatewayId"),
    GATEWAY_USER_NAME("gatewayUserName"),
    APPLICATION_NAME("applicationName"),
    QUEUE_SPECIFIC_MACROS("queueSpecificMacros")
    ;

    String name;
    Script(String name) {
        this.name = name;
    }
}
