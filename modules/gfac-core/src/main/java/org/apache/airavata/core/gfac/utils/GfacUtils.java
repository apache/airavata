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

package org.apache.airavata.core.gfac.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.UUID;

import org.apache.airavata.commons.gfac.type.ActualParameter;
import org.apache.airavata.schemas.gfac.*;
import org.apache.axiom.om.OMElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GfacUtils {
    private final static Logger log = LoggerFactory.getLogger(GfacUtils.class);

    private GfacUtils() {
    }

    /**
     * Read data from inputStream and convert it to String.
     * 
     * @param in
     * @return String read from inputStream
     * @throws IOException
     */
    public static String readFromStream(InputStream in) throws IOException {
        try {
            StringBuffer wsdlStr = new StringBuffer();

            int read;

            byte[] buf = new byte[1024];
            while ((read = in.read(buf)) > 0) {
                wsdlStr.append(new String(buf, 0, read));
            }
            return wsdlStr.toString();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.warn("Cannot close InputStream: " + in.getClass().getName(), e);
                }
            }
        }
    }

    public static String readFileToString(String file) throws FileNotFoundException, IOException {
        BufferedReader instream = null;
        try {

            instream = new BufferedReader(new FileReader(file));
            StringBuffer buff = new StringBuffer();
            String temp = null;
            while ((temp = instream.readLine()) != null) {
                buff.append(temp);
                buff.append(GFacConstants.NEWLINE);
            }
            return buff.toString();
        } finally {
            if (instream != null) {
                try {
                    instream.close();
                } catch (IOException e) {
                    log.warn("Cannot close FileinputStream", e);
                }
            }
        }
    }

    public static boolean isLocalHost(String appHost) throws UnknownHostException {
        String localHost = InetAddress.getLocalHost().getCanonicalHostName();
        return (localHost.equals(appHost) || GFacConstants.LOCALHOST.equals(appHost) || GFacConstants._127_0_0_1
                .equals(appHost));
    }

    public static String createUniqueNameForService(String serviceName) {
        String date = new Date().toString();
        date = date.replaceAll(" ", "_");
        date = date.replaceAll(":", "_");
        return serviceName + "_" + date + "_" + UUID.randomUUID();
    }

    public static URI createGsiftpURI(GridFTPContactInfo host, String localPath) throws URISyntaxException {
        StringBuffer buf = new StringBuffer();

        if (!host.hostName.startsWith("gsiftp://"))
            buf.append("gsiftp://");
        buf.append(host).append(":").append(host.port);
        if (!host.hostName.endsWith("/"))
            buf.append("/");
        buf.append(localPath);
        return new URI(buf.toString());
    }

    public static URI createGsiftpURI(String host, String localPath) throws URISyntaxException {
        StringBuffer buf = new StringBuffer();
        if (!host.startsWith("gsiftp://"))
            buf.append("gsiftp://");
        buf.append(host);
        if (!host.endsWith("/"))
            buf.append("/");
        buf.append(localPath);
        return new URI(buf.toString());
    }

    public static ActualParameter getInputActualParameter(Parameter parameter, OMElement element) {
        OMElement innerelement = null;
        ActualParameter actualParameter = new ActualParameter();
        if ("String".equals(parameter.getParameterType().getName())) {
            actualParameter = new ActualParameter(StringParameterType.type);
            if (!"".equals(element.getText())) {
                ((StringParameterType) actualParameter.getType()).setValue(element.getText());
            } else if(element.getChildrenWithLocalName("value").hasNext()){
                innerelement = (OMElement) element.getChildrenWithLocalName("value").next();
                ((StringParameterType) actualParameter.getType()).setValue(innerelement.getText());
            } else{
               ((StringParameterType) actualParameter.getType()).setValue("");
            }
        } else if ("Double".equals(parameter.getParameterType().getName())) {
            actualParameter = new ActualParameter(DoubleParameterType.type);
            if (!"".equals(element.getText())) {
                ((DoubleParameterType) actualParameter.getType()).setValue(new Double(innerelement.getText()));
            } else {
                innerelement = (OMElement) element.getChildrenWithLocalName("value").next();
                ((DoubleParameterType) actualParameter.getType()).setValue(new Double(innerelement.getText()));
            }
        } else if ("Integer".equals(parameter.getParameterType().getName())) {
            actualParameter = new ActualParameter(IntegerParameterType.type);
            if (!"".equals(element.getText())) {
                ((IntegerParameterType) actualParameter.getType()).setValue(new Integer(element.getText()));
            } else {
                innerelement = (OMElement) element.getChildrenWithLocalName("value").next();
                ((IntegerParameterType) actualParameter.getType()).setValue(new Integer(innerelement.getText()));
            }
        } else if ("Float".equals(parameter.getParameterType().getName())) {
            actualParameter = new ActualParameter(FloatParameterType.type);
            if (!"".equals(element.getText())) {
                ((FloatParameterType) actualParameter.getType()).setValue(new Float(element.getText()));
            } else {
                innerelement = (OMElement) element.getChildrenWithLocalName("value").next();
                ((FloatParameterType) actualParameter.getType()).setValue(new Float(innerelement.getText()));
            }
        } else if ("Boolean".equals(parameter.getParameterType().getName())) {
            actualParameter = new ActualParameter(BooleanParameterType.type);
            if (!"".equals(element.getText())) {
                ((BooleanParameterType) actualParameter.getType()).setValue(new Boolean(element.getText()));
            } else {
                innerelement = (OMElement) element.getChildrenWithLocalName("value").next();
                ((BooleanParameterType) actualParameter.getType()).setValue(Boolean.parseBoolean(innerelement.getText()));
            }
        } else if ("File".equals(parameter.getParameterType().getName())) {
            actualParameter = new ActualParameter(FileParameterType.type);
            if (!"".equals(element.getText())) {
                ((FileParameterType) actualParameter.getType()).setValue(element.getText());
            } else {
                innerelement = (OMElement) element.getChildrenWithLocalName("value").next();
                ((FileParameterType) actualParameter.getType()).setValue(innerelement.getText());
            }
        } else if ("URI".equals(parameter.getParameterType().getName())) {
            actualParameter = new ActualParameter(URIParameterType.type);
            if (!"".equals(element.getText())) {
                ((URIParameterType) actualParameter.getType()).setValue(element.getText());
            } else if(element.getChildrenWithLocalName("value").hasNext()){
                innerelement = (OMElement) element.getChildrenWithLocalName("value").next();
                System.out.println(actualParameter.getType().toString());
                log.debug(actualParameter.getType().toString());
                ((URIParameterType) actualParameter.getType()).setValue(innerelement.getText());
            } else{
                ((URIParameterType) actualParameter.getType()).setValue("");
            }
        } else if ("StringArray".equals(parameter.getParameterType().getName())) {
            actualParameter = new ActualParameter(StringArrayType.type);
            Iterator value = element.getChildrenWithLocalName("value");
            int i = 0;
            if (!"".equals(element.getText())) {
                String[] list = element.getText().split(",");
                for(String arrayValue:list){
                    ((StringArrayType) actualParameter.getType()).insertValue(i++, arrayValue);
                }
            } else {
                while (value.hasNext()) {
                    innerelement = (OMElement) value.next();
                    ((StringArrayType) actualParameter.getType()).insertValue(i++, innerelement.getText());
                }
            }
        } else if ("DoubleArray".equals(parameter.getParameterType().getName())) {
            actualParameter = new ActualParameter(DoubleArrayType.type);
            Iterator value = element.getChildrenWithLocalName("value");
            int i = 0;
            if (!"".equals(element.getText())) {
                String[] list = element.getText().split(",");
                for(String arrayValue:list){
                    ((DoubleArrayType) actualParameter.getType()).insertValue(i++, new Double(arrayValue));
                }
            } else {
                while (value.hasNext()) {
                    innerelement = (OMElement) value.next();
                    ((DoubleArrayType) actualParameter.getType()).insertValue(i++, new Double(innerelement.getText()));
                }
            }

        } else if ("IntegerArray".equals(parameter.getParameterType().getName())) {
            actualParameter = new ActualParameter(IntegerArrayType.type);
            Iterator value = element.getChildrenWithLocalName("value");
            int i = 0;
            if (!"".equals(element.getText())) {
               String[] list = element.getText().split(",");
                for(String arrayValue:list){
                    ((IntegerArrayType) actualParameter.getType()).insertValue(i++, new Integer(arrayValue));
                }
            } else {
                while (value.hasNext()) {
                    innerelement = (OMElement) value.next();
                    ((IntegerArrayType) actualParameter.getType()).insertValue(i++, new Integer(innerelement.getText()));
                }
            }
        } else if ("FloatArray".equals(parameter.getParameterType().getName())) {
            actualParameter = new ActualParameter(FloatArrayType.type);
            Iterator value = element.getChildrenWithLocalName("value");
            int i = 0;
            if (!"".equals(element.getText())) {
                String[] list = element.getText().split(",");
                for(String arrayValue:list){
                    ((FloatArrayType) actualParameter.getType()).insertValue(i++, new Float(arrayValue));
                }
            } else {

                while (value.hasNext()) {
                    innerelement = (OMElement) value.next();
                    ((FloatArrayType) actualParameter.getType()).insertValue(i++, new Float(innerelement.getText()));
                }
            }
        } else if ("BooleanArray".equals(parameter.getParameterType().getName())) {
            actualParameter = new ActualParameter(BooleanArrayType.type);
            Iterator value = element.getChildrenWithLocalName("value");
            int i = 0;
            if (!"".equals(element.getText())) {
                String[] list = element.getText().split(",");
                for(String arrayValue:list){
                    ((BooleanArrayType) actualParameter.getType()).insertValue(i++, new Boolean(arrayValue));
                }
            } else {

                while (value.hasNext()) {
                    innerelement = (OMElement) value.next();
                    ((BooleanArrayType) actualParameter.getType()).insertValue(i++, new Boolean(innerelement.getText()));
                }
            }
        } else if ("FileArray".equals(parameter.getParameterType().getName())) {
            actualParameter = new ActualParameter(FileArrayType.type);
            Iterator value = element.getChildrenWithLocalName("value");
            int i = 0;
            if (!"".equals(element.getText())) {
                String[] list = element.getText().split(",");
                for(String arrayValue:list){
                    ((FileArrayType) actualParameter.getType()).insertValue(i++, arrayValue);
                }
            } else {

                while (value.hasNext()) {
                    innerelement = (OMElement) value.next();
                    ((FileArrayType) actualParameter.getType()).insertValue(i++, innerelement.getText());
                }
            }
        } else if ("URIArray".equals(parameter.getParameterType().getName())) {
            actualParameter = new ActualParameter(URIArrayType.type);
            Iterator value = element.getChildrenWithLocalName("value");
            int i = 0;
            if (!"".equals(element.getText())) {
                String[] list = element.getText().split(",");
                for(String arrayValue:list){
                    ((URIArrayType) actualParameter.getType()).insertValue(i++,arrayValue);
                }
            } else {

                while (value.hasNext()) {
                    innerelement = (OMElement) value.next();
                    ((URIArrayType) actualParameter.getType()).insertValue(i++, innerelement.getText());
                }
            }
        }
        return actualParameter;
    }

    public static ActualParameter getInputActualParameter(Parameter parameter, String inputVal) {
        OMElement innerelement = null;
        ActualParameter actualParameter = new ActualParameter();
        if ("String".equals(parameter.getParameterType().getName())) {
            actualParameter = new ActualParameter(StringParameterType.type);
            ((StringParameterType) actualParameter.getType()).setValue(inputVal);
        } else if ("Double".equals(parameter.getParameterType().getName())) {
            actualParameter = new ActualParameter(DoubleParameterType.type);
            ((DoubleParameterType) actualParameter.getType()).setValue(new Double(inputVal));
        } else if ("Integer".equals(parameter.getParameterType().getName())) {
            actualParameter = new ActualParameter(IntegerParameterType.type);
            ((IntegerParameterType) actualParameter.getType()).setValue(new Integer(inputVal));
        } else if ("Float".equals(parameter.getParameterType().getName())) {
            actualParameter = new ActualParameter(FloatParameterType.type);
            ((FloatParameterType) actualParameter.getType()).setValue(new Float(inputVal));
        } else if ("Boolean".equals(parameter.getParameterType().getName())) {
            actualParameter = new ActualParameter(BooleanParameterType.type);
            ((BooleanParameterType) actualParameter.getType()).setValue(new Boolean(inputVal));
        } else if ("File".equals(parameter.getParameterType().getName())) {
            actualParameter = new ActualParameter(FileParameterType.type);
            ((FileParameterType) actualParameter.getType()).setValue(inputVal);
        } else if ("URI".equals(parameter.getParameterType().getName())) {
            actualParameter = new ActualParameter(URIParameterType.type);
            ((URIParameterType) actualParameter.getType()).setValue(inputVal);
        } else if ("StringArray".equals(parameter.getParameterType().getName())) {
            actualParameter = new ActualParameter(StringArrayType.type);
            Iterator iterator = Arrays.asList(inputVal.split(",")).iterator();
            int i = 0;
            while (iterator.hasNext()) {
                innerelement = (OMElement) iterator.next();
                ((StringArrayType) actualParameter.getType()).insertValue(i++, innerelement.getText());
            }
        } else if ("DoubleArray".equals(parameter.getParameterType().getName())) {
            actualParameter = new ActualParameter(DoubleArrayType.type);
            Iterator value =Arrays.asList(inputVal.split(",")).iterator();
            int i = 0;
            while (value.hasNext()) {
                innerelement = (OMElement) value.next();
                ((DoubleArrayType) actualParameter.getType()).insertValue(i++, new Double(innerelement.getText()));
            }
        } else if ("IntegerArray".equals(parameter.getParameterType().getName())) {
            actualParameter = new ActualParameter(IntegerArrayType.type);
            Iterator value = Arrays.asList(inputVal.split(",")).iterator();
            int i = 0;
            while (value.hasNext()) {
                innerelement = (OMElement) value.next();
                ((IntegerArrayType) actualParameter.getType()).insertValue(i++, new Integer(innerelement.getText()));
            }
        } else if ("FloatArray".equals(parameter.getParameterType().getName())) {
            actualParameter = new ActualParameter(FloatArrayType.type);
            Iterator value = Arrays.asList(inputVal.split(",")).iterator();
            int i = 0;
            while (value.hasNext()) {
                innerelement = (OMElement) value.next();
                ((FloatArrayType) actualParameter.getType()).insertValue(i++, new Float(innerelement.getText()));
            }
        } else if ("BooleanArray".equals(parameter.getParameterType().getName())) {
            actualParameter = new ActualParameter(BooleanArrayType.type);
            Iterator value = Arrays.asList(inputVal.split(",")).iterator();
            int i = 0;
            while (value.hasNext()) {
                innerelement = (OMElement) value.next();
                ((BooleanArrayType) actualParameter.getType()).insertValue(i++, new Boolean(innerelement.getText()));
            }
        } else if ("FileArray".equals(parameter.getParameterType().getName())) {
            actualParameter = new ActualParameter(FileArrayType.type);
            Iterator value = Arrays.asList(inputVal.split(",")).iterator();
            int i = 0;
            while (value.hasNext()) {
                innerelement = (OMElement) value.next();
                ((FileArrayType) actualParameter.getType()).insertValue(i++, innerelement.getText());
            }
        } else if ("URIArray".equals(parameter.getParameterType().getName())) {
            actualParameter = new ActualParameter(URIArrayType.type);
            Iterator value = Arrays.asList(inputVal.split(",")).iterator();
            int i = 0;
            while (value.hasNext()) {
                innerelement = (OMElement) value.next();
                ((URIArrayType) actualParameter.getType()).insertValue(i++, innerelement.getText());
            }
        }
        return actualParameter;
    }

}
