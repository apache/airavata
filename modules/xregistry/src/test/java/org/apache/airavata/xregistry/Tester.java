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

import org.apache.airavata.xregistry.client.DocumentRegistryClient;
import org.apache.airavata.xregistry.context.GlobalContext;
import org.apache.airavata.xregistry.utils.Utils;

import edu.indiana.extreme.gfac.utils.GfacUtils;



public class Tester {

    /**
     * @param args
     * @throws XregistryException 
     */
    public static void main(String[] args) throws Exception {
        GlobalContext globalContext = new GlobalContext(true);
        DocumentRegistryClient client = new DocumentRegistryClient(globalContext,"https://tyr15.cs.indiana.edu:6666/xregistry?wsdl");
        client.registerConcreteWsdl(GfacUtils.readFile("/u/hperera/temp/cwsdls/AdderService.wsdl"), 99999999);
        
        
        System.out.println(Utils.canonicalizeDN("O=LEAD Project,OU=portal.leadprojec t.org,OU=cs.indiana.edu,CN=hperera/Email=hperera@cs.indiana.edu,CN=proxy"));
        
//        String actorsToAddStr = "#U/C=US/O=National Center for Supercomputing Applications/CN=Suresh Marru$Read";
//        
//        if(actorsToAddStr != null){
//            Map<String,String> actorsToAdd = new HashMap<String,String>();
//            
//            String[] actorsToAddRaw = actorsToAddStr.split("#");
//            for(String raw:actorsToAddRaw){
//                if(raw != null && raw.trim().length() > 0){
//                    System.out.println("raw ="+raw);
//                    String[] data = raw.split("\\$");
//                    actorsToAdd.put(data[0], data[1]);
//                    boolean isUser = data[0].startsWith("U");
//                    String actor = data[0].substring(1);
//                }
//            }
//        }   
//        
        
//        String file = Utils.readFile("src/IXregistry.xml");
//        String prettywsdl = Utils.prettyPrint2String(XmlConstants.BUILDER.parseFragmentFromReader(new StringReader(file)));
//        System.out.println(prettywsdl);
//        
//        FileWriter out = new FileWriter("src/Xregistry.xml");
//        out.write(prettywsdl);
//        out.close();
        
//        ProxyRenewer proxyRenewer = new ProxyRenewer("hperera", "hperera1234",
//                MyProxy.DEFAULT_PORT, 1234, "portal.leadproject.org");
//        
//        GSSCredential credential = proxyRenewer.renewProxy();
//        System.out.println(credential.getName());
//        GlobalContext globalContext = new GlobalContext(true);
//        globalContext.setTrustedCertsFile("/etc/lead/certificates/trusted_cas.pem");
//        globalContext.setCredential(credential);
//        //globalContext.setHostcertsKeyFile("/etc/lead/certificates/hostcertkey.pem.hperera");
//        DocumentRegistryClient client = new DocumentRegistryClient(globalContext,"https://129.79.246.253:6666/xregistry?wsdl");
//        //client.addCapability("urn:hostdesc:grid-hg.ncsa.teragrid.org", "extreme", false, XregistryConstants.Action.All.toString());
//        
//        DocData[] data = client.findHosts("");
//        for(DocData doc:data){
//            System.out.println(doc.name);
//        }
//        client.removeConcreteWsdl("{http://www.extreme.indiana.edu/namespaces/2004/01/gFac}SrinathLsService35_Thu_Jun_28_11_40_48_EDT_2007_941422");
        
        
//        Scanner scanner = new Scanner(new File("src/xregistry/tables.sql"));
//        scanner.useDelimiter(";");
//        //Pattern pattern = Pattern.compile(".*\\;");
//        
//        while(scanner.hasNext()){
//            String line = scanner.next();
//            if(line.trim().length() > 0 && !line.startsWith("--")){
//                System.out.println(line.trim()+"\n");
//                //statement.executeUpdate(line.trim());
//            }
//        }
    }

}

