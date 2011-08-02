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
package org.apache.airavata.xregistry;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.airavata.xregistry.client.DocumentRegistryClient;
import org.apache.airavata.xregistry.context.GlobalContext;
import org.apache.airavata.xregistry.doc.AppData;
import org.apache.airavata.xregistry.doc.DocData;
import org.apache.airavata.xregistry.doc.DocParser;
import org.apache.airavata.xregistry.utils.ProxyRenewer;
import org.apache.airavata.xregistry.utils.Utils;
import org.globus.myproxy.MyProxy;



public class FunctionalTest extends TestCase{
    //private String regsitryURL = "https://tyr11.cs.indiana.edu:6666/xregistry?wsdl";
    //private String regsitryURL = "https://tyr14.cs.indiana.edu:6666/xregistry?wsdl";
    private String regsitryURL = "https://linbox3.extreme.indiana.edu:6666/xregistry?wsdl";
    
    public void testStatelessOperations() throws XregistryException{
        
        ProxyRenewer renewer = new ProxyRenewer("hperea","hperera1234",MyProxy.DEFAULT_PORT,14400,"portal.leadproject.org");
        GlobalContext context = new GlobalContext(true);
        context.setHostcertsKeyFile("/etc/lead/certificates/hostcertkey.pem.hperera");
        //context.setCredential(renewer.renewProxy());
        DocumentRegistryClient client = new DocumentRegistryClient(context,regsitryURL);
        DocData[] data;
        data  = client.findHosts("");
        if(data != null){
            for(DocData host:data){
              verifyAllFeildsNotNull(host);
              assertNotNull(client.getHostDesc(host.name.toString()));
          }
        }
        data = client.findServiceDesc("");
        if(data != null){
            for(DocData item:data){
              verifyAllFeildsNotNull(item);
              assertNotNull(client.getServiceDesc(item.name));
          }
        }
        data = client.findServiceInstance("");
        if(data != null){
            for(DocData item:data){
              verifyAllFeildsNotNull(item);
              assertNotNull(client.getConcreateWsdl(item.name));
          }
        }
        AppData[] appdata = client.findAppDesc("");
        if(data != null){
            for(AppData item:appdata){
              verifyAllFeildsNotNull(item);
              assertNotNull(client.getAppDesc(item.name.toString(),item.secondryName));
          }
        }        
        data = client.findResource("");
        if(data != null){
            for(DocData item:data){
              verifyAllFeildsNotNull(item);
              assertNotNull(client.getResource(item.name));
          }
        }
        
        QName nonExisitsResource = new QName("nonExisitsResource");
        assertNull(client.getAppDesc(nonExisitsResource.toString(), "foo"));
        assertNull(client.getHostDesc("foo123456"));
        assertNull(client.getServiceDesc(nonExisitsResource));
        assertNull(client.getConcreateWsdl(nonExisitsResource));
        assertNull(client.getResource(nonExisitsResource));

//        //client.registerResource(new QName("AFoo"), "bar");
//        //System.out.println(client.getResource(new QName("AFoo")));
//        client.removeResource(new QName("AFoo"));
//        DocData[] data = client.findResource("");
//        if(data != null){
//            for(DocData user:data){
//                System.out.println(user.name);
//            }
//        }
        //CWSDL update the if there is a copy exists, so we keep it in statelss tests
        String wsdlAsStr = DocParser.createWsdl(Utils.readFile("samples/services/service-filebreed-drlead.xml"), false);
        //client.registerConcreteWsdl(wsdlAsStr, 1000);
        //make sure you can update the Cwsdl
       // client.registerConcreteWsdl(wsdlAsStr, 1000);

    }
    

    
    
    private void verifyAllFeildsNotNull(DocData docData){
        assertNotNull(docData.name);
        assertNotNull(docData.resourceID);
        assertNotNull(docData.allowedAction);
        assertNotNull(docData.owner);
        if(docData instanceof AppData){
            assertNotNull(((AppData)docData).secondryName);
        }
    }
    
//    public void testAdminOperations() throws XregistryException{
//        ProxyRenewer renewer = new ProxyRenewer("hperera","hperera1234",MyProxy.DEFAULT_PORT,14400,"portal.leadproject.org");
//        GlobalContext context = new GlobalContext(true);
//        context.setCredential(renewer.renewProxy());
//        AdminClient client = new AdminClient(context,regsitryURL);
//        
//        String[] users = client.listGroups();
//        assertNotNull(users);
//        for(String user:users){
//            client.listGroupsGivenAUser(user);
//        }
//        String[] groups = client.listGroups();
//        assertNotNull(groups);
//        for(String group:groups){
//            client.listSubActorsGivenAGroup(group);
//    }
//        
//        //client.registerResource(new QName("AFoo"), "bar");
//        //System.out.println(client.getResource(new QName("AFoo")));
//        client.removeResource(new QName("AFoo"));
//        DocData[] data = client.findResource("");
//        if(data != null){
//            for(DocData user:data){
//                System.out.println(user.name);
//            }
//        }
  //  }
    
    
    
//    public void testAuthorization() throws Exception{
//        DocumentRegistryClient client1 = new DocumentRegistryClient(createContext("hperera", "hperera1234"),regsitryURL);
//        DocumentRegistryClient client2 = new DocumentRegistryClient(createContext("gfac", "gfac1234"),regsitryURL);
//        QName resourceName = new QName("Resource"+System.currentTimeMillis());
//        client1.registerResource(resourceName,"");
//        assertTrue(hasResource(resourceName, client1.findResource("")));
//        assertFalse(hasResource(resourceName, client2.findResource("")));
//        
//        client1.addCapability(resourceName.toString(), "lead", false, XregistryConstants.Action.Read.toString());
//        assertTrue(hasResource(resourceName, client2.findResource("")));
//        
//        client1.removeCapability(resourceName.toString(), "lead");
//        assertFalse(hasResource(resourceName, client2.findResource("")));
//        client1.removeResource(resourceName);
//        
//        QName otherresourceName = new QName("Resource2"+System.currentTimeMillis());
//        client2.registerResource(otherresourceName, "");
//        assertTrue(hasResource(otherresourceName, client2.findResource(otherresourceName.toString())));
//        assertFalse(hasResource(otherresourceName, client1.findResource(otherresourceName.toString())));
//        
//        String user1 = "/o=lead project/ou=portal.leadproject.org/ou=cs.indiana.edu/cn=hperera/email=hperera@cs.indiana.edu";
//        client2.addCapability(otherresourceName.toString(),user1 , 
//                true, XregistryConstants.Action.Read.toString());
//        assertTrue(hasResource(otherresourceName, client1.findResource(otherresourceName.toString())));
//        client2.removeCapability(otherresourceName.toString(), user1);
//        assertFalse(hasResource(otherresourceName, client1.findResource(otherresourceName.toString())));
//        client2.removeResource(otherresourceName);
//        
//    }
    
    
    private GlobalContext createContext(String userName,String passwd) throws XregistryException{
        ProxyRenewer renewer = new ProxyRenewer(userName,passwd,MyProxy.DEFAULT_PORT,14400,"portal.leadproject.org");
        GlobalContext context = new GlobalContext(true);
        context.setCredential(renewer.renewProxy());
        return context;
    }
    
    
    private boolean hasResource(QName name,DocData[] datalist){
        if(datalist == null){
            return false;
        }
        for(DocData dataItem:datalist){
            if(dataItem.name.equals(name)){
                return true;
            }
        }
        return false;
    }
    
    
    
    
    
    
    
    
    
//    public void testConnectUsingGridCredendials() throws Exception{
//        ProxyRenewer renewer = new ProxyRenewer("hperera","hperera1234",MyProxy.DEFAULT_PORT,14400,"portal.leadproject.org");
//        
//        WSIFClient client;
//        String certFile = "/etc/lead/certificates/iu_services_ca.pem";
//        //String wsdlUrl = "https://129.79.246.108:20443/resource_catalog?wsdl";
//        String wsdlUrl = "https://tyr14.cs.indiana.edu:6666/xregistry?wsdl";
//        
//        
//        GSSCredential credential = renewer.renewProxy();
//        
//        SoapHttpDynamicInfosetInvoker invoker;
//            //invoker = new PuretlsInvoker(keyfile, "", certFile);
//        X509Certificate[] certs = CertUtil.loadCertificates(certFile);
//        invoker = new GsiInvoker(credential,certs);
//        
//        WsdlResolver wsdlResolver = WsdlResolver.getInstance();
//        wsdlResolver.setSecureInvoker(invoker);
//        
//        WsdlDefinitions def = wsdlResolver.loadWsdl(new URI(wsdlUrl));
//        
//        WSIFServiceFactory wsf = WSIFServiceFactory.newInstance();
//        WSIFService serv = wsf.getService(def);
//        serv.addLocalProvider(new Provider(invoker));
//        client = XmlBeansWSIFRuntime.getDefault().newClientFor(serv.getPort());
//        ((XsulSoapPort) client.getPort()).setInvoker(invoker);
//        XregistryPortType proxy = (XregistryPortType)client.generateDynamicStub(XregistryPortType.class);
//        System.out.println(proxy);
//    }
    
    
    
}

