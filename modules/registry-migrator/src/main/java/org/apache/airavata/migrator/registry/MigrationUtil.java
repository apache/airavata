package org.apache.airavata.migrator.registry;

import org.apache.airavata.commons.gfac.type.ApplicationDeploymentDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.schemas.gfac.*;
import org.ogce.schemas.gfac.beans.ApplicationBean;
import org.ogce.schemas.gfac.beans.HostBean;
import org.ogce.schemas.gfac.beans.ServiceBean;

import java.util.ArrayList;
import java.util.List;

public class MigrationUtil {
    /**
     * Creates a HostDescription from HostBean
     *
     * @param hostBean HostBean
     * @return HostDescription
     */
    public static HostDescription createHostDescription(HostBean hostBean) {
        HostDescription host = new HostDescription();
        if(hostBean.getGateKeeperendPointReference()!=null ||
                hostBean.getGridFtpendPointReference()!=null) {
            host.getType().changeType(GlobusHostType.type);
            host.getType().setHostName(hostBean.getHostName());
            host.getType().setHostAddress(hostBean.getHostName());
            ((GlobusHostType) host.getType()).
                    setGridFTPEndPointArray(new String[]{hostBean.getGridFtpendPointReference()});
            ((GlobusHostType) host.getType()).
                    setGlobusGateKeeperEndPointArray(new String[]{hostBean.getGateKeeperendPointReference()});
        } else {
            host.getType().setHostName(hostBean.getHostName());
            host.getType().setHostAddress(hostBean.getHostName());
        }
        return host;
    }

    /**
     * Creates ServiceDescription from ServiceBean
     * @param serviceBean ServiceBean
     * @return ServiceDescription
     */
    public static ServiceDescription createServiceDescription(ServiceBean serviceBean) {
        ServiceDescription serv = new ServiceDescription();
        serv.getType().setName(serviceBean.getServiceName());

        org.ogce.schemas.gfac.documents.InputParameterType[] inputParameterTypes = serviceBean.getInputParam();
        List<InputParameterType> inputList = new ArrayList<InputParameterType>();
        if (inputParameterTypes != null) {
            for (org.ogce.schemas.gfac.documents.InputParameterType inputParameterType : inputParameterTypes) {
                InputParameterType input = InputParameterType.Factory.newInstance();
                String inputParamName = inputParameterType.getParameterName();
                input.setParameterName(inputParamName);

                //TODO
                org.ogce.schemas.gfac.documents.InputDataType.Enum en = inputParameterType.getParameterType();
                input.setParameterType(StringParameterType.Factory.newInstance());
                inputList.add(input);
            }
            InputParameterType[] inputParamList = inputList.toArray(new InputParameterType[inputList.size()]);
            serv.getType().setInputParametersArray(inputParamList);
        }

        org.ogce.schemas.gfac.documents.OutputParameterType[] outputParameterTypes = serviceBean.getOutputParam();
        List<OutputParameterType> outputList = new ArrayList<OutputParameterType>();
        if (outputParameterTypes != null){
            for (org.ogce.schemas.gfac.documents.OutputParameterType outputParameterType : outputParameterTypes) {
                String outputParamName = outputParameterType.getParameterName();
                OutputParameterType output = OutputParameterType.Factory.newInstance();
                output.setParameterName(outputParamName);

                //TODO
                org.ogce.schemas.gfac.documents.OutputDataType.Enum en = outputParameterType.getParameterType();
                output.setParameterType(StringParameterType.Factory.newInstance());
                outputList.add(output);

            }
            OutputParameterType[] outputParamList = outputList.toArray(new OutputParameterType[outputList.size()]);
            serv.getType().setOutputParametersArray(outputParamList);
        }

        return serv;
    }

    /**
     * Creates ApplicationDeploymentDescription from ApplicationBean
     * @param appBean ApplicationBean
     * @return ApplicationDeploymentDescription
     */
    public static ApplicationDeploymentDescription createAppDeploymentDescription(ApplicationBean appBean) {
        ApplicationDeploymentDescription appDesc = new ApplicationDeploymentDescription();
        ApplicationDeploymentDescriptionType app = appDesc.getType();
        ApplicationDeploymentDescriptionType.ApplicationName name =
                ApplicationDeploymentDescriptionType.ApplicationName.Factory.newInstance();
        name.setStringValue(appBean.getApplicationName());
        app.setApplicationName(name);

        app.setExecutableLocation(appBean.getExecutable());
        app.setScratchWorkingDirectory(appBean.getWorkDir());
//      TODO : following are not there in the OGCE schema
        app.setInputDataDirectory("/tmp/input");
        app.setOutputDataDirectory("/tmp/output");
        app.setStandardOutput("/tmp/echo.stdout");
        app.setStandardError("/tmp/echo.stdout");
        return appDesc;

    }
}
