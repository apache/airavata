package org.apache.airavata.gfac.bes.handlers;


import org.apache.airavata.gfac.core.context.JobExecutionContext;
import org.apache.airavata.gfac.core.handler.GFacHandler;
import org.apache.airavata.gfac.core.handler.GFacHandlerException;
import org.apache.airavata.gfac.core.provider.GFacProviderException;

/**
 * Download upload job's input files to the temporary SMS directory.
 * 
 * */
public class SMSByteIOInHandler extends AbstractSMSHandler implements GFacHandler {


	@Override
	public void invoke(JobExecutionContext jobExecutionContext) throws GFacHandlerException	{
		super.invoke(jobExecutionContext);
    try{    
        	if(jobExecutionContext.getInMessageContext().getParameters().size() < 1) return;
        	dataTransferrer.uploadLocalFiles();
	} catch (GFacProviderException e) {
		throw new GFacHandlerException("Cannot upload local data",e);
	}

 	}
	
	
}