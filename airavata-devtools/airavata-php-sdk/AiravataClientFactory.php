<!--Licensed to the Apache Software Foundation (ASF) under one-->
<!--or more contributor license agreements. See the NOTICE file-->
<!-- distributed with this work for additional information-->
<!-- regarding copyright ownership. The ASF licenses this file-->
<!-- to you under the Apache License, Version 2.0 (the-->
<!-- "License"); you may not use this file except in compliance-->
<!-- with the License. You may obtain a copy of the License at-->
<!---->
<!-- http://www.apache.org/licenses/LICENSE-2.0-->
<!---->
<!-- Unless required by applicable law or agreed to in writing,-->
<!-- software distributed under the License is distributed on an-->
<!-- "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY-->
<!-- KIND, either express or implied. See the License for the-->
<!-- specific language governing permissions and limitations-->
<!-- under the License-->

<?php

namespace Airavata\Client;

$GLOBALS['THRIFT_ROOT'] = 'Thrift/';
//require_once $GLOBALS['THRIFT_ROOT'] . 'Thrift.php';
require_once $GLOBALS['THRIFT_ROOT'] . 'Transport/TTransport.php';
require_once $GLOBALS['THRIFT_ROOT'] . 'Transport/TSocket.php';
require_once $GLOBALS['THRIFT_ROOT'] . 'Protocol/TProtocol.php';
require_once $GLOBALS['THRIFT_ROOT'] . 'Protocol/TBinaryProtocol.php';
require_once $GLOBALS['THRIFT_ROOT'] . 'Exception/TException.php';
require_once $GLOBALS['THRIFT_ROOT'] . 'Exception/TTransportException.php';
require_once $GLOBALS['THRIFT_ROOT'] . 'Type/TType.php';
require_once $GLOBALS['THRIFT_ROOT'] . 'Type/TMessageType.php';
require_once $GLOBALS['THRIFT_ROOT'] . 'Factory/TStringFuncFactory.php';
require_once $GLOBALS['THRIFT_ROOT'] . 'StringFunc/TStringFunc.php';
require_once $GLOBALS['THRIFT_ROOT'] . 'StringFunc/Core.php';

$GLOBALS['AIRAVATA_ROOT'] = 'Airavata/';
require_once $GLOBALS['AIRAVATA_ROOT'] . 'API/Airavata.php';

use Thrift\Protocol\TBinaryProtocol;
use Thrift\Transport\TSocket;
use Airavata\API\AiravataClient;

class AiravataClientFactory
{

    private $airavataServerHost;
    private $airavataServerPort;

    private $appCatalogServerHost;
    private $appCatalogServerPort;

    public function __construct($options)
    {
        $this->airavataServerHost = isset($options['airavataServerHost']) ? $options['airavataServerHost'] : null;
        $this->airavataServerPort = isset($options['airavataServerPort']) ? $options['airavataServerPort'] : null;

		$this->appCatalogServerHost = isset($options['appCatalogServerHost']) ? $options['appCatalogServerHost'] : null;
        $this->appCatalogServerPort = isset($options['appCatalogServerPort']) ? $options['appCatalogServerPort'] : null;
    }

    public function getAiravataClient()
    {
        $transport = new TSocket($this->airavataServerHost, $this->airavataServerPort);
        $protocol = new TBinaryProtocol($transport);
	    $transport->open();
        return new AiravataClient($protocol);
    }

    public function getApplicationCatalogClient()
    {
        $transport = new TSocket($this->appCatalogServerHost, $this->appCatalogServerPort);
        $protocol = new TBinaryProtocol($transport);
		$transport->open();
        return new AiravataClient($protocol);
    }
}

