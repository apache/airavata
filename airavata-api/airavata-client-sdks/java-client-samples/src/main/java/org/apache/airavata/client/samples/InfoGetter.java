package org.apache.airavata.client.samples;

import org.apache.airavata.api.Airavata;
import org.apache.airavata.api.client.AiravataClientFactory;
import org.apache.airavata.client.tools.RegisterSampleApplicationsUtils;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationParallelismType;
import org.apache.airavata.model.appcatalog.appinterface.DataType;
import org.apache.airavata.model.appcatalog.appinterface.InputDataObjectType;
import org.apache.airavata.model.appcatalog.appinterface.OutputDataObjectType;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.computeresource.DataMovementInterface;
import org.apache.airavata.model.appcatalog.computeresource.DataMovementProtocol;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionInterface;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionProtocol;
import org.apache.airavata.model.appcatalog.computeresource.LOCALSubmission;
import org.apache.airavata.model.appcatalog.computeresource.ResourceJobManager;
import org.apache.airavata.model.appcatalog.computeresource.ResourceJobManagerType;
import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.model.error.AiravataClientConnectException;
import org.apache.airavata.model.error.AiravataClientException;
import org.apache.airavata.model.error.AiravataSystemException;
import org.apache.airavata.model.error.InvalidRequestException;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.thrift.TException;
import org.omg.CORBA.DynAnyPackage.Invalid;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Doug on 6/6/15.
 */
public class InfoGetter {

    private static final String THRIFT_SERVER_HOST = "127.0.0.1";
    private static final int THRIFT_SERVER_PORT = 8930;

    private Airavata.Client airavataClient;
    private String localhost_ip = "127.0.0.1";

    public static void main(String[] args) throws AiravataClientConnectException, TException, InvalidRequestException, AiravataClientException, AiravataSystemException  {
        System.out.println("Hello World");
        InfoGetter infoGetter = new InfoGetter();

        // activate the connection
        infoGetter.register();

        // get bigRed description
        String bigRed = "fsd-cloud15.zam.kfa-juelich.de_212e906e-6cf5-427d-94b1-8f4a10c9cca3";
        ComputeResourceDescription before = infoGetter.getDescription(bigRed);

        // change active
        System.out.println(before.isActive());
        before.setActive(true);
        System.out.println(before.isActive());

        // check if resource updated
        infoGetter.updateComputeResource(bigRed, before);
        ComputeResourceDescription after = infoGetter.getDescription(bigRed);
        System.out.println(after.isActive());
    }


    /*
    bool updateComputeResource(1: required string computeResourceId,
                               2: required computeResourceModel.ComputeResourceDescription computeResourceDescription)
            throws (1: airavataErrors.InvalidRequestException ire,
    2: airavataErrors.AiravataClientException ace,
    3: airavataErrors.AiravataSystemException ase)
    */
    public void updateComputeResource(String computeResourceId, ComputeResourceDescription computeResourceDescription) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException  {
        System.out.format("Update: %b\n", airavataClient.updateComputeResource(computeResourceId, computeResourceDescription));
    }

    public ComputeResourceDescription getDescription(String computeResourceId) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException  {
        try {
            ComputeResourceDescription c;
            c = airavataClient.getComputeResource(computeResourceId);
            System.out.format("Print Resource: %s\n", c);
            return c;
        }
        catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }

    public void getResources() throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        Map<String, String> map = airavataClient.getAllComputeResourceNames();
        for (Map.Entry<String, String> o: map.entrySet()) {
            System.out.println(o.getKey());
        }
    }

    public void register() throws AiravataClientConnectException, TException {
        airavataClient = AiravataClientFactory.createAiravataClient(THRIFT_SERVER_HOST, THRIFT_SERVER_PORT);
        System.out.println("Create Airavata Client - Success");
    }
}
