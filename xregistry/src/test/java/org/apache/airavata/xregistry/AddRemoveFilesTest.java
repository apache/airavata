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

import java.util.List;

import org.apache.airavata.xregistry.context.GlobalContext;
import org.apache.airavata.xregistry.doc.DocData;
import org.apache.airavata.xregistry.impl.XregistryImpl;
import org.apache.airavata.xregistry.utils.Utils;
import org.apache.xmlbeans.impl.tool.XSTCTester.TestCase;



public class AddRemoveFilesTest extends TestCase{
    
    public void testAddRemoveAllDesc() throws Exception{
        GlobalContext globalContext = new GlobalContext(false);
        XregistryImpl mySqlRegistry = new XregistryImpl(globalContext);
//        
        //String user = "/C=US/O=National Center for Supercomputing Applications/CN=Hemapani Srinath Perera";
        String user = "/O=LEAD Project/OU=Indiana University Extreme Lab/OU=linbox1.extreme.indiana.edu/OU=extreme.indiana.edu/CN=sshirasu/Email=sshirasu@cs.indiana.edu";
        
        List<DocData> list = mySqlRegistry.findServiceDesc(Utils.canonicalizeDN(user), "");
        for(DocData dataitem:list){
            System.out.println(dataitem.name);
        }
        
        
//        String otherUser = "/C=US/O=National Center for Supercomputing Applications/CN=Suresh Marru";
//        
//        String hostName = "rainier.extreme.indiana.edu";
//        mySqlRegistry.registerHostDesc(user, Utils.readFile("samples/hosts/rainier.xml"));
//        String hostDesc = mySqlRegistry.getHostDesc(otherUser, "rainier.extreme.indiana.edu");
//        System.out.println(hostDesc);
//        assertNotNull(hostDesc);
//        String[] hostList = mySqlRegistry.findHosts(otherUser, "rainier.extreme.indiana.edu");
//        TestUtils.testCantainment(hostList, "rainier.extreme.indiana.edu");
//        
//        hostList = mySqlRegistry.findHosts(otherUser, "");
//        TestUtils.testCantainment(hostList, "rainier.extreme.indiana.edu");
//        TestUtils.printList(hostList);
//        mySqlRegistry.removeHostDesc(user, hostName);
//        
//        String appName = new QName("http://www.extreme.indiana.edu/lead","FILEBREED").toString();
//         mySqlRegistry.registerAppDesc(user, Utils.readFile("samples/apps/filebreed-rainier.xml"));
//      String appDesc = mySqlRegistry.getAppDesc(otherUser,appName, "rainier.extreme.indiana.edu");
//      System.out.println(appDesc);
//      assertNotNull(appDesc);
//      String[][] appList = mySqlRegistry.findAppDesc(otherUser, "FILEBREED");
//      TestUtils.printListArray(appList);  
//      TestUtils.testCantainmentArray(appList, appName);
//      
//      appList = mySqlRegistry.findAppDesc(otherUser, "");
//      TestUtils.printListArray(appList);  
//        mySqlRegistry.removeAppDesc(user, appName, hostName);
//        
//        String serviceName = new QName("http://www.extreme.indiana.edu/lead","FilebreedServiceDrLead").toString();
//      String serviceMapAsStr = Utils.readFile("samples/services/service-filebreed-drlead.xml");
//      mySqlRegistry.registerServiceDesc(user, serviceMapAsStr,DocParser.createAWsdl(serviceMapAsStr));
//      String serviceDesc = mySqlRegistry.getServiceDesc(otherUser,serviceName );
//      System.out.println(serviceDesc);
//      assertNotNull(serviceDesc);
//      String[] serviceList = mySqlRegistry.findServiceDesc(otherUser, "Filebreed");
//      TestUtils.testCantainment(serviceList, serviceName);
//      
//      hostList = mySqlRegistry.findServiceDesc(otherUser, "");
//      TestUtils.testCantainment(serviceList, serviceName);
//      TestUtils.printList(hostList);
//      
//      String awsdl = mySqlRegistry.getAbstractWsdl(otherUser, serviceName);
//      System.out.println(awsdl);
//        mySqlRegistry.removeServiceDesc(user, serviceName);
        
    }
    
    
 
    
    
//    public void xtestAddHostDesc() throws XregistryException{
//        GlobalContext globalContext = new GlobalContext();
//        String user = "/C=US/O=National Center for Supercomputing Applications/CN=Hemapani Srinath Perera";
//        String user1 = "/C=US/O=National Center for Supercomputing Applications/CN=Suresh Marru";
//        //String hostName = "bigred.teragrid.iu.edu";
//        DocRegistryImpl docRegistryImpl = new DocRegistryImpl(globalContext);
////        String resourceID = docRegistryImpl.registerHostDesc(user, Utils.readFile("samples/hosts/chinkapin.xml"));
////        System.out.println(resourceID);
////        String hostDescAsStr = docRegistryImpl.getHostDesc(user, "chinkapin.cs.indiana.edu");
////        assertNotNull(hostDescAsStr);
////        System.out.println(hostDescAsStr);
//        docRegistryImpl.removeHostDesc(user, "chinkapin.cs.indiana.edu");
//        
////        String resourceID = "urn:hostdesc:bigred.teragrid.iu.edu";
//////        docRegistryImpl.authorizeResouce(user, resourceID, "extreme", false, AuthConstants.ACTION_ALL);
////        
////        
////        GroupManager groupManager = new GroupManager(globalContext);
////        AuthorizerImpl authorizer = new AuthorizerImpl(globalContext,groupManager);
////        assertTrue(authorizer.isAuthorized(user1, resourceID, AuthConstants.ACTION_READ, false));
//        
//    }
}

