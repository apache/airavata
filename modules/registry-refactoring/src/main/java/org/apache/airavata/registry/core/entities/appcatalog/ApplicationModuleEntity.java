package org.apache.airavata.registry.core.entities.appcatalog;

import java.io.Serializable;
import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

/**
 * The persistent class for the application_module database table.
 * 
 */
@Entity
@Table(name = "application_module")
public class ApplicationModuleEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "MODULE_ID")
	private String moduleId;

	@Column(name = "CREATION_TIME")
	private Timestamp creationTime;

	@Column(name = "GATEWAY_ID")
	private String gatewayId;

	@Column(name = "MODULE_DESC")
	private String moduleDesc;

	@Column(name = "MODULE_NAME")
	private String moduleName;

	@Column(name = "MODULE_VERSION")
	private String moduleVersion;

	@Column(name = "UPDATE_TIME")
	private Timestamp updateTime;

	public ApplicationModuleEntity() {
	}

	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
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

	public String getModuleDesc() {
		return moduleDesc;
	}

	public void setModuleDesc(String moduleDesc) {
		this.moduleDesc = moduleDesc;
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public String getModuleVersion() {
		return moduleVersion;
	}

	public void setModuleVersion(String moduleVersion) {
		this.moduleVersion = moduleVersion;
	}

	public Timestamp getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}
}