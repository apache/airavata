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

import javax.xml.namespace.QName;

import org.apache.airavata.core.gfac.context.ExecutionContext;
import org.apache.airavata.core.gfac.context.MessageContext;
import org.apache.airavata.core.gfac.exception.GfacException;
import org.apache.airavata.core.gfac.exception.GfacException.FaultCode;
import org.globus.gram.Gram;
import org.globus.gram.GramAttributes;
import org.globus.gram.GramJob;
import org.globus.gram.internal.GRAMConstants;
import org.ietf.jgss.GSSCredential;
import org.ogce.namespaces.x2010.x08.x30.workflowContextHeader.WorkflowContextHeaderDocument.WorkflowContextHeader;
import org.ogce.namespaces.x2010.x08.x30.workflowResourceMapping.ResourceMappingDocument.ResourceMapping;
import org.ogce.schemas.gfac.documents.ApplicationDescriptionDocument;
import org.ogce.schemas.gfac.documents.ApplicationDescriptionType;
import org.ogce.schemas.gfac.documents.GlobusGatekeeperType;
import org.ogce.schemas.gfac.documents.GlobusJobManagerType;
import org.ogce.schemas.gfac.documents.HostDescriptionDocument;
import org.ogce.schemas.gfac.documents.HostDescriptionType;
import org.ogce.schemas.gfac.documents.MethodType;
import org.ogce.schemas.gfac.documents.PortTypeType;
import org.ogce.schemas.gfac.documents.ServiceMapType;
import org.ogce.schemas.gfac.documents.ServiceType.ServiceName;
import org.ogce.schemas.gfac.validator.SchemaValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GfacUtils {
    protected final static Logger log = LoggerFactory.getLogger(GfacUtils.class);
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
        FileWriter fw = null;
        try {
            fw = new FileWriter(file);
            // get the standard out of the application and write to file
            fw.write(data);
        } catch (IOException io) {
            throw io;
        } finally {
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException e) {
                    throw e;
                }
            }
        }
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

    public static String readFile(String file) throws GfacException {
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            byte[] content = new byte[in.available()];
            in.read(content);
            return new String(content);
        } catch (FileNotFoundException e) {
            throw new GfacException(e, FaultCode.InvaliedLocalArgumnet);
        } catch (IOException e) {
            throw new GfacException(e, FaultCode.LocalError);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    throw new GfacException(e, FaultCode.LocalError);
                }
            }
        }
    }

    public static void syncFileSystem(String host, GSSCredential gssCred) {
        try {
            GramAttributes gramAttr = new GramAttributes();
            gramAttr.setExecutable("/bin/sync");
            GramJob job = new GramJob(gramAttr.toRSL());
            job.setCredentials(gssCred);
            log.info("RSL = " + job.getRSL());
            try {
                Gram.request(host, job, false, false);
            } catch (Exception e) {
                String error = "Cannot launch GRAM job to sync filesystem. " + e.getMessage();
                log.error(error, e);
                throw new Exception(e);
            }

            int twoMin = 1000 * 60 * 2;
            long start = System.currentTimeMillis();

            while ((System.currentTimeMillis() - start) < twoMin) {
                int jobStatus = job.getStatus();

                // job finished successfully
                if (jobStatus == GRAMConstants.STATUS_DONE) {
                    log.info("Filesystem sync succeeded");
                    return;
                } else if (jobStatus == GRAMConstants.STATUS_FAILED) {
                    String error = "Filesystem sync failed";
                    log.info(error);
                    throw new Exception(error);
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {
                    log.error("Thread " + Thread.currentThread().getName() + " interrupted", ie);
                    try {
                        job.cancel();
                    } catch (Exception e) {
                        log.error("Exception cancelling job", e);
                    }
                }
            }
            log.info("Filesystem sync timed out");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static HostDescriptionType parseHostDescirption(String hostDescStr) throws GfacException {
        try {
            HostDescriptionType hostDesc = HostDescriptionDocument.Factory.parse(hostDescStr).getHostDescription();
            SchemaValidator validator = new SchemaValidator(hostDesc);
            validator.validate();
            return hostDesc;
        } catch (Exception e) {
            throw new GfacException(e, FaultCode.InvaliedLocalArgumnet);
        }
    }

    public static ApplicationDescriptionType parseAppDescirption(Reader reader) throws GfacException {
        try {
            ApplicationDescriptionType appDesc = ApplicationDescriptionDocument.Factory.parse(reader)
                    .getApplicationDescription();
            SchemaValidator validator = new SchemaValidator(appDesc);
            validator.validate();
            return appDesc;
        } catch (IOException e) {
            throw new GfacException(e, FaultCode.InvaliedLocalArgumnet);
        } catch (Exception e) {
            throw new GfacException(e, FaultCode.InvaliedLocalArgumnet);
        }
    }

    public static MethodType findOperationFromServiceMap(String operationName, ServiceMapType serviceMap)
            throws GfacException {
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

        throw new GfacException("Method name " + operationName + " not found", FaultCode.InvaliedLocalArgumnet);
    }

    public static MethodType findOperationWithApplication(ServiceMapType serviceMap) throws GfacException {
        MethodType[] methods = serviceMap.getPortTypeArray()[0].getMethodArray();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getApplication() != null) {
                return methods[i];
            }
        }
        return null;
    }

    public static boolean isLocalHost(String appHost) throws GfacException {
        try {
            String localHost = InetAddress.getLocalHost().getCanonicalHostName();

            if (localHost.equals(appHost) || GFacConstants.LOCALHOST.equals(appHost)
                    || GFacConstants._127_0_0_1.equals(appHost)) {
                return true;
            } else {
                return false;
            }
        } catch (UnknownHostException e) {
            throw new GfacException(e, FaultCode.LocalError);
        }
    }    

    public static boolean isInbuiltOperation(String name) {
        return GFacConstants.InbuitOperations.OP_KILL.equals(name)
                || GFacConstants.InbuitOperations.OP_PING.equals(name)
                || GFacConstants.InbuitOperations.OP_SHUTDOWN.equals(name);
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

    // public static String prettyPrint2String(XmlElement ele) {
    // try {
    // MXSerializer serializer = new MXSerializer();
    // StringWriter writer = new StringWriter();
    // serializer.setOutput(writer);
    // serializer.setProperty(PROPERTY_SERIALIZER_INDENTATION, INDENT);
    // XmlConstants.BUILDER.serialize(ele, serializer);
    // return writer.toString();
    // } catch (Exception e) {
    // e.printStackTrace();
    // return "Error happend pretty printing";
    // }
    // }

    // public static void prettyPrint(XmlElement ele) throws GfacException {
    // try {
    //
    // System.out.println(prettyPrint2String(ele));
    //
    // } catch (IllegalArgumentException e) {
    // throw new GfacException(e, FaultCode.InvaliedLocalArgumnet);
    // } catch (IllegalStateException e) {
    // throw new GfacException(e, FaultCode.InvaliedLocalArgumnet);
    // } catch (XmlBuilderException e) {
    // throw new GfacException(e, FaultCode.InvaliedLocalArgumnet);
    // }
    // }

    public static URI createGsiftpURI(ContactInfo host, String localPath) throws GfacException {
        try {
            StringBuffer buf = new StringBuffer();

            if (!host.hostName.startsWith("gsiftp://"))
                buf.append("gsiftp://");
            buf.append(host).append(":").append(host.port);
            if (!host.hostName.endsWith("/"))
                buf.append("/");
            buf.append(localPath);
            return new URI(buf.toString());
        } catch (URISyntaxException e) {
            throw new GfacException(e, FaultCode.InvaliedLocalArgumnet);
        }
    }

    public static URI createGsiftpURI(String host, String localPath) throws GfacException {
        try {
            StringBuffer buf = new StringBuffer();
            if (!host.startsWith("gsiftp://"))
                buf.append("gsiftp://");
            buf.append(host);
            if (!host.endsWith("/"))
                buf.append("/");
            buf.append(localPath);
            return new URI(buf.toString());
        } catch (URISyntaxException e) {
            throw new GfacException(e, FaultCode.InvaliedLocalArgumnet);
        }
    }

    public static Vector<URI> listLocalDir(String dirName, String localhost) throws GfacException {
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
                throw new GfacException("can not find the output data directory to list files",
                        FaultCode.InvaliedLocalArgumnet);
            }
            return files;
        } catch (URISyntaxException e) {
            throw new GfacException(e, FaultCode.InvaliedLocalArgumnet);
        }
    }

    public static QName getServiceNameFromServiceMap(ServiceMapType serviceMap) {
        ServiceName serviceName = serviceMap.getService().getServiceName();
        return new QName(serviceName.getTargetNamespace(), serviceName.getStringValue());
    }

    public static URI createWorkflowQName(QName name) throws GfacException {
        try {
            return new URI("urn:qname:" + name.getNamespaceURI() + ":" + name.getLocalPart());
        } catch (URISyntaxException e) {
            throw new GfacException(e, FaultCode.InvaliedLocalArgumnet);
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

    // public static String buildAnnotations(QName name, String value) {
    // XmlElement anno = builder.newFragment(name.getNamespaceURI(),
    // name.getLocalPart());
    // anno.addChild(value);
    // return builder.serializeToString(anno);
    // }

    public static QName findApplcationName(ServiceMapType serviceMap) throws GfacException {
        MethodType method = GfacUtils.findOperationWithApplication(serviceMap);

        if (method == null) {
            throw new GfacException("None of the methods has application defined", FaultCode.InvaliedLocalArgumnet);
        }

        String applicationName = method.getApplication().getApplicationName().getStringValue();
        String applicationNameNs = method.getApplication().getApplicationName().getTargetNamespace();
        return new QName(applicationNameNs, applicationName);
    }

    public static GlobusGatekeeperType findGateKeeper(ExecutionContext appExecContext, boolean wsgram)
            throws GfacException {
        WorkflowContextHeader header = appExecContext.getWorkflowHeader();
        boolean isSpruceEnabled = false;
        if (header != null) {
            isSpruceEnabled = (appExecContext.getWorkflowHeader().getURGENCY() != null);
            ResourceMapping resourceMapping = appExecContext.getWorkflowHeader().getResourceMappings()
                    .getResourceMappingArray()[0];
            if (resourceMapping != null) {
                URI gatekeeperfromResourceMapping = null;
                try {
                    gatekeeperfromResourceMapping = new URI(resourceMapping.getGatekeeperEpr());
                } catch (URISyntaxException e) {
                    throw new GfacException(e.getLocalizedMessage(), e);
                }
                if (gatekeeperfromResourceMapping != null) {
                    log.info("Gate keeper selected from resource mapping");
                    GlobusGatekeeperType gatekeeper = GlobusGatekeeperType.Factory.newInstance();

                    if (!resourceMapping.getWsgramPreferred()) {
                        if (resourceMapping.getJobManager() != null) {
                            throw new GfacException(
                                    "Job Manager parameter must not defined for Pre-WSGram in Resource Mapping, "
                                            + "include it with your gatekeepr EPR in resource mapping",
                                    FaultCode.InternalServiceError);
                        }
                    } else {
                        if (resourceMapping.getJobManager() != null) {
                            gatekeeper.setJobmanagertype(GlobusJobManagerType.Enum.forString(resourceMapping
                                    .getJobManager()));
                        }
                    }
                    gatekeeper.setEndPointReference(gatekeeperfromResourceMapping.toString());
                    return gatekeeper;
                }
            }
        }

        HostDescriptionType hostDesc = appExecContext.getExecutionModel().getHostDesc();
        GlobusGatekeeperType[] gatekeepers = hostDesc.getHostConfiguration().getGlobusGatekeeperArray();
        for (GlobusGatekeeperType gateKeeper : gatekeepers) {
            if (gateKeeper.getWsGram() == wsgram
                    && (isSpruceEnabled == gateKeeper.getJobmanagertype().equals(GlobusJobManagerType.SPRUCE))) {
                log.info((wsgram ? "WSGram" : "Gram ") + (!isSpruceEnabled ? "Non" : "") + " Spruce Gate keeper"
                        + gateKeeper.xmlText() + " selected");
                return gateKeeper;
            }
        }
        log.warn("Even though urgency header precent, there is no spruce job manager present. Moving on with non spruce job manager");
        for (GlobusGatekeeperType gateKeeper : gatekeepers) {
            if (gateKeeper.getWsGram() == wsgram) {
                log.info((wsgram ? "WSGram" : "Gram ") + (!isSpruceEnabled ? "Non" : "") + " Spruce Gate keeper"
                        + gateKeeper.xmlText() + " selected");
                return gateKeeper;
            }
        }
        return null;
    }

    public static String formatJobStatus(String jobid, String jobstatus) {
        return "Status of job " + jobid + "is " + jobstatus;
    }

    // public static GlobusGatekeeperType getSpruceGatekeeper(ExecutionContext
    // appExecContext) {
    // GlobusGatekeeperType spruceGatekeeper = null;
    //
    // HostDescriptionType hostDesc = appExecContext.getHostDesc();
    // GlobusGatekeeperType[] gatekeepers =
    // hostDesc.getHostConfiguration().getGlobusGatekeeperArray();
    //
    // if (gatekeepers != null && gatekeepers.length > 0) {
    // for (GlobusGatekeeperType gatekeeper : gatekeepers) {
    // if (gatekeeper.getJobmanagertype().equals(GlobusJobManagerType.SPRUCE)) {
    // spruceGatekeeper = gatekeeper;
    // break;
    // }
    // }
    // }
    //
    // return spruceGatekeeper;
    // }

    // public static String findServiceRegistryUrl(LeadContextHeader header){
    // if(header == null){
    // return null;
    // }else{
    // URI serviceRegistryUrl = header.getXRegistryUrl();
    // if(serviceRegistryUrl == null){
    // serviceRegistryUrl = header.getResourceCatalogUrl();
    // }
    // if(serviceRegistryUrl != null){
    // return serviceRegistryUrl.toString();
    // }else{
    // return null;
    // }
    // }
    // }

    public static String createRandomName(String name) {
        return name + System.currentTimeMillis() + "_" + tempFileCount.incrementAndGet();
    }

    public static Random getRandom() {
        return random;
    }

    public static boolean isArray(String typeName) {
        if (typeName.endsWith("Array")) {
            // TODO make it more tigter
            return true;
        } else {
            return false;
        }
    }
}
