package org.apache.airavata.helix.impl.task.submission;

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
