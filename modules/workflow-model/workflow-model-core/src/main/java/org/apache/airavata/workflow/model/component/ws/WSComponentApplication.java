/**
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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.workflow.model.utils.WorkflowConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import xsul5.XmlConstants;

@XmlRootElement(name = "Application")
@XmlType(propOrder = {"applicationId", "name", "description", "inputParameters", "outputParameters"})
public class WSComponentApplication {
    private static final Logger log = LoggerFactory.getLogger(WSComponentApplication.class);
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
        app.addInputParameter(new WSComponentApplicationParameter("asas", DataType.STRING, null, "sdfds", 1));
        app.addOutputParameter(new WSComponentApplicationParameter("9842", DataType.STRING, null, null));
        app.addOutputParameter(new WSComponentApplicationParameter("AAAAA", DataType.STRING, "sdfsdf", "243bs sd fsd fs f dfd"));
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
            log.error(e.getMessage(), e);
        }
        try {
            JAXBContext context = JAXBContext.newInstance(WSComponentApplication.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            // parse xml.
            WSComponentApplication d = (WSComponentApplication) unmarshaller.unmarshal(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
            System.out.println(d.getApplicationId());
        } catch (JAXBException e) {
            log.error(e.getMessage(), e);
        }
    }

    public static WSComponentApplication parse(org.xmlpull.infoset.XmlElement element) {
        String xmlString = XMLUtil.xmlElementToString(element);
        try {
            JAXBContext context = JAXBContext.newInstance(WSComponentApplication.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            return (WSComponentApplication) unmarshaller.unmarshal(new ByteArrayInputStream(xmlString.getBytes()));
        } catch (JAXBException e) {
            log.error(e.getMessage(), e);
            return null;
        }

    }

    public static WSComponentApplication parse(JsonObject applicationObject) {
        WSComponentApplication wsComponentApplication = new WSComponentApplication();
        wsComponentApplication.description = applicationObject.getAsJsonPrimitive(WorkflowConstants.APPLICATION_COMPONENT_DESCRIPTION).getAsString();
        wsComponentApplication.name = applicationObject.getAsJsonPrimitive(WorkflowConstants.APPLICATION_COMPONENT_NAME).getAsString();
        wsComponentApplication.applicationId = applicationObject.getAsJsonPrimitive(WorkflowConstants.APPLICATION_COMPONENT_APPLICATION).getAsString();

        if (applicationObject.get(WorkflowConstants.APPLICATION_INPUT) != null) {
            JsonArray inputArray = applicationObject.getAsJsonArray(WorkflowConstants.APPLICATION_INPUT);
            WSComponentApplicationParameter inputParameter;
            JsonObject inputObject;
            for (JsonElement jsonElement : inputArray) {
                if (jsonElement instanceof JsonObject) {
                    inputObject = (JsonObject) jsonElement;
                    inputParameter = new WSComponentApplicationParameter();
                    inputParameter.setDefaultValue(inputObject.getAsJsonPrimitive(WorkflowConstants.APPLICATION_DATA_DEFAULT_VALUE).getAsString());
                    inputParameter.setDescription(inputObject.getAsJsonPrimitive(WorkflowConstants.APPLICATION_DATA_DESCRIPTION).getAsString());
                    inputParameter.setName(inputObject.getAsJsonPrimitive(WorkflowConstants.APPLICATION_DATA_NAME).getAsString());
                    inputParameter.setType(DataType.valueOf(inputObject.getAsJsonPrimitive(WorkflowConstants.APPLICATION_DATA_DATA_TYPE).getAsString()));
                    inputParameter.setInputOrder(inputObject.getAsJsonPrimitive(WorkflowConstants.APPLICATION_DATA_INPUT_ORDER).getAsInt());
                    if (inputObject.getAsJsonPrimitive(WorkflowConstants.APPLICATION_DATA_APP_ARGUMENT) != null) {
                        inputParameter.setApplicationArgument(inputObject.getAsJsonPrimitive(WorkflowConstants.APPLICATION_DATA_APP_ARGUMENT).getAsString());
                    }
                    wsComponentApplication.addInputParameter(inputParameter);
                }
            }
        }

        if (applicationObject.get(WorkflowConstants.APPLICATION_OUTPUT) != null) {
            JsonArray outputArray = applicationObject.getAsJsonArray(WorkflowConstants.APPLICATION_OUTPUT);
            WSComponentApplicationParameter outputParameter;
            JsonObject outputObject;
            for (JsonElement jsonElement : outputArray) {
                if (jsonElement instanceof JsonObject) {
                    outputObject = (JsonObject) jsonElement;
                    outputParameter = new WSComponentApplicationParameter();
                    outputParameter.setDescription(outputObject.getAsJsonPrimitive(WorkflowConstants.APPLICATION_DATA_DESCRIPTION).getAsString());
                    outputParameter.setName(outputObject.getAsJsonPrimitive(WorkflowConstants.APPLICATION_DATA_NAME).getAsString());
                    outputParameter.setType(DataType.valueOf(outputObject.getAsJsonPrimitive(WorkflowConstants.APPLICATION_DATA_DATA_TYPE).getAsString()));
                    wsComponentApplication.addOutputParameter(outputParameter);
                }
            }
        }

        return wsComponentApplication;
    }

    public org.xmlpull.infoset.XmlElement toXml() {
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
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public JsonObject toJSON() {
        JsonObject componentObject = new JsonObject();
        componentObject.addProperty(WorkflowConstants.APPLICATION_COMPONENT_DESCRIPTION, this.description);
        componentObject.addProperty(WorkflowConstants.APPLICATION_COMPONENT_NAME, this.name);
        componentObject.addProperty(WorkflowConstants.APPLICATION_COMPONENT_APPLICATION, this.applicationId);
        JsonArray inputArray = new JsonArray();
        JsonObject inputObject;
        for (WSComponentApplicationParameter inputParameter : this.inputParameters) {
            inputObject = new JsonObject();
            inputObject.addProperty(WorkflowConstants.APPLICATION_DATA_DESCRIPTION, inputParameter.getDescription());
            inputObject.addProperty(WorkflowConstants.APPLICATION_DATA_NAME, inputParameter.getName());
            inputObject.addProperty(WorkflowConstants.APPLICATION_DATA_DEFAULT_VALUE, inputParameter.getDefaultValue());
            inputObject.addProperty(WorkflowConstants.APPLICATION_DATA_DATA_TYPE, inputParameter.getType().toString());
            inputObject.addProperty(WorkflowConstants.APPLICATION_DATA_INPUT_ORDER, inputParameter.getInputOrder());
            if (inputParameter.getApplicationArgument() != null) {
                inputObject.addProperty(WorkflowConstants.APPLICATION_DATA_APP_ARGUMENT, inputParameter.getApplicationArgument());
            }
            inputArray.add(inputObject);
        }
        componentObject.add(WorkflowConstants.APPLICATION_INPUT, inputArray);

        JsonArray outputArray = new JsonArray();
        JsonObject outputObject;
        for (WSComponentApplicationParameter outputParameter : this.outputParameters) {
            outputObject = new JsonObject();
            outputObject.addProperty(WorkflowConstants.APPLICATION_DATA_DESCRIPTION, outputParameter.getDescription());
            outputObject.addProperty(WorkflowConstants.APPLICATION_DATA_NAME, outputParameter.getName());
            outputObject.addProperty(WorkflowConstants.APPLICATION_DATA_DATA_TYPE, outputParameter.getType().toString());
            outputArray.add(outputObject);
        }
        componentObject.add(WorkflowConstants.APPLICATION_OUTPUT, outputArray);

        return componentObject;
    }

    public WSComponentApplication(ApplicationInterfaceDescription application) {
        setApplicationId(application.getApplicationInterfaceId());
        setName(application.getApplicationName());
        setDescription(application.getApplicationDescription());

        List<InputDataObjectType> applicationInputs = application.getApplicationInputs();
        for (InputDataObjectType inputDataObjectType : applicationInputs) {
            addInputParameter(new WSComponentApplicationParameter(inputDataObjectType.getName(), inputDataObjectType.getType(),
                    inputDataObjectType.getUserFriendlyDescription(), inputDataObjectType.getValue(),
                    inputDataObjectType.getApplicationArgument(), inputDataObjectType.getInputOrder()));
        }

        List<OutputDataObjectType> applicationOutputs = application.getApplicationOutputs();
        for (OutputDataObjectType outputDataObjectType : applicationOutputs) {
            addOutputParameter(new WSComponentApplicationParameter(outputDataObjectType.getName(),
                    outputDataObjectType.getType(), outputDataObjectType.getName(), outputDataObjectType.getValue()));
        }
    }

    @XmlAttribute(required = true, name = "application")
    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    @XmlAttribute(required = true)
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

    public void addInputParameter(WSComponentApplicationParameter inputParameter) {
        if (inputParameters == null) {
            inputParameters = new ArrayList<WSComponentApplicationParameter>();
        }
        inputParameters.add(inputParameter);
    }

    public void addOutputParameter(WSComponentApplicationParameter outputParameter) {
        if (outputParameters == null) {
            outputParameters = new ArrayList<WSComponentApplicationParameter>();
        }
        outputParameters.add(outputParameter);
    }

    @XmlAttribute(required = false)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
