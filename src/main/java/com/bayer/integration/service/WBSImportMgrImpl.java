/**
 * 
 */
package com.bayer.integration.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Cookie;
import javax.xml.bind.JAXBElement;

import org.joda.time.format.ISOPeriodFormat;

import com.bayer.integration.odata.SapWBSODataType;
import com.bayer.integration.persistence.EcoSysResultErrorDAO;
import com.bayer.integration.persistence.WbsApiErrorDAO;
import com.bayer.integration.properties.GlobalConstants;
import com.bayer.integration.rest.calcwbs.BayerCalculateWBSIDAPIResultType;
import com.bayer.integration.rest.calcwbs.CalcWObjectFactory;
import com.bayer.integration.rest.log.*;
import com.bayer.integration.rest.project.BayerProjectAPIType;
import com.bayer.integration.rest.wbs.BayerWBSAPIRequestType;
import com.bayer.integration.rest.wbs.BayerWBSAPIResultType;
import com.bayer.integration.rest.wbs.BayerWBSAPIType;
import com.bayer.integration.rest.wbs.WObjectFactory;
import com.bayer.integration.rest.wbs.WObjectResultType;
import com.bayer.integration.rest.wbsread.BayerWBSReadAPIResultType;
import com.bayer.integration.rest.wbsread.BayerWBSReadAPIType;

import com.bayer.integration.utils.DebugBanner;
import com.bayer.integration.utils.EpcIntegrationLog;

import com.ecosys.exception.SystemException;
import com.ecosys.util.Stopwatch;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.org.apache.xerces.internal.dom.ElementNSImpl;

/**
 * @author skdas
 *
 */
public class WBSImportMgrImpl extends ImportManagerBase implements
ImportManager {

	/* (non-Javadoc)
	 * @see com.bayer.integration.service.ImportManager#importData()
	 */
	//@Override	
	final String itgInterface = "WBS";
	List<IntegrationIssuesAPIType> itgIssueLog = new ArrayList<IntegrationIssuesAPIType>();
	//EpcIntegrationLog epcLog = new EpcIntegrationLog(itgInterface);
	
	public int importData() {
		int retCode=GlobalConstants.IMPORT_SAP_WBS_SUCCESS;
		try{			
			if (!GlobalConstants.SKIP_SAP_WBS_INTERFACE) {
				if(GlobalConstants.DEBUGMODE)
					DebugBanner.outputBanner(this.getClass().toString());
				boolean skipError = GlobalConstants.SKIP_LOG;
				//change the value to false if using sample data
				boolean isLive = GlobalConstants.IS_LIVE_SAP;

				//Create Web Service Client
				if (client == null) 
					setClient(epcRestMgr.createClient(GlobalConstants.EPC_REST_USERNAME, GlobalConstants.EPC_REST_PASSWORD));

				//Read Project Data from EcoSys using project API
				Cookie session = null;

				prjExpMgr.ExportData();
				List<BayerProjectAPIType> projectAPITypes = prjExpMgr.getBayerProjectAPITypes();

				//Loop through the project list 
				for (int i = 0; i < projectAPITypes.size(); i++) 
				{
					itgIssueLog.clear();
					BayerProjectAPIType projectAPIType = projectAPITypes.get(i);
					String projectId = projectAPIType.getID();
					GlobalConstants.EPC_PROJECT_ID_PROCESSED = projectId;
					String sapProjectId = projectAPIType.getSapProjectId();					
					String systemId = projectAPIType.getSAPSystemID();
					String projectCurrency = projectAPIType.getRootCostObjectCurrencyCode();
					//systemId = "X2R";
					String masterProjectId = null;
					if (projectAPIType.getParentCostObjectID()!=null && projectAPIType.getParentCostObjectID()!="")
						masterProjectId = projectAPIType.getParentCostObjectID();
					boolean isSub = false;
					if (projectAPIType.getProjectTypeID().equals(GlobalConstants.EPC_API_PROJECT_TYPE_SUB))
						isSub = true;

					List<WbsApiErrorDAO> errorDAOList = new ArrayList<WbsApiErrorDAO>();
					List<WbsApiErrorDAO> lastTimeFailedDAOList = new ArrayList<WbsApiErrorDAO>();
					List<BayerWBSAPIType> inputList = new ArrayList<BayerWBSAPIType>();
					List<WbsApiErrorDAO> invalidRecords = new ArrayList<WbsApiErrorDAO>(); 
					List<BayerWBSAPIType> validRecords = new ArrayList<BayerWBSAPIType>();
					List<SapWBSODataType> inputListSample = null;
					try
					{
						//Start OData2 Section
						//Read new SAP input and process
						logger.debug("Reading WBS data from SAP Input for Project: "+ projectId);
						if (isLive)
						{
							inputList = this.readWBSData(sapProjectId, systemId, isSub, masterProjectId);
							if (inputList.size()==0)
								continue;	
							logger.debug(inputList.size() + " rows of WBS data read from SAP for Project: "+ projectId);
						}
						else
						{
							//Start Hexagon Sample Section

							//validRecords = this.getBayerWBSAPITypesValidO(inputListSample);
							//This is for sub project
							if (isSub)
								inputListSample = importHelper.getSapWBSTypesSampleSub();
							else
								inputListSample = importHelper.getSapWBSTypesSample();					

							inputList = this.getBayerWBSAPITypesO(inputListSample);
							//invalidRecords = this.getBayerWBSAPITypesInvalidO(inputListSample);
						}

						//Process Invalid Records
						invalidRecords = this.getBayerWBSAPITypesInvalid(inputList, projectId, sapProjectId);
						logger.debug(invalidRecords.size() + " rows of invalid WBS data read from SAP for Project: "+ projectId);

						if (invalidRecords.size()>0 && !skipError) {
							logger.debug("Processing Invalid Records, If Any for Project: " + projectId);
							//this.processStatussMessages(invalidRecords, true);
							logger.debug("Processing Invalid Records Completed for Project: "+ projectId);	
						}
						//Process Valid Records
						if (isSub == false)
						{
							//validRecords = this.getBayerWBSAPITypesValidO(oDataTypes);
							validRecords = this.getBayerWBSAPITypesValid(inputList, projectId, sapProjectId);
						}
						else
						{
							//validRecords = this.getBayerWBSAPITypesValidOSub(inputList, masterProjectId);
							validRecords = this.getBayerWBSAPITypesValidSub(inputList, projectId, sapProjectId, masterProjectId);
						}
						logger.debug(validRecords.size() + " rows of valid WBS data read from SAP for Project: "+ projectId);

						if (validRecords.size() > 0)
						{
							int level = 0;
							for (int intLevel=1; intLevel<10; intLevel++)
							{
								level++;
								List<BayerWBSAPIType> levelRecords = new ArrayList<BayerWBSAPIType>();
								levelRecords = this.getBayerWBSAPITypesByLevel(projectCurrency, validRecords, intLevel);
								if (levelRecords.size()>0)
								{
									List<BayerWBSAPIType> filteredLevelRecords = new ArrayList<BayerWBSAPIType>();
									if (level>2)
										filteredLevelRecords = hasValidParentCo(sapProjectId, levelRecords, level);
									//errorDAOList = processProjects(levelRecords, projectId);
									errorDAOList = processProjects(filteredLevelRecords, projectId);

									//Process Status Messages
									if (!skipError)
									{
										logger.debug("Processing ERR Messages, If Any for Project: " + projectId);
										//this.processStatusMessages(errorDAOList, true);
										logger.debug("Processing ERR Message Completed for Project: "+ projectId);
									}
									for (int intSize=0; intSize<levelRecords.size(); intSize++)
									{
									}
								}
							}
							//re-calculate WBS ID/Name for the Project
							logger.debug("Trigger Action Batch to Recalc WBS ID/Name Custom Field for Project: "+ projectId);
							this.calcWBSIds(projectId);
							logger.debug("Trigger Action Batch to Recalc WBS ID/Name Custom Field completed for Project: "+ projectId);
							//this.processCCL(projectAPIType.getInternalID());
							logger.debug("Integration issue log cleared");
							logger.debug("posting "+itgIssueLog.size() +" " + itgInterface+" issues to EPC");
							if (itgIssueLog.size()>0)
								processIssueLog(itgIssueLog);
						}
						else
							logger.debug("No valid records for Project: "+ projectId);	

					}
					catch(SystemException se) {
						logger.error("1005 -- WBS Import Failed for Project: " + GlobalConstants.EPC_PROJECT_ID_PROCESSED, se);
						retCode=GlobalConstants.IMPORT_SAP_WBS_FAILED;
						continue;
					}
				}

			} else {
				logger.info("Skipped WBS Import Interface. Change the skip property to 'false'");
				retCode = GlobalConstants.IMPORT_SAP_WBS_SKIPPED;
				//logger.debug("WBS Interface SKIPPED");
			}

		}catch(Exception e) {			
			logger.error("1005 -- WBS Import Failed", e);			
			retCode=GlobalConstants.IMPORT_SAP_WBS_FAILED;
		}

		if (retCode==GlobalConstants.IMPORT_SAP_WBS_SUCCESS)
			logger.debug("1000 -- WBS Import Completed Successfully");

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
			session = this.request(itgIssueLog, session, baseUri);
		} catch(Exception e) {
			logger.error(e);
		}
	}

	private Cookie request(List<IntegrationIssuesAPIType> itgIssueLog, Cookie session, String baseUri) throws SystemException {
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

	
	private List<BayerWBSAPIType> hasValidParentCo(String sapProjectId, List<BayerWBSAPIType> levelRecordsIn, int level) {
		List<BayerWBSReadAPIType> ecosysWbs = new ArrayList<BayerWBSReadAPIType>();									  
		List<BayerWBSAPIType> levelRecordsOut = new ArrayList<BayerWBSAPIType>();

		logger.debug("checking ecosys for "+ levelRecordsIn.size() + " level " + level + " parent cost objects");	

		for (BayerWBSAPIType levelRecord : levelRecordsIn) {
			String hpid = levelRecord.getHierarchyPathID();
			String parent = hpid.substring(0,hpid.lastIndexOf("."));			

			try {
				ecosysWbs = this.getParentCO(sapProjectId, parent);
			} catch (SystemException e) {
				logger.error("error calling the read wbs API");
				e.printStackTrace();
			}
			if(!ecosysWbs.isEmpty()) {
				levelRecordsOut.add(levelRecord);
				//logger.debug("parent found: " + ecosysWbs.get(0).getHierarchyPathID());
			}
			else {
				logger.error("WBS : MISSING PARENT : WBS hierarchyPathId: "+ hpid +" --> Parent WBS: " + parent);
				//String logErrorId = "MissingParent";
				//String logDescription = hpid + " does not have a valid parent WBS";
				//String logComment = "Missing Parent - "+ parent;
				//itgIssueLog.add(epcLog.getIntegrationIssuesAPIType(logErrorId, logDescription, logComment));
				itgIssueLog.add(getIntegrationIssuesAPIType("", "WBS " + hpid + " does not have a valid parent WBS", sapProjectId/*"Missing Parent - "+ parent*/,levelRecord.getExternalKey()));
				}
		}
		logger.info("||  Records checked:"+ levelRecordsIn.size() + "\t >> \tRecords valid:"+ levelRecordsOut.size());
		return levelRecordsOut;
	}


	/*	private List<BayerWBSAPIType> confirmParent(String sapProjectId, List<BayerWBSAPIType> levelRecordsIn, int level) {
		List<BayerWBSReadAPIType> ecosysWbs = new ArrayList<>();									  
		try {
			logger.debug("reading EcoSys cost objects for project: " + sapProjectId);
			ecosysWbs = this.readParentCOs(sapProjectId, level-1); logger.debug("read " + ecosysWbs.size() + " EcoSys cost objects");
		} catch (SystemException e) {
			logger.error("error calling the read wbs API");
			e.printStackTrace(); }

		List<BayerWBSAPIType> levelRecordsOut = new ArrayList<BayerWBSAPIType>();

		for (BayerWBSAPIType levelRecord : levelRecordsIn) {
			logger.debug("External Key: " + levelRecord.getHierarchyPathID());
			logger.debug("Parent: " + levelRecord.getParentCostObjectHierarchyPathID());
			//logger.debug("NEED TO CONVERT PARENT PATH ID in levelRecordsIn");
			List<String> parentIds = ecosysWbs.stream()
					.filter(t ->t.getExternalKey().equalsIgnoreCase(levelRecord.getParentCostObjectHierarchyPathID()))
					.map(t -> t.getHierarchyPathID())
					.collect(Collectors.toList());
			if(!parentIds.isEmpty()) {
				levelRecordsOut.add(levelRecord);
				//logger.debug("Parent found with hierarchypathid: " + parentIds.get(0));
			}
			else
				logger.error("parent cost object does not exist for wbs: "+levelRecord.getHierarchyPathID());
		}

		logger.debug("Records checked:"+ levelRecordsIn.size() + "\tRecords valid:"+ levelRecordsOut.size());
		return levelRecordsOut;
	}
	 */
	private List<BayerWBSAPIType> readProjectData() {
		return null;
	}

	private List<WbsApiErrorDAO> processProjects( List<BayerWBSAPIType> prjRecords,String projectId) throws SystemException{

		logger.debug("Importing CostObjects to EPC...");

		List<WbsApiErrorDAO> retStatusMsgList = new ArrayList<WbsApiErrorDAO>();
		try {

			if(prjRecords == null || prjRecords.size() == 0) {
				return retStatusMsgList;
			}

			//Prepare for the REST call
			String baseUri = GlobalConstants.EPC_REST_PROTOCOL + "://" + GlobalConstants.EPC_REST_SERVER + ":" + 
					GlobalConstants.EPC_REST_PORT + "/" + GlobalConstants.EPC_REST_BASEURI2;

			long total = prjRecords.size();
			Stopwatch timerBatch = new Stopwatch();
			Cookie session = null;
			for(int i = 0; i < prjRecords.size(); i += GlobalConstants.EPC_REST_BATCHSIZE) {
				int end = (prjRecords.size() < (i + GlobalConstants.EPC_REST_BATCHSIZE) ? prjRecords.size() : GlobalConstants.EPC_REST_BATCHSIZE + i);
				List<WbsApiErrorDAO> statusMsgList = new ArrayList<WbsApiErrorDAO>();
				timerBatch.start();
				session = this.request(prjRecords.subList(i, end), session, baseUri, projectId, statusMsgList);
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

	private List<WbsApiErrorDAO> processProjectsSub( List<BayerWBSAPIType> prjRecords, String projectId, String hierarchyPath) throws SystemException{

		logger.debug("Importing CostObjects to EPC...");

		List<WbsApiErrorDAO> retStatusMsgList = new ArrayList<WbsApiErrorDAO>();
		try {

			if(prjRecords == null || prjRecords.size() == 0) {
				return retStatusMsgList;
			}

			//Prepare for the REST call
			String baseUri = GlobalConstants.EPC_REST_PROTOCOL + "://" + GlobalConstants.EPC_REST_SERVER + ":" + 
					GlobalConstants.EPC_REST_PORT + "/" + GlobalConstants.EPC_REST_BASEURI2;

			long total = prjRecords.size();
			Stopwatch timerBatch = new Stopwatch();
			Cookie session = null;
			for(int i = 0; i < prjRecords.size(); i += GlobalConstants.EPC_REST_BATCHSIZE) {
				int end = (prjRecords.size() < (i + GlobalConstants.EPC_REST_BATCHSIZE) ? prjRecords.size() : GlobalConstants.EPC_REST_BATCHSIZE + i);
				List<WbsApiErrorDAO> statusMsgList = new ArrayList<WbsApiErrorDAO>();
				timerBatch.start();
				session = this.request(prjRecords.subList(i, end), session, baseUri, projectId, statusMsgList);
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
			Cookie session, String baseUri, String projectId, List<WbsApiErrorDAO> errorList) throws SystemException {

		BayerWBSAPIRequestType request = new BayerWBSAPIRequestType();
		request.getBayerWBSAPI().addAll(subList);

		WObjectFactory objectFactory = new WObjectFactory();
		JAXBElement<BayerWBSAPIRequestType> requestWrapper = objectFactory.createBayerWBSAPIRequest(request);
		HashMap<String, String> prjMap = new HashMap<String, String>();
		prjMap.put(GlobalConstants.EPC_PROJECT_PARAM, projectId);
		ClientResponse response = epcRestMgr
				.postApplicationXmlAsApplicationXml(client, requestWrapper,
						baseUri, GlobalConstants.EPC_REST_IMPORT_SAP_WBS,
						session, prjMap);

		logger.debug(response);
		BayerWBSAPIResultType result = epcRestMgr.responseToObject(response, BayerWBSAPIResultType.class);
		if(session == null)
			session = epcRestMgr.getSessionCookie(response);

		if(!result.isSuccessFlag()){
			String errMsg="The interface failed to load any record due to data issues; please verify data.";
			if (result.getError() != null)
			{
				errMsg=result.getError().toString();
				for(BayerWBSAPIType wbs: subList)
				{
					//convert to APIErrorDAO type
					WbsApiErrorDAO statusMsg = this.getWBSAPIErrorDAO(wbs);
					statusMsg.setRootCostObjectID(projectId);
					statusMsg.setStatus(GlobalConstants.STAGING_RECORD_STATUS_FAILED);
					statusMsg.setErrorMsg(errMsg);
					errorList.add(statusMsg);
				}
			}	
			throw new SystemException(errMsg);
		} else {

			List<WbsApiErrorDAO> statusMsgList = new ArrayList<WbsApiErrorDAO>();
			int i=0;
			for(WObjectResultType or : result.getObjectResult()) {
				BayerWBSAPIType wbs = subList.get(i++);				
				String wbsID = wbs.getCostObjectID();

				//convert to APIErrorDAO type
				WbsApiErrorDAO statusMsg = this.getWBSAPIErrorDAO(wbs);
				statusMsg.setRootCostObjectID(projectId);

				if(or.isSuccessFlag()) {
					logger.debug("UPDATE RESULT --> " + or.getInternalId());
					logger.debug("Record with External Key ID of : " + wbsID +" "+ GlobalConstants.STAGING_RECORD_STATUS_LOADED);

					statusMsg.setStatus(GlobalConstants.STAGING_RECORD_STATUS_LOADED);

				} else {
					String str = ((ElementNSImpl)or.getResultMessage().get(0).getMessage()).getTextContent();
					logger.error("ERROR --> " + or.getInternalId() + "|" + wbsID + "|" 
							+ "CostObjectHierarchyLevel: " + wbs.getCostObjectHierarchyLevel() + "|"
							+ "CostObjectID: " + wbs.getCostObjectID() + "|"
							+ "CostObjectName: " + wbs.getCostObjectName() + "|"
							+ "CostObjectStatus: " + wbs.getCostObjectStatus() + "|"
							+ "CostObjectTypeName: " + wbs.getCostObjectTypeName() + "|"
							+ "ExternalKey: " + wbs.getExternalKey() + "|"
							+ "HierarchyPathID: " + wbs.getHierarchyPathID() + "|"
							+ "LocationID: " + wbs.getLocationID() + "|"
							+ "ObjectClass: " + wbs.getObjectClass() + "|"							 
							+ "ParentCostObjectHierarchyPathID: " + wbs.getParentCostObjectHierarchyPathID() + "|"
							+ "PersonResponsible: " + wbs.getPersonResponsible() + "|"
							+ "ProfitCenter: " + wbs.getProfitCenter() + "|"
							+ "ResponsibleCostCenter: " + wbs.getResponsibleCostCenter() + "|"		
							+ "SAPDeleteFlagID: " + wbs.getSAPDeleteFlagID() + "|"
							+ "SAPProjectTypeID: " + wbs.getSAPProjectTypeID()+ "|"
							+ "SAPStatus: " + wbs.getSAPStatus() + "|"
							+ "ResponsibleCostCenter: " + wbs.getResponsibleCostCenter() + "|"		
							+ or.isSuccessFlag() + "|" + str);

					statusMsg.setStatus(GlobalConstants.STAGING_RECORD_STATUS_FAILED);
					statusMsg.setErrorMsg(str);
				}
				//statusMsgList.add(statusMsg);
				//this.wbsAPIErrorDAOList.add(statusMsg);
				errorList.add(statusMsg);
			}
		}
		return session;
	}

	private Cookie requestSub(List<BayerWBSAPIType> subList,
			Cookie session, String baseUri, String projectId, String subHierarchy, List<WbsApiErrorDAO> errorList) throws SystemException {

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
			{
				errMsg=result.getError().toString();
				for(BayerWBSAPIType wbs: subList)
				{
					//convert to APIErrorDAO type
					WbsApiErrorDAO statusMsg = this.getWBSAPIErrorDAO(wbs);
					statusMsg.setRootCostObjectID(projectId);
					statusMsg.setStatus(GlobalConstants.STAGING_RECORD_STATUS_FAILED);
					statusMsg.setErrorMsg(errMsg);
					errorList.add(statusMsg);
				}
			}	
			throw new SystemException(errMsg);
		} else {

			List<WbsApiErrorDAO> statusMsgList = new ArrayList<WbsApiErrorDAO>();
			int i=0;
			for(WObjectResultType or : result.getObjectResult()) {
				BayerWBSAPIType wbs = subList.get(i++);				
				String wbsID = wbs.getCostObjectID();

				//convert to APIErrorDAO type
				WbsApiErrorDAO statusMsg = this.getWBSAPIErrorDAO(wbs);
				statusMsg.setRootCostObjectID(projectId);

				if(or.isSuccessFlag()) {
					logger.debug("UPDATE RESULT --> " + or.getInternalId());
					logger.debug("Record with External Key ID of : " + wbsID +" "+ GlobalConstants.STAGING_RECORD_STATUS_LOADED);

					statusMsg.setStatus(GlobalConstants.STAGING_RECORD_STATUS_LOADED);

				} else {
					String str = ((ElementNSImpl)or.getResultMessage().get(0).getMessage()).getTextContent();
					logger.error("ERROR --> " + or.getInternalId() + "|" + wbsID 					
							+ "CostObjectHierarchyLevel: " + wbs.getCostObjectHierarchyLevel() + "|"
							+ "CostObjectID: " + wbs.getCostObjectID() + "|"
							+ "CostObjectName: " + wbs.getCostObjectName() + "|"
							+ "CostObjectStatus: " + wbs.getCostObjectStatus() + "|"
							+ "CostObjectTypeName: " + wbs.getCostObjectTypeName() + "|"
							+ "ExternalKey: " + wbs.getExternalKey() + "|"
							+ "HierarchyPathID: " + wbs.getHierarchyPathID() + "|"
							+ "LocationID: " + wbs.getLocationID() + "|"
							+ "ObjectClass: " + wbs.getObjectClass() + "|"							 
							+ "ParentCostObjectHierarchyPathID: " + wbs.getParentCostObjectHierarchyPathID() + "|"
							+ "PersonResponsible: " + wbs.getPersonResponsible() + "|"
							+ "ProfitCenter: " + wbs.getProfitCenter() + "|"
							+ "ResponsibleCostCenter: " + wbs.getResponsibleCostCenter() + "|"		
							+ "SAPDeleteFlagID: " + wbs.getSAPDeleteFlagID() + "|"
							+ "SAPProjectTypeID: " + wbs.getSAPProjectTypeID()+ "|"
							+ "SAPStatus: " + wbs.getSAPStatus() + "|"
							+ "ResponsibleCostCenter: " + wbs.getResponsibleCostCenter() + "|"		
							+ "|" + or.isSuccessFlag() + "|" + str);

					statusMsg.setStatus(GlobalConstants.STAGING_RECORD_STATUS_FAILED);
					statusMsg.setErrorMsg(str);
				}
				//statusMsgList.add(statusMsg);
				//this.wbsAPIErrorDAOList.add(statusMsg);
				errorList.add(statusMsg);
			}
		}
		return session;
	}

	private void processStatusMessages(List<WbsApiErrorDAO> errorList, boolean isNew)  throws SystemException {
		try{
			//stgDBMgr.insertWbsBatch(this.wbsAPIErrorDAOList);
			if (isNew)
				stgDBMgr.insertWbsBatch(errorList);
			else
				stgDBMgr.updateWbsBatch(errorList);

		} catch(Exception e) {
			throw new SystemException (e);
		}
	}

	private WbsApiErrorDAO getWBSAPIErrorDAO(BayerWBSAPIType wbs)
	{
		WbsApiErrorDAO wbsApiError = new WbsApiErrorDAO();
		wbsApiError.setCostObjectID(wbs.getCostObjectID());
		wbsApiError.setCostObjectName(wbs.getCostObjectName());
		wbsApiError.setCostObjectStatus(wbs.getCostObjectStatus());
		wbsApiError.setCostObjectTypeName(wbs.getCostObjectTypeName());
		wbsApiError.setExternalKey(wbs.getExternalKey());
		wbsApiError.setHierarchyPathID(wbs.getHierarchyPathID());
		wbsApiError.setLocationID(wbs.getLocationID());
		wbsApiError.setObjectClass(wbs.getObjectClass());
		wbsApiError.setPersonResponsible(wbs.getPersonResponsible());
		wbsApiError.setSAPProjectTypeID(wbs.getSAPProjectTypeID());
		wbsApiError.setResponsibleCostCenter(wbs.getResponsibleCostCenter());
		wbsApiError.setProfitCenter(wbs.getProfitCenter());
		wbsApiError.setSAPDeleteFlagID(wbs.getSAPDeleteFlagID());
		return wbsApiError;
	}

	//Get ErrorList ID for reprocessed Error
	private List<WbsApiErrorDAO> getErrorListWithId(List<WbsApiErrorDAO> oldErrorList, List<WbsApiErrorDAO> newErrorList){
		for (int i = 0; i < newErrorList.size(); i++)
		{
			long j = this.getErrorId(newErrorList.get(i).getHierarchyPathID(), oldErrorList);
			newErrorList.get(i).setId(j);;
		}
		return newErrorList;
	}

	//Get ErrorID for reprocessed Error
	private long getErrorId(String pathId, List<WbsApiErrorDAO> errorList){
		long id = 0;
		for (int i = 0; i < errorList.size(); i++)
		{

			if(errorList.get(i).getHierarchyPathID().equals(pathId))
				id = errorList.get(i).getId();
		}
		return id;
	}

	//Convert WBSAPIErrorDAO list to BayerWBSAPIType List
	private List<BayerWBSAPIType> getBayerWBSAPITypesFromError(List<WbsApiErrorDAO> errorList){

		BayerWBSAPIRequestType request = new BayerWBSAPIRequestType();
		for (int i = 0; i < errorList.size(); i++)
		{
			WbsApiErrorDAO errorDAO = errorList.get(i);
			BayerWBSAPIType apiType = this.getBayerWBSAPITypeFromError(errorDAO);
			request.getBayerWBSAPI().add(apiType);
		}
		return request.getBayerWBSAPI();
	}

	private BayerWBSAPIType getBayerWBSAPITypeFromError(WbsApiErrorDAO wbsError)
	{
		BayerWBSAPIType apiType = new BayerWBSAPIType();
		apiType.setCostObjectID(wbsError.getCostObjectID());
		apiType.setCostObjectName(wbsError.getCostObjectName());
		apiType.setCostObjectStatus(wbsError.getCostObjectStatus());
		apiType.setCostObjectTypeName(wbsError.getCostObjectTypeName());
		apiType.setExternalKey(wbsError.getExternalKey());
		apiType.setHierarchyPathID(wbsError.getHierarchyPathID());
		apiType.setLocationID(wbsError.getLocationID());
		apiType.setObjectClass(wbsError.getObjectClass());
		apiType.setPersonResponsible(wbsError.getPersonResponsible());
		apiType.setSAPProjectTypeID(wbsError.getSAPProjectTypeID());
		apiType.setResponsibleCostCenter(wbsError.getResponsibleCostCenter());
		apiType.setProfitCenter(wbsError.getProfitCenter());
		apiType.setSAPDeleteFlagID(wbsError.getSAPDeleteFlagID());
		return apiType;
	}


	//Convert SAPWBSODataType List to BayerWBSAPIType List
	public List<BayerWBSAPIType> getBayerWBSAPITypesO(List<SapWBSODataType> oDataTypes){

		BayerWBSAPIRequestType request = new BayerWBSAPIRequestType();
		for (int i = 0; i < oDataTypes.size(); i++)
		{
			SapWBSODataType oDataType = oDataTypes.get(i);
			BayerWBSAPIType apiType = this.getWBSAPIType(oDataType);
			request.getBayerWBSAPI().add(apiType);
			if (isValidODataType(oDataType))
			{

			}
		}
		return request.getBayerWBSAPI();
	}

	public List<WbsApiErrorDAO> getBayerWBSAPITypesInvalidO(List<SapWBSODataType> oDataTypes){

		List<WbsApiErrorDAO> errorList = new ArrayList<WbsApiErrorDAO>();
		for (int i = 0; i < oDataTypes.size(); i++)
		{
			SapWBSODataType oDataType = oDataTypes.get(i);
			if (!isValidODataType(oDataType))
			{
				BayerWBSAPIType apiType = this.getWBSAPIType(oDataType);
				WbsApiErrorDAO errorDAO = this.getWBSAPIErrorDAO(apiType);
				errorDAO.setStatus(GlobalConstants.STAGING_RECORD_STATUS_SKIPPED);
				errorList.add(errorDAO);
			}
		}
		return errorList;
	}

	private boolean isValidODataType (SapWBSODataType oDataType)
	{
		boolean isValid = true;
		BayerWBSAPIType apiType = new BayerWBSAPIType();
		String projectId = oDataType.getProjectDefinition();
		String wbsId = oDataType.getWbsElement();
		String parentWbs = oDataType.getParentWbs();
		if (projectId == null ||projectId.equals("")||
				wbsId == null||wbsId.equals(""))
			isValid = false;
		return isValid;
	}
	/* Convert SapWBSODataType Object to BayerWBSAPIType Object
	 * 
	 */
	public BayerWBSAPIType getWBSAPIType(SapWBSODataType oDataType)
	{
		BayerWBSAPIType apiType = new BayerWBSAPIType();
		String projectId = oDataType.getProjectDefinition();
		String hierarchyPathId = this.getCostObjectHierarchyPathId(oDataType);
		if (!hierarchyPathId.equals(projectId))
		{
			apiType.setHierarchyPathID(hierarchyPathId);
			apiType.setCostObjectID(this.getCostObjectId(hierarchyPathId));
			//apiType.setCostObjectTypeInternalID(GlobalConstants.EPC_REST_COSTOBJECTTYPE_WBS_INTERNAL_ID);
		}
		else
		{
			apiType.setHierarchyPathID(projectId);
			apiType.setCostObjectID(projectId);
		}
		apiType.setCostObjectName(oDataType.getDescription());
		apiType.setExternalKey(oDataType.getWbsElement());
		apiType.setCostObjectHierarchyLevel(oDataType.getHierarchyLevel());
		apiType.setCostObjectStatus(GlobalConstants.EPC_REST_DEFAULT_COSTOBJECT_STATUS);
		apiType.setCostObjectTypeName("WBS");
		apiType.setObjectClass(oDataType.getObjectClass());

		//apiType.setCostObjectHierarchyLevel(oDataType.getWbsLevel());
		//apiType.setParentCostObjectID(oDataType.getParentWbs());
		//What CostObject Status should be?
		//No api field for ObjectClass
		apiType.setLocationID(oDataType.getLocation());
		apiType.setResponsibleCostCenter(oDataType.getResponsibleNo());
		apiType.setPersonResponsible(oDataType.getRespsblCctr());
		apiType.setSAPProjectTypeID(oDataType.getProjType());
		apiType.setProfitCenter(oDataType.getProfitCtr());
		apiType.setSAPStatus(oDataType.getStatus());
		if (oDataType.getDeletionFlag().equalsIgnoreCase("X"))
			apiType.setSAPDeleteFlagID(GlobalConstants.EPC_API_ERROR_FLAG_Y);

		return apiType;
	}

	public String getCostObjectHierarchyPathId(SapWBSODataType oDataType)
	{
		String heirarchyPathId = new String();
		String wbsElement = oDataType.getWbsElement();
		String projectId = oDataType.getProjectDefinition();
		int wbsLevel = oDataType.getWbsLevel();
		String parentWbs = oDataType.getParentWbs();
		heirarchyPathId = this.buildHierarchyPath(wbsElement, parentWbs, projectId);
		return heirarchyPathId;
	}

	public String getCostObjectId(String hierarchyPathId)
	{
		return hierarchyPathId.substring(hierarchyPathId.lastIndexOf(GlobalConstants.EPC_REST_HIERARCHY_PATH_SEPERATOR)+1);
	}

	public String buildHierarchyPath(String wbsElement, String parentWbs, String projectId)
	{
		String hierarchyPath = new String();
		if (parentWbs==null)
			hierarchyPath = wbsElement;
		else if (parentWbs.equals(projectId))
			hierarchyPath = projectId + "." + this.processWbsElement(wbsElement, projectId); 
		else
		{
			if (wbsElement.equals("A00NC-003247-C"))
				hierarchyPath = "A00NC-003247.C";
			if (wbsElement.equals("A00NC-003247-C1"))
				hierarchyPath = "A00NC-003247.C.1";
			if (wbsElement.equals("A00NC-003247-C2"))
				hierarchyPath = "A00NC-003247.C.2";
			if (wbsElement.equals("A00NC-003247-E"))
				hierarchyPath = "A00NC-003247.E";
			if (wbsElement.equals("A00NC-003247-E1"))
				hierarchyPath = "A00NC-003247.E.1";
			if (wbsElement.equals("A00NC-003247"))
				hierarchyPath = "A00NC-003247";
			if (wbsElement.equals("A00NC-003247-C"))
				hierarchyPath = "A00NC-003247.C";
			if (wbsElement.equals("A00NC-003247-C1"))
				hierarchyPath = "A00NC-003247.C.1";
			if (wbsElement.equals("A00NC-003247-C2"))
				hierarchyPath = "A00NC-003247.C.2";
			if (wbsElement.equals("A00NC-003247-E"))
				hierarchyPath = "A00NC-003247.E";
			if (wbsElement.equals("A00NC-003247-E1"))
				hierarchyPath = "A00NC-003247.E.1";
		}
		return hierarchyPath;
	}

	public String processWbsElement(String wbsElement, String projectId)
	{
		return wbsElement.substring(wbsElement.indexOf(projectId)+projectId.length()+1);
	}

	//Maybe useful if need to retrieve WBS tree from EcoSys
	private List<BayerWBSAPIType> getWBSAPITypes(Client client, Cookie session, String baseUri, HashMap wbsMap) throws SystemException {

		@SuppressWarnings("unchecked")
		ClientResponse response = epcRestMgr
		.getAsApplicationXml(client, baseUri,
				GlobalConstants.EPC_REST_IMPORT_SAP_WBS,session,wbsMap);
		//ClientResponse response = epcRestMgr.ge;
		logger.debug(response);
		BayerWBSAPIResultType result = epcRestMgr.responseToObject(response, BayerWBSAPIResultType.class);
		List<BayerWBSAPIType> wbsAPITypes = result.getBayerWBSAPI();
		if(session == null)
			session = epcRestMgr.getSessionCookie(response);

		if(!result.isSuccessFlag()){
			String errMsg="The interface failed to retrieve Wbs; please verify connection.";
			if (result.getError() != null)
				errMsg=result.getError().toString();

			throw new SystemException(errMsg);
		} else {

			//List<StatusMessage> statusMsgList = new ArrayList<StatusMessage>();
			int i=0;
		}
		return wbsAPITypes;
	}

	private List<BayerWBSAPIType> readWBSData(String projectId, String systemId, boolean isSub, String masterProjectId) throws SystemException {
		//Map<String, Map<String, String>> dataRows = odataSvcMgr.readWBSHierarchy(projectId, systemId);
		Map<String, Map<String, Object>> dataRows = odataSvcMgr.readWBSHierarchy(projectId, systemId);
		List<BayerWBSAPIType> wbsRecords = new ArrayList<BayerWBSAPIType>();
		if (dataRows !=null)
		{
			Map<String, BayerWBSAPIType> wbsList = odataSvcMgr.mapWBSHierarchyForImport(dataRows, isSub, masterProjectId);
			wbsRecords.addAll(wbsList.values());
		}
		return wbsRecords;
	}


	public List<BayerWBSAPIType> getBayerWBSAPITypesValid(List<BayerWBSAPIType> apiTypes, String projectId, String sapProjectId){

		BayerWBSAPIRequestType request = new BayerWBSAPIRequestType();

		for (int i = 0; i < apiTypes.size(); i++)
		{
			BayerWBSAPIType apiType = apiTypes.get(i);

			if (isValidAPIType(apiType))
			{
				//apiType.setCostObjectHierarchyLevel(apiType.getCostObjectHierarchyLevel());
				apiType.setCostObjectTypeName(GlobalConstants.EPC_REST_COSTOBJECTTYPE_WBS);
				if (!projectId.equalsIgnoreCase(sapProjectId))
				{	
					apiType.setHierarchyPathID(this.recalcHierarchyPathID(apiType.getHierarchyPathID(), projectId));
				}
				request.getBayerWBSAPI().add(apiType);
			}
		}
		return request.getBayerWBSAPI();
	}

	private  List<BayerWBSReadAPIType> readParentCOs(String projectId) throws SystemException{

		List<BayerWBSReadAPIType> parentCOs = new ArrayList<BayerWBSReadAPIType>();
		try 
		{	
			//Read PM Order Data from EcoSys using PMOrder API
			Cookie session = null;
			String baseUri2 = GlobalConstants.EPC_REST_PROTOCOL + "://" + GlobalConstants.EPC_REST_SERVER + ":"
					+ GlobalConstants.EPC_REST_PORT + "/" + GlobalConstants.EPC_REST_BASEURI2;				

			HashMap<String, String> aMap = new HashMap<String, String>();
			aMap.put(GlobalConstants.EPC_PROJECT_PARAM, projectId);
			parentCOs = this.getBayerWBSReadAPITypes(session, baseUri2, aMap);
			return parentCOs;

		} catch(Exception e) {
			throw new SystemException(e);
		}
	}

	private  List<BayerWBSReadAPIType> getParentCO(String projectId, String hpid) throws SystemException{

		List<BayerWBSReadAPIType> parentCOs = new ArrayList<BayerWBSReadAPIType>();
		try 
		{	
			//Read PM Order Data from EcoSys using PMOrder API
			Cookie session = null;
			String baseUri2 = GlobalConstants.EPC_REST_PROTOCOL + "://" + GlobalConstants.EPC_REST_SERVER + ":"
					+ GlobalConstants.EPC_REST_PORT + "/" + GlobalConstants.EPC_REST_BASEURI2;				

			HashMap<String, String> aMap = new HashMap<String, String>();
			aMap.put(GlobalConstants.EPC_PROJECT_PARAM, projectId);
			//aMap.put("Level", Integer.toString(level));
			aMap.put("hpid", hpid);
			parentCOs = this.getBayerWBSReadAPITypes(session, baseUri2, aMap);
			return parentCOs;

		} catch(Exception e) {
			throw new SystemException(e);
		}
	}

	private List<BayerWBSReadAPIType> getBayerWBSReadAPITypes(Cookie session, String baseUri, HashMap<String, String> coMap) throws SystemException {

		ClientResponse response = epcRestMgr
				.getAsApplicationXml(client, baseUri,
						GlobalConstants.EPC_REST_READ_ECOSYS_WBS,session,coMap);

		logger.debug(response);
		BayerWBSReadAPIResultType result = epcRestMgr.responseToObject(response, BayerWBSReadAPIResultType.class);
		List<BayerWBSReadAPIType> apiTypes = result.getBayerWBSReadAPI();
		if(session == null)
			session = epcRestMgr.getSessionCookie(response);

		if(!result.isSuccessFlag()){
			String errMsg="The interface failed to retrieve po line item cost objects; please verify connection.";
			if (result.getError() != null)
				errMsg=result.getError().toString();

			throw new SystemException(errMsg);
		} else {
			int i=0;
		}
		return apiTypes;
	}


	private String recalcHierarchyPathID (String oldPath, String projectId)
	{
		String newPath = "";
		int dotPos = oldPath.indexOf(GlobalConstants.EPC_REST_HIERARCHY_PATH_SEPERATOR);
		if (dotPos > 0)
			newPath = projectId +GlobalConstants.EPC_REST_HIERARCHY_PATH_SEPERATOR + oldPath.substring(dotPos+1);
		else
			newPath = projectId;
		return newPath;
	}

	public List<BayerWBSAPIType> getBayerWBSAPITypesByLevel(String projectCurrency, List<BayerWBSAPIType> apiTypes, int level){

		BayerWBSAPIRequestType request = new BayerWBSAPIRequestType();
		String missingWBS = GlobalConstants.EPC_TEST_MISSING_PARENT;
		for (int i = 0; i < apiTypes.size(); i++)
		{
			BayerWBSAPIType apiType = apiTypes.get(i);
			apiType.setCostObjectCurrencyCode(projectCurrency);
			
			if (apiType.getCostObjectHierarchyLevel()==level)
			{
				if (missingWBS == null) 
					request.getBayerWBSAPI().add(apiType);
				else if (!apiType.getCostObjectID().equalsIgnoreCase(missingWBS))
					request.getBayerWBSAPI().add(apiType);
				else
					logger.debug("Skipping Testing Missing WBS: " + apiType.getExternalKey());
			}
		}
		return request.getBayerWBSAPI();
	}

	public List<WbsApiErrorDAO> getBayerWBSAPITypesInvalid(List<BayerWBSAPIType> apiTypes, String projectId, String sapProjectId){

		List<WbsApiErrorDAO> errorList = new ArrayList<WbsApiErrorDAO>();
		for (int i = 0; i < apiTypes.size(); i++)
		{
			BayerWBSAPIType apiType = apiTypes.get(i);
			if (!isValidAPIType(apiType))
			{
				if (!projectId.equalsIgnoreCase(sapProjectId))
				{	
					apiType.setHierarchyPathID(this.recalcHierarchyPathID(apiType.getHierarchyPathID(), projectId));
				}
				WbsApiErrorDAO errorDAO = this.getWBSAPIErrorDAO(apiType);
				errorDAO.setStatus(GlobalConstants.STAGING_RECORD_STATUS_SKIPPED);
				errorList.add(errorDAO);
			}
		}
		return errorList;
	}

	private boolean isValidAPIType (BayerWBSAPIType apiType)
	{
		boolean isValid = true;
		String wbsId = apiType.getCostObjectID();
		String pathId = apiType.getHierarchyPathID();
		if (pathId == null ||pathId.equals("")||
				wbsId == null||wbsId.equals(""))
			isValid = false;
		return isValid;
	}

	//Convert SAPWBSODataType List to BayerWBSAPIType List
	public List<BayerWBSAPIType> getBayerWBSAPITypesValidO(List<SapWBSODataType> oDataTypes){

		BayerWBSAPIRequestType request = new BayerWBSAPIRequestType();
		for (int i = 0; i < oDataTypes.size(); i++)
		{
			SapWBSODataType oDataType = oDataTypes.get(i);
			if (isValidODataType(oDataType))
			{
				BayerWBSAPIType apiType = this.getWBSAPIType(oDataType);
				request.getBayerWBSAPI().add(apiType);
			}
		}
		return request.getBayerWBSAPI();
	}

	//Convert SAPWBSODataType List to BayerWBSAPIType List
	public List<BayerWBSAPIType> getBayerWBSAPITypesValidOSub(List<SapWBSODataType> oDataTypes, String masterProjectId){

		BayerWBSAPIRequestType request = new BayerWBSAPIRequestType();
		for (int i = 0; i < oDataTypes.size(); i++)
		{
			SapWBSODataType oDataType = oDataTypes.get(i);
			if (isValidODataType(oDataType))
			{
				BayerWBSAPIType apiType = this.getWBSAPIType(oDataType);
				String pathId = masterProjectId + "." + apiType.getHierarchyPathID();
				apiType.setHierarchyPathID(pathId);
				apiType.setExternalKey(masterProjectId+"."+apiType.getExternalKey());
				request.getBayerWBSAPI().add(apiType);
			}
		}
		return request.getBayerWBSAPI();
	}

	public List<BayerWBSAPIType> getBayerWBSAPITypesValidSub(List<BayerWBSAPIType> apiTypes, String projectId, String sapProjectId, String masterProjectId){

		BayerWBSAPIRequestType request = new BayerWBSAPIRequestType();
		for (int i = 0; i < apiTypes.size(); i++)
		{
			BayerWBSAPIType apiType = apiTypes.get(i);
			if (isValidAPIType(apiType))
			{	
				if (!projectId.equalsIgnoreCase(sapProjectId))
				{	
					apiType.setHierarchyPathID(this.recalcHierarchyPathID(apiType.getHierarchyPathID(), projectId));
				}
				String pathId = masterProjectId + GlobalConstants.EPC_REST_HIERARCHY_PATH_SEPERATOR + apiType.getHierarchyPathID();
				apiType.setHierarchyPathID(pathId);
				//apiType.setExternalKey(masterProjectId+"."+apiType.getExternalKey());
				apiType.setExternalKey(apiType.getExternalKey());
				request.getBayerWBSAPI().add(apiType);
			}
		}
		return request.getBayerWBSAPI();
	}

	private List<String> calcWBSIds( String projectId) throws SystemException{

		logger.debug("Trigger Action to recalculate WBS ID and Name Custom Field for Project: " + projectId);

		List<String> retStatusMsgList = new ArrayList<String>();
		try {


			//Prepare for the REST call
			String baseUri = GlobalConstants.EPC_REST_PROTOCOL + "://" + GlobalConstants.EPC_REST_SERVER + ":" + 
					GlobalConstants.EPC_REST_PORT + "/" + GlobalConstants.EPC_REST_BASEURI2;


			Stopwatch timerBatch = new Stopwatch();			
			Cookie session = null;

			List<String> statusMsgList = new ArrayList<String>();
			timerBatch.start();
			session = this.requestCalc(projectId, session, baseUri);
			//logger.info(end + " of " + total + " - elapsed time: " + timerBatch.stop().toString(ISOPeriodFormat.alternateExtended()));
			retStatusMsgList.addAll(statusMsgList);


			//this.epcRestMgr.logout(client, baseUri, session);

		} catch(Exception e) {
			throw new SystemException(e);
		}
		logger.debug("Complete!");
		return retStatusMsgList;
	}
	private Cookie requestCalc(String projectId, Cookie session, 
			String baseUri) throws SystemException {

		BayerCalculateWBSIDAPIResultType request = new BayerCalculateWBSIDAPIResultType();
		//request.getBayerCommitmentLIAPI().addAll(subList);

		HashMap<String,String> filterMap = new HashMap<String, String>();
		filterMap.put(GlobalConstants.EPC_ROOTCOSTOBJECT_PARAM, projectId);

		CalcWObjectFactory objectFactory = new CalcWObjectFactory();
		JAXBElement<BayerCalculateWBSIDAPIResultType> requestWrapper = objectFactory.createBayerCalculateWBSIDAPIResult(request);

		ClientResponse response = epcRestMgr
				.postApplicationXmlAsApplicationXml(client, requestWrapper,
						baseUri, GlobalConstants.EPC_REST_CALC_WBS_ID,
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

			//List<StatusMessage> statusMsgList = new ArrayList<StatusMessage>();
			logger.debug("1000 -- WBS Import - WBS ID Recalculation Completed Successfully");
			int i=0;
		}
		return session;
	}
}