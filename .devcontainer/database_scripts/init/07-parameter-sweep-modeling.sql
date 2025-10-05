USE `app_catalog`;

-- Parent collection for one parameter sweep / batch
CREATE TABLE IF NOT EXISTS `JOB_BATCH`
(
    `ID`               VARCHAR(255) NOT NULL,
    `EXPERIMENT_ID`    VARCHAR(255) NOT NULL,
    `CREATED_AT`       TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `PAYLOAD_JSON`     JSON         NULL,     -- original request payload
    `COMMAND_TEMPLATE` TEXT         NOT NULL, -- application_command (template)
    PRIMARY KEY (`ID`),
    KEY `IDX_BATCH_EXPERIMENT` (`EXPERIMENT_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Units of work (one per parameter combination)
CREATE TABLE IF NOT EXISTS `JOB_UNIT`
(
    `ID`               VARCHAR(255)                                        NOT NULL,
    `BATCH_ID`         VARCHAR(255)                                        NOT NULL,
    `EXPERIMENT_ID`    VARCHAR(255)                                        NOT NULL,
    `CREATED_AT`       TIMESTAMP(6)                                        NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `RESOLVED_COMMAND` TEXT                                                NOT NULL, -- fully expanded command to run
    `STATUS`           ENUM('PENDING','IN_PROGRESS','COMPLETED','FAILED')  NOT NULL DEFAULT 'PENDING',
    `AGENT_ID`         VARCHAR(255)                                        NULL,
    `STARTED_AT`       TIMESTAMP(6)                                        NULL,
    `COMPLETED_AT`     TIMESTAMP(6)                                        NULL,
    PRIMARY KEY (`ID`),
    KEY `IDX_UNIT_BATCH_STATUS_FIFO` (`BATCH_ID`, `STATUS`, `CREATED_AT`, `ID`),
    KEY `IDX_UNIT_EXP_STATUS` (`EXPERIMENT_ID`, `STATUS`),
    CONSTRAINT `FK_JOB_UNIT_BATCH` FOREIGN KEY (`BATCH_ID`)
        REFERENCES `JOB_BATCH` (`ID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;