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

Airavata\Client\Samples\sampleDisabled();

use Airavata\Model\AppCatalog\AppInterface\ApplicationInterfaceDescription;
use Airavata\Model\AppCatalog\AppInterface\InputDataObjectType;
use Airavata\Model\AppCatalog\AppInterface\OutputDataObjectType;
use Airavata\Model\AppCatalog\AppInterface\DataType;

try
{
    if ($argc != 2)
    {
        echo 'php updateApplicationInterface.php <appInterfaceId>';
    }
    else
    {
        $appInterfaceId = $argv[1];
        //$appDescription = $argv[2];
        //$appModuleId = $argv[3];


        $appInput1 = new InputDataObjectType();
        $appInput1->name = "Namelist_File";
        $appInput1->userFriendlyDescription = "Namelist Configuration File";
        $appInput1->type = DataType::URI;

        $appInput2 = new InputDataObjectType();
        $appInput2->name = "WRF_Boundary_File";
        $appInput2->userFriendlyDescription = "Boundary Conditions File";
        $appInput2->type = DataType::URI;

        $appInput3 = new InputDataObjectType();
        $appInput3->name = "WRF_Initial_Condition";
        $appInput3->userFriendlyDescription = "Input Data file";
        $appInput3->type = DataType::URI;

        $appOutput1 = new OutputDataObjectType();
        $appOutput1->name = "WRF_Standard_Out";
        $appOutput1->type = DataType::STRING;

        $appOutput2 = new OutputDataObjectType();
        $appOutput2->name = "WRF_RSL_Out";
        $appOutput2->type = DataType::STRING;

        $appInterface = $airavataclient->getApplicationInterface($appInterfaceId);
        $appInterface->applicationInputs = array($appInput1, $appInput2, $appInput3);
        $appInterface->applicationOutputs = array($appOutput1, $appOutput2);
        var_dump($appInterface);

        $status = $airavataclient->updateApplicationInterface($appInterfaceId, $appInterface);

        if ($status)
        {
            echo "\n Application Interface $appInterfaceId is updated! \n    ";
        }
        else
        {
            echo "\n Failed to update application interface. \n";
        }
    }
}
catch (InvalidRequestException $ire)
{
    print 'InvalidRequestException: ' . $ire->getMessage()."\n";
}
catch (AiravataClientException $ace)
{
    print 'Airavata System Exception: ' . $ace->getMessage()."\n";
}
catch (AiravataSystemException $ase)
{
    print 'Airavata System Exception: ' . $ase->getMessage()."\n";
}
catch (\Exception $e)
{
    echo 'Exception!<br><br>' . $e->getMessage();
}

$transport->close();

?>

