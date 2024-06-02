/**
 * 
 */
package com.bayer.integration.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Cookie;
import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.joda.time.format.ISOPeriodFormat;

import com.bayer.integration.odata.SapPMOODataType;
import com.bayer.integration.persistence.PmOrderApiErrorDAO;
import com.bayer.integration.properties.GlobalConstants;
import com.bayer.integration.rest.calcwbs.BayerCalculateWBSIDAPIResultType;
import com.bayer.integration.rest.calcwbs.CalcWObjectFactory;
import com.bayer.integration.rest.pmorder.BayerPMOrderAPIRequestType;
import com.bayer.integration.rest.pmorder.BayerPMOrderAPIResultType;
import com.bayer.integration.rest.pmorder.BayerPMOrderAPIType;
import com.bayer.integration.rest.pmorder.ObjectFactory;
import com.bayer.integration.rest.pmorder.ObjectResultType;
import com.bayer.integration.rest.pmordertran.BayerPMOrderTranAPIRequestType;
import com.bayer.integration.rest.pmordertran.BayerPMOrderTranAPIResultType;
import com.bayer.integration.rest.pmordertran.BayerPMOrderTranAPIType;
import com.bayer.integration.rest.pmordertran.PMOTObjectFactory;
import com.bayer.integration.rest.pmordertran.PMOTObjectResultType;
import com.bayer.integration.rest.project.BayerProjectAPIType;
import com.ecosys.exception.SystemException;
import com.ecosys.util.Stopwatch;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.org.apache.xerces.internal.dom.ElementNSImpl;

/**
 * @author skdas
 *
 */
public class PMOrderImportMgrImplV1 extends ImportManagerBase implements
		ImportManager {

	/* (non-Javadoc)
	 * @see com.bayer.integration.service.ImportManager#importData()
	 */
	//@Override

	
	public int importData() {
		int retCode=GlobalConstants.IMPORT_SAP_PMO_SUCCESS;
		try{			
			if (!GlobalConstants.SKIP_SAP_PMO_INTERFACE) {
				
				//Create Web Service Client
				if (client == null) 
					setClient(epcRestMgr.createClient(GlobalConstants.EPC_REST_USERNAME, GlobalConstants.EPC_REST_PASSWORD));

				boolean skipError = GlobalConstants.SKIP_LOG;
				//change the value to false if using sample data
				boolean isLive = GlobalConstants.IS_LIVE_SAP;
				//Read Project Data from EcoSys using project API
				Cookie session = null;
				
				//ImportManagerHelper importHelper = new ImportManagerHelper();
				
				prjExpMgr.ExportData();
				List<BayerProjectAPIType> projectAPITypes = prjExpMgr.getBayerProjectAPITypes();
		    	DatatypeFactory dFactory = DatatypeFactory.newInstance();
				XMLGregorianCalendar currentDate = dFactory.newXMLGregorianCalendar(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
							
				//Loop through the project list
				for (int i = 0; i < projectAPITypes.size(); i++) 
		 		{
					BayerProjectAPIType projectAPIType = projectAPITypes.get(i);
					String projectId = projectAPIType.getID();
					GlobalConstants.EPC_PROJECT_ID_PROCESSED = projectId;
					String sapProjectId = projectAPIType.getSapProjectId();
					String systemId = projectAPIType.getSAPSystemID();
					String masterProjectId = null;
					if (projectAPIType.getParentCostObjectID()!=null && projectAPIType.getParentCostObjectID()!="")
						masterProjectId = projectAPIType.getParentCostObjectID();
					
					boolean isSub = false;
					if (projectAPIType.getProjectTypeID().equals(GlobalConstants.EPC_API_PROJECT_TYPE_SUB))
							isSub = true;
					
					List<PmOrderApiErrorDAO> errorDAOList = new ArrayList<PmOrderApiErrorDAO>();
					
					List<PmOrderApiErrorDAO> lastTimeFailedDAOList = new ArrayList<PmOrderApiErrorDAO>();
					List<BayerPMOrderAPIType> currentList = new ArrayList<BayerPMOrderAPIType>();
					List<BayerPMOrderAPIType> inputList = new ArrayList<BayerPMOrderAPIType>();
					List<PmOrderApiErrorDAO> invalidRecords = new ArrayList<PmOrderApiErrorDAO>();
					List<SapPMOODataType> inputListSample = new ArrayList<SapPMOODataType>();
					List<BayerPMOrderAPIType> validRecords = new ArrayList<BayerPMOrderAPIType>();
					List<BayerPMOrderAPIType> movedRecords = new ArrayList<BayerPMOrderAPIType>();
					List<BayerPMOrderTranAPIType> validTranRecords = new ArrayList<BayerPMOrderTranAPIType>();
										
					currentList = this.readPMOrders(projectId);
					
					// Comment out for Odata Test
					//Retrieve last run failed records and process
					/*
					logger.debug("Reading previous run failed PM Order data from Error Log table for Project: "+ projectId);
					if (!skipError)
					{
						//lastTimeFailedDAOList = stgDBMgr.findFailedPmoByProject(projectId);
						if (lastTimeFailedDAOList.size()>0){
							logger.debug("Processing previous run failed PM Order data from Error Log table for Project"+ projectId);
							errorDAOList = this.processProjects(this.getBayerPMOrderAPITypesFromError(lastTimeFailedDAOList), projectId);
							errorDAOList = this.getErrorListWithId(lastTimeFailedDAOList, errorDAOList);
							
							this.processStatusMessages(errorDAOList, false);
							logger.debug("Processing previous run failed PM Order data completed for Project: 0"+ projectId);
							
							logger.debug("Marking processed previous run failed PM Order data from Error Log table as retired for Project"+ projectId);
							this.processRetired(lastTimeFailedDAOList);
							logger.debug("Marking processed previous run failed PM Order data from Error Log table as retired completed for Project"+ projectId);
						}
						else{
							logger.debug("No previous run failed PM Order data for processing for Project: "+ projectId);
						}
					}
					*/
					try
					{
						//OData Processing Section
						//read PMOrder data from SAP and convert them to EcoSys API payload
						logger.debug("Reading PMOrder data from SAP Input for Project: "+ projectId);
						if (isLive)
						{
							inputList = readSapData(sapProjectId, systemId); 
							invalidRecords = this.getSapInputInvalid(inputList);
							validRecords = this.getSapInputValid(inputList, currentDate);					
						}
						else
						{
							//ImportHelper Test Section
							inputListSample = importHelper.getSapPMOTypesSample();
							invalidRecords = this.getBayerPMOrderAPITypesInvalid(inputListSample);
							validRecords = this.getBayerPMOrderAPITypesValid(inputListSample);
						}
						validTranRecords = this.getPMOTranValid(validRecords, currentDate);
					
						//Process Invalid Records
						logger.debug("Processing Invalid Records, If Any for Project: " + projectId);
						if (invalidRecords.size()>0 && !skipError)
							this.processStatusMessages(invalidRecords, true);
						logger.debug("Processing Invalid Records Completed for Project: "+ projectId);	
						

						//Process Moved Records
						movedRecords = this.getMovedPMOrders(currentList, validRecords, currentDate);
						
						if (validRecords.size()>0 || movedRecords.size()>0)
						{
							//Post Process for the Project
							logger.debug("Trigger Action Batch to Pre Process PM Order for Project: "+ projectId);
							this.preProcess(projectId);
							logger.debug("Trigger Action Batch to Pre Process PM Order completed for Project: "+ projectId);
							
							if (movedRecords.size()>0)
							{						
								errorDAOList = processProjects(movedRecords, projectId);
								//Process Status Messages
								if (!skipError)
								{
									logger.debug("Processing Error Messages, If Any for Project: " + projectId);
									//this.processStatusMessages(errorDAOList, true);
									logger.debug("Processing Error Messge Completed for Project: "+ projectId);
								}
							}
							
							if (validRecords.size()>0)
							{
							
								logger.debug("Processing Valid PM Order Records, If Any for Project: " + projectId);
								errorDAOList = processProjects(validRecords, projectId);
								logger.debug("Processing Valid PM Records Completed for Project: "+ projectId);	
								//Process Status Messages
								if (!skipError)
								{
									logger.debug("Processing Error Messages, If Any for Project: " + projectId);
									//this.processStatusMessages(errorDAOList, true);
									logger.debug("Processing Error Messge Completed for Project: "+ projectId);
								}
								
								errorDAOList = processPMOTran(validTranRecords, projectId);
								
								//Process Status Messages
								if (!skipError)
								{
									logger.debug("Processing Error Messages, If Any for Project: " + projectId);
									//this.processStatusMessages(errorDAOList, true);
									logger.debug("Processing Error Messge Completed for Project: "+ projectId);
								}
							}
							
							//Post Process for the Project
							logger.debug("Trigger Action Batch to Post Process PM Order for Project: "+ projectId);
							this.postProcess(projectId);
							logger.debug("Trigger Action Batch to Post Process PM Order completed for Project: "+ projectId);
						}

					}
		 			catch(SystemException se) {
		 				logger.error("2005 � PMOrder Import Failed for Project: " + GlobalConstants.EPC_PROJECT_ID_PROCESSED, se);
		 				retCode=GlobalConstants.IMPORT_SAP_PMO_FAILED;
		 				continue;
		 			}
		 		}
				
			} else {
				logger.info("Skipped Project Interface. Change the skip property to 'false'");				
			}		
		}
		catch(SystemException se) {			
			logger.error("2005 � PMOrder Import Failed", se);			
			retCode=GlobalConstants.IMPORT_SAP_PMO_FAILED;
		}catch(Exception e) {			
			logger.error("2005 � PMOrder Import Failed", e);			
			retCode=GlobalConstants.IMPORT_SAP_PMO_FAILED;
		}
		
		if (retCode==GlobalConstants.IMPORT_SAP_PMO_SUCCESS)
			logger.debug("2000 � PMOrder Import Completed Successfully");
		
		return retCode;
	}
	
private List<BayerPMOrderAPIType> readProjectData() {
		return null;
	}

private List<PmOrderApiErrorDAO> processProjects( List<BayerPMOrderAPIType> prjRecords, String projectId) throws SystemException{
		
		logger.debug("Importing PM Orders to EPC...");
		
		List<PmOrderApiErrorDAO> retStatusMsgList = new ArrayList<PmOrderApiErrorDAO>();
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
				List<PmOrderApiErrorDAO> statusMsgList = new ArrayList<PmOrderApiErrorDAO>();
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



	private Cookie request(List<BayerPMOrderAPIType> subList,
			Cookie session, String baseUri, String projectId,  List<PmOrderApiErrorDAO> errorList) throws SystemException {
		
		BayerPMOrderAPIRequestType request = new BayerPMOrderAPIRequestType();
		request.getBayerPMOrderAPI().addAll(subList);

		ObjectFactory objectFactory = new ObjectFactory();
		JAXBElement<BayerPMOrderAPIRequestType> requestWrapper = objectFactory.createBayerPMOrderAPIRequest(request);
		HashMap<String, String> prjMap = new HashMap<String, String>();
		prjMap.put(GlobalConstants.EPC_PROJECT_PARAM, projectId);
		ClientResponse response = epcRestMgr
					.postApplicationXmlAsApplicationXml(client, requestWrapper,
							baseUri, GlobalConstants.EPC_REST_IMPORT_SAP_PMO,
							session, prjMap);
							
		logger.debug(response);
		BayerPMOrderAPIResultType result = epcRestMgr.responseToObject(response, BayerPMOrderAPIResultType.class);
		if(session == null)
			session = epcRestMgr.getSessionCookie(response);
		
		if(!result.isSuccessFlag()){
			String errMsg="The interface failed to load any record due to data issues; please verify data.";
			if (result.getError() != null)
				errMsg=result.getError().toString();
				for(BayerPMOrderAPIType pmorder: subList)
				{
					//convert to APIErrorDAO type
					PmOrderApiErrorDAO statusMsg = this.getPMOrderAPIErrorDAO(pmorder);
					statusMsg.setRootCostObjectID(projectId);
					statusMsg.setStatus(GlobalConstants.STAGING_RECORD_STATUS_FAILED);
					statusMsg.setErrorMsg(errMsg);
					errorList.add(statusMsg);
				}
			throw new SystemException(errMsg);
		} else {
		
			//List<PMOrderAPIErrorDAO> statusMsgList = new ArrayList<PMOrderAPIErrorDAO>();
			int i=0;
			for(ObjectResultType or : result.getObjectResult()) {
				BayerPMOrderAPIType pmorder = subList.get(i++);
				String pmorderID = pmorder.getCostObjectID();
				
				//convert to APIErrorDAO type
				PmOrderApiErrorDAO statusMsg = this.getPMOrderAPIErrorDAO(pmorder);
				statusMsg.setRootCostObjectID(projectId);
				
				if(or.isSuccessFlag()) {
					logger.debug("UPDATE RESULT --> " + or.getInternalId());
					logger.debug("Record with External Key ID of : " + pmorderID +" "+ GlobalConstants.STAGING_RECORD_STATUS_LOADED);
					
					statusMsg.setStatus(GlobalConstants.STAGING_RECORD_STATUS_LOADED);
				} else {
					String str = ((ElementNSImpl)or.getResultMessage().get(0).getMessage()).getTextContent();
					logger.error("ERROR --> " + or.getInternalId() + "|" + pmorderID + "|" + or.isSuccessFlag() + "|" + str);	
					
					statusMsg.setStatus(GlobalConstants.STAGING_RECORD_STATUS_FAILED);
					statusMsg.setErrorMsg(str);
				}
				//statusMsgList.add(statusMsg);
				errorList.add(statusMsg);			
			}
		}
		return session;
	}
	
    
	//Convert SAPWBSODataType List to BayerWBSAPIType List
    public List<BayerPMOrderAPIType> getBayerPMOrderAPITypesValid(List<SapPMOODataType> oDataTypes){
		
    	BayerPMOrderAPIRequestType request = new BayerPMOrderAPIRequestType();
    	for (int i = 0; i < oDataTypes.size(); i++)
    	{
    		SapPMOODataType oDataType = oDataTypes.get(i);
    		if (isValidODataType(oDataType))
    		{
        		BayerPMOrderAPIType apiType = this.getPMOrderAPIType(oDataType);
        		request.getBayerPMOrderAPI().add(apiType);
    		}
    	}
        return request.getBayerPMOrderAPI();
    }

	
    public List<PmOrderApiErrorDAO> getBayerPMOrderAPITypesInvalid(List<SapPMOODataType> oDataTypes){
		
    	List<PmOrderApiErrorDAO> errorList = new ArrayList<PmOrderApiErrorDAO>();
    	for (int i = 0; i < oDataTypes.size(); i++)
    	{
    		SapPMOODataType oDataType = oDataTypes.get(i);
    		if (!isValidODataType(oDataType))
    		{
    			BayerPMOrderAPIType apiType = this.getPMOrderAPIType(oDataType);
				PmOrderApiErrorDAO errorDAO = this.getPMOrderAPIErrorDAO(apiType);
				errorDAO.setStatus(GlobalConstants.STAGING_RECORD_STATUS_FAILED);
    			errorList.add(errorDAO);
    		}
    	}
        return errorList;
    }
    
	private boolean isValidODataType (SapPMOODataType oDataType)
	{
		boolean isValid = true;
		BayerPMOrderAPIType apiType = new BayerPMOrderAPIType();
		String projectId = oDataType.getProjectDefinition();
		String pmoId = oDataType.getPmOrderid();
		String parentId = oDataType.getWbsElement();
		if (projectId == null ||projectId.equals("")||
				pmoId == null || pmoId.equals("")||
				parentId == null||parentId.equals(""))
			isValid = false;
		return isValid;
	}
    /* Convert SapWBSODataType Object to BayerWBSAPIType Object
     * 
     */
	public BayerPMOrderAPIType getPMOrderAPIType(SapPMOODataType oDataType)
	{
		BayerPMOrderAPIType apiType = new BayerPMOrderAPIType();
		String projectId = oDataType.getProjectDefinition();
		String pmoId = oDataType.getPmOrderid();
		//String hierarchyPathId = this.getParentCostObjectHierarchyPathId(oDataType);
		//apiType.setParentCostObjectHierarchyPathID(hierarchyPathId);
		//hierarchyPathId = hierarchyPathId + GlobalConstants.EPC_REST_HIERARCHY_PATH_SEPERATOR + pmoId;
		apiType.setParentCostObjectExternalKey(oDataType.getWbsElement());
		apiType.setCostObjectID(pmoId);
		//apiType.setCostObjectTypeInternalID(GlobalConstants.EPC_REST_COSTOBJECTTYPE_PMO_INTERNAL_ID);

		apiType.setCostObjectName(oDataType.getPmDescription());
		//apiType.setExternalKey(oDataType.getWbsElement());
		//apiType.setCostObjectStatus(GlobalConstants.EPC_REST_DEFAULT_COSTOBJECT_STATUS);
	
		apiType.setPMActualCost(oDataType.getPmActCost());
		apiType.setPMEstimatedCost(oDataType.getPmEstCost());
		apiType.setExternalKey(pmoId);
		apiType.setPmStatus(oDataType.getPmStatus());

		return apiType;
	}

	private void processStatusMessages(List<PmOrderApiErrorDAO> errorList, boolean isNew)  throws SystemException {
		try{
			//stgDBMgr.insertWbsBatch(this.wbsAPIErrorDAOList);
		if (isNew)
			stgDBMgr.insertPmoBatch(errorList);
		else
			stgDBMgr.updatePmoBatch(errorList);
		
		} catch(Exception e) {
			throw new SystemException (e);
		}
	}
	
	private void processRetired(List<PmOrderApiErrorDAO> errorList)  throws SystemException {
		try{
			stgDBMgr.updatePmoBatchRetired(errorList);
		
		} catch(Exception e) {
			throw new SystemException (e);
		}
	}
	
	public String getParentCostObjectHierarchyPathId(SapPMOODataType oDataType)
	{
		String heirarchyPathId = new String();
		String wbsElement = oDataType.getWbsElement();
		String projectId = oDataType.getProjectDefinition();
		//String pmOrderId = oDataType.getPmOrderid();
		heirarchyPathId = this.buildHierarchyPath(wbsElement, projectId);
		return heirarchyPathId;
	}
	
	public String buildHierarchyPath(String wbsElement, String projectId)
	{
		String hierarchyPath = new String();
		if (wbsElement.equals("A00GV-999990-C1"))
			hierarchyPath = "A00GV-999990.C.1";

		return hierarchyPath;
	}
	
	private PmOrderApiErrorDAO getPMOrderAPIErrorDAO(BayerPMOrderAPIType apiType)
	{
		PmOrderApiErrorDAO apiError = new PmOrderApiErrorDAO();
		apiError.setCostObjectID(apiType.getCostObjectID());
		apiError.setCostObjectName(apiType.getCostObjectName());
		apiError.setCostObjectStatus(apiType.getCostObjectStatus());
		apiError.setCostObjectTypeName(apiType.getCostObjectTypeName());
		apiError.setExternalKey(apiType.getExternalKey());
		apiError.setPMEstimatedCost(apiType.getPMEstimatedCost());
		apiError.setPMActualCost(apiType.getPMActualCost());
		//apiError.setHierarchyPathID(apiType.getHierarchyPathID());
		apiError.setParentCostObjectExternalKey(apiType.getParentCostObjectExternalKey());
		apiError.setPmStatus(apiType.getPmStatus());
		return apiError;
	}
	
	private PmOrderApiErrorDAO getPMOrderTranAPIErrorDAO(BayerPMOrderTranAPIType apiType)
	{
		PmOrderApiErrorDAO apiError = new PmOrderApiErrorDAO();
		apiError.setCostObjectID(apiType.getCostObjectID());
		apiError.setCostObjectName(apiType.getCostObjectName());
		//apiError.setCostObjectStatus(apiType.getCostObjectStatus());
		//apiError.setCostObjectTypeName(apiType.getCostObjectTypeName());
		apiError.setExternalKey(apiType.getExternalKey());
		apiError.setPMEstimatedCost(apiType.getCostTransactionCurrency());
		apiError.setPMActualCost(apiType.getAlternateCostTransactionCurrency());
		//apiError.setHierarchyPathID(apiType.getHierarchyPathID());
		//apiError.setParentCostObjectExternalKey(apiType.getParentCostObjectExternalKey());
		apiError.setPmStatus(apiType.getSAPStatus());
		return apiError;
	}
	
	//Get ErrorList ID for reprocessed Error
    private List<PmOrderApiErrorDAO> getErrorListWithId(List<PmOrderApiErrorDAO> oldErrorList, List<PmOrderApiErrorDAO> newErrorList){
    	for (int i = 0; i < newErrorList.size(); i++)
    	{
    		long j = this.getErrorId(newErrorList.get(i), oldErrorList);
    		newErrorList.get(i).setId(j);;
    	}
        return newErrorList;
    }
    
	//Get ErrorID for reprocessed Error
    private long getErrorId(PmOrderApiErrorDAO newError, List<PmOrderApiErrorDAO> errorList){
		long id = 0;
    	for (int i = 0; i < errorList.size(); i++)
    	{

    		if(errorList.get(i).getParentCostObjectExternalKey().equals(newError.getParentCostObjectExternalKey())
    				&& errorList.get(i).getCostObjectID().equals(newError.getCostObjectID()))
    			id = errorList.get(i).getId();
    	}
        return id;
    }
    
	//Convert PMOrderAPIErrorDAO list to BayerPMOrderAPIType List
    private List<BayerPMOrderAPIType> getBayerPMOrderAPITypesFromError(List<PmOrderApiErrorDAO> errorList){
		
    	BayerPMOrderAPIRequestType request = new BayerPMOrderAPIRequestType();
    	for (int i = 0; i < errorList.size(); i++)
    	{
    		PmOrderApiErrorDAO errorDAO = errorList.get(i);
    		BayerPMOrderAPIType apiType = this.getBayerPMOrderAPITypeFromError(errorDAO);
    		request.getBayerPMOrderAPI().add(apiType);
    	}
        return request.getBayerPMOrderAPI();
    }
    
    
	private BayerPMOrderAPIType getBayerPMOrderAPITypeFromError(PmOrderApiErrorDAO errorDAO)
	{
		BayerPMOrderAPIType apiType = new BayerPMOrderAPIType();
		apiType.setCostObjectID(errorDAO.getCostObjectID());
		apiType.setCostObjectName(errorDAO.getCostObjectName());
		apiType.setCostObjectStatus(errorDAO.getCostObjectStatus());
		apiType.setCostObjectTypeName(errorDAO.getCostObjectTypeName());
		apiType.setExternalKey(errorDAO.getExternalKey());
		//apiType.setHierarchyPathID(errorDAO.getHierarchyPathID());
		apiType.setParentCostObjectExternalKey(errorDAO.getParentCostObjectExternalKey());
		apiType.setPMActualCost(errorDAO.getPMActualCost());
		apiType.setPMEstimatedCost(errorDAO.getPMEstimatedCost());
		apiType.setPmStatus(errorDAO.getPmStatus());
		return apiType;
	}
	
	private  List<BayerPMOrderAPIType> readPMOrders(String projectId) throws SystemException{
	
		List<BayerPMOrderAPIType> bayerPMOrders = new ArrayList<BayerPMOrderAPIType>();
		try 
		{	
			//Read PM Order Data from EcoSys using PMOrder API
			Cookie session = null;
			String baseUri2 = GlobalConstants.EPC_REST_PROTOCOL + "://" + GlobalConstants.EPC_REST_SERVER + ":"
					+ GlobalConstants.EPC_REST_PORT + "/" + GlobalConstants.EPC_REST_BASEURI2;				
			
			HashMap<String, String> aMap = new HashMap<String, String>();
			aMap.put(GlobalConstants.EPC_PROJECT_PARAM, projectId);
			bayerPMOrders = this.getPMOrderAPITypes(session, baseUri2, aMap);
			return bayerPMOrders;
			
		} catch(Exception e) {
			throw new SystemException(e);
		}
	}
	
	private List<BayerPMOrderAPIType> getPMOrderAPITypes(Cookie session, String baseUri, HashMap<String, String> pmorderMap) throws SystemException {
		
		ClientResponse response = epcRestMgr
			.getAsApplicationXml(client, baseUri,
					GlobalConstants.EPC_REST_IMPORT_SAP_PMO,session,pmorderMap);

		logger.debug(response);
		BayerPMOrderAPIResultType result = epcRestMgr.responseToObject(response, BayerPMOrderAPIResultType.class);
		List<BayerPMOrderAPIType> pmorderAPITypes = result.getBayerPMOrderAPI();
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
		return pmorderAPITypes;
	}
	
	private  List<BayerPMOrderAPIType> getMovedPMOrders(List<BayerPMOrderAPIType> currentList, List<BayerPMOrderAPIType> newList, XMLGregorianCalendar currentDate){
		
		List<BayerPMOrderAPIType> movedPMOrders = new ArrayList<BayerPMOrderAPIType>();
		for(BayerPMOrderAPIType pmorder: currentList)
		{
			if (isMoved(pmorder, newList)==true)
			{
				pmorder.setCOParentChangedID(GlobalConstants.EPC_API_ERROR_FLAG_Y);
				pmorder.setParentCOChangeDate(currentDate);
				pmorder.setExternalKey(pmorder.getExternalKey() + GlobalConstants.EPC_CO_Changed);
				//pmorder.setParentCostObjectExternalKey("A00NC-003247-CXENGFEL3EX");
				movedPMOrders.add(pmorder);
			}
			/*
			if (pmorder.getExternalKey().equalsIgnoreCase("12075012338555"))
			{
				pmorder.setCOParentChangedID(GlobalConstants.EPC_API_ERROR_FLAG_Y);
				pmorder.setParentCOChangeDate(currentDate);
				pmorder.setExternalKey(pmorder.getExternalKey() + GlobalConstants.EPC_CO_Changed);
				//pmorder.setCostObjectID((pmorder.getCostObjectID() + GlobalConstants.EPC_CO_Changed));
				pmorder.setParentCostObjectExternalKey("A00NC-003247-C2H11");
				movedPMOrders.add(pmorder);
			}*/
		}
		return movedPMOrders;
	}
	
	private boolean isMoved (BayerPMOrderAPIType apiType,List<BayerPMOrderAPIType> newList )
	{
		boolean isMoved = false;
		for(BayerPMOrderAPIType pmorder: newList)
		{
			if (pmorder.getExternalKey().equalsIgnoreCase(apiType.getExternalKey())
					&& !pmorder.getParentCostObjectExternalKey().equalsIgnoreCase(apiType.getParentCostObjectExternalKey()))
				isMoved = true;
		}
		return isMoved;
	}
	
	private List<BayerPMOrderAPIType> readSapData(String projectId, String systemId) throws SystemException {
		Map<String, Map<String, Object>> dataRows = odataSvcMgr.readPMOrders(projectId, systemId);
		List<BayerPMOrderAPIType> apiTypes = new ArrayList<BayerPMOrderAPIType>();
		if (dataRows!=null)
		{
			Map<String, BayerPMOrderAPIType> apiList = odataSvcMgr.mapPMOrderForImport(dataRows);
			apiTypes.addAll(apiList.values());
		}
		return apiTypes;
	}
	
	private boolean isValidAPIType (BayerPMOrderAPIType apiType)
	{
		boolean isValid = true;
		//String projectId = oDataType.getProjectDefinition();
		String pmoId = apiType.getCostObjectID();
		String parentId = apiType.getParentCostObjectExternalKey();
		if (	pmoId == null || pmoId.equals("")||
				parentId == null||parentId.equals(""))
			isValid = false;
		return isValid;
	}
	
    
    public List<PmOrderApiErrorDAO> getSapInputInvalid(List<BayerPMOrderAPIType> apiTypes){
		
    	List<PmOrderApiErrorDAO> errorList = new ArrayList<PmOrderApiErrorDAO>();
    	for (int i = 0; i < apiTypes.size(); i++)
    	{
    		BayerPMOrderAPIType apiType = apiTypes.get(i);
    		if (!isValidAPIType(apiType))
    		{
				PmOrderApiErrorDAO errorDAO = this.getPMOrderAPIErrorDAO(apiType);
				errorDAO.setStatus(GlobalConstants.STAGING_RECORD_STATUS_FAILED);
    			errorList.add(errorDAO);
    		}
    	}
        return errorList;
    }
    public List<BayerPMOrderAPIType> getSapInputValid(List<BayerPMOrderAPIType> apiTypes, XMLGregorianCalendar currentDate){
		
    	BayerPMOrderAPIRequestType request = new BayerPMOrderAPIRequestType();
    	for (int i = 0; i < apiTypes.size(); i++)
    	{
    		BayerPMOrderAPIType apiType = apiTypes.get(i);
    		if (isValidAPIType(apiType))
    		{
        		apiType.setPMOrderStgDate(currentDate);
    			request.getBayerPMOrderAPI().add(apiType);
    		}
    	}
        return request.getBayerPMOrderAPI();
    }
    public List<BayerPMOrderTranAPIType> getPMOTranValid(List<BayerPMOrderAPIType> apiTypes, XMLGregorianCalendar currentDate){
		
    	BayerPMOrderTranAPIRequestType request = new BayerPMOrderTranAPIRequestType();
    	for (int i = 0; i < apiTypes.size(); i++)
    	{
    		BayerPMOrderAPIType apiType = apiTypes.get(i);
    		BayerPMOrderTranAPIType apiTranType = this.getPMOTranValid(apiType, currentDate);
        	request.getBayerPMOrderTranAPI().add(apiTranType);
    	}
        return request.getBayerPMOrderTranAPI();
    }
    
    public BayerPMOrderTranAPIType getPMOTranValid(BayerPMOrderAPIType apiType, XMLGregorianCalendar currentDate){
		
    	BayerPMOrderTranAPIType apiTranType = new BayerPMOrderTranAPIType();
    	apiTranType.setCostObjectExternalKey(apiType.getExternalKey());
    	apiTranType.setCostTransactionCurrency(apiType.getPMEstimatedCost());
    	apiTranType.setAlternateCostTransactionCurrency(apiType.getPMActualCost());
    	apiTranType.setSAPStatus(apiType.getPmStatus());
    	apiTranType.setSAPWBSElement(apiType.getExternalKey());
    	apiTranType.setTransactionDate(currentDate);
    	apiTranType.setCurrentTranFlagID(GlobalConstants.EPC_API_ERROR_FLAG_Y);
    	String exKey = apiType.getExternalKey()+ "-"+ currentDate.toString();
    	apiTranType.setExternalKey(exKey);    	
    	if (currentDate.getDay()==1)
    		apiTranType.setPeriodEndFlagID(GlobalConstants.EPC_API_ERROR_FLAG_Y);
    	else
    		apiTranType.setPeriodEndFlagID(GlobalConstants.EPC_API_ERROR_FLAG_N);
    	
    	apiTranType.setVersionID(GlobalConstants.EPC_PMOTran_Version);
    	
        return apiTranType;
    }
    
    private List<PmOrderApiErrorDAO> processPMOTran( List<BayerPMOrderTranAPIType> prjRecords, String projectId) throws SystemException{
		
		logger.debug("Importing PM Orders to EPC...");
		
		List<PmOrderApiErrorDAO> retStatusMsgList = new ArrayList<PmOrderApiErrorDAO>();
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
				List<PmOrderApiErrorDAO> statusMsgList = new ArrayList<PmOrderApiErrorDAO>();
				timerBatch.start();
				session = this.requestTran(prjRecords.subList(i, end), session, baseUri, projectId, statusMsgList);
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
    
	private Cookie requestTran(List<BayerPMOrderTranAPIType> subList,
			Cookie session, String baseUri, String projectId,  List<PmOrderApiErrorDAO> errorList) throws SystemException {
		
		BayerPMOrderTranAPIRequestType request = new BayerPMOrderTranAPIRequestType();
		request.getBayerPMOrderTranAPI().addAll(subList);

		PMOTObjectFactory objectFactory = new PMOTObjectFactory();
		JAXBElement<BayerPMOrderTranAPIRequestType> requestWrapper = objectFactory.createBayerPMOrderTranAPIRequest(request);
		HashMap<String, String> prjMap = new HashMap<String, String>();
		prjMap.put(GlobalConstants.EPC_PROJECT_PARAM, projectId);
		ClientResponse response = epcRestMgr
					.postApplicationXmlAsApplicationXml(client, requestWrapper,
							baseUri, GlobalConstants.EPC_REST_IMPORT_SAP_PMO_TRAN,
							session, prjMap);
							
		logger.debug(response);
		BayerPMOrderTranAPIResultType result = epcRestMgr.responseToObject(response, BayerPMOrderTranAPIResultType.class);
		if(session == null)
			session = epcRestMgr.getSessionCookie(response);
		
		if(!result.isSuccessFlag()){
			String errMsg="The interface failed to load any record due to data issues; please verify data.";
			if (result.getError() != null)
				errMsg=result.getError().toString();
				for(BayerPMOrderTranAPIType pmorder: subList)
				{
					//convert to APIErrorDAO type
					PmOrderApiErrorDAO statusMsg = this.getPMOrderTranAPIErrorDAO(pmorder);
					statusMsg.setRootCostObjectID(projectId);
					statusMsg.setStatus(GlobalConstants.STAGING_RECORD_STATUS_FAILED);
					statusMsg.setErrorMsg(errMsg);
					errorList.add(statusMsg);
				}
			throw new SystemException(errMsg);
		} else {
		
			//List<PMOrderAPIErrorDAO> statusMsgList = new ArrayList<PMOrderAPIErrorDAO>();
			int i=0;
			for(PMOTObjectResultType or : result.getObjectResult()) {
				BayerPMOrderTranAPIType pmorder = subList.get(i++);
				String pmorderID = pmorder.getCostObjectID();
				
				//convert to APIErrorDAO type
				PmOrderApiErrorDAO statusMsg = this.getPMOrderTranAPIErrorDAO(pmorder);
				statusMsg.setRootCostObjectID(projectId);
				pmorderID = statusMsg.getExternalKey();
				
				if(or.isSuccessFlag()) {
					logger.debug("UPDATE RESULT --> " + or.getInternalId());
					logger.debug("Record with External Key ID of : " + pmorderID +" "+ GlobalConstants.STAGING_RECORD_STATUS_LOADED);
					
					statusMsg.setStatus(GlobalConstants.STAGING_RECORD_STATUS_LOADED);
				} else {
					String str = ((ElementNSImpl)or.getResultMessage().get(0).getMessage()).getTextContent();
					logger.error("ERROR --> " + or.getInternalId() + "|" + pmorderID + "|" + or.isSuccessFlag() + "|" + str);	
					
					statusMsg.setStatus(GlobalConstants.STAGING_RECORD_STATUS_FAILED);
					statusMsg.setErrorMsg(str);
				}
				//statusMsgList.add(statusMsg);
				errorList.add(statusMsg);			
			}
		}
		return session;
	}
	
    private List<String> postProcess( String projectId) throws SystemException{
		
		logger.debug("Trigger Action to Post Process PM Order for Project: " + projectId);
		
		List<String> retStatusMsgList = new ArrayList<String>();
		try {
			
			
			//Prepare for the REST call
			String baseUri = GlobalConstants.EPC_REST_PROTOCOL + "://" + GlobalConstants.EPC_REST_SERVER + ":" + 
					GlobalConstants.EPC_REST_PORT + "/" + GlobalConstants.EPC_REST_BASEURI2;
					

			Stopwatch timerBatch = new Stopwatch();			
			Cookie session = null;
			
			List<String> statusMsgList = new ArrayList<String>();
			timerBatch.start();
			session = this.requestPP(projectId, session, baseUri);
			//logger.info(end + " of " + total + " - elapsed time: " + timerBatch.stop().toString(ISOPeriodFormat.alternateExtended()));
			retStatusMsgList.addAll(statusMsgList);

			
			//this.epcRestMgr.logout(client, baseUri, session);
			
		} catch(Exception e) {
			throw new SystemException(e);
		}
		logger.debug("Complete!");
		return retStatusMsgList;
	}
    
	private Cookie requestPP(String projectId, Cookie session, 
			String baseUri) throws SystemException {

		BayerCalculateWBSIDAPIResultType request = new BayerCalculateWBSIDAPIResultType();
		//request.getBayerCommitmentLIAPI().addAll(subList);

		HashMap<String,String> filterMap = new HashMap<String, String>();
		filterMap.put(GlobalConstants.EPC_ROOTCOSTOBJECT_PARAM, projectId);

		CalcWObjectFactory objectFactory = new CalcWObjectFactory();
		JAXBElement<BayerCalculateWBSIDAPIResultType> requestWrapper = objectFactory.createBayerCalculateWBSIDAPIResult(request);

		ClientResponse response = epcRestMgr
				.postApplicationXmlAsApplicationXml(client, requestWrapper,
						baseUri, GlobalConstants.EPC_REST_POST_PROCESS_PMO,
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
			logger.debug("2000 � PM Order Import - PM Order Post Process Completed Successfully");
			int i=0;
		}
		return session;
	}
	
	   private List<String> preProcess( String projectId) throws SystemException{
			
			logger.debug("Trigger Action to pre process PM Order for Project: " + projectId);
			
			List<String> retStatusMsgList = new ArrayList<String>();
			try {
				
				
				//Prepare for the REST call
				String baseUri = GlobalConstants.EPC_REST_PROTOCOL + "://" + GlobalConstants.EPC_REST_SERVER + ":" + 
						GlobalConstants.EPC_REST_PORT + "/" + GlobalConstants.EPC_REST_BASEURI2;
						

				Stopwatch timerBatch = new Stopwatch();			
				Cookie session = null;
				
				List<String> statusMsgList = new ArrayList<String>();
				timerBatch.start();
				session = this.requestPP(projectId, session, baseUri);
				//logger.info(end + " of " + total + " - elapsed time: " + timerBatch.stop().toString(ISOPeriodFormat.alternateExtended()));
				retStatusMsgList.addAll(statusMsgList);

				
				//this.epcRestMgr.logout(client, baseUri, session);
				
			} catch(Exception e) {
				throw new SystemException(e);
			}
			logger.debug("Complete!");
			return retStatusMsgList;
		}
	    
		private Cookie requestPre(String projectId, Cookie session, 
				String baseUri) throws SystemException {

			BayerCalculateWBSIDAPIResultType request = new BayerCalculateWBSIDAPIResultType();
			//request.getBayerCommitmentLIAPI().addAll(subList);

			HashMap<String,String> filterMap = new HashMap<String, String>();
			filterMap.put(GlobalConstants.EPC_ROOTCOSTOBJECT_PARAM, projectId);

			CalcWObjectFactory objectFactory = new CalcWObjectFactory();
			JAXBElement<BayerCalculateWBSIDAPIResultType> requestWrapper = objectFactory.createBayerCalculateWBSIDAPIResult(request);

			ClientResponse response = epcRestMgr
					.postApplicationXmlAsApplicationXml(client, requestWrapper,
							baseUri, GlobalConstants.EPC_REST_PRE_PROCESS_PMO,
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
				logger.debug("2000 � PM Order Import - PM Order Pre Process Completed Successfully");
				int i=0;
			}
			return session;
		}
}
