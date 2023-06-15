<?php
/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * @license http://www.apache.org/licenses/LICENSE-2.0 Apache V2
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

/**
 * Bundle all thrift and Airavata stubs into a include file. This is simple but not so elegant way.
 *  Contributions welcome to improve writing PHP Client Samples.
 *
 */
include 'getAiravataClient.php';
global $airavataclient;
global $transport;

use Airavata\API\Error\AiravataClientException;
use Airavata\API\Error\AiravataSystemException;
use Airavata\API\Error\InvalidRequestException;
use Airavata\Client\AiravataClientFactory;
use Thrift\Protocol\TBinaryProtocol;
use Thrift\Transport\TBufferedTransport;
use Thrift\Transport\TSocket;
use Airavata\API\AiravataClient;

use Airavata\Model\Workspace\Project;
use Airavata\Model\Workspace\Experiment\Experiment;
use Airavata\Model\Workspace\Experiment\DataObjectType;
use Airavata\Model\Workspace\Experiment\UserConfigurationData;
use Airavata\Model\Workspace\Experiment\ComputationalResourceScheduling;
use Airavata\Model\Workspace\Experiment\DataType;

try {
    if ($argc < 4) {
        echo "Please provide the User, Experiment Name and the Project ID."."\n". "Usage: php createExperiment.php <user_name> <experiment_name> <project_ID> \n"."\n";
    } else {

        /* User provides input values */
        $userName = $argv[1];
        $experimentName = $argv[2];
        $projectId = $argv[3];

        /**
         * Configure Experiment by selecting application and configuring its input values.
         *   This sample scripts executes a simple Echo command on one of the remote machines as a illustrations.
         *   The getAllApplicationInterfaceNames.php scripts will list other available samples applications.
         *   Examples include Amber, AutoDock, ESPRESSO, GROMACS, LAMMPS, NWChem, Trinity and WRF.
         */

        $applicationId = $appcatalogdocs['echoInterfaceId'];

        $applicationInput = new DataObjectType();
        $applicationInput->key = "Input_to_Echo";
        $applicationInput->value = "Hello World";
        $experimentInputs = array($applicationInput);

        /**
         *  NOTE: For convenience, all the computational hosts are provided.
         *        Comment/Uncomment appropriately to schedule on alternative resources
         *        If all hosts are uncommented, the last one will be picked.
         */

        $scheduling = new ComputationalResourceScheduling();

        /* IU BigRed II Cluster */
        $scheduling->resourceHostId = $appcatalogdocs['bigredResourceId'];

        /* TACC Stampede Cluster */
        $scheduling->resourceHostId = $appcatalogdocs['stampedeResourceId'];
        $scheduling->ComputationalProjectAccount = "TG-STA110014S";

        /* SDSC Trestles Cluster */
        $scheduling->resourceHostId = $appcatalogdocs['trestlesResourceId'];
        $scheduling->ComputationalProjectAccount = "sds128";

        /* Job dimensions and resource queue */
        $scheduling->totalCPUCount = 1;
        $scheduling->nodeCount = 1;
        $scheduling->wallTimeLimit = 15;
        $scheduling->queueName = "normal";

        $userConfigurationData = new UserConfigurationData();
        $userConfigurationData->airavataAutoSchedule = 0;
        $userConfigurationData->overrideManualScheduledParams = 0;
        $userConfigurationData->computationalResourceScheduling = $scheduling;

        /**
         * An experiment is created within Airavata and all the provided inputs and configurations are persisted
         *  within the Airavata Registry.
         *
         * NOTE: Airavata uses a 2 step launch process. The creation just creates the experiment. The launch step
         *   executes the created experiment.
         */

        $experiment = new Experiment();
        $experiment->projectID = $projectId;
        $experiment->userName = $userName;
        $experiment->name = $experimentName;
        $experiment->applicationId = $applicationId;
        $experiment->userConfigurationData = $userConfigurationData;
        $experiment->experimentInputs = $experimentInputs;

        $experimentId = $airavataclient->createExperiment($experiment);

        if ($experimentId) {
            var_dump($experiment);
            echo "Experiment $experimentId is successfully created! \n    "."\n";
        } else {
            echo "Failed to create experiment. \n"."\n";
        }
    }

} catch (InvalidRequestException $ire) {
    print 'Invalid Request Exception: ' . $ire->getMessage() . "\n";
} catch (AiravataClientException $ace) {
    print 'Airavata System Exception: ' . $ace->getMessage() . "\n";
} catch (AiravataSystemException $ase) {
    print 'Airavata System Exception: ' . $ase->getMessage() . "\n";
}

$transport->close();

?>
