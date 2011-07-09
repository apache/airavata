/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.airavata.xregistry.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;

import org.apache.airavata.xregistry.XregistryConstants;
import org.apache.airavata.xregistry.XregistryException;
import org.apache.airavata.xregistry.context.GlobalContext;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.TrustedCertificates;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.gsi.ptls.PureTLSContext;
import org.ietf.jgss.GSSCredential;

import xsul.MLogger;
import xsul.http_server.HttpServerException;
import xsul.http_server.ServerSocketFactory;
import xsul.processor.DynamicInfosetProcessorException;
import xsul.puretls_server_socket_factory.PuretlsServerSocketFactory;
import xsul.xservices_xbeans.XmlBeansBasedService;
import xsul.xservo.XService;
import xsul.xservo_soap_http.HttpBasedServices;
import COM.claymoresystems.sslg.SSLPolicyInt;

public class XregistryServer {
    static{
        System.setProperty("log",XregistryConstants.LOGGER_NAME+":ALL");
        System.setProperty("showtime","false");
        System.setProperty("log.multiline","true");
    }
    protected static MLogger log = MLogger.getLogger(XregistryConstants.LOGGER_NAME);
    private XService cmsvc;

    public XregistryServer(GlobalContext globalContext) throws XregistryException {
        try {
            int port = globalContext.getPort();
            PureTLSContext ctx = null;
            String trustedCertsFile = globalContext.getTrustedCertsFile();
            String certKeyFile = globalContext.getHostcertsKeyFile();
            
            //try to load host certificate
            if(globalContext.isSecurityEnabled()){
                if(certKeyFile != null){
                    ctx = new PureTLSContext();
                    if(globalContext.getTrustedCertificates() != null){
                        ctx.setTrustedCertificates(globalContext.getTrustedCertificates());
                    }else if(trustedCertsFile != null && new File(trustedCertsFile).isFile()){
                        ctx.loadRootCertificates(trustedCertsFile);    
                    }else if(trustedCertsFile != null && new File(trustedCertsFile).isDirectory()){
                    	TrustedCertificates certificates = TrustedCertificates.load(trustedCertsFile);
                		TrustedCertificates.setDefaultTrustedCertificates(certificates);
                    	ctx.setTrustedCertificates(certificates.getCertificates());    
                    }
                    else{
                        TrustedCertificates tc = TrustedCertificates.getDefaultTrustedCertificates();
                        ctx.setTrustedCertificates(tc.getCertificates());
                    }
                    ctx.loadEAYKeyFile(certKeyFile, "");
                    SSLPolicyInt policy = new SSLPolicyInt();
                    policy.requireClientAuth(true);
                    policy.setAcceptNoClientCert(true);
                    ctx.setPolicy(policy);
                }else{
                    //Use Globous crednatials if it is there
                    try {
                        ctx = new PureTLSContext();
                        GSSCredential gssCredntial = globalContext.getCredential();
                        if(gssCredntial instanceof GlobusGSSCredentialImpl){
                            GlobusCredential globusCred = ((GlobusGSSCredentialImpl)gssCredntial).getGlobusCredential();
                            TrustedCertificates tc = TrustedCertificates.getDefaultTrustedCertificates();
                            if (tc == null)
                            {
                                throw new XregistryException("Trusted certificates is null");
                            }
                            X509Certificate[] certs = tc.getCertificates();
                            ctx.setTrustedCertificates(certs);
                            ctx.setCredential(globusCred);
                        }else{
                            throw new XregistryException("Can not find the credantial to start a secure server");
                        }
                    } catch (RuntimeException e) {
                        throw new XregistryException("Secuirty is enabled, but no credentials found");
                    }
                }

            }
            //This is to provide rest support
//            HttpBasedServices httpServices;
//            XregistryXmlBeansWrapper xregistryXmlBeansWrapper = new XregistryXmlBeansWrapper(globalContext);
//            if(ctx != null){
//                ServerSocketFactory secureSocketFactory = new PuretlsServerSocketFactory(port, ctx);
//                httpServices = new XregistryHttpbasedServices(secureSocketFactory,xregistryXmlBeansWrapper.getRegistryImpl());
//            }else{
//                httpServices = new XregistryHttpbasedServices(port,xregistryXmlBeansWrapper.getRegistryImpl());
//            }
//            String cwsdlLoc = Thread.currentThread().getContextClassLoader().getResource("xregistry.wsdl").toString();
//            
//
//            ExtendedXbeanBasedService service = new ExtendedXbeanBasedService("xregistry", cwsdlLoc,
//                    xregistryXmlBeansWrapper);
//            cmsvc = httpServices.addService(service);
//            service.addHandler(new xsul.xhandler_context.ServerContextAccessHandler("service-context"));
//            //service.startService();
//            service.initManagmentAgent();
            
            XregistryXmlBeansWrapper xregistryXmlBeansWrapper = new XregistryXmlBeansWrapper(globalContext);
            HttpBasedServices httpServices;
            if(ctx != null){
                ServerSocketFactory secureSocketFactory = new PuretlsServerSocketFactory(port, ctx);
                httpServices = new XregistryHttpbasedServices(secureSocketFactory,xregistryXmlBeansWrapper.getRegistryImpl());
            }else{
                httpServices = new XregistryHttpbasedServices(port,xregistryXmlBeansWrapper.getRegistryImpl());
            }
            String cwsdlLoc = Thread.currentThread().getContextClassLoader().getResource("xregistry.wsdl").toString();
            
            
            XmlBeansBasedService xbeanBasedService = new XmlBeansBasedService("xregistry", cwsdlLoc,
                    xregistryXmlBeansWrapper);
            cmsvc = httpServices.addService(xbeanBasedService)
            .addHandler(new xsul.xhandler_context.ServerContextAccessHandler("service-context"));
            
            System.out.println("Server started on "+httpServices.getServer().getLocation());
        } catch (HttpServerException e) {
            throw new XregistryException(e);
        } catch (DynamicInfosetProcessorException e) {
            throw new XregistryException(e);
        } catch (FileNotFoundException e) {
            throw new XregistryException(e);
        } catch (IOException e) {
            throw new XregistryException(e);
        } catch (GeneralSecurityException e) {
            throw new XregistryException(e);
        }
    }

    public void start() {
        cmsvc.startService();
    }
    
    public static void main(String[] args){
        try {
            GlobalContext globalContext;
            if(args.length > 0){
                globalContext = new GlobalContext(false,args[0]);
            }else{
                globalContext = new GlobalContext(false);
            }
            XregistryServer xregistryService = new XregistryServer(globalContext);
            xregistryService.start();
        } catch (XregistryException e) {
            log.caught(e);
            //Killing off everything
            System.exit(0);
        }
    }

}
