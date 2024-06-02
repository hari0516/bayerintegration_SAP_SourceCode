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

import com.bayer.integration.odata.SapPOHODataType;
import com.bayer.integration.odata.SapPRHODataType;
import com.bayer.integration.persistence.PohApiErrorDAO;
import com.bayer.integration.properties.GlobalConstants;
import com.bayer.integration.rest.project.BayerProjectAPIType;
import com.bayer.integration.rest.wbs.BayerWBSAPIRequestType;
import com.bayer.integration.rest.wbs.BayerWBSAPIType;
import com.bayer.integration.rest.project.BayerProjectAPIResultType;
import com.bayer.integration.rest.log.IntegrationIssuesAPIRequestType;
import com.bayer.integration.rest.log.IntegrationIssuesAPIResultType;
import com.bayer.integration.rest.log.IntegrationIssuesAPIType;
import com.bayer.integration.rest.log.LogObjectFactory;
import com.bayer.integration.rest.poprh2.BayerPOPRHeadersV2APIRequestType;
import com.bayer.integration.rest.poprh2.BayerPOPRHeadersV2APIResultType;
import com.bayer.integration.rest.poprh2.BayerPOPRHeadersV2APIType;
import com.bayer.integration.rest.poprh2.ObjectFactory;
import com.bayer.integration.rest.poprh2.ObjectResultType;
import com.ecosys.exception.SystemException;
import com.ecosys.util.Stopwatch;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.org.apache.xerces.internal.dom.ElementNSImpl;

/**
 * @author pwng
 *
 */
public class PRHeaderImportMgrImpl extends ImportManagerBase implements
		ImportManager {

	/* (non-Javadoc)
	 * @see com.bayer.integration.service.ImportManager#importData()
	 */
	//@Override
	final String itgInterface = "PRH";
	List<IntegrationIssuesAPIType> itgIssueLog = new ArrayList<IntegrationIssuesAPIType>();
	public int importData() {
		int retCode=GlobalConstants.IMPORT_SAP_PRH_SUCCESS;
		try{			
			if (!GlobalConstants.SKIP_SAP_PRH_INTERFACE) {
				
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
					String projectHierarchyPathId = projectAPIType.getHierarchyPathID();
					String sapProjectId = projectAPIType.getSapProjectId();
					String systemId = projectAPIType.getSAPSystemID();
					List<PohApiErrorDAO> errorDAOList = new ArrayList<PohApiErrorDAO>();
					List<BayerPOPRHeadersV2APIType> inputList = new ArrayList<BayerPOPRHeadersV2APIType>();
					List<PohApiErrorDAO> invalidRecords = new ArrayList<PohApiErrorDAO>(); 
					List<BayerPOPRHeadersV2APIType> validRecords = new ArrayList<BayerPOPRHeadersV2APIType>();
					List<SapPRHODataType> inputListSample = new ArrayList<SapPRHODataType>();
					
					String masterProjectId = null;
					if (projectAPIType.getParentCostObjectID()!=null && projectAPIType.getParentCostObjectID()!="")
						masterProjectId = projectAPIType.getParentCostObjectID();
					
					boolean isSub = false;
					if (projectAPIType.getProjectTypeID().equals(GlobalConstants.EPC_API_PROJECT_TYPE_SUB))
					{
							isSub = true;
							projectHierarchyPathId = masterProjectId;
					}
					
					try
					{
						//Retrieve last run failed records and process\
						/*
						if (!skipError)
						{
							logger.debug("Reading previous run failed PR Headers data from ERR Log table for Project: "+ projectId);
							List<PohApiErrorDAO> lastTimeFailedDAOList = new ArrayList<PohApiErrorDAO>();
							//lastTimeFailedDAOList = stgDBMgr.findFailedPohByProject(projectId, GlobalConstants.EPC_PRH_API_ERROR_SELECT_BY_PROJECT);

							if (lastTimeFailedDAOList.size()>0){
								logger.debug("Processing previous run failed PR Headers data from ERR Log table for Project"+ projectId);
								errorDAOList = this.processProjects(this.getBayerPOPRHeadersV2APITypesFromError(lastTimeFailedDAOList), projectId);
								errorDAOList = this.getErrorListWithId(lastTimeFailedDAOList, errorDAOList);
								
								this.processStatusMessages(errorDAOList, false);
								logger.debug("Processing previous run failed PR Headers data completed for Project: 0"+ projectId);
							}
							else{
								logger.debug("No previous run failed PR Headers data for processing for Project: "+ projectId);
							}
						}
						*/
						
						logger.debug("Reading PR Headers data from SAP Input for Project: "+ projectId);
						if (isLive)
						{
							inputList = readSapData(projectHierarchyPathId, sapProjectId, systemId);	
							invalidRecords = this.getSapInputInvalid(inputList);
							validRecords = this.getBayerPOPRHeadersV2APITypesValid(inputList);
							logger.debug("Number of Records Read in from OData: " + inputList.size() + " for Project: " + projectId);
						}
						else
						{
							inputListSample = importHelper.getSapPRHTypesSample();						
							invalidRecords = this.getBayerPOPRHeadersV2APITypesInvalid(inputListSample);
							validRecords = this.getBayerPOPRHeadersV2APITypes(inputListSample);
						}

						//Process Invalid Records
						logger.debug("Processing Invalid Records, If Any for Project: " + projectId);
						if (invalidRecords.size()>0 && !skipError)
							this.processStatusMessages(invalidRecords, true);
						logger.debug("Processing Invalid Records Completed for Project: "+ projectId);	
											
						errorDAOList  = processProjects(validRecords, projectId);

						//Process Status Messages
						if (!skipError)
						{
							logger.debug("Processing ERR Messages, If Any for Project: " + projectId);
							//this.processStatusMessages(errorDAOList, true);
							logger.debug("Processing ERR Message Completed for Project: "+ projectId);	
						}
						
						// call method to process errors for EPC request
						logger.debug("posting "+itgIssueLog.size()+" " +itgInterface+" issues to EPC");
						if (itgIssueLog.size()>0)
							processIssueLog(itgIssueLog);
					}
		 			catch(SystemException se) {
		 				logger.error("6005 -- PR Header Import Failed for Project: " + GlobalConstants.EPC_PROJECT_ID_PROCESSED, se);
		 				retCode=GlobalConstants.IMPORT_SAP_PRH_FAILED;
		 				continue;
		 			}

		 		}
				
			} else {
				logger.info("Skipped PR Header Import Interface. Change the skip property to 'false'");				
			}
			
		}catch(Exception e) {			
			logger.error("6005 -- PR Header Import Failed", e);			
			retCode=GlobalConstants.IMPORT_SAP_PRH_FAILED;
		}
		
		if (retCode==GlobalConstants.IMPORT_SAP_PRH_SUCCESS)
			logger.debug("6000 -- PR Header Import Completed Successfully");
		
		return retCode;
	}

	
	// create IntegrationIssuesAPIType
	private IntegrationIssuesAPIType getIntegrationIssuesAPIType(String logErrorId, String logDescription, String logComment, String externalKey) {
		IntegrationIssuesAPIType logEntry = new IntegrationIssuesAPIType();
		logEntry.setIntegrationLogID(itgInterface);
		//logEntry.setIntegrationLogID(itgInterface+"."+logErrorId);
		logEntry.setDescription(logDescription);
		logEntry.setComment(logComment);
		return logEntry;	
	}

	// process log to EPC
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

	// POST to EPC
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
	// see below
	return session;	
	}
	
	
	
private List<BayerPOPRHeadersV2APIType> readProjectData() {
		return null;
	}

private List<PohApiErrorDAO> processProjects( List<BayerPOPRHeadersV2APIType> prjRecords, String projectId) throws SystemException{
		
		logger.debug("Importing PR Headers to EPC...");
		
		List<PohApiErrorDAO> retStatusMsgList = new ArrayList<PohApiErrorDAO>();
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
				List<PohApiErrorDAO> statusMsgList = new ArrayList<PohApiErrorDAO>();
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

	
	private List<BayerPOPRHeadersV2APIType> getPOPRHeadersAPITypes(Client client, Cookie session, String baseUri, HashMap POPRHeadersMap) throws SystemException {
		
		ClientResponse response = epcRestMgr
			.getAsApplicationXml(client, baseUri,
					GlobalConstants.EPC_REST_IMPORT_SAP_PO_HDR,session,POPRHeadersMap);
		//ClientResponse response = epcRestMgr.ge;
		logger.debug(response);
		BayerPOPRHeadersV2APIResultType result = epcRestMgr.responseToObject(response, BayerPOPRHeadersV2APIResultType.class);
		List<BayerPOPRHeadersV2APIType> PRHAPITypes = result.getBayerPOPRHeadersV2API();
		if(session == null)
			session = epcRestMgr.getSessionCookie(response);
	
		if(!result.isSuccessFlag()){
			String errMsg="The interface failed to retrieve Commitment Headers; please verify connection.";
		if (result.getError() != null)
			errMsg=result.getError().toString();
		
			throw new SystemException(errMsg);
		} else {
	
		//List<StatusMessage> statusMsgList = new ArrayList<StatusMessage>();
		int i=0;
		}
		return PRHAPITypes;
	}

	private Cookie request(List<BayerPOPRHeadersV2APIType> subList,
			Cookie session, String baseUri, String projectId, List<PohApiErrorDAO> errorList) throws SystemException {
		
		BayerPOPRHeadersV2APIRequestType request = new BayerPOPRHeadersV2APIRequestType();
		request.getBayerPOPRHeadersV2API().addAll(subList);

		ObjectFactory objectFactory = new ObjectFactory();
		JAXBElement<BayerPOPRHeadersV2APIRequestType> requestWrapper = objectFactory.createBayerPOPRHeadersV2APIRequest(request);
		HashMap<String, String> prjMap = new HashMap<String, String>();
		//prjMap.put(GlobalConstants.EPC_PROJECT_PARAM, projectId);
		ClientResponse response = epcRestMgr
					.postApplicationXmlAsApplicationXml(client, requestWrapper,
							baseUri, GlobalConstants.EPC_REST_IMPORT_SAP_PO_HDR_V2,
							session, prjMap);
							
		logger.debug(response);
		BayerPOPRHeadersV2APIResultType result = epcRestMgr.responseToObject(response, BayerPOPRHeadersV2APIResultType.class);
		if(session == null)
			session = epcRestMgr.getSessionCookie(response);
		
		if(!result.isSuccessFlag()){
			String errMsg="The interface failed to load any record due to data issues; please verify data.";
			if (result.getError() != null)
				errMsg=result.getError().toString();
				for(BayerPOPRHeadersV2APIType poh: subList)
				{
					//convert to APIErrorDAO type
					PohApiErrorDAO statusMsg = this.getPohAPIErrorDAO(poh);
					statusMsg.setRootCostObjectID(projectId);
					statusMsg.setStatus(GlobalConstants.STAGING_RECORD_STATUS_FAILED);
					statusMsg.setErrorMsg(errMsg);
					errorList.add(statusMsg);
				}
			throw new SystemException(errMsg);
		} else {
		
			//List<StatusMessage> statusMsgList = new ArrayList<StatusMessage>();
			int i=0;
			for(ObjectResultType or : result.getObjectResult()) {
				BayerPOPRHeadersV2APIType prHeader = subList.get(i++);
				String prhID = prHeader.getTaskID();
					
				//convert to APIErrorDAO type
				PohApiErrorDAO statusMsg = this.getPohAPIErrorDAO(prHeader);
				statusMsg.setRootCostObjectID(projectId);
				
				
				if(or.isSuccessFlag()) {
					logger.debug("UPDATE RESULT --> " + or.getInternalId());
					logger.debug("Record with External Key ID of : " + prhID +" "+ GlobalConstants.STAGING_RECORD_STATUS_LOADED);
					/*
					logger.debug("Record --> " + or.getInternalId() + "|" + prhID + "|"
							 + "CommitmentTypeHierarchyPathID: " + prHeader.getCommitmentTypeHierarchyPathID() + "|"
							 + "HierarchyPathID: " + prHeader.getHierarchyPathID() + "|"
							 + "OwnerCostObjectExternalKey: " + prHeader.getOwnerCostObjectExternalKey() + "|"
							 + "OwnerCostObjectHierarchyPathID: " + prHeader.getOwnerCostObjectHierarchyPathID() + "|"
							 + "Receiver: " + prHeader.getReceiver() + "|"
							 + "Requestor: " + prHeader.getRequestor() + "|"
							 + "TaskID: " + prHeader.getTaskID() + "|"
							 + "TaskInternalID: " + prHeader.getTaskInternalID() + "|"
							 + "TaskName: " + prHeader.getTaskName() + "|"
							 + "VendorID: " + prHeader.getVendorID() + "|"
							 + "VendorName: " + prHeader.getVendorName() + "|"
							 + "ActualCosts: " + prHeader.getActualCosts() + "|"
							 + "WorkingForecastCosts: " + prHeader.getWorkingForecastCosts() + "|"
							 + "CommittedDate: " + prHeader.getCommittedDate() + "|"
							 + "LastInvoiceDate: " + prHeader.getLastInvoiceDate() + "|"
							+ or.isSuccessFlag() + "| " + "updated");	
					*/
					statusMsg.setStatus(GlobalConstants.STAGING_RECORD_STATUS_LOADED);
				} else {
					String str = ((ElementNSImpl)or.getResultMessage().get(0).getMessage()).getTextContent();
					logger.error("ERROR --> " + or.getInternalId() + "|" + prhID + "|"
							 + "CommitmentTypeHierarchyPathID: " + prHeader.getCommitmentTypeHierarchyPathID() + "|"
							 + "HierarchyPathID: " + prHeader.getHierarchyPathID() + "|"
							 + "OwnerCostObjectExternalKey: " + prHeader.getOwnerCostObjectExternalKey() + "|"
							 + "OwnerCostObjectHierarchyPathID: " + prHeader.getOwnerCostObjectHierarchyPathID() + "|"
							 + "Receiver: " + prHeader.getReceiver() + "|"
							 + "Requestor: " + prHeader.getRequestor() + "|"
							 + "TaskID: " + prHeader.getTaskID() + "|"
							 + "TaskInternalID: " + prHeader.getTaskInternalID() + "|"
							 + "TaskName: " + prHeader.getTaskName() + "|"
							 + "VendorID: " + prHeader.getVendorID() + "|"
							 + "VendorName: " + prHeader.getVendorName() + "|"
							 + "ActualCosts: " + prHeader.getActualCosts() + "|"
							 + "WorkingForecastCosts: " + prHeader.getWorkingForecastCosts() + "|"
							 + "CommittedDate: " + prHeader.getCommittedDate() + "|"
							 + "LastInvoiceDate: " + prHeader.getLastInvoiceDate() + "|"
							+ or.isSuccessFlag() + "|" + str);	
					statusMsg.setStatus(GlobalConstants.STAGING_RECORD_STATUS_FAILED);
					statusMsg.setErrorMsg(str);
					itgIssueLog.add(getIntegrationIssuesAPIType("" ,str, projectId,prhID));
				}
				errorList.add(statusMsg);					
			}
		}
		return session;
	}
	
    
	//Convert SAPWBSODataType List to BayerWBSAPIType List
    public List<BayerPOPRHeadersV2APIType> getBayerPOPRHeadersV2APITypes(List<SapPRHODataType> oDataTypes){
		
    	BayerPOPRHeadersV2APIRequestType request = new BayerPOPRHeadersV2APIRequestType();
    	for (int i = 0; i < oDataTypes.size(); i++)
    	{
    		
    		SapPRHODataType oDataType = oDataTypes.get(i);
    		if (isValidODataType(oDataType))
    		{
    			BayerPOPRHeadersV2APIType apiType = this.getPOPRHeadersAPIType(oDataType);
    			request.getBayerPOPRHeadersV2API().add(apiType);
    		}
    	}
        return request.getBayerPOPRHeadersV2API();
    }
    
    public List<PohApiErrorDAO> getBayerPOPRHeadersV2APITypesInvalid(List<SapPRHODataType> oDataTypes){
		
    	List<PohApiErrorDAO> errorList = new ArrayList<PohApiErrorDAO>();
    	for (int i = 0; i < oDataTypes.size(); i++)
    	{
    		SapPRHODataType oDataType = oDataTypes.get(i);
    		if (!isValidODataType(oDataType))
    		{
    			BayerPOPRHeadersV2APIType apiType = this.getPOPRHeadersAPIType(oDataType);
				PohApiErrorDAO errorDAO = this.getPohAPIErrorDAO(apiType);
				errorDAO.setStatus(GlobalConstants.STAGING_RECORD_STATUS_SKIPPED);
    			errorList.add(errorDAO);
    		}
    	}
        return errorList;
    }
    
	private boolean isValidODataType (SapPRHODataType oDataType)
	{
		boolean isValid = true;
		BayerPOPRHeadersV2APIType apiType = new BayerPOPRHeadersV2APIType();
		String projectId = oDataType.getProjectDefinition();
		//String pmoId = oDataType.getPmOrderid();
		String commitmentId = oDataType.getPurchReq();
		if (projectId == null ||projectId.equals("")||
				commitmentId == null||commitmentId.equals(""))
			isValid = false;
		return isValid;
	}
    
    /* Convert SapWBSODataType Object to BayerWBSAPIType Object
     * 
     */
	public BayerPOPRHeadersV2APIType getPOPRHeadersAPIType(SapPRHODataType oDataType)
	{
		BayerPOPRHeadersV2APIType apiType = new BayerPOPRHeadersV2APIType();
		String projectId = oDataType.getProjectDefinition();
		String prhId = oDataType.getPurchReq();

		apiType.setOwnerCostObjectExternalKey(oDataType.getProjectDefinition());
		apiType.setTaskID(prhId);
		apiType.setTaskName(oDataType.getPurchReqDesc());
		apiType.setVendorID(oDataType.getVendorId());
		apiType.setVendorName(oDataType.getVendorName());
		apiType.setCommitmentTypeHierarchyPathID(GlobalConstants.EPC_REST_COMMITMENT_TYPE_PR);	
		//apiType.setActualCosts(oDataType.getPrActCost());
		//apiType.setWorkingForecastCosts(oDataType.getPrComCost());

		return apiType;
	}
	
	private void processStatusMessages(List<PohApiErrorDAO> errorList, boolean isNew)  throws SystemException {
		try{
		if (isNew)
			stgDBMgr.insertPohBatch(errorList, GlobalConstants.EPC_PRH_API_ERROR_BATCH_INSERT);
		else
			stgDBMgr.updatePohBatch(errorList, GlobalConstants.EPC_PRH_API_ERROR_BATCH_UPDATE);
		
		} catch(Exception e) {
			throw new SystemException (e);
		}
	}
	
	private PohApiErrorDAO getPohAPIErrorDAO(BayerPOPRHeadersV2APIType apiType)
	{
		PohApiErrorDAO apiError = new PohApiErrorDAO();
		apiError.setTaskID(apiType.getTaskID());
		apiError.setTaskName(apiType.getTaskName());
		apiError.setActualCosts(apiType.getActualCosts());
		apiError.setCommitmentTypeHierarchyPathID(apiType.getCommitmentTypeHierarchyPathID());
		apiError.setCommittedDate(apiType.getCommittedDate());
		apiError.setLastInvoiceDate(apiType.getLastInvoiceDate());
		apiError.setOwnerCostObjectExternalKey(apiType.getOwnerCostObjectExternalKey());
		apiError.setReceiver(apiType.getReceiver());
		apiError.setRequestor(apiType.getRequestor());
		apiError.setVendorID(apiType.getVendorID());
		apiError.setVendorName(apiType.getVendorName());
		apiError.setWorkingForecastCosts(apiType.getWorkingForecastCosts());
		return apiError;
	}

	//Get ErrorList ID for reprocessed Error
    private List<PohApiErrorDAO> getErrorListWithId(List<PohApiErrorDAO> oldErrorList, List<PohApiErrorDAO> newErrorList){
    	for (int i = 0; i < newErrorList.size(); i++)
    	{
    		long j = this.getErrorId(newErrorList.get(i).getOwnerCostObjectExternalKey(), oldErrorList);
    		newErrorList.get(i).setId(j);;
    	}
        return newErrorList;
    }
    
	//Get ErrorID for reprocessed Error
    private long getErrorId(String ownerId, List<PohApiErrorDAO> errorList){
		long id = 0;
    	for (int i = 0; i < errorList.size(); i++)
    	{

    		if(errorList.get(i).getOwnerCostObjectExternalKey().equals(ownerId))
    			id = errorList.get(i).getId();
    	}
        return id;
    }
	
	//Convert PMOrderAPIErrorDAO list to BayerPMOrderAPIType List
    private List<BayerPOPRHeadersV2APIType> getBayerPOPRHeadersV2APITypesFromError(List<PohApiErrorDAO> errorList){
		
    	BayerPOPRHeadersV2APIRequestType request = new BayerPOPRHeadersV2APIRequestType();
    	for (int i = 0; i < errorList.size(); i++)
    	{
    		PohApiErrorDAO errorDAO = errorList.get(i);
    		BayerPOPRHeadersV2APIType apiType = this.getBayerPOPRHeadersV2APITypeFromError(errorDAO);
    		request.getBayerPOPRHeadersV2API().add(apiType);
    	}
        return request.getBayerPOPRHeadersV2API();
    }
    
    
	private BayerPOPRHeadersV2APIType getBayerPOPRHeadersV2APITypeFromError(PohApiErrorDAO errorDAO)
	{
		BayerPOPRHeadersV2APIType apiType = new BayerPOPRHeadersV2APIType();
		apiType.setTaskID(errorDAO.getTaskID());
		apiType.setTaskName(errorDAO.getTaskName());
		apiType.setActualCosts(errorDAO.getActualCosts());
		apiType.setCommitmentTypeHierarchyPathID(errorDAO.getCommitmentTypeHierarchyPathID());
		apiType.setCommittedDate(errorDAO.getCommittedDate());
		apiType.setLastInvoiceDate(errorDAO.getLastInvoiceDate());
		apiType.setOwnerCostObjectExternalKey(errorDAO.getOwnerCostObjectExternalKey());
		apiType.setReceiver(errorDAO.getReceiver());
		apiType.setRequestor(errorDAO.getRequestor());
		apiType.setVendorID(errorDAO.getVendorID());
		apiType.setVendorName(errorDAO.getVendorName());
		apiType.setWorkingForecastCosts(errorDAO.getWorkingForecastCosts());
		return apiType;
	}

	private List<BayerPOPRHeadersV2APIType> readSapData(String projectHierarchyPathId, String projectId, String systemId) throws SystemException {
		Map<String, Map<String, Object>> dataRows = odataSvcMgr.readPRHeader(projectId, systemId);
		List<BayerPOPRHeadersV2APIType> apiTypes = new ArrayList<BayerPOPRHeadersV2APIType>();
		if (dataRows!=null)
		{
			Map<String, BayerPOPRHeadersV2APIType> apiList = odataSvcMgr.mapPRHeaderV2ForImport(projectHierarchyPathId, dataRows);
			apiTypes.addAll(apiList.values());			
		}
		return apiTypes;
	}
	
	private boolean isValidAPIType (BayerPOPRHeadersV2APIType apiType)
	{
		boolean isValid = true;
		//BayerPOPRHeadersV2APIType apiType = new BayerPOPRHeadersV2APIType();
		String commitmentId = apiType.getTaskID();
		//String pmoId = oDataType.getPmOrderid();
		String projectId = apiType.getOwnerCostObjectExternalKey();
		if (projectId == null ||projectId.equals("")||
				commitmentId == null||commitmentId.equals(""))
			isValid = false;
		return isValid;
	}
	
    public List<PohApiErrorDAO> getSapInputInvalid(List<BayerPOPRHeadersV2APIType> apiTypes){
		
    	List<PohApiErrorDAO> errorList = new ArrayList<PohApiErrorDAO>();
    	for (int i = 0; i < apiTypes.size(); i++)
    	{
    		BayerPOPRHeadersV2APIType apiType = apiTypes.get(i);
    		if (!isValidAPIType(apiType))
    		{
				PohApiErrorDAO errorDAO = this.getPohAPIErrorDAO(apiType);
				errorDAO.setStatus(GlobalConstants.STAGING_RECORD_STATUS_SKIPPED);
    			errorList.add(errorDAO);
    		}
    	}
        return errorList;
    }
    
    public List<BayerPOPRHeadersV2APIType> getBayerPOPRHeadersV2APITypesValid(List<BayerPOPRHeadersV2APIType> apiTypes){
		
    	BayerPOPRHeadersV2APIRequestType request = new BayerPOPRHeadersV2APIRequestType();
    	
    	for (int i = 0; i < apiTypes.size(); i++)
    	{
    		BayerPOPRHeadersV2APIType apiType = apiTypes.get(i);
    		if (isValidAPIType(apiType))
    		{
        		request.getBayerPOPRHeadersV2API().add(apiType);
    		}
    	}
        return request.getBayerPOPRHeadersV2API();
    }
    
    
}
