/**
 * 
 */
package com.bayer.integration.service;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.bayer.integration.persistence.StagingDatabaseManager;
import com.bayer.integration.sapservice.SAPGatewayODataServiceManager;
import com.bayer.integration.service.ProjectExportManagerImpl;
import com.bayer.integration.service.WBSExportManagerImpl;
import com.bayer.integration.utils.EpcIntegrationLog;
import com.bayer.integration.service.ImportManagerHelper;
import com.ecosys.exception.SystemException;
import com.ecosys.service.EpcRestMgr;
import com.sun.jersey.api.client.Client;


/**
 * @author sdas
 *
 */
public class ImportManagerBase {

	protected static Logger logger = Logger.getLogger(ImportManagerBase.class);
	
	@Autowired
	protected EpcRestMgr epcRestMgr;
	
	@Autowired
	protected StagingDatabaseManager stgDBMgr;	
	
	@Autowired
	protected ProjectExportManagerImpl prjExpMgr;
	
	@Autowired
	protected WBSExportManagerImpl wbsExpMgr;
	public void setWbsExpMgr(WBSExportManagerImpl wbsExpMgr) {
		this.wbsExpMgr = wbsExpMgr;
	}
	
	@Autowired
	protected ImportManagerHelper importHelper;	

	public void setImportHelper(ImportManagerHelper importHelper) {
		this.importHelper = importHelper;
	}

	public void setPrjExpMgr(ProjectExportManagerImpl prjExpMgr) {
		this.prjExpMgr = prjExpMgr;
	}

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
	
	protected SAPGatewayODataServiceManager odataSvcMgr = null;
	
	public void setOdataSvcMgr(SAPGatewayODataServiceManager odataSvcMgr) { 
		this.odataSvcMgr = odataSvcMgr; 
	}
}

