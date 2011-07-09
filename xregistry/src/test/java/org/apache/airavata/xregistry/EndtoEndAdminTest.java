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
//import javax.xml.namespace.QName;
//
//import xregistry.XregistryException;
//import xregistry.client.DocumentRegistryClient;
//import xregistry.context.GlobalContext;
//import xregistry.doc.DocParser;
//import xregistry.impl.XregistryServer;
//import xregistry.utils.Utils;
//
//
//
//public class EndtoEndAdminTest extends AbstractXregistryTestCase{
//
//    @Override
//    protected void setUp() throws Exception {
//        super.setUp();
//        Thread t = new Thread(new Runnable() {
//            public void run() {
//                    XregistryServer.main(new String[]{});
//            }
//        
//        });
//        t.setDaemon(true);
//        t.start();
//        Thread.sleep(2000);
//    }
//    
//    
////    public void testListing() throws XregistryException{
////        GlobalContext context =  new GlobalContext(true);
////        DocumentRegistryClient client = new DocumentRegistryClient(context,"http://linbox3.extreme.indiana.edu:6666/xregistry?wsdl");
////        ArrayList<String> data = client.findHosts("");
////        TestUtils.printList(data);
////        
////        data = client.findServiceDesc("");
////        TestUtils.printList(data);
////        
////        
////        data = client.findServiceInstance("");
////        TestUtils.printList(data);
////        
////        
////        
////        
////        
////        ArrayList<String[]> apps = client.findAppDesc("");
////        TestUtils.printListArray(apps);
////    }
//    
//    
//    public void testAddremoveDocTest() throws Exception{
//        GlobalContext context =  new GlobalContext(true);
//        DocumentRegistryClient client = new DocumentRegistryClient(context,"http://linbox3.extreme.indiana.edu:6666/xregistry?wsdl");
//        String user = "/C=US/O=National Center for Supercomputing Applications/CN=Hemapani Srinath Perera";
//        String otherUser = "/C=US/O=National Center for Supercomputing Applications/CN=Suresh Marru";
//       String[] hostList;
//        String hostName = "rainier.extreme.indiana.edu";
//       client.registerHostDesc( Utils.readFile("samples/hosts/rainier.xml"));
//        String hostDesc = client.getHostDesc("rainier.extreme.indiana.edu");
//        System.out.println(hostDesc);
//        assertNotNull(hostDesc);
//        hostList = client.findHosts( "rainier.extreme.indiana.edu");
//        TestUtils.testCantainment(hostList, "rainier.extreme.indiana.edu");
//        
//        hostList = client.findHosts( "");
//        TestUtils.testCantainment(hostList, "rainier.extreme.indiana.edu");
//        TestUtils.printList(hostList);
//       
//        client.removeHostDesc( hostName);
//        
//        String appName = new QName("http://www.extreme.indiana.edu/lead","FILEBREED").toString();
//         client.registerAppDesc( Utils.readFile("samples/apps/filebreed-rainier.xml"));
//      String appDesc = client.getAppDesc(appName, "rainier.extreme.indiana.edu");
//      System.out.println(appDesc);
//      assertNotNull(appDesc);
//      String[][] appList = client.findAppDesc( "FILEBREED");
//      TestUtils.printListArray(appList);  
//      TestUtils.testCantainmentArray(appList, appName);
//      
//      appList = client.findAppDesc( "");
//      TestUtils.printListArray(appList);  
//        client.removeAppDesc(appName, hostName);
//        
//        String serviceName = new QName("http://www.extreme.indiana.edu/lead","FilebreedServiceDrLead").toString();
//      String serviceMapAsStr = Utils.readFile("samples/services/service-filebreed-drlead.xml");
//      client.registerServiceDesc(serviceMapAsStr,DocParser.createAWsdl(serviceMapAsStr));
//      String serviceDesc = client.getServiceDesc(serviceName );
//      System.out.println(serviceDesc);
//      assertNotNull(serviceDesc);
//      String[] serviceList = client.findServiceDesc( "Filebreed");
//      TestUtils.testCantainment(serviceList, serviceName);
//      
//      hostList = client.findServiceDesc( "");
//      TestUtils.testCantainment(serviceList, serviceName);
//      TestUtils.printList(hostList);
//      
//      String awsdl = client.getAbstractWsdl( serviceName);
//      System.out.println(awsdl);
//        client.removeServiceDesc(serviceName);
//    }
//    
//
//}
//
