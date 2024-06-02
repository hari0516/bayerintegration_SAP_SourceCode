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
import com.bayer.integration.persistence.WbsApiErrorDAO;
import com.bayer.integration.properties.GlobalConstants;
import com.bayer.integration.rest.project.BayerProjectAPIType;
import com.bayer.integration.rest.wbs.BayerWBSAPIRequestType;
import com.bayer.integration.rest.wbs.BayerWBSAPIResultType;
import com.bayer.integration.rest.wbs.BayerWBSAPIType;
import com.bayer.integration.rest.wbs.WObjectFactory;
import com.bayer.integration.rest.wbs.WObjectResultType;
import com.ecosys.exception.SystemException;
import com.ecosys.util.Stopwatch;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.org.apache.xerces.internal.dom.ElementNSImpl;

/**
 * @author skdas
 *
 */
public class WBSImportMgrImpl2 extends ImportManagerBase implements
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
				if (client == null) 
					setClient(epcRestMgr.createClient(GlobalConstants.EPC_REST_USERNAME, GlobalConstants.EPC_REST_PASSWORD));

				//Read Project Data from EcoSys using project API
				Cookie session = null;
				
				//ImportManagerHelper importHelper = new ImportManagerHelper();		
				//Retrieve list of projects for SAP Integration from EcoSys
				/*ProjectExportManagerImpl projectExportMgr = new ProjectExportManagerImpl();
				
				projectExportMgr.setClient(client);
				projectExportMgr.setEpcRestMgr(epcRestMgr);
				projectExportMgr.setStgDBMgr(stgDBMgr);
				projectExportMgr.ExportData();*/
				
				prjExpMgr.ExportData();
				List<BayerProjectAPIType> projectAPITypes = prjExpMgr.getBayerProjectAPITypes();
					
				//Loop through the project list 
				for (int i = 0; i < projectAPITypes.size(); i++) 
		 		{
					BayerProjectAPIType projectAPIType = projectAPITypes.get(i);
					String projectId = projectAPIType.getID();
					String systemId = projectAPIType.getSAPSystemID();
					
					String masterProjectId = null;
					if (projectAPIType.getParentCostObjectID()!=null && projectAPIType.getParentCostObjectID()!="")
						masterProjectId = projectAPIType.getParentCostObjectID();
					
					boolean isSub = false;
					if (projectAPIType.getProjectTypeID().equals(GlobalConstants.EPC_API_PROJECT_TYPE_SUB))
							isSub = true;
					
					List<WbsApiErrorDAO> errorDAOList = new ArrayList<WbsApiErrorDAO>();
					
					//TODO remove this after getting live data
					if (!projectId.equals("A00NC-000047")){
						continue;
					}
					
					//Retrieve last run failed records and process
					logger.debug("Reading previous run failed WBS data from Error Log table for Project: " + projectId);
					List<WbsApiErrorDAO> lastTimeFailedDAOList = stgDBMgr.findFailedWbsByProject(projectId);
					/*
					if (lastTimeFailedDAOList.size()>0){
						logger.debug("Processing previous run failed WBS data retrieved from Error Log table for Project:" + projectId);
						errorDAOList = this.processProjects(this.getBayerWBSAPITypesFromError(lastTimeFailedDAOList), projectId);
						errorDAOList = this.getErrorListWithId(lastTimeFailedDAOList, errorDAOList);
						
						this.processStatusMessages(errorDAOList, false);
						logger.debug("Processing previous run failed WBS data completed for Project:" + projectId);
					}
					else{
						logger.debug("No previous run failed WBS data for processing for Project" + projectId);
					}
					*/
					
					//Read new SAP input and process
					logger.debug("Reading WBS data from SAP Input for Project: "+ projectId);
					//List<BayerWBSAPIType> inputList = readWBSData(projectId, systemId, isSub, masterProjectId);
					List<SapWBSODataType> oDataTypes = importHelper.getSapWBSTypesSampleSub();
					
					
					List<WbsApiErrorDAO> invalidRecords = new ArrayList<WbsApiErrorDAO>(); 
					//invalidRecords = this.getBayerWBSAPITypesInvalid(inputList);
					invalidRecords = this.getBayerWBSAPITypesInvalidO(oDataTypes);
			
					//Process Invalid Records
					logger.debug("Processing Invalid Records, If Any for Project: " + projectId);
					if (invalidRecords.size()>0)
						this.processStatusMessages(invalidRecords, true);
					logger.debug("Processing Invalid Records Completed for Project: "+ projectId);	

					List<BayerWBSAPIType> validRecords = new ArrayList<BayerWBSAPIType>();
					if (isSub == false)
					{
						validRecords = this.getBayerWBSAPITypesValidO(oDataTypes);
					}
					else
					{
						validRecords = this.getBayerWBSAPITypesValidOSub(oDataTypes, masterProjectId);
					}
					//List<BayerWBSAPIType> validRecords = this.getBayerWBSAPITypesValid(inputList);
					
					//HashMap<String,String> wbsMap = new HashMap<String, String>();
					//wbsMap.put("ProjectId", projectId);
					//session = null;
					//List<BayerWBSAPIType> existingList = this.getWBSAPITypes(client, session, baseUri2, wbsMap);						
					//List<BayerWBSAPIType> updatedList = this.updateBayerWBSAPITypes(inputList, existingList);
					
					errorDAOList = processProjects(validRecords, projectId);
					//Process Status Messages
					logger.debug("Processing Error Messages, If Any for Project: " + projectId);
					this.processStatusMessages(errorDAOList, true);
					logger.debug("Processing Error Messge Completed for Project: "+ projectId);	
		 		}

			} else {
				logger.info("Skipped Project Interface. Change the skip property to 'false'");				
			}
			
		}
		catch(SystemException se) {			
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

private List<WbsApiErrorDAO> processProjects( List<BayerWBSAPIType> prjRecords, String projectId) throws SystemException{
		
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
					logger.error("ERROR --> " + or.getInternalId() + "|" + wbsID + "|" + or.isSuccessFlag() + "|" + str);
					
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
					logger.error("ERROR --> " + or.getInternalId() + "|" + wbsID + "|" + or.isSuccessFlag() + "|" + str);
					
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
    		if (isValidODataType(oDataType))
    		{
    			BayerWBSAPIType apiType = this.getWBSAPIType(oDataType);
    			request.getBayerWBSAPI().add(apiType);
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
		//apiType.setCostObjectStatus(GlobalConstants.EPC_REST_DEFAULT_COSTOBJECT_STATUS);
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
		apiType.setSAPDeleteFlagID(oDataType.getDeletionFlag());
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
			if (wbsElement.equals("A00NC-000047-C"))
				hierarchyPath = "A00NC-000047.C";
			if (wbsElement.equals("A00NC-000047-C1"))
				hierarchyPath = "A00NC-000047.C.1";
			if (wbsElement.equals("A00NC-000047-C2"))
				hierarchyPath = "A00NC-000047.C.2";
			if (wbsElement.equals("A00NC-000047-E"))
				hierarchyPath = "A00NC-000047.E";
			if (wbsElement.equals("A00NC-000047-E1"))
				hierarchyPath = "A00NC-000047.E.1";
			if (wbsElement.equals("A00NC-000047"))
				hierarchyPath = "A00NC-000047";
			if (wbsElement.equals("A00GV-999990-C"))
				hierarchyPath = "A00GV-999990.C";
			if (wbsElement.equals("A00GV-999990-C1"))
				hierarchyPath = "A00GV-999990.C.1";
			if (wbsElement.equals("A00GV-999990-C2"))
				hierarchyPath = "A00GV-999990.C.2";
			if (wbsElement.equals("A00GV-999990-E"))
				hierarchyPath = "A00GV-999990.E";
			if (wbsElement.equals("A00GV-999990-E1"))
				hierarchyPath = "A00GV-999990.E.1";
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
		Map<String, Map<String, Object>> dataRows = odataSvcMgr.readWBSHierarchy(projectId, systemId);
		Map<String, BayerWBSAPIType> wbsList = odataSvcMgr.mapWBSHierarchyForImport(dataRows, isSub, masterProjectId);
		List<BayerWBSAPIType> wbsRecords = new ArrayList<BayerWBSAPIType>();
		wbsRecords.addAll(wbsList.values());
		return wbsRecords;
	}
	
	
    public List<BayerWBSAPIType> getBayerWBSAPITypesValid(List<BayerWBSAPIType> apiTypes){
		
    	BayerWBSAPIRequestType request = new BayerWBSAPIRequestType();
    	for (int i = 0; i < apiTypes.size(); i++)
    	{
			BayerWBSAPIType apiType = apiTypes.get(i);
    		if (isValidAPIType(apiType))
    			request.getBayerWBSAPI().add(apiType);
    	}
        return request.getBayerWBSAPI();
    }
    
    public List<WbsApiErrorDAO> getBayerWBSAPITypesInvalid(List<BayerWBSAPIType> apiTypes){
		
    	List<WbsApiErrorDAO> errorList = new ArrayList<WbsApiErrorDAO>();
    	for (int i = 0; i < apiTypes.size(); i++)
    	{
    		BayerWBSAPIType apiType = apiTypes.get(i);
    		if (!isValidAPIType(apiType))
    		{
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
}
