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
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import javax.ws.rs.core.Cookie;
import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.joda.time.format.ISOPeriodFormat;

import com.bayer.integration.odata.SapPMOODataType;
import com.bayer.integration.persistence.PmOrderApiErrorDAO;
import com.bayer.integration.persistence.WbsApiErrorDAO;
import com.bayer.integration.properties.GlobalConstants;
import com.bayer.integration.rest.actual.BayerActualsAPIType;
import com.bayer.integration.rest.calcwbs.BayerCalculateWBSIDAPIResultType;
import com.bayer.integration.rest.calcwbs.CalcWObjectFactory;
import com.bayer.integration.rest.log.IntegrationIssuesAPIRequestType;
import com.bayer.integration.rest.log.IntegrationIssuesAPIResultType;
import com.bayer.integration.rest.log.IntegrationIssuesAPIType;
import com.bayer.integration.rest.log.LogObjectFactory;
import com.bayer.integration.rest.pmorder2.BayerPMOrderV2APIRequestType;
import com.bayer.integration.rest.pmorder2.BayerPMOrderV2APIResultType;
import com.bayer.integration.rest.pmorder2.BayerPMOrderV2APIType;
import com.bayer.integration.rest.pmorder2.ObjectFactory;
import com.bayer.integration.rest.pmorder2.ObjectResultType;
import com.bayer.integration.rest.pmordertran.BayerPMOrderTranAPIRequestType;
import com.bayer.integration.rest.pmordertran.BayerPMOrderTranAPIResultType;
import com.bayer.integration.rest.pmordertran.BayerPMOrderTranAPIType;
import com.bayer.integration.rest.pmordertran.PMOTObjectFactory;
import com.bayer.integration.rest.pmordertran.PMOTObjectResultType;
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
import com.sun.jersey.api.client.ClientResponse;
import com.sun.org.apache.xerces.internal.dom.ElementNSImpl;

/**
 * @author vharman
 *
 */
public class PMOrderImportMgrImpl extends ImportManagerBase implements
ImportManager {

	/* (non-Javadoc)
	 * @see com.bayer.integration.service.ImportManager#importData()
	 */
	//@Override
	final String itgInterface = "PMO";
	List<IntegrationIssuesAPIType> itgIssueLog = new ArrayList<IntegrationIssuesAPIType>();
	//EpcIntegrationLsog epcLog = new EpcIntegrationLog(itgInterface);

	public int importData() {
		int retCode=GlobalConstants.IMPORT_SAP_PMO_SUCCESS;
		try{			
			if (!GlobalConstants.SKIP_SAP_PMO_INTERFACE) {
				if(GlobalConstants.DEBUGMODE)
					DebugBanner.outputBanner(this.getClass().toString());

				//Create Web Service Client
				if (client == null) 
					setClient(epcRestMgr.createClient(GlobalConstants.EPC_REST_USERNAME, GlobalConstants.EPC_REST_PASSWORD));

				boolean skipError = GlobalConstants.SKIP_LOG;
				boolean isLive = GlobalConstants.IS_LIVE_SAP;

				//Read Project Data from EcoSys using project API
				Cookie session = null;

				//ImportManagerHelper importHelper = new ImportManagerHelper();

				prjExpMgr.ExportData();
				List<BayerProjectAPIType> projectAPITypes = prjExpMgr.getBayerProjectAPITypes();
				DatatypeFactory dFactory = DatatypeFactory.newInstance();
				XMLGregorianCalendar currentDate = dFactory.newXMLGregorianCalendar(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

				//Loop through the project list
				for (int i = 0; i < projectAPITypes.size(); i++) 
				{
					itgIssueLog.clear();
					BayerProjectAPIType projectAPIType = projectAPITypes.get(i);
					String projectId = projectAPIType.getID();
					String sapProjectId = projectAPIType.getSapProjectId();
					GlobalConstants.EPC_PROJECT_ID_PROCESSED = projectId;
					String systemId = projectAPIType.getSAPSystemID();
					String masterProjectId = null;
					String projectCurrency = projectAPIType.getRootCostObjectCurrencyCode();
					if (projectAPIType.getParentCostObjectID()!=null && projectAPIType.getParentCostObjectID()!="")
						masterProjectId = projectAPIType.getParentCostObjectID();

					boolean isSub = false;
					if (projectAPIType.getProjectTypeID().equals(GlobalConstants.EPC_API_PROJECT_TYPE_SUB))
						isSub = true;

					List<PmOrderApiErrorDAO> errorDAOList = new ArrayList<PmOrderApiErrorDAO>();
					List<PmOrderApiErrorDAO> lastTimeFailedDAOList = new ArrayList<PmOrderApiErrorDAO>();
					List<BayerPMOrderV2APIType> currentList = new ArrayList<BayerPMOrderV2APIType>();
					List<BayerPMOrderV2APIType> inputList = new ArrayList<BayerPMOrderV2APIType>();
					List<PmOrderApiErrorDAO> invalidRecords = new ArrayList<PmOrderApiErrorDAO>();
					List<SapPMOODataType> inputListSample = new ArrayList<SapPMOODataType>();
					List<BayerPMOrderV2APIType> validRecords = new ArrayList<BayerPMOrderV2APIType>();
					List<BayerPMOrderV2APIType> movedRecords = new ArrayList<BayerPMOrderV2APIType>();
					List<BayerPMOrderV2APIType> moved2Records = new ArrayList<BayerPMOrderV2APIType>();
					List<BayerPMOrderTranAPIType> validTranRecords = new ArrayList<BayerPMOrderTranAPIType>();

					
					//Phase IV - CR02
					//List<BayerPMOrderV2APIType> validRecords = new ArrayList<BayerPMOrderV2APIType>();
					
					if (isSub)
					{
						currentList = this.readPMOrders(masterProjectId);
						//currentParentCOs = this.readParentCOs(masterProjectId);
					}
					else
					{
						currentList = this.readPMOrders(projectId);
						//currentParentCOs = this.readParentCOs(projectId);
					}
					// Comment out for Odata Test
					//Retrieve last run failed records and process
					try
					{
						//OData Processing Section
						//read PMOrder data from SAP and convert them to EcoSys API payload
						logger.debug("Reading PMOrder data from SAP Input for Project: "+ projectId);
						if (isLive)
						{
							inputList = readSapData(sapProjectId, systemId); 
							invalidRecords = this.getSapInputInvalid(inputList);
							validRecords = this.getSapInputValid(inputList, currentDate, projectCurrency, sapProjectId);							
						}
						else
						{
							//ImportHelper Test Section
							inputListSample = importHelper.getSapPMOTypesSample();
							invalidRecords = this.getBayerPMOrderV2APITypesInvalid(inputListSample);
							validRecords = this.getBayerPMOrderV2APITypesValid(inputListSample);
						}

						//validRecords = this.getValidParentCOs(validRecords, noParentRecords, currentParentCOs);
						//validRecords = hasParentCostObject(validInputRecords, projectId);
						validTranRecords = this.getPMOTranValid(validRecords, currentDate);

						//Process Invalid Records
						logger.debug("Processing Invalid Records, If Any for Project: " + projectId);
						if (invalidRecords.size()>0 && !skipError)
							this.processStatusMessages(invalidRecords, true);
						logger.debug("Processing Invalid Records Completed for Project: "+ projectId);	

						//Process Moved Back Records
						moved2Records = this.getReMovedPMOrders(currentList, validRecords, currentDate);

						//Process Moved Records
						movedRecords = this.getMovedPMOrders(currentList, validRecords, currentDate);	

						if (validRecords.size()>0 && movedRecords.size()>0 )
							validRecords = this.getUpdatedPMOrders(movedRecords, validRecords);

						if (validRecords.size()>0 || movedRecords.size()>0 || moved2Records.size()>0)
						{

							//Pre Process for the Project
							logger.debug("Trigger Action Batch to Pre Process PM Order for Project: "+ projectId);
							if (isSub)
								this.preProcess(masterProjectId);
							else
								this.preProcess(projectId);
							logger.debug("Trigger Action Batch to Pre Process PM Order completed for Project: "+ projectId);

							if (moved2Records.size()>0)
							{						
								if (isSub)
									errorDAOList = processProjects(moved2Records, masterProjectId);
								else
									errorDAOList = processProjects(moved2Records, projectId);
								//Process Status Messages
								if (!skipError)
								{
									logger.debug("Processing Moved Back PM Order ERR Messages, If Any for Project: " + projectId);
									//this.processStatusMessages(errorDAOList, true);
									logger.debug("Processing ERR Message Completed for Project: "+ projectId);
								}
							}

							if (movedRecords.size()>0)
							{						
								if (isSub)
									errorDAOList = processProjects(movedRecords, masterProjectId);
								else
									errorDAOList = processProjects(movedRecords, projectId);
								//Process Status Messages
								if (!skipError)
								{
									logger.debug("Processing Moved PM Order ERR Messages, If Any for Project: " + projectId);
									//this.processStatusMessages(errorDAOList, true);
									logger.debug("Processing ERR Message Completed for Project: "+ projectId);
								}
							}

							if (moved2Records.size()>0)
							{						
								moved2Records = this.getReMovedPMOrdersActivated(moved2Records);
								if (isSub)
									errorDAOList = processProjects(moved2Records, masterProjectId);
								else
									errorDAOList = processProjects(moved2Records, projectId);
								//Process Status Messages
								if (!skipError)
								{
									logger.debug("Processing Moved Back PM Order Activating ERR Messages, If Any for Project: " + projectId);
									//this.processStatusMessages(errorDAOList, true);
									logger.debug("Processing ERR Message Completed for Project: "+ projectId);
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
									logger.debug("Processing ERR Messages, If Any for Project: " + projectId);
									//this.processStatusMessages(errorDAOList, true);
									logger.debug("Processing ERR Message Completed for Project: "+ projectId);
								}

								errorDAOList = processPMOTran(validTranRecords, projectId);

								//Process Status Messages
								if (!skipError)
								{
									logger.debug("Processing ERR Messages, If Any for Project: " + projectId);
									//this.processStatusMessages(errorDAOList, true);
									logger.debug("Processing ERR Message Completed for Project: "+ projectId);
								}
							}

							//Aug 2021 Phase 3 CR2: Process PM Order with Missing Parent - Disabled
							/*
							 * if (noParentRecords.size()>0) {
							 * 
							 * noParentTranRecords = this.getPMOTranValid(noParentRecords, currentDate);
							 * 
							 * if (this.isDefaultParentMissing(defaultParent, currentParentCOs)) {
							 * BayerWBSAPIType apiType = new BayerWBSAPIType(); if (isSub) apiType =
							 * this.getDefaultWBS(masterProjectId, projectId, projectCurrency,
							 * defaultParent); else apiType = this.getDefaultWBS(masterProjectId, "",
							 * projectCurrency, defaultParent); defaultParentCOs.add(apiType);
							 * logger.debug("Creating default WBS Parent for Project:" + projectId);
							 * errorWbsDAOList = this.processWBS(defaultParentCOs, masterProjectId);
							 * logger.debug("Creating default WBS Parent Completed for Project:" +
							 * projectId); }
							 * 
							 * 
							 * logger.
							 * debug("Processing Missing Parent PM Order Records in EcoSys for Project: " +
							 * projectId); errorDAOList = this.processMissingParentCOs(noParentRecords,
							 * projectId); logger.
							 * debug("Processing Missing Parent PM Order Records Completed for Project: "+
							 * projectId);
							 * 
							 * //errorDAOList = processPMOTran(noParentTranRecords, projectId); }
							 */
							
							// call method to process issues to EPC
							logger.debug("posting " + itgIssueLog.size() + " " +itgInterface+" issues to EPC");
							if (itgIssueLog.size()>0)
								processIssueLog(itgIssueLog);
							
							
							//Post Process for the Project
							logger.debug("Trigger Action Batch to Post Process PM Order for Project: "+ projectId);
							if (isSub)
								this.postProcess(masterProjectId);
							else
								this.postProcess(projectId);
							logger.debug("Trigger Action Batch to Post Process PM Order completed for Project: "+ projectId);
						}

					}
					catch(SystemException se) {
						logger.error("2005 -- PMOrder Import Failed for Project: " + GlobalConstants.EPC_PROJECT_ID_PROCESSED, se);
						retCode=GlobalConstants.IMPORT_SAP_PMO_FAILED;
						continue;
					}
				}


			} else {
				logger.info("Skipped PMOrder Import Interface. Change the skip property to 'false'");				
			}		
		}
		catch(SystemException se) {			
			logger.error("2005 -- PMOrder Import Failed", se);			
			retCode=GlobalConstants.IMPORT_SAP_PMO_FAILED;
		}catch(Exception e) {
			logger.error("2005 -- PMOrder Import Failed", e);			
			retCode=GlobalConstants.IMPORT_SAP_PMO_FAILED;
		}

		if (retCode==GlobalConstants.IMPORT_SAP_PMO_SUCCESS)
			logger.debug("2000 -- PMOrder Import Completed Successfully");

		return retCode;
	}


	private List<BayerPMOrderV2APIType> readProjectData() {
		return null;
	}

	private List<PmOrderApiErrorDAO> processProjects( List<BayerPMOrderV2APIType> prjRecords, String projectId) throws SystemException{

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



	private Cookie request(List<BayerPMOrderV2APIType> subList,
			Cookie session, String baseUri, String projectId,  List<PmOrderApiErrorDAO> errorList) throws SystemException {

		BayerPMOrderV2APIRequestType request = new BayerPMOrderV2APIRequestType();
		request.getBayerPMOrderV2API().addAll(subList);

		ObjectFactory objectFactory = new ObjectFactory();
		JAXBElement<BayerPMOrderV2APIRequestType> requestWrapper = objectFactory.createBayerPMOrderV2APIRequest(request);
		HashMap<String, String> prjMap = new HashMap<String, String>();
		prjMap.put(GlobalConstants.EPC_PROJECT_PARAM, projectId);
		ClientResponse response = epcRestMgr
				.postApplicationXmlAsApplicationXml(client, requestWrapper,
						baseUri, GlobalConstants.EPC_REST_IMPORT_SAP_PMO_V2,
						session, prjMap);

		logger.debug(response);
		BayerPMOrderV2APIResultType result = epcRestMgr.responseToObject(response, BayerPMOrderV2APIResultType.class);
		if(session == null)
			session = epcRestMgr.getSessionCookie(response);

		if(!result.isSuccessFlag()){
			String errMsg="The interface failed to load any record due to data issues; please verify data.";
			if (result.getError() != null)
				errMsg=result.getError().toString();
			for(BayerPMOrderV2APIType pmorder: subList)
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
				BayerPMOrderV2APIType pmorder = subList.get(i++);
				String pmorderID = pmorder.getCostObjectID();

				//convert to APIErrorDAO type
				PmOrderApiErrorDAO statusMsg = this.getPMOrderAPIErrorDAO(pmorder);
				statusMsg.setRootCostObjectID(projectId);

				if(or.isSuccessFlag()) {
					logger.debug("UPDATE RESULT --> " + or.getInternalId());
					logger.debug("Record with External Key ID of : " + pmorderID +" "+ GlobalConstants.STAGING_RECORD_STATUS_LOADED);

					statusMsg.setStatus(GlobalConstants.STAGING_RECORD_STATUS_LOADED);
				} else {
					//String str = ((ElementNSImpl)or.getResultMessage().get(0).getMessage()).getTextContent();
					String str = or.getResultMessage().get(0).getMessage();
					logger.error("ERROR --> " + or.getInternalId() + "|" + pmorderID + "|" 	
							+ "COParentChangedID: " + pmorder.getCOParentChangedID() + "|"
							+ "CostObjectID: " + pmorder.getCostObjectID() + "|"
							+ "CostObjectInternalID: " + pmorder.getCostObjectInternalID() + "|"
							+ "CostObjectName: " + pmorder.getCostObjectName() + "|"
							+ "CostObjectStatus: " + pmorder.getCostObjectStatus() + "|"
							+ "CostObjectTypeName: " + pmorder.getCostObjectTypeName() + "|"
							+ "ExternalKey: " + pmorder.getExternalKey() + "|"
							+ "ParentCostObjectExternalKey: " + pmorder.getParentCostObjectExternalKey()+ "|"
							+ "PmStatus: " + pmorder.getPmStatus() + "|"
							+ "ProjectPathID: " + pmorder.getProjectPathID() + "|"
							+ "RootCostObjectExternalKey: " + pmorder.getRootCostObjectExternalKey() + "|"
							+ "RootCostObjectID: " + pmorder.getRootCostObjectID() + "|"
							+ "SAPWBSElement: " + pmorder.getSAPWBSElement() + "|"
							+ "PMActualCost: " + pmorder.getPMActualCost() + "|"
							+ "PMEstimatedCost: " + pmorder.getPMEstimatedCost() + "|"
							+ "ParentCOChangeDate: " + pmorder.getParentCOChangeDate() + "|"
							+ "PMOrderStgDate: " + pmorder.getPMOrderStgDate() + "|"			
							+ "CostObjectCurrencyCode: " + pmorder.getCostObjectCurrencyCode()+ "|"		
							+ or.isSuccessFlag() + "|" + str);
					statusMsg.setStatus(GlobalConstants.STAGING_RECORD_STATUS_FAILED);
					statusMsg.setErrorMsg(str);
					//itgIssueLog.add(getIntegrationIssuesAPIType("", str, projectId, pmorder.getExternalKey()));
					if (or.getResultMessage().get(0).getMessage().equalsIgnoreCase("Object was ignored due to defined filter"))
							itgIssueLog.add(getIntegrationIssuesAPIType("","PM Order "+pmorder.getExternalKey() + " does not have a valid parent WBS", projectId, pmorder.getExternalKey()));
				}
				//statusMsgList.add(statusMsg);
				errorList.add(statusMsg);			
			}
		}
		return session;
	}

	private List<PmOrderApiErrorDAO> processMissingParentCOs(List<BayerPMOrderV2APIType> missingParentCOs, String projectId)
	{
		List<PmOrderApiErrorDAO> errorList = new ArrayList<PmOrderApiErrorDAO>();
		for(BayerPMOrderV2APIType pmorder: missingParentCOs)
		{

			logger.error("ERROR Missing Parent WBS in EcoSys --> PM Order: " +  pmorder.getCostObjectID() + "|"
					+ "Project ID: " + projectId + "|" 	
					+ "Parent WBS: " + pmorder.getParentCostObjectExternalKey()+ "|"
					+ "COParentChangedID: " + pmorder.getCOParentChangedID() + "|"
					+ "CostObjectID: " + pmorder.getCostObjectID() + "|"
					+ "CostObjectInternalID: " + pmorder.getCostObjectInternalID() + "|"
					+ "CostObjectName: " + pmorder.getCostObjectName() + "|"
					+ "CostObjectStatus: " + pmorder.getCostObjectStatus() + "|"
					+ "CostObjectTypeName: " + pmorder.getCostObjectTypeName() + "|"
					+ "ExternalKey: " + pmorder.getExternalKey() + "|"
					+ "ParentCostObjectExternalKey: " + pmorder.getParentCostObjectExternalKey()+ "|"
					+ "PmStatus: " + pmorder.getPmStatus() + "|"
					+ "ProjectPathID: " + pmorder.getProjectPathID() + "|"
					+ "RootCostObjectExternalKey: " + pmorder.getRootCostObjectExternalKey() + "|"
					+ "RootCostObjectID: " + pmorder.getRootCostObjectID() + "|"
					+ "SAPWBSElement: " + pmorder.getSAPWBSElement() + "|"
					+ "PMActualCost: " + pmorder.getPMActualCost() + "|"
					+ "PMEstimatedCost: " + pmorder.getPMEstimatedCost() + "|"
					+ "ParentCOChangeDate: " + pmorder.getParentCOChangeDate() + "|"
					+ "PMOrderStgDate: " + pmorder.getPMOrderStgDate() + "|"			
					+ "CostObjectCurrencyCode: " + pmorder.getCostObjectCurrencyCode());

			String errMsg="The interface skipped to load PM Order " + pmorder.getCostObjectID() + " due to parent WBS missing in EcoSys; please verify data.";
			logger.error(errMsg);

			PmOrderApiErrorDAO statusMsg = this.getPMOrderAPIErrorDAO(pmorder);
			statusMsg.setRootCostObjectID(projectId);
			statusMsg.setStatus(GlobalConstants.STAGING_RECORD_STATUS_FAILED);
			statusMsg.setErrorMsg(errMsg);
			errorList.add(statusMsg);
		}
		return errorList;
	}


	//Convert SAPWBSODataType List to BayerWBSAPIType List
	public List<BayerPMOrderV2APIType> getBayerPMOrderV2APITypesValid(List<SapPMOODataType> oDataTypes){

		BayerPMOrderV2APIRequestType request = new BayerPMOrderV2APIRequestType();
		for (int i = 0; i < oDataTypes.size(); i++)
		{
			SapPMOODataType oDataType = oDataTypes.get(i);
			if (isValidODataType(oDataType))
			{
				BayerPMOrderV2APIType apiType = this.getPMOrderAPIType(oDataType);
				request.getBayerPMOrderV2API().add(apiType);
			}
		}
		return request.getBayerPMOrderV2API();
	}


	public List<PmOrderApiErrorDAO> getBayerPMOrderV2APITypesInvalid(List<SapPMOODataType> oDataTypes){

		List<PmOrderApiErrorDAO> errorList = new ArrayList<PmOrderApiErrorDAO>();
		for (int i = 0; i < oDataTypes.size(); i++)
		{
			SapPMOODataType oDataType = oDataTypes.get(i);
			if (!isValidODataType(oDataType))
			{
				BayerPMOrderV2APIType apiType = this.getPMOrderAPIType(oDataType);
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
		BayerPMOrderV2APIType apiType = new BayerPMOrderV2APIType();
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
	public BayerPMOrderV2APIType getPMOrderAPIType(SapPMOODataType oDataType)
	{
		BayerPMOrderV2APIType apiType = new BayerPMOrderV2APIType();
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

	private PmOrderApiErrorDAO getPMOrderAPIErrorDAO(BayerPMOrderV2APIType apiType)
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

	//Convert PMOrderAPIErrorDAO list to BayerPMOrderV2APIType List
	private List<BayerPMOrderV2APIType> getBayerPMOrderV2APITypesFromError(List<PmOrderApiErrorDAO> errorList){

		BayerPMOrderV2APIRequestType request = new BayerPMOrderV2APIRequestType();
		for (int i = 0; i < errorList.size(); i++)
		{
			PmOrderApiErrorDAO errorDAO = errorList.get(i);
			BayerPMOrderV2APIType apiType = this.getBayerPMOrderV2APITypeFromError(errorDAO);
			request.getBayerPMOrderV2API().add(apiType);
		}
		return request.getBayerPMOrderV2API();
	}


	private BayerPMOrderV2APIType getBayerPMOrderV2APITypeFromError(PmOrderApiErrorDAO errorDAO)
	{
		BayerPMOrderV2APIType apiType = new BayerPMOrderV2APIType();
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

	private  List<BayerPMOrderV2APIType> readPMOrders(String projectId) throws SystemException{

		List<BayerPMOrderV2APIType> bayerPMOrders = new ArrayList<BayerPMOrderV2APIType>();
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

	private List<BayerPMOrderV2APIType> getPMOrderAPITypes(Cookie session, String baseUri, HashMap<String, String> pmorderMap) throws SystemException {

		ClientResponse response = epcRestMgr
				.getAsApplicationXml(client, baseUri,
						GlobalConstants.EPC_REST_IMPORT_SAP_PMO_V2
						,session,pmorderMap);

		logger.debug(response);
		BayerPMOrderV2APIResultType result = epcRestMgr.responseToObject(response, BayerPMOrderV2APIResultType.class);
		List<BayerPMOrderV2APIType> pmorderAPITypes = result.getBayerPMOrderV2API();
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

	private  List<BayerPMOrderV2APIType> getMovedPMOrders(List<BayerPMOrderV2APIType> currentList, List<BayerPMOrderV2APIType> newList, XMLGregorianCalendar currentDate){

		List<BayerPMOrderV2APIType> movedPMOrders = new ArrayList<BayerPMOrderV2APIType>();
		for(BayerPMOrderV2APIType pmorder: currentList)
		{
			if (isMoved(pmorder, newList)==true)
			{
				if (!isReMoved(pmorder,newList))
				{
					pmorder.setCOParentChangedID(GlobalConstants.EPC_API_ERROR_FLAG_Y);
					pmorder.setParentCOChangeDate(currentDate);
					pmorder.setExternalKey(pmorder.getExternalKey() + GlobalConstants.EPC_CO_Changed + currentDate.toString());
					pmorder.setNewlyRetiredID(GlobalConstants.EPC_API_ERROR_FLAG_Y);
					//pmorder.setParentCostObjectExternalKey("A00NC-003247-CXENGFEL3EX");
					movedPMOrders.add(pmorder);
				}
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


	private  List<BayerPMOrderV2APIType> getUpdatedPMOrders(List<BayerPMOrderV2APIType> movedList, List<BayerPMOrderV2APIType> newList){

		List<BayerPMOrderV2APIType> updatedPMOrders = new ArrayList<BayerPMOrderV2APIType>();
		for(BayerPMOrderV2APIType pmorder: newList)
		{
			pmorder = this.getPmoUpdated(pmorder, movedList);
			updatedPMOrders.add(pmorder);
		}
		return updatedPMOrders;
	}

	private  List<BayerPMOrderV2APIType> getReMovedPMOrders(List<BayerPMOrderV2APIType> currentList, List<BayerPMOrderV2APIType> newList, XMLGregorianCalendar currentDate){

		List<BayerPMOrderV2APIType> movedPMOrders = new ArrayList<BayerPMOrderV2APIType>();
		for(BayerPMOrderV2APIType pmorder: currentList)
		{
			if (isReMoved(pmorder, newList)==true)
			{
				pmorder.setCOParentChangedID(GlobalConstants.EPC_API_ERROR_FLAG_N);
				pmorder.setParentCOChangeDate(null);
				pmorder.setExternalKey(pmorder.getExternalKey() + GlobalConstants.EPC_CO_RE_Changed);
				//pmorder.setParentCostObjectExternalKey("A00NC-003247-CXENGFEL3EX");
				movedPMOrders.add(pmorder);
			}
		}
		return movedPMOrders;
	}

	private  List<BayerPMOrderV2APIType> getReMovedPMOrdersActivated(List<BayerPMOrderV2APIType> reMovedList){

		List<BayerPMOrderV2APIType> movedPMOrders = new ArrayList<BayerPMOrderV2APIType>();
		for(BayerPMOrderV2APIType pmorder: reMovedList)
		{
			pmorder.setCOParentChangedID(GlobalConstants.EPC_API_ERROR_FLAG_N);
			pmorder.setParentCOChangeDate(null);
			//String exKey = pmorder.getExternalKey();
			pmorder.setExternalKey(pmorder.getCostObjectID());
			//pmorder.setParentCostObjectExternalKey("A00NC-003247-CXENGFEL3EX");
			movedPMOrders.add(pmorder);
		}
		return movedPMOrders;
	}

	private boolean isMoved (BayerPMOrderV2APIType apiType,List<BayerPMOrderV2APIType> newList )
	{
		boolean isMoved = false;
		for(BayerPMOrderV2APIType pmorder: newList)
		{
			if (pmorder.getExternalKey().equalsIgnoreCase(apiType.getExternalKey())
					&& !pmorder.getParentCostObjectExternalKey().equalsIgnoreCase(apiType.getParentCostObjectExternalKey()))
				isMoved = true;
		}
		return isMoved;
	}

	private BayerPMOrderV2APIType getPmoUpdated (BayerPMOrderV2APIType apiType,List<BayerPMOrderV2APIType> movedList )
	{
		BayerPMOrderV2APIType pmoOld = apiType;
		for(BayerPMOrderV2APIType pmorder: movedList)
		{
			String extKey = pmorder.getExternalKey().replaceFirst(GlobalConstants.EPC_CO_Changed, "").trim();
			if (extKey.contains(apiType.getExternalKey())
					&& !pmorder.getParentCostObjectExternalKey().equalsIgnoreCase(apiType.getParentCostObjectExternalKey()))
			{ if(pmorder.getCostControlLevelID()!=null)
				pmoOld.setCostControlLevelID(pmorder.getCostControlLevelID());
			}
		}
		return pmoOld;
	}

	private boolean isReMoved (BayerPMOrderV2APIType apiType,List<BayerPMOrderV2APIType> newList )
	{
		boolean isMoved = false;
		for(BayerPMOrderV2APIType pmorder: newList)
		{
			if ((apiType.getExternalKey().contains(pmorder.getExternalKey()+GlobalConstants.EPC_CO_Changed))
					&& pmorder.getParentCostObjectExternalKey().equalsIgnoreCase(apiType.getParentCostObjectExternalKey()))
				isMoved = true;
		}
		return isMoved;
	}

	private List<BayerPMOrderV2APIType> readSapData(String projectId, String systemId) throws SystemException {
		Map<String, Map<String, Object>> dataRows = odataSvcMgr.readPMOrders(projectId, systemId);
		List<BayerPMOrderV2APIType> apiTypes = new ArrayList<BayerPMOrderV2APIType>();
		if (dataRows!=null)
		{
			Map<String, BayerPMOrderV2APIType> apiList = odataSvcMgr.mapPMOrderV2ForImport(dataRows);
			apiTypes.addAll(apiList.values());
		}
		return apiTypes;
	}

	private List<BayerPMOrderV2APIType> updateCOCurrency(String projectCurrency, List<BayerPMOrderV2APIType> inputList) throws SystemException {
		List<BayerPMOrderV2APIType> apiTypes = new ArrayList<BayerPMOrderV2APIType>();
		for(BayerPMOrderV2APIType apiType : inputList) 
		{
			apiType.setCostObjectCurrencyCode(projectCurrency);
			apiTypes.add(apiType);
		}
		return apiTypes;
	}

	//	private boolean parentExists (/*BayerWBSReadAPIType ecosysWbs, */BayerPMOrderV2APIType apiType) {
	//		List<BayerWBSReadAPIType> ecosysWbs = new ArrayList<>();
	//		try {
	//			ecosysWbs = this.readParentCOs("A00NC-003247");
	//			logger.debug("successfully read " + ecosysWbs.size() + " WBS cost objects");
	//			List<String> parentIds =
	//					ecosysWbs.stream()
	//					.filter(t -> t.getCostObjectTypeName().equalsIgnoreCase("WBS"))
	//					.filter(t -> t.getExternalKey().equalsIgnoreCase(apiType.getParentCostObjectExternalKey()))
	//					.map(t -> t.getParentCostObjectHierarchyPathID())
	//					.collect(Collectors.toList());
	//
	//			if (parentIds.size()==0) {
	//				logger.debug("No parent WBS found - record will not be imported");
	//				return false;
	//			} else
	//				logger.debug("Parent WBS found - PM order will be checked for validity");
	//
	//		} catch (SystemException e) {
	//			e.printStackTrace();
	//		}
	//		return true;
	//	}

	private boolean isValidAPIType (BayerPMOrderV2APIType apiType)
	{
		// original isValid checks carried out only if WBS parent exists
		boolean isValid = true;
		String pmoId = apiType.getCostObjectID();
		String parentId = apiType.getParentCostObjectExternalKey();

		if (	pmoId == null || pmoId.equals("")||
				parentId == null||parentId.equals(""))
			isValid = false;
		return isValid;
	}


	public List<PmOrderApiErrorDAO> getSapInputInvalid(List<BayerPMOrderV2APIType> apiTypes){

		List<PmOrderApiErrorDAO> errorList = new ArrayList<PmOrderApiErrorDAO>();
		for (int i = 0; i < apiTypes.size(); i++)
		{
			BayerPMOrderV2APIType apiType = apiTypes.get(i);
			if (!isValidAPIType(apiType))
			{
				PmOrderApiErrorDAO errorDAO = this.getPMOrderAPIErrorDAO(apiType);
				errorDAO.setStatus(GlobalConstants.STAGING_RECORD_STATUS_FAILED);
				errorList.add(errorDAO);
			}
		}
		return errorList;
	}
	public List<BayerPMOrderV2APIType> getSapInputValid(List<BayerPMOrderV2APIType> apiTypes, XMLGregorianCalendar currentDate, String projectCurrency, String project){

		BayerPMOrderV2APIRequestType request = new BayerPMOrderV2APIRequestType();
		List<BayerWBSReadAPIType> ecosysWbs = new ArrayList<>();

		try {
			logger.debug("reading wbs elements for project: " + project);
			ecosysWbs = this.readEPCCostObjects(project, "WBS");
			logger.debug("successfully read " + ecosysWbs.size() + " WBS cost objects");    	

		} catch (SystemException e) {
			logger.error("error calling the read wbs API");
			e.printStackTrace();
		}

		logger.debug("checking records for valid WBS parent");
		List<BayerPMOrderV2APIType> apiTypes2 = new ArrayList<BayerPMOrderV2APIType>();
		for (BayerPMOrderV2APIType apiType : apiTypes) {
			try {
				String hpid =ecosysWbs.stream()
						.filter(t -> t.getExternalKey().equalsIgnoreCase(apiType.getParentCostObjectExternalKey()))
						.findFirst().get().getHierarchyPathID();
				apiTypes2.add(apiType);
			}
			catch (NoSuchElementException e) {
				logger.error("PM ORDER : MISSING PARENT : ExternalKey: "+apiType.getExternalKey()+
						" --> Parent ExternalKey: " + apiType.getParentCostObjectExternalKey());
				//String logErrorId = "MissingParent";
				//String logDescription = "PM Order "+apiType.getExternalKey() + " does not have a valid parent WBS";				
				//String logComment = "Missing Parent - "+ apiType.getParentCostObjectExternalKey();
				//epcLog.processIssueLog(logErrorId, logDescription, logComment);
				itgIssueLog.add(getIntegrationIssuesAPIType("",
						"PM Order "+apiType.getExternalKey() + " does not have a valid parent WBS", project, apiType.getExternalKey()));				
				continue;
			}
		}
		logger.debug("Records checked: "+ apiTypes.size()+" || Parents found: "+ apiTypes2.size());
		for (int i = 0; i < apiTypes2.size(); i++)
		{
			BayerPMOrderV2APIType apiType2 = apiTypes2.get(i);
			if (isValidAPIType(apiType2))

			{
				apiType2.setPMOrderStgDate(currentDate);
				// Set currency      ----------------------
				apiType2.setCostObjectCurrencyCode(projectCurrency);
				//-----------------------------------------

				request.getBayerPMOrderV2API().add(apiType2);
			}
		}
		return request.getBayerPMOrderV2API();
	}
	private IntegrationIssuesAPIType getIntegrationIssuesAPIType(String logErrorId, String logDescription, String logComment, String externalKey) {
		IntegrationIssuesAPIType logEntry = new IntegrationIssuesAPIType();
		logEntry.setIntegrationLogID(itgInterface);
		//logEntry.setIntegrationLogID(itgInterface+"."+logErrorId);
		logEntry.setDescription(logDescription);
		logEntry.setComment(logComment);
		logEntry.getExternalKey();
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
		return session;	
	}	

	public List<BayerPMOrderTranAPIType> getPMOTranValid(List<BayerPMOrderV2APIType> apiTypes, XMLGregorianCalendar currentDate){

		BayerPMOrderTranAPIRequestType request = new BayerPMOrderTranAPIRequestType();
		for (int i = 0; i < apiTypes.size(); i++)
		{
			BayerPMOrderV2APIType apiType = apiTypes.get(i);
			BayerPMOrderTranAPIType apiTranType = this.getPMOTranValid(apiType, currentDate);
			request.getBayerPMOrderTranAPI().add(apiTranType);
		}
		return request.getBayerPMOrderTranAPI();
	}

	public BayerPMOrderTranAPIType getPMOTranValid(BayerPMOrderV2APIType apiType, XMLGregorianCalendar currentDate){

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
					logger.error("ERROR --> " + or.getInternalId() + "|" + pmorderID + "|" 
							+ "CostObjectExternalKey: " + pmorder.getCostObjectExternalKey() + "|"
							+ "CostObjectID: " + pmorder.getCostObjectID() + "|"
							+ "CostObjectName: " + pmorder.getCostObjectName() + "|"
							+ "CurrencyCostObjectCode: " + pmorder.getCurrencyCostObjectCode()+ "|"
							+ "CurrencyTransactionCode: " + pmorder.getCurrencyTransactionCode() + "|"
							+ "CurrentTranFlagID: " + pmorder.getCurrentTranFlagID()+ "|"
							+ "ExternalKey: " + pmorder.getExternalKey() + "|"
							+ "PeriodEndFlagID: " + pmorder.getPeriodEndFlagID() + "|"						 
							+ "SAPStatus: " + pmorder.getSAPStatus() + "|"
							+ "SAPWBSElement: " + pmorder.getSAPWBSElement() + "|"
							+ "VersionID: " + pmorder.getVersionID() + "|"
							+ "AlternateCostTransactionCurrency: " + pmorder.getAlternateCostTransactionCurrency() + "|"
							+ "CostCostObjectCurrency: " + pmorder.getCostCostObjectCurrency() + "|"
							+ "CostTransactionCurrency: " + pmorder.getCostTransactionCurrency() + "|"		
							+ "TransactionDate: " + pmorder.getTransactionDate() + "|"								
							+ or.isSuccessFlag() + "|" + str);	

					statusMsg.setStatus(GlobalConstants.STAGING_RECORD_STATUS_FAILED);
					statusMsg.setErrorMsg(str);
					//itgIssueLog.add(getIntegrationIssuesAPIType("", str, projectId, pmorder.getExternalKey()));
					//if (or.getResultMessage().get(0).getMessage().equalsIgnoreCase("Object was ignored due to defined filter"))
					itgIssueLog.add(getIntegrationIssuesAPIType("","Transactions on PM Order "+pmorder.getCostObjectExternalKey() + " do not have a valid parent WBS", projectId, pmorder.getExternalKey()));
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
			session = this.requestPostP(projectId, session, baseUri);
			//logger.info(end + " of " + total + " - elapsed time: " + timerBatch.stop().toString(ISOPeriodFormat.alternateExtended()));
			retStatusMsgList.addAll(statusMsgList);


			//this.epcRestMgr.logout(client, baseUri, session);

		} catch(Exception e) {
			throw new SystemException(e);
		}
		logger.debug("Complete!");
		return retStatusMsgList;
	}

	private Cookie requestPreP(String projectId, Cookie session, 
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
			logger.debug("2000 -- PM Order Import - PM Order Post Process Completed Successfully");
			int i=0;
		}
		return session;
	}

	private Cookie requestPostP(String projectId, Cookie session, 
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
			logger.debug("2000 -- PM Order Import - PM Order Post Process Completed Successfully");
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
			session = this.requestPreP(projectId, session, baseUri);
			//logger.info(end + " of " + total + " - elapsed time: " + timerBatch.stop().toString(ISOPeriodFormat.alternateExtended()));
			retStatusMsgList.addAll(statusMsgList);


			//this.epcRestMgr.logout(client, baseUri, session);

		} catch(Exception e) {
			throw new SystemException(e);
		}
		logger.debug("Complete!");
		return retStatusMsgList;
	}

	//	private Cookie requestPre(String projectId, Cookie session, 
	//			String baseUri) throws SystemException {
	//
	//		BayerCalculateWBSIDAPIResultType request = new BayerCalculateWBSIDAPIResultType();
	//		//request.getBayerCommitmentLIAPI().addAll(subList);
	//
	//		HashMap<String,String> filterMap = new HashMap<String, String>();
	//		filterMap.put(GlobalConstants.EPC_ROOTCOSTOBJECT_PARAM, projectId);
	//
	//		CalcWObjectFactory objectFactory = new CalcWObjectFactory();
	//		JAXBElement<BayerCalculateWBSIDAPIResultType> requestWrapper = objectFactory.createBayerCalculateWBSIDAPIResult(request);
	//
	//		ClientResponse response = epcRestMgr
	//				.postApplicationXmlAsApplicationXml(client, requestWrapper,
	//						baseUri, GlobalConstants.EPC_REST_PRE_PROCESS_PMO,
	//						session, filterMap);
	//
	//		logger.debug(response);
	//		BayerCalculateWBSIDAPIResultType result = epcRestMgr.responseToObject(response, BayerCalculateWBSIDAPIResultType.class);
	//
	//		if(session == null)
	//			session = epcRestMgr.getSessionCookie(response);
	//
	//		if(!result.isSuccessFlag()){
	//			String errMsg="The interface failed to load any record due to data issues; please verify data.";
	//			if (result.getError() != null)
	//				errMsg=result.getError().toString();
	//
	//			throw new SystemException(errMsg);
	//		} else {
	//
	//			//List<StatusMessage> statusMsgList = new ArrayList<StatusMessage>();
	//			logger.debug("2000 -- PM Order Import - PM Order Pre Process Completed Successfully");
	//			int i=0;
	//		}
	//		return session;
	//	}

	private  List<BayerWBSReadAPIType> readEPCCostObjects(String projectId,String coType) throws SystemException{

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

	private  List<BayerPMOrderV2APIType> getValidParentCOs(List<BayerPMOrderV2APIType> inputList, List<BayerPMOrderV2APIType> missingList, List<BayerWBSReadAPIType> parentList){

		List<BayerPMOrderV2APIType> mParentCOs = new ArrayList<BayerPMOrderV2APIType>();
		for(BayerPMOrderV2APIType pmo: inputList)
		{
			if (!this.isParentMissing(pmo, parentList))
			{
				mParentCOs.add(pmo);
			}
			else
			{
				//pmo.setParentCostObjectExternalKey(defaultParent);
				missingList.add(pmo);
			}
		}
		return mParentCOs;
	}

	private boolean isParentMissing (BayerPMOrderV2APIType apiType,List<BayerWBSReadAPIType> parentList )
	{
		boolean isMissing = true;
		for(BayerWBSReadAPIType pco: parentList)
		{
			if (apiType.getParentCostObjectExternalKey().equalsIgnoreCase(pco.getExternalKey()))
				isMissing = false;
		}
		return isMissing;
	}
	//	private List<WbsApiErrorDAO> processWBS( List<BayerWBSAPIType> prjRecords, String projectId) throws SystemException{
	//
	//		logger.debug("Importing CostObjects to EPC...");
	//
	//		List<WbsApiErrorDAO> retStatusMsgList = new ArrayList<WbsApiErrorDAO>();
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
	//				List<WbsApiErrorDAO> statusMsgList = new ArrayList<WbsApiErrorDAO>();
	//				timerBatch.start();
	//				session = this.requestWBS(prjRecords.subList(i, end), session, baseUri, projectId, statusMsgList);
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

	private Cookie requestWBS(List<BayerWBSAPIType> subList,
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

	private BayerWBSAPIType getDefaultWBS(String masterProjectId, String subProjectId, String currCode, String defaultParent)
	{
		BayerWBSAPIType apiType = new BayerWBSAPIType();
		String hiPath = masterProjectId;
		int hiLevel = 1;
		if (subProjectId.equalsIgnoreCase(""))
			hiPath = hiPath + "." + GlobalConstants.EPC_REST_DEFAULT_WBS;
		else
		{
			hiPath = hiPath + "." + subProjectId + "." + GlobalConstants.EPC_REST_DEFAULT_WBS;
			hiLevel = 2;
		}
		apiType.setCostObjectCurrencyCode(currCode);
		apiType.setCostObjectHierarchyLevel(hiLevel);
		apiType.setCostObjectID(GlobalConstants.EPC_REST_DEFAULT_WBS);
		apiType.setCostObjectName(GlobalConstants.EPC_REST_DEFAULT_WBS);
		apiType.setExternalKey(defaultParent);
		apiType.setHierarchyPathID(hiPath);
		return apiType;
	}

	private boolean isDefaultParentMissing (String defaultParent,List<BayerWBSReadAPIType> parentList )
	{
		boolean isMissing = true;
		for(BayerWBSReadAPIType pco: parentList)
		{
			if (pco.getExternalKey().equalsIgnoreCase(defaultParent))
				isMissing = false;
		}
		return isMissing;
	}
	
	private List<BayerPMOrderV2APIType> hasParentCostObject(List<BayerPMOrderV2APIType> apiTypes, String projectId){
		List<BayerWBSReadAPIType> ecosysWBS = new ArrayList<>();
		List<BayerPMOrderV2APIType> apiTypes2 = new ArrayList<BayerPMOrderV2APIType>();
		try {
			logger.debug("reading EcoSys cost objects for project: " + projectId);
			ecosysWBS = this.processEPCCostObjects(projectId, "WBS");

			findAndShowRecords(apiTypes); // utility to see something in the records  // debugMode = true
			logger.debug("checking " + ecosysWBS.size() + " records for parent cost object");
			for (BayerPMOrderV2APIType apiType : apiTypes) {
				try {
					String hpid =ecosysWBS.stream()
							.filter(t -> t.getExternalKey().equalsIgnoreCase(apiType.getSAPWBSElement()))
							.findFirst().get().getHierarchyPathID();
					apiTypes2.add(apiType);
				}
				catch (NoSuchElementException e) {
					//logger.error(":: MISSING PARENT :: parent cost object does not exist for PM Order with SAP unique ID: "+apiType.getSAPUniqueID());

					logger.error("PM Order Import : MISSING PARENT : ExternalKey: "+apiType.getExternalKey()+" --> Parent CostObjectId: " + apiType.getSAPWBSElement());
					itgIssueLog.add(getIntegrationIssuesAPIType("", "PM Order "+ apiType.getExternalKey() + " does not have a valid co parent", projectId,apiType.getExternalKey()));

					//System.out.println(projectId);
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
		try {			//Read PM Order Data from EcoSys using PMOrder API
			Cookie session = null;
			String baseUri2 = GlobalConstants.EPC_REST_PROTOCOL + "://" + GlobalConstants.EPC_REST_SERVER + ":"					
					+ GlobalConstants.EPC_REST_PORT + "/" + GlobalConstants.EPC_REST_BASEURI2;
			HashMap<String, String> aMap = new HashMap<String, String>();
			aMap.put(GlobalConstants.EPC_PROJECT_PARAM, projectId);
			aMap.put("structureType", coType);
			parentCOs = this.requestEPCCostObjects(session, baseUri2, aMap);
			return parentCOs;
		}
		catch(Exception e) {
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
		}
		else {
			int i=0;
		}
		return apiTypes;
	}
	
	private void findAndShowRecords(List<BayerPMOrderV2APIType> dataSent) {
		List<BayerPMOrderV2APIType> dataFiltered = new ArrayList<>();
		if (GlobalConstants.DEBUGMODE) {
			dataFiltered=dataSent.stream()
					.filter(t -> t.getSAPWBSElement()=="")
					//.forEach(System.out.println(t.getSAPWBSElement()))
					.collect(Collectors.toList());
			logger.info("Cost Objects Filtered: " + dataFiltered.size());
			for (BayerPMOrderV2APIType act : dataSent ) {
				//System.out.println(act.getSAPWBSElement());
			}
		}		
		return;
	}
	
}
