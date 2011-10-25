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

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.apache.airavata.commons.gfac.type.Type;
import org.apache.airavata.schemas.gfac.BatchApplicationDeploymentDescriptionDocument;
import org.apache.airavata.schemas.gfac.BatchApplicationDeploymentDescriptionType;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

public class SchemaUtil {
    public static Type parseFromXML(String xml) {
        ByteArrayInputStream bs = new ByteArrayInputStream(xml.getBytes());
        XMLDecoder d = new XMLDecoder(bs);
        Object result = d.readObject();
        d.close();
        return (Type) result;
    }

    public static String toXML(Type type) {
        ByteArrayOutputStream x = new ByteArrayOutputStream();
        XMLEncoder e = new XMLEncoder(x);
        e.writeObject(type);
        e.close();
        return x.toString();
    }
    
    
    public static void main(String[] args) throws XmlException {
        
        BatchApplicationDeploymentDescriptionDocument t = BatchApplicationDeploymentDescriptionDocument.Factory.newInstance();
        
        BatchApplicationDeploymentDescriptionType t2 = BatchApplicationDeploymentDescriptionType.Factory.newInstance();
        
        t.setBatchApplicationDeploymentDescription(t2 );
        
        t2.setCpuCount(4);
        System.out.println(t);
        
        
        XmlObject k = XmlObject.Factory.parse(t.toString());
        if (k instanceof BatchApplicationDeploymentDescriptionDocument ){
            System.out.println("ooo");
        }
    }
}
