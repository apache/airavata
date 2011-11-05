/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.airavata.commons.gfac.wsdl;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.schemas.gfac.InputParameterType;
import org.apache.airavata.schemas.gfac.MethodType;
import org.apache.airavata.schemas.gfac.OutputParameterType;
import org.apache.airavata.schemas.gfac.PortTypeType;
import org.apache.airavata.schemas.gfac.ServiceDescriptionDocument;
import org.apache.airavata.schemas.gfac.ServiceDescriptionType;
import org.apache.airavata.schemas.gfac.StringParameterType;
import org.apache.airavata.schemas.gfac.ServiceType.ServiceName;
import org.apache.xmlbeans.XmlException;
import org.junit.Test;

public class TestWSDLGeneration {

    public static String createAwsdl4ServiceMap(String serviceDescAsStr) throws GFacWSDLException {
        try {
            ServiceDescriptionType serviceDesc = ServiceDescriptionDocument.Factory.parse(serviceDescAsStr)
                    .getServiceDescription();
            WSDLGenerator wsdlGenerator = new WSDLGenerator();
            Hashtable serviceTable = wsdlGenerator.generateWSDL(null, null, null, serviceDesc, true);
            String wsdl = (String) serviceTable.get(WSDLConstants.AWSDL);
            System.out.println("The generated AWSDL is " + wsdl);
            return wsdl;
        } catch (XmlException e) {
            throw new GFacWSDLException(e);
        }
    }

    public static String createCwsdl4ServiceMap(String serviceDescAsStr) throws GFacWSDLException {
        try {
            ServiceDescriptionType serviceDesc = ServiceDescriptionDocument.Factory.parse(serviceDescAsStr)
                    .getServiceDescription();
            WSDLGenerator wsdlGenerator = new WSDLGenerator();
            String security = WSDLConstants.TRANSPORT_LEVEL;
            String serviceLocation = "http://localhost:8080/axis2/services/test?wsdl";

            Hashtable serviceTable = wsdlGenerator.generateWSDL(serviceLocation, null, security, serviceDesc, false);

            String wsdl = (String) serviceTable.get(WSDLConstants.WSDL);

            System.out.println("The generated CWSDL is " + wsdl);
            return wsdl;

        } catch (XmlException e) {
            throw new GFacWSDLException(e);
        }
    }

    @Test
    public void test() {

        /*
         * Service
         */
        ServiceDescription serv = new ServiceDescription();
        serv.getType().setName("SimpleEcho");
        serv.getType().addNewService();
        ServiceName name = serv.getType().getService().addNewServiceName();
        name.setStringValue("SimpleEcho");
        PortTypeType portType = serv.getType().addNewPortType();
        MethodType methodType = portType.addNewMethod();

        methodType.setMethodName("invoke");

        List<InputParameterType> inputList = new ArrayList<InputParameterType>();
        InputParameterType input = InputParameterType.Factory.newInstance();
        input.setParameterName("echo_input");
        input.setParameterType(StringParameterType.Factory.newInstance());
        inputList.add(input);
        InputParameterType[] inputParamList = inputList.toArray(new InputParameterType[inputList.size()]);

        List<OutputParameterType> outputList = new ArrayList<OutputParameterType>();
        OutputParameterType output = OutputParameterType.Factory.newInstance();
        output.setParameterName("echo_output");
        output.setParameterType(StringParameterType.Factory.newInstance());
        outputList.add(output);
        OutputParameterType[] outputParamList = outputList.toArray(new OutputParameterType[outputList.size()]);

        serv.getType().setInputParametersArray(inputParamList);
        serv.getType().setOutputParametersArray(outputParamList);

        try {
            WSDLGenerator generator = new WSDLGenerator();
            Hashtable table = generator.generateWSDL("http://localhost.com", new QName("xxxx"), "xx", serv.getType(),
                    true);
            Set set = table.entrySet();
            for (Object object : set) {
                System.out.println(((Entry) object).getKey());
                System.out.println(((Entry) object).getValue());

            }
            System.out.println("DONE");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
