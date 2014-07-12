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

package org.apache.airavata.workflow.model.component.ws;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.apache.airavata.common.utils.WSConstants;
import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.appinterface.InputDataObjectType;
import org.apache.airavata.model.appcatalog.appinterface.OutputDataObjectType;
import org.xmlpull.infoset.XmlNamespace;

import xsul.dsig.apache.axis.uti.XMLUtils;
import xsul5.XmlConstants;

@XmlRootElement(name="Application")
@XmlType(propOrder = {"applicationId", "name", "description", "inputParameters", "outputParameters"})
public class WSComponentApplication {
	
	private String applicationId;
	private String name;
	private String description;
	private List<WSComponentApplicationParameter> inputParameters;
	private List<WSComponentApplicationParameter> outputParameters;
	
	public WSComponentApplication() {
	}
	
	public static void main(String[] args) {
		WSComponentApplication app = new WSComponentApplication();
		app.setApplicationId("dsfds");
		app.setName("dfd");
		app.setDescription("sdfdsfds");
		app.addInputParameter(new WSComponentApplicationParameter("asas", new QName("sdf"), null, "sdfds"));
		app.addOutputParameter(new WSComponentApplicationParameter("9842", new QName("sdv99304"), null, null));
		app.addOutputParameter(new WSComponentApplicationParameter("AAAAA", new QName("sdfd"), "sdfsdf", "243bs sd fsd fs f dfd"));
	      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try {
		      JAXBContext context = JAXBContext.newInstance(WSComponentApplication.class);
		      Marshaller marshaller = context.createMarshaller();
		      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true); // pretty
		      marshaller.setProperty(Marshaller.JAXB_ENCODING, "ISO-8859-1"); // specify encoding
		      // output xml to outputstream.
		      marshaller.marshal(app, byteArrayOutputStream);
		      org.xmlpull.infoset.XmlElement s = XMLUtil.stringToXmlElement(byteArrayOutputStream.toString());
		      System.out.println(s.toString());
		    } catch (JAXBException e) {
		      e.printStackTrace();
		    }
		try {
		      JAXBContext context = JAXBContext.newInstance(WSComponentApplication.class);
		      Unmarshaller unmarshaller = context.createUnmarshaller();
		      // parse xml.
		      WSComponentApplication d = (WSComponentApplication)unmarshaller.unmarshal(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
		      System.out.println(d.getApplicationId());
		    } catch (JAXBException e) {
		      e.printStackTrace();
		    }
	}
	
	public static WSComponentApplication parse(org.xmlpull.infoset.XmlElement element) {
		String xmlString = XMLUtil.xmlElementToString(element);
		try {
			JAXBContext context = JAXBContext.newInstance(WSComponentApplication.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			return (WSComponentApplication) unmarshaller.unmarshal(new ByteArrayInputStream(xmlString.getBytes()));
		} catch (JAXBException e) {
			e.printStackTrace();
			return null;
		}

	}
	public org.xmlpull.infoset.XmlElement toXml(){
		      try {
				JAXBContext context = JAXBContext.newInstance(WSComponentApplication.class);
				  Marshaller marshaller = context.createMarshaller();
				  marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true); // pretty
				  marshaller.setProperty(Marshaller.JAXB_ENCODING, "ISO-8859-1"); // specify encoding
				  // output xml to outputstream.
				  ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				  marshaller.marshal(this, byteArrayOutputStream);
				  return XMLUtil.stringToXmlElement(byteArrayOutputStream.toString());
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
	}
	
	public WSComponentApplication(ApplicationInterfaceDescription application) {
		setApplicationId(application.getApplicationInterfaceId());
		setName(application.getApplicationName());
		setDescription(application.getApplicationDesription());
                
        List<InputDataObjectType> applicationInputs = application.getApplicationInputs();
        for (InputDataObjectType inputDataObjectType : applicationInputs) {
        	String typeName = inputDataObjectType.getType().toString().toLowerCase();
            XmlNamespace namespace = null;
            namespace = XmlConstants.BUILDER.newNamespace("xsd", WSConstants.XSD_NS_URI);
            String prefix = "xsd";
            QName type = new QName(namespace.getName(), typeName, prefix);
            
			addInputParameter(new WSComponentApplicationParameter(inputDataObjectType.getName(),type ,inputDataObjectType.getUserFriendlyDescription(), inputDataObjectType.getValue()));
		}

        List<OutputDataObjectType> applicationOutputs = application.getApplicationOutputs();
        for (OutputDataObjectType outputDataObjectType : applicationOutputs) {
        	String typeName = outputDataObjectType.getType().toString().toLowerCase();
            XmlNamespace namespace = null;
            namespace = XmlConstants.BUILDER.newNamespace("xsd", WSConstants.XSD_NS_URI);
            String prefix = "xsd";
            QName type = new QName(namespace.getName(), typeName, prefix);
            
			addOutputParameter(new WSComponentApplicationParameter(outputDataObjectType.getName(),type ,outputDataObjectType.getName(), outputDataObjectType.getValue()));
		}
	}
	
	@XmlAttribute (required = true, name = "application")
	public String getApplicationId() {
		return applicationId;
	}
	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}
	@XmlAttribute (required = true)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
//	@XmlElementWrapper(name = "Input")
	@XmlElement(name = "Input")
	public List<WSComponentApplicationParameter> getInputParameters() {
		return inputParameters;
	}
	
	public void setInputParameters(
			List<WSComponentApplicationParameter> inputParameters) {
		this.inputParameters = inputParameters;
	}
	
	@XmlElement(name = "Output")
	public List<WSComponentApplicationParameter> getOutputParameters() {
		return outputParameters;
	}
	public void setOutputParameters(
			List<WSComponentApplicationParameter> outputParameters) {
		this.outputParameters = outputParameters;
	}
	
	public void addInputParameter(WSComponentApplicationParameter inputParameter){
		if (inputParameters==null){
			inputParameters = new ArrayList<WSComponentApplicationParameter>();
		}
		inputParameters.add(inputParameter);
	}
	
	public void addOutputParameter(WSComponentApplicationParameter outputParameter){
		if (outputParameters==null){
			outputParameters = new ArrayList<WSComponentApplicationParameter>();
		}
		outputParameters.add(outputParameter);
	}
	
	@XmlAttribute (required = false)
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
