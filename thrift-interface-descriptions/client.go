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
)

func handleClient(client *airavata_api.AiravataClient) (err error) {

	//pointer
	accesstoken:= "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJsaHVqcnRpblpqMlV0Q3Faalg2bkR0Wm1zaDdYUUZsazZCdWRnMFFKQ29nIn0.eyJqdGkiOiI2YmQ4MmExOS1kMWM0LTRmYWEtOTk2ZS03YjExMWRmNGU5ZGEiLCJleHAiOjE1MDY4ODcwMzAsIm5iZiI6MCwiaWF0IjoxNTA2ODg2NzMwLCJpc3MiOiJodHRwczovL2lhbWRldi5zY2lnYXAub3JnL2F1dGgvcmVhbG1zL2RlZmF1bHQiLCJhdWQiOiJwZ2EiLCJzdWIiOiJhZDVmYjY5Yy0xNjFiLTQzNTMtOWVlZi02NzExNzZiZDNkMzAiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJwZ2EiLCJhdXRoX3RpbWUiOjAsInNlc3Npb25fc3RhdGUiOiI2OWJiZWNhYS0wMWVkLTQ1OTgtOGYxNC03YjhmMjNmNTk3ZGQiLCJhY3IiOiIxIiwiY2xpZW50X3Nlc3Npb24iOiJjMWM2NzZhZi0yODQzLTQ2MDctYTQxZi0yMWY0MmNmMzA4Y2YiLCJhbGxvd2VkLW9yaWdpbnMiOlsiaHR0cHM6Ly9kZXYudGVzdGRyaXZlLmFpcmF2YXRhLm9yZyJdLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsiYWRtaW4iLCJ1bWFfYXV0aG9yaXphdGlvbiJdfSwicmVzb3VyY2VfYWNjZXNzIjp7InBnYSI6eyJyb2xlcyI6WyJnYXRld2F5LXVzZXIiXX0sImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJ2aWV3LXByb2ZpbGUiXX19LCJuYW1lIjoiS3VtYXIgU2F0eWFtIiwicHJlZmVycmVkX3VzZXJuYW1lIjoic2F0eWFtc2FoMSIsImdpdmVuX25hbWUiOiJLdW1hciIsImZhbWlseV9uYW1lIjoiU2F0eWFtIiwiZW1haWwiOiJrci5zYXR5YW0uaW5kQGdtYWlsLmNvbSJ9.aOuoS7dSv9N-hOZvbv5Lbp1azOL6rqwnOs_l4LMKYM60f8zLIhxcmzhdq1iHJDjnU9FHO9ZXdKtUuuapPbEDcu2gvJv45JcyDGaCeFjZ6aTbWsuND00JR08c-dfAt8ngiJCZRKsdu4N5zB9-_RLRdjjbDfqNBMQXlJCMEvEDzh87gtPkcJcjBI3VtQpWog_piHcqAwbXmSjoKZwCvtYQJlioK-7t21j4DOMZrX6qikyPvRihuy3-iMMJrMMiQt9Nu-GusMtKaj7DMCeNwQiWQKRtlaOTk9CRSbMoLs0xJIVj2h2EdqOax32UkZFS5Eps3AS6GZRMbLzAiTu4Vngjgw"
		authorizedToken := security_model.AuthzToken{AccessToken:accesstoken,ClaimsMap:{"gatewayID": "default", "userName": "satyamsah1"}}
	//client.GetUserProject
	client.GetUserProjects(&authorizedToken, "default","satyamsah1",-1,0)
	return err
}

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
	transport = transportFactory.GetTransport(transport)
	defer transport.Close()
	if err := transport.Open(); err != nil {
		return err
	}
	return  handleClient(airavata_api.NewAiravataClientFactory(transport,protocolFactory))
	//return handleClient(tutorial.NewCalculatorClientFactory(transport, protocolFactory))
}
