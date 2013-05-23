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

package org.apache.airavata.client.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.airavata.client.AiravataClient;
import org.apache.airavata.client.api.WorkflowManager;
import org.apache.airavata.client.api.exception.AiravataAPIInvocationException;
import org.apache.airavata.client.api.exception.WorkflowAlreadyExistsException;
import org.apache.airavata.common.exception.AiravataConfigurationException;
import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.registry.api.exception.RegistryException;
import org.apache.airavata.registry.api.exception.worker.UserWorkflowAlreadyExistsException;
import org.apache.airavata.workflow.model.wf.Workflow;
import org.apache.airavata.workflow.model.wf.WorkflowData;
import org.apache.airavata.workflow.model.wf.WorkflowInput;

public class WorkflowManagerImpl implements WorkflowManager {
	private AiravataClient client;

	public WorkflowManagerImpl(AiravataClient client) {
		setClient(client);
	}
	
	public AiravataClient getClient() {
		return client;
	}

	public void setClient(AiravataClient client) {
		this.client = client;
	}
	
	@Override
	public boolean saveWorkflow(String workflowAsString, String owner)
			throws AiravataAPIInvocationException {
		return saveWorkflow(getWorkflowFromString(workflowAsString), workflowAsString, owner);
	}

    @Override
    public void addOwnerWorkflow (String workflowAsString, String owner)
            throws AiravataAPIInvocationException, WorkflowAlreadyExistsException {
         addWorkflow(getWorkflowFromString(workflowAsString), workflowAsString, owner);
    }

    @Override
    public void updateOwnerWorkflow (String workflowAsString, String owner)
            throws AiravataAPIInvocationException {
        updateWorkflow(getWorkflowFromString(workflowAsString), workflowAsString, owner);
    }

    @Override
    public void addOwnerWorkflow (URI workflowPath, String owner)
            throws AiravataAPIInvocationException, WorkflowAlreadyExistsException {
        Workflow workflow = getWorkflowFromURI(workflowPath);
        addWorkflow(workflow, XMLUtil.xmlElementToString(workflow.toXML()), owner);
    }

    @Override
    public void updateOwnerWorkflow (URI workflowPath, String owner)
            throws AiravataAPIInvocationException {
        Workflow workflow = getWorkflowFromURI(workflowPath);
        updateWorkflow(workflow, XMLUtil.xmlElementToString(workflow.toXML()), owner);
    }

	@Override
	public boolean saveWorkflow(Workflow workflow, String owner)
			throws AiravataAPIInvocationException {
		return saveWorkflow(workflow, XMLUtil.xmlElementToString(workflow.toXML()), owner);
	}

    @Override
    public void addOwnerWorkflow (Workflow workflow, String owner) throws AiravataAPIInvocationException,
            WorkflowAlreadyExistsException {
        addWorkflow(workflow, XMLUtil.xmlElementToString(workflow.toXML()), owner);
    }

    @Override
    public void updateOwnerWorkflow (Workflow workflow, String owner) throws AiravataAPIInvocationException {
        updateWorkflow(workflow, XMLUtil.xmlElementToString(workflow.toXML()), owner);
    }

    private void addWorkflow(Workflow workflow, String workflowAsString, String owner)
            throws WorkflowAlreadyExistsException, AiravataAPIInvocationException {
        try {
            getClient().getRegistryClient().addWorkflow(workflow.getName(), workflowAsString);
        } catch (UserWorkflowAlreadyExistsException e) {
            throw new WorkflowAlreadyExistsException("Workflow " +
                    workflow.getName()
                    + " already exists in the system.", e);
        } catch (RegistryException e) {
            throw new AiravataAPIInvocationException("An internal error occurred while adding workflow " +
                    workflow.getName(), e);
        } catch (AiravataConfigurationException e) {
            throw new AiravataAPIInvocationException("Error retrieving registry client for workflow " +
                    workflow.getName(), e);
        }

        if (owner == null) {
            try {
                getClient().getRegistryClient().publishWorkflow(workflow.getName());
            } catch (RegistryException e) {
                throw new AiravataAPIInvocationException("An internal error occurred while adding workflow " +
                        workflow.getName(), e);
            } catch (AiravataConfigurationException e) {
                throw new AiravataAPIInvocationException("Error retrieving registry client for workflow " +
                        workflow.getName(), e);
            }
        }
    }

    private void updateWorkflow(Workflow workflow, String workflowAsString, String owner)
            throws AiravataAPIInvocationException {
        try {
            getClient().getRegistryClient().updateWorkflow(workflow.getName(), workflowAsString);
        } catch (RegistryException e) {
            throw new AiravataAPIInvocationException("An internal error occurred while adding workflow " +
                    workflow.getName(), e);
        } catch (AiravataConfigurationException e) {
            throw new AiravataAPIInvocationException("Error retrieving registry client for workflow " +
                    workflow.getName(), e);
        }

        if (owner == null) {
            try {
                getClient().getRegistryClient().publishWorkflow(workflow.getName());
            } catch (RegistryException e) {
                throw new AiravataAPIInvocationException("An internal error occurred while adding workflow " +
                        workflow.getName(), e);
            } catch (AiravataConfigurationException e) {
                throw new AiravataAPIInvocationException("Error retrieving registry client for workflow " +
                        workflow.getName(), e);
            }
        }
    }

    // Remove once deprecated methods are removed from the API
    @Deprecated
	private boolean saveWorkflow(Workflow workflow, String workflowAsString,String owner)
			throws AiravataAPIInvocationException {
		try {
			
			if (getClient().getRegistryClient().isWorkflowExists(workflow.getName())) {
				getClient().getRegistryClient().updateWorkflow(workflow.getName(),workflowAsString);
			}else{
				getClient().getRegistryClient().addWorkflow(workflow.getName(),workflowAsString);
			}
			if (owner==null){
				getClient().getRegistryClient().publishWorkflow(workflow.getName());
			}
			return true;
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public List<Workflow> getWorkflows(String owner)
			throws AiravataAPIInvocationException {
		try {
			List<Workflow> workflows=new ArrayList<Workflow>();
			Map<String, String> workflowMap = getClient().getRegistryClient().getWorkflows();
			for(String workflowStr:workflowMap.values()){
				workflows.add(getWorkflowFromString(workflowStr));
			}
			return workflows;
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public List<String> getWorkflowTemplateIds(String owner)
			throws AiravataAPIInvocationException {
		try {
			List<String> workflowList = new ArrayList<String>();
			Map<String, String> workflows;
			workflows = getClient().getRegistryClient().getWorkflows();
			for (String name : workflows.keySet()) {
				workflowList.add(name);
			}
			return workflowList;
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public Workflow getWorkflow(String workflowName, String owner)
			throws AiravataAPIInvocationException {
		return getWorkflowFromString(getWorkflowAsString(workflowName, owner));
	}

	@Override
	public String getWorkflowAsString(String workflowName, String owner)
			throws AiravataAPIInvocationException {
		try {
			return getClient().getRegistryClient().getWorkflowGraphXML(workflowName);
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public boolean deleteWorkflow(String workflowName, String owner)
			throws AiravataAPIInvocationException {
		try {
			getClient().getRegistryClient().removeWorkflow(workflowName);
			return true;
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public boolean saveWorkflow(String workflowAsString)
			throws AiravataAPIInvocationException {
		return saveWorkflow(workflowAsString, getCurrentUser());
	}

    @Override
    public void addWorkflow (String workflowAsString) throws WorkflowAlreadyExistsException,
            AiravataAPIInvocationException {
        addOwnerWorkflow(workflowAsString, getCurrentUser());
    }

    @Override
    public void updateWorkflow (String workflowAsString) throws AiravataAPIInvocationException {
        updateOwnerWorkflow(workflowAsString, getCurrentUser());
    }

	@Override
	public boolean saveWorkflowAsPublic(String workflowAsString)
			throws AiravataAPIInvocationException {
		return saveWorkflow(workflowAsString, null);
	}

    @Override
    public void addWorkflowAsPublic (String workflowAsString) throws AiravataAPIInvocationException,
            WorkflowAlreadyExistsException {
        addOwnerWorkflow (workflowAsString, null);
    }

    @Override
    public void updateWorkflowAsPublic (String workflowAsString) throws AiravataAPIInvocationException {
        updateOwnerWorkflow(workflowAsString, null);
    }

    @Override
    public void addWorkflowAsPublic (URI workflowPath) throws AiravataAPIInvocationException,
            WorkflowAlreadyExistsException {
        addOwnerWorkflow (getWorkflowFromURI(workflowPath), null);
    }

    @Override
    public void updateWorkflowAsPublic (URI workflowPath) throws AiravataAPIInvocationException {
        updateOwnerWorkflow(getWorkflowFromURI(workflowPath), null);
    }

	@Override
	public boolean saveWorkflow(Workflow workflow)
			throws AiravataAPIInvocationException {
		return saveWorkflow(workflow, getCurrentUser());
	}

    @Override
    public void addWorkflow (Workflow workflow) throws AiravataAPIInvocationException,
            WorkflowAlreadyExistsException{
        addOwnerWorkflow(workflow, getCurrentUser());
    }

    @Override
    public void updateWorkflow (Workflow workflow) throws AiravataAPIInvocationException {
        updateOwnerWorkflow(workflow, getCurrentUser());
    }

    @Override
    public void addWorkflow (URI workflowPath) throws AiravataAPIInvocationException,
            WorkflowAlreadyExistsException {
        addOwnerWorkflow(getWorkflowFromURI(workflowPath), getCurrentUser());
    }

    @Override
    public void updateWorkflow (URI workflowPath) throws AiravataAPIInvocationException {
        updateOwnerWorkflow(getWorkflowFromURI(workflowPath), getCurrentUser());
    }

	private String getCurrentUser() {
		return getClient().getCurrentUser();
	}

	@Override
	public boolean saveWorkflowAsPublic(Workflow workflow)
			throws AiravataAPIInvocationException {
		return saveWorkflow(workflow, null);
	}

	@Override
	public List<Workflow> getWorkflows() throws AiravataAPIInvocationException {
		return getWorkflows(getCurrentUser());
	}

	@Override
	public List<String> getWorkflowTemplateIds()
			throws AiravataAPIInvocationException {
		return getWorkflowTemplateIds(getCurrentUser());
	}

	@Override
	public Workflow getWorkflow(String workflowName)
			throws AiravataAPIInvocationException {
		return getWorkflow(workflowName, getCurrentUser());
	}

	@Override
	public String getWorkflowAsString(String workflowName)
			throws AiravataAPIInvocationException {
		return getWorkflowAsString(workflowName, getCurrentUser());
	}

	@Override
	public boolean deleteWorkflow(String workflowName)
			throws AiravataAPIInvocationException {
		return deleteWorkflow(workflowName, getCurrentUser());
	}

	@Override
	public Workflow getWorkflowFromString(String workflowAsString)
			throws AiravataAPIInvocationException {
		try {
			return new Workflow(workflowAsString);
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

    @Override
    public Workflow getWorkflowFromURI(URI workflowPath) throws AiravataAPIInvocationException {
        try {
            return new Workflow(workflowPath);
        } catch (Exception e) {
            throw new AiravataAPIInvocationException(e);
        }
    }

    @Override
	public String getWorkflowAsString(Workflow workflow)
			throws AiravataAPIInvocationException {
		return XMLUtil.xmlElementToString(workflow.toXML());
	}

	@Override
	public List<String> getWorkflowServiceNodeIDs(String templateID) throws AiravataAPIInvocationException{
        return getWorkflow(templateID).getWorkflowServiceNodeIDs();
	}

	@Override
	public boolean isPublishedWorkflowExists(String workflowName)
			throws AiravataAPIInvocationException {
		try {
			return getClient().getRegistryClient().isPublishedWorkflowExists(workflowName);
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public void publishWorkflow(String workflowName, String publishWorkflowName)
			throws AiravataAPIInvocationException {
		try {
			getClient().getRegistryClient().publishWorkflow(workflowName, publishWorkflowName);
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public void publishWorkflow(String workflowName)
			throws AiravataAPIInvocationException {
		try {
			getClient().getRegistryClient().publishWorkflow(workflowName);
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public String getPublishedWorkflowGraphXML(String workflowName)
			throws AiravataAPIInvocationException {
		try {
			return getClient().getRegistryClient().getPublishedWorkflowGraphXML(workflowName);
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}
	
	@Override
	public Workflow getPublishedWorkflow(String workflowName)
			throws AiravataAPIInvocationException {
		return getWorkflowFromString(getPublishedWorkflowGraphXML(workflowName));
	}

	@Override
	public List<String> getPublishedWorkflowNames()
			throws AiravataAPIInvocationException {
		try {
			return getClient().getRegistryClient().getPublishedWorkflowNames();
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public Map<String, Workflow> getPublishedWorkflows()
			throws AiravataAPIInvocationException {
		try {
			Map<String, Workflow> workflows=new HashMap<String, Workflow>();
			Map<String, String> publishedWorkflows = getClient().getRegistryClient().getPublishedWorkflows();
			for (String name : publishedWorkflows.keySet()) {
				workflows.put(name, getWorkflowFromString(publishedWorkflows.get(name)));
			}
			return workflows;
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public void removePublishedWorkflow(String workflowName)
			throws AiravataAPIInvocationException {
		try {
			getClient().getRegistryClient().removePublishedWorkflow(workflowName);
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public List<WorkflowInput> getWorkflowInputs(String workflowName) throws AiravataAPIInvocationException {
		try {
			return getWorkflow(workflowName).getWorkflowInputs();
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public List<WorkflowInput> getWorkflowInputs(WorkflowData workflowData) throws AiravataAPIInvocationException {
		try {
			if (workflowData.isPublished()){
				return getWorkflowFromString(getClient().getRegistryClient().getPublishedWorkflowGraphXML(workflowData.getName())).getWorkflowInputs();
			}else{
				return getWorkflowInputs(workflowData.getName());
			}
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public List<WorkflowData> getAllWorkflows() throws AiravataAPIInvocationException {
		List<WorkflowData> list = new ArrayList<WorkflowData>();
		List<String> workflowTemplateIds = getWorkflowTemplateIds();
		try {
			for (String id : workflowTemplateIds) {
				list.add(new WorkflowData(id,null,false));
			}
			List<String> publishedWorkflowNames = getClient().getRegistryClient().getPublishedWorkflowNames();
			for (String id : publishedWorkflowNames) {
				list.add(new WorkflowData(id,null,false));
			}
			return list;
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

    @Override
    public boolean isWorkflowExists(String workflowName) throws AiravataAPIInvocationException {
        try {
            return getClient().getRegistryClient().isWorkflowExists(workflowName);
        } catch (Exception e) {
            throw new AiravataAPIInvocationException(e);
        }
    }

    @Override
    public void updateWorkflow(String workflowName, String workflowGraphXml) throws AiravataAPIInvocationException {
        try {
            getClient().getRegistryClient().updateWorkflow(workflowName, workflowGraphXml);
        } catch (Exception e) {
            throw new AiravataAPIInvocationException(e);
        }
    }

    @Override
    public void removeWorkflow(String workflowName) throws AiravataAPIInvocationException {
        try {
            getClient().getRegistryClient().removeWorkflow(workflowName);
        } catch (Exception e) {
            throw new AiravataAPIInvocationException(e);
        }
    }

}
