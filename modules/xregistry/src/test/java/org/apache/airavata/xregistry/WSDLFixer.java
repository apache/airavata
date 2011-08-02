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

import java.io.StringReader;
import java.util.Iterator;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.PortType;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.wsdl.xml.WSDLWriter;
import javax.xml.namespace.QName;

import org.apache.airavata.xregistry.utils.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.ibm.wsdl.BindingOutputImpl;
import com.ibm.wsdl.MessageImpl;
import com.ibm.wsdl.OutputImpl;
import com.ibm.wsdl.PartImpl;
import com.ibm.wsdl.extensions.schema.SchemaImpl;
import com.ibm.wsdl.extensions.soap.SOAPBodyImpl;



public class WSDLFixer {
    
    private Binding binding;
    private Element types;
    private Definition definition;
    private WSDLFactory factory;
    
    
    public void fixBindingOperation(String name,BindingOperation operation){
        BindingOutput out = new BindingOutputImpl();
        SOAPBodyImpl bodyImpl = new SOAPBodyImpl();
        bodyImpl.setUse("literal");
        bodyImpl.setNamespaceURI("http://extreme.indiana.edu/xregistry2/2007_02_21");
        operation.setBindingOutput(out);
    }

    public void fixOperation(Message message,Operation op){
        Output output = new OutputImpl();
        output.setMessage(message);
        op.setOutput(output);

    }
    
    
    public Element createType(String name,Document doc){
        Element ele = doc.createElement("xs:element");
        ele.setAttribute("name",name+"Response");
        ele.appendChild(doc.createElement("xs:complexType"));
        return ele;
    }
//    <wsdl:message name="listGroupsResponseMessage">
//    <wsdl:part name="part1" element="ns:listGroupsResponse" />
//</wsdl:message>
    
    public Message createMessage(String name){
        Message message =  new MessageImpl();
        message.setQName(new QName("http://extreme.indiana.edu/xregistry2/2007_02_21",name+"ResponseMessage","msgns"));
        Part part = new PartImpl();
        part.setElementName(new QName("http://extreme.indiana.edu/xregistry2/2007_02_21",name+"Response","ns"));
        message.addPart(part);
        return message;
    }
    
    
    
    public static void main(String[] args) throws Exception{
        WSDLFixer fixer = new WSDLFixer();
        fixer.fix();
//        javax.xml.parsers.DocumentBuilderFactory domfactory =
//            javax.xml.parsers.DocumentBuilderFactory.newInstance();
//        javax.xml.parsers.DocumentBuilder builder =  domfactory.newDocumentBuilder();
//        
//
//        DOMImplementation dImpl = builder.getDOMImplementation();

    }
    
    public void fix() throws Exception{
        
        
        String targetNs = "http://extreme.indiana.edu/xregistry2/2007_02_21";
        //Document doc = dImpl.createDocument(targetNs, "factoryServices", null);
        
        
        String wsdlAsStr = Utils.readFile("src/xregistry.wsdl");
       

        WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();
        Definition olddefinition = reader.readWSDL(null, new InputSource(new StringReader(wsdlAsStr)));
        //Definition definition = reader.readWSDL(null, new InputSource(new StringReader(wsdlAsStr)));;
        Definition definition = WSDLFactory.newInstance().newDefinition();
        
        Iterator ns = olddefinition.getNamespaces().keySet().iterator();
        while(ns.hasNext()){
            String prefix = (String)ns.next();
            String nsURI = (String)olddefinition.getNamespaces().get(prefix);
            definition.addNamespace(prefix,nsURI);
        }
        Iterator bs = olddefinition.getBindings().values().iterator();
        while(bs.hasNext()){
            definition.addBinding((Binding)bs.next());
        }
        
        Iterator pts = olddefinition.getPortTypes().values().iterator();
        while(pts.hasNext()){
            definition.addPortType((PortType)pts.next());
        }
        
        Iterator msgs = olddefinition.getMessages().values().iterator();
        while(msgs.hasNext()){
            definition.addMessage((Message)msgs.next());
        }
        
        Iterator ss = olddefinition.getServices().values().iterator();
        while(ss.hasNext()){
            definition.addService((javax.wsdl.Service)ss.next());
        }
        definition.setTypes(olddefinition.getTypes());
        
        
        
        Binding binding = definition.getBinding(new QName(targetNs,"IXregistrySOAP11Binding"));
        SchemaImpl impl;
        Document doc = null;
        
        Iterator it = definition.getTypes().getExtensibilityElements().iterator();
        if(it.hasNext()){
            impl = (SchemaImpl)it.next();
            types = impl.getElement();
            
            doc = types.getOwnerDocument();
            
            
        }
        
       // System.out.println(binding.toString());
        
        
        
        
        
        
        
        
        
        
        Iterator ops = binding.getPortType().getOperations().iterator();
        while(ops.hasNext()){
            Operation op = (Operation)ops.next();
            if(op.getOutput() == null){
                
                String name = op.getName();
                //System.out.println("Fixed ="+name);
                //System.out.println("<wsdl:message name=\""+name+"ResponseMessage\">\n<wsdl:part name=\"part1\" element=\"ns:"+name+"Response\" />\n</wsdl:message>");
                System.out.println("<wsdl:operation name=\""+name+"\">\n<soap:operation soapAction=\"urn:addCapability\" style=\"document\" />\n<wsdl:input><soap:body use=\"literal\" namespace=\"http://extreme.indiana.edu/xregistry2/2007_02_21\" /></wsdl:input></wsdl:operation>");
            
                
                Element ele = createType(name, doc);
                types.appendChild(ele);
                Message message = createMessage(name);
                definition.addMessage(message);
                fixOperation(message, op);
                fixBindingOperation(name, binding.getBindingOperation(name, null, null));
            }
        }
        
        
        
        WSDLWriter w =  WSDLFactory.newInstance().newWSDLWriter();
        
        w.writeWSDL(definition, System.out);
        
    }
    
    
    
//    public static Message createMessage(){
//        
//    }
    
}

