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
use Airavata\Model\Workspace\Experiment\UserConfigurationData;
use Airavata\Model\Workspace\Experiment\ComputationalResourceScheduling;
use Airavata\Model\AppCatalog\AppInterface\InputDataObjectType;

try {
    /* User provides input values */
    $gatewayId = 'ultrascan';
    $userName = 'us3';
    $projectId = 'ultrascan_cd0900d4-2b4d-4919-9aa2-b7649ea1f391';
    $applicationId = 'Ultrascan_856df1d5-944a-49d3-a476-d969e57a8f37';
    $credStoreToken = '00409bfe-8e5f-4e50-b8eb-138bf0158e90';

//  $hostId = 'stampede.tacc.xsede.org_28c4bf70-ed52-4f87-b481-31a64a1f5808';
//  $hostId = 'lonestar.tacc.utexas.edu_6d62fa0c-a9b1-4414-a76a-a4e2cbd9d290';
    $hostId = 'alamo.uthscsa.edu_a591c220-345b-4f67-9337-901b76360df6';
    $queueName = 'batch';
//  $hostId = 'comet-ln1.sdsc.edu_0bb9bd78-b5e7-40cf-a5dd-fd6f8bd6b537';
//    $queueName = 'compute';
//  $hostId = 'gordon.sdsc.edu_9ee43a5a-cee7-4efd-996b-4fc11662a726';

    $scheduling = new ComputationalResourceScheduling();
    $scheduling->resourceHostId = $hostId;
    $scheduling->queueName = $queueName;
    $scheduling->totalCPUCount = 1;
    $scheduling->nodeCount = 1;
    $scheduling->wallTimeLimit = 15;

    $userConfigurationData = new UserConfigurationData();
    $userConfigurationData->airavataAutoSchedule = 0;
    $userConfigurationData->overrideManualScheduledParams = 0;
    $userConfigurationData->computationalResourceScheduling = $scheduling;

    for ($expCount = 1; $expCount <= 2; $expCount++) {

        $experimentName = "Experiment$expCount";

        $applicationInputs = $airavataclient->getApplicationInputs($applicationId);
        foreach ($applicationInputs as $applicationInput){
            if($applicationInput->name =='input'){
                $applicationInput->value = "test_dummy_app_input$expCount";
            } else if($applicationInput->name =='walltime'){
                $applicationInput->value = "-walltime=" . 10;
            } else if($applicationInput->name =='mgroupcount'){
                $applicationInput->value = "-mgroupcount=" . 1;
            }
        }
        $experimentInputs = $applicationInputs;

        $experiment = new Experiment();
        $experiment->projectID = $projectId;
        $experiment->userName = $userName;
        $experiment->name = $experimentName;
        $experiment->applicationId = $applicationId;
        $experiment->userConfigurationData = $userConfigurationData;
        $experiment->experimentInputs = $experimentInputs;

        $experimentId = $airavataclient->createExperiment($gatewayId, $experiment);

        if ($experimentId) {
            //var_dump($experiment);
            echo "Experiment$expCount is successfully created with id  $experimentId    "."\n";
        } else {
            echo "Failed to create experiment. \n"."\n";
        }

        $airavataclient->launchExperiment($experimentId, $credStoreToken);
        echo "Launched Experiment$expCount Successfully \n";
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
