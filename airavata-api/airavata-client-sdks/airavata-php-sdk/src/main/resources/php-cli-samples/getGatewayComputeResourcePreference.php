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
use Thrift\Exception\TTransportException;
use Airavata\Model\AppCatalog\AppInterface\DataType;

try
{

    if ($argc < 3)
    {
        echo 'Please provide valid Gateway ID and Compute Resoruce ID.'."\n". "php getGatewayComputeResourcePreference.php <gateway_ID> <compute_resource_ID>"."\n";
    }
    else {

        $gatewayId = $argv[1];
        $computeResourceId = $argv[2];

        $computeResourcePreferences = $airavataclient->getGatewayComputeResourcePreference($gatewayId, $computeResourceId);

        if ($computeResourcePreferences) {
                var_dump($computeResourcePreferences);
        } else {
            echo "Failed to fetch Compute Resource preference of the Gateway. \n";
        }
}
}
catch (InvalidRequestException $ire)
{
    print 'Invalid Request Exception: '."\n" . $ire->getMessage()."\n";
}
catch (AiravataClientException $ace)
{
    print 'Airavata System Exception: '."\n"  . $ace->getMessage()."\n";
}
catch (AiravataSystemException $ase)
{
    print 'Airavata System Exception: '."\n"  . $ase->getMessage()."\n";
}
catch (TTransportException $tte)
{
    echo 'TTransport Exception!'."\n"  . $tte->getMessage();
}
catch (\Exception $e)
{
    echo 'Exception!'."\n"  . $e->getMessage();
}

$transport->close();

?>

