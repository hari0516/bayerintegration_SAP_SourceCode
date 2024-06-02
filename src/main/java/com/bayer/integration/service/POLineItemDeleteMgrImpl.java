/**
 * 
 */
package com.bayer.integration.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.core.Cookie;
import javax.xml.bind.JAXBElement;

import org.joda.time.format.ISOPeriodFormat;

import com.bayer.integration.odata.SapPOLODataType;
import com.bayer.integration.properties.GlobalConstants;
import com.bayer.integration.rest.project.BayerProjectAPIType;
import com.bayer.integration.rest.project.BayerProjectAPIResultType;
import com.bayer.integration.rest.pol.BayerCommitmentLIAPIRequestType;
import com.bayer.integration.rest.pold.BayerCommitmentPOLIDeleteAPIResultType;
import com.bayer.integration.rest.pold.DocumentLinkType;
import com.bayer.integration.rest.pold.ErrorType;
import com.bayer.integration.rest.pold.PerformanceType;
import com.bayer.integration.rest.pold.PoldObjectFactory;
import com.ecosys.exception.SystemException;
import com.ecosys.util.Stopwatch;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.org.apache.xerces.internal.dom.ElementNSImpl;

/**
 * @author pwng
 *
 */
public class POLineItemDeleteMgrImpl extends ImportManagerBase {

	/* (non-Javadoc)
	 * @see com.bayer.integration.service.ImportManager#importData()
	 */
	//@Override
	public int DeleteData() {
		int retCode=GlobalConstants.IMPORT_SAP_POL_SUCCESS;
		try{			
			if (!GlobalConstants.SKIP_SAP_POL_INTERFACE) {
				
				//Create Web Service Client
				if (client == null) setClient(epcRestMgr.createClient(GlobalConstants.EPC_REST_USERNAME, GlobalConstants.EPC_REST_PASSWORD));
				
				//Read Project Data from EcoSys using project API
				Cookie session = null;
				ImportManagerHelper importHelper = new ImportManagerHelper();
				ProjectExportManagerImpl projectExportMgr = new ProjectExportManagerImpl();
				projectExportMgr.setClient(client);
				projectExportMgr.setEpcRestMgr(epcRestMgr);			
				projectExportMgr.ExportData();
				List<BayerProjectAPIType> projectAPITypes = projectExportMgr.getBayerProjectAPITypes();
							
				String baseUri2 = GlobalConstants.EPC_REST_PROTOCOL + "://" + GlobalConstants.EPC_REST_SERVER + ":"
						+ GlobalConstants.EPC_REST_PORT + "/" + GlobalConstants.EPC_REST_BASEURI2;	
				
				for (int i = 0; i < projectAPITypes.size(); i++) 
		 		{
					BayerProjectAPIType projectAPIType = projectAPITypes.get(i);
					String projectId = projectAPIType.getID();
					
					//Loop through the project List to get PMO List from Bayer Web Services
						
					//Invoke EcoSys API with input records
					if (projectId.equals("A00GV-999990")) {						
						session = null;
						
						List<String> processMessage = deleteLIs(projectId);
						//Process Status Messages
						logger.debug("Processing Error Messages, If Any");
						processStatusMessages();
					}
		 		}
				
			} else {
				logger.info("Skipped Project Interface. Change the skip property to 'false'");				
			}
			
		}
		catch(SystemException se) {			
			logger.error("5005 � PO Line Item Import Failed", se);			
			retCode=GlobalConstants.IMPORT_SAP_POL_FAILED;
		}catch(Exception e) {			
			logger.error("5005 � PO Line Item Import Failed", e);			
			retCode=GlobalConstants.IMPORT_SAP_POL_FAILED;
		}
		
		if (retCode==GlobalConstants.IMPORT_SAP_POL_SUCCESS)
			logger.debug("5000 � PO Line Item Import Completed Successfully");
		
		return retCode;
	}
	

	public int DeleteData(String projectId) {
		int retCode=GlobalConstants.IMPORT_SAP_POL_SUCCESS;
		try{			
			if (!GlobalConstants.SKIP_SAP_POL_INTERFACE) {
				
				//Create Web Service Client
				if (client == null) setClient(epcRestMgr.createClient(GlobalConstants.EPC_REST_USERNAME, GlobalConstants.EPC_REST_PASSWORD));
				
				//Read Project Data from EcoSys using project API
				Cookie session = null;
				ImportManagerHelper importHelper = new ImportManagerHelper();
				ProjectExportManagerImpl projectExportMgr = new ProjectExportManagerImpl();
				projectExportMgr.setClient(client);
				projectExportMgr.setEpcRestMgr(epcRestMgr);			
				projectExportMgr.ExportData();
				List<BayerProjectAPIType> projectAPITypes = projectExportMgr.getBayerProjectAPITypes();
							
				String baseUri2 = GlobalConstants.EPC_REST_PROTOCOL + "://" + GlobalConstants.EPC_REST_SERVER + ":"
						+ GlobalConstants.EPC_REST_PORT + "/" + GlobalConstants.EPC_REST_BASEURI2;	
				
				for (int i = 0; i < projectAPITypes.size(); i++) 
		 		{
					BayerProjectAPIType projectAPIType = projectAPITypes.get(i);
					//projectId = projectAPIType.getID();
					
					//Loop through the project List to get PMO List from Bayer Web Services
						
					//Invoke EcoSys API with input records
					if (projectId.equals(projectAPIType.getID())) {						
						session = null;
						
						List<String> processMessage = deleteLIs(projectId);
						//Process Status Messages
						logger.debug("Processing Error Messages, If Any");
						processStatusMessages();
					}
		 		}
				
			} else {
				logger.info("Skipped Project Interface. Change the skip property to 'false'");				
			}
			
		}
		catch(SystemException se) {			
			logger.error("5005 � PO Line Item Import Failed", se);			
			retCode=GlobalConstants.IMPORT_SAP_POL_FAILED;
		}catch(Exception e) {			
			logger.error("5005 � PO Line Item Import Failed", e);			
			retCode=GlobalConstants.IMPORT_SAP_POL_FAILED;
		}
		
		if (retCode==GlobalConstants.IMPORT_SAP_POL_SUCCESS)
			logger.debug("5000 � PO Line Item Import Completed Successfully");
		
		return retCode;
	}
	
private List<String> deleteLIs( String projectId) throws SystemException{
		
		logger.debug("Deleting PO/PR Line Items for Project: " + projectId);
		
		List<String> retStatusMsgList = new ArrayList<String>();
		try {
			
			
			//Prepare for the REST call
			String baseUri = GlobalConstants.EPC_REST_PROTOCOL + "://" + GlobalConstants.EPC_REST_SERVER + ":" + 
					GlobalConstants.EPC_REST_PORT + "/" + GlobalConstants.EPC_REST_BASEURI2;
					

			Stopwatch timerBatch = new Stopwatch();			
			Cookie session = null;
			
			List<String> statusMsgList = new ArrayList<String>();
			timerBatch.start();
			session = this.request(projectId, session, baseUri);
			//logger.info(end + " of " + total + " - elapsed time: " + timerBatch.stop().toString(ISOPeriodFormat.alternateExtended()));
			retStatusMsgList.addAll(statusMsgList);

			
			//this.epcRestMgr.logout(client, baseUri, session);
			
		} catch(Exception e) {
			throw new SystemException(e);
		}
		logger.debug("Complete!");
		return retStatusMsgList;
	}



	private Cookie request(String projectId,
			Cookie session, String baseUri) throws SystemException {
		
		BayerCommitmentPOLIDeleteAPIResultType request = new BayerCommitmentPOLIDeleteAPIResultType();
		//request.getBayerCommitmentLIAPI().addAll(subList);

		PoldObjectFactory objectFactory = new PoldObjectFactory();
		JAXBElement<BayerCommitmentPOLIDeleteAPIResultType> requestWrapper = objectFactory.createBayerCommitmentPOLIDeleteAPIResult(request);
		HashMap<String, String> hashMap = new HashMap<String, String>();
		hashMap.put("ProjectId", projectId);
		
		ClientResponse response = epcRestMgr
					.postApplicationXmlAsApplicationXml(client, requestWrapper,
							baseUri, GlobalConstants.EPC_REST_IMPORT_SAP_PO_LNITM,
							session, hashMap);
							
		logger.debug(response);
		BayerCommitmentPOLIDeleteAPIResultType result = epcRestMgr.responseToObject(response, BayerCommitmentPOLIDeleteAPIResultType.class);
		if(session == null)
			session = epcRestMgr.getSessionCookie(response);
		
		if(!result.isSuccessFlag()){
			String errMsg="The interface failed to load any record due to data issues; please verify data.";
			if (result.getError() != null)
				errMsg=result.getError().toString();
			
			throw new SystemException(errMsg);
		} else {
		
			//List<StatusMessage> statusMsgList = new ArrayList<StatusMessage>();
			int i=0;
		}
		return session;
	}

	private void processStatusMessages() throws SystemException {
		try{
			
		} catch(Exception e) {
			throw new SystemException (e);
		}
	}
}
