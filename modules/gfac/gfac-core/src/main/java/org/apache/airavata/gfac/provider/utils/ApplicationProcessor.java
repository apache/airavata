package org.apache.airavata.gfac.provider.utils;

import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.schemas.gfac.ExtendedKeyValueType;
import org.apache.airavata.schemas.gfac.HpcApplicationDeploymentType;
import org.apache.airavata.schemas.gfac.JobTypeType;
import org.apache.airavata.schemas.gfac.NameValuePairType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.ApplicationType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionType;
import org.ggf.schemas.jsdl.x2005.x11.jsdlPosix.EnvironmentType;
import org.ggf.schemas.jsdl.x2005.x11.jsdlPosix.FileNameType;
import org.ggf.schemas.jsdl.x2005.x11.jsdlPosix.UserNameType;
import org.ogf.schemas.jsdl.x2007.x02.jsdlSpmd.NumberOfProcessesType;
import org.ogf.schemas.jsdl.x2007.x02.jsdlSpmd.ProcessesPerHostType;
import org.ogf.schemas.jsdl.x2007.x02.jsdlSpmd.ThreadsPerProcessType;

import java.io.File;


public class ApplicationProcessor {
	
	public static void generateJobSpecificAppElements(JobDefinitionType value, JobExecutionContext context){
		
		String userName = getUserNameFromContext(context);
		if (userName.equalsIgnoreCase("admin")){
			userName = "CN=zdv575, O=Ultrascan Gateway, C=DE";
		}
		
		HpcApplicationDeploymentType appDepType = (HpcApplicationDeploymentType) context
				.getApplicationContext().getApplicationDeploymentDescription()
				.getType();
		
		createGenericApplication(value, appDepType);
		
		if (appDepType.getApplicationEnvironmentArray().length > 0) {
			createApplicationEnvironment(value,
					appDepType.getApplicationEnvironmentArray(), appDepType);
		}

		
		if (appDepType.getExecutableLocation() != null) {
			FileNameType fNameType = FileNameType.Factory.newInstance();
			fNameType.setStringValue(appDepType.getExecutableLocation());
			if(isParallelJob(appDepType)) {
				JSDLUtils.getOrCreateSPMDApplication(value).setExecutable(fNameType);
				JSDLUtils.getSPMDApplication(value).setSPMDVariation(getSPMDVariation(appDepType));
				
				if(getValueFromMap(appDepType, JSDLUtils.NUMBEROFPROCESSES)!=null){
					NumberOfProcessesType num = NumberOfProcessesType.Factory.newInstance();
					num.setStringValue(getValueFromMap(appDepType, JSDLUtils.NUMBEROFPROCESSES));
					JSDLUtils.getSPMDApplication(value).setNumberOfProcesses(num);
				}
							
				if(getValueFromMap(appDepType, JSDLUtils.PROCESSESPERHOST)!=null){
					ProcessesPerHostType pph = ProcessesPerHostType.Factory.newInstance();
					pph.setStringValue(getValueFromMap(appDepType, JSDLUtils.PROCESSESPERHOST));
					JSDLUtils.getSPMDApplication(value).setProcessesPerHost(pph);
				}
				
				if(getValueFromMap(appDepType, JSDLUtils.THREADSPERHOST)!=null){
					ThreadsPerProcessType tpp = ThreadsPerProcessType.Factory.newInstance();
					tpp.setStringValue(getValueFromMap(appDepType, JSDLUtils.THREADSPERHOST));
					JSDLUtils.getSPMDApplication(value).setThreadsPerProcess(tpp);
					
				}
				
				if(userName != null) {
					UserNameType userNameType = UserNameType.Factory.newInstance();
					userNameType.setStringValue(userName);
					JSDLUtils.getSPMDApplication(value).setUserName(userNameType);
				}
			}
			else {
				JSDLUtils.getOrCreatePOSIXApplication(value).setExecutable(fNameType);
				if(userName != null) {
					UserNameType userNameType = UserNameType.Factory.newInstance();
					userNameType.setStringValue(userName);
					JSDLUtils.getOrCreatePOSIXApplication(value).setUserName(userNameType);
				}
			}
		}
		

		String stdout = (appDepType.getStandardOutput() != null) ? new File(appDepType.getStandardOutput()).getName(): "stdout"; 
		ApplicationProcessor.setApplicationStdOut(value, appDepType, stdout);
		
	
		String stderr = (appDepType.getStandardError() != null) ? new File(appDepType.getStandardError()).getName() : "stderr"; 
		ApplicationProcessor.setApplicationStdErr(value, appDepType, stderr);
	
	}
	
	public static String getUserNameFromContext(JobExecutionContext jobContext) {
		if(jobContext.getContextHeader() == null)
			return null;
		return jobContext.getContextHeader().getUserIdentifier();
	}
	public static boolean isParallelJob(HpcApplicationDeploymentType appDepType) {
		
		boolean isParallel = false;
		
		if (appDepType.getJobType() != null) {
			// TODO set data output directory
			int status = appDepType.getJobType().intValue();

			switch (status) {
			// TODO: this check should be done outside this class
			case JobTypeType.INT_MPI:
			case JobTypeType.INT_OPEN_MP:
				isParallel = true;
				break;
				
			case JobTypeType.INT_SERIAL:
			case JobTypeType.INT_SINGLE:
				isParallel = false;
				break;

			default:
				isParallel = false;
				break;
			}
		}
		return isParallel;
	}

	
	public static void createApplicationEnvironment(JobDefinitionType value, NameValuePairType[] nameValuePairs, HpcApplicationDeploymentType appDepType) {
		
		if(isParallelJob(appDepType)) {
			for (NameValuePairType nv : nameValuePairs) {
				EnvironmentType envType = JSDLUtils.getOrCreateSPMDApplication(value).addNewEnvironment();
				envType.setName(nv.getName());
				envType.setStringValue(nv.getValue());
			}
		}
		else {
			for (NameValuePairType nv : nameValuePairs) {
				EnvironmentType envType = JSDLUtils.getOrCreatePOSIXApplication(value).addNewEnvironment();
				envType.setName(nv.getName());
				envType.setStringValue(nv.getValue());
			}
		}

	}
	
	
	public static String getSPMDVariation (HpcApplicationDeploymentType appDepType) {
		
		String variation = null;
		
		if (appDepType.getJobType() != null) {
			// TODO set data output directory
			int status = appDepType.getJobType().intValue();

			switch (status) {
			// TODO: this check should be done outside this class
			case JobTypeType.INT_MPI:
				variation = SPMDVariations.MPI.value();				
				break;
				
			case JobTypeType.INT_OPEN_MP:
				variation = SPMDVariations.OpenMPI.value();
				break;
				
			}
		}
		return variation;
	}
	
	
	public static void addApplicationArgument(JobDefinitionType value, HpcApplicationDeploymentType appDepType, String stringPrm) {
		if(isParallelJob(appDepType)) 		
			JSDLUtils.getOrCreateSPMDApplication(value)
			.addNewArgument().setStringValue(stringPrm);
		else 
		    JSDLUtils.getOrCreatePOSIXApplication(value)
				.addNewArgument().setStringValue(stringPrm);

	}
	
	public static void setApplicationStdErr(JobDefinitionType value, HpcApplicationDeploymentType appDepType, String stderr) {
		FileNameType fName = FileNameType.Factory.newInstance();
		fName.setStringValue(stderr);
		if (isParallelJob(appDepType)) 
			JSDLUtils.getOrCreateSPMDApplication(value).setError(fName);
		else 
			JSDLUtils.getOrCreatePOSIXApplication(value).setError(fName);
	}
	
	public static void setApplicationStdOut(JobDefinitionType value, HpcApplicationDeploymentType appDepType, String stderr) {
		FileNameType fName = FileNameType.Factory.newInstance();
		fName.setStringValue(stderr);
		if (isParallelJob(appDepType)) 
			JSDLUtils.getOrCreateSPMDApplication(value).setOutput(fName);
		else 
			JSDLUtils.getOrCreatePOSIXApplication(value).setOutput(fName);
	}
	
	public static String getApplicationStdOut(JobDefinitionType value, HpcApplicationDeploymentType appDepType) throws RuntimeException {
		if (isParallelJob(appDepType)) return JSDLUtils.getOrCreateSPMDApplication(value).getOutput().getStringValue();
		else return JSDLUtils.getOrCreatePOSIXApplication(value).getOutput().getStringValue();
	}
	
	public static String getApplicationStdErr(JobDefinitionType value, HpcApplicationDeploymentType appDepType) throws RuntimeException {
		if (isParallelJob(appDepType)) return JSDLUtils.getOrCreateSPMDApplication(value).getError().getStringValue();
		else return JSDLUtils.getOrCreatePOSIXApplication(value).getError().getStringValue();
	}
	
	public static void createGenericApplication(JobDefinitionType value, HpcApplicationDeploymentType appDepType) {
		if (appDepType.getApplicationName() != null) {
			ApplicationType appType = JSDLUtils.getOrCreateApplication(value);
			String appName = appDepType.getApplicationName()
					.getStringValue();
			appType.setApplicationName(appName);
			JSDLUtils.getOrCreateJobIdentification(value).setJobName(appName);
		}
	}
	
	
	public static String getValueFromMap(HpcApplicationDeploymentType appDepType, String name) {
		ExtendedKeyValueType[] extended = appDepType.getKeyValuePairsArray();
		for(ExtendedKeyValueType e: extended) {
			if(e.getName().equalsIgnoreCase(name)) {
				return e.getStringValue();
			}
		}
		return null;
	}
	
}
