package org.apache.airavata.registry.core.entities.appcatalog;

import java.io.Serializable;
import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;


/**
 * The persistent class for the application_interface database table.
 * 
 */
@Entity
@Table(name="application_interface")
public class ApplicationInterfaceEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="INTERFACE_ID")
	private String interfaceId;

	@Column(name="APPLICATION_DESCRIPTION")
	private String applicationDescription;

	@Column(name="APPLICATION_NAME")
	private String applicationName;

	@Column(name="ARCHIVE_WORKING_DIRECTORY")
	private short archiveWorkingDirectory;

	@Column(name="CREATION_TIME")
	private Timestamp creationTime;

	@Column(name="GATEWAY_ID")
	private String gatewayId;

	@Column(name="UPDATE_TIME")
	private Timestamp updateTime;

	
	public ApplicationInterfaceEntity() {
	}

	public String getInterfaceId() {
		return interfaceId;
	}

	public void setInterfaceId(String interfaceId) {
		this.interfaceId = interfaceId;
	}

	public String getApplicationDescription() {
		return applicationDescription;
	}

	public void setApplicationDescription(String applicationDescription) {
		this.applicationDescription = applicationDescription;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public short getArchiveWorkingDirectory() {
		return archiveWorkingDirectory;
	}

	public void setArchiveWorkingDirectory(short archiveWorkingDirectory) {
		this.archiveWorkingDirectory = archiveWorkingDirectory;
	}

	public Timestamp getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(Timestamp creationTime) {
		this.creationTime = creationTime;
	}

	public String getGatewayId() {
		return gatewayId;
	}

	public void setGatewayId(String gatewayId) {
		this.gatewayId = gatewayId;
	}

	public Timestamp getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}
}