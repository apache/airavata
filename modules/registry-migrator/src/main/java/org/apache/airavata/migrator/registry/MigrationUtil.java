package org.apache.airavata.migrator.registry;

import org.apache.airavata.commons.gfac.type.ApplicationDeploymentDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.schemas.gfac.*;
import org.ogce.schemas.gfac.beans.ApplicationBean;
import org.ogce.schemas.gfac.beans.HostBean;
import org.ogce.schemas.gfac.beans.ServiceBean;
import org.ogce.schemas.gfac.beans.utils.ParamObject;

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

        ArrayList<ParamObject> inputParameterTypes = serviceBean.getMethodBean().getInputParms();
        List<InputParameterType> inputList = new ArrayList<InputParameterType>();
        if (inputParameterTypes != null) {
            for (ParamObject inputParameterType : inputParameterTypes) {
                InputParameterType input = InputParameterType.Factory.newInstance();
                input.setParameterName(inputParameterType.getName());
                input.setParameterDescription(inputParameterType.getDesc());

                ParameterType parameterType = input.addNewParameterType();
                parameterType.setType(DataType.Enum.forString(inputParameterType.getType()));
                parameterType.setName(inputParameterType.getType());

                inputList.add(input);
            }
            InputParameterType[] inputParamList = inputList.toArray(new InputParameterType[inputList.size()]);
            serv.getType().setInputParametersArray(inputParamList);
        }

        ArrayList<ParamObject> outputParameterTypes = serviceBean.getMethodBean().getOutputParms();
        List<OutputParameterType> outputList = new ArrayList<OutputParameterType>();
        if (outputParameterTypes != null){
            for (ParamObject outputParameterType : outputParameterTypes) {
                OutputParameterType output = OutputParameterType.Factory.newInstance();
                output.setParameterName(outputParameterType.getName());
                output.setParameterDescription(outputParameterType.getDesc());

                ParameterType parameterType = output.addNewParameterType();
                parameterType.setType(DataType.Enum.forString(outputParameterType.getType()));
                parameterType.setName(outputParameterType.getType());

                outputList.add(output);

            }
            OutputParameterType[] outputParamList = outputList.toArray(new OutputParameterType[outputList.size()]);
            serv.getType().setOutputParametersArray(outputParamList);
        }

        return serv;
    }

    private static ParameterType createParameterType(ParamObject outputParameterType) {
        String en = outputParameterType.getType();

        //DataType dt;
        if("String".equalsIgnoreCase(en)) {
            return StringParameterType.Factory.newInstance();
        } else if ("double".equalsIgnoreCase(en)) {
            return DoubleParameterType.Factory.newInstance();
        } else {
            // TODO check
            return StringParameterType.Factory.newInstance();
        }
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
        app.setScratchWorkingDirectory(appBean.getTmpDir());
//      TODO : following are not there in the OGCE schema
//        app.setInputDataDirectory("/tmp/input");
//        app.setOutputDataDirectory("/tmp/output");
//        app.setStandardOutput("/tmp/echo.stdout");
//        app.setStandardError("/tmp/echo.stdout");
        return appDesc;

    }
}
