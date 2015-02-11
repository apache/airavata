package org.apache.ariavata.simple.workflow.engine;

import org.apache.airavata.model.appcatalog.appinterface.InputDataObjectType;
import org.apache.airavata.model.appcatalog.appinterface.OutputDataObjectType;

/**
 * Created by shameera on 2/9/15.
 */
public class WorkflowUtil {

    public static InputDataObjectType copyValues(InputDataObjectType fromInputObj, InputDataObjectType toInputObj){
        toInputObj.setValue(fromInputObj.getValue());
        if (fromInputObj.getApplicationArgument() != null
                && !fromInputObj.getApplicationArgument().trim().equals("")) {
            toInputObj.setApplicationArgument(fromInputObj.getApplicationArgument());
        }
        return fromInputObj;
    }

    public static InputDataObjectType copyValues(OutputDataObjectType outputData, InputDataObjectType inputData) {
        inputData.setValue(outputData.getValue());
        return inputData;
    }

}
