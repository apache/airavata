package org.apache.airavata.registry.core.entities.appcatalog;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the unicore_datamovement database table.
 * 
 */
@Entity
@Table(name="unicore_datamovement")
public class UnicoreDatamovement implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="DATAMOVEMENT_ID")
	private String datamovementId;

	@Column(name="SECURITY_PROTOCAL")
	private String securityProtocal;

	@Column(name="UNICORE_ENDPOINT_URL")
	private String unicoreEndpointUrl;

	public UnicoreDatamovement() {
	}

	public String getDatamovementId() {
		return datamovementId;
	}

	public void setDatamovementId(String datamovementId) {
		this.datamovementId = datamovementId;
	}

	public String getSecurityProtocal() {
		return securityProtocal;
	}

	public void setSecurityProtocal(String securityProtocal) {
		this.securityProtocal = securityProtocal;
	}

	public String getUnicoreEndpointUrl() {
		return unicoreEndpointUrl;
	}

	public void setUnicoreEndpointUrl(String unicoreEndpointUrl) {
		this.unicoreEndpointUrl = unicoreEndpointUrl;
	}
}