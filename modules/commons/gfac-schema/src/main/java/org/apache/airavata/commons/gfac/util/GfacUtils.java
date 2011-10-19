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

package org.apache.airavata.commons.gfac.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlException;

import org.ietf.jgss.GSSCredential;
import org.xmlpull.mxp1_serializer.MXSerializer;
import org.xmlpull.v1.builder.Iterable;
import org.xmlpull.v1.builder.XmlBuilderException;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;

import xsul.XmlConstants;
import xsul.invoker.http.HttpDynamicInfosetInvoker;
import xsul.lead.LeadContextHeader;
import xsul.soap.SoapUtil;
import xsul.ws_addressing.WsaInvoker;
import xsul.ws_addressing.WsaMessageInformationHeaders;
import xsul.ws_addressing.WsaRelatesTo;
import xsul.wsif.impl.WSIFMessageElement;

public class GfacUtils {

    private final static String PROPERTY_SERIALIZER_INDENTATION = "http://xmlpull.org/v1/doc/properties.html#serializer-indentation";

    private final static String INDENT = "    ";
    private static AtomicInteger tempFileCount = new AtomicInteger();
    
    private static Random random = new Random();
    

    public static void writeToFile(InputStream is, File file) throws IOException {
            FileWriter fw = new FileWriter(file);

            // get the standard out of the application and write to file

            Reader reader = new InputStreamReader(is);
            char[] cbuf = new char[1024];
            while ((reader.read(cbuf, 0, 1024)) != -1) {
                fw.write(cbuf);
            }
            fw.close();
            reader.close();
            is.close();
    }

    public static void writeToFile(String data, File file) throws IOException {
            FileWriter fw = new FileWriter(file);

            // get the standard out of the application and write to file
            fw.write(data);
            fw.close();
    }

    public static String readFromStream(InputStream in) throws IOException {
            StringBuffer wsdlStr = new StringBuffer();

            int read;

            byte[] buf = new byte[1024];
            while ((read = in.read(buf)) > 0) {
                wsdlStr.append(new String(buf, 0, read));
            }
            in.close();
            return wsdlStr.toString();
    }


    public static HostDescriptionType parseHostDescirption(String hostDescStr) throws GFacSchemaException {
        try {
            HostDescriptionType hostDesc = HostDescriptionDocument.Factory.parse(hostDescStr)
                    .getHostDescription();
            SchemaValidator validator = new SchemaValidator(hostDesc);
            validator.validate();
            return hostDesc;
        } catch (XmlException e) {
            throw new GFacSchemaException(e);
        } catch (Exception e) {
            throw new GFacSchemaException(e);
        }
    }

    public static ApplicationDescriptionType parseAppDescirption(Reader reader)
            throws GFacSchemaException {
        try {
            ApplicationDescriptionType appDesc = ApplicationDescriptionDocument.Factory.parse(
                    reader).getApplicationDescription();
            SchemaValidator validator = new SchemaValidator(appDesc);
            validator.validate();
            return appDesc;
        } catch (XmlException e) {
            throw new GFacSchemaException(e);
        } catch (IOException e) {
            throw new GFacSchemaException(e);
        } catch (Exception e) {
            throw new GFacSchemaException(e);
        }
    }

    public static MethodType findOperationFromServiceMap(String operationName,
            ServiceMapType serviceMap) throws GFacSchemaException {
        PortTypeType portType = serviceMap.getPortTypeArray()[0];
        MethodType[] methods = portType.getMethodArray();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getMethodName().equals(operationName)) {
                return methods[i];
            }
        }

        if (isInbuiltOperation(operationName)) {
            MethodType method = portType.addNewMethod();
            method.setMethodName(operationName);
            return method;
        }

        throw new GFacSchemaException("Method name " + operationName + " not found");
    }

    public static MethodType findOperationWithApplication(ServiceMapType serviceMap)
            throws GFacSchemaException {
        MethodType[] methods = serviceMap.getPortTypeArray()[0].getMethodArray();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getApplication() != null) {
                return methods[i];
            }
        }
        return null;
    }

    public static boolean isLocalHost(String appHost) throws GFacSchemaException {
        try {
            String localHost = InetAddress.getLocalHost().getCanonicalHostName();

            if (localHost.equals(appHost) || GFacConstants.LOCALHOST.equals(appHost)
                    || GFacConstants._127_0_0_1.equals(appHost)) {
                return true;
            } else {
                return false;
            }
        } catch (UnknownHostException e) {
            throw new GFacSchemaException(e);
        }
    }

    public static boolean isArray(String typeName) {
        // <simpleType name="outputDataType">
        // <restriction base="xsd:string">
        // <enumeration value="String"/>
        // <enumeration value="Integer"/>
        // <enumeration value="Float"/>
        // <enumeration value="Double"/>
        // <enumeration value="Boolean"/>
        // <enumeration value="QName"/>
        // <enumeration value="URI"/>
        // <enumeration value="StringArray"/>
        // <enumeration value="IntegerArray"/>
        // <enumeration value="FloatArray"/>
        // <enumeration value="DoubleArray"/>
        // <enumeration value="BooleanArray"/>
        // <enumeration value="QNameArray"/>
        // <enumeration value="URIArray"/>
        // <enumeration value="LEADFileID"/>
        // <enumeration value="LEADFileIDArray"/>
        // <enumeration value="StdOut"/>
        // <enumeration value="StdErr"/>
        // </restriction>
        // </simpleType>
        if (typeName.endsWith("Array")) {
            // TODO make it more tigter
            return true;
        } else {
            return false;
        }
    }

    /**
     * This method perpare the arguments to be passed to the invocation
     * 
     * @param inputMessage
     * @param parmsValuesOnly,
     *            this is usually true. If it is false, the parameters are
     *            passed as Name value pairs. If the parameter name is foo and
     *            it is a array type parameters will be passed as foo0,foo1 ....
     * @return
     * @throws GFacSchemaException
     */
    public static ArrayList<String> prepareParameters(MessageContext inputMessage,
            boolean parmsValuesOnly) throws GFacSchemaException {
        try {
            ArrayList<String> params = new ArrayList<String>();
            Iterator<String> names = inputMessage.getParameterNames();
            while (names.hasNext()) {
                String name = names.next();
                String type = inputMessage.getParameterType(name);

                if (GFacConstants.Types.TYPE_DATAID.equals(type)) {
                    // add parameter name if needed
                    if (!parmsValuesOnly) {
                        params.add(name);
                    }
                    params.add(new URI(inputMessage.getStringParameterValue(name)).getPath());
                } else if (GFacConstants.Types.TYPE_DATAID_ARRAY.equals(type)) {
                    Object value = inputMessage.getParameterValue(name);
                    if (value instanceof Object[]) {
                        Object[] valueArray = (Object[]) value;
                        for (int j = 0; j < valueArray.length; j++) {
                            // add parameter name if needed
                            if (!parmsValuesOnly) {
                                params.add(name + j);
                            }
                            params.add(new URI(valueArray[j].toString()).getPath());
                        }
                    }
                } else if (GFacConstants.Types.TYPE_LEADFILEID.equals(type)
                        || GFacConstants.Types.TYPE_URI.equals(type)) {
                    // add parameter name if needed
                    if (!parmsValuesOnly) {
                        params.add(name);
                    }
                    params.add(new URI(inputMessage.getStringParameterValue(name)).getPath());
                } else if (GFacConstants.Types.TYPE_LEADFILEID_ARRAY.equals(type)
                        || GFacConstants.Types.TYPE_URI_ARRAY.equals(type)) {
                    Object value = inputMessage.getParameterValue(name);
                    if (value instanceof Object[]) {
                        Object[] valueArray = (Object[]) value;
                        for (int j = 0; j < valueArray.length; j++) {
                            // add parameter name if needed
                            if (!parmsValuesOnly) {
                                params.add(name + j);
                            }
                            params.add(new URI(valueArray[j].toString()).getPath());
                        }
                    }
                } else {
                    if (GfacUtils.isArray(type)) {
                        Object value = inputMessage.getParameterValue(name);
                        if (value instanceof Object[]) {
                            Object[] valueArray = (Object[]) value;
                            for (int j = 0; j < valueArray.length; j++) {
                                // add parameter name if needed
                                if (!parmsValuesOnly) {
                                    params.add(name + j);
                                }
                                params.add(valueArray[j].toString());
                            }
                        }
                    } else {
                        if (!parmsValuesOnly) {
                            params.add(name);
                        }
                        params.add(inputMessage.getStringParameterValue(name));
                    }

                    // add parameter name if needed
                }
            }
            return params;
        } catch (URISyntaxException e) {
            throw new GFacSchemaException(e);
        }
    }

    public static boolean isInbuiltOperation(String name) {
        return GFacConstants.InbuitOperations.OP_KILL.equals(name)
                || GFacConstants.InbuitOperations.OP_PING.equals(name)
                || GFacConstants.InbuitOperations.OP_SHUTDOWN.equals(name);
    }

    public static String findStringValue(String name, WSIFMessageElement response) {
        return (String) response.getObjectPart(name);
    }

    public static ArrayList<String> findArrayValue(String name, WSIFMessageElement response) {
        XmlElement param = response.element(null, name);
        if (param != null) {
            Iterable it = param.elements(null, "value");
            if (it != null) {
                ArrayList<String> values = new ArrayList<String>();

                Iterator arrayValues = it.iterator();
                while (arrayValues.hasNext()) {
                    values.add(((XmlElement) arrayValues.next()).requiredTextContent());
                }
                return values;
            }
        }
        return null;
    }

    public static void printArray(ArrayList<String> list) {
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            System.out.println(iter.next());
        }
    }

    public static String createServiceDirName(QName serviceName) {
        String date = new Date().toString();
        date = date.replaceAll(" ", "_");
        date = date.replaceAll(":", "_");
        return serviceName.getLocalPart() + "_" + date + "_" + UUID.randomUUID();
    }

    public static String createErrorMessage(Exception e) {
        StringWriter errStrW = new StringWriter();
        e.printStackTrace(new PrintWriter(errStrW));
        return errStrW.getBuffer().toString();
    }

    public static String prettyPrint2String(XmlElement ele) {
        try {
            MXSerializer serializer = new MXSerializer();
            StringWriter writer = new StringWriter();
            serializer.setOutput(writer);
            serializer.setProperty(PROPERTY_SERIALIZER_INDENTATION, INDENT);
            XmlConstants.BUILDER.serialize(ele, serializer);
            return writer.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error happend pretty printing";
        }
    }

    public static void prettyPrint(XmlElement ele) throws GFacSchemaException {
        try {

            System.out.println(prettyPrint2String(ele));

        } catch (IllegalArgumentException e) {
            throw new GFacSchemaException(e);
        } catch (IllegalStateException e) {
            throw new GFacSchemaException(e);
        } catch (XmlBuilderException e) {
            throw new GFacSchemaException(e);
        }
    }

    public static URI createGsiftpURI(String host, String localPath) throws GFacSchemaException {
        try {
            StringBuffer buf = new StringBuffer();
            buf.append("gsiftp://").append(host).append("/").append(localPath);
            return new URI(buf.toString());
        } catch (URISyntaxException e) {
            throw new GFacSchemaException(e);
        }
    }

    public static Vector<URI> listLocalDir(String dirName, String localhost) throws GFacSchemaException {
        try {
            Vector<URI> files = new Vector<URI>();
            File dir = new File(dirName);

            if (dir.exists()) {
                File[] fileList = dir.listFiles();
                for (File file : fileList) {
                    if (!file.getName().equals(".") && !file.getName().equals("..")) {
                        URI uri = new URI("gsiftp://" + localhost + "/" + file.getAbsolutePath());
                        files.add(uri);
                    }
                }
            } else {
                throw new GFacSchemaException("can not find the output data directory to list files");
            }
            return files;
        } catch (URISyntaxException e) {
            throw new GFacSchemaException(e);
        }
    }

    public static QName getServiceNameFromServiceMap(ServiceMapType serviceMap) {
        ServiceName serviceName = serviceMap.getService().getServiceName();
        return new QName(serviceName.getTargetNamespace(), serviceName.getStringValue());
    }

  

    public static URI createWorkflowQName(QName name) throws GFacSchemaException {
        try {
            return new URI("urn:qname:" + name.getNamespaceURI() + ":" + name.getLocalPart());
        } catch (URISyntaxException e) {
            throw new GFacSchemaException(e);
        }
    }

    public static String findStrProperty(Properties config, String name, String defaultVal) {
        String value = config.getProperty(name);
        if (value == null) {
            return defaultVal;
        }
        return value.trim();
    }
    
    public static boolean findBooleanProperty(Properties config, String name, boolean defaultVal) {
        String value = config.getProperty(name);
        if (value == null) {
            return defaultVal;
        }
        return Boolean.valueOf(value.trim());
    }
    

    private static XmlInfosetBuilder builder = XmlConstants.BUILDER;;

    public static String buildAnnotations(QName name, String value) {
        XmlElement anno = builder.newFragment(name.getNamespaceURI(), name.getLocalPart());
        anno.addChild(value);
        return builder.serializeToString(anno);
    }

    public static QName findApplcationName(ServiceMapType serviceMap) throws GFacSchemaException{
        MethodType method = GfacUtils.findOperationWithApplication(serviceMap);

        if (method == null) {
            throw new GFacSchemaException("None of the methods has application defined");
        }

        String applicationName = method.getApplication().getApplicationName().getStringValue();
        String applicationNameNs = method.getApplication().getApplicationName()
                .getTargetNamespace();
        return new QName(applicationNameNs, applicationName);
    }
    
    public static String formatJobStatus(String jobid,String jobstatus){
        return "Status of job " + jobid + "is " + jobstatus ;
    }
    
    public static String findServiceRegistryUrl(LeadContextHeader header){
        if(header == null){
            return null;
        }else{
            URI serviceRegistryUrl = header.getXRegistryUrl();
            if(serviceRegistryUrl == null){
                serviceRegistryUrl = header.getResourceCatalogUrl();
            }
            if(serviceRegistryUrl != null){
                return serviceRegistryUrl.toString();
            }else{
                return null;
            }
        }
    }
    
    
     public static String createRandomName(String name){
    	return name + System.currentTimeMillis() + "_" + tempFileCount.incrementAndGet();
    }
     
     public static Throwable getRootCause(Throwable e){
         Throwable cause = e.getCause();
         while(cause != null && cause.getCause() != null){
             cause = cause.getCause();
         }
         if(cause == null){
             return e;
         }else{
             return cause;
         }
     }
     
     public static Random getRandom(){
         return random;
     }
}
