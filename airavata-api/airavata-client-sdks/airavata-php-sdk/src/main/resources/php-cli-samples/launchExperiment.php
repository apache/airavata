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
use Airavata\API\Error\ExperimentNotFoundException;
use Airavata\API\Error\InvalidRequestException;
use Airavata\Client\AiravataClientFactory;
use Thrift\Protocol\TBinaryProtocol;
use Thrift\Transport\TBufferedTransport;
use Thrift\Transport\TSocket;
use Airavata\API\AiravataClient;

use Airavata\Model\Workspace\Project;
use Airavata\Model\Workspace\Experiment\Experiment;

try {
    if (count($argv) < 2) {
        exit("Please provide the Experiment ID to be launched."."\n".  "Usage: php launchExperiment.php <experiment_ID> \n"."\n");
    } else {
        $experimentId = $argv[1];
        $airavataclient->launchExperiment($experimentId, $airavataconfig['AIRAVATA_CREDENTIAL_STORE_TOKEN']);
        echo "Experiment $experimentId is Successfully launched! \n";
    }
} catch (InvalidRequestException $ire) {
    print 'Invalid Request Exception: ' ."\n" . $ire->getMessage() . "\n";
} catch (AiravataClientException $ace) {
    print 'Airavata System Exception: '."\n"  . $ace->getMessage() . "\n";
} catch (AiravataSystemException $ase) {
    print 'Airavata System Exception: '."\n"  . $ase->getMessage() . "\n";
} catch (ExperimentNotFoundException $enf) {
    print 'Experiment Not Found Exception: '."\n" . $enf->getMessage() . "\n";
} catch (LaunchValidationException $lve) {
    print 'Experiment Validation Failed: '."\n"  . $lve->getMessage() . "\n";
}

$transport->close();

?>
