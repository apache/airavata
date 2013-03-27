package org.apache.airavata.gfac.provider.utils;

import org.apache.airavata.commons.gfac.type.ActualParameter;
import org.apache.airavata.commons.gfac.type.MappingFactory;
import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.gfac.context.MessageContext;
import org.apache.airavata.gfac.provider.GFacProviderException;
import org.apache.airavata.schemas.gfac.InputParameterType;

import java.util.ArrayList;
import java.util.List;

public class ProviderUtils {

    public static List<String> getInputParameters(JobExecutionContext jobExecutionContext) throws GFacProviderException {
        List<String> parameters = new ArrayList<String>();
        MessageContext inMessageContext = jobExecutionContext.getInMessageContext();
        InputParameterType[] inputParamDefinitionArray = jobExecutionContext.getApplicationContext().
                getServiceDescription().getType().getInputParametersArray();
        for (InputParameterType inputParam : inputParamDefinitionArray) {
            String parameterName = inputParam.getParameterName();
            ActualParameter parameter = (ActualParameter)inMessageContext.getParameter(parameterName);
            if(parameter == null){
                throw new GFacProviderException("Cannot find required input parameter " + parameterName + ".");
            }

            parameters.add(MappingFactory.toString(parameter));
        }

        return parameters;
    }

}
