package org.airavata.appcatalog.cpi;

import org.apache.airavata.model.computehost.*;

import java.util.List;
import java.util.Map;

public interface ComputeResource {
    String addComputeResource (ComputeResourceDescription description);

    String addSSHJobSubmission (SSHJobSubmission sshJobSubmission);

    void addSSHJobSubmissionProtocol(String computeResourceId, String jobSubmissionId);

    String addGSISSHJobSubmission (GSISSHJobSubmission gsisshJobSubmission);

    void addGSISSHJobSubmissionProtocol (String computeResourceId, String jobSubmissionId);

    String addGlobusJobSubmission (GlobusJobSubmission globusJobSubmission);

    void addGlobusJobSubmissionProtocol (String computeResourceId, String jobSubmissionId);

    String addScpDataMovement (SCPDataMovement scpDataMovement);

    void addScpDataMovementProtocol (String computeResourceId, String dataMoveId);

    String addGridFTPDataMovement (GridFTPDataMovement gridFTPDataMovement);

    void addGridFTPDataMovementProtocol (String computeResourceId, String dataMoveId);

    ComputeResourceDescription getComputeResource (String resourceId);

    List<ComputeResourceDescription> getComputeResourceList (Map<String, String> filters);

    GSISSHJobSubmission getGSISSHJobSubmission (String submissionId);

    List<GSISSHJobSubmission> getGSISSHJobSubmissionList (Map<String, String> filters);

    GlobusJobSubmission getGlobusJobSubmission (String submissionId);

    List<GlobusJobSubmission> getGlobusJobSubmissionList (Map<String, String> filters);

    SCPDataMovement getSCPDataMovement (String dataMoveId);

    List<SCPDataMovement> getSCPDataMovementList (Map<String, String> filters);

    GridFTPDataMovement getGridFTPDataMovement (String dataMoveId);

    List<GridFTPDataMovement> getGridFTPDataMovementList (Map<String, String> filters);

    boolean isComputeResourceExists (String resourceId);

}
