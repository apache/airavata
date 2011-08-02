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

package org.apache.airavata.xregistry.doc;

import org.apache.airavata.xregistry.XregistryConstants;
import org.apache.airavata.xregistry.XregistryException;
import org.apache.airavata.xregistry.XregistryConstants.DocType;

public class ResourceUtils {
    
    public static String getResourceID(DocType type,String... names) throws XregistryException{
        StringBuffer buf = new StringBuffer();
        buf.append("urn");
        switch(type){
            case ServiceDesc:
                buf.append(":servicedesc");
                break;
            case AppDesc:
                buf.append(":appdesc");
                break;
            case HostDesc:
                buf.append(":hostdesc");
                break;
            case CWsdl:
                buf.append(":cwsdl");
                break; 
            default:
                throw new XregistryException("Unknown resource type, type =" + type);
        }
        
        for(String name:names){
            buf.append(":");
            buf.append(name);    
        }
        
        return buf.toString();
    }
    
    public static String getOGCEResourceID(String resourceType, String... names) throws XregistryException{
        if (resourceType==null || resourceType == "") {
        	resourceType = XregistryConstants.DEFAULTOGCERESOURCETYPE;
        }
        
        StringBuffer buf = new StringBuffer();
        buf.append("urn:");
        buf.append(resourceType);
        for(String name:names){
            buf.append(":");
            buf.append(name);    
        }
        
        return buf.toString();
    }
    
}

