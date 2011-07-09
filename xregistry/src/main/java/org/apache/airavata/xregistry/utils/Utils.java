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
 package org.apache.airavata.xregistry.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.airavata.xregistry.XregistryException;
import org.apache.airavata.xregistry.context.GlobalContext;
import org.apache.airavata.xregistry.doc.DocData;
import org.globus.gsi.CertUtil;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.GlobusCredentialException;
import org.globus.gsi.TrustedCertificates;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.gsi.ptls.PureTLSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.xmlpull.mxp1_serializer.MXSerializer;
import org.xmlpull.v1.builder.XmlBuilderException;
import org.xmlpull.v1.builder.XmlElement;

import xsul.XmlConstants;
import xsul.XsulException;
import xsul.invoker.gsi.GsiInvoker;
import xsul.invoker.puretls.PuretlsInvoker;
import xsul.invoker.soap_over_http.SoapHttpDynamicInfosetInvoker;
import xsul.wsdl.WsdlDefinitions;
import xsul.wsif.WSIFException;
import xsul.wsif.WSIFService;
import xsul.wsif.WSIFServiceFactory;
import xsul.wsif_xsul_soap_gsi.Provider;
import xsul.wsif_xsul_soap_http.XsulSoapPort;
import xsul.xwsif_runtime.WSIFClient;
import xsul.xwsif_runtime.XmlBeansWSIFRuntime;
import COM.claymoresystems.sslg.SSLPolicyInt;

public class Utils {
    private final static String PROPERTY_SERIALIZER_INDENTATION = "http://xmlpull.org/v1/doc/properties.html#serializer-indentation";

    private final static String INDENT = "    ";

    public static String readFromStream(InputStream in) throws XregistryException {
        try {
            StringBuffer wsdlStr = new StringBuffer();

            int read;

            byte[] buf = new byte[1024];
            while ((read = in.read(buf)) > 0) {
                wsdlStr.append(new String(buf, 0, read));
            }
            in.close();
            return wsdlStr.toString();
        } catch (IOException e) {
            throw new XregistryException(e);
        }
    }

    public static String readFile(String file) throws XregistryException {
        try {
            FileInputStream in = new FileInputStream(file);
            byte[] content = new byte[in.available()];
            in.read(content);
            in.close();
            return new String(content);
        } catch (FileNotFoundException e) {
            throw new XregistryException(e);
        } catch (IOException e) {
            throw new XregistryException(e);
        }
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

    public static void prettyPrint(XmlElement ele) throws XregistryException {
        try {

            System.out.println(prettyPrint2String(ele));

        } catch (IllegalArgumentException e) {
            throw new XregistryException(e);
        } catch (IllegalStateException e) {
            throw new XregistryException(e);
        } catch (XmlBuilderException e) {
            throw new XregistryException(e);
        }
    }
    
    public static WSIFClient createWSIFClient(GlobalContext globalConfiguration,
            String serviceURL) throws XregistryException {
        WSIFClient client;
        try {
            if (serviceURL.startsWith("https")) {
                boolean useHostKey = true;

                if (globalConfiguration == null) {
                    throw new XregistryException(
                            "To make Secure WSIF client Global configuration must nor be Null");
                }
                SoapHttpDynamicInfosetInvoker invoker = createSecureInvoker(globalConfiguration,
                        useHostKey);
                String wsdlAsStr = invoker.invokeHttpGet(serviceURL);
                // System.out.println(wsdlAsStr);
                XmlElement el = XmlConstants.BUILDER
                        .parseFragmentFromReader(new StringReader(wsdlAsStr));
                WsdlDefinitions def = new WsdlDefinitions(el);
                
                
                WSIFServiceFactory wsf = WSIFServiceFactory.newInstance();
                WSIFService serv = wsf.getService(def);
                serv.addLocalProvider(new Provider(invoker));
                client = XmlBeansWSIFRuntime.getDefault().newClientFor(serv.getPort());
                ((XsulSoapPort) client.getPort()).setInvoker(invoker);
                
                
                
//                WsdlResolver wsdlResolver = WsdlResolver.getInstance();
//                wsdlResolver.setSecureInvoker(invoker);
//                
////                String wsdlAsStr = invoker.invokeHttpGet(serviceURL);
////                // System.out.println(wsdlAsStr);
////                XmlElement el = XmlConstants.BUILDER
////                        .parseFragmentFromReader(new StringReader(wsdlAsStr));
////                WsdlDefinitions def = new WsdlDefinitions(el);
//                WsdlDefinitions def = wsdlResolver.loadWsdl(new URI(serviceURL));
//                
//                WSIFServiceFactory wsf = WSIFServiceFactory.newInstance();
//                WSIFService serv = wsf.getService(def);
//                serv.addLocalProvider(new Provider(invoker));
//                client = XmlBeansWSIFRuntime.getDefault().newClientFor(serv.getPort());
//                ((XsulSoapPort) client.getPort()).setInvoker(invoker);
            } else {
                client = XmlBeansWSIFRuntime.newClient(serviceURL);
            }
        } catch (WSIFException e) {
            throw new XregistryException(e);
        } catch (XsulException e) {
            throw new XregistryException(e);
        } catch (XmlBuilderException e) {
            throw new XregistryException(e);
        } catch (IOException e) {
            throw new XregistryException(e);
        } catch (GeneralSecurityException e) {
            throw new XregistryException(e);
        } catch (GSSException e) {
            throw new XregistryException(e);
        }
        return client;
    }

    public static SoapHttpDynamicInfosetInvoker createSecureInvoker(
            GlobalContext globalConfiguration, boolean useHostKey) throws GeneralSecurityException,
            FileNotFoundException, IOException, XregistryException, GSSException {
        String certFile = globalConfiguration.getTrustedCertsFile();
        String keyfile = globalConfiguration.getHostcertsKeyFile();

        SoapHttpDynamicInfosetInvoker invoker;
        if(useHostKey && globalConfiguration.getTrustedCertificates() != null && keyfile != null){
            PureTLSContext ctx = new PureTLSContext();
            ctx.setTrustedCertificates(globalConfiguration.getTrustedCertificates());
            ctx.loadEAYKeyFile(keyfile, "");
            SSLPolicyInt policy = new SSLPolicyInt();
            policy.requireClientAuth(true);
            policy.setAcceptNoClientCert(true);
            ctx.setPolicy(policy);
            invoker = new PuretlsInvoker(ctx);
        }else if(useHostKey && certFile != null && keyfile != null && new File(certFile).isFile()){
            invoker = new PuretlsInvoker(keyfile, "", certFile);
        }else {
            X509Certificate[] certs = globalConfiguration.getTrustedCertificates();
            if(certs != null && certFile != null){
            	TrustedCertificates certificates = TrustedCertificates.load(certFile);
        		TrustedCertificates.setDefaultTrustedCertificates(certificates);
            	certs = certificates.getCertificates();
            }else{
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                InputStream trustedCasInStream = cl.getResourceAsStream("xregistry/trusted_cas.pem");
                if(trustedCasInStream != null){
                    File tempTrustedCas = File.createTempFile("trusted_cas","pem");
                    FileOutputStream tempTrustedCasOut = new FileOutputStream(tempTrustedCas);
                    tempTrustedCasOut.write(Utils.readFromStream(trustedCasInStream).getBytes());
                    tempTrustedCasOut.close();
                    certs = CertUtil.loadCertificates(tempTrustedCas.getAbsolutePath());
                    tempTrustedCas.deleteOnExit();
                }else{
                    throw new XregistryException("Server is secured, but can not find trusted certificates file");        
                }
            }
            GSSCredential credential = globalConfiguration.getCredential();
            invoker = new GsiInvoker(credential,certs);
            globalConfiguration.setUserDN(credential.getName().toString());
        }
        return invoker;
    }
    
    public static ArrayList<String> fromArrayToList(String[] vals){
        if(vals == null){
            return null;
        }
        ArrayList<String > list =  new ArrayList<String>(vals.length);
        for(String val:vals){
            list.add(val);
        }
        return list;
    }
    
    public static GSSCredential createCredentials() throws XregistryException {
        try {
            // load the x509 proxy. if not found, quit.
            String proxyPath = System.getProperty("X509_USER_PROXY");
            System.out.println("Proxy location = " + proxyPath);
            GlobusCredential globusCred;
            GSSCredential gssCred;

            // if delegated proxy is used, then the proxy path is denoted by the
            // env variable X509_USER_PROXY
            if (proxyPath != null && !"".equals(proxyPath)) {
                globusCred = new GlobusCredential(proxyPath);
            } else {
                globusCred = GlobusCredential.getDefaultCredential();
            }
            globusCred.verify();
            gssCred = getGSSCredential(globusCred);

            return gssCred;
        } catch (GlobusCredentialException e) {
            throw new XregistryException(e);
        } catch (Exception e) {
            throw new XregistryException(e);
        }
    }
    public static GSSCredential getGSSCredential(GlobusCredential globusCred) throws Exception {
        return new GlobusGSSCredentialImpl(globusCred, GSSCredential.INITIATE_AND_ACCEPT);
    }
    
    public static String[] toStrListToArray(List<String> array){
        if(array == null || array.size() == 0){
            return null;
        }
        return array.toArray(new String[0]);
    }
    
    public static String[][] toStrArrayListToArray(ArrayList<String[]> array){
        if(array == null || array.size() == 0){
            return null;
        }
        return array.toArray(new String[0][0]);
    }
    
    
    public static boolean isSameDN(String dn1,String dn2){
        dn1 = canonicalizeDN(dn1);
        dn2 = canonicalizeDN(dn2);
        return dn1.equals(dn2);
    }
    
    public static String canonicalizeDN(String dn){
        if(!dn.startsWith("/")){
            dn =   "/" + dn;
        }
        dn = dn.toLowerCase();
        dn = dn.replaceAll(",", "/");
        dn = dn.replace("/cn=proxy", "");
        dn = dn.replace("/ou=people", "");
        
        
        StringBuffer normalizedDn = new StringBuffer();
        StringTokenizer tn = new StringTokenizer(dn,"/");
        boolean foundDn = false;
        while(tn.hasMoreTokens()){
            String t = tn.nextToken();
            if(t.startsWith("cn=")){
                if(!foundDn){
                    foundDn = true;
                    normalizedDn.append("/");
                    normalizedDn.append(t);
                }
            }else{
                normalizedDn.append("/");
                normalizedDn.append(t);
            }
        }
        return normalizedDn.toString();
    }
    
    public static String findStringProperty(Properties config, String name, String defaultVal) {
        String value = config.getProperty(name);
        if (value == null) {
            value = defaultVal;
        }
        return value;
    }
    
    public static int findIntegerProperty(Properties config, String name, int defaultVal) {
        int value;
        String strValue = config.getProperty(name);
        if (strValue == null) {
            value = defaultVal;
        }else{
            value = Integer.parseInt(strValue.trim());
        }
        return value;
    }
    
    public static boolean findBooleanProperty(Properties config, String name, boolean defaultVal) {
        boolean value;
        String strValue = config.getProperty(name);
        if (strValue == null) {
            value = defaultVal;
        }else{
            value = Boolean.parseBoolean(strValue);
        }
        return value;
    }
 
    public static String[] docData2String(DocData[] docData){
        if(docData == null){
            return null;
        }
        String[] results = new String[docData.length];
        for(int i = 0;i<docData.length;i++){
            results[i] = docData[i].resourceID.toString();
        }
        return results;
    }   
    
    
    
}
