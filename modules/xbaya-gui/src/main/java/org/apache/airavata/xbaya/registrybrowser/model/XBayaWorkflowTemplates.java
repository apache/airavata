package org.apache.airavata.xbaya.registrybrowser.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.xml.namespace.QName;

import org.apache.airavata.registry.api.Registry;

public class XBayaWorkflowTemplates {
    private Registry registry;

    public XBayaWorkflowTemplates(Registry registry) {
        setRegistry(registry);
    }

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    public List<XBayaWorkflowTemplate> getWorkflows() {
        List<XBayaWorkflowTemplate> workflows = new ArrayList<XBayaWorkflowTemplate>();
        Map<QName, Node> workflowMap = registry.getWorkflows(registry.getUsername());
        for (Node xBayaWorkflowNode : workflowMap.values()) {
            workflows.add(new XBayaWorkflowTemplate(xBayaWorkflowNode));
        }
        return workflows;
    }
}
