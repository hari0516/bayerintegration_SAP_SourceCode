package com.bayer.integration.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.core.Cookie;
import javax.xml.bind.JAXBElement;

import com.bayer.integration.properties.GlobalConstants;
import com.bayer.integration.rest.calcwbs.BayerCalculateWBSIDAPIResultType;
import com.bayer.integration.rest.catg.BayerCreateVendorAPIRequestType;
import com.bayer.integration.rest.catg.BayerCreateVendorAPIResultType;
import com.bayer.integration.rest.catg.BayerCreateVendorAPIType;
import com.bayer.integration.rest.catg.ObjectFactory;
import com.bayer.integration.rest.catg.ObjectResultType;
import com.bayer.integration.rest.ccl.BayerCCLValidationAPIResultType;
import com.bayer.integration.rest.ccl.ObjectFactoryCCL;
import com.bayer.integration.rest.log.IntegrationIssuesAPIRequestType;
import com.bayer.integration.rest.log.IntegrationIssuesAPIResultType;
import com.bayer.integration.rest.log.IntegrationIssuesAPIType;
import com.bayer.integration.rest.log.LogObjectFactory;
import com.bayer.integration.rest.project.BayerProjectAPIType;
import com.ecosys.exception.SystemException;
import com.ecosys.util.Stopwatch;
import com.sun.jersey.api.client.ClientResponse;

public class MasterDataManagerImpl extends ImportManagerBase implements ImportManager{

	final String itgInterface = "General";
	List<IntegrationIssuesAPIType> itgIssueLog = new ArrayList<IntegrationIssuesAPIType>();
	@Override
	public int importData() {
		int retCode=0;

		// run CCLValidation for all projects
		if (!GlobalConstants.SKIP_EVT_CCL_INTERFACE) {
			//Create Web Service Client
			if (client == null) 
				setClient(epcRestMgr.createClient(GlobalConstants.EPC_REST_USERNAME, GlobalConstants.EPC_REST_PASSWORD));
			Cookie session = null;
			prjExpMgr.ExportData();
			List<BayerProjectAPIType> projectAPITypes = prjExpMgr.getBayerProjectAPITypes();	
			for (BayerProjectAPIType projectAPIType : projectAPITypes) {
				retCode=processCCL(projectAPIType.getInternalID());
				if (retCode != GlobalConstants.EPC_EVT_CCL_SUCCESS)
					break;
			}		
		}
		// create new vendor on demand
		if (!GlobalConstants.SKIP_EPC_NEW_VENDOR) {
			try{
				retCode=GlobalConstants.EPC_CREATE_NEW_VENDOR_SUCCESS;
				List<String> statusMsgList = new ArrayList<String>();
				List<BayerCreateVendorAPIType> currentList = new ArrayList<BayerCreateVendorAPIType>();
				BayerCreateVendorAPIType newVendor = getBayerCreateVendorAPIType();
				currentList = this.readVendors();

				if(!isExist(newVendor.getValueID(),currentList) &&
						isValidEcoSysCategory(newVendor.getValueID(), newVendor.getValueName())) {
					//newVendor.setValueID(newVendor.getValueID() + "-1");
					List<BayerCreateVendorAPIType> vendors = new ArrayList<BayerCreateVendorAPIType>();
					vendors.add(newVendor);
					retCode = processVendor(vendors);				
				} else {

					if(!isValidEcoSysCategory(newVendor.getValueID(), newVendor.getValueName())) {
						System.out.println("Vendor not valid - no Vendor was created");
						logger.debug("Vendor not valid - no Vendor was created");
					}else
						if(isExist(newVendor.getValueID(),currentList))
						{
							itgIssueLog.add(getIntegrationIssuesAPIType("", "Vendor with ID " + newVendor.getValueID() + " already exists, no new vendor was created", "",""));
							processIssueLog(itgIssueLog);
							logger.error("Vendor with ID " + newVendor.getValueID() + " already exists, no new vendor was created");
						}
				}		
			} catch (SystemException e) {
				logger.debug(e.getMessage());
				e.printStackTrace();
				return retCode = 1;
			}
		}
		return retCode;
	}
	private IntegrationIssuesAPIType getIntegrationIssuesAPIType(String logErrorId, String logDescription, String logComment, String externalKey) {
		IntegrationIssuesAPIType logEntry = new IntegrationIssuesAPIType();
		logEntry.setIntegrationLogID(itgInterface);
		//logEntry.setIntegrationLogID(itgInterface+"."+logErrorId);
		logEntry.setDescription(logDescription);
		logEntry.setComment(logComment);
		logEntry.setExternalKey(externalKey);
		return logEntry;
	}	
	public void processIssueLog(List<IntegrationIssuesAPIType> itgIssueLog) {

		String baseUri = GlobalConstants.EPC_REST_PROTOCOL + "://" + GlobalConstants.EPC_REST_SERVER + ":" +
				GlobalConstants.EPC_REST_PORT + "/" + GlobalConstants.EPC_REST_BASEURI2;
		try {
			Cookie session = null;
			session = this.requestIssueLog(itgIssueLog, session, baseUri);
		} catch(Exception e) {
			logger.error(e);
		}
	}

	private Cookie requestIssueLog(List<IntegrationIssuesAPIType> itgIssueLog, Cookie session, String baseUri) throws SystemException {
		IntegrationIssuesAPIRequestType request = new IntegrationIssuesAPIRequestType();
		request.getIntegrationIssuesAPI().addAll(itgIssueLog);
		LogObjectFactory objectFactory = new LogObjectFactory();
		JAXBElement<IntegrationIssuesAPIRequestType> requestWrapper = objectFactory.createIntegrationIssuesAPIRequest(request);

		ClientResponse response = epcRestMgr.postApplicationXmlAsApplicationXml(client,requestWrapper,baseUri,
				GlobalConstants.EPC_REST_IMPORT_INTEGRATION_LOG,session);
		logger.debug(response);

		IntegrationIssuesAPIResultType result = epcRestMgr.responseToObject(response, IntegrationIssuesAPIResultType.class);
		if(session == null)
			session = epcRestMgr.getSessionCookie(response);
		/*		if(!result.isSuccessFlag()){			String errMsg="The interface failed to load any record due to data issues; please verify data.";			if (result.getError() != null)				errMsg=result.getError().toString();			for(IntegrationIssuesAPIType issue: issueList)			{				if (errMsg.length() > GlobalConstants.errorMsgSize)					errMsg = errMsg.substring(0, GlobalConstants.errorMsgSize-1);			}			throw new SystemException(errMsg);		} else {			int i=0;			int c=0;			for(ObjectResultType or : result.getObjectResult()) {				IntegrationIssuesAPIType apiType = issueList.get(i++);				String apiID = apiType.getExternalKey();				if(or.isSuccessFlag()) {					logger.debug("UPDATE RESULT --> " + or.getInternalId());					logger.debug("Record with External Key ID of : "  + "|ExternalId: " + or.getExternalId() );				} else {							}			}		}*/	
		return session;
	}	

	private BayerCreateVendorAPIType getBayerCreateVendorAPIType() {
		BayerCreateVendorAPIType vendor = new BayerCreateVendorAPIType();
		vendor.setValueID(GlobalConstants.EPC_VENDOR_ID);
		vendor.setValueName(GlobalConstants.EPC_VENDOR_NAME);
		//vendor.setPathID(GlobalConstants.EPC_VENDOR_ID);
		vendor.setParentID("");
		vendor.setExternalKey(vendor.getValueID()+"(manual)");;
		return vendor;
	}


	private boolean isValidEcoSysCategory(String id, String name) {
		if(id.chars().count()>40) return false;
		if(name.chars().count()>120) return false;
		return true;
	}

	private boolean isExist(String id, List<BayerCreateVendorAPIType> currentList) {
		//List<BayerCreateVendorAPIType> vendor = new ArrayList<BayerCreateVendorAPIType>();
		for(BayerCreateVendorAPIType currentVendor: currentList)
		{
			if (currentVendor.getValueID().equalsIgnoreCase(id))
				return true;
		}
		return false;
	}

	private int processVendor(List<BayerCreateVendorAPIType> vendors) throws SystemException{
		logger.debug("creating vendor");
		try {

			//Prepare for the REST call
			String baseUri = GlobalConstants.EPC_REST_PROTOCOL + "://" + GlobalConstants.EPC_REST_SERVER + ":" +
					GlobalConstants.EPC_REST_PORT + "/" + GlobalConstants.EPC_REST_BASEURI2;
			//baseUri = "https://ecosys-dev.intranet.cnb/ecosys/api"; //hari comment
			Stopwatch timerBatch = new Stopwatch();

			//Create Web Service Client
			if (client == null)
				setClient(epcRestMgr.createClient(GlobalConstants.EPC_REST_USERNAME, GlobalConstants.EPC_REST_PASSWORD));
			Cookie session = null;
			timerBatch.start();
			session = this.requestVendor(vendors, session, baseUri);

		} catch(Exception e) {
			logger.error("Interface failed with error code: " + GlobalConstants.EPC_CREATE_NEW_VENDOR_FAIL);
			return GlobalConstants.EPC_CREATE_NEW_VENDOR_FAIL;
		}
		logger.debug("Create manual vendor complete!");
		return 0;
	}	

	private Cookie requestVendor(List<BayerCreateVendorAPIType> vendors,
			Cookie session, String baseUri) throws SystemException {

		BayerCreateVendorAPIRequestType request = new BayerCreateVendorAPIRequestType();
		request.getBayerCreateVendorAPI().addAll(vendors);

		ObjectFactory objectFactory = new ObjectFactory();
		JAXBElement<BayerCreateVendorAPIRequestType> requestWrapper = objectFactory.createBayerCreateVendorAPIRequest(request);
		HashMap<String, String> paramMap = new HashMap<>();
		paramMap.put("Category", "Vendor");
		ClientResponse response = epcRestMgr
				.postApplicationXmlAsApplicationXml(client, requestWrapper,
						baseUri, GlobalConstants.EPC_REST_IMPORT_CATEGORY,
						session, paramMap);

		logger.debug(response);
		BayerCreateVendorAPIResultType result = epcRestMgr.responseToObject(response, BayerCreateVendorAPIResultType.class);
		if(session == null)
			session = epcRestMgr.getSessionCookie(response);

		if(!result.isSuccessFlag()){
			String errMsg="The interface failed to insert the record due to data issues; please verify data.";
			if (result.getError() != null)
				errMsg=result.getError().toString();

			throw new SystemException(errMsg);
		} else {

			ObjectResultType or = result.getObjectResult().get(0);
			if(or.isSuccessFlag()) {
				logger.debug("UPDATE RESULT --> " + or.getInternalId());
				logger.debug("Record with External Key ID of : " + or.getExternalId());
			} else {
				String str = (or.getResultMessage().get(0).getMessage());
				logger.error("ERROR --> "+ str);
				throw new SystemException(or.getResultMessageString());
			}
		}
		return session;
	}
	private int processCCL( String projectId) {

		int retCode=GlobalConstants.EPC_EVT_CCL_SUCCESS;
		logger.debug("validating CCL assignments for Project: " + projectId);
		List<String> retStatusMsgList = new ArrayList<String>();
		try {
			String baseUri = GlobalConstants.EPC_REST_PROTOCOL + "://" + GlobalConstants.EPC_REST_SERVER + ":" + 
					GlobalConstants.EPC_REST_PORT + "/" + GlobalConstants.EPC_REST_BASEURI2;
			Stopwatch timerBatch = new Stopwatch();			
			Cookie session = null;
			List<String> statusMsgList = new ArrayList<String>();
			timerBatch.start();
			session = this.requestCCL(projectId, session, baseUri);
			retStatusMsgList.addAll(statusMsgList);
		} catch(Exception e) {
			logger.error("Interface failed with error code: " + GlobalConstants.EPC_EVT_CCL_FAIL);
			logger.error("ccl validation for project with internal Id: " + projectId + " failed due to a " + e + " , CCL validation will be skipped");
			return retCode=GlobalConstants.EPC_EVT_CCL_FAIL;
		}
		logger.debug("Complete!\n");
		return retCode;
	}


	private Cookie requestCCL(String projectId, Cookie session, 
			String baseUri) throws SystemException {

		BayerCCLValidationAPIResultType request = new BayerCCLValidationAPIResultType();
		HashMap<String,String> filterMap = new HashMap<String, String>();
		filterMap.put(GlobalConstants.EPC_ROOTCOSTOBJECT_PARAM, projectId);
		ObjectFactoryCCL objectFactory = new ObjectFactoryCCL();
		JAXBElement<BayerCCLValidationAPIResultType> requestWrapper = objectFactory.createBayerCCLValidationAPIResult(request);
		ClientResponse response = epcRestMgr
				.postApplicationXmlAsApplicationXml(client, requestWrapper,
						baseUri, GlobalConstants.EPC_REST_EVT_CCL,
						session, filterMap);
		logger.debug(response);
		BayerCalculateWBSIDAPIResultType result = epcRestMgr.responseToObject(response, BayerCalculateWBSIDAPIResultType.class);
		if(session == null)
			session = epcRestMgr.getSessionCookie(response);
		if(!result.isSuccessFlag()){
			String errMsg="The interface failed to load any record due to data issues; please verify data.";
			if (result.getError() != null)
				errMsg=result.getError().toString();
			throw new SystemException(errMsg);
		} else {
			logger.debug("CCL validation complete for project: "+ projectId);
		}
		return session;
	}

	//Added by PW on May 4th 2023 - Read Vendor
	private  List<BayerCreateVendorAPIType> readVendors() throws SystemException{

		List<BayerCreateVendorAPIType> bayerVendors = new ArrayList<BayerCreateVendorAPIType>();
		try 
		{	
			if (client == null) 
				setClient(epcRestMgr.createClient(GlobalConstants.EPC_REST_USERNAME, GlobalConstants.EPC_REST_PASSWORD));

			//Read Vendor Data from EcoSys using createVendor API
			Cookie session = null;
			String baseUri2 = GlobalConstants.EPC_REST_PROTOCOL + "://" + GlobalConstants.EPC_REST_SERVER + ":"
					+ GlobalConstants.EPC_REST_PORT + "/" + GlobalConstants.EPC_REST_BASEURI2;		
			//baseUri2 = "https://ecosys-dev.intranet.cnb/ecosys/api";  //hari comment

			HashMap<String, String> aMap = new HashMap<String, String>();
			aMap.put(GlobalConstants.EPC_PROJECT_PARAM,"");
			bayerVendors = this.getVendorAPITypes(session, baseUri2);
			return bayerVendors;

		} catch(Exception e) {
			throw new SystemException(e);
		}
	}

	private List<BayerCreateVendorAPIType> getVendorAPITypes(Cookie session, String baseUri) throws SystemException {
		HashMap<String, String> paramMap = new HashMap<>();
		paramMap.put("Category", "Vendor");
		ClientResponse response = epcRestMgr
				.getAsApplicationXml(client, baseUri,
						GlobalConstants.EPC_REST_IMPORT_CATEGORY
						,session, paramMap);

		logger.debug(response);
		BayerCreateVendorAPIResultType result = epcRestMgr.responseToObject(response, BayerCreateVendorAPIResultType.class);
		List<BayerCreateVendorAPIType> vendorAPITypes = result.getBayerCreateVendorAPI();
		if(session == null)
			session = epcRestMgr.getSessionCookie(response);

		if(!result.isSuccessFlag()){
			String errMsg="The interface failed to retrieve pmorder; please verify connection.";
			if (result.getError() != null)
				errMsg=result.getError().toString();

			throw new SystemException(errMsg);
		} else {
			int i=0;
		}
		return vendorAPITypes;
	}
}