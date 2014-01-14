package org.apache.airavata.registry.api.orchestrator;

import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;

import org.apache.airavata.registry.api.orchestrator.impl.OrchestratorDataImpl;

@WebService
@XmlSeeAlso(OrchestratorDataImpl.class)
public interface OrchestratorData {

	/**
	 * Returns the orchestrator run id
	 * 
	 * @return
	 */
	public int getOrchestratorId();

	/**
	 * 
	 * @return the unique experiment id
	 */
	public String getExperimentId();

	/**
	 * Returns the user of the run
	 * 
	 * @return
	 */
	public String getUser();

	/**
	 * Returns GFAC service URL
	 * 
	 * @return
	 */
	public String getGFACServiceEPR();

	/**
	 * Returns state of processing
	 * 
	 * @return
	 */
	public String getState();

	/**
	 * Returns run status
	 * 
	 * @return
	 */
	public String getStatus();

	/**
	 * 
	 * @param experimentId
	 */
	public void setExperimentId(String experimentId);

	/**
	 * 
	 * @param user
	 */
	public void setUser(String user);

	/**
	 * 
	 * @param gfacEPR
	 */
	public void setGFACServiceEPR(String gfacEPR);
	
	/**
	 * 
	 * @param state
	 */
	public void setState(String state);
	
	/**
	 * 
	 * @param status
	 */
	public void setStatus(String status);


	
}
