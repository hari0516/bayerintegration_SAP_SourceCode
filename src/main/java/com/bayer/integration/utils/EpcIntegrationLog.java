package com.bayer.integration.utils;

import java.util.List;

import javax.ws.rs.core.Cookie;
import javax.xml.bind.JAXBElement;

import org.apache.log4j.Logger;

import com.bayer.integration.properties.GlobalConstants;
import com.bayer.integration.rest.log.IntegrationIssuesAPIRequestType;
import com.bayer.integration.rest.log.IntegrationIssuesAPIResultType;
import com.bayer.integration.rest.log.IntegrationIssuesAPIType;
import com.bayer.integration.rest.log.LogObjectFactory;
import com.bayer.integration.service.ImportManagerBase;
import com.ecosys.exception.SystemException;
import com.ecosys.service.EpcRestMgr;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

public class EpcIntegrationLog extends ImportManagerBase {

	String itgInterface = null;
	public EpcIntegrationLog(String itgInterface) {
		this.itgInterface=itgInterface;
	}
	
	public IntegrationIssuesAPIType getIntegrationIssuesAPIType(
			String logErrorId, String logDescription, String logComment) {

		IntegrationIssuesAPIType logEntry = new IntegrationIssuesAPIType();		
		logEntry.setIntegrationLogID(itgInterface+"."+logErrorId);		
		logEntry.setDescription(logDescription);		
		logEntry.setComment(logComment);	

		return logEntry;
	}
	public void processIssueLog(List<IntegrationIssuesAPIType> itgIssueLog) {

		//Create Web Service Client
		if (client == null)
			setClient(epcRestMgr.createClient(GlobalConstants.EPC_REST_USERNAME, GlobalConstants.EPC_REST_PASSWORD));

		//Prepare for the REST call		
		String baseUri = GlobalConstants.EPC_REST_PROTOCOL + "://" + GlobalConstants.EPC_REST_SERVER + ":" + 				
				GlobalConstants.EPC_REST_PORT + "/" + GlobalConstants.EPC_REST_BASEURI2;
		try {
			Cookie session = null;
			session = this.request(itgIssueLog, session, baseUri);
		} catch(Exception e) {
			logger.error(e);		
		}	
	}

public void processIssueLog(String logErrorId, String logDescription, String logComment) {
	//Create Web Service Client
	if (client == null)
		setClient(epcRestMgr.createClient(GlobalConstants.EPC_REST_USERNAME, GlobalConstants.EPC_REST_PASSWORD));

	IntegrationIssuesAPIType logEntry = new IntegrationIssuesAPIType();
	logEntry.setIntegrationLogID(itgInterface+"."+logErrorId);
	logEntry.setDescription(logDescription);
	logEntry.setComment(logComment);

	//Prepare for the REST call
	String baseUri = GlobalConstants.EPC_REST_PROTOCOL + "://" + GlobalConstants.EPC_REST_SERVER + ":" + 
			GlobalConstants.EPC_REST_PORT + "/" + GlobalConstants.EPC_REST_BASEURI2;
	try {
		Cookie session = null;
		session = this.request(logEntry, session, baseUri);
	} catch(Exception e) {	
		logger.error(e);
	}

}

private Cookie request(List<IntegrationIssuesAPIType> itgIssueLog, Cookie session, String baseUri) throws SystemException {
	IntegrationIssuesAPIRequestType request = new IntegrationIssuesAPIRequestType();
	request.getIntegrationIssuesAPI().addAll(itgIssueLog);
	LogObjectFactory objectFactory = new LogObjectFactory();
	JAXBElement<IntegrationIssuesAPIRequestType> requestWrapper = objectFactory
			.createIntegrationIssuesAPIRequest(request);

	ClientResponse response = epcRestMgr.postApplicationXmlAsApplicationXml(client,requestWrapper,
			baseUri, GlobalConstants.EPC_REST_IMPORT_INTEGRATION_LOG,session);

	logger.debug(response);
	IntegrationIssuesAPIResultType result = epcRestMgr.responseToObject(response, IntegrationIssuesAPIResultType.class);
	if(session == null)
		session = epcRestMgr.getSessionCookie(response);
	/*		if(!result.isSuccessFlag()){			String errMsg="The interface failed to load any record due to data issues; please verify data.";			if (result.getError() != null)				errMsg=result.getError().toString();			for(IntegrationIssuesAPIType issue: issueList)			{				if (errMsg.length() > GlobalConstants.errorMsgSize)					errMsg = errMsg.substring(0, GlobalConstants.errorMsgSize-1);			}			throw new SystemException(errMsg);		} else {			int i=0;			int c=0;			for(ObjectResultType or : result.getObjectResult()) {				IntegrationIssuesAPIType apiType = issueList.get(i++);				String apiID = apiType.getExternalKey();				if(or.isSuccessFlag()) {					logger.debug("UPDATE RESULT --> " + or.getInternalId());					logger.debug("Record with External Key ID of : "  + "|ExternalId: " + or.getExternalId() );				} else {							}			}		}*/
	return session;	
}	


private Cookie request(IntegrationIssuesAPIType logEntry, Cookie session, String baseUri) throws SystemException {

	IntegrationIssuesAPIRequestType request = new IntegrationIssuesAPIRequestType();
	request.getIntegrationIssuesAPI().add(logEntry);
	LogObjectFactory objectFactory = new LogObjectFactory();
	JAXBElement<IntegrationIssuesAPIRequestType> requestWrapper = objectFactory.createIntegrationIssuesAPIRequest(request);
	ClientResponse response = epcRestMgr
			.postApplicationXmlAsApplicationXml(client,requestWrapper,
					baseUri, GlobalConstants.EPC_REST_IMPORT_INTEGRATION_LOG,
					session);

	logger.debug(response);
	IntegrationIssuesAPIResultType result = epcRestMgr.responseToObject(response, IntegrationIssuesAPIResultType.class);
	if(session == null)
		session = epcRestMgr.getSessionCookie(response);
	/*
		if(!result.isSuccessFlag()){
			String errMsg="The interface failed to load any record due to data issues; please verify data.";
			if (result.getError() != null)
				errMsg=result.getError().toString();
			for(IntegrationIssuesAPIType issue: issueList)
			{
				if (errMsg.length() > GlobalConstants.errorMsgSize)
					errMsg = errMsg.substring(0, GlobalConstants.errorMsgSize-1);
			}
			throw new SystemException(errMsg);
		} else {
			int i=0;
			int c=0;

			for(ObjectResultType or : result.getObjectResult()) {
				IntegrationIssuesAPIType apiType = issueList.get(i++);
				String apiID = apiType.getExternalKey();
				if(or.isSuccessFlag()) {
					logger.debug("UPDATE RESULT --> " + or.getInternalId());
					logger.debug("Record with External Key ID of : "  + "|ExternalId: " + or.getExternalId() );
				} else {			
				}
			}
		}*/
	return session;
}	

}
