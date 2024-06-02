/**
 * 
 */
package com.bayer.integration.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.bayer.integration.persistence.StagingDatabaseManager;
import com.ecosys.exception.SystemException;
import com.ecosys.service.EpcRestMgr;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;


/**
 * @author sdas
 *
 */
public class ExportManagerBase {

	protected static Logger logger = Logger.getLogger(ExportManagerBase.class);
	
	@Autowired
	protected EpcRestMgr epcRestMgr;
	
	@Autowired
	protected StagingDatabaseManager stgDBMgr;	
	
	public void setEpcRestMgr(EpcRestMgr epcRestMgr){
		this.epcRestMgr = epcRestMgr;
	}

	public void setStgDBMgr(StagingDatabaseManager stgDBMgr){
		this.stgDBMgr=stgDBMgr;
	}
	
	protected Client client = null;
	
	public void setClient(Client client) { 
		this.client = client; 
	}
}

