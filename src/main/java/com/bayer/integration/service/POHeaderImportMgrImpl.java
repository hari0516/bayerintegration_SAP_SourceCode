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
import com.bayer.integration.persistence.PohApiErrorDAO;
import com.bayer.integration.persistence.TacApiErrorDAO;
import com.bayer.integration.persistence.TrcApiErrorDAO;
import com.bayer.integration.properties.GlobalConstants;
import com.bayer.integration.rest.project.BayerProjectAPIType;
import com.bayer.integration.rest.actual.BayerActualsAPIType;
import com.bayer.integration.rest.log.IntegrationIssuesAPIRequestType;
import com.bayer.integration.rest.log.IntegrationIssuesAPIResultType;
import com.bayer.integration.rest.log.IntegrationIssuesAPIType;
import com.bayer.integration.rest.log.LogObjectFactory;
import com.bayer.integration.rest.pmorder.BayerPMOrderAPIResultType;
import com.bayer.integration.rest.pmorder.BayerPMOrderAPIType;
import com.bayer.integration.rest.poprh2.BayerPOPRHeadersV2APIRequestType;
import com.bayer.integration.rest.poprh2.BayerPOPRHeadersV2APIResultType;
import com.bayer.integration.rest.poprh2.BayerPOPRHeadersV2APIType;
import com.bayer.integration.rest.poprh2.ObjectFactory;
import com.bayer.integration.rest.poprh2.ObjectResultType;

import com.bayer.integration.rest.taskcategory.BayerTaskCategoryAPIRequestType;
import com.bayer.integration.rest.taskcategory.BayerTaskCategoryAPIResultType;
import com.bayer.integration.rest.taskcategory.BayerTaskCategoryAPIType;
import com.bayer.integration.rest.taskcategory.TACObjectFactory;
import com.bayer.integration.rest.taskcategory.TACObjectResultType;
import com.bayer.integration.rest.trancategory.BayerTransactionCategoryAPIRequestType;
import com.bayer.integration.rest.trancategory.BayerTransactionCategoryAPIResultType;
import com.bayer.integration.rest.trancategory.BayerTransactionCategoryAPIType;
import com.bayer.integration.rest.trancategory.TRCObjectFactory;
import com.bayer.integration.rest.trancategory.TRCObjectResultType;
import com.ecosys.exception.SystemException;
import com.ecosys.util.Stopwatch;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.org.apache.xerces.internal.dom.ElementNSImpl;

/**
 * @author pwng
 *
 */
public class POHeaderImportMgrImpl extends ImportManagerBase implements
		ImportManager {

	/* (non-Javadoc)
	 * @see com.bayer.integration.service.ImportManager#importData()
	 */
	//@Override
	final String itgInterface = "POH";
	List<IntegrationIssuesAPIType> itgIssueLog = new ArrayList<IntegrationIssuesAPIType>();

	public int importData() {
		int retCode=GlobalConstants.IMPORT_SAP_POH_SUCCESS;
		try{			
			if (!GlobalConstants.SKIP_SAP_POH_INTERFACE) {
				
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
					String systemId = projectAPIType.getSAPSystemID();
					String sapProjectId = projectAPIType.getSapProjectId();
					String projectHierarchyPathId = projectAPIType.getHierarchyPathID();
					
					String masterProjectId = null;
					if (projectAPIType.getParentCostObjectID()!=null && projectAPIType.getParentCostObjectID()!="")
						masterProjectId = projectAPIType.getParentCostObjectID();
					
					boolean isSub = false;
					if (projectAPIType.getProjectTypeID().equals(GlobalConstants.EPC_API_PROJECT_TYPE_SUB))
					{
							isSub = true;
							projectHierarchyPathId = masterProjectId;
					}	
					List<PohApiErrorDAO> errorDAOList = new ArrayList<PohApiErrorDAO>();
					List<TacApiErrorDAO> errorTACDAOList = new ArrayList<TacApiErrorDAO>();
					List<BayerPOPRHeadersV2APIType> inputList = new ArrayList<BayerPOPRHeadersV2APIType>();
					List<PohApiErrorDAO> invalidRecords = new ArrayList<PohApiErrorDAO>(); 
					List<SapPOHODataType> inputListSample = new ArrayList<SapPOHODataType>();
					List<BayerPOPRHeadersV2APIType> validRecords = new ArrayList<BayerPOPRHeadersV2APIType>();
					List<BayerTaskCategoryAPIType> tacVendors = new ArrayList<BayerTaskCategoryAPIType>();

					try
					{
						//Retrieve last run failed records and process
						logger.debug("Skip Reading previous run failed PO Headers data from ERR Log table for Project: "+ projectId);

						/*
						if(!skipError)
						{
							logger.debug("Reading previous run failed PO Headers data from ERR Log table for Project: "+ projectId);
							List<PohApiErrorDAO> lastTimeFailedDAOList = new ArrayList<PohApiErrorDAO>(); 
							//lastTimeFailedDAOList= stgDBMgr.findFailedPohByProject(projectId, GlobalConstants.EPC_POH_API_ERROR_SELECT_BY_PROJECT);

							if (lastTimeFailedDAOList.size()>0){
								logger.debug("Processing previous run failed PO Headers data from ERR Log table for Project"+ projectId);
								errorDAOList = this.processProjects(this.getBayerPOPRHeadersV2APITypesFromError(lastTimeFailedDAOList), projectId);
								errorDAOList = this.getErrorListWithId(lastTimeFailedDAOList, errorDAOList);
								
								this.processStatusMessages(errorDAOList, false);
								logger.debug("Processing previous run failed PO Headers data completed for Project: 0"+ projectId);
							}
							else{
								logger.debug("No previous run failed PO Headers data for processing for Project: "+ projectId);
							}
						}
						*/
						
						logger.debug("Reading PO Headers data from SAP Input for Project: "+ projectId);		
						if (isLive)
						{
							inputList = readSapData(projectHierarchyPathId, sapProjectId, systemId);
							logger.debug("Number of Records Read in from OData: " + inputList.size() + " for Project: " + projectId);
							invalidRecords = this.getSapInputInvalid(inputList);						
							validRecords = this.getBayerPOPRHeadersV2APITypesValid(inputList);
							//tacVendors = this.getBayerTaskCategoryAPITypes(inputList, GlobalConstants.EPC_TASK_CATEGORY_VENDOR);
						}
						else
						{
							inputListSample = importHelper.getSapPOHTypesSample();
							invalidRecords = this.getBayerPOPRHeadersV2APITypesInvalid(inputListSample);
							validRecords = this.getBayerPOPRHeadersV2APITypes(inputListSample);
							//tacVendors = this.getBayerTaskCategoryAPITypes(validRecords, GlobalConstants.EPC_TASK_CATEGORY_VENDOR);
						}
						
						//tacVendors = this.getBayerTaskCategoryAPITypes(inputList, GlobalConstants.EPC_TASK_CATEGORY_VENDOR);

						//Process Invalid Records
						logger.debug("Processing Invalid Records, If Any for Project: " + projectId);
						if (invalidRecords.size()>0 && !skipError)
							this.processStatusMessages(invalidRecords, true);
						logger.debug("Processing Invalid Records Completed for Project: "+ projectId);	
						
						//Process PO Header related Task Category - Vendors
						//logger.debug("Processing PO Headers related Task Category Vendors for Project: " + projectId);
						//errorTACDAOList = processTaskCategory(tacVendors, projectId, GlobalConstants.EPC_TASK_CATEGORY_VENDOR);	
						//Process  Actuals related Cost Elements
						//logger.debug("Processing PO Headers related Task Category Vendors for Project: " + projectId);
						

						
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
		 				logger.error("4005 -- PO Header Import Failed for Project: " + GlobalConstants.EPC_PROJECT_ID_PROCESSED, se);
		 				retCode=GlobalConstants.IMPORT_SAP_POH_FAILED;
		 				continue;
		 			}
		 		}				
			} else {
				logger.info("Skipped PO Header Import Interface. Change the skip property to 'false'");				
			}
			
		}catch(Exception e) {			
			logger.error("4005 -- PO Header Import Failed", e);			
			retCode=GlobalConstants.IMPORT_SAP_POH_FAILED;
		}
		
		if (retCode==GlobalConstants.IMPORT_SAP_POH_SUCCESS)
			logger.debug("4000 -- PO Header Import Completed Successfully");
		
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
		
		logger.debug("Importing PO Headers to EPC...");
		
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
				BayerPOPRHeadersV2APIType poHeader = subList.get(i++);
				String pohID = poHeader.getTaskID();
				
				//convert to APIErrorDAO type
				PohApiErrorDAO statusMsg = this.getPohAPIErrorDAO(poHeader);
				statusMsg.setRootCostObjectID(projectId);
				
				if(or.isSuccessFlag()) {
					logger.debug("UPDATE RESULT --> " + or.getInternalId());
					logger.debug("Record with External Key ID of : " + pohID +" "+ GlobalConstants.STAGING_RECORD_STATUS_LOADED);
					
					/*
					logger.debug("Record --> " + or.getInternalId() + "|ExternalId: " + pohID + "|" 
							 + "CommitmentTypeHierarchyPathID: " + poHeader.getCommitmentTypeHierarchyPathID() + "|"
							 + "HierarchyPathID: " + poHeader.getHierarchyPathID() + "|"
							 + "OwnerCostObjectExternalKey: " + poHeader.getOwnerCostObjectExternalKey() + "|"
							 + "OwnerCostObjectHierarchyPathID: " + poHeader.getOwnerCostObjectHierarchyPathID() + "|"
							 + "Receiver: " + poHeader.getReceiver() + "|"
							 + "Requestor: " + poHeader.getRequestor() + "|"
							 + "TaskID: " + poHeader.getTaskID() + "|"
							 + "TaskInternalID: " + poHeader.getTaskInternalID() + "|"
							 + "TaskName: " + poHeader.getTaskName() + "|"
							 + "VendorID: " + poHeader.getVendorID() + "|"
							 + "VendorName: " + poHeader.getVendorName() + "|"
							 + "ActualCosts: " + poHeader.getActualCosts() + "|"
							 + "WorkingForecastCosts: " + poHeader.getWorkingForecastCosts() + "|"
							 + "CommittedDate: " + poHeader.getCommittedDate() + "|"
							 + "LastInvoiceDate: " + poHeader.getLastInvoiceDate() + "|"
							+ or.isSuccessFlag() + "|" + "Updated");
					*/
					
					statusMsg.setStatus(GlobalConstants.STAGING_RECORD_STATUS_LOADED);

				} else {
					String str = ((ElementNSImpl)or.getResultMessage().get(0).getMessage()).getTextContent();
					logger.error("ERROR --> " + or.getInternalId() + "|ExternalId: " + pohID + "|" 
							 + "CommitmentTypeHierarchyPathID: " + poHeader.getCommitmentTypeHierarchyPathID() + "|"
							 + "HierarchyPathID: " + poHeader.getHierarchyPathID() + "|"
							 + "OwnerCostObjectExternalKey: " + poHeader.getOwnerCostObjectExternalKey() + "|"
							 + "OwnerCostObjectHierarchyPathID: " + poHeader.getOwnerCostObjectHierarchyPathID() + "|"
							 + "Receiver: " + poHeader.getReceiver() + "|"
							 + "Requestor: " + poHeader.getRequestor() + "|"
							 + "TaskID: " + poHeader.getTaskID() + "|"
							 + "TaskInternalID: " + poHeader.getTaskInternalID() + "|"
							 + "TaskName: " + poHeader.getTaskName() + "|"
							 + "VendorID: " + poHeader.getVendorID() + "|"
							 + "VendorName: " + poHeader.getVendorName() + "|"
							 + "ActualCosts: " + poHeader.getActualCosts() + "|"
							 + "WorkingForecastCosts: " + poHeader.getWorkingForecastCosts() + "|"
							 + "CommittedDate: " + poHeader.getCommittedDate() + "|"
							 + "LastInvoiceDate: " + poHeader.getLastInvoiceDate() + "|"
							+ or.isSuccessFlag() + "|" + str);	
					
					statusMsg.setStatus(GlobalConstants.STAGING_RECORD_STATUS_FAILED);
					statusMsg.setErrorMsg(str);
					itgIssueLog.add(getIntegrationIssuesAPIType("" ,str, projectId,pohID));
				
				}
				errorList.add(statusMsg);		
			}
		}
		return session;
	}
	
    
	//Convert SAPWBSODataType List to BayerWBSAPIType List
    public List<BayerPOPRHeadersV2APIType> getBayerPOPRHeadersV2APITypes(List<SapPOHODataType> oDataTypes){
		
    	BayerPOPRHeadersV2APIRequestType request = new BayerPOPRHeadersV2APIRequestType();
    	for (int i = 0; i < oDataTypes.size(); i++)
    	{
    		SapPOHODataType oDataType = oDataTypes.get(i);
    		if (isValidODataType(oDataType))
    		{
    			BayerPOPRHeadersV2APIType apiType = this.getPOPRHeadersAPIType(oDataType);
    			request.getBayerPOPRHeadersV2API().add(apiType);
    		}
    	}
        return request.getBayerPOPRHeadersV2API();
    }
    
    public List<PohApiErrorDAO> getBayerPOPRHeadersV2APITypesInvalid(List<SapPOHODataType> oDataTypes){
		
    	List<PohApiErrorDAO> errorList = new ArrayList<PohApiErrorDAO>();
    	for (int i = 0; i < oDataTypes.size(); i++)
    	{
    		SapPOHODataType oDataType = oDataTypes.get(i);
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
    
	private boolean isValidODataType (SapPOHODataType oDataType)
	{
		boolean isValid = true;
		BayerPOPRHeadersV2APIType apiType = new BayerPOPRHeadersV2APIType();
		String projectId = oDataType.getProjectDefinition();
		//String pmoId = oDataType.getPmOrderid();
		String commitmentId = oDataType.getPurchOrd();
		if (projectId == null ||projectId.equals("")||
				commitmentId == null||commitmentId.equals(""))
			isValid = false;
		return isValid;
	}
    
    /* Convert SapWBSODataType Object to BayerWBSAPIType Object
     * 
     */
	public BayerPOPRHeadersV2APIType getPOPRHeadersAPIType(SapPOHODataType oDataType)
	{
		BayerPOPRHeadersV2APIType apiType = new BayerPOPRHeadersV2APIType();
		String projectId = oDataType.getProjectDefinition();
		String pohId = oDataType.getPurchOrd();
		String wbsElement = oDataType.getWbsElement();
		String pmoId = oDataType.getPmOrderid();

		apiType.setOwnerCostObjectExternalKey(oDataType.getProjectDefinition());
		apiType.setTaskID(pohId);
		apiType.setTaskName(oDataType.getPurchOrdDesc());
		apiType.setReceiver(oDataType.getReceiver());
		apiType.setRequestor(oDataType.getRequestor());
		apiType.setVendorID(oDataType.getVendorId());
		apiType.setVendorName(oDataType.getVendorName());
		apiType.setLastInvoiceDate(oDataType.getLastInvDt());	
		//apiType.setActualCosts(oDataType.getPoActCost());
		//apiType.setWorkingForecastCosts(oDataType.getPoComCost());
		apiType.setCommitmentTypeHierarchyPathID(GlobalConstants.EPC_REST_COMMITMENT_TYPE_PO);

		return apiType;
	}
	
	private void processStatusMessages(List<PohApiErrorDAO> errorList, boolean isNew)  throws SystemException {
		try{
		if (isNew)
			stgDBMgr.insertPohBatch(errorList, GlobalConstants.EPC_POH_API_ERROR_BATCH_INSERT);
		else
			stgDBMgr.updatePohBatch(errorList, GlobalConstants.EPC_POH_API_ERROR_BATCH_UPDATE);
		
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
		Map<String, Map<String, Object>> dataRows = odataSvcMgr.readPOHeader(projectId, systemId);
		List<BayerPOPRHeadersV2APIType> apiTypes = new ArrayList<BayerPOPRHeadersV2APIType>();
		if (dataRows!=null)
		{
			Map<String, BayerPOPRHeadersV2APIType> apiList = odataSvcMgr.mapPOHeaderV2ForImport(projectHierarchyPathId, dataRows);
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
		String projectId = apiType.getOwnerCostObjectHierarchyPathID();
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
    
    
	//Start Section Processing Categories: Vendor
	//Convert BayerCommitementAPIType to BayerTaskCategoryAPIType
    public List<BayerTaskCategoryAPIType> getBayerTaskCategoryAPITypes(List<BayerPOPRHeadersV2APIType> apiTypes, String categoryType){
		
    	BayerTaskCategoryAPIRequestType request = new BayerTaskCategoryAPIRequestType();
    	Map<String, BayerTaskCategoryAPIType> tcMap = new HashMap<String, BayerTaskCategoryAPIType>();
    	for (int i = 0; i < apiTypes.size(); i++)
    	{
    		BayerPOPRHeadersV2APIType actApiType = apiTypes.get(i);
    		
    		if (isValidTACVendorDataType(actApiType))
    		{
    			BayerTaskCategoryAPIType apiType = this.getBayerTACVendorAPIType(actApiType, categoryType);
    			if (!tcMap.containsKey(apiType.getTaskCategoryID()))
    			{
        			request.getBayerTaskCategoryAPI().add(apiType);
        			tcMap.put(apiType.getTaskCategoryID(), apiType);
    			}
    		}
    	}
        return request.getBayerTaskCategoryAPI();
    }
    
	private boolean isValidTACVendorDataType (BayerPOPRHeadersV2APIType apiType)
	{
		boolean isValid = false;
		String catgId = apiType.getVendorID();
		if (catgId!=null || !catgId.equals(""))
			isValid = true;
		return isValid;
	}
	
	
	public BayerTaskCategoryAPIType getBayerTACVendorAPIType(BayerPOPRHeadersV2APIType actApiType, String categoryType)
	{
		BayerTaskCategoryAPIType apiType = new BayerTaskCategoryAPIType();
		//String projectId = oDataType.getProjectDefinition();
		apiType.setCategoryID(categoryType);
		//apiType.setHierarchyPathID(actApiType.getVendorID());
		if (actApiType.getVendorID().contains("65"))
			apiType.setTaskCategoryID("44444444444");
		else 
			apiType.setTaskCategoryID(actApiType.getVendorID());
		apiType.setTaskCategoryName(actApiType.getVendorName());
		//apiType.setActive(true);
		//apiType.setName(oDataType.getCostElement());
		return apiType;
	}
	
	private List<TacApiErrorDAO> processTaskCategory( List<BayerTaskCategoryAPIType> prjRecords, String projectId,
			String categoryType) throws SystemException{
		
		logger.debug("Importing PO Header associated Task Category " + categoryType +" of project " + projectId + " to EPC as Cost Accounts...");
		
		List<TacApiErrorDAO> retStatusMsgList = new ArrayList<TacApiErrorDAO>();
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
				List<TacApiErrorDAO> statusMsgList = new ArrayList<TacApiErrorDAO>();
				timerBatch.start();
				session = this.requestTAC(prjRecords.subList(i, end), 
						session, baseUri, projectId, categoryType, statusMsgList);
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
	
	private TacApiErrorDAO getTacAPIErrorDAO(BayerTaskCategoryAPIType apiType, String categoryType)
	{
		TacApiErrorDAO apiError = new TacApiErrorDAO();
		apiError.setCategoryID(categoryType);
		apiError.setTaskCategoryID(apiType.getTaskCategoryID());
		//apiError.setCostObjectHierarchyPathID(apiType.getCostObjectHierarchyPathID());
		apiError.setTaskCategoryName(apiType.getTaskCategoryName());
		apiError.setActive(apiType.isActive());
		//apiError.setHierarchyPathID(apiType.set);
		return apiError;
	}
	private void processStatusMessagesTAC(List<TacApiErrorDAO> errorList, boolean isNew)  throws SystemException {
		try{
			//stgDBMgr.insertWbsBatch(this.wbsAPIErrorDAOList);
		if (isNew)
			stgDBMgr.insertTacBatch(errorList, GlobalConstants.EPC_CA_API_ERROR_BATCH_INSERT);
		else
			stgDBMgr.updateTacBatch(errorList, GlobalConstants.EPC_CA_API_ERROR_BATCH_UPDATE);
		
		} catch(Exception e) {
			throw new SystemException (e);
		}
	}
	
	private Cookie requestTAC(List<BayerTaskCategoryAPIType> subList, Cookie session,
			String baseUri, String projectId, String categoryType, List<TacApiErrorDAO> errorList) throws SystemException {
		
		BayerTaskCategoryAPIRequestType request = new BayerTaskCategoryAPIRequestType();
		request.getBayerTaskCategoryAPI().addAll(subList);

		TACObjectFactory objectFactory = new TACObjectFactory();
		JAXBElement<BayerTaskCategoryAPIRequestType> requestWrapper = objectFactory.createBayerTaskCategoryAPIRequest(request);
		
		HashMap<String, String> paramMap = new HashMap<String, String>();
		paramMap.put(GlobalConstants.EPC_TASK_CATEGORY_PARAM, categoryType);
		
		ClientResponse response = epcRestMgr
					.postApplicationXmlAsApplicationXml(client, requestWrapper,
							baseUri, GlobalConstants.EPC_REST_IMPORT_SAP_TAC,
							session, paramMap);
							
		logger.debug(response);
		BayerTaskCategoryAPIResultType result = epcRestMgr.responseToObject(response, BayerTaskCategoryAPIResultType.class);
		if(session == null)
			session = epcRestMgr.getSessionCookie(response);
		
		if(!result.isSuccessFlag()){
			String errMsg="The interface failed to load any record due to data issues; please verify data.";
			if (result.getError() != null)
				errMsg=result.getError().toString();
				for(BayerTaskCategoryAPIType ca: subList)
				{
					//convert to APIErrorDAO type
					TacApiErrorDAO statusMsg = this.getTacAPIErrorDAO(ca, categoryType);
					statusMsg.setRootCostObjectID(projectId);
					statusMsg.setStatus(GlobalConstants.STAGING_RECORD_STATUS_FAILED);
					statusMsg.setErrorMsg(errMsg);
					errorList.add(statusMsg);
				}
			throw new SystemException(errMsg);
		} else {
		
			//List<StatusMessage> statusMsgList = new ArrayList<StatusMessage>();
			int i=0;
			for(TACObjectResultType or : result.getObjectResult()) {
				BayerTaskCategoryAPIType ca = subList.get(i++);
				String caID = ca.getTaskCategoryID();
				
				//convert to APIErrorDAO type
				TacApiErrorDAO statusMsg = this.getTacAPIErrorDAO(ca, categoryType);
				statusMsg.setRootCostObjectID(projectId);
				
				if(or.isSuccessFlag()) {
					logger.debug("UPDATE RESULT --> " + or.getInternalId());
					logger.debug("Record with External Key ID of : " + caID +" "+ GlobalConstants.STAGING_RECORD_STATUS_LOADED);
					statusMsg.setStatus(GlobalConstants.STAGING_RECORD_STATUS_LOADED);

				} else {
					String str = ((ElementNSImpl)or.getResultMessage().get(0).getMessage()).getTextContent();
					logger.error("ERROR --> " + or.getInternalId() + "|" + caID + "|" + or.isSuccessFlag() + "|" + str);	
				
					statusMsg.setStatus(GlobalConstants.STAGING_RECORD_STATUS_FAILED);
					statusMsg.setErrorMsg(str);				
				}
				errorList.add(statusMsg);				
			}
		}
		return session;
	}
	
	private  List<BayerPOPRHeadersV2APIType> readPOs(String projectId) throws SystemException{
		
		List<BayerPOPRHeadersV2APIType> bayerPOs = new ArrayList<BayerPOPRHeadersV2APIType>();
		try 
		{	
			//Read PM Order Data from EcoSys using PMOrder API
			Cookie session = null;
			String baseUri2 = GlobalConstants.EPC_REST_PROTOCOL + "://" + GlobalConstants.EPC_REST_SERVER + ":"
					+ GlobalConstants.EPC_REST_PORT + "/" + GlobalConstants.EPC_REST_BASEURI2;				
			
			HashMap<String, String> aMap = new HashMap<String, String>();
			aMap.put(GlobalConstants.EPC_PROJECT_PARAM, projectId);
			bayerPOs = this.getPOPRHeadersV2APITypes(session, baseUri2, aMap);
			return bayerPOs;
			
		} catch(Exception e) {
			throw new SystemException(e);
		}
	}
	
	private List<BayerPOPRHeadersV2APIType> getPOPRHeadersV2APITypes(Cookie session, String baseUri, HashMap<String, String> pmorderMap) throws SystemException {
		
		ClientResponse response = epcRestMgr
			.getAsApplicationXml(client, baseUri,
					GlobalConstants.EPC_REST_IMPORT_SAP_PMO,session,pmorderMap);

		logger.debug(response);
		BayerPOPRHeadersV2APIResultType result = epcRestMgr.responseToObject(response, BayerPOPRHeadersV2APIResultType.class);
		List<BayerPOPRHeadersV2APIType> poprAPITypes = result.getBayerPOPRHeadersV2API();
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
		return poprAPITypes;
	}
    //End Section Processing Categories: Vendor
}
