package com.bayer.integration.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.core.Cookie;
import javax.xml.bind.JAXBElement;

import org.apache.log4j.Logger;

import com.bayer.integration.properties.GlobalConstants;
import com.bayer.integration.rest.calcwbs.BayerCalculateWBSIDAPIResultType;
import com.bayer.integration.rest.ccl.BayerCCLValidationAPIResultType;
import com.bayer.integration.rest.ccl.ObjectFactoryCCL;

import com.ecosys.exception.SystemException;
import com.ecosys.service.EpcRestMgr;
import com.ecosys.util.Stopwatch;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

// Constructor
public class EcoSysEventTrigger implements ExportManager {	
	public EcoSysEventTrigger() {
	}
	
	//TODO: Check the logger is working
	//TODO: trap and deal with all exceptions correctly
	
	private String apiName;
	public String getApiName() {
		return apiName;	}
	public void setApiName(String apiConst) {
		this.apiName = apiConst; }

	protected static Logger logger = Logger.getLogger(EcoSysEventTrigger.class);


	public int trigger() {
		try {
			HashMap<String, String> params = new HashMap<>();
			this.process();
		} catch(Exception e) {
		}
		return 0;
	}

	
	public int trigger(HashMap<String, String> params) {
		try {
			this.process(apiName, params);
		} catch(Exception e) {
		}
		return 0;
	}

	
	
	private List<String> process() throws SystemException{
		logger.debug("validating CCL assignments for all Projects");
		List<String> retStatusMsgList = new ArrayList<String>();
		try {
			//Prepare for the REST call
			String baseUri = GlobalConstants.EPC_REST_PROTOCOL + "://" + GlobalConstants.EPC_REST_SERVER + ":" + 
					GlobalConstants.EPC_REST_PORT + "/" + GlobalConstants.EPC_REST_BASEURI2;
			Stopwatch timerBatch = new Stopwatch();			
			Cookie session = null;			
			List<String> statusMsgList = new ArrayList<String>();
			timerBatch.start();
			session = this.request(session, baseUri);
			retStatusMsgList.addAll(statusMsgList);

		} catch(Exception e) {
			throw new SystemException(e);
		}
		logger.debug("Complete!");
		return retStatusMsgList;
	}


	private Cookie request(Cookie session, String baseUri) throws SystemException {

		BayerCCLValidationAPIResultType request = new BayerCCLValidationAPIResultType();
		ObjectFactoryCCL objectFactory = new ObjectFactoryCCL();
		JAXBElement<BayerCCLValidationAPIResultType> requestWrapper = objectFactory.createBayerCCLValidationAPIResult(request);
		EpcRestMgr epcRestMgr = null;
		Client client = null;
		
		/*							   |
		//FIX: epcRestMgr is null here |
		**							   V
		*/
		
		ClientResponse response = epcRestMgr
				.postApplicationXmlAsApplicationXml(client, requestWrapper,
						baseUri, GlobalConstants.EPC_REST_EVT_CCL,
						session);
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

			logger.debug("XXXX Actuals Import | Trigger Action Batch to validate CLL completed for all Projects");
			int i=0;
		}
		return session;
	}

	private List<String> process( String projectId, HashMap<String,String> params) throws SystemException{
		logger.debug("validating CCL assignments for Project: " + projectId);

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
			retStatusMsgList.addAll(statusMsgList);

		} catch(Exception e) {
			throw new SystemException(e);
		}
		logger.debug("Complete!");
		return retStatusMsgList;
	}
	private Cookie request(String projectId, Cookie session, 
			String baseUri) throws SystemException {

		BayerCCLValidationAPIResultType request = new BayerCCLValidationAPIResultType();
		HashMap<String,String> filterMap = new HashMap<String, String>();
		filterMap.put(GlobalConstants.EPC_ROOTCOSTOBJECT_PARAM, projectId);

		ObjectFactoryCCL objectFactory = new ObjectFactoryCCL();
		JAXBElement<BayerCCLValidationAPIResultType> requestWrapper = objectFactory.createBayerCCLValidationAPIResult(request);

		EpcRestMgr epcRestMgr = null;
		Client client = null;

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

			logger.debug("3000 Actuals Import | Trigger Action Batch to validate CLL completed Project: "+ projectId);
			int i=0;
		}
		return session;
	}
	@Override
	public int ExportData() {
		return 0;
	}	
}
