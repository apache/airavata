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

package org.apache.airavata.testsuite.multitenantedairavata.utils;

public class TestFrameworkConstants {
    public static final String AIRAVATA_CLIENT_PROPERTIES = "airavata-client.properties";
    public static final String TEST_FREAMEWORK_JSON = "test-framework.json";

    public static final class AiravataClientConstants {
        public static final String THRIFT_SERVER_HOST = "thrift.server.host";
        public static final String THRIFT_SERVER_PORT = "thrift.server.port";
        public static final String CS_JBDC_URL = "credential.store.jdbc.url";
        public static final String CS_JBDC_DRIVER = "credential.store.jdbc.driver";
        public static final String CS_DB_USERNAME = "credential.store.jdbc.user";
        public static final String CS_DB_PWD = "credential.store.jdbc.password";
        public static final String RABBIT_BROKER_URL = "rabbitmq.broker.url";
        public static final String RABBIT_EXCHANGE_NAME = "rabbitmq.exchange.name";
    }

    public static final class FrameworkPropertiesConstants {
        public static final String NUMBER_OF_GATEWAYS = "numberof.gateways";
        public static final String USERS_PER_GATEWAY = "users.per.gateway";
        public static final String COMPUTE_RESOURCE_LIST = "compute.resoure.list";
        public static final String LOGIN_USERNAME_LIST = "login.usernames";
        public static final String APPLICATION_LIST = "application.list";
        public static final String SSH_PUBKEY_LOCATION = "ssh.pub.key.location";
        public static final String SSH_PRIKEY_LOCATION = "ssh.private.key.location";
        public static final String SSH_PWD = "ssh.password";
        public static final String SSH_USERNAME = "ssh.username";
        public static final String TOKEN_WRITE_LOCATION = "token.file.location";
        public static final String RESULT_WRITE_LOCATION = "result.file.location";
        public static final String GATEWAYS_TOSKIP = "gateways.toskip";
        public static final String TEST_USER = "test.user";
        public static final String TEST_PROJECT = "test.project";
    }

    public static final class ErrorTypeConstants {
        public static final String BADINPUTS = "badinputs";
        public static final String ERROR_CONFIG = "error.configuration";
        public static final String ALLOCATION_PROJECT = "projectId";
        public static final String QUEUE_NAME = "queueName";
        public static final String WALLTIME = "walltime";
        public static final String HOST_NAME = "hostName";

    }

    public static final class GatewayConstants {
        public static final String GENERIC_GATEWAY_NAME = "generic.gateway.name";
        public static final String GENERIC_GATEWAY_DOMAIN = "generic.gateway.domain";
    }

    public static final class AppcatalogConstants {
        public static final String AMBER_APP_NAME = "Amber_Sander";
        public static final String ECHO_NAME = "Echo";
        public static final String ULTRASCAN = "Ultrascan";
        public static final String ESPRESSO_NAME = "ESPRESSO";
        public static final String GROMACS_NAME = "GROMACS";
        public static final String LAMMPS_NAME = "LAMMPS";
        public static final String NWCHEM_NAME = "NWChem";
        public static final String TRINITY_NAME = "Trinity";
        public static final String WRF_NAME = "WRF";
        public static final String PHASTA_NAME = "PHASTA";
        public static final String TINKER_MONTE_NAME = "TinkerMonte";
        public static final String GAUSSIAN_NAME = "Gaussian";
        public static final String GAMESS_NAME = "Gamess";

        public static final String ECHO_DESCRIPTION = "A Simple Echo Application";
        public static final String AMBER_DESCRIPTION = "Assisted Model Building with Energy Refinement MD Package";
        public static final String ULTRASCAN_DESCRIPTION = "Ultrascan application";
        public static final String ESPRESSO_DESCRIPTION = "Nanoscale electronic-structure calculations and materials modeling";
        public static final String GROMACS_DESCRIPTION = "GROMACS Molecular Dynamics Package";
        public static final String LAMMPS_DESCRIPTION = "Large-scale Atomic/Molecular Massively Parallel Simulator";
        public static final String NWCHEM_DESCRIPTION = "Ab initio computational chemistry software package";
        public static final String TRINITY_DESCRPTION = "de novo reconstruction of transcriptomes from RNA-seq data";
        public static final String WRF_DESCRIPTION = "Weather Research and Forecasting";
        public static final String PHASTA_DESCRIPTION = "Computational fluid dynamics solver";
        public static final String TINKER_MONTE_DESCRIPTION = "Grid Chem Tinker Monte Application";
        public static final String GAUSSIAN_DESCRIPTION = "Grid Chem Gaussian Application";
        public static final String GAMESS_DESCRIPTION = "A Gamess Application";

        public static final String STAMPEDE_RESOURCE_NAME = "stampede.tacc.xsede.org";
        public static final String TRESTLES_RESOURCE_NAME = "trestles.sdsc.xsede.org";
        public static final String BR2_RESOURCE_NAME = "bigred2.uits.iu.edu";
        public static final String GORDEN_RESOURCE_NAME = "gordon.sdsc.edu";
        public static final String ALAMO_RESOURCE_NAME = "alamo.uthscsa.edu";
        public static final String COMET_RESOURCE_NAME = "comet.sdsc.edu";
        public static final String LONESTAR_RESOURCE_NAME = "lonestar.tacc.utexas.edu";


        public static final String AMBER_HEAT_RST_LOCATION = "02_Heat.rst_location";
        public static final String AMBER_PROD_IN_LOCATION = "03_Prod.in_location";
        public static final String AMBER_PRMTOP_LOCATION = "prmtop_location";
    }

    public static final class CredentialStoreConstants {
        public static final String TOKEN_FILE_NAME = "testFrameworkTokens";

    }


}
