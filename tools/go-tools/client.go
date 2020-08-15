package main

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import (
	"crypto/tls"
	"fmt"
	"git.apache.org/thrift.git/lib/go/thrift"
	"airavata_api"
	"security_model"
	"context"

	//"experiment_model"
	//"time"
)

func handleClient(client *airavata_api.AiravataClient) (err error) {

	//pointer
	accessToken:= token999
	myMap:=map[string]string{"gatewayID": "default", "userName": "satyamsah1"}

	authorizedToken := security_model.AuthzToken{AccessToken:accessToken,ClaimsMap:myMap}
	//client.GetUserProject


	 // _,x := client.GetUserProjects(context.Background(), &authorizedToken, "default","satyamsah1",-1,0)
	 //fmt.Printf(x.)
	fmt.Println(client.GetUserProjects(context.Background(), &authorizedToken, "default","satyamsah1",-1,0))

//	createNewExperiment(&authorizedToken)

	return err
}

//func createNewExperiment( authzToken *security_model.AuthzToken )  {
//
//	 var desc="yahi hai"
//	 var descPtr=&desc
//
//	experiment_model.ExperimentModel{
//		ExperimentId : "1245" ,
//		ProjectId : "345356645" ,
//		GatewayId  : "default",
//		ExperimentType : 0,
//		UserName : "satyamsah1" ,
//		ExperimentName : "newexperiment" ,
//		CreationTime : time.Unix(1,0),
//		Description : descPtr,
//		ExecutionId :
//		GatewayExecutionId : *string
//		GatewayInstanceId *string
//		EnableEmailNotification *bool
//		EmailAddresses []string
//		UserConfigurationData *UserConfigurationDataModel
//		ExperimentInputs []*application_io_models.InputDataObjectType
//		ExperimentOutputs []*application_io_models.OutputDataObjectType
//		ExperimentStatus []*status_models.ExperimentStatus
//		Errors []*airavata_commons.ErrorModel
//		Processes []*process_model.ProcessModel
//
//
//
//	}
//
//}

func runClient(transportFactory thrift.TTransportFactory, protocolFactory thrift.TProtocolFactory, addr string, secure bool) error {
	var transport thrift.TTransport
	var err error
	if secure {
		cfg := new(tls.Config)
		cfg.InsecureSkipVerify = true
		transport, err = thrift.NewTSSLSocket(addr, cfg)
	} else {
		transport, err = thrift.NewTSocket(addr)
	}
	if err != nil {
		fmt.Println("Error opening socket:", err)
		return err
	}
	transport, err = transportFactory.GetTransport(transport)
	if err != nil {
		return err
	}
	defer transport.Close()
	if err := transport.Open(); err != nil {
		return err
	}
	return  handleClient(airavata_api.NewAiravataClientFactory(transport,protocolFactory))
	//return handleClient(tutorial.NewCalculatorClientFactory(transport, protocolFactory))
}
