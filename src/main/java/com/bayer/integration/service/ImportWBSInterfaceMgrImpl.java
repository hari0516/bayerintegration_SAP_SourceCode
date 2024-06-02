/**
 * 
 */
package com.bayer.integration.service;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Cookie;
import javax.xml.bind.JAXBElement;

import org.joda.time.format.ISOPeriodFormat;

import com.bayer.integration.properties.GlobalConstants;
import com.bayer.integration.rest.wbs.BayerWBSAPIRequestType;
import com.bayer.integration.rest.wbs.BayerWBSAPIResultType;
import com.bayer.integration.rest.wbs.BayerWBSAPIType;
import com.bayer.integration.rest.wbs.WObjectFactory;
import com.bayer.integration.rest.wbs.WObjectResultType;
import com.ecosys.exception.SystemException;
import com.ecosys.util.Stopwatch;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.org.apache.xerces.internal.dom.ElementNSImpl;

/**
 * @author skdas
 *
 */
public class ImportWBSInterfaceMgrImpl extends ImportManagerBase implements
		ImportManager {

	/* (non-Javadoc)
	 * @see com.bayer.integration.service.ImportManager#importData()
	 */
	//@Override
	public int importData() {
		int retCode=GlobalConstants.IMPORT_SAP_WBS_SUCCESS;
		try{			
			if (!GlobalConstants.SKIP_SAP_WBS_INTERFACE) {
				//Create Web Service Client
				if (client == null) setClient(epcRestMgr.createClient(GlobalConstants.EPC_REST_USERNAME, GlobalConstants.EPC_REST_PASSWORD));
				
				//Read Project Data from EcoSys using project API
				//Loop through the project List to get WBS List from Bayer Web Services
				
				//read WBS data from above and convert them to EcoSys API payload
				logger.debug("Reading Project data");
				List<BayerWBSAPIType> prjRecords = readProjectData();
				
				//Invoke EcoSys API with input records
				//List<ProcessMessage> = processProjects(prjRecords);
				
				//Process Status Messages
				logger.debug("Processing Error Messages, If Any");
				processStatusMessages();
				
				
			} else {
				logger.info("Skipped Project Interface. Change the skip property to 'false'");				
			}
			
		}catch(SystemException se) {			
			logger.error("1005 � WBS Import Failed", se);			
			retCode=GlobalConstants.IMPORT_SAP_WBS_FAILED;
		}catch(Exception e) {			
			logger.error("1005 � WBS Import Failed", e);			
			retCode=GlobalConstants.IMPORT_SAP_WBS_FAILED;
		}
		
		if (retCode==GlobalConstants.IMPORT_SAP_WBS_SUCCESS)
			logger.debug("1000 � WBS Import Completed Successfully");
		
		return retCode;
	}

	private List<BayerWBSAPIType> readProjectData() {
		return null;
	}

	private List<String> processProjects( List<BayerWBSAPIType> prjRecords) throws SystemException{
		
		logger.debug("Importing CostObjects to EPC...");
		
		List<String> retStatusMsgList = new ArrayList<String>();
		try {
			
			if(prjRecords == null || prjRecords.size() == 0) {
				return retStatusMsgList;
			}
			
			//Prepare for the REST call
			String baseUri = GlobalConstants.EPC_REST_PROTOCOL + "://" + GlobalConstants.EPC_REST_SERVER + ":" + 
					GlobalConstants.EPC_REST_PORT + "/" + GlobalConstants.EPC_REST_BASEURI;
					
			long total = prjRecords.size();
			Stopwatch timerBatch = new Stopwatch();
			Cookie session = null;
			for(int i = 0; i < prjRecords.size(); i += GlobalConstants.EPC_REST_BATCHSIZE) {
				int end = (prjRecords.size() < (i + GlobalConstants.EPC_REST_BATCHSIZE) ? prjRecords.size() : GlobalConstants.EPC_REST_BATCHSIZE + i);
				List<String> statusMsgList = new ArrayList<String>();
				timerBatch.start();
				session = this.request(prjRecords.subList(i, end), session, baseUri);
				logger.info(end + " of " + total + " - elapsed time: " + timerBatch.stop().toString(ISOPeriodFormat.alternateExtended()));
				retStatusMsgList.addAll(statusMsgList);
			}
			
			//this.epcRestMgr.logout(client, baseUri, session);
			
		} catch(Exception e) {
			throw new SystemException(e);
		}
		logger.debug("Complete!");
		return retStatusMsgList;
	}
	
	private Cookie request(List<BayerWBSAPIType> subList,
			Cookie session, String baseUri) throws SystemException {
		
		BayerWBSAPIRequestType request = new BayerWBSAPIRequestType();
		request.getBayerWBSAPI().addAll(subList);

		WObjectFactory objectFactory = new WObjectFactory();
		JAXBElement<BayerWBSAPIRequestType> requestWrapper = objectFactory.createBayerWBSAPIRequest(request);
		
		ClientResponse response = epcRestMgr
					.postApplicationXmlAsApplicationXml(client, requestWrapper,
							baseUri, GlobalConstants.EPC_REST_IMPORT_SAP_WBS,
							session);
							
		logger.debug(response);
		BayerWBSAPIResultType result = epcRestMgr.responseToObject(response, BayerWBSAPIResultType.class);
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
			for(WObjectResultType or : result.getObjectResult()) {
				BayerWBSAPIType wbs = subList.get(i++);
				String wbsID = wbs.getCostObjectID();
				if(or.isSuccessFlag()) {
					logger.debug("UPDATE RESULT --> " + or.getInternalId());
					logger.debug("Record with External Key ID of : " + wbsID +" "+ GlobalConstants.STAGING_RECORD_STATUS_LOADED);
				} else {
					String str = ((ElementNSImpl)or.getResultMessage().get(0).getMessage()).getTextContent();
					logger.error("ERROR --> " + or.getInternalId() + "|" + wbsID + "|" + or.isSuccessFlag() + "|" + str);
					
				}
							
			}
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
