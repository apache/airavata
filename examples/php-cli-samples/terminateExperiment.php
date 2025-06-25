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
use Airavata\Model\Workspace\Experiment\ExperimentState;
use Thrift\Exception\TTransportException;
use Thrift\Protocol\TBinaryProtocol;
use Thrift\Transport\TBufferedTransport;
use Thrift\Transport\TSocket;
use Airavata\API\AiravataClient;


if ($argc != 2)
{
    echo 'Please provide a valid Experiment ID.'."\n". 'Usage: php terminateExperiment.php <experiment_ID>'."\n";
}
else
{
    terminate_experiment($argv[1]);

    echo 'If there are no exceptions, assume the Experiment is terminated successfully.';
}


$transport->close();


/**
 * End the experiment with the given ID
 * @param $expId
 */
function terminate_experiment($expId)
{
    global $airavataclient;

    try
    {
        $airavataclient->terminateExperiment($expId);
    }
    catch (InvalidRequestException $ire)
    {
        echo 'Invalid Request Exception!\n\n' . $ire->getMessage();
    }
    catch (ExperimentNotFoundException $enf)
    {
        echo 'Experiment Not Found Exception!\n\n' . $enf->getMessage();
    }
    catch (AiravataClientException $ace)
    {
        echo 'Airavata Client Exception!\n\n' . $ace->getMessage();
    }
    catch (AiravataSystemException $ase)
    {
        echo 'Airavata System Exception!\n\n' . $ase->getMessage();
    }
    catch (TTransportException $tte)
    {
        echo 'TTransport Exception!\n\n' . $tte->getMessage();
    }
    catch (\Exception $e)
    {
        echo 'Exception!\n\n' . $e->getMessage();
    }
}

?>

