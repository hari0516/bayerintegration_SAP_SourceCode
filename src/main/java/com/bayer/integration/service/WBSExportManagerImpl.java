/**
 * 
 */
package com.bayer.integration.service;


import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import javax.ws.rs.core.Cookie;

import com.bayer.integration.properties.GlobalConstants;
import com.bayer.integration.rest.wbs.BayerWBSAPIType;
import com.bayer.integration.rest.wbs.BayerWBSAPIResultType;
import com.ecosys.exception.SystemException;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;


/**
 * @author pwang
 *
 */
public class WBSExportManagerImpl extends ImportManagerBase implements
		ExportManager {

	/* (non-Javadoc)
	 * @see com.bayer.integration.service.ExportManager#ExportData()
	 */
	//@Override
    protected List<BayerWBSAPIType> bayerWBSAPI;
    
    //public ProjectExportManagerImpl(){}

    protected String projectId;

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public int ExportData() {
		int retCode=0;
		try{			
			//Create Web Service Client
			if (client == null) setClient(epcRestMgr.createClient(GlobalConstants.EPC_REST_USERNAME, GlobalConstants.EPC_REST_PASSWORD));
			
			//Read Project Data from EcoSys using project API
			Cookie session = null;
			String baseUri2 = GlobalConstants.EPC_REST_PROTOCOL + "://" + GlobalConstants.EPC_REST_SERVER + ":"
					+ GlobalConstants.EPC_REST_PORT + "/" + GlobalConstants.EPC_REST_BASEURI2;				
			
			//List<BayerProjectAPIType> projectAPITypes = importHelper.getBayerProjectAPITypes(client, projUri);
			HashMap<String, String> wbsMap = new HashMap<String, String>();
			wbsMap.put("ProjectId", projectId);
			bayerWBSAPI = this.getWBSAPITypes(client, session, baseUri2, wbsMap);
		}
		catch(SystemException se) {			
			logger.error("105 � Project Export Failed", se);			
			retCode=GlobalConstants.EXPORT_ECOSYS_PROJECT_FAILED;
		}catch(Exception e) {			
			logger.error("105 � Project Export Failed", e);			
			retCode=GlobalConstants.EXPORT_ECOSYS_PROJECT_FAILED;
		}
		
		if (retCode==GlobalConstants.EXPORT_ECOSYS_PROJECT_SUCCESS)
			logger.debug("100 � Project Export Completed Successfully");
		
		return retCode;
	}
	
	private List<BayerWBSAPIType> getWBSAPITypes(Client client, Cookie session, String baseUri, HashMap wbsMap) throws SystemException {
	
		ClientResponse response = epcRestMgr
			.getAsApplicationXml(client, baseUri, 
					GlobalConstants.EPC_REST_IMPORT_SAP_WBS,session, wbsMap);
	
		logger.debug(response);
		BayerWBSAPIResultType result = epcRestMgr.responseToObject(response, BayerWBSAPIResultType.class);
		List<BayerWBSAPIType> apiTypes = result.getBayerWBSAPI();
		if(session == null)
			session = epcRestMgr.getSessionCookie(response);
	
		if(!result.isSuccessFlag()){
			String errMsg="The interface failed to retrieve project list from EcoSys API; please verify connection.";
		if (result.getError() != null)
			errMsg=result.getError().toString();
		
			throw new SystemException(errMsg);
		} 
		return apiTypes;
	}
	
	public List<BayerWBSAPIType> getBayerWBSAPITypes() {
		return this.bayerWBSAPI;
	}
}
