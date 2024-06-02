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
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.core.Cookie;
import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.joda.time.format.ISOPeriodFormat;

import com.bayer.integration.odata.SapPOLODataType;
import com.bayer.integration.persistence.CaApiErrorDAO;
import com.bayer.integration.persistence.PolApiErrorDAO;
import com.bayer.integration.persistence.PolcoApiErrorDAO;
import com.bayer.integration.persistence.WbsApiErrorDAO;
import com.bayer.integration.properties.GlobalConstants;
import com.bayer.integration.rest.actual.BayerActualsAPIType;
import com.bayer.integration.rest.calcwbs.BayerCalculateWBSIDAPIResultType;
import com.bayer.integration.rest.calcwbs.CalcWObjectFactory;
//import com.bayer.integration.rest.ccl.BayerCCLValidationAPIResultType;
//import com.bayer.integration.rest.ccl.ObjectFactoryCCL;
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
import com.bayer.integration.rest.polco2.BayerCommitmentLICOV2APIRequestType;
import com.bayer.integration.rest.polco2.BayerCommitmentLICOV2APIResultType;
import com.bayer.integration.rest.polco2.BayerCommitmentLICOV2APIType;
import com.bayer.integration.rest.polco2.COObjectFactory;
import com.bayer.integration.rest.polco2.COObjectResultType;
import com.bayer.integration.rest.pold.BayerCommitmentPOLIDeleteAPIResultType;
import com.bayer.integration.rest.pold.PoldObjectFactory;
import com.bayer.integration.rest.polhis.BayerCommitmentLIHistoryAPIResultType;
import com.bayer.integration.rest.polhis.BayerCommitmentLIHistoryAPIType;
import com.bayer.integration.rest.polth.BayerCommitmentPOLITrackHistoryAPIResultType;
import com.bayer.integration.rest.polth.PolthObjectFactory;
import com.bayer.integration.rest.polthi.BayerCommitmentPOLITrackHistoryIniAPIResultType;
import com.bayer.integration.rest.polthi.PolthiObjectFactory;
import com.bayer.integration.rest.poprh2.BayerPOPRHeadersV2APIType;
import com.bayer.integration.rest.postproc.BayerPostProcessAPIResultType;
import com.bayer.integration.rest.postproc.PPObjectFactory;
import com.bayer.integration.rest.postprocini.BayerPostProcessIniAPIResultType;
import com.bayer.integration.rest.postprocini.PPIniObjectFactory;
import com.bayer.integration.rest.project.BayerProjectAPIType;
import com.bayer.integration.rest.wbs.BayerWBSAPIRequestType;
import com.bayer.integration.rest.wbs.BayerWBSAPIResultType;
import com.bayer.integration.rest.wbs.BayerWBSAPIType;
import com.bayer.integration.rest.wbs.WObjectFactory;
import com.bayer.integration.rest.wbs.WObjectResultType;
import com.bayer.integration.rest.wbsread.BayerWBSReadAPIResultType;
import com.bayer.integration.rest.wbsread.BayerWBSReadAPIType;
import com.bayer.integration.utils.DebugBanner;
import com.ecosys.exception.SystemException;
import com.ecosys.util.Stopwatch;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.org.apache.xerces.internal.dom.ElementNSImpl;
/**
 * @author pwng
 *
 */
public class POLineItemImportMgrImpl extends ImportManagerBase implements
ImportManager {

	/* (non-Javadoc)
	 * @see com.bayer.integration.service.ImportManager#importData()
	 */
	//@Override
	final String itgInterface = "POL";
	List<IntegrationIssuesAPIType> itgIssueLog = new ArrayList<IntegrationIssuesAPIType>();

	public int importData() {
		int retCode=GlobalConstants.IMPORT_SAP_POL_SUCCESS;
		try{			
			if (!GlobalConstants.SKIP_SAP_POL_INTERFACE) {
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
				DatatypeFactory dFactory = DatatypeFactory.newInstance();
				XMLGregorianCalendar currentDate = dFactory.newXMLGregorianCalendar(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

				Integer dayInMonth = currentDate.getDay();

				//Loop through the project list
				for (int i = 0; i < projectAPITypes.size(); i++) 
				{
					itgIssueLog.clear();
					boolean isTracking = false;
					BayerProjectAPIType projectAPIType = projectAPITypes.get(i);
					String projectId = projectAPIType.getID();
					GlobalConstants.EPC_PROJECT_ID_PROCESSED = projectId;
					String systemId = projectAPIType.getSAPSystemID();
					String sapProjectId = projectAPIType.getSapProjectId();
					String projectCurrency = projectAPIType.getRootCostObjectCurrencyCode();
					if (projectAPIType.getPOHistoryTrackedID()!=null && projectAPIType.getPOHistoryTrackedID().equals(GlobalConstants.EPC_API_ERROR_FLAG_Y))
						isTracking = true;
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

					//if (!isSub)
					//masterProjectId = projectId;

					List<CaApiErrorDAO> errorCaDAOList = new ArrayList<CaApiErrorDAO>();
					List<PolcoApiErrorDAO> errorCoDAOList = new ArrayList<PolcoApiErrorDAO>();
					List<PolApiErrorDAO> errorDAOList = new ArrayList<PolApiErrorDAO>();
					List<BayerCommitmentLICOV2APIType> inputListCO = new ArrayList<BayerCommitmentLICOV2APIType>();
					List<BayerCommitmentLICOV2APIType> currentListCO = new ArrayList<BayerCommitmentLICOV2APIType>();					
					List<BayerCommitmentLICOV2APIType> movedRecords = new ArrayList<BayerCommitmentLICOV2APIType>();
					List<BayerCommitmentLICOV2APIType> moved2Records = new ArrayList<BayerCommitmentLICOV2APIType>();

					List<BayerCommitmentLIAPIType> inputListPOL = new ArrayList<BayerCommitmentLIAPIType>();
					List<BayerCommitmentLIAPIType> inputList = new ArrayList<BayerCommitmentLIAPIType>();
					List<BayerCommitmentLIAPIType> inputListPRL = new ArrayList<BayerCommitmentLIAPIType>();
					List<BayerCommitmentLIHistoryAPIType> hisList = new ArrayList<BayerCommitmentLIHistoryAPIType>();

					List<PolApiErrorDAO> invalidRecords = new ArrayList<PolApiErrorDAO>(); 
					List<BayerCostAccountsAPIType> costAccounts = new ArrayList<BayerCostAccountsAPIType>();
					List<BayerCommitmentLICOV2APIType> commitmentLICOs = new ArrayList<BayerCommitmentLICOV2APIType>();
					List<BayerCommitmentLICOV2APIType> commitmentLICOs2 = new ArrayList<BayerCommitmentLICOV2APIType>();
					List<BayerCommitmentLIAPIType> validRecords = new ArrayList<BayerCommitmentLIAPIType>();
					List<BayerCommitmentLICOV2APIType> splitLICOs = new ArrayList<BayerCommitmentLICOV2APIType>();

					//Aug2021 Phase 3 CR2 & 3 variables
					List<BayerPOPRHeadersV2APIType> inputPOHList = new ArrayList<BayerPOPRHeadersV2APIType>();

					/*Aug 2021 Phase 3 CR02 related - Disabled
					List<BayerWBSReadAPIType> currentParentCOs = new ArrayList<BayerWBSReadAPIType>();
					List<BayerCommitmentLICOV2APIType> validLICOs = new ArrayList<BayerCommitmentLICOV2APIType>();
					List<BayerCommitmentLICOV2APIType> noParentLICOs = new ArrayList<BayerCommitmentLICOV2APIType>();
					String defaultParent = projectId + "-" + GlobalConstants.EPC_REST_DEFAULT_WBS;
					List<WbsApiErrorDAO> errorWbsDAOList = new ArrayList<WbsApiErrorDAO>();
					List<BayerWBSAPIType> defaultParentCOs = new ArrayList<BayerWBSAPIType>();
					 */
					//completed for Phase 3 CR2 & 3 variables

					session = null;
					try
					{
						//soft delete existing records from EcoSys
						logger.debug("Purging existing PO Line Item data from EcoSys for Project: "+ projectId);
						this.deleteLIs(projectId);
						logger.debug("Purging existing PO Line Item data from EcoSys completed for Project: "+ projectId);

						//Delete existing records from ERR Log table
						//logger.debug("Purging existing PO Line Item log records from ERR Log table for Project: "+ projectId);
						//int logCount = this.deleteLogs(projectId, GlobalConstants.EPC_POL_API_ERROR_BATCH_DELETE);
						//logger.debug("Purging " + logCount + " existing PO Line Item log records from ERR Log table for Project: "+ projectId);

						//Invoke EcoSys API with input records
						logger.debug("Reading PO Line Item data from SAP Input for Project: "+ projectId);



						if (isLive)
						{
							commitmentLICOs = readSapDataCO(sapProjectId, systemId);
							inputPOHList = readSapPOHData(projectHierarchyPathId, sapProjectId, systemId);

							//TODO this line is commented out on 21 July 2021, need testing
							commitmentLICOs2 = getCOCurrencyUpdatedLICOs(projectCurrency, commitmentLICOs);

							commitmentLICOs = getCOUpdatedLICOs(projectCurrency, inputPOHList, commitmentLICOs);

							inputListPOL = readSapData(sapProjectId, systemId);
							inputListPRL = readSapPRLData(sapProjectId, systemId);

							inputList = this.getBayerCommitmentLIAPITypesPRStatus(inputListPOL, inputListPRL);
							invalidRecords = this.getSapInputInvalid(inputList);
							logger.debug("Number of Records Read in from OData: " + inputList.size() + " for Project: " + projectId);
						}
						else
						{
							//Start Hexagon Dev Sample Data Section
							List<SapPOLODataType> inputListSample = importHelper.getSapPOLTypesSample();
							commitmentLICOs = this.getBayerCommitmentLICOV2APITypesSample(inputListSample);
							inputList = this.getBayerCommitmentLIAPITypesSample(inputListSample);
							invalidRecords = this.getBayerCommitmentLIAPITypesInvalidSample(inputListSample);
							//End Hexagon Dev Sample Data Section
						}


						if (isSub)
						{
							currentListCO = this.readCommitmentLICOs(masterProjectId);
							//currentParentCOs = this.readParentCOs(masterProjectId);
						}
						else
						{
							currentListCO = this.readCommitmentLICOs(projectId);
							//currentParentCOs = this.readParentCOs(projectId);
						}

						//validLICOs = this.getValidParentLICOs(commitmentLICOs, noParentLICOs, currentParentCOs, defaultParent);

						//noParentLICOs = this.getMissingParentLICOs(commitmentLICOs, currentParentCOs, defaultParent);


						costAccounts = this.getBayerCostAccountsAPITypes(inputList);

						//						Map<String, String> parentMap = commitmentLICOs.stream()
						//								.collect(Collectors.toMap(e -> e.getExternalKey(), p -> p.getParentCostObjectExternalKey()));					
						//						
						//						List<BayerWBSReadAPIType> wbsPmoType = readCOByType(sapProjectId, "PM Order");		
						//						List<BayerWBSReadAPIType> wbsWbsType = readCOByType(sapProjectId, "WBS");
						//						wbsPmoType.addAll(wbsWbsType);
						//						Set<String> extKeys = wbsPmoType.stream()
						//								.map(ext -> ext.getExternalKey())
						//								.collect(Collectors.toSet());
						//						
						//						logger.debug("EcoSys returned " +extKeys.size() + " unique External Keys");
						//						logger.debug("LICO mapped " +parentMap.size() + " mapped External Keys");
						//validRecords = this.getBayerCommitmentLIAPITypesValid(inputList, extKeys, parentMap);
						validRecords = this.getBayerCommitmentLIAPITypesValid(inputList,commitmentLICOs,sapProjectId);

						//Process Invalid Records
						logger.debug("Processing Invalid Records, If Any for Project: " + projectId);
						if (invalidRecords.size()>0 && !skipError)
							this.processStatusMessages(invalidRecords, true);
						logger.debug("Processing Invalid Records Completed for Project: "+ projectId);

						//Process  PO Line Items related Cost Elements
						logger.debug("Processing PO Line Items related Cost Elements for Project: " + projectId);
						errorCaDAOList = processCostAccounts(costAccounts, projectId);			
						logger.debug("Processing PO Line Items related Cost Elements completed for Project: " + projectId);

						//Aug 2021 Phase 3 CR02 Process Moved Back Records - Disabled
						/*
						moved2Records = this.getReMovedCommitmentLICOs(currentListCO, validLICOs, currentDate);

						//Process Moved Records
						movedRecords = this.getMovedCommitmentLICOs(currentListCO, validLICOs, currentDate);

						//if (commitmentLICOs.size()>0)
							//commitmentLICOs = this.getCOCurrencyUpdatedLICOs(projectCurrency, commitmentLICOs);

						if (validLICOs.size()>0 && movedRecords.size()>0 )
							validLICOs = this.getUpdatedLICOs(movedRecords, validLICOs);
						 */

						//Process Moved Back Records
						moved2Records = this.getReMovedCommitmentLICOs(currentListCO, commitmentLICOs, currentDate);

						//Process Moved Records
						movedRecords = this.getMovedCommitmentLICOs(currentListCO, commitmentLICOs, currentDate);

						//if (commitmentLICOs.size()>0)
						//commitmentLICOs = this.getCOCurrencyUpdatedLICOs(projectCurrency, commitmentLICOs);

						if (commitmentLICOs.size()>0 && movedRecords.size()>0 )
							commitmentLICOs = this.getUpdatedLICOs(movedRecords, commitmentLICOs);

						if (moved2Records.size()>0)
						{						
							if (isSub)
								errorCoDAOList = processCommitmentLICOs(movedRecords, masterProjectId);
							else
								errorCoDAOList = processCommitmentLICOs(movedRecords, projectId);

							//Process Status Messages
							if (!skipError)
							{
								logger.debug("Processing Moved Back PO Line ERR Messages, If Any for Project: " + projectId);
								this.processStatusMessages(errorDAOList, true);
								logger.debug("Processing ERR Message Completed for Project: "+ projectId);
							}
						}

						if (movedRecords.size()>0)
						{						
							if (isSub)
								errorCoDAOList = processCommitmentLICOs(movedRecords, masterProjectId);
							else
								errorCoDAOList = processCommitmentLICOs(movedRecords, projectId);

							//Process Status Messages
							if (!skipError)
							{
								logger.debug("Processing Moved PO Line ERR Messages, If Any for Project: " + projectId);
								this.processStatusMessages(errorDAOList, true);
								logger.debug("Processing ERR Message Completed for Project: "+ projectId);
							}
						}

						if (moved2Records.size()>0)
						{						
							moved2Records = this. getReMovedCommitmentLICOsActivated(moved2Records);
							if (isSub)
								errorCoDAOList = processCommitmentLICOs(movedRecords, masterProjectId);
							else
								errorCoDAOList = processCommitmentLICOs(movedRecords, projectId);

							//Process Status Messages
							if (!skipError)
							{
								logger.debug("Processing Moved Back PO Line ERR Messages, If Any for Project: " + projectId);
								this.processStatusMessages(errorDAOList, true);
								logger.debug("Processing ERR Message Completed for Project: "+ projectId);
							}
						}
						//Process PO Line Items as CO
						logger.debug("Creating/Updating PO Line Items as Cost Objects for Project: " + projectId);
						errorCoDAOList = processCommitmentLICOs(commitmentLICOs, projectId);
						//errorCoDAOList = processCommitmentLICOs(validLICOs, projectId);			
						logger.debug("Creating/Updating PO Line Items as Cost Objects completed for Project: " + projectId);

						//Aug 2021 Phase 3 CR02 Process PO Line Items as CO with Missing Parent - Disabled
						/*
						if (noParentLICOs.size()>0)
						{

							if (this.isDefaultParentMissing(defaultParent, currentParentCOs))
							{
								BayerWBSAPIType apiType = new BayerWBSAPIType();
								if (isSub)
										apiType = this.getDefaultWBS(masterProjectId, projectId, projectCurrency, defaultParent);
								else
									apiType = this.getDefaultWBS(masterProjectId, "", projectCurrency, defaultParent);
								defaultParentCOs.add(apiType);
								logger.debug("Createing default WBS Parent for Project:" + projectId);
								errorWbsDAOList = this.processWBS(defaultParentCOs, masterProjectId);
								logger.debug("Createing default WBS Parent Completed for Project:" + projectId);
							}


							logger.debug("Processing PO Line Items with Missing Parent WBS/PM Order in EcoSys for Project: " + projectId);

							//errorCoDAOList = processMissingParentCOs(noParentLICOs, projectId);		
							validRecords = this.removeParentMissingTrans(validRecords, noParentLICOs, projectId);

							logger.debug("Processing PO Line Items with Missing Parent WBS/PM Order completed for Project: " + projectId);
						}
						 */

						//Process Status Messages
						if (!skipError)
						{
							logger.debug("Processing ERR Messages, If Any for Project: " + projectId);
							//this.processStatusMessagesCo(errorCoDAOList, true);
							logger.debug("Processing ERR Message Completed for Project: "+ projectId);	
						}


						//Read PO/PR Line Items from Commitments - History version to track Net Order Value Change History
						if (isSub)
							hisList = this.readCommitmentLIHISs(masterProjectId);
						else
							hisList = this.readCommitmentLIHISs(projectId);

						validRecords = this.updateLIChangeHistory(validRecords, hisList);

						//errorDAOList.removeAll(errorDAOList);			
						errorDAOList = processCommitmentLIs(validRecords, projectId);


						//Process Status Messages
						if (!skipError)
						{
							logger.debug("Processing ERR Messages, If Any for Project: " + projectId);
							//this.processStatusMessages(errorDAOList, true);
							logger.debug("Processing ERR Message Completed for Project: "+ projectId);	
						}

						//re-calculate WBS ID/Name for the Project
						logger.debug("Trigger Action Batch to Recalc WBS ID/Name Custom Field for Project: "+ projectId);
						this.calcWBSIds(projectId);
						logger.debug("Trigger Action Batch to Recalc WBS ID/Name Custom Field completed for Project: "+ projectId);
						//logger.debug("Triggering Action Batch to validate CCL for Project: "+ projectId);
						//this.processCCL(projectAPIType.getInternalID());
						//logger.debug("Trigger Action Batch to validate CCL completed for Project: "+ projectId);

						//Trigger Tracking History Workflow/Action Batch in EcoSys
						//dayInMonth = 1;
						if (dayInMonth == 1 && !GlobalConstants.SKIP_SAP_PP_INTERFACE)
						{
							//logger.info("Running Post Processing Action Batch from EcoSys for Project: "+ projectId);
							//this.postprocessLIs(sapProjectId, isTracking);
							//this.trackLIs(projectId, isTracking);
							//logger.info("Running Post Processing Action Batch from EcoSys completed for Project: "+ projectId);
						}
						else
						{
							//logger.info("Skipped Running Post Processing Action Batch from EcoSys for Project: "+ projectId);
						}
						logger.debug("posting "+itgIssueLog.size()+" " +itgInterface+" issues to EPC");
						if (itgIssueLog.size()>0)
							processIssueLog(itgIssueLog);
					}
					catch(SystemException se) {
						logger.error("5005 -- PO Line Item Import Failed: " + GlobalConstants.EPC_PROJECT_ID_PROCESSED, se);
						retCode=GlobalConstants.IMPORT_SAP_POL_FAILED;
						continue;
					}
				}
			}

			else {
				logger.info("Skipped PO Line Item Import Interface. Change the skip property to 'false'");				
			}

		}catch(Exception e) {			
			logger.error("5005 -- PO Line Item Import Failed", e);			
			retCode=GlobalConstants.IMPORT_SAP_POL_FAILED;
		}

		if (retCode==GlobalConstants.IMPORT_SAP_POL_SUCCESS)
			logger.debug("5000 -- PO Line Item Import Completed Successfully");

		return retCode;
	}
	private void findAndShowRecords(List<BayerCommitmentLIAPIType> dataSent) {
		List<BayerCommitmentLIAPIType> dataFiltered = new ArrayList<>();
		if (GlobalConstants.DEBUGMODE) {
			dataFiltered=dataSent.stream()
					.filter(t -> t.getExternalKey()=="")
					.collect(Collectors.toList());
			logger.info(dataFiltered.size());
		}
		return;
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
			
			//findAndShowRecords(apiTypes); // utility to see something in the records  // debugMode = true
			
			logger.debug("checking " + ecosysWBS.size() + " records for parent cost object");
			for (BayerCommitmentLIAPIType apiType : apiTypes) {	
				try {
					String hpid =ecosysWBS.stream()
							.filter(t -> /*t.getCostObjectID().equalsIgnoreCase(apiType.getCostObjectExternalKey().substring(0,12)) //parent is a PMO
									||*/ t.getExternalKey().equalsIgnoreCase(apiType.getExternalKey().substring(0,apiType.getExternalKey().indexOf(apiType.getCostObjectID())-1))) //parent is a WBS
							// logic not working
							.findFirst().get().getHierarchyPathID();
					apiTypes2.add(apiType);
				}			
				catch (NoSuchElementException e) {	
					//logger.error(": PO Line : MISSING PARENT --> parent WBS or PM Order does not exist for PO Line Item with SAP purchasing document number : "+apiType.getSAPPurchasingDocumentNumberID() +" SAP PO line item number: "+apiType.getSAPPurchasingDocumentLineItemNumber());					
					logger.error("PO Line : MISSING PARENT : SAP Purchasing Document Number: " +apiType.getSAPPurchasingDocumentNumberID()
						+" --> Line Number: " + apiType.getSAPPurchasingDocumentLineItemNumber()+" --> Parent ExternalKey: " + apiType.getExternalKey().substring(0,apiType.getExternalKey().indexOf(apiType.getCostObjectID())-1));					
					itgIssueLog.add(getIntegrationIssuesAPIType("",
							"SAP Purchasing Document Number: " +apiType.getSAPPurchasingDocumentNumberID()+", line Number: " + apiType.getSAPPurchasingDocumentLineItemNumber() + " does not have a valid parent"
							,projectId,apiType.getExternalKey()));
					
					System.out.println(projectId);
					continue;
				}			
			}		
		}			catch (SystemException e) {	
			logger.error("error calling the read wbs API");	
			e.printStackTrace();	
		}		
		logger.debug("PM order checked: "+ apiTypes.size()+" || "+" Parents found: "+ apiTypes2.size());
		return apiTypes2;	
	}
	// Check if POL has valid parent cost object in EcoSys
	//		Set<String> extKeys = null;
	//		Map<String, String> parentMap = commitmentLICOs.stream()
	//				.collect(Collectors.toMap(e -> e.getExternalKey(), p -> p.getParentCostObjectExternalKey()));							
	//		try {
	//			List<BayerWBSReadAPIType> wbsPmoType = readCOByType(sapProjectId, "PM Order");		
	//			List<BayerWBSReadAPIType> wbsWbsType = readCOByType(sapProjectId, "WBS");
	//			wbsPmoType.addAll(wbsWbsType);
	//			extKeys = wbsPmoType.stream()
	//					.map(ext -> ext.getExternalKey())
	//					.collect(Collectors.toSet());
	////			logger.debug("EcoSys returned " +extKeys.size() + " unique External Keys");
	//			logger.debug("LICO mapped " +parentMap.size() + " mapped External Keys");
	//		} catch (SystemException e) {
	//			System.out.println("System exception occurred: " + e + " integration continuing");//		}
	
	
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
	
	private List<PolApiErrorDAO> processCommitmentLIs( List<BayerCommitmentLIAPIType> prjRecords,
			String projectId) throws SystemException{

		logger.debug("Importing PO Line Items to EPC as Commitment transactions...");

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
	private  List<BayerWBSReadAPIType> processEPCCostObjects(String projectId,String coType) throws SystemException{
		List<BayerWBSReadAPIType> parentCOs = new ArrayList<BayerWBSReadAPIType>();
		try 		{
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
	
	private List<PolcoApiErrorDAO> processCommitmentLICOs( List<BayerCommitmentLICOV2APIType> prjRecords,
			String projectId) throws SystemException{

		logger.debug("Importing PO Line Items to EPC as Cost Objects...");

		List<PolcoApiErrorDAO> retStatusMsgList = new ArrayList<PolcoApiErrorDAO>();
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
				List<PolcoApiErrorDAO> statusMsgList = new ArrayList<PolcoApiErrorDAO>();
				timerBatch.start();
				session = this.requestCO(prjRecords.subList(i, end), 
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


	private Cookie requestCO(List<BayerCommitmentLICOV2APIType> subList, Cookie session,
			String baseUri, String projectId, List<PolcoApiErrorDAO> errorList) throws SystemException {

		BayerCommitmentLICOV2APIRequestType request = new BayerCommitmentLICOV2APIRequestType();
		request.getBayerCommitmentLICOV2API().addAll(subList);

		COObjectFactory objectFactory = new COObjectFactory();
		JAXBElement<BayerCommitmentLICOV2APIRequestType> requestWrapper = objectFactory.createBayerCommitmentLICOV2APIRequest(request);
		HashMap<String, String> prjMap = new HashMap<String, String>();
		prjMap.put(GlobalConstants.EPC_PROJECT_PARAM, projectId);
		ClientResponse response = epcRestMgr
				.postApplicationXmlAsApplicationXml(client, requestWrapper,
						baseUri, GlobalConstants.EPC_REST_IMPORT_SAP_PO_LNITM_CO_V2,
						session, prjMap);

		logger.debug(response);
		BayerCommitmentLICOV2APIResultType result = epcRestMgr.responseToObject(response, BayerCommitmentLICOV2APIResultType.class);
		if(session == null)
			session = epcRestMgr.getSessionCookie(response);

		if(!result.isSuccessFlag()){
			String errMsg="The interface failed to load any record due to data issues; please verify data.";
			if (result.getError() != null)
				errMsg=result.getError().toString();
			for(BayerCommitmentLICOV2APIType pol: subList)
			{
				//convert to APIErrorDAO type
				PolcoApiErrorDAO statusMsg = this.getPolcoAPIErrorDAO(pol);
				statusMsg.setRootCostObjectID(projectId);
				statusMsg.setStatus(GlobalConstants.STAGING_RECORD_STATUS_FAILED);
				statusMsg.setErrorMsg(errMsg);
				errorList.add(statusMsg);
			}
			throw new SystemException(errMsg);
		} else {

			//List<StatusMessage> statusMsgList = new ArrayList<StatusMessage>();
			int i=0;
			for(COObjectResultType or : result.getObjectResult()) {
				BayerCommitmentLICOV2APIType poLI = subList.get(i++);
				String polID = poLI.getCostObjectID();

				//convert to APIErrorDAO type
				PolcoApiErrorDAO statusMsg = this.getPolcoAPIErrorDAO(poLI);
				statusMsg.setRootCostObjectID(projectId);

				if(or.isSuccessFlag()) {
					logger.debug("UPDATE RESULT --> " + or.getInternalId());
					logger.debug("Record with External Key ID of : " + polID +" "+ GlobalConstants.STAGING_RECORD_STATUS_LOADED);
					/*
					logger.debug("Record update with External Key ID of : " + polID +" |"
							 + "CostObjectID: " + poLI.getCostObjectID() + "|"
							 + "CostObjectName: " + poLI.getCostObjectName()+ "|"
							 + "CostObjectStatus: " + poLI.getCostObjectStatus() + "|"
							 + "CostObjectTypeName: " + poLI.getCostObjectTypeName() + "|"
						     + "ExternalKey: " + poLI.getExternalKey()+ "|"
							 + "ParentCostObjectExternalKey: " + poLI.getParentCostObjectExternalKey() + "|"					
							 + "COParentChangedID: " + poLI.getCOParentChangedID() + "|"
							 + "ParentCOChangeDate " + poLI.getParentCOChangeDate()+ "|"			     
							+ GlobalConstants.STAGING_RECORD_STATUS_LOADED);
					 */
					statusMsg.setStatus(GlobalConstants.STAGING_RECORD_STATUS_LOADED);

				} else {
					//String str = ((ElementNSImpl)or.getResultMessage().get(0).getMessage()).getTextContent();
					String str = or.getResultMessage().get(0).getMessage();
					logger.error("PO Line : MISSING PARENT : " + or.getInternalId() + "|" + polID + "|" 
							+ "CostObjectID: " + poLI.getCostObjectID() + "|"
							+ "CostObjectName: " + poLI.getCostObjectName()+ "|"
							+ "CostObjectStatus: " + poLI.getCostObjectStatus() + "|"
							+ "CostObjectTypeName: " + poLI.getCostObjectTypeName() + "|"
							+ "ExternalKey: " + poLI.getExternalKey()+ "|"
							+ "ParentCostObjectExternalKey: " + poLI.getParentCostObjectExternalKey() + "|"					
							+ "COParentChangedID: " + poLI.getCOParentChangedID() + "|"
							+ "ParentCOChangeDate " + poLI.getParentCOChangeDate()+ "|"
							+ "CostObjectCurrencyCode " + poLI.getCostObjectCurrencyCode()+ "|"
							+ or.isSuccessFlag() + "|" + str);	

					statusMsg.setStatus(GlobalConstants.STAGING_RECORD_STATUS_FAILED);
					statusMsg.setErrorMsg(str);				
					//itgIssueLog.add(getIntegrationIssuesAPIType("", polID + " " + str.substring(str.indexOf(":")+1), projectId, polID));
				}
				errorList.add(statusMsg);				
			}
		}
		return session;
	}

	private Cookie request(List<BayerCommitmentLIAPIType> subList, Cookie session,
			String baseUri, String projectId, List<PolApiErrorDAO> errorList) throws SystemException {

		BayerCommitmentLIAPIRequestType request = new BayerCommitmentLIAPIRequestType();
		request.getBayerCommitmentLIAPI().addAll(subList);

		ObjectFactory objectFactory = new ObjectFactory();
		JAXBElement<BayerCommitmentLIAPIRequestType> requestWrapper = objectFactory.createBayerCommitmentLIAPIRequest(request);
		HashMap<String, String> prjMap = new HashMap<String, String>();
		//prjMap.put(GlobalConstants.EPC_PROJECT_PARAM, projectId);
		ClientResponse response = epcRestMgr
				.postApplicationXmlAsApplicationXml(client, requestWrapper,
						baseUri, GlobalConstants.EPC_REST_IMPORT_SAP_PO_LNITM,
						session, prjMap);

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
				String polID = poLI.getCommitmentID() + "-" + poLI.getSAPPurchasingDocumentLineItemNumber();

				//convert to APIErrorDAO type
				PolApiErrorDAO statusMsg = this.getPolAPIErrorDAO(poLI);
				statusMsg.setRootCostObjectID(projectId);

				if(or.isSuccessFlag()) {
					logger.debug("UPDATE RESULT --> " + or.getInternalId());
					logger.debug("Record with External Key ID of : " + polID +" "+ GlobalConstants.STAGING_RECORD_STATUS_LOADED);
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
					//String str = (ElementNSImpl)or.getResultMessage().get(0).getMessage()).getTextContent();
					//DEBUG
					String str = or.getResultMessage().get(0).getMessage();
					logger.error("ERROR --> " + or.getInternalId() + "|ExternalId: " + polID + "|"
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
				}
				errorList.add(statusMsg);				
			}
		}
		return session;
	}



	private void processStatusMessages() throws SystemException {
		try{

		} catch(Exception e) {
			throw new SystemException (e);
		}
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
			session = this.requestDelete(projectId, session, baseUri);
			//logger.info(end + " of " + total + " - elapsed time: " + timerBatch.stop().toString(ISOPeriodFormat.alternateExtended()));
			retStatusMsgList.addAll(statusMsgList);


			//this.epcRestMgr.logout(client, baseUri, session);

		} catch(Exception e) {
			throw new SystemException(e);
		}
		logger.debug("Complete!");
		return retStatusMsgList;
	}

	private List<String> postprocessLIs( String projectId, boolean isTracking) throws SystemException{

		logger.debug("Trigger post processing for Project: " + projectId);

		List<String> retStatusMsgList = new ArrayList<String>();
		try {
			//Prepare for the REST call
			String baseUri = GlobalConstants.EPC_REST_PROTOCOL + "://" + GlobalConstants.EPC_REST_SERVER + ":" + 
					GlobalConstants.EPC_REST_PORT + "/" + GlobalConstants.EPC_REST_BASEURI2;


			Stopwatch timerBatch = new Stopwatch();			
			Cookie session = null;

			List<String> statusMsgList = new ArrayList<String>();
			timerBatch.start();
			if (isTracking == true)
				session = this.requestPostProcess(projectId, session, baseUri);
			else
				session = this.requestPostProcessIni(projectId, session, baseUri);

			//logger.info(end + " of " + total + " - elapsed time: " + timerBatch.stop().toString(ISOPeriodFormat.alternateExtended()));
			retStatusMsgList.addAll(statusMsgList);


			//this.epcRestMgr.logout(client, baseUri, session);

		} catch(Exception e) {
			throw new SystemException(e);
		}
		logger.debug("Complete!");
		return retStatusMsgList;
	}


	private List<String> trackLIs( String projectId, boolean isTracking) throws SystemException{

		logger.debug("Track PO Line Item History for Project: " + projectId);

		List<String> retStatusMsgList = new ArrayList<String>();
		try {


			//Prepare for the REST call
			String baseUri = GlobalConstants.EPC_REST_PROTOCOL + "://" + GlobalConstants.EPC_REST_SERVER + ":" + 
					GlobalConstants.EPC_REST_PORT + "/" + GlobalConstants.EPC_REST_BASEURI2;


			Stopwatch timerBatch = new Stopwatch();			
			Cookie session = null;

			List<String> statusMsgList = new ArrayList<String>();
			timerBatch.start();
			if (isTracking == true)
				session = this.requestTrack(projectId, session, baseUri);
			else
				session = this.requestTrackIni(projectId, session, baseUri);

			//logger.info(end + " of " + total + " - elapsed time: " + timerBatch.stop().toString(ISOPeriodFormat.alternateExtended()));
			retStatusMsgList.addAll(statusMsgList);


			//this.epcRestMgr.logout(client, baseUri, session);

		} catch(Exception e) {
			throw new SystemException(e);
		}
		logger.debug("Complete!");
		return retStatusMsgList;
	}

	private Cookie requestDelete(String projectId, Cookie session, 
			String baseUri) throws SystemException {

		BayerCommitmentPOLIDeleteAPIResultType request = new BayerCommitmentPOLIDeleteAPIResultType();
		//request.getBayerCommitmentLIAPI().addAll(subList);

		HashMap<String,String> filterMap = new HashMap<String, String>();
		filterMap.put(GlobalConstants.EPC_PROJECT_PARAM, projectId);

		PoldObjectFactory objectFactory = new PoldObjectFactory();
		JAXBElement<BayerCommitmentPOLIDeleteAPIResultType> requestWrapper = objectFactory.createBayerCommitmentPOLIDeleteAPIResult(request);

		ClientResponse response = epcRestMgr
				.postApplicationXmlAsApplicationXml(client, requestWrapper,
						baseUri, GlobalConstants.EPC_REST_DELETE_SAP_PO_LNITM,
						session, filterMap);

		logger.debug(response);
		BayerCommitmentPOLIDeleteAPIResultType result = epcRestMgr.responseToObject(response, BayerCommitmentPOLIDeleteAPIResultType.class);

		if(session == null)
			session = epcRestMgr.getSessionCookie(response);

		if(!result.isSuccessFlag()){
			String errMsg="The interface failed to load any record due to data issues; please verify data.";
			if (result.getError() != null)
				errMsg=result.getError().toString();

			throw new SystemException(errMsg);
		} else {

			//List<StatusMessage> statusMsgList = new ArrayList<StatusMessage>();
			logger.debug("5000 -- PO Line Item Delete Completed Successfully for Project: "+ projectId);
			int i=0;
		}
		return session;
	}

	private Cookie requestPostProcess(String projectId, Cookie session, 
			String baseUri) throws SystemException {

		BayerPostProcessAPIResultType request = new BayerPostProcessAPIResultType();
		//request.getBayerCommitmentLIAPI().addAll(subList);

		HashMap<String,String> filterMap = new HashMap<String, String>();
		filterMap.put(GlobalConstants.EPC_ROOTCOSTOBJECT_PARAM, projectId);

		PPObjectFactory objectFactory = new PPObjectFactory();
		JAXBElement<BayerPostProcessAPIResultType> requestWrapper = objectFactory.createBayerPostProcessAPIResult(request);


		ClientResponse response = epcRestMgr
				.postApplicationXmlAsApplicationXml(client, requestWrapper,
						baseUri, GlobalConstants.EPC_REST_POST_PROCESS,
						session, filterMap);

		logger.debug(response);
		BayerPostProcessAPIResultType result = epcRestMgr.responseToObject(response, BayerPostProcessAPIResultType.class);

		if(session == null)
			session = epcRestMgr.getSessionCookie(response);

		if(!result.isSuccessFlag()){
			String errMsg="The interface failed to load any record due to data issues; please verify data.";
			if (result.getError() != null)
				errMsg=result.getError().toString();

			throw new SystemException(errMsg);
		} else {

			//List<StatusMessage> statusMsgList = new ArrayList<StatusMessage>();
			logger.debug("8000 -- Post Process Completed Successfully for Project: "+ projectId);
			int i=0;
		}
		return session;
	}

	private Cookie requestPostProcessIni(String projectId, Cookie session, 
			String baseUri) throws SystemException {

		BayerPostProcessIniAPIResultType request = new BayerPostProcessIniAPIResultType();
		//request.getBayerCommitmentLIAPI().addAll(subList);

		HashMap<String,String> filterMap = new HashMap<String, String>();
		filterMap.put(GlobalConstants.EPC_ROOTCOSTOBJECT_PARAM, projectId);

		PPIniObjectFactory objectFactory = new PPIniObjectFactory();
		JAXBElement<BayerPostProcessIniAPIResultType> requestWrapper = objectFactory.createBayerPostProcessIniAPIResult(request);


		ClientResponse response = epcRestMgr
				.postApplicationXmlAsApplicationXml(client, requestWrapper,
						baseUri, GlobalConstants.EPC_REST_POST_PROCESS_INI,
						session, filterMap);

		logger.debug(response);
		BayerPostProcessIniAPIResultType result = epcRestMgr.responseToObject(response, BayerPostProcessIniAPIResultType.class);

		if(session == null)
			session = epcRestMgr.getSessionCookie(response);

		if(!result.isSuccessFlag()){
			String errMsg="The interface failed to load any record due to data issues; please verify data.";
			if (result.getError() != null)
				errMsg=result.getError().toString();

			throw new SystemException(errMsg);
		} else {

			//List<StatusMessage> statusMsgList = new ArrayList<StatusMessage>();
			logger.debug("8000 -- Post Process Completed Successfully for Project: "+ projectId);
			int i=0;
		}
		return session;
	}


	private Cookie requestTrack(String projectId, Cookie session, 
			String baseUri) throws SystemException {

		BayerCommitmentPOLITrackHistoryAPIResultType request = new BayerCommitmentPOLITrackHistoryAPIResultType();
		//request.getBayerCommitmentLIAPI().addAll(subList);

		HashMap<String,String> filterMap = new HashMap<String, String>();
		filterMap.put(GlobalConstants.EPC_PROJECT_PARAM, projectId);

		PolthObjectFactory objectFactory = new PolthObjectFactory();
		JAXBElement<BayerCommitmentPOLITrackHistoryAPIResultType> requestWrapper = objectFactory.createBayerCommitmentPOLITrackHistoryAPIResult(request);


		ClientResponse response = epcRestMgr
				.postApplicationXmlAsApplicationXml(client, requestWrapper,
						baseUri, GlobalConstants.EPC_REST_TRACK_SAP_PO_LNITM,
						session, filterMap);

		logger.debug(response);
		BayerCommitmentPOLITrackHistoryAPIResultType result = epcRestMgr.responseToObject(response, BayerCommitmentPOLITrackHistoryAPIResultType.class);

		if(session == null)
			session = epcRestMgr.getSessionCookie(response);

		if(!result.isSuccessFlag()){
			String errMsg="The interface failed to load any record due to data issues; please verify data.";
			if (result.getError() != null)
				errMsg=result.getError().toString();

			throw new SystemException(errMsg);
		} else {

			//List<StatusMessage> statusMsgList = new ArrayList<StatusMessage>();
			logger.debug("5000 -- PO Line Item Track History Completed Successfully for Project: "+ projectId);
			int i=0;
		}
		return session;
	}

	private Cookie requestTrackIni(String projectId, Cookie session, 
			String baseUri) throws SystemException {

		BayerCommitmentPOLITrackHistoryIniAPIResultType request = new BayerCommitmentPOLITrackHistoryIniAPIResultType();
		//request.getBayerCommitmentLIAPI().addAll(subList);

		HashMap<String,String> filterMap = new HashMap<String, String>();
		filterMap.put(GlobalConstants.EPC_PROJECT_PARAM, projectId);

		PolthiObjectFactory objectFactory = new PolthiObjectFactory();
		JAXBElement<BayerCommitmentPOLITrackHistoryIniAPIResultType> requestWrapper = objectFactory.createBayerCommitmentPOLITrackHistoryIniAPIResult(request);


		ClientResponse response = epcRestMgr
				.postApplicationXmlAsApplicationXml(client, requestWrapper,
						baseUri, GlobalConstants.EPC_REST_TRACK_INI_SAP_PO_LNITM,
						session, filterMap);

		logger.debug(response);
		BayerCommitmentPOLITrackHistoryIniAPIResultType result = epcRestMgr.responseToObject(response, BayerCommitmentPOLITrackHistoryIniAPIResultType.class);

		if(session == null)
			session = epcRestMgr.getSessionCookie(response);

		if(!result.isSuccessFlag()){
			String errMsg="The interface failed to load any record due to data issues; please verify data.";
			if (result.getError() != null)
				errMsg=result.getError().toString();

			throw new SystemException(errMsg);
		} else {

			//List<StatusMessage> statusMsgList = new ArrayList<StatusMessage>();
			logger.debug("5000 -- PO Line Item Track History Initiation Completed Successfully for Project: "+ projectId);
			int i=0;
		}
		return session;
	}

	private void processStatusMessagesCo(List<PolcoApiErrorDAO> errorList, boolean isNew)  throws SystemException {
		try{
			//stgDBMgr.insertWbsBatch(this.wbsAPIErrorDAOList);
			if (isNew)
				stgDBMgr.insertPolcoBatch(errorList, GlobalConstants.EPC_POLCO_API_ERROR_BATCH_INSERT);
			else
				stgDBMgr.updatePolcoBatch(errorList, GlobalConstants.EPC_POLCO_API_ERROR_BATCH_UPDATE);

		} catch(Exception e) {
			throw new SystemException (e);
		}
	}

	private void processStatusMessages(List<PolApiErrorDAO> errorList, boolean isNew)  throws SystemException {
		try{
			//stgDBMgr.insertWbsBatch(this.wbsAPIErrorDAOList);
			if (isNew)
				stgDBMgr.insertPolBatch(errorList, GlobalConstants.EPC_POL_API_ERROR_BATCH_INSERT);
			else
				stgDBMgr.updatePolBatch(errorList, GlobalConstants.EPC_POL_API_ERROR_BATCH_UPDATE);

		} catch(Exception e) {
			throw new SystemException (e);
		}
	}
	private int deleteLogs(String projectId, String sql)  throws SystemException {
		try{
			int counter = stgDBMgr.deleteBatch(projectId, sql);
			return counter;
		} catch(Exception e) {
			throw new SystemException (e);
		}
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
		apiError.setFinalConfirmationID(apiType.getFinalConfirmationID());
		apiError.setDeletionFlagID(apiType.getDeletionFlagID());
		apiError.setPODistributionPCT(apiType.getPODistributionPCT());
		apiError.setPOQuantity(apiType.getPOQuantity());
		apiError.setSAPPurcharsingOrderSeqNumber(apiType.getSAPPurcharsingOrderSeqNumber());
		apiError.setSAPExchangeRate(apiType.getSAPExchangeRate());
		return apiError;
	}

	private PolcoApiErrorDAO getPolcoAPIErrorDAO(BayerCommitmentLICOV2APIType apiType)
	{
		PolcoApiErrorDAO apiError = new PolcoApiErrorDAO();
		apiError.setParentCostObjectExternalKey(apiType.getParentCostObjectExternalKey());
		//apiError.setCostObjectHierarchyPathID(apiType.getCostObjectHierarchyPathID());
		apiError.setCostObjectID(apiType.getCostObjectID());
		apiError.setExternalKey(apiType.getExternalKey());
		apiError.setCostObjectTypeName(apiType.getCostObjectTypeName());
		return apiError;
	}


	//Get ErrorList ID for reprocessed Error
	private List<PolApiErrorDAO> getErrorListWithId(List<PolApiErrorDAO> oldErrorList, List<PolApiErrorDAO> newErrorList){
		for (int i = 0; i < newErrorList.size(); i++)
		{
			long j = this.getErrorId(newErrorList.get(i).getCostObjectExternalKey(), 
					newErrorList.get(i).getCommitmentID(),
					oldErrorList);
			newErrorList.get(i).setId(j);;
		}
		return newErrorList;
	}

	//Get ErrorID for reprocessed Error
	private long getErrorId(String pathId, String commitId, List<PolApiErrorDAO> errorList){
		long id = 0;
		for (int i = 0; i < errorList.size(); i++)
		{

			if(errorList.get(i).getCostObjectExternalKey().equals(pathId)
					&&errorList.get(i).getCommitmentID().equals(commitId))
				id = errorList.get(i).getId();
		}
		return id;
	}

	//Convert PMOrderAPIErrorDAO list to BayerPMOrderAPIType List
	private List<BayerCommitmentLIAPIType> getBayerCommitmentLIAPITypesFromError(List<PolApiErrorDAO> errorList){

		BayerCommitmentLIAPIRequestType request = new BayerCommitmentLIAPIRequestType();
		for (int i = 0; i < errorList.size(); i++)
		{
			PolApiErrorDAO errorDAO = errorList.get(i);
			BayerCommitmentLIAPIType apiType = this.getBayerCommitmentLIAPITypeFromError(errorDAO);
			request.getBayerCommitmentLIAPI().add(apiType);
		}
		return request.getBayerCommitmentLIAPI();
	}

	private BayerCommitmentLIAPIType getBayerCommitmentLIAPITypeFromError(PolApiErrorDAO errorDAO)
	{
		BayerCommitmentLIAPIType apiType = new BayerCommitmentLIAPIType();
		apiType.setCommitmentID(errorDAO.getCommitmentID());
		apiType.setConversionRateCostObjectCurrency(errorDAO.getConversionRateCostObjectCurrency());
		apiType.setCostAccountID(errorDAO.getCostAccountID());
		apiType.setCostAccountName(errorDAO.getCostAccountName());
		apiType.setCostCostObjectCurrency(errorDAO.getCostCostObjectCurrency());
		apiType.setCostObjectExternalKey(errorDAO.getCostObjectExternalKey());
		//apiType.setCostObjectHierarchyPathID(errorDAO.getCostObjectHierarchyPathID());
		apiType.setCostObjectID(errorDAO.getCostObjectID());
		apiType.setCostTransactionCurrency(errorDAO.getCostTransactionCurrency());
		apiType.setActualCostTransactionCurrency(errorDAO.getActualCostTransactionCurrency());
		apiType.setObligo(errorDAO.getObligo());
		apiType.setCurrencyCostObjectCode(errorDAO.getCurrencyCostObjectCode());
		apiType.setCurrencyTransactionCode(errorDAO.getCurrencyTransactionCode());
		apiType.setDeletionFlagID(errorDAO.getDeletionFlagID());
		apiType.setExternalKey(errorDAO.getExternalKey());
		apiType.setSAPPurchaseRequisitionLineItemNumbe(errorDAO.getSAPPurchaseRequisitionLineItemNumbe());
		apiType.setSAPPurchaseRequisitionNumber(errorDAO.getSAPPurchaseRequisitionNumber());
		apiType.setSAPPurchasingDocumentLineItemNumber(errorDAO.getSAPPurchasingDocumentLineItemNumber());
		apiType.setSAPPurchasingDocumentNumberID(errorDAO.getSAPPurchasingDocumentNumberID());
		apiType.setSAPWBSElement(errorDAO.getSAPWBSElement());
		apiType.setTransactionDate(errorDAO.getTransactionDate());
		apiType.setVersionID(errorDAO.getVersionID());
		apiType.setFinalConfirmationID(errorDAO.getFinalConfirmationID());
		apiType.setDeletionFlagID(errorDAO.getDeletionFlagID());
		apiType.setSAPPurcharsingOrderSeqNumber(errorDAO.getSAPPurcharsingOrderSeqNumber());
		apiType.setUnitofMeasureID(errorDAO.getUnitofMeasureID());
		apiType.setPODistributionPCT(errorDAO.getPODistributionPCT());
		apiType.setPOQuantity(errorDAO.getPOQuantity());
		apiType.setSAPExchangeRate(errorDAO.getSAPExchangeRate());
		apiType.setSAPPRProcessingState(errorDAO.getSAPPRProcessingState());
		apiType.setSAPPRProcessingStatus(errorDAO.getSAPPRProcessingStatus());
		return apiType;
	}


	private List<BayerCommitmentLICOV2APIType> readSapDataCO(String projectId, String systemId) throws SystemException {
		Map<String, Map<String, Object>> dataRows = odataSvcMgr.readPOLineItems(projectId, systemId);
		List<BayerCommitmentLICOV2APIType> apiTypes = new ArrayList<BayerCommitmentLICOV2APIType>();
		if (dataRows!=null)
		{
			Map<String, BayerCommitmentLICOV2APIType> apiList = odataSvcMgr.mapPOLineItemCOV2ForImport(dataRows);
			apiTypes.addAll(apiList.values());
		}
		return apiTypes;
	}

	private List<BayerCommitmentLIAPIType> readSapData(String projectId, String systemId) throws SystemException {
		Map<String, Map<String, Object>> dataRows = odataSvcMgr.readPOLineItems(projectId, systemId);
		List<BayerCommitmentLIAPIType> apiTypes = new ArrayList<BayerCommitmentLIAPIType>();
		if (dataRows!=null)
		{
			Map<String, BayerCommitmentLIAPIType> apiList = odataSvcMgr.mapPOLineItemForImport(dataRows);
			apiTypes.addAll(apiList.values());			
		}
		return apiTypes;
	}

	private List<BayerCommitmentLIAPIType> readSapPRLData(String projectId, String systemId) throws SystemException {
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

	public List<BayerCommitmentLIAPIType> getBayerCommitmentLIAPITypesValid (List<BayerCommitmentLIAPIType> apiTypes, List<BayerCommitmentLICOV2APIType> commitmentLICOs, String sapProjectId) {	
		
		BayerCommitmentLIAPIRequestType request = new BayerCommitmentLIAPIRequestType();	    	
		List<BayerCommitmentLIAPIType>apiTypes2 = hasParentCostObject(apiTypes,sapProjectId);
		for (int i = 0; i < apiTypes2.size(); i++) {
			BayerCommitmentLIAPIType apiType = apiTypes2.get(i);
			if (isValidAPIType(apiType))
				request.getBayerCommitmentLIAPI().add(apiType);
			}
		return request.getBayerCommitmentLIAPI();
	}
	
	// Check if POL has valid parent cost object in EcoSys//		Set<String> extKeys = null;//		Map<String, String> parentMap = commitmentLICOs.stream()//				.collect(Collectors.toMap(e -> e.getExternalKey(), p -> p.getParentCostObjectExternalKey()));							//		try {//			List<BayerWBSReadAPIType> wbsPmoType = readCOByType(sapProjectId, "PM Order");		//			List<BayerWBSReadAPIType> wbsWbsType = readCOByType(sapProjectId, "WBS");//			wbsPmoType.addAll(wbsWbsType);//			extKeys = wbsPmoType.stream()//					.map(ext -> ext.getExternalKey())//					.collect(Collectors.toSet());////			logger.debug("EcoSys returned " +extKeys.size() + " unique External Keys");//			logger.debug("LICO mapped " +parentMap.size() + " mapped External Keys");//		} catch (SystemException e) {//			System.out.println("System exception occurred: " + e + " integration continuing");//		}
	
	public List<BayerCommitmentLICOV2APIType> getBayerCommitmentLIAPITypes(List<BayerCommitmentLIAPIType> colApiTypes){			
		BayerCommitmentLICOV2APIRequestType request = new BayerCommitmentLICOV2APIRequestType();
		for (int i = 0; i < colApiTypes.size(); i++)
		{
			BayerCommitmentLIAPIType colApiType = colApiTypes.get(i);
			if (isValidAPIType(colApiType))
			{
				BayerCommitmentLICOV2APIType apiType = this.getBayerCommitmentLICOV2APIType(colApiType);
				request.getBayerCommitmentLICOV2API().add(apiType);
			}
		}
		return request.getBayerCommitmentLICOV2API();
	}	

	public List<BayerCommitmentLIAPIType> getBayerCommitmentLIAPITypesPRStatus
	(List<BayerCommitmentLIAPIType> polList, List<BayerCommitmentLIAPIType> prlList){			
		BayerCommitmentLIAPIRequestType request = new BayerCommitmentLIAPIRequestType();
		for (int i = 0; i < polList.size(); i++)
		{
			BayerCommitmentLIAPIType pol = polList.get(i);
			if (isValidAPIType(pol))
			{
				BayerCommitmentLIAPIType apiType = this.getBayerCommitmentLIAPITypePRStatus(pol, prlList);
				request.getBayerCommitmentLIAPI().add(apiType);
			}
		}
		return request.getBayerCommitmentLIAPI();
	}	

	public BayerCommitmentLIAPIType getBayerCommitmentLIAPITypePRStatus
	(BayerCommitmentLIAPIType pol, List<BayerCommitmentLIAPIType> prlList){			
		BayerCommitmentLIAPIRequestType request = new BayerCommitmentLIAPIRequestType();
		BayerCommitmentLIAPIType apiType = pol;
		String prId = apiType.getSAPPurchaseRequisitionNumber();
		String prlId = apiType.getSAPPurchaseRequisitionLineItemNumbe();
		for (int i = 0; i < prlList.size(); i++)
		{
			BayerCommitmentLIAPIType prl = prlList.get(i);
			if (prId.equalsIgnoreCase(prl.getSAPPurchaseRequisitionNumber())
					&& prlId.equalsIgnoreCase(prl.getSAPPurchaseRequisitionLineItemNumbe()))
			{
				apiType.setSAPPRProcessingState(prl.getSAPPRProcessingState());
				apiType.setSAPPRProcessingStatus(prl.getSAPPRProcessingStatus());
				apiType.setCostExternal(prl.getCostTransactionCurrency()*prl.getConversionRateCostObjectCurrency());
				apiType.setSAPPRExchangeRate(prl.getConversionRateCostObjectCurrency());
				apiType.setAlternateCostExternal(prl.getCostTransactionCurrency());
			}
		}
		if (apiType.getSAPPRProcessingStatus()!=null 
				&& apiType.getSAPPRProcessingStatus().equalsIgnoreCase(GlobalConstants.SAP_PR_PROC_STATUS_N))
			apiType.setObligo(0.0);

		if (apiType.getSAPPRProcessingStatus()!=null 
				&& apiType.getSAPPRProcessingStatus().equalsIgnoreCase(GlobalConstants.SAP_PR_PROC_STATUS_B)
				&& !apiType.getSAPPRProcessingState().equalsIgnoreCase(GlobalConstants.SAP_PR_PROC_STATE_05))
			apiType.setObligo(0.0);

		return apiType;
	}	

	public BayerCommitmentLICOV2APIType getBayerCommitmentLICOV2APIType(BayerCommitmentLIAPIType colApiType)
	{
		BayerCommitmentLICOV2APIType apiType = new BayerCommitmentLICOV2APIType();
		String pohId = colApiType.getCommitmentID() +"_"+ colApiType.getSAPPurchasingDocumentLineItemNumber();
		apiType.setParentCostObjectExternalKey(colApiType.getCostObjectExternalKey());
		//String wbsElement = colApiType.getCostObjectExternalKey();			
		apiType.setCostObjectID(pohId);
		apiType.setCostObjectName(pohId);
		apiType.setExternalKey(pohId);
		apiType.setCostObjectTypeName(GlobalConstants.EPC_REST_COSTOBJECTTYPE_POC);
		return apiType;
	}
	//End Section Processing CommitmentLICO
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

	private void processStatusMessagesCA(List<PolcoApiErrorDAO> errorList, boolean isNew)  throws SystemException {
		try{
			//stgDBMgr.insertWbsBatch(this.wbsAPIErrorDAOList);
			if (isNew)
				stgDBMgr.insertPolcoBatch(errorList, GlobalConstants.EPC_POLCO_API_ERROR_BATCH_INSERT);
			else
				stgDBMgr.updatePolcoBatch(errorList, GlobalConstants.EPC_POLCO_API_ERROR_BATCH_UPDATE);

		} catch(Exception e) {
			throw new SystemException (e);
		}
	}


	private boolean isValidCADataType (BayerCommitmentLIAPIType apiType)
	{
		boolean isValid = false;
		String caId = apiType.getCostAccountID();
		if (caId!=null || !caId.equals(""))
			isValid = true;

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
	//End Section Processing Cost Accounts


	//Start Section Bayer Hexagon Sample

	//Convert SapPOLODataType List to BayerCostAccountsAPIType List
	//Convert SAPWBSODataType List to BayerWBSAPIType List
	public List<BayerCommitmentLIAPIType> getBayerCommitmentLIAPITypesSample(List<SapPOLODataType> oDataTypes){			
		BayerCommitmentLIAPIRequestType request = new BayerCommitmentLIAPIRequestType();
		for (int i = 0; i < oDataTypes.size(); i++)
		{
			SapPOLODataType oDataType = oDataTypes.get(i);
			BayerCommitmentLIAPIType apiType = this.getBayerCommitmentLIAPITypeSample(oDataType);
			request.getBayerCommitmentLIAPI().add(apiType);
		}
		return request.getBayerCommitmentLIAPI();
	}


	//Convert SAP ODataType List to Bayer API Type List
	public List<BayerCommitmentLICOV2APIType> getBayerCommitmentLICOV2APITypesSample(List<SapPOLODataType> oDataTypes){			
		BayerCommitmentLICOV2APIRequestType request = new BayerCommitmentLICOV2APIRequestType();
		for (int i = 0; i < oDataTypes.size(); i++)
		{
			SapPOLODataType oDataType = oDataTypes.get(i);
			if (isValidODataTypeSample(oDataType))
			{
				BayerCommitmentLICOV2APIType apiType = this.getCommitmentLICOAPITypeSample(oDataType);
				request.getBayerCommitmentLICOV2API().add(apiType);
			}
		}
		return request.getBayerCommitmentLICOV2API();
	}	    

	public List<PolApiErrorDAO> getBayerCommitmentLIAPITypesInvalidSample(List<SapPOLODataType> oDataTypes){

		List<PolApiErrorDAO> errorList = new ArrayList<PolApiErrorDAO>();
		for (int i = 0; i < oDataTypes.size(); i++)
		{
			SapPOLODataType oDataType = oDataTypes.get(i);
			if (!isValidODataTypeSample(oDataType))
			{
				BayerCommitmentLIAPIType apiType = this.getCommitmentLIAPITypeSampleRaw(oDataType);
				PolApiErrorDAO errorDAO = this.getPolAPIErrorDAO(apiType);
				errorDAO.setStatus(GlobalConstants.STAGING_RECORD_STATUS_SKIPPED);
				errorList.add(errorDAO);
			}
		}
		return errorList;
	}

	private boolean isValidODataTypeSample (SapPOLODataType oDataType)
	{
		boolean isValid = true;
		BayerCommitmentLIAPIType apiType = new BayerCommitmentLIAPIType();
		String projectId = oDataType.getProjectDefinition();
		String wbsId = oDataType.getWbsElement();
		String pmoId = oDataType.getPmOrder();
		String commitmentId = oDataType.getPurchOrd();
		if (projectId == null ||projectId.equals("")||
				((wbsId == null ||wbsId.equals("")) 
						&& (pmoId==null||pmoId.equals("")))||
				commitmentId == null||commitmentId.equals(""))
			isValid = false;
		return isValid;
	}

	/* Convert SapWBSODataType Object to BayerCommitmentLIAPIType Object
	 * 
	 */
	public BayerCommitmentLIAPIType getBayerCommitmentLIAPITypeSample(SapPOLODataType oDataType)
	{
		BayerCommitmentLIAPIType apiType = new BayerCommitmentLIAPIType();
		String projectId = oDataType.getProjectDefinition();
		String pohId = oDataType.getPurchOrd()+"_"+oDataType.getPurchOrdItem();
		double obligo = 0.0;
		/*
			String pohId = oDataType.getPurchOrd();
			String wbsElement = oDataType.getWbsElement();
			String pmoId = oDataType.getPmOrder();

			if (pmoId!=null&&!pmoId.equals(""))
				apiType.setCostObjectExternalKey(pmoId);
			else 
				apiType.setCostObjectExternalKey(oDataType.getWbsElement());
		 */

		apiType.setCostObjectExternalKey(pohId);
		apiType.setCommitmentID(oDataType.getPurchOrd());
		apiType.setSAPPurchasingDocumentNumberID(oDataType.getPurchOrd());
		apiType.setSAPPurchasingDocumentLineItemNumber(oDataType.getPurchOrdItem());
		apiType.setCostTransactionCurrency(oDataType.getPoComCost());
		apiType.setActualCostTransactionCurrency(oDataType.getPoActCost());
		apiType.setCostAccountID(oDataType.getCostElement());
		apiType.setCostAccountName(oDataType.getCostElementDesc());
		apiType.setCurrencyTransactionCode(oDataType.getPoDocCurr());
		apiType.setConversionRateCostObjectCurrency(oDataType.getExchangeRate());
		apiType.setTransactionDate(oDataType.getPoDate());
		apiType.setDeletionFlagID(oDataType.getDelFlag());
		apiType.setSAPPurchaseRequisitionNumber(oDataType.getPurReq());
		apiType.setSAPPurchaseRequisitionLineItemNumbe(oDataType.getPurReqItem());
		apiType.setSAPWBSElement(oDataType.getWbsElement());
		apiType.setFinalConfirmationID(oDataType.getFinalConf());
		obligo = apiType.getCostTransactionCurrency() - apiType.getActualCostTransactionCurrency();
		//obligo = obligo * 
		if (obligo < 0.0 || apiType.getFinalConfirmationID().equals(GlobalConstants.EPC_API_ERROR_FLAG_Y))
			obligo = 0.0;

		apiType.setObligo(obligo);

		return apiType;
	}

	public BayerCommitmentLIAPIType getCommitmentLIAPITypeSampleRaw(SapPOLODataType oDataType)
	{
		BayerCommitmentLIAPIType apiType = new BayerCommitmentLIAPIType();
		String projectId = oDataType.getProjectDefinition();
		String pohId = oDataType.getPurchOrd()+"_"+oDataType.getPurchOrdItem();
		double obligo = 0.0;

		//String pohId = oDataType.getPurchOrd();
		String wbsElement = oDataType.getWbsElement();
		String pmoId = oDataType.getPmOrder();
		if (pmoId!=null&&!pmoId.equals(""))
			apiType.setCostObjectExternalKey(pmoId);
		else 
			apiType.setCostObjectExternalKey(oDataType.getWbsElement());
		//apiType.setCostObjectExternalKey(pohId);

		apiType.setCommitmentID(oDataType.getPurchOrd());
		apiType.setSAPPurchasingDocumentNumberID(oDataType.getPurchOrd());
		apiType.setSAPPurchasingDocumentLineItemNumber(oDataType.getPurchOrdItem());
		apiType.setCostTransactionCurrency(oDataType.getPoComCost());
		apiType.setActualCostTransactionCurrency(oDataType.getPoActCost());
		apiType.setCostAccountID(oDataType.getCostElement());
		apiType.setCostAccountName(oDataType.getCostElementDesc());
		apiType.setCurrencyTransactionCode(oDataType.getPoDocCurr());
		apiType.setConversionRateCostObjectCurrency(oDataType.getExchangeRate());
		apiType.setTransactionDate(oDataType.getPoDate());
		apiType.setDeletionFlagID(oDataType.getDelFlag());
		apiType.setSAPPurchaseRequisitionNumber(oDataType.getPurReq());
		apiType.setSAPPurchaseRequisitionLineItemNumbe(oDataType.getPurReqItem());
		apiType.setSAPWBSElement(apiType.getCostObjectExternalKey());
		apiType.setFinalConfirmationID(oDataType.getFinalConf());
		obligo = apiType.getCostTransactionCurrency() - apiType.getActualCostTransactionCurrency();
		if (obligo < 0.0 || apiType.getFinalConfirmationID().equals(GlobalConstants.EPC_API_ERROR_FLAG_Y))
			obligo = 0.0;

		apiType.setObligo(obligo);

		return apiType;
	}

	/* Convert SapWBSODataType Object to BayerCommitmentLICOV2APIType Object
	 * 
	 */
	public BayerCommitmentLICOV2APIType getCommitmentLICOAPITypeSample(SapPOLODataType oDataType)
	{
		BayerCommitmentLICOV2APIType apiType = new BayerCommitmentLICOV2APIType();
		String projectId = oDataType.getProjectDefinition();
		String pohId = oDataType.getPurchOrd()+"_"+oDataType.getPurchOrdItem();
		//String wbsElement = oDataType.getWbsElement();
		String pmoId = oDataType.getPmOrder();

		if (pmoId!=null&&!pmoId.equals(""))
			apiType.setParentCostObjectExternalKey(pmoId);
		else 
			apiType.setParentCostObjectExternalKey(oDataType.getWbsElement());

		//if (apiType.getCostObjectExternalKey().equals("A00GV-999990-C1"))
		//apiType.setCostObjectHierarchyPathID("A00GV-999990.C.1");

		apiType.setCostObjectID(pohId);
		apiType.setCostObjectName(oDataType.getPurchOrdDesc());
		apiType.setExternalKey(pohId);
		apiType.setCostObjectTypeName(GlobalConstants.EPC_REST_COSTOBJECTTYPE_POC);
		/*
			apiType.setSAPPurchasingDocumentNumberID(pohId);
			apiType.setSAPPurchasingDocumentLineItemNumber(oDataType.getPurchOrdItem());
			apiType.setCostTransactionCurrency(oDataType.getPoComCost());
			apiType.setCostCostObjectCurrency(oDataType.getPoComCost());
			apiType.setCostAccountID(oDataType.getCostElement());
			apiType.setCostAccountName(oDataType.getCostElementDesc());
			apiType.setCurrencyTransactionCode(oDataType.getPoDocCurr());
			apiType.setConversionRateCostObjectCurrency(oDataType.getExchangeRate());
			apiType.setTransactionDate(oDataType.getPoDate());
			apiType.setDeletionFlagID(oDataType.getDelFlag());
			apiType.setSAPPurchaseRequisitionNumber(oDataType.getPurReq());
			apiType.setSAPPurchaseRequisitionLineItemNumbe(oDataType.getPurReqItem());
			apiType.setSAPWBSElement(oDataType.getWbsElement());
			apiType.setFinalConfirmationID(oDataType.getFinalConf());
		 */
		return apiType;
	}

	public List<BayerCostAccountsAPIType> getBayerCostAccountsAPITypesSample(List<SapPOLODataType> oDataTypes){

		BayerCostAccountsAPIRequestType request = new BayerCostAccountsAPIRequestType();
		Map<String, BayerCostAccountsAPIType> caMap = new HashMap<String, BayerCostAccountsAPIType>();
		for (int i = 0; i < oDataTypes.size(); i++)
		{
			SapPOLODataType oDataType = oDataTypes.get(i);

			if (isValidCADataTypeSample(oDataType))
			{
				BayerCostAccountsAPIType apiType = this.getBayerCostAccountsAPITypeSample(oDataType);
				if (!caMap.containsKey(apiType.getID()))
				{
					request.getBayerCostAccountsAPI().add(apiType);
					caMap.put(apiType.getID(), apiType);
				}
			}
		}
		return request.getBayerCostAccountsAPI();
	}

	public BayerCostAccountsAPIType getBayerCostAccountsAPITypeSample(SapPOLODataType oDataType)
	{
		BayerCostAccountsAPIType apiType = new BayerCostAccountsAPIType();
		String projectId = oDataType.getProjectDefinition();
		apiType.setID(oDataType.getCostElement());
		apiType.setName(oDataType.getCostElementDesc());
		//apiType.setName(oDataType.getCostElement());
		return apiType;
	}

	private boolean isValidCADataTypeSample (SapPOLODataType oDataType)
	{
		boolean isValid = false;
		BayerActualsAPIType apiType = new BayerActualsAPIType();
		String caId = oDataType.getCostElement();
		if (caId!=null || !caId.equals(""))
			isValid = true;

		return isValid;
	}
	//End Section Bayer Hexagon Sample

	//Parent CO Moved Section

	private  List<BayerCommitmentLICOV2APIType> readCommitmentLICOs(String projectId) throws SystemException{

		List<BayerCommitmentLICOV2APIType> bayerCommitmentLICOs = new ArrayList<BayerCommitmentLICOV2APIType>();
		try 
		{	
			//Read PM Order Data from EcoSys using PMOrder API
			Cookie session = null;
			String baseUri2 = GlobalConstants.EPC_REST_PROTOCOL + "://" + GlobalConstants.EPC_REST_SERVER + ":"
					+ GlobalConstants.EPC_REST_PORT + "/" + GlobalConstants.EPC_REST_BASEURI2;				

			HashMap<String, String> aMap = new HashMap<String, String>();
			aMap.put(GlobalConstants.EPC_PROJECT_PARAM, projectId);
			bayerCommitmentLICOs = this.getCommitmentLICOV2APITypes(session, baseUri2, aMap);
			return bayerCommitmentLICOs;

		} catch(Exception e) {
			throw new SystemException(e);
		}
	}

	private List<BayerCommitmentLICOV2APIType> getCommitmentLICOV2APITypes(Cookie session, String baseUri, HashMap<String, String> polcoMap) throws SystemException {

		ClientResponse response = epcRestMgr
				.getAsApplicationXml(client, baseUri,
						GlobalConstants.EPC_REST_IMPORT_SAP_PO_LNITM_CO_V2,session,polcoMap);

		logger.debug(response);
		BayerCommitmentLICOV2APIResultType result = epcRestMgr.responseToObject(response, BayerCommitmentLICOV2APIResultType.class);
		List<BayerCommitmentLICOV2APIType> apiTypes = result.getBayerCommitmentLICOV2API();
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

	private  List<BayerCommitmentLICOV2APIType> getMovedCommitmentLICOs(List<BayerCommitmentLICOV2APIType> currentList, List<BayerCommitmentLICOV2APIType> newList, XMLGregorianCalendar currentDate){

		List<BayerCommitmentLICOV2APIType> movedCOs = new ArrayList<BayerCommitmentLICOV2APIType>();
		for(BayerCommitmentLICOV2APIType lico: currentList)
		{
			if (isMoved(lico, newList)==true)
			{
				lico.setCOParentChangedID(GlobalConstants.EPC_API_ERROR_FLAG_Y);
				lico.setParentCOChangeDate(currentDate);
				lico.setExternalKey(lico.getExternalKey() + GlobalConstants.EPC_CO_Changed+currentDate.toString());
				lico.setNewlyRetiredID(GlobalConstants.EPC_API_ERROR_FLAG_Y);
				movedCOs.add(lico);
			}
			if (lico.getExternalKey().equalsIgnoreCase("120750132637"))
			{
				lico.setCOParentChangedID(GlobalConstants.EPC_API_ERROR_FLAG_Y);
				lico.setParentCOChangeDate(currentDate);
				lico.setExternalKey(lico.getExternalKey() + GlobalConstants.EPC_CO_Changed +currentDate.toString());
				//pmorder.setCostObjectID((pmorder.getCostObjectID() + GlobalConstants.EPC_CO_Changed));
				lico.setParentCostObjectExternalKey("A00NC-003247-C2H11");
				lico.setNewlyRetiredID(GlobalConstants.EPC_API_ERROR_FLAG_Y);
				movedCOs.add(lico);
			}
		}
		return movedCOs;
	}

	private  List<BayerCommitmentLICOV2APIType> getReMovedCommitmentLICOs(List<BayerCommitmentLICOV2APIType> currentList, List<BayerCommitmentLICOV2APIType> newList, XMLGregorianCalendar currentDate){

		List<BayerCommitmentLICOV2APIType> movedCOs = new ArrayList<BayerCommitmentLICOV2APIType>();
		for(BayerCommitmentLICOV2APIType lico: currentList)
		{
			if (isReMoved(lico, newList)==true)
			{
				lico.setCOParentChangedID(GlobalConstants.EPC_API_ERROR_FLAG_N);
				lico.setParentCOChangeDate(null);
				lico.setExternalKey(lico.getExternalKey() + GlobalConstants.EPC_CO_RE_Changed);
				movedCOs.add(lico);
			}
		}
		return movedCOs;
	}


	private  List<BayerCommitmentLICOV2APIType> getReMovedCommitmentLICOsActivated(List<BayerCommitmentLICOV2APIType> reMovedList){

		List<BayerCommitmentLICOV2APIType> movedCOs = new ArrayList<BayerCommitmentLICOV2APIType>();
		for(BayerCommitmentLICOV2APIType lico: reMovedList)
		{
			lico.setCOParentChangedID(GlobalConstants.EPC_API_ERROR_FLAG_N);
			lico.setParentCOChangeDate(null);
			lico.setExternalKey(lico.getExternalKey().replaceAll(GlobalConstants.EPC_CO_Changed, "-"));
			movedCOs.add(lico);
		}
		return movedCOs;
	}

	private boolean isMoved (BayerCommitmentLICOV2APIType apiType,List<BayerCommitmentLICOV2APIType> newList )
	{
		boolean isMoved = false;
		for(BayerCommitmentLICOV2APIType lico: newList)
		{
			if (lico.getExternalKey().equalsIgnoreCase(apiType.getExternalKey())
					&& !lico.getParentCostObjectExternalKey().equalsIgnoreCase(apiType.getParentCostObjectExternalKey()))
				isMoved = true;
		}
		return isMoved;
	}

	private boolean isReMoved (BayerCommitmentLICOV2APIType apiType,List<BayerCommitmentLICOV2APIType> newList )
	{
		boolean isMoved = false;
		for(BayerCommitmentLICOV2APIType lico: newList)
		{
			if (apiType.getExternalKey().contains(lico.getExternalKey()+GlobalConstants.EPC_CO_Changed)
					&& lico.getParentCostObjectExternalKey().equalsIgnoreCase(apiType.getParentCostObjectExternalKey()))
				isMoved = true;

		}
		return isMoved;
	}

	private  List<BayerCommitmentLICOV2APIType> getUpdatedLICOs(List<BayerCommitmentLICOV2APIType> movedList, List<BayerCommitmentLICOV2APIType> newList){

		List<BayerCommitmentLICOV2APIType> updatedLICOs = new ArrayList<BayerCommitmentLICOV2APIType>();
		for(BayerCommitmentLICOV2APIType lico: newList)
		{
			lico = this.getLicoUpdated(lico, movedList);
			updatedLICOs.add(lico);
		}
		return updatedLICOs;
	}

	private  List<BayerCommitmentLICOV2APIType> getCOCurrencyUpdatedLICOs(String projectCurrency, List<BayerCommitmentLICOV2APIType> newList){

		List<BayerCommitmentLICOV2APIType> updatedLICOs = new ArrayList<BayerCommitmentLICOV2APIType>();
		for(BayerCommitmentLICOV2APIType lico: newList)
		{
			lico.setCostObjectCurrencyCode(projectCurrency);
			updatedLICOs.add(lico);
		}
		return updatedLICOs;
	}



	private BayerCommitmentLICOV2APIType getLicoUpdated (BayerCommitmentLICOV2APIType apiType,List<BayerCommitmentLICOV2APIType> movedList )
	{
		BayerCommitmentLICOV2APIType licoOld = apiType;
		for(BayerCommitmentLICOV2APIType lico: movedList)
		{
			String extKey = lico.getExternalKey().replaceFirst(GlobalConstants.EPC_CO_Changed, "").trim();
			if (extKey.contains(apiType.getExternalKey())
					&& !lico.getParentCostObjectExternalKey().equalsIgnoreCase(apiType.getParentCostObjectExternalKey()))
			{ if(lico.getCostControlLevelID()!=null)
				licoOld.setCostControlLevelID(lico.getCostControlLevelID());
			}
		}
		return licoOld;
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

	private  List<BayerCommitmentLIHistoryAPIType> readCommitmentLIHISs(String projectId) throws SystemException{

		List<BayerCommitmentLIHistoryAPIType> bayerCommitmentLIs = new ArrayList<BayerCommitmentLIHistoryAPIType>();
		try 
		{	
			//Read PM Order Data from EcoSys using PMOrder API
			Cookie session = null;
			String baseUri2 = GlobalConstants.EPC_REST_PROTOCOL + "://" + GlobalConstants.EPC_REST_SERVER + ":"
					+ GlobalConstants.EPC_REST_PORT + "/" + GlobalConstants.EPC_REST_BASEURI2;				

			HashMap<String, String> aMap = new HashMap<String, String>();
			aMap.put(GlobalConstants.EPC_PROJECT_PARAM, projectId);
			bayerCommitmentLIs = this.getCommitmentLIHistoryAPITypes(session, baseUri2, aMap);
			return bayerCommitmentLIs;

		} catch(Exception e) {
			throw new SystemException(e);
		}
	}

	private List<BayerCommitmentLIHistoryAPIType> getCommitmentLIHistoryAPITypes(Cookie session, String baseUri, HashMap<String, String> polMap) throws SystemException {

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


	private List<BayerPOPRHeadersV2APIType> readSapPOHData(String projectHierarchyPathId, String projectId, String systemId) throws SystemException {
		Map<String, Map<String, Object>> dataRows = odataSvcMgr.readPOHeader(projectId, systemId);
		List<BayerPOPRHeadersV2APIType> apiTypes = new ArrayList<BayerPOPRHeadersV2APIType>();
		if (dataRows!=null)
		{
			Map<String, BayerPOPRHeadersV2APIType> apiList = odataSvcMgr.mapPOHeaderV2ForImport(projectHierarchyPathId, dataRows);
			apiTypes.addAll(apiList.values());
		}
		return apiTypes;
	}

	private  List<BayerCommitmentLICOV2APIType> getCOUpdatedLICOs(String projectCurrency, List<BayerPOPRHeadersV2APIType> pohList, List<BayerCommitmentLICOV2APIType> polList){

		List<BayerCommitmentLICOV2APIType> updatedLICOs = new ArrayList<BayerCommitmentLICOV2APIType>();
		for(BayerCommitmentLICOV2APIType lico: polList)
		{
			String poId = lico.getCostObjectID();
			poId = poId.substring(0, poId.indexOf("-"));
			String vId = "";
			String vName ="";
			lico.setCostObjectCurrencyCode(projectCurrency);

			//BayerPOPRHeadersV2APIType poh = new BayerPOPRHeadersV2APIType();
			for(BayerPOPRHeadersV2APIType poh: pohList)
			{
				if (poh.getTaskID().equalsIgnoreCase(poId))
				{
					vId = poh.getVendorID();
					vName = poh.getVendorName();
				}
			}
			lico.setVendorID(vId);
			lico.setVendorName(vName);
			updatedLICOs.add(lico);
		}
		return updatedLICOs;
	}

	private  List<BayerWBSReadAPIType> readCOs(String projectId) throws SystemException{

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

	private  List<BayerCommitmentLICOV2APIType> getMissingParentLICOs(List<BayerCommitmentLICOV2APIType> inputList, List<BayerWBSReadAPIType> parentList, String defaultParent){

		List<BayerCommitmentLICOV2APIType> mParentCOs = new ArrayList<BayerCommitmentLICOV2APIType>();
		for(BayerCommitmentLICOV2APIType lico: inputList)
		{
			if (this.isParentMissing(lico, parentList))
			{
				lico.setParentCostObjectExternalKey(defaultParent);
				mParentCOs.add(lico);
			}
		}
		return mParentCOs;
	}


	private  List<BayerCommitmentLICOV2APIType> getValidParentLICOs(List<BayerCommitmentLICOV2APIType> inputList, List<BayerCommitmentLICOV2APIType> missingList, List<BayerWBSReadAPIType> parentList, String defaultParent){

		List<BayerCommitmentLICOV2APIType> mParentCOs = new ArrayList<BayerCommitmentLICOV2APIType>();
		for(BayerCommitmentLICOV2APIType lico: inputList)
		{
			if (!this.isParentMissing(lico, parentList))
			{
				mParentCOs.add(lico);
			}
			else
			{
				//lico.setParentCostObjectExternalKey(defaultParent);
				missingList.add(lico);
			}
		}
		return mParentCOs;
	}

	private  List<BayerCommitmentLICOV2APIType> getNoParentLICOs(List<BayerCommitmentLICOV2APIType> inputList, List<BayerWBSReadAPIType> parentList, String defaultParent){

		List<BayerCommitmentLICOV2APIType> mParentCOs = new ArrayList<BayerCommitmentLICOV2APIType>();
		for(BayerCommitmentLICOV2APIType lico: inputList)
		{
			if (this.isParentMissing(lico, parentList))
			{
				mParentCOs.add(lico);
			}
		}
		return mParentCOs;
	}

	private boolean isParentMissing (BayerCommitmentLICOV2APIType apiType,List<BayerWBSReadAPIType> parentList )
	{
		boolean isMissing = true;
		for(BayerWBSReadAPIType pco: parentList)
		{
			if (apiType.getParentCostObjectExternalKey().equalsIgnoreCase(pco.getExternalKey()))
				isMissing = false;
		}
		return isMissing;
	}

	private List<WbsApiErrorDAO> processWBS( List<BayerWBSAPIType> prjRecords, String projectId) throws SystemException{

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
				session = this.requestWBS(prjRecords.subList(i, end), session, baseUri, projectId, statusMsgList);
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

	private List<PolcoApiErrorDAO> processMissingParentCOs(List<BayerCommitmentLICOV2APIType> missingParentCOs, String projectId)
	{
		List<PolcoApiErrorDAO> errorList = new ArrayList<PolcoApiErrorDAO>();
		for(BayerCommitmentLICOV2APIType poLI: missingParentCOs)
		{
			String errMsg="The interface failed to load this PO Line Item due to parent Cost Object missing in EcoSys; please verify data.";
			//convert to APIErrorDAO type
			logger.error("ERROR Missing Parent CO --> Project Id: " + projectId + "|" 
					+ "CostObjectID: " + poLI.getCostObjectID() + "|"
					+ "CostObjectName: " + poLI.getCostObjectName()+ "|"
					+ "CostObjectStatus: " + poLI.getCostObjectStatus() + "|"
					+ "CostObjectTypeName: " + poLI.getCostObjectTypeName() + "|"
					+ "ExternalKey: " + poLI.getExternalKey()+ "|"
					+ "ParentCostObjectExternalKey: " + poLI.getParentCostObjectExternalKey() + "|"					
					+ "COParentChangedID: " + poLI.getCOParentChangedID() + "|"
					+ "ParentCOChangeDate " + poLI.getParentCOChangeDate()+ "|"
					+ "CostObjectCurrencyCode " + poLI.getCostObjectCurrencyCode());
			logger.error(errMsg);

			PolcoApiErrorDAO statusMsg = this.getPolcoAPIErrorDAO(poLI);
			statusMsg.setRootCostObjectID(projectId);
			statusMsg.setStatus(GlobalConstants.STAGING_RECORD_STATUS_FAILED);
			statusMsg.setErrorMsg(errMsg);
			errorList.add(statusMsg);
		}
		return errorList;
	}

	private  List<BayerCommitmentLIAPIType> removeParentMissingTrans(List<BayerCommitmentLIAPIType> inputList, List<BayerCommitmentLICOV2APIType> noParentLICOs, String projectId){

		List<BayerCommitmentLIAPIType> updatedLIs = new ArrayList<BayerCommitmentLIAPIType>();
		for(BayerCommitmentLIAPIType poLI: inputList)
		{
			String coId = poLI.getCostObjectExternalKey();
			if (!isParentCOMissing(coId,noParentLICOs))
			{
				updatedLIs.add(poLI);
			}
			else
			{
				String parentCO = poLI.getCostObjectExternalKey();
				parentCO = parentCO.substring(0, parentCO.indexOf(poLI.getCostObjectID())-1);
				logger.error("ERROR Missing Parent Cost Object in EcoSys --> PO Line Item:" + poLI.getCostObjectID() + "|"
						+ "Project ID: " + projectId + "|"
						+ "Parent Cost Object: " + parentCO + "|"
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
						+ "TransactionDate: " + poLI.getTransactionDate() + "|");	
				String errMsg="The interface skipped to load PO Line Item " +  poLI.getCostObjectID() + " due to parent Cost Object missing in EcoSys; please verify data.";
				logger.error(errMsg);
			}
		}
		return updatedLIs;
	}

	private boolean isParentCOMissing (String coId,List<BayerCommitmentLICOV2APIType> noParentLICOs )
	{
		boolean isMissing = false;
		for(BayerCommitmentLICOV2APIType pco: noParentLICOs)
		{
			if (pco.getExternalKey().equalsIgnoreCase(coId))
				isMissing = true;
		}
		return isMissing;
	}

	private  List<BayerWBSReadAPIType> readCOByType (String projectId, String type) throws SystemException{
		List<BayerWBSReadAPIType> wbsTypes = new ArrayList<BayerWBSReadAPIType>();
		try 
		{	
			Cookie session = null;
			String baseUri2 = GlobalConstants.EPC_REST_PROTOCOL + "://" + GlobalConstants.EPC_REST_SERVER + ":"
					+ GlobalConstants.EPC_REST_PORT + "/" + GlobalConstants.EPC_REST_BASEURI2;				
			HashMap<String, String> aMap = new HashMap<String, String>();
			aMap.put(GlobalConstants.EPC_PROJECT_PARAM, projectId);
			aMap.put("structureType", type);
			wbsTypes = this.getBayerWBSReadAPITypes(session, baseUri2, aMap);
			return wbsTypes;
		} catch(Exception e) {
			throw new SystemException(e);
		}
	}
}

