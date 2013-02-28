package org.apache.airavata.gfac.provider.utils;

import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.schemas.gfac.HpcApplicationDeploymentType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionType;

public class ResourceProcessor {

	
	public static void generateResourceElements(JobDefinitionType value, JobExecutionContext context) throws Exception{
		
		HpcApplicationDeploymentType appDepType = (HpcApplicationDeploymentType) context
				.getApplicationContext().getApplicationDeploymentDescription()
				.getType();
		
		createMemory(value, appDepType);
		
		if (appDepType.getCpuCount() > 0) {
			RangeValueType rangeType = new RangeValueType();
			rangeType.setLowerBound(Double.NaN);
			rangeType.setUpperBound(Double.NaN);
			rangeType.setExact(appDepType.getCpuCount());
			JSDLUtils.setTotalCPUCountRequirements(value, rangeType);
		}

		if (appDepType.getProcessorsPerNode() > 0) {
			RangeValueType rangeType = new RangeValueType();
			rangeType.setLowerBound(Double.NaN);
			rangeType.setUpperBound(Double.NaN);
			rangeType.setExact(appDepType.getProcessorsPerNode());
			JSDLUtils.setIndividualCPUCountRequirements(value, rangeType);
		}
		
		if (appDepType.getNodeCount() > 0) {
			RangeValueType rangeType = new RangeValueType();
			rangeType.setLowerBound(Double.NaN);
			rangeType.setUpperBound(Double.NaN);
			rangeType.setExact(appDepType.getNodeCount());
			JSDLUtils.setTotalResourceCountRequirements(value, rangeType);
		}

	}
	
	
	private static void createMemory(JobDefinitionType value, HpcApplicationDeploymentType appDepType){
		if (appDepType.getMinMemory() > 0 && appDepType.getMaxMemory() > 0) {
			RangeValueType rangeType = new RangeValueType();
			rangeType.setLowerBound(appDepType.getMinMemory());
			rangeType.setUpperBound(appDepType.getMaxMemory());
			JSDLUtils.setIndividualPhysicalMemoryRequirements(value, rangeType);
		}

		else if (appDepType.getMinMemory() > 0 && appDepType.getMaxMemory() <= 0) {
			// TODO set Wall time
			RangeValueType rangeType = new RangeValueType();
			rangeType.setLowerBound(appDepType.getMinMemory());
			JSDLUtils.setIndividualPhysicalMemoryRequirements(value, rangeType);
		}
		
		else if (appDepType.getMinMemory() <= 0 && appDepType.getMaxMemory() > 0) {
			// TODO set Wall time
			RangeValueType rangeType = new RangeValueType();
			rangeType.setUpperBound(appDepType.getMinMemory());
			JSDLUtils.setIndividualPhysicalMemoryRequirements(value, rangeType);
		}
		
	}

	
	

	
}
