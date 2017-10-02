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

)

func handleClient(client *airavata_api.AiravataClient) (err error) {

	//pointer
	accessToken:= "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJsaHVqcnRpblpqMlV0Q3Faalg2bkR0Wm1zaDdYUUZsazZCdWRnMFFKQ29nIn0.eyJqdGkiOiI3MTNiMjc2MS03MDhkLTRiMmUtODY3Yi0zZmNjZTViMmJiZWQiLCJleHAiOjE1MDY5MTI3NDUsIm5iZiI6MCwiaWF0IjoxNTA2OTEyNDQ1LCJpc3MiOiJodHRwczovL2lhbWRldi5zY2lnYXAub3JnL2F1dGgvcmVhbG1zL2RlZmF1bHQiLCJhdWQiOiJwZ2EiLCJzdWIiOiJhZDVmYjY5Yy0xNjFiLTQzNTMtOWVlZi02NzExNzZiZDNkMzAiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJwZ2EiLCJhdXRoX3RpbWUiOjAsInNlc3Npb25fc3RhdGUiOiIzYTFmYThlMC1iZjE0LTQ5ZDEtOTE2NS00N2UyNzQxOTkzZjgiLCJhY3IiOiIxIiwiY2xpZW50X3Nlc3Npb24iOiIyODVhOWIzZS1jOGU4LTQ5OTgtYWZkOS05ODJlNzAwOTg2ZmUiLCJhbGxvd2VkLW9yaWdpbnMiOlsiaHR0cHM6Ly9kZXYudGVzdGRyaXZlLmFpcmF2YXRhLm9yZyJdLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsiYWRtaW4iLCJ1bWFfYXV0aG9yaXphdGlvbiJdfSwicmVzb3VyY2VfYWNjZXNzIjp7InBnYSI6eyJyb2xlcyI6WyJnYXRld2F5LXVzZXIiXX0sImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJ2aWV3LXByb2ZpbGUiXX19LCJuYW1lIjoiS3VtYXIgU2F0eWFtIiwicHJlZmVycmVkX3VzZXJuYW1lIjoic2F0eWFtc2FoMSIsImdpdmVuX25hbWUiOiJLdW1hciIsImZhbWlseV9uYW1lIjoiU2F0eWFtIiwiZW1haWwiOiJrci5zYXR5YW0uaW5kQGdtYWlsLmNvbSJ9.DDHztOgKTW3JyBQH3yE-nwBQN9Je1-H3mhLiB9XYt60jSchT_7riXIeesZ25PDZ-2zelbftqQIM9LDSlWqs7r-xbmB5PeBGytjVrTb9rsyYJ6YZ2l6VpctfVI193cH2PENgM5gkAts07_yPM06VxVSI_JJHU9LHPAUC8YPFKgD_rlTjliZSEp_leh876CG3uYCKb2dbPBfIWi_WBWibcCoPFbxuAHyZZFPGV8t3RxFeAVj19Kp0cR4tzPSYjwfbp-h_4M2OAlvfuINHnRGNT5a3KKGGvHpPFsRpgDGvEJyga7a8UsNa8Nc9ows8AZ1G2GR7Si4aD3PRUAvDQxzwUZA"
    myMap:=map[string]string{"gatewayID": "default", "userName": "satyamsah1"}

	authorizedToken := security_model.AuthzToken{AccessToken:accessToken,ClaimsMap:myMap}
	//client.GetUserProject


	 // _,x := client.GetUserProjects(context.Background(), &authorizedToken, "default","satyamsah1",-1,0)
	 //fmt.Printf(x.)
	fmt.Println(client.GetUserProjects(context.Background(), &authorizedToken, "default","satyamsah1",-1,0))



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
