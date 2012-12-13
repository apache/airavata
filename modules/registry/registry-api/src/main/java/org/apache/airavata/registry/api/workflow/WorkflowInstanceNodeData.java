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

package org.apache.airavata.registry.api.workflow;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class WorkflowInstanceNodeData{
	private WorkflowInstanceNode workflowInstanceNode;
	private List<WorkflowInstanceNodePortData> inputData;
	private List<WorkflowInstanceNodePortData> outputData;
	private String input;
	private String output;
    private WorkflowInstanceNodeStatus status;
    private WorkflowNodeType.WorkflowNode type;

    public WorkflowInstanceNodeData() {
    }

    public WorkflowInstanceNodeData(WorkflowInstanceNode workflowInstanceNode) {
		setWorkflowInstanceNode(workflowInstanceNode);
	}

	public WorkflowInstanceNode getWorkflowInstanceNode() {
		return workflowInstanceNode;
	}

	public void setWorkflowInstanceNode(WorkflowInstanceNode workflowInstanceNode) {
		this.workflowInstanceNode = workflowInstanceNode;
	}

    public WorkflowInstanceNodeStatus getStatus() {
        return status;
    }

    public void setStatus(WorkflowInstanceNodeStatus status) {
        this.status = status;
    }

    public void setStatus(WorkflowInstanceStatus.ExecutionStatus status, Date date) {
        setStatus(new WorkflowInstanceNodeStatus(this.workflowInstanceNode, status, date));

    }

    private static class NameValue{
		String name;
		String value;
		public NameValue(String name, String value) {
			this.name=name;
			this.value=value;
		}
	}
	
	private static List<NameValue> getIOParameterData(String data){
		List<NameValue> parameters=new ArrayList<NameValue>();
		if (data!=null) {
			String[] pairs = data.split(",");
			for (String paras : pairs) {
				String[] nameVals = paras.trim().split("=");
                NameValue pair = null;
                if(nameVals.length >= 2){
				 pair = new NameValue(nameVals[0].trim(),
						nameVals.length>1? nameVals[1].trim():"");
                }else if(nameVals.length == 1){
                  pair = new NameValue(nameVals[0].trim(),
						"");
                }
				parameters.add(pair);
			}
		}
		return parameters;
	}
	
	public List<WorkflowInstanceNodePortData> getInputData() {
		if (inputData==null){
			inputData=new ArrayList<WorkflowInstanceNodePortData>();
			List<NameValue> data = getIOParameterData(getInput());
			for (NameValue nameValue : data) {
				inputData.add(new WorkflowInstanceNodePortData(getWorkflowInstanceNode(), nameValue.name, nameValue.value));
			}
		}
		return inputData;
	}

	public void setInputData(List<WorkflowInstanceNodePortData> inputData) {
		this.inputData = inputData;
	}

	public List<WorkflowInstanceNodePortData> getOutputData() {
		if (outputData==null){
			outputData=new ArrayList<WorkflowInstanceNodePortData>();
			List<NameValue> data = getIOParameterData(getOutput());
			for (NameValue nameValue : data) {
				outputData.add(new WorkflowInstanceNodePortData(getWorkflowInstanceNode(), nameValue.name, nameValue.value));
			}
		}
		return outputData;
	}

	public void setOutputData(List<WorkflowInstanceNodePortData> outputData) {
		this.outputData = outputData;
	}

	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}

    public static void main(String[] args) {
        String input="molecule_id=3pom, geom_mol2=http://ccg-mw1.ncsa.uiuc.edu/cgenff/x_baya/cgenff_project/3pom/3pom.mol2, toppar_main_tgz=/u/ac/ccguser/proposed_dir_structure/toppar/cgenff/releases/2b7/main.tgz, toppar_usr_tgz=gsiftp://login-ember.ncsa.teragrid.org, toppar_mol_str=http://ccg-mw1.ncsa.uiuc.edu/cgenff/x_baya/cgenff_project/3pom/toppar/3pom.str, molecule_dir_in_tgz=, GC_UserName=x_baya, GC_ProjectName=x_baya, GC_WorkflowName=3pom__1349212666 | opt_freq_input_gjf=/gpfs2/scratch/users/ccguser/xbaya-workdirs/login-ember.ncsa.teragrid.org_application_Tue_Oct_02_17_18_34_EDT_2012_0933dae4-f7c7-4022-87d9-ab370c49a8bd/3pom/gauss/3pom_opt_freq_mp2.gjf, charmm_miminized_crd=/gpfs2/scratch/users/ccguser/xbaya-workdirs/login-ember.ncsa.teragrid.org_application_Tue_Oct_02_17_18_34_EDT_2012_0933dae4-f7c7-4022-87d9-ab370c49a8bd/3pom/generate/3pom_min.crd, step1_log=/gpfs2/scratch/users/ccguser/xbaya-workdirs/login-ember.ncsa.teragrid.org_application_Tue_Oct_02_17_18_34_EDT_2012_0933dae4-f7c7-4022-87d9-ab370c49a8bd/3pom/generate/generate.out, molecule_dir_out_tgz=/gpfs2/scratch/users/ccguser/xbaya-workdirs/login-ember.ncsa.teragrid.org_application_Tue_Oct_02_17_18_34_EDT_2012_0933dae4-f7c7-4022-87d9-ab370c49a8bd/molecule_dir_out.tgz, gcvars=/gpfs2/scratch/users/ccguser/xbaya-workdirs/login-ember.ncsa.teragrid.org_application_Tue_Oct_02_17_18_34_EDT_2012_0933dae4-f7c7-4022-87d9-ab370c49a8bd/GCVARS";

         getIOParameterData(input);
    }

    public WorkflowNodeType.WorkflowNode getType() {
        return type;
    }

    public void setType(WorkflowNodeType.WorkflowNode type) {
        this.type = type;
    }
}
