//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.11.28 at 05:19:16 PM CET 
//


package com.bayer.integration.persistence;

import com.bayer.integration.rest.polco.BayerCommitmentLICOAPIType;

/**
Author: Pwang
 */

public class PolcoApiErrorDAO extends BayerCommitmentLICOAPIType {
	protected long id;
    protected String status;
    protected String errorMsg;
	protected String rootCostObjectID;
	
	public String getRetired() {
		return retired;
	}
	public void setRetired(String retired) {
		this.retired = retired;
	}
	protected String retired;
    
    public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getErrorMsg() {
		return errorMsg;
	}
	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}
    public String getRootCostObjectID() {
		return rootCostObjectID;
	}
	public void setRootCostObjectID(String rootCostObjectID) {
		this.rootCostObjectID = rootCostObjectID;
	}
}
