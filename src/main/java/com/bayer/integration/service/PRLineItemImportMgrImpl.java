/**
 * 
 */
package com.bayer.integration.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import javax.ws.rs.core.Cookie;
import javax.xml.bind.JAXBElement;

import org.joda.time.format.ISOPeriodFormat;

import com.bayer.integration.odata.SapPOLODataType;
import com.bayer.integration.odata.SapPRLODataType;
import com.bayer.integration.persistence.CaApiErrorDAO;
import com.bayer.integration.persistence.PolApiErrorDAO;
import com.bayer.integration.persistence.PolcoApiErrorDAO;
import com.bayer.integration.properties.GlobalConstants;
import com.bayer.integration.rest.actual.BayerActualsAPIType;
import com.bayer.integration.rest.costaccount.BayerCostAccountsAPIRequestType;
import com.bayer.integration.rest.costaccount.BayerCostAccountsAPIResultType;
import com.bayer.integration.rest.costaccount.BayerCostAccountsAPIType;
import com.bayer.integration.rest.costaccount.CAObjectFactory;
import com.bayer.integration.rest.costaccount.CAObjectResultType;
import com.bayer.integration.rest.log.IntegrationIssuesAPIRequestType;
import com.bayer.integration.rest.log.IntegrationIssuesAPIResultType;
import com.bayer.integration.rest.log.IntegrationIssuesAPIType;
import com.bayer.integration.rest.log.LogObjectFactory;
import com.bayer.integration.rest.pol.BayerCommitmentLIAPIRequestType;
import com.bayer.integration.rest.pol.BayerCommitmentLIAPIResultType;
import com.bayer.integration.rest.pol.BayerCommitmentLIAPIType;
import com.bayer.integration.rest.pol.ObjectFactory;
import com.bayer.integration.rest.pol.ObjectResultType;
import com.bayer.integration.rest.polco.BayerCommitmentLICOAPIRequestType;
import com.bayer.integration.rest.polco.BayerCommitmentLICOAPIResultType;
import com.bayer.integration.rest.polco.BayerCommitmentLICOAPIType;
import com.bayer.integration.rest.polco.COObjectFactory;
import com.bayer.integration.rest.polco.COObjectResultType;
import com.bayer.integration.rest.polhis.BayerCommitmentLIHistoryAPIResultType;
import com.bayer.integration.rest.polhis.BayerCommitmentLIHistoryAPIType;
import com.bayer.integration.rest.prld.BayerCommitmentPRLIDeleteAPIResultType;
import com.bayer.integration.rest.prld.PrldObjectFactory;
import com.bayer.integration.rest.project.BayerProjectAPIType;
import com.bayer.integration.rest.wbsread.BayerWBSReadAPIResultType;
import com.bayer.integration.rest.wbsread.BayerWBSReadAPIType;
import com.bayer.integration.utils.DebugBanner;
import com.bayer.integration.utils.EcosysStructureException;
import com.ecosys.exception.SystemException;
import com.ecosys.util.Stopwatch;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.org.apache.xerces.internal.dom.ElementNSImpl;

/**
 * @author pwng
 *
 */
public class PRLineItemImportMgrImpl extends ImportManagerBase implements
ImportManager {

	/* (non-Javadoc)
	 * @see com.bayer.integration.service.ImportManager#importData()
	 */
	//@Override
	final String itgInterface = "PRL";
	List<IntegrationIssuesAPIType> itgIssueLog = new ArrayList<IntegrationIssuesAPIType>();

	public int importData() {
		int retCode=GlobalConstants.IMPORT_SAP_PRL_SUCCESS;
		try{			
			if (!GlobalConstants.SKIP_SAP_PRL_INTERFACE) {
				if(GlobalConstants.DEBUGMODE)
					DebugBanner.outputBanner(this.getClass().toString());

				boolean skipError = GlobalConstants.SKIP_LOG;
				boolean isLive = GlobalConstants.IS_LIVE_SAP;

				//Create Web Service Client
				if (client == null) setClient(epcRestMgr.createClient(GlobalConstants.EPC_REST_USERNAME, GlobalConstants.EPC_REST_PASSWORD));

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
					String masterProjectId = null;
					if (projectAPIType.getParentCostObjectID()!=null && projectAPIType.getParentCostObjectID()!="")
						masterProjectId = projectAPIType.getParentCostObjectID();

					boolean isSub = false;
					if (projectAPIType.getProjectTypeID().equals(GlobalConstants.EPC_API_PROJECT_TYPE_SUB))
						isSub = true;

					List<PolApiErrorDAO> errorDAOList = new ArrayList<PolApiErrorDAO>();
					List<CaApiErrorDAO> errorCaDAOList = new ArrayList<CaApiErrorDAO>();
					List<PolApiErrorDAO> invalidRecords = new ArrayList<PolApiErrorDAO>(); 
					List<BayerCommitmentLIAPIType> inputList = new ArrayList<BayerCommitmentLIAPIType>();
					List<BayerCommitmentLIHistoryAPIType> hisList = new ArrayList<BayerCommitmentLIHistoryAPIType>();
					List<BayerCommitmentLIAPIType> validRecords = new ArrayList<BayerCommitmentLIAPIType>();
					List<BayerCostAccountsAPIType> costAccounts = new ArrayList<BayerCostAccountsAPIType>();
					session = null;
					try
					{
						//soft delete existing records from EcoSys
						logger.debug("Purging existing PR Line Item data from EcoSys for Project"+ projectId);
						this.deleteLIs(projectId);
						logger.debug("Purging existing PR Line Item data from EcoSys completed for Project"+ projectId);

						//Delete existing records from ERR Log table
						logger.debug("Purging existing PR Line Item log records from ERR Log table for Project"+ projectId);
						//int logCount = this.deleteLogs(projectId, GlobalConstants.EPC_PRL_API_ERROR_BATCH_DELETE);
						//logger.debug("Purging " + logCount + " existing PR Line Item log records from ERR Log table for Project"+ projectId);

						//Invoke EcoSys API with input records
						logger.debug("Reading PR Line Item data from SAP Input for Project: "+ projectId);	

						if (isLive)
						{
							inputList = readSapData(sapProjectId, systemId); 
							invalidRecords = this.getSapInputInvalid(inputList);
							logger.debug("Number of Records Read in from OData: " + inputList.size() + " for Project: " + projectId);
						}
						else
						{
							//Start Section Sample Data
							List<SapPRLODataType> inputListSample = importHelper.getSapPRLTypesSample();
							invalidRecords = this.getBayerCommitmentLIAPITypesInvalid(inputListSample);	
							inputList = this.getBayerCommitmentLIAPITypesSampleRaw(inputListSample);
							//End Section Sample Data
						}

						costAccounts = this.getBayerCostAccountsAPITypes(inputList);
						validRecords = this.getBayerCommitmentLIAPITypesValid(inputList, sapProjectId);	

						//Phase 3 CR2: Process input to filter out Actuals with Missing owner COs - Disabled
						//validRecords = this.getValidParentCoLIs(validRecords, noCORecords, currentParentCOs);

						//Read PO/PR Line Items from Commitments - History version to track Net Order Value Change History
						if (isSub)
							hisList = this.readPOLHistory(masterProjectId);
						else
							hisList = this.readPOLHistory(projectId);

						validRecords = this.updateLIChangeHistory(validRecords, hisList);

						//Process Invalid Records
						logger.debug("Processing Invalid Records, If Any for Project: " + projectId);
						if (invalidRecords.size()>0 && !skipError)
							this.processStatusMessages(invalidRecords, true);
						logger.debug("Processing Invalid Records Completed for Project: "+ projectId);	

						//Process  PO Line Items related Cost Elements
						logger.debug("Processing PO Line Items related Cost Elements for Project: " + projectId);
						errorCaDAOList = processCostAccounts(costAccounts, projectId);			
						logger.debug("Processing PO Line Items related Cost Elements completed for Project: " + projectId);					
						errorDAOList = processCommitmentLIs(validRecords, projectId);

						//Aug 2021 Phase 3 CR02 Process PR Line Items with Missing Owner CO - Disabled
						/*
						if (noCORecords.size()>0)
						{
							logger.debug("Processing PR Line Item with Missing Owner Cost Object in EcoSys for Project: " + projectId);

							errorMissingDAOList = this.processMissingCOLIs(noCORecords, projectId);		
							//validRecords = this.removeParentMissingTrans(validRecords, noParentLICOs, projectId);

							logger.debug("Processing PR Line Item with Missing Owner Cost Object in EcoSys completed for Project: " + projectId);
						}*/

						//Process Status Messages
						if (!skipError)
						{
							logger.debug("Processing ERR Messages, If Any for Project: " + projectId);
							//this.processStatusMessages(errorDAOList, true);
							logger.debug("Processing ERR Message Completed for Project: "+ projectId);	
						}
						logger.debug("posting "+itgIssueLog.size()+" " +itgInterface+" issues to EPC");
						if (itgIssueLog.size()>0)
							processIssueLog(itgIssueLog);					
					}
					catch(SystemException se) {
						logger.error("7005 -- PR Line Item Import Failed: " + GlobalConstants.EPC_PROJECT_ID_PROCESSED, se);
						retCode=GlobalConstants.IMPORT_SAP_PRL_FAILED;
						continue;
					}
				}

			} else {
				logger.info("Skipped PR Line Item Import Interface. Change the skip property to 'false'");				
			}

		}catch(Exception e) {			
			logger.error("7005 -- PR Line Item Import Failed", e);			
			retCode=GlobalConstants.IMPORT_SAP_PRL_FAILED;
		}

		if (retCode==GlobalConstants.IMPORT_SAP_POL_SUCCESS)
			logger.debug("7000 -- PR Line Item Import Completed Successfully");

		return retCode;
	}


	private IntegrationIssuesAPIType getIntegrationIssuesAPIType(String logErrorId, String logDescription, String logComment, String ExternalKey) {
		IntegrationIssuesAPIType logEntry = new IntegrationIssuesAPIType();
		logEntry.setIntegrationLogID(itgInterface);
		//logEntry.setIntegrationLogID(itgInterface+"."+logErrorId);
		logEntry.setDescription(logDescription);
		logEntry.setComment(logComment);
		logEntry.setExternalKey(ExternalKey);
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
		if(session == null)			session = epcRestMgr.getSessionCookie(response);
		/*		if(!result.isSuccessFlag()){			String errMsg="The interface failed to load any record due to data issues; please verify data.";			if (result.getError() != null)				errMsg=result.getError().toString();			for(IntegrationIssuesAPIType issue: issueList)			{				if (errMsg.length() > GlobalConstants.errorMsgSize)					errMsg = errMsg.substring(0, GlobalConstants.errorMsgSize-1);			}			throw new SystemException(errMsg);		} else {			int i=0;			int c=0;			for(ObjectResultType or : result.getObjectResult()) {				IntegrationIssuesAPIType apiType = issueList.get(i++);				String apiID = apiType.getExternalKey();				if(or.isSuccessFlag()) {					logger.debug("UPDATE RESULT --> " + or.getInternalId());					logger.debug("Record with External Key ID of : "  + "|ExternalId: " + or.getExternalId() );				} else {							}			}		}*/
		return session;	
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
			session = this.requestEvt(projectId, session, baseUri);
			//logger.info(end + " of " + total + " - elapsed time: " + timerBatch.stop().toString(ISOPeriodFormat.alternateExtended()));
			retStatusMsgList.addAll(statusMsgList);


			//this.epcRestMgr.logout(client, baseUri, session);

		} catch(Exception e) {
			throw new SystemException(e);
		}
		logger.debug("Complete!");
		return retStatusMsgList;
	}

	private Cookie requestEvt(String projectId, Cookie session, 
			String baseUri) throws SystemException {

		BayerCommitmentPRLIDeleteAPIResultType request = new BayerCommitmentPRLIDeleteAPIResultType();
		//request.getBayerCommitmentLIAPI().addAll(subList);

		HashMap<String,String> filterMap = new HashMap<String, String>();
		filterMap.put("ProjectId", projectId);

		PrldObjectFactory objectFactory = new PrldObjectFactory();
		JAXBElement<BayerCommitmentPRLIDeleteAPIResultType> requestWrapper = objectFactory.createBayerCommitmentPRLIDeleteAPIResult(request);

		ClientResponse response = epcRestMgr
				.postApplicationXmlAsApplicationXml(client, requestWrapper,
						baseUri, GlobalConstants.EPC_REST_DELETE_SAP_PR_LNITM,
						session, filterMap);

		logger.debug(response);
		BayerCommitmentPRLIDeleteAPIResultType result = epcRestMgr.responseToObject(response, BayerCommitmentPRLIDeleteAPIResultType.class);

		if(session == null)
			session = epcRestMgr.getSessionCookie(response);

		if(!result.isSuccessFlag()){
			String errMsg="The interface failed to load any record due to data issues; please verify data.";
			if (result.getError() != null)
				errMsg=result.getError().toString();

			throw new SystemException(errMsg);
		} else {

			//List<StatusMessage> statusMsgList = new ArrayList<StatusMessage>();
			logger.debug("5000 -- PR Line Item Delete Completed Successfully for Project: "+ projectId);
			int i=0;
		}
		return session;
	}

	private List<BayerCommitmentLIAPIType> readSapData(String projectId, String systemId) throws SystemException {
		Map<String, Map<String, Object>> dataRows = odataSvcMgr.readPRLineItems(projectId, systemId);
		List<BayerCommitmentLIAPIType> apiTypes = new ArrayList<BayerCommitmentLIAPIType>();
		if (dataRows!=null)
		{
			Map<String, BayerCommitmentLIAPIType> apiList = odataSvcMgr.mapPRLineItemForImport(dataRows);
			apiTypes.addAll(apiList.values());
		}
		return apiTypes;
	}

	public List<PolApiErrorDAO> getSapInputInvalid(List<BayerCommitmentLIAPIType > apiTypes){

		List<PolApiErrorDAO> errorList = new ArrayList<PolApiErrorDAO>();
		for (int i = 0; i < apiTypes.size(); i++)
		{
			BayerCommitmentLIAPIType apiType = apiTypes.get(i);
			if (!isValidAPIType(apiType))
			{
				PolApiErrorDAO errorDAO = this.getPolAPIErrorDAO(apiType);
				errorDAO.setStatus(GlobalConstants.STAGING_RECORD_STATUS_SKIPPED);
				errorList.add(errorDAO);
			}
		}
		return errorList;
	}

	private boolean isValidAPIType (BayerCommitmentLIAPIType apiType)
	{
		boolean isValid = true;
		//String projectId = oDataType.getProjectDefinition();
		String wbsId = apiType.getCostObjectExternalKey();
		//String pmoId = oDataType.getPmOrder();
		String commitmentId = apiType.getCommitmentID();
		if (wbsId == null ||wbsId.equals("")||(
				commitmentId == null||commitmentId.equals("")))
			isValid = false;
		return isValid;
	}

	//Convert SAPACTODataType List to BayerDirectChargeCOAPIType List
	public List<BayerCostAccountsAPIType> getBayerCostAccountsAPITypes(List<BayerCommitmentLIAPIType> colApiTypes){

		BayerCostAccountsAPIRequestType request = new BayerCostAccountsAPIRequestType();
		Map<String, BayerCostAccountsAPIType> caMap = new HashMap<String, BayerCostAccountsAPIType>();
		for (int i = 0; i < colApiTypes.size(); i++)
		{
			BayerCommitmentLIAPIType colApiType = colApiTypes.get(i);

			if (isValidCADataType(colApiType))
			{
				BayerCostAccountsAPIType apiType = this.getBayerCostAccountsAPIType(colApiType);
				if (!caMap.containsKey(apiType.getID()))
				{
					request.getBayerCostAccountsAPI().add(apiType);
					caMap.put(apiType.getID(), apiType);
				}
			}
		}
		return request.getBayerCostAccountsAPI();
	}

	public BayerCostAccountsAPIType getBayerCostAccountsAPIType(BayerCommitmentLIAPIType colApiType)
	{
		BayerCostAccountsAPIType apiType = new BayerCostAccountsAPIType();
		apiType.setID(colApiType.getCostAccountID());
		apiType.setName(colApiType.getCostAccountName());
		//apiType.setName(oDataType.getCostElement());
		return apiType;
	}

	private boolean isValidCADataType (BayerCommitmentLIAPIType apiType)
	{
		boolean isValid = false;
		String caId = apiType.getCostAccountID();
		if (caId!=null || !caId.equals(""))
			isValid = true;

		return isValid;
	}

	public List<BayerCommitmentLIAPIType> getBayerCommitmentLIAPITypesValid(List<BayerCommitmentLIAPIType> apiTypes, String sapProjectId) {

		BayerCommitmentLIAPIRequestType request = new BayerCommitmentLIAPIRequestType();
		List<BayerCommitmentLIAPIType>apiTypes2 = hasParentCostObject(apiTypes,sapProjectId);

		for (int i = 0; i < apiTypes2.size(); i++) {
			BayerCommitmentLIAPIType apiType = apiTypes2.get(i);
			if (isValidAPIType(apiType))
				request.getBayerCommitmentLIAPI().add(apiType);	
		}
		return request.getBayerCommitmentLIAPI();
	}

	private List<BayerCommitmentLIAPIType> hasParentCostObject(List<BayerCommitmentLIAPIType> apiTypes, String projectId) {

		List<BayerWBSReadAPIType> ecosysWBS = new ArrayList<>();
		List<BayerWBSReadAPIType> ecosysPMO = new ArrayList<>();
		List<BayerCommitmentLIAPIType> apiTypes2 = new ArrayList<BayerCommitmentLIAPIType>();

		// has valid parent CO?
		try {		
			logger.debug("reading EcoSys cost objects for project: " + projectId);
			ecosysWBS = this.processEPCCostObjects(projectId, "WBS");
			ecosysPMO = this.processEPCCostObjects(projectId, "PM Order");
			ecosysWBS.addAll(ecosysPMO);
			ecosysPMO.clear();
			logger.debug("checking " + ecosysWBS.size() + " records for parent cost object");
			for (BayerCommitmentLIAPIType apiType : apiTypes) {		
				try {
					String hpid =ecosysWBS.stream()
							.filter(t -> t.getExternalKey().equalsIgnoreCase(apiType.getCostObjectExternalKey()))
							.findFirst().get().getHierarchyPathID();
					apiTypes2.add(apiType);			
				}
				catch (NoSuchElementException e) {
					logger.error("PR Line : MISSING PARENT : SAP Requisition Number: " +apiType.getSAPPurchaseRequisitionNumber() +" --> SAP PR Line Item Number: " +apiType.getSAPPurchaseRequisitionLineItemNumbe());
					//itgIssueLog.add(getIntegrationIssuesAPIType("", "PR Line : MISSING PARENT : SAP Requisition Number: "+apiType.getSAPPurchaseRequisitionNumber() +" --> SAP PR Line Item Number: "+apiType.getSAPPurchaseRequisitionLineItemNumbe(),"",""));
					itgIssueLog.add(getIntegrationIssuesAPIType(""
							,"SAP Requisition Number: "+apiType.getSAPPurchaseRequisitionNumber() +", line Number: "+apiType.getSAPPurchaseRequisitionLineItemNumbe()+ " does not have a valid parent"
							,projectId,""));

					System.out.println(projectId);
					continue;
				}
			}
		}	
		catch (SystemException e) {
			logger.error("error calling the read wbs API");
			e.printStackTrace();
		}
		logger.debug("PM order checked: "+ apiTypes.size()+" || "+" Parents found: "+ apiTypes2.size());
		return apiTypes2;
	}
	private  List<BayerWBSReadAPIType> processEPCCostObjects(String projectId,String coType) throws SystemException{
		List<BayerWBSReadAPIType> parentCOs = new ArrayList<BayerWBSReadAPIType>();
		try 
		{	
			//Read PM Order Data from EcoSys using PMOrder API
			Cookie session = null;
			String baseUri2 = GlobalConstants.EPC_REST_PROTOCOL + "://" + GlobalConstants.EPC_REST_SERVER + ":"
					+ GlobalConstants.EPC_REST_PORT + "/" + GlobalConstants.EPC_REST_BASEURI2;				

			HashMap<String, String> aMap = new HashMap<String, String>();
			aMap.put(GlobalConstants.EPC_PROJECT_PARAM, projectId);
			aMap.put("structureType", coType);
			parentCOs = this.requestEPCCostObjects(session, baseUri2, aMap);
			return parentCOs;

		} catch(Exception e) {
			throw new SystemException(e);
		}
	}

	private List<BayerWBSReadAPIType> requestEPCCostObjects(Cookie session, String baseUri, HashMap<String, String> coMap) throws SystemException {

		ClientResponse response = epcRestMgr.getAsApplicationXml(client, baseUri,GlobalConstants.EPC_REST_READ_ECOSYS_WBS,session,coMap);

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

	private  List<BayerCommitmentLIHistoryAPIType> readPOLHistory(String projectId) throws SystemException{

		List<BayerCommitmentLIHistoryAPIType> bayerCommitmentLIs = new ArrayList<BayerCommitmentLIHistoryAPIType>();
		try 
		{	
			//Read PM Order Data from EcoSys using PMOrder API
			Cookie session = null;
			String baseUri2 = GlobalConstants.EPC_REST_PROTOCOL + "://" + GlobalConstants.EPC_REST_SERVER + ":"
					+ GlobalConstants.EPC_REST_PORT + "/" + GlobalConstants.EPC_REST_BASEURI2;				

			HashMap<String, String> aMap = new HashMap<String, String>();
			aMap.put(GlobalConstants.EPC_PROJECT_PARAM, projectId);
			bayerCommitmentLIs = this.requestPOLHistory(session, baseUri2, aMap);
			return bayerCommitmentLIs;

		} catch(Exception e) {
			throw new SystemException(e);
		}
	}

	private List<BayerCommitmentLIHistoryAPIType> requestPOLHistory(Cookie session, String baseUri, HashMap<String, String> polMap) throws SystemException {

		ClientResponse response = epcRestMgr
				.getAsApplicationXml(client, baseUri,
						GlobalConstants.EPC_REST_IMPORT_SAP_PO_LNITM_HIS,session,polMap);

		logger.debug(response);
		BayerCommitmentLIHistoryAPIResultType result = epcRestMgr.responseToObject(response, BayerCommitmentLIHistoryAPIResultType.class);
		List<BayerCommitmentLIHistoryAPIType> apiTypes = result.getBayerCommitmentLIHistoryAPI();
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

	private  List<BayerCommitmentLIAPIType> updateLIChangeHistory(List<BayerCommitmentLIAPIType> inputList, List<BayerCommitmentLIHistoryAPIType> prdList){

		List<BayerCommitmentLIAPIType> updatedLIs = new ArrayList<BayerCommitmentLIAPIType>();
		for(BayerCommitmentLIAPIType li: inputList)
		{
			double changeAmt = 0.0;
			changeAmt = this.checkChange(li, prdList);		
			if (changeAmt>0.0||changeAmt<0.0)
			{
				li.setNetOrderValueChangedID(GlobalConstants.EPC_API_ERROR_FLAG_Y);
				li.setPOAltCostBackup(changeAmt);
			}
			else
			{
				li.setNetOrderValueChangedID(GlobalConstants.EPC_API_ERROR_FLAG_N);
				li.setPOAltCostBackup(0.0);
			}
			updatedLIs.add(li);
		}
		return updatedLIs;
	}

	private double checkChange (BayerCommitmentLIAPIType apiType,List<BayerCommitmentLIHistoryAPIType> prdList )
	{		
		double chgAmt =apiType.getCostTransactionCurrency();

		if (!prdList.isEmpty())
		{
			for(BayerCommitmentLIHistoryAPIType li: prdList)
			{
				if (li.getCostObjectExternalKey().equalsIgnoreCase(apiType.getCostObjectExternalKey())
						&& li.getCommitmentID().equalsIgnoreCase(apiType.getCommitmentID())
						&& li.getSAPPurchaseRequisitionLineItemNumbe().equalsIgnoreCase(apiType.getSAPPurchaseRequisitionLineItemNumbe()))
				{
					chgAmt = chgAmt - li.getCostTransactionCurrency();	
				}
			}
		}

		return chgAmt;
	}

	//Start Section Processing Cost Accounts
	private List<CaApiErrorDAO> processCostAccounts( List<BayerCostAccountsAPIType> prjRecords,
			String projectId) throws SystemException{

		logger.debug("Importing Actuals associated Cost Elements to project " + projectId + " to EPC as Cost Accounts...");

		List<CaApiErrorDAO> retStatusMsgList = new ArrayList<CaApiErrorDAO>();
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
				List<CaApiErrorDAO> statusMsgList = new ArrayList<CaApiErrorDAO>();
				timerBatch.start();
				session = this.requestCA(prjRecords.subList(i, end), 
						session, baseUri, projectId, statusMsgList);
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

	private Cookie requestCA(List<BayerCostAccountsAPIType> subList, Cookie session,
			String baseUri, String projectId, List<CaApiErrorDAO> errorList) throws SystemException {

		BayerCostAccountsAPIRequestType request = new BayerCostAccountsAPIRequestType();
		request.getBayerCostAccountsAPI().addAll(subList);

		CAObjectFactory objectFactory = new CAObjectFactory();
		JAXBElement<BayerCostAccountsAPIRequestType> requestWrapper = objectFactory.createBayerCostAccountsAPIRequest(request);

		ClientResponse response = epcRestMgr
				.postApplicationXmlAsApplicationXml(client, requestWrapper,
						baseUri, GlobalConstants.EPC_REST_IMPORT_SAP_CA,
						session);

		logger.debug(response);
		BayerCostAccountsAPIResultType result = epcRestMgr.responseToObject(response, BayerCostAccountsAPIResultType.class);
		if(session == null)
			session = epcRestMgr.getSessionCookie(response);

		if(!result.isSuccessFlag()){
			String errMsg="The interface failed to load any record due to data issues; please verify data.";
			if (result.getError() != null)
				errMsg=result.getError().toString();
			for(BayerCostAccountsAPIType ca: subList)
			{
				//convert to APIErrorDAO type
				CaApiErrorDAO statusMsg = this.getCaAPIErrorDAO(ca);
				statusMsg.setRootCostObjectID(projectId);
				statusMsg.setStatus(GlobalConstants.STAGING_RECORD_STATUS_FAILED);
				statusMsg.setErrorMsg(errMsg);
				errorList.add(statusMsg);
			}
			throw new SystemException(errMsg);
		} else {

			//List<StatusMessage> statusMsgList = new ArrayList<StatusMessage>();
			int i=0;
			for(CAObjectResultType or : result.getObjectResult()) {
				BayerCostAccountsAPIType ca = subList.get(i++);
				String caID = ca.getID();

				//convert to APIErrorDAO type
				CaApiErrorDAO statusMsg = this.getCaAPIErrorDAO(ca);
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

	private CaApiErrorDAO getCaAPIErrorDAO(BayerCostAccountsAPIType apiType)
	{
		CaApiErrorDAO apiError = new CaApiErrorDAO();
		apiError.setID(apiType.getID());
		//apiError.setCostObjectHierarchyPathID(apiType.getCostObjectHierarchyPathID());
		apiError.setName(apiType.getName());

		return apiError;
	}

	private List<PolApiErrorDAO> processCommitmentLIs( List<BayerCommitmentLIAPIType> prjRecords,
			String projectId) throws SystemException{

		logger.debug("Importing PR Line Items to EPC...");

		List<PolApiErrorDAO> retStatusMsgList = new ArrayList<PolApiErrorDAO>();
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
				List<PolApiErrorDAO> statusMsgList = new ArrayList<PolApiErrorDAO>();
				timerBatch.start();
				session = this.request(prjRecords.subList(i, end), 
						session, baseUri, projectId, statusMsgList);
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

	private Cookie request(List<BayerCommitmentLIAPIType> subList, Cookie session,
			String baseUri, String projectId, List<PolApiErrorDAO> errorList) throws SystemException {

		BayerCommitmentLIAPIRequestType request = new BayerCommitmentLIAPIRequestType();
		request.getBayerCommitmentLIAPI().addAll(subList);

		ObjectFactory objectFactory = new ObjectFactory();
		JAXBElement<BayerCommitmentLIAPIRequestType> requestWrapper = objectFactory.createBayerCommitmentLIAPIRequest(request);
		HashMap<String, String> prjMap = new HashMap<String, String>();
		prjMap.put(GlobalConstants.EPC_PROJECT_PARAM, projectId);
		ClientResponse response = epcRestMgr
				.postApplicationXmlAsApplicationXml(client, requestWrapper,
						baseUri, GlobalConstants.EPC_REST_IMPORT_SAP_PR_LNITM,
						session);

		logger.debug(response);
		BayerCommitmentLIAPIResultType result = epcRestMgr.responseToObject(response, BayerCommitmentLIAPIResultType.class);
		if(session == null)
			session = epcRestMgr.getSessionCookie(response);

		if(!result.isSuccessFlag()){
			String errMsg="The interface failed to load any record due to data issues; please verify data.";
			if (result.getError() != null)
				errMsg=result.getError().toString();
			for(BayerCommitmentLIAPIType prl: subList)
			{
				//convert to APIErrorDAO type
				PolApiErrorDAO statusMsg = this.getPolAPIErrorDAO(prl);
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
				BayerCommitmentLIAPIType poLI = subList.get(i++);
				String polID = poLI.getCommitmentID() + "-" + poLI.getSAPPurchaseRequisitionLineItemNumbe();

				//convert to APIErrorDAO type
				PolApiErrorDAO statusMsg = this.getPolAPIErrorDAO(poLI);
				statusMsg.setRootCostObjectID(projectId);

				if(or.isSuccessFlag()) {
					logger.debug("UPDATE RESULT --> " + or.getInternalId());
					logger.debug("Record with External Key ID of : " + polID 
							+" "+ GlobalConstants.STAGING_RECORD_STATUS_LOADED);
					/*
					logger.debug("Record --> " + or.getInternalId() + "|ExternalId: " + polID 
							 + "CommitmentID: " + poLI.getCommitmentID() + "|"
							 + "CostAccountID: " + poLI.getCostAccountID()+ "|"
							 + "CostAccountName: " + poLI.getCostAccountName()+ "|"
							 + "CostObjectExternalKey: " + poLI.getCostObjectExternalKey() + "|"
							 + "CostObjectID: " + poLI.getCostObjectID()+ "|"
							 + "CostObjectName: " + poLI.getCostObjectName()+ "|"
							 + "CurrencyCostObjectCode: " + poLI.getCurrencyCostObjectCode() + "|"
							 + "CurrencyTransactionCode: " + poLI.getCurrencyTransactionCode() + "|"
							 + "DeletionFlagID: " + poLI.getDeletionFlagID()+ "|"
							 + "ExternalKey: " + poLI.getExternalKey() + "|"
						     + "FinalConfirmationID: " + poLI.getFinalConfirmationID()+ "|"
							 + "LineItemText: " + poLI.getLineItemText()+ "|"
							 + "Receiver: " + poLI.getReceiver() + "|"
							 + "Requestor: " + poLI.getRequestor() + "|"
							 + "SAPPRProcessingState: " + poLI.getSAPPRProcessingState()+ "|"
							 + "SAPPRProcessingStatus: " + poLI.getSAPPRProcessingStatus() + "|"
						     + "SAPPurcharsingOrderSeqNumber: " + poLI.getSAPPurcharsingOrderSeqNumber()+ "|"
							 + "SAPPurchaseRequisitionLineItemNumbe: " + poLI.getSAPPurchaseRequisitionLineItemNumbe()+ "|"
							 + "SAPPurchaseRequisitionNumber: " + poLI.getSAPPurchaseRequisitionNumber()+ "|"
							 + "SAPPurchasingDocumentLineItemNumber: " + poLI.getSAPPurchasingDocumentLineItemNumber() + "|"
							 + "SAPPurchasingDocumentNumberID: " + poLI.getSAPPurchasingDocumentNumberID()+ "|"
							 + "SAPWBSElement: " + poLI.getSAPWBSElement() + "|"
						     + "TransactionExchangeRateSource: " + poLI.getTransactionExchangeRateSource()+ "|"
							 + "UnitofMeasureID: " + poLI.getUnitofMeasureID()+ "|" 
							 + "VersionID: " + poLI.getVersionID()+ "|"
							 + "ActualCostTransactionCurrency: " + poLI.getActualCostTransactionCurrency() + "|"
							 + "AlternateCostExternal: " + poLI.getAlternateCostExternal()+ "|"
							 + "ConversionRateCostObjectCurrency: " + poLI.getConversionRateCostObjectCurrency()+ "|"
						     + "CostCostObjectCurrency: " + poLI.getCostCostObjectCurrency()+ "|"
							 + "CostExternal: " + poLI.getCostExternal()+ "|"
							 + "CostTransactionCurrency: " + poLI.getCostTransactionCurrency()+ "|"
							 + "Obligo: " + poLI.getObligo() + "|"
							 + "PODistributionPCT: " + poLI.getPODistributionPCT()+ "|"
							 + "POQuantity: " + poLI.getPOQuantity()+ "|"
						     + "PRQuantity: " + poLI.getPRQuantity() + "|"
							 + "SAPExchangeRate: " + poLI.getSAPExchangeRate()+ "|"
							 + "SAPPRExchangeRate: " + poLI.getSAPPRExchangeRate()+ "|"
							 + "TransactionDate: " + poLI.getTransactionDate() + "|"
								+ "|" + or.isSuccessFlag() + "|" + "Updated");	
					 */
					statusMsg.setStatus(GlobalConstants.STAGING_RECORD_STATUS_LOADED);

				} else {
					//String str = ((ElementNSImpl)or.getResultMessage().get(0).getMessage()).getTextContent();
					String str = or.getResultMessage().get(0).getMessage();
					//str = ((ElementNSImpl)str;
					logger.error("ERROR --> " + or.getInternalId() + "|ExternalId: " + polID 
							+ "CommitmentID: " + poLI.getCommitmentID() + "|"
							+ "CostAccountID: " + poLI.getCostAccountID()+ "|"
							+ "CostAccountName: " + poLI.getCostAccountName()+ "|"
							+ "CostObjectExternalKey: " + poLI.getCostObjectExternalKey() + "|"
							+ "CostObjectID: " + poLI.getCostObjectID()+ "|"
							+ "CostObjectName: " + poLI.getCostObjectName()+ "|"
							+ "CurrencyCostObjectCode: " + poLI.getCurrencyCostObjectCode() + "|"
							+ "CurrencyTransactionCode: " + poLI.getCurrencyTransactionCode() + "|"
							+ "DeletionFlagID: " + poLI.getDeletionFlagID()+ "|"
							+ "ExternalKey: " + poLI.getExternalKey() + "|"
							+ "FinalConfirmationID: " + poLI.getFinalConfirmationID()+ "|"
							+ "LineItemText: " + poLI.getLineItemText()+ "|"
							+ "Receiver: " + poLI.getReceiver() + "|"
							+ "Requestor: " + poLI.getRequestor() + "|"
							+ "SAPPRProcessingState: " + poLI.getSAPPRProcessingState()+ "|"
							+ "SAPPRProcessingStatus: " + poLI.getSAPPRProcessingStatus() + "|"
							+ "SAPPurcharsingOrderSeqNumber: " + poLI.getSAPPurcharsingOrderSeqNumber()+ "|"
							+ "SAPPurchaseRequisitionLineItemNumbe: " + poLI.getSAPPurchaseRequisitionLineItemNumbe()+ "|"
							+ "SAPPurchaseRequisitionNumber: " + poLI.getSAPPurchaseRequisitionNumber()+ "|"
							+ "SAPPurchasingDocumentLineItemNumber: " + poLI.getSAPPurchasingDocumentLineItemNumber() + "|"
							+ "SAPPurchasingDocumentNumberID: " + poLI.getSAPPurchasingDocumentNumberID()+ "|"
							+ "SAPWBSElement: " + poLI.getSAPWBSElement() + "|"
							+ "TransactionExchangeRateSource: " + poLI.getTransactionExchangeRateSource()+ "|"
							+ "UnitofMeasureID: " + poLI.getUnitofMeasureID()+ "|" 
							+ "VersionID: " + poLI.getVersionID()+ "|"
							+ "ActualCostTransactionCurrency: " + poLI.getActualCostTransactionCurrency() + "|"
							+ "AlternateCostExternal: " + poLI.getAlternateCostExternal()+ "|"
							+ "ConversionRateCostObjectCurrency: " + poLI.getConversionRateCostObjectCurrency()+ "|"
							+ "CostCostObjectCurrency: " + poLI.getCostCostObjectCurrency()+ "|"
							+ "CostExternal: " + poLI.getCostExternal()+ "|"
							+ "CostTransactionCurrency: " + poLI.getCostTransactionCurrency()+ "|"
							+ "Obligo: " + poLI.getObligo() + "|"
							+ "PODistributionPCT: " + poLI.getPODistributionPCT()+ "|"
							+ "POQuantity: " + poLI.getPOQuantity()+ "|"
							+ "PRQuantity: " + poLI.getPRQuantity() + "|"
							+ "SAPExchangeRate: " + poLI.getSAPExchangeRate()+ "|"
							+ "SAPPRExchangeRate: " + poLI.getSAPPRExchangeRate()+ "|"
							+ "TransactionDate: " + poLI.getTransactionDate() + "|"
							+ "|" + or.isSuccessFlag() + "|" + str);	
					statusMsg.setStatus(GlobalConstants.STAGING_RECORD_STATUS_FAILED);
					statusMsg.setErrorMsg(str);
					itgIssueLog.add(getIntegrationIssuesAPIType("", polID + "  " +str.substring(str.indexOf(":")+2), projectId, polID));
				}
				errorList.add(statusMsg);								
			}
		}
		return session;
	}

	private PolApiErrorDAO getPolAPIErrorDAO(BayerCommitmentLIAPIType apiType)
	{
		PolApiErrorDAO apiError = new PolApiErrorDAO();
		apiError.setCommitmentID(apiType.getCommitmentID());
		apiError.setConversionRateCostObjectCurrency(apiType.getConversionRateCostObjectCurrency());
		apiError.setCostAccountID(apiType.getCostAccountID());
		apiError.setCostAccountName(apiType.getCostAccountName());
		apiError.setCostCostObjectCurrency(apiType.getCostCostObjectCurrency());
		apiError.setCostObjectExternalKey(apiType.getCostObjectExternalKey());
		//apiError.setCostObjectHierarchyPathID(apiType.getCostObjectHierarchyPathID());
		apiError.setCostObjectID(apiType.getCostObjectID());
		apiError.setCostTransactionCurrency(apiType.getCostTransactionCurrency());
		apiError.setActualCostTransactionCurrency(apiType.getActualCostTransactionCurrency());
		apiError.setObligo(apiType.getObligo());
		apiError.setCurrencyCostObjectCode(apiType.getCurrencyCostObjectCode());
		apiError.setCurrencyTransactionCode(apiType.getCurrencyTransactionCode());
		apiError.setDeletionFlagID(apiType.getDeletionFlagID());
		apiError.setExternalKey(apiType.getExternalKey());
		apiError.setSAPPurchaseRequisitionLineItemNumbe(apiType.getSAPPurchaseRequisitionLineItemNumbe());
		apiError.setSAPPurchaseRequisitionNumber(apiType.getSAPPurchaseRequisitionNumber());
		apiError.setSAPPurchasingDocumentLineItemNumber(apiType.getSAPPurchasingDocumentLineItemNumber());
		apiError.setSAPPurchasingDocumentNumberID(apiType.getSAPPurchasingDocumentNumberID());
		apiError.setSAPWBSElement(apiType.getSAPWBSElement());
		apiError.setTransactionDate(apiType.getTransactionDate());
		apiError.setVersionID(apiType.getVersionID());
		apiError.setPODistributionPCT(apiType.getPODistributionPCT());
		apiError.setPRQuantity(apiType.getPRQuantity());
		apiError.setUnitofMeasureID(apiType.getUnitofMeasureID());
		apiError.setSAPPurcharsingOrderSeqNumber(apiType.getSAPPurcharsingOrderSeqNumber());
		apiError.setSAPPRProcessingState(apiType.getSAPPRProcessingState());
		apiError.setSAPPRProcessingStatus(apiType.getSAPPRProcessingStatus());
		apiError.setSAPExchangeRate(apiType.getSAPExchangeRate());
		return apiError;
	}

	private void processStatusMessages(List<PolApiErrorDAO> errorList, boolean isNew)  throws SystemException {
		try{
			//stgDBMgr.insertWbsBatch(this.wbsAPIErrorDAOList);
			if (isNew)
				stgDBMgr.insertPolBatch(errorList, GlobalConstants.EPC_PRL_API_ERROR_BATCH_INSERT);
			else
				stgDBMgr.updatePolBatch(errorList, GlobalConstants.EPC_PRL_API_ERROR_BATCH_UPDATE);

		} catch(Exception e) {
			throw new SystemException (e);
		}
	}

	public List<BayerCommitmentLICOAPIType> getBayerCommitmentLICOAPITypes(List<SapPRLODataType> oDataTypes){			
		BayerCommitmentLICOAPIRequestType request = new BayerCommitmentLICOAPIRequestType();
		for (int i = 0; i < oDataTypes.size(); i++)
		{
			SapPRLODataType oDataType = oDataTypes.get(i);
			if (isValidODataTypeSample(oDataType))
			{
				BayerCommitmentLICOAPIType apiType = this.getCommitmentLICOAPIType(oDataType);
				request.getBayerCommitmentLICOAPI().add(apiType);
			}
		}
		return request.getBayerCommitmentLICOAPI();
	}	

	/* Convert SapWBSODataType Object to BayerCommitmentLICOAPIType Object
	 * 
	 */
	public BayerCommitmentLICOAPIType getCommitmentLICOAPIType(SapPRLODataType oDataType)
	{
		BayerCommitmentLICOAPIType apiType = new BayerCommitmentLICOAPIType();
		String projectId = oDataType.getProjectDefinition();
		String pohId = oDataType.getPurchReq()+"_"+oDataType.getPurchReqItem();
		//String wbsElement = oDataType.getWbsElement();
		String pmoId = oDataType.getPmOrder();

		if (pmoId!=null&&!pmoId.equals(""))
			apiType.setParentCostObjectExternalKey(pmoId);
		else 
			apiType.setParentCostObjectExternalKey(oDataType.getWbsElement());

		//if (apiType.getCostObjectExternalKey().equals("A00GV-999990-C1"))
		//apiType.setCostObjectHierarchyPathID("A00GV-999990.C.1");

		apiType.setCostObjectID(pohId);
		apiType.setCostObjectName(oDataType.getPurchReqDesc());
		apiType.setExternalKey(pohId);

		return apiType;
	}


	private boolean isValidCOAPIType (BayerCommitmentLICOAPIType apiType)
	{
		boolean isValid = true;
		//String projectId = oDataType.getProjectDefinition();
		String wbsId = apiType.getParentCostObjectExternalKey();
		//String pmoId = oDataType.getPmOrder();
		String commitmentId = apiType.getCostObjectID();
		if (wbsId == null ||wbsId.equals("")||(
				commitmentId == null||commitmentId.equals("")))
			isValid = false;
		return isValid;
	}

	public List<BayerCommitmentLICOAPIType> getBayerCommitmentLICOAPITypesValid(List<BayerCommitmentLICOAPIType> apiTypes){

		BayerCommitmentLICOAPIRequestType request = new BayerCommitmentLICOAPIRequestType();
		for (int i = 0; i < apiTypes.size(); i++)
		{
			BayerCommitmentLICOAPIType apiType = apiTypes.get(i);
			if (isValidCOAPIType(apiType))
			{
				request.getBayerCommitmentLICOAPI().add(apiType);
			}
		}
		return request.getBayerCommitmentLICOAPI();
	}


	//Convert SAPACTODataType List to BayerDirectChargeCOAPIType List
	public List<BayerCostAccountsAPIType> getBayerCostAccountsAPITypesSample(List<SapPRLODataType> oDataTypes){

		BayerCostAccountsAPIRequestType request = new BayerCostAccountsAPIRequestType();
		Map<String, BayerCostAccountsAPIType> caMap = new HashMap<String, BayerCostAccountsAPIType>();
		for (int i = 0; i < oDataTypes.size(); i++)
		{
			SapPRLODataType oDataType = oDataTypes.get(i);
			BayerCostAccountsAPIType apiType = this.getBayerCostAccountsAPITypeSample(oDataType);
			if (!caMap.containsKey(apiType.getID()))
			{
				request.getBayerCostAccountsAPI().add(apiType);
				caMap.put(apiType.getID(), apiType);
			}
		}
		return request.getBayerCostAccountsAPI();
	}

	public BayerCostAccountsAPIType getBayerCostAccountsAPITypeSample(SapPRLODataType oDataType)
	{
		BayerCostAccountsAPIType apiType = new BayerCostAccountsAPIType();
		String projectId = oDataType.getProjectDefinition();
		apiType.setID(oDataType.getCostElement());
		apiType.setName(oDataType.getCostElementDesc());
		//apiType.setName(oDataType.getCostElement());
		return apiType;
	}

	//Convert SAPWBSODataType List to BayerWBSAPIType List
	public List<BayerCommitmentLIAPIType> getBayerCommitmentLIAPITypesSample(List<SapPRLODataType> oDataTypes){

		BayerCommitmentLIAPIRequestType request = new BayerCommitmentLIAPIRequestType();
		for (int i = 0; i < oDataTypes.size(); i++)
		{
			SapPRLODataType oDataType = oDataTypes.get(i);
			if (isValidODataTypeSample(oDataType))
			{
				BayerCommitmentLIAPIType apiType = this.getCommitmentLIAPITypeSample(oDataType);
				request.getBayerCommitmentLIAPI().add(apiType);
			}
		}
		return request.getBayerCommitmentLIAPI();
	}

	private boolean isValidODataTypeSample (SapPRLODataType oDataType)
	{
		boolean isValid = true;
		BayerCommitmentLIAPIType apiType = new BayerCommitmentLIAPIType();
		String projectId = oDataType.getProjectDefinition();
		String wbsId = oDataType.getWbsElement();
		String pmoId = oDataType.getPmOrder();
		String commitmentId = oDataType.getPurchReq();
		if (projectId == null ||projectId.equals("")||
				((wbsId == null ||wbsId.equals("")) 
						&& (pmoId==null||pmoId.equals("")))||
				commitmentId == null||commitmentId.equals(""))
			isValid = false;
		return isValid;
	}

	/* Convert SapWBSODataType Object to BayerWBSAPIType Object
	 * 
	 */
	public BayerCommitmentLIAPIType getCommitmentLIAPITypeSample(SapPRLODataType oDataType)
	{
		BayerCommitmentLIAPIType apiType = new BayerCommitmentLIAPIType();
		double obligo = 0.0;
		String projectId = oDataType.getProjectDefinition();
		String prhId = oDataType.getPurchReq()+"_"+oDataType.getPurchReqItem();

		String wbsElement = oDataType.getWbsElement();
		String pmoId = oDataType.getPmOrder();
		if (pmoId!=null&&!pmoId.equals(""))
			apiType.setCostObjectExternalKey(pmoId);
		else 
			apiType.setCostObjectExternalKey(oDataType.getWbsElement());

		//apiType.setCostObjectExternalKey(prhId);
		//apiType.setCostObjectHierarchyPathID(oDataType.getWbsElement());
		apiType.setCommitmentID(oDataType.getPurchReq());
		apiType.setSAPPurchasingDocumentNumberID(prhId);
		apiType.setSAPPurchasingDocumentLineItemNumber(oDataType.getPurOrdItem());
		apiType.setCostTransactionCurrency(oDataType.getPrComCost());
		apiType.setActualCostTransactionCurrency(oDataType.getPrActCost());
		apiType.setCostAccountID(oDataType.getCostElement());
		apiType.setCostAccountName(oDataType.getCostElementDesc());
		apiType.setCurrencyTransactionCode(oDataType.getPrDocCurr());
		apiType.setConversionRateCostObjectCurrency(oDataType.getExchangeRate());
		apiType.setTransactionDate(oDataType.getPrDate());
		apiType.setDeletionFlagID(oDataType.getDeleteInd());
		apiType.setSAPPurchaseRequisitionNumber(oDataType.getPurchReq());
		apiType.setSAPPurchaseRequisitionLineItemNumbe(oDataType.getPurchReqItem());
		apiType.setSAPWBSElement(oDataType.getWbsElement());
		obligo = apiType.getCostTransactionCurrency() - apiType.getActualCostTransactionCurrency();
		if (obligo < 0.0)
			obligo = 0.0;

		apiType.setObligo(obligo);
		return apiType;
	}

	public List<PolApiErrorDAO> getBayerCommitmentLIAPITypesInvalid(List<SapPRLODataType> oDataTypes){

		List<PolApiErrorDAO> errorList = new ArrayList<PolApiErrorDAO>();
		for (int i = 0; i < oDataTypes.size(); i++)
		{
			SapPRLODataType oDataType = oDataTypes.get(i);
			if (!isValidODataTypeSample(oDataType))
			{
				BayerCommitmentLIAPIType apiType = this.getCommitmentLIAPITypeSample(oDataType);
				PolApiErrorDAO errorDAO = this.getPolAPIErrorDAO(apiType);
				errorDAO.setStatus(GlobalConstants.STAGING_RECORD_STATUS_SKIPPED);
				errorList.add(errorDAO);
			}
		}
		return errorList;
	}

	//Convert SAPWBSODataType List to BayerWBSAPIType List
	public List<BayerCommitmentLIAPIType> getBayerCommitmentLIAPITypesSampleRaw(List<SapPRLODataType> oDataTypes){

		BayerCommitmentLIAPIRequestType request = new BayerCommitmentLIAPIRequestType();
		for (int i = 0; i < oDataTypes.size(); i++)
		{
			SapPRLODataType oDataType = oDataTypes.get(i);
			BayerCommitmentLIAPIType apiType = this.getCommitmentLIAPITypeSample(oDataType);
			request.getBayerCommitmentLIAPI().add(apiType);
		}
		return request.getBayerCommitmentLIAPI();
	}

	//	private int deleteLogs(String projectId, String sql)  throws SystemException {
	//		try{
	//			int counter = stgDBMgr.deleteBatch(projectId, sql);
	//			return counter;
	//		} catch(Exception e) {
	//			throw new SystemException (e);
	//		}
	//	}

	//Aug 2021: Phase 3 CR2 section
	//	private  List<BayerWBSReadAPIType> readParentCOs(String projectId) throws SystemException{
	//	
	//		List<BayerWBSReadAPIType> parentCOs = new ArrayList<BayerWBSReadAPIType>();
	//		try 
	//		{	
	//			//Read PM Order Data from EcoSys using PMOrder API
	//			Cookie session = null;
	//			String baseUri2 = GlobalConstants.EPC_REST_PROTOCOL + "://" + GlobalConstants.EPC_REST_SERVER + ":"
	//					+ GlobalConstants.EPC_REST_PORT + "/" + GlobalConstants.EPC_REST_BASEURI2;				
	//	
	//			HashMap<String, String> aMap = new HashMap<String, String>();
	//			aMap.put(GlobalConstants.EPC_PROJECT_PARAM, projectId);
	//			parentCOs = this.getBayerWBSReadAPITypes(session, baseUri2, aMap);
	//			return parentCOs;
	//	
	//		} catch(Exception e) {
	//			throw new SystemException(e);
	//		}
	//	}

	//Convert PMOrderAPIErrorDAO list to BayerPMOrderAPIType List
	//	private List<BayerCommitmentLIAPIType> getBayerCommitmentLIAPITypesFromError(List<PolApiErrorDAO> errorList){
	//	
	//		BayerCommitmentLIAPIRequestType request = new BayerCommitmentLIAPIRequestType();
	//		for (int i = 0; i < errorList.size(); i++)
	//		{
	//			PolApiErrorDAO errorDAO = errorList.get(i);
	//			BayerCommitmentLIAPIType apiType = this.getBayerCommitmentLIAPITypeFromError(errorDAO);
	//			request.getBayerCommitmentLIAPI().add(apiType);
	//		}
	//		return request.getBayerCommitmentLIAPI();
	//	}

	//	private Cookie requestCO(List<BayerCommitmentLICOAPIType> subList, Cookie session,
	//			String baseUri, String projectId, List<PolApiErrorDAO> errorList) throws SystemException {
	//	
	//		BayerCommitmentLICOAPIRequestType request = new BayerCommitmentLICOAPIRequestType();
	//		request.getBayerCommitmentLICOAPI().addAll(subList);
	//	
	//		COObjectFactory objectFactory = new COObjectFactory();
	//		JAXBElement<BayerCommitmentLICOAPIRequestType> requestWrapper = objectFactory.createBayerCommitmentLICOAPIRequest(request);
	//	
	//		ClientResponse response = epcRestMgr
	//				.postApplicationXmlAsApplicationXml(client, requestWrapper,
	//						baseUri, GlobalConstants.EPC_REST_IMPORT_SAP_PR_LNITM_CO,
	//						session);
	//	
	//		logger.debug(response);
	//		BayerCommitmentLICOAPIResultType result = epcRestMgr.responseToObject(response, BayerCommitmentLICOAPIResultType.class);
	//		if(session == null)
	//			session = epcRestMgr.getSessionCookie(response);
	//	
	//		if(!result.isSuccessFlag()){
	//			String errMsg="The interface failed to load any record due to data issues; please verify data.";
	//			if (result.getError() != null)
	//				errMsg=result.getError().toString();
	//			for(BayerCommitmentLICOAPIType pol: subList)
	//			{
	//				//convert to APIErrorDAO type
	//				PolApiErrorDAO statusMsg = this.getPolcoAPIErrorDAO(pol);
	//				statusMsg.setRootCostObjectID(projectId);
	//				statusMsg.setStatus(GlobalConstants.STAGING_RECORD_STATUS_FAILED);
	//				statusMsg.setErrorMsg(errMsg);
	//				errorList.add(statusMsg);
	//			}
	//			throw new SystemException(errMsg);
	//		} else {
	//	
	//			//List<StatusMessage> statusMsgList = new ArrayList<StatusMessage>();
	//			int i=0;
	//			for(COObjectResultType or : result.getObjectResult()) {
	//				BayerCommitmentLICOAPIType poLI = subList.get(i++);
	//				String polID = poLI.getCostObjectID();
	//	
	//				//convert to APIErrorDAO type
	//				PolApiErrorDAO statusMsg = this.getPolcoAPIErrorDAO(poLI);
	//				statusMsg.setRootCostObjectID(projectId);
	//	
	//				if(or.isSuccessFlag()) {
	//					logger.debug("UPDATE RESULT --> " + or.getInternalId());
	//					logger.debug("Record with External Key ID of : " + polID +" "+ GlobalConstants.STAGING_RECORD_STATUS_LOADED);
	//					statusMsg.setStatus(GlobalConstants.STAGING_RECORD_STATUS_LOADED);
	//	
	//				} else {
	//					String str = ((ElementNSImpl)or.getResultMessage().get(0).getMessage()).getTextContent();
	//					logger.error("ERROR --> " + or.getInternalId() + "|" + polID + "|" + or.isSuccessFlag() + "|" + str);	
	//	
	//					statusMsg.setStatus(GlobalConstants.STAGING_RECORD_STATUS_FAILED);
	//					statusMsg.setErrorMsg(str);				
	//				}
	//				errorList.add(statusMsg);				
	//			}
	//		}
	//		return session;
	//	}

	//	private void processStatusMessagesCA(List<PolcoApiErrorDAO> errorList, boolean isNew)  throws SystemException {
	//		try{
	//			//stgDBMgr.insertWbsBatch(this.wbsAPIErrorDAOList);
	//			if (isNew)
	//				stgDBMgr.insertPolcoBatch(errorList, GlobalConstants.EPC_POLCO_API_ERROR_BATCH_INSERT);
	//			else
	//				stgDBMgr.updatePolcoBatch(errorList, GlobalConstants.EPC_POLCO_API_ERROR_BATCH_UPDATE);
	//	
	//		} catch(Exception e) {
	//			throw new SystemException (e);
	//		}
	//	}

	//	private List<BayerCommitmentLICOAPIType> readSapDataCO(String projectId, String systemId) throws SystemException {
	//		Map<String, Map<String, Object>> dataRows = odataSvcMgr.readPRLineItems(projectId, systemId);
	//		Map<String, BayerCommitmentLICOAPIType> apiList = odataSvcMgr.mapPRLineItemCOForImport(dataRows);
	//		List<BayerCommitmentLICOAPIType> apiTypes = new ArrayList<BayerCommitmentLICOAPIType>();
	//		apiTypes.addAll(apiList.values());
	//		return apiTypes;
	//	}

	//	private List<BayerCommitmentLIAPIType> readProjectData() {
	//		return null;
	//	}

	//	private PolApiErrorDAO getPolcoAPIErrorDAO(BayerCommitmentLICOAPIType apiType)
	//	{
	//		PolApiErrorDAO apiError = new PolApiErrorDAO();
	//		apiError.setCostObjectExternalKey(apiType.getParentCostObjectExternalKey());
	//		//apiError.setCostObjectHierarchyPathID(apiType.getCostObjectHierarchyPathID());
	//		apiError.setCostObjectID(apiType.getCostObjectID());
	//		apiError.setExternalKey(apiType.getExternalKey());
	//		return apiError;
	//	}

	//Get ErrorList ID for reprocessed Error
	//	private List<PolApiErrorDAO> getErrorListWithId(List<PolApiErrorDAO> oldErrorList, List<PolApiErrorDAO> newErrorList){
	//		for (int i = 0; i < newErrorList.size(); i++)
	//		{
	//			long j = this.getErrorId(newErrorList.get(i).getCostObjectExternalKey(), 
	//					newErrorList.get(i).getCommitmentID(),
	//					oldErrorList);
	//			newErrorList.get(i).setId(j);;
	//		}
	//		return newErrorList;
	//	}

	//End Section Processing Cost Accounts


	//	private long getErrorId(String pathId, String commitId, List<PolApiErrorDAO> errorList){
	//	//Get ErrorID for reprocessed Error
	//		long id = 0;
	//		for (int i = 0; i < errorList.size(); i++)
	//		{
	//	
	//			if(errorList.get(i).getCostObjectExternalKey().equals(pathId)
	//					&&errorList.get(i).getCommitmentID().equals(commitId))
	//				id = errorList.get(i).getId();
	//		}
	//		return id;
	//	}

	//	private List<PolApiErrorDAO> processCommitmentLICOs( List<BayerCommitmentLICOAPIType> prjRecords,
	//			String projectId) throws SystemException{
	//	
	//		logger.debug("Importing PR Line Items to EPC as Cost Objects...");
	//	
	//		List<PolApiErrorDAO> retStatusMsgList = new ArrayList<PolApiErrorDAO>();
	//		try {
	//	
	//			if(prjRecords == null || prjRecords.size() == 0) {
	//				return retStatusMsgList;
	//			}
	//	
	//			//Prepare for the REST call
	//			String baseUri = GlobalConstants.EPC_REST_PROTOCOL + "://" + GlobalConstants.EPC_REST_SERVER + ":" + 
	//					GlobalConstants.EPC_REST_PORT + "/" + GlobalConstants.EPC_REST_BASEURI2;
	//	
	//			long total = prjRecords.size();
	//			Stopwatch timerBatch = new Stopwatch();
	//			Cookie session = null;
	//			for(int i = 0; i < prjRecords.size(); i += GlobalConstants.EPC_REST_BATCHSIZE) {
	//				int end = (prjRecords.size() < (i + GlobalConstants.EPC_REST_BATCHSIZE) ? prjRecords.size() : GlobalConstants.EPC_REST_BATCHSIZE + i);
	//				List<PolApiErrorDAO> statusMsgList = new ArrayList<PolApiErrorDAO>();
	//				timerBatch.start();
	//				session = this.requestCO(prjRecords.subList(i, end), 
	//						session, baseUri, projectId, statusMsgList);
	//				logger.info(end + " of " + total + " - elapsed time: " + timerBatch.stop().toString(ISOPeriodFormat.alternateExtended()));
	//				retStatusMsgList.addAll(statusMsgList);
	//			}
	//	
	//			//this.epcRestMgr.logout(client, baseUri, session);
	//	
	//		} catch(Exception e) {
	//			throw new SystemException(e);
	//		}
	//		logger.debug("Complete!");
	//		return retStatusMsgList;
	//	}

	//	private BayerCommitmentLIAPIType getBayerCommitmentLIAPITypeFromError(PolApiErrorDAO errorDAO)
	//	{
	//		BayerCommitmentLIAPIType apiType = new BayerCommitmentLIAPIType();
	//		apiType.setCommitmentID(errorDAO.getCommitmentID());
	//		apiType.setConversionRateCostObjectCurrency(errorDAO.getConversionRateCostObjectCurrency());
	//		apiType.setCostAccountID(errorDAO.getCostAccountID());
	//		apiType.setCostAccountName(errorDAO.getCostAccountName());
	//		apiType.setCostCostObjectCurrency(errorDAO.getCostCostObjectCurrency());
	//		apiType.setCostObjectExternalKey(errorDAO.getCostObjectExternalKey());
	//		//apiType.setCostObjectHierarchyPathID(errorDAO.getCostObjectHierarchyPathID());
	//		apiType.setCostObjectID(errorDAO.getCostObjectID());
	//		apiType.setCostTransactionCurrency(errorDAO.getCostTransactionCurrency());
	//		apiType.setActualCostTransactionCurrency(errorDAO.getActualCostTransactionCurrency());
	//		apiType.setObligo(errorDAO.getObligo());
	//		apiType.setCurrencyCostObjectCode(errorDAO.getCurrencyCostObjectCode());
	//		apiType.setCurrencyTransactionCode(errorDAO.getCurrencyTransactionCode());
	//		apiType.setDeletionFlagID(errorDAO.getDeletionFlagID());
	//		apiType.setExternalKey(errorDAO.getExternalKey());
	//		apiType.setSAPPurchaseRequisitionLineItemNumbe(errorDAO.getSAPPurchaseRequisitionLineItemNumbe());
	//		apiType.setSAPPurchaseRequisitionNumber(errorDAO.getSAPPurchaseRequisitionNumber());
	//		apiType.setSAPPurchasingDocumentLineItemNumber(errorDAO.getSAPPurchasingDocumentLineItemNumber());
	//		apiType.setSAPPurchasingDocumentNumberID(errorDAO.getSAPPurchasingDocumentNumberID());
	//		apiType.setSAPWBSElement(errorDAO.getSAPWBSElement());
	//		apiType.setTransactionDate(errorDAO.getTransactionDate());
	//		apiType.setVersionID(errorDAO.getVersionID());
	//		apiType.setPODistributionPCT(errorDAO.getPODistributionPCT());
	//		apiType.setPRQuantity(errorDAO.getPRQuantity());
	//		apiType.setUnitofMeasureID(errorDAO.getUnitofMeasureID());
	//		apiType.setSAPPurcharsingOrderSeqNumber(errorDAO.getSAPPurcharsingOrderSeqNumber());
	//		apiType.setSAPPRProcessingState(errorDAO.getSAPPRProcessingState());
	//		apiType.setSAPPRProcessingStatus(errorDAO.getSAPPRProcessingStatus());
	//		apiType.setSAPExchangeRate(errorDAO.getSAPExchangeRate());
	//		return apiType;
	//	}

	//	private boolean isValidCADataTypeSample (SapPOLODataType oDataType)
	//	{
	//		boolean isValid = false;
	//		BayerActualsAPIType apiType = new BayerActualsAPIType();
	//		String caId = oDataType.getCostElement();
	//		if (caId!=null || !caId.equals(""))
	//			isValid = true;
	//	
	//		return isValid;
	//	}

	//	private boolean isParentMissing (BayerCommitmentLIAPIType apiType,List<BayerWBSReadAPIType> parentList )
	//	{
	//		boolean isMissing = true;
	//		for(BayerWBSReadAPIType pco: parentList)
	//		{
	//			if (apiType.getCostObjectExternalKey().equalsIgnoreCase(pco.getExternalKey()))
	//				isMissing = false;
	//		}
	//		return isMissing;
	//	}

	//	private  List<BayerCommitmentLIAPIType> getValidParentCoLIs(List<BayerCommitmentLIAPIType> inputList, List<BayerCommitmentLIAPIType> missingList, List<BayerWBSReadAPIType> parentList){
	//
	//		List<BayerCommitmentLIAPIType> withParentCOs = new ArrayList<BayerCommitmentLIAPIType>();
	//		for(BayerCommitmentLIAPIType lico: inputList)
	//		{
	//			if (!this.isParentMissing(lico, parentList))
	//			{
	//				withParentCOs.add(lico);
	//			}
	//			else
	//			{
	//				//lico.setParentCostObjectExternalKey(defaultParent);
	//				missingList.add(lico);
	//			}
	//		}
	//		return withParentCOs;
	//	}

	//	private List<PolApiErrorDAO> processMissingCOLIs(List<BayerCommitmentLIAPIType> missingCOLIs, String projectId)
	//	{
	//		List<PolApiErrorDAO> errorList = new ArrayList<PolApiErrorDAO>();
	//		for(BayerCommitmentLIAPIType poLI: missingCOLIs)
	//		{
	//			logger.error("ERROR Missing Owner Cost Object in EcoSys --> Project ID: " + projectId + "|"
	//					+ "CostObjectExternalKey: " + poLI.getCostObjectExternalKey() + "|"
	//					+ "CommitmentID: " + poLI.getCommitmentID() + "|"
	//					+ "SAPPurchaseRequisitionLineItemNumbe: " + poLI.getSAPPurchaseRequisitionLineItemNumbe()+ "|"
	//					+ "CostAccountID: " + poLI.getCostAccountID()+ "|"
	//					+ "CostAccountName: " + poLI.getCostAccountName()+ "|"
	//					+ "CostObjectID: " + poLI.getCostObjectID()+ "|"
	//					+ "CostObjectName: " + poLI.getCostObjectName()+ "|"
	//					+ "CurrencyCostObjectCode: " + poLI.getCurrencyCostObjectCode() + "|"
	//					+ "CurrencyTransactionCode: " + poLI.getCurrencyTransactionCode() + "|"
	//					+ "DeletionFlagID: " + poLI.getDeletionFlagID()+ "|"
	//					+ "ExternalKey: " + poLI.getExternalKey() + "|"
	//					+ "FinalConfirmationID: " + poLI.getFinalConfirmationID()+ "|"
	//					+ "LineItemText: " + poLI.getLineItemText()+ "|"
	//					+ "Receiver: " + poLI.getReceiver() + "|"
	//					+ "Requestor: " + poLI.getRequestor() + "|"
	//					+ "SAPPRProcessingState: " + poLI.getSAPPRProcessingState()+ "|"
	//					+ "SAPPRProcessingStatus: " + poLI.getSAPPRProcessingStatus() + "|"
	//					+ "SAPPurcharsingOrderSeqNumber: " + poLI.getSAPPurcharsingOrderSeqNumber()+ "|"
	//					+ "SAPPurchaseRequisitionNumber: " + poLI.getSAPPurchaseRequisitionNumber()+ "|"
	//					+ "SAPPurchasingDocumentLineItemNumber: " + poLI.getSAPPurchasingDocumentLineItemNumber() + "|"
	//					+ "SAPPurchasingDocumentNumberID: " + poLI.getSAPPurchasingDocumentNumberID()+ "|"
	//					+ "SAPWBSElement: " + poLI.getSAPWBSElement() + "|"
	//					+ "TransactionExchangeRateSource: " + poLI.getTransactionExchangeRateSource()+ "|"
	//					+ "UnitofMeasureID: " + poLI.getUnitofMeasureID()+ "|" 
	//					+ "VersionID: " + poLI.getVersionID()+ "|"
	//					+ "ActualCostTransactionCurrency: " + poLI.getActualCostTransactionCurrency() + "|"
	//					+ "AlternateCostExternal: " + poLI.getAlternateCostExternal()+ "|"
	//					+ "ConversionRateCostObjectCurrency: " + poLI.getConversionRateCostObjectCurrency()+ "|"
	//					+ "CostCostObjectCurrency: " + poLI.getCostCostObjectCurrency()+ "|"
	//					+ "CostExternal: " + poLI.getCostExternal()+ "|"
	//					+ "CostTransactionCurrency: " + poLI.getCostTransactionCurrency()+ "|"
	//					+ "Obligo: " + poLI.getObligo() + "|"
	//					+ "PODistributionPCT: " + poLI.getPODistributionPCT()+ "|"
	//					+ "POQuantity: " + poLI.getPOQuantity()+ "|"
	//					+ "PRQuantity: " + poLI.getPRQuantity() + "|"
	//					+ "SAPExchangeRate: " + poLI.getSAPExchangeRate()+ "|"
	//					+ "SAPPRExchangeRate: " + poLI.getSAPPRExchangeRate()+ "|"
	//					+ "TransactionDate: " + poLI.getTransactionDate() + "|");	
	//
	//			String errMsg="The interface skipped to load PR Line Item for " + poLI.getCostObjectExternalKey() + " due to owner Cost Object missing in EcoSys; please verify data.";
	//			logger.error(errMsg);
	//
	//			PolApiErrorDAO statusMsg = this.getPolAPIErrorDAO(poLI);
	//			statusMsg.setRootCostObjectID(projectId);
	//			statusMsg.setStatus(GlobalConstants.STAGING_RECORD_STATUS_FAILED);
	//			statusMsg.setErrorMsg(errMsg);
	//			errorList.add(statusMsg);
	//		}
	//		return errorList;
	//	}

	//End Section Phase 3 CR02
}
