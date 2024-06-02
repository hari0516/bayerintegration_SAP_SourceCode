/**
 * 
 */
package com.bayer.integration.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.core.Cookie;
import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.joda.time.format.ISOPeriodFormat;

import com.bayer.integration.odata.SapACTODataType;
import com.bayer.integration.persistence.ActApiErrorDAO;
import com.bayer.integration.persistence.PolcoApiErrorDAO;
import com.bayer.integration.persistence.CaApiErrorDAO;
import com.bayer.integration.persistence.PmOrderApiErrorDAO;
import com.bayer.integration.persistence.TrcApiErrorDAO;
import com.bayer.integration.persistence.PrjApiErrorDAO;

import com.bayer.integration.properties.GlobalConstants;
import com.bayer.integration.rest.project.BayerProjectAPIRequestType;
import com.bayer.integration.rest.project.BayerProjectAPIType;
import com.bayer.integration.rest.project.BayerProjectAPIResultType;
import com.bayer.integration.rest.project.PRJObjectFactory;
import com.bayer.integration.rest.project.PRJObjectResultType;

import com.bayer.integration.rest.actual.BayerActualsAPIRequestType;
import com.bayer.integration.rest.actual.BayerActualsAPIResultType;
import com.bayer.integration.rest.actual.BayerActualsAPIType;
import com.bayer.integration.rest.actual.ObjectFactory;
import com.bayer.integration.rest.actual.ObjectResultType;
import com.bayer.integration.rest.calcwbs.BayerCalculateWBSIDAPIResultType;
import com.bayer.integration.rest.calcwbs.CalcWObjectFactory;
import com.bayer.integration.rest.ccl.BayerCCLValidationAPIResultType;
import com.bayer.integration.rest.ccl.ObjectFactoryCCL;
import com.bayer.integration.rest.dco.BayerDirectChargeCOAPIRequestType;
import com.bayer.integration.rest.dco.BayerDirectChargeCOAPIResultType;
import com.bayer.integration.rest.dco.BayerDirectChargeCOAPIType;
import com.bayer.integration.rest.dco.DCObjectFactory;
import com.bayer.integration.rest.dco.DCObjectResultType;
import com.bayer.integration.rest.log.IntegrationIssuesAPIRequestType;
import com.bayer.integration.rest.log.IntegrationIssuesAPIResultType;
import com.bayer.integration.rest.log.IntegrationIssuesAPIType;
import com.bayer.integration.rest.log.LogObjectFactory;
import com.bayer.integration.rest.pmorder2.BayerPMOrderV2APIType;
import com.bayer.integration.rest.pol.BayerCommitmentLIAPIRequestType;
import com.bayer.integration.rest.pol.BayerCommitmentLIAPIType;
import com.bayer.integration.rest.polco.BayerCommitmentLICOAPIResultType;
import com.bayer.integration.rest.polco.BayerCommitmentLICOAPIType;
import com.bayer.integration.rest.polco2.BayerCommitmentLICOV2APIType;
import com.bayer.integration.rest.costaccount.BayerCostAccountsAPIRequestType;
import com.bayer.integration.rest.costaccount.BayerCostAccountsAPIResultType;
import com.bayer.integration.rest.costaccount.BayerCostAccountsAPIType;
import com.bayer.integration.rest.costaccount.CAObjectFactory;
import com.bayer.integration.rest.costaccount.CAObjectResultType;

import com.bayer.integration.rest.trancategory.BayerTransactionCategoryAPIRequestType;
import com.bayer.integration.rest.trancategory.BayerTransactionCategoryAPIResultType;
import com.bayer.integration.rest.trancategory.BayerTransactionCategoryAPIType;
import com.bayer.integration.rest.trancategory.TRCObjectFactory;
import com.bayer.integration.rest.trancategory.TRCObjectResultType;


import com.bayer.integration.rest.wbsread.BayerWBSReadAPIRequestType;
import com.bayer.integration.rest.wbsread.BayerWBSReadAPIResultType;
import com.bayer.integration.rest.wbsread.BayerWBSReadAPIType;
import com.bayer.integration.rest.wbsread.WPObjectFactory;
import com.bayer.integration.rest.wbsread.WPObjectResultType;
import com.bayer.integration.utils.DebugBanner;
import com.bayer.integration.rest.postproc.BayerPostProcessAPIResultType;
import com.bayer.integration.rest.postproc.PPObjectFactory;
import com.bayer.integration.rest.postprocini.BayerPostProcessIniAPIResultType;
import com.bayer.integration.rest.postprocini.PPIniObjectFactory;

import com.bayer.integration.rest.prld.BayerCommitmentPRLIDeleteAPIResultType;
import com.bayer.integration.rest.prld.PrldObjectFactory;
import com.bayer.integration.rest.actpd.BayerActualsPeriodDeleteAPIResultType;
import com.bayer.integration.rest.actpd.ActpdObjectFactory;


import com.ecosys.exception.SystemException;
import com.ecosys.util.Stopwatch;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.org.apache.xerces.internal.dom.ElementNSImpl;


/**
 * @author pwng
 *
 */
public class ActualImportMgrImpl extends ImportManagerBase implements
ImportManager {

	/* (non-Javadoc)
	 * @see com.bayer.integration.service.ImportManager#importData()
	 */
	//@Override
	final String itgInterface = "ACT";
	List<IntegrationIssuesAPIType> itgIssueLog = new ArrayList<IntegrationIssuesAPIType>();

	public int importData() {
		int retCode=GlobalConstants.IMPORT_SAP_ACT_SUCCESS;
		try{			
			if (!GlobalConstants.SKIP_SAP_ACT_INTERFACE) {
				if(GlobalConstants.DEBUGMODE)
					DebugBanner.outputBanner(this.getClass().toString());

				boolean skipError = GlobalConstants.SKIP_LOG;
				boolean isLive = GlobalConstants.IS_LIVE_SAP;

				//Create Web Service Client
				if (client == null) 
					setClient(epcRestMgr.createClient(GlobalConstants.EPC_REST_USERNAME, GlobalConstants.EPC_REST_PASSWORD));

				//TODO: Remove these 2 lines of code
				Cookie session = null;
				ImportManagerHelper importHelper = new ImportManagerHelper();

				//Retrieve list of projects for SAP Integration from EcoSys				
				prjExpMgr.ExportData();
				List<BayerProjectAPIType> projectAPITypes = prjExpMgr.getBayerProjectAPITypes();

				DatatypeFactory dFactory = DatatypeFactory.newInstance();
				XMLGregorianCalendar currentDate = dFactory.newXMLGregorianCalendar(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
				
				if (GlobalConstants.PROCESS_SAP_YEARCLOSE && !GlobalConstants.EPC_YEAR_CLOSE_DATE.equalsIgnoreCase("2000-01-01"))
					currentDate = dFactory.newXMLGregorianCalendar(GlobalConstants.EPC_YEAR_CLOSE_DATE);

				/*
				 * Test First Day situation
				 * currentDate.add(dFactory.newDuration("-P16D"));;
				 */

				XMLGregorianCalendar reportDate = (XMLGregorianCalendar)currentDate.clone();
				reportDate.add(dFactory.newDuration("-P1D")); // reportDate minus 1 day
				Integer dayInMonth = currentDate.getDay();

				//Loop through the project list
				for (int i = 0; i < projectAPITypes.size(); i++) 
				{
					itgIssueLog.clear();
					BayerProjectAPIType projectAPIType = projectAPITypes.get(i);
					String masterProjectId = projectAPIType.getRootCostObjectID();
					String projectId = projectAPIType.getID();
					String projectInternalId = projectAPIType.getInternalID();
					GlobalConstants.EPC_PROJECT_ID_PROCESSED = projectId;
					String systemId = projectAPIType.getSAPSystemID();
					String sapProjectId = projectAPIType.getSapProjectId();
					String projectCurrency = projectAPIType.getRootCostObjectCurrencyCode();
					String migrationFlag = null;
					String startDate = null;
					String endDate = null;
					boolean isTracking = false;									
					List<ActApiErrorDAO> errorDAOList = new ArrayList<ActApiErrorDAO>();
					List<ActApiErrorDAO> errorMissingDAOList = new ArrayList<ActApiErrorDAO>();
					List<PolcoApiErrorDAO> errorCoDAOList = new ArrayList<PolcoApiErrorDAO>();
					List<CaApiErrorDAO> errorCaDAOList = new ArrayList<CaApiErrorDAO>();
					List<TrcApiErrorDAO> errorTrcDAOList = new ArrayList<TrcApiErrorDAO>();
					List<PrjApiErrorDAO> errorPrjDAOList = new ArrayList<PrjApiErrorDAO>();
					List<BayerCommitmentLICOAPIType> commitmentLICOs = new
							ArrayList<BayerCommitmentLICOAPIType>();				
					startDate = GlobalConstants.SAP_ACTUAL_START_DATE;
					try
					{						
						//DatatypeFactory dFactory = DatatypeFactory.newInstance();
						if (projectAPIType.getMigrationFlagID()!=null)
							migrationFlag = projectAPIType.getMigrationFlagID();

						if (projectAPIType.getPOHistoryTrackedID()!=null 
								&& projectAPIType.getPOHistoryTrackedID().equals(GlobalConstants.EPC_API_ERROR_FLAG_Y))
							isTracking = true;

						boolean isSub = false;
						if (projectAPIType.getProjectTypeID().equals(GlobalConstants.EPC_API_PROJECT_TYPE_SUB))
							isSub = true;

						XMLGregorianCalendar lastRunDate = null;
						XMLGregorianCalendar filterEndDate = dFactory.newXMLGregorianCalendar(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
						filterEndDate.add(dFactory.newDuration("-P1D"));
						XMLGregorianCalendar filterEndDate2 = (XMLGregorianCalendar)filterEndDate.clone();
						filterEndDate2.add(dFactory.newDuration("P1D"));

						if (migrationFlag!=null && migrationFlag.equals("Y") && projectAPIType.getSAPActualImportEndDate()!=null)
							filterEndDate = projectAPIType.getSAPActualImportEndDate();

						//endDate = filterEndDate.toString();
						String endMonth = String.valueOf(filterEndDate.getMonth());
						if (endMonth.length()==1)
							endMonth = "0"+endMonth; // prefix 0 for formatting
						String endDay = String.valueOf(filterEndDate.getDay());
						if (endDay.length()==1)
							endDay = "0"+endDay;
						
						// if process december then set dates
						if (GlobalConstants.PROCESS_SAP_YEARCLOSE) {
							startDate = String.valueOf(filterEndDate.getYear()-1)+ "12"+ "01";
							endDate = String.valueOf(filterEndDate.getYear()-1)+ "12"+ "31";							
						}
						else
						{	
							endDate = String.valueOf(filterEndDate.getYear())+ endMonth+ endDay; 

							if (projectAPIType.getSAPLastRunDate()!=null)
							{
								lastRunDate = projectAPIType.getSAPLastRunDate();
								//lastRunDate.add(dFactory.newDuration("-P1D"));
								//startDate = lastRunDate.toString();
								String startMonth = String.valueOf(lastRunDate.getMonth());
								if (startMonth.length()==1)
									startMonth = "0"+startMonth;

								String startDay = String.valueOf(lastRunDate.getDay());							
								if (lastRunDate.getDay()==1
										&&lastRunDate.getDay()==filterEndDate2.getDay()
										&& lastRunDate.getMonth()==filterEndDate2.getMonth()
										&& lastRunDate.getYear()==filterEndDate2.getYear())
								{
									startDate = String.valueOf(filterEndDate.getYear())+ endMonth+ "01";
								}
								else
								{
									startDay = "01";
									startDate = String.valueOf(lastRunDate.getYear())+ startMonth+ startDay; 
								}
							}
						}
						List<ActApiErrorDAO> invalidRecords = new ArrayList<ActApiErrorDAO>();
						List<BayerCostAccountsAPIType> costAccounts = new ArrayList<BayerCostAccountsAPIType>();
						List<BayerTransactionCategoryAPIType> trcVendors = new ArrayList<BayerTransactionCategoryAPIType>();
						List<BayerDirectChargeCOAPIType> directChargeCOs = new ArrayList<BayerDirectChargeCOAPIType>();
						List<BayerActualsAPIType> validRecords = new ArrayList<BayerActualsAPIType>();
						List<BayerActualsAPIType> inputList = new ArrayList<BayerActualsAPIType>();
						List<BayerActualsAPIType> validRecordsDC = new ArrayList<BayerActualsAPIType>();
						List<BayerCommitmentLICOAPIType> currentListCO = new ArrayList<BayerCommitmentLICOAPIType>();					
						List<BayerCommitmentLICOAPIType> movedRecords = new ArrayList<BayerCommitmentLICOAPIType>();

						//Aug 2021 Phase 3 CR2 Variables - Disabled
						List<BayerActualsAPIType> noCORecords = new ArrayList<BayerActualsAPIType>();

						session = null;	

						//Retrieve last run failed records and process						
						if (!skipError)
						{
							logger.debug("Reading previous run failed Actuals data from ERR Log table for Project: "+ projectId);
							List<ActApiErrorDAO> lastTimeFailedDAOList = new ArrayList<ActApiErrorDAO>();
							if (lastTimeFailedDAOList.size()>0){
								logger.debug("Processing previous run failed PO Line Item data from ERR Log table for Project"+ projectId);
								errorDAOList = this.processProjects(this.getBayerActualsAPITypesFromError(lastTimeFailedDAOList), projectId, projectAPIType);
								errorDAOList = this.getErrorListWithId(lastTimeFailedDAOList, errorDAOList);

								this.processStatusMessages(errorDAOList, false);
								logger.debug("Processing previous run failed PO Line Item data completed for Project: 0"+ projectId);
							}
							else{
								logger.debug("No previous run failed PM Order data for processing for Project: "+ projectId);
							}
						}
						logger.debug("Reading Actuals data from SAP Input for Project: "+ projectId);

						if (isLive)
						{
							inputList = readSapData(sapProjectId, systemId, startDate, endDate); // get odata records
							if (inputList.size()!=0) {
								invalidRecords = this.getSapInputInvalid(inputList);  // check CostObjectExternalKey exists
								validRecords = this.getBayerActualsAPITypesValid(inputList);  // check CostObjectExternal Key exists
							}
							else
								logger.info(inputList.size()+ " rows of odata read\n");			
						}
						else
						{
							List<SapACTODataType> inputListSample = importHelper.getSapACTTypesSample();
							inputList = this.getBayerActualsAPITypesSampleRaw(inputListSample);
							invalidRecords = this.getBayerActAPITypesInvalidSample(inputListSample);
							validRecords = this.getBayerActualsAPITypesValidSample(inputListSample);						
						}

						if (inputList.size()!=0) {


							//Phase 3 CR2: Process input to filter out Actuals with Missing owner COs - Disabled
							//validRecords = this.getValidParentCoLIs(validRecords, noCORecords, currentParentCOs);

							currentListCO = this.readCommitmentLICOs(projectId);  // read BayerCommitmentLICOAPI
							costAccounts = this.getBayerCostAccountsAPITypes(inputList);
							directChargeCOs = this.getBayerDirectChargeCOAPITypes(projectCurrency, validRecords, currentListCO);
							validRecordsDC = this.getBayerActualsAPITypesDC(validRecords, currentListCO);

							//Process Invalid Records
							logger.debug("Processing Invalid Records, If Any for Project: " + projectId);
							if (invalidRecords.size()>0 && !skipError)
								this.processStatusMessages(invalidRecords, true);
							logger.debug("Processing Invalid Records Completed for Project: "+ projectId);	

							//Process  Actuals related Transaction Category - Vendors
							//logger.debug("Processing Actuals related Transaction Category Vendors for Project: " + projectId);
							//errorTrcDAOList = processTransactionCategory(trcVendors, projectId, GlobalConstants.EPC_TRANSACTION_CATEGORY_VENDOR);	
							//Process  Actuals related Cost Elements
							//logger.debug("Processing Actuals related Transaction Category Vendors for Project: " + projectId);

							//Process  Actuals related Cost Elements
							logger.debug("Processing Actuals related Cost Elements for Project: " + projectId);
							errorCaDAOList = processCostAccounts(costAccounts, projectId);	

							//Process Direct Charge Actuals as CO
							logger.debug("Creating/Updating Direct Charge Actuals as Direct Charge Cost Objects for Project: " + projectId);
							errorCoDAOList = processDirectChargeCOs(directChargeCOs, projectId);	// POST direct charge cost objects to EPC		

							//Process Cost Accounts Status Messages
							if (!skipError)
							{
								logger.debug("Processing Direct Charge CO ERR Messages, If Any for Project: " + projectId);
								//this.processStatusMessagesCO(errorCoDAOList, true);
								logger.debug("Processing Direct Charge CO ERR Message Completed for Project: "+ projectId);	

								logger.debug("Processing Related Cost Accounts ERR Messages, If Any for Project: " + projectId);
								//this.processStatusMessagesCA(errorCaDAOList, true);
								logger.debug("Processing Related Cost Accounts ERR Message Completed for Project: "+ projectId);	

								logger.debug("Processing Related Transaction Catory Vendor ERR Messages, If Any for Project: " + projectId);
								//this.processStatusMessagesTRC(errorTrcDAOList, true);
								logger.debug("Processing Related Transaction Category Vendor ERR Message Completed for Project: "+ projectId);	
							}

							//Process Valid Records
							if (validRecords.size()>0)
							{
								logger.debug("filtering out actuals without co parent objects");
								List<BayerActualsAPIType> actualsWithParents = new ArrayList<BayerActualsAPIType>();

								//actualsWithParents = getActualsWithParents(validRecordsDC, currentListCO);

								actualsWithParents = hasParentCostObject(validRecordsDC, projectId);							
								errorDAOList = processProjects(actualsWithParents, projectId, projectAPIType);
								//filterEndDate.add(dFactory.newDuration("P1D"));
								//projectAPIType.setSAPLastRunDate(filterEndDate);
								//errorPrjDAOList = this.UpdateProject(projectAPIType, projectId);

								//Process Status Messages
								if (!skipError)
								{
									//logger.debug("Processing ERR Messages, If Any for Project: " + projectId);
									//this.processStatusMessages(errorDAOList, true);
									//this.processStatusMessagesPRJ(errorPrjDAOList, true);
									//logger.debug("Processing ERR Message Completed for Project: "+ projectId);							
								}
							}

							//Aug 2021 Phase 3 CR2: Process Actual Line Items with Missing Owner CO - Disabled

							//						if (noCORecords.size()>0)
							//						{
							//							logger.debug("Processing Actuals with Missing Owner Cost Object in EcoSys for Project: " + projectId);
							//						
							//							errorMissingDAOList = this.processMissingCOLIs(noCORecords, projectId);		
							//							//validRecords = this.removeParentMissingTrans(validRecords, noParentLICOs, projectId);
							//							
							//							logger.debug("Processing Actuals with Missing Owner Cost Object in EcoSys completed for Project: " + projectId);
							//						}

						}
						if (!GlobalConstants.PROCESS_SAP_YEARCLOSE) {
							//update LastRunDate if not running year close
							logger.debug("Update LastRunDate for Project: "+ projectId);
							filterEndDate.add(dFactory.newDuration("P1D"));
							projectAPIType.setSAPLastRunDate(filterEndDate);
							errorPrjDAOList = this.UpdateProject(projectAPIType, projectId);	
							logger.debug("Update LastRunDate complete for Project: "+ projectId);
						}
						else
						{
							//update LastRunDate if not running year close
							logger.debug("Update LastRunDate for Project: "+ projectId);
							//filterEndDate.add(dFactory.newDuration("P1D"));
							projectAPIType.setSAPLastRunDate(currentDate);
							errorPrjDAOList = this.UpdateProject(projectAPIType, projectId);	
							logger.debug("Update LastRunDate complete for Project: "+ projectId);
							
							// do not update LastRunDate if running year close 
							//logger.debug("Update LastRunDate NOT updated for Project: "+ projectId);
						}
						//re-calculate WBS ID/Name for the Project
						logger.debug("Trigger Action Batch to Recalc WBS ID/Name Custom Field for Project: "+ projectId);
						this.calcWBSIds(projectId);
						logger.debug("Trigger Action Batch to Recalc WBS ID/Name Custom Field completed for Project: "+ projectId);


						
						logger.debug("SELECT THE CORRECT SKIP PROPERTY 413 THEN COMMENT THIS OUT!");

						if (GlobalConstants.PROCESS_SAP_YEARCLOSE) 
								//(currentDate.getMonth()==1 || currentDate.getMonth()==6) )
							// also need to run January
						{
							logger.debug("Running monthly close for DECEMBER for Project"+ projectId);
							String reportPeriod = this.getPeriodId(reportDate.getYear()-1, 12 /*reportDate.getMonth()*/);
							//delete existing last period actuals from EcoSys
							this.deletePeriodActuals(projectId, reportPeriod);
							logger.debug("Purging DECEMBER actuals from EcoSys completed for Project"+ projectId);

							logger.info("Running Post Processing Action Batch (Monthly Closing) from EcoSys for Project: "+ projectId);
							String projectPeriod = this.getPeriodId(currentDate.getYear(), 1 );

							this.postprocessLIs(masterProjectId, projectId, projectPeriod, isTracking);
							logger.info("Running Post Processing Action Batch (Monthly Closing) from EcoSys completed for Project: "+ projectId);
						}
						
						//dayInMonth = 1;
						else if (dayInMonth == 1 && !GlobalConstants.SKIP_SAP_PP_INTERFACE)
							// EcoSys masters not in SAP monthly run
						{
							String reportPeriod = this.getPeriodId(reportDate.getYear(), reportDate.getMonth());
							//delete existing last period actuals from EcoSys
							this.deletePeriodActuals(projectId, reportPeriod);
							logger.debug("Purging last period actuals from EcoSys completed for Project"+ projectId);

							logger.info("Running Post Processing Action Batch (Monthly Closing) from EcoSys for Project: "+ projectId);
							String projectPeriod = this.getPeriodId(currentDate.getYear(), currentDate.getMonth());

							this.postprocessLIs(masterProjectId, projectId, projectPeriod, isTracking);
							logger.info("Running Post Processing Action Batch (Monthly Closing) from EcoSys completed for Project: "+ projectId);
						}
						else if (!GlobalConstants.SKIP_POST_PROCESS)
							// grown into forecasting etc - facility to switch property to force a mid month close
							// All SAP projects only not EcoSys orphan master projects
						{
							logger.info("Running Post Processing Action Batch (Monthly Closing) from EcoSys for Project: "+ projectId);
							String projectPeriod = this.getPeriodId(currentDate.getYear(), currentDate.getMonth());

							this.postprocessLIs(masterProjectId, projectId, projectPeriod, isTracking);
							logger.info("Running Post Processing Action Batch (Monthly Closing) from EcoSys completed for Project: "+ projectId);
						}
						else
						{
							logger.info("Skipped Running Post Processing Action Batch (Monthly Closing) from EcoSys for Project: "+ projectId);
						}
						logger.debug("posting "+itgIssueLog.size()+" " +itgInterface+" issues to EPC");
						if (itgIssueLog.size()>0)
							processIssueLog(itgIssueLog);
					}
					catch(SystemException se) {
						logger.error("3005 -- Project Actuals Import Failed for Project: " + GlobalConstants.EPC_PROJECT_ID_PROCESSED, se);
						retCode=GlobalConstants.IMPORT_SAP_ACT_FAILED;
						continue;
					}
				}				
			} else {
				logger.info("Skipped Project Actuals Import Interface. Change the skip property to 'false'");				
			}

		}catch(Exception e) {			
			logger.error("3005 -- Project Actuals Import Failed", e);			
			retCode=GlobalConstants.IMPORT_SAP_ACT_FAILED;
		}

		if (retCode==GlobalConstants.IMPORT_SAP_ACT_SUCCESS)
			logger.debug("3000 -- Project Actuals Import Completed Successfully");

		return retCode;
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

	private List<BayerActualsAPIType> hasParentCostObject(List<BayerActualsAPIType> apiTypes, String projectId){
		List<BayerWBSReadAPIType> ecosysWBS = new ArrayList<>();
		List<BayerWBSReadAPIType> ecosysPMO = new ArrayList<>();
		List<BayerActualsAPIType> apiTypes2 = new ArrayList<BayerActualsAPIType>();
		try {
			logger.debug("reading EcoSys cost objects for project: " + projectId);
			ecosysWBS = this.processEPCCostObjects(projectId, "WBS");
			ecosysPMO = this.processEPCCostObjects(projectId, "PM Order");
			ecosysWBS.addAll(ecosysPMO);
			ecosysPMO.clear();
			findAndShowRecords(apiTypes); // utility to see something in the records  // debugMode = true
			logger.debug("checking " + ecosysWBS.size() + " records for parent cost object");
			for (BayerActualsAPIType apiType : apiTypes) {
				if (apiType.getCostObjectExternalKey().contains("Direct Charge")) {
					apiTypes2.add(apiType);
				}
				else {
					try {
						String hpid =ecosysWBS.stream()
								.filter(t -> t.getExternalKey().equalsIgnoreCase(apiType.getSAPWBSElement()))
								.findFirst().get().getHierarchyPathID();
						apiTypes2.add(apiType);
					}
					catch (NoSuchElementException e) {
						//logger.error(":: MISSING PARENT :: parent cost object does not exist for Actual Transaction with SAP unique ID: "+apiType.getSAPUniqueID());

						logger.error("ACTUAL TRANSACTION : MISSING PARENT : ExternalKey: "+apiType.getExternalKey()+" --> Parent CostObjectId: " + apiType.getSAPWBSElement());
						itgIssueLog.add(getIntegrationIssuesAPIType("", "Actual charge "+ apiType.getExternalKey() + " does not have a valid co parent", projectId,apiType.getExternalKey()));

						//System.out.println(projectId);
						continue;
					}
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

	private void findAndShowRecords(List<BayerActualsAPIType> dataSent) {
		List<BayerActualsAPIType> dataFiltered = new ArrayList<>();
		if (GlobalConstants.DEBUGMODE) {
			dataFiltered=dataSent.stream()
					.filter(t -> t.getSAPWBSElement()=="")
					//.forEach(System.out.println(t.getSAPWBSElement()))
					.collect(Collectors.toList());
			logger.info("Cost Objects Filtered: " + dataFiltered.size());
			for (BayerActualsAPIType act : dataSent ) {
				//System.out.println(act.getSAPWBSElement());
			}
		}		
		return;
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

/*
	private List<BayerActualsAPIType> getActualsWithParents(List<BayerActualsAPIType> validRecordsDC, List<BayerCommitmentLICOAPIType> currentListCO) {
		List<BayerActualsAPIType> validActuals = new ArrayList<>();
		for (BayerActualsAPIType act : validRecordsDC) {
			boolean isValid = false;
			for (BayerCommitmentLICOAPIType co : currentListCO) {
				if (act.getCostObjectExternalKey().equalsIgnoreCase(co.getExternalKey()) || act.getCostObjectExternalKey().contains("Direct Charge")) {
					validActuals.add(act);
					isValid = true;
				}
				if (isValid) break;
			}
			if (!isValid)
				logger.error("actual charge "+act.getExternalKey()+" does not have a valid co parent");
			itgIssueLog.add(getIntegrationIssuesAPIType("", "actual charge "+act.getExternalKey() + " does not have a valid co parent", act.getExternalKey(), act.getExternalKey()));

		}
		logger.debug(validRecordsDC.size() + " records processed");
		logger.debug(validActuals.size() + " records valid");
		return validActuals;
	}

	private List<BayerActualsAPIType> readProjectData() {
		return null;
	}
*/
	//Start Section Process Actuals
	private List<ActApiErrorDAO> processProjects( List<BayerActualsAPIType> prjRecords, 
			String projectId, BayerProjectAPIType apiType) throws SystemException{

		logger.debug("Importing Project Actuals to EPC...");

		List<ActApiErrorDAO> retStatusMsgList = new ArrayList<ActApiErrorDAO>();
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
			//for(int i = 0; i < prjRecords.size(); i += 1) {
			for(int i = 0; i < prjRecords.size(); i += GlobalConstants.EPC_REST_BATCHSIZE) {
				int end = (prjRecords.size() < (i + GlobalConstants.EPC_REST_BATCHSIZE) ? prjRecords.size() : GlobalConstants.EPC_REST_BATCHSIZE + i);
				List<ActApiErrorDAO> statusMsgList = new ArrayList<ActApiErrorDAO>();
				timerBatch.start();
				session = this.request(prjRecords.subList(i, end), session, baseUri,
						projectId, statusMsgList, apiType);
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

	private Cookie request(List<BayerActualsAPIType> subList,
			Cookie session, String baseUri,  String projectId, List<ActApiErrorDAO> errorList, BayerProjectAPIType apiPrjType) 
					throws SystemException, DatatypeConfigurationException{
		// API BayerActualsAPI
		BayerActualsAPIRequestType request = new BayerActualsAPIRequestType();
		request.getBayerActualsAPI().addAll(subList);

		ObjectFactory objectFactory = new ObjectFactory();
		JAXBElement<BayerActualsAPIRequestType> requestWrapper = objectFactory.createBayerActualsAPIRequest(request);
		//HashMap<String, String> prjMap = new HashMap<String, String>();
		//prjMap.put(GlobalConstants.EPC_PROJECT_PARAM, projectId);
		ClientResponse response = epcRestMgr
				.postApplicationXmlAsApplicationXml(client, requestWrapper,
						baseUri, GlobalConstants.EPC_REST_IMPORT_SAP_ACT,
						session);

		logger.debug(response);
		BayerActualsAPIResultType result = epcRestMgr.responseToObject(response, BayerActualsAPIResultType.class);
		if(session == null)
			session = epcRestMgr.getSessionCookie(response);

		if(!result.isSuccessFlag()){
			String errMsg="The interface failed to load any record due to data issues; please verify data.";
			if (result.getError() != null)
				errMsg=result.getError().toString();
			for(BayerActualsAPIType act: subList)
			{
				//convert to APIErrorDAO type
				ActApiErrorDAO statusMsg = this.getActAPIErrorDAO(act);
				statusMsg.setRootCostObjectID(projectId);
				statusMsg.setStatus(GlobalConstants.STAGING_RECORD_STATUS_FAILED);
				if (errMsg.length() > GlobalConstants.errorMsgSize)
					errMsg = errMsg.substring(0, GlobalConstants.errorMsgSize-1);
				statusMsg.setErrorMsg(errMsg);
				errorList.add(statusMsg);
			}
			throw new SystemException(errMsg);
		} else {

			//List<StatusMessage> statusMsgList = new ArrayList<StatusMessage>();
			int i=0;
			int c=0;
			DatatypeFactory dFactory = DatatypeFactory.newInstance();

			//Update ProjectLastTimeRunDate
			//apiPrjType.setSAPActualImportLastRunDate(dFactory.newXMLGregorianCalendar(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
			//i = prjExpMgr.updateProjectLastTimeRunDate(apiPrjType);

			for(ObjectResultType or : result.getObjectResult()) {
				BayerActualsAPIType apiType = subList.get(i++);
				String apiID = apiType.getCommitmentID() + "-" + apiType.getSAPPurchasingDocumentLineItemNumber();

				//convert to APIErrorDAO type
				ActApiErrorDAO statusMsg = this.getActAPIErrorDAO(apiType);
				statusMsg.setRootCostObjectID(projectId);

				if(or.isSuccessFlag()) {
					logger.debug("UPDATE RESULT --> " + or.getInternalId());
					logger.debug("Record with External Key ID of : "  + "|ExternalId: " + or.getExternalId() + "|PO Line Item: " + apiID + "|" 
							+ "CostObjectExternalKey: " + apiType.getCostObjectExternalKey() + "|"
							+ GlobalConstants.STAGING_RECORD_STATUS_LOADED);
					/*
					logger.debug("Updated record --> " + or.getInternalId() + "|ExternalId: " + or.getExternalId() + "|PO Line Item: " + apiID + "|" 
							 + "ActualReferenceDoc: " + apiType.getActualReferenceDoc() + "|"
							 + "ActualReferenceHeaderText: " + apiType.getActualReferenceHeaderText() + "|"
							 + "CommitmentID: " + apiType.getCommitmentID() + "|"
							 + "ControllingArea: " + apiType.getControllingArea() + "|"
							 + "CostAccountID: " + apiType.getCostAccountID() + "|"
							 + "CostAccountName: " + apiType.getCostAccountName() + "|"
							 + "CostObjectExternalKey: " + apiType.getCostObjectExternalKey() + "|"
							 + "CostObjectID: " + apiType.getCostObjectID() + "|"
							 + "CurrencyCostObjectCode: " + apiType.getCurrencyCostObjectCode() + "|"
							 + "CurrencyTransactionCode: " + apiType.getCurrencyTransactionCode() + "|"
							 + "ExternalKey: " + apiType.getExternalKey() + "|"
							 + "FIDocumentNumber: " + apiType.getFIDocumentNumber() + "|"
							 + "OffsettingAccountNumber: " + apiType.getOffsettingAccountNumber() + "|"
							 + "SAPAccrualID: " + apiType.getSAPAccrualID() + "|"
							 + "SAPPurcharsingOrderSeqNumber: " + apiType.getSAPPurcharsingOrderSeqNumber() + "|"
							 + "SAPPurchasingDocumentLineItemNumber: " + apiType.getSAPPurchasingDocumentLineItemNumber() + "|"
							 + "SAPUniqueID: " + apiType.getSAPUniqueID() + "|"
							 + "SAPWBSElement: " + apiType.getSAPWBSElement() + "|"
					         + "TransactionExchangeRateSource: " + apiType.getTransactionExchangeRateSource() + "|" 
							 + "VendorID: " + apiType.getVendorID() + "|"
							 + "VendorName: " + apiType.getVendorName() + "|"
							 + "VersionID: " + apiType.getVersionID() + "|"  
							 + "AlternateCostTransactionCurrency: " + apiType.getAlternateCostTransactionCurrency() + "|"
							 + "ConversionRateCostObjectCurrency: " + apiType.getConversionRateCostObjectCurrency() + "|"
							 + "CostCostObjectCurrency: " + apiType.getCostCostObjectCurrency()+  "|"
							 + "CostTransactionCurrency: " + apiType.getCostTransactionCurrency() + "|"
							 + "PostingRow: " + apiType.getPostingrow() + "|"
							 + "SAPExchangeRate: " + apiType.getSAPExchangeRate()+ "|"
							 + "Transaction Date: " + apiType.getTransactionDate() );
					 */
					statusMsg.setStatus(GlobalConstants.STAGING_RECORD_STATUS_LOADED);

				} else {
					c++;
					String str = ((ElementNSImpl)or.getResultMessage().get(0).getMessage()).getTextContent();
					if (str.equals("Cost Object does not have a valid value. This is a required field. Please select a correct value, or speak with the administrator who configured this Spreadsheet.")) {
						logger.error("ACTUAL TRANSACTION : MISSING PARENT : InternalId: " + or.getInternalId() + " | ExternalId: " + or.getExternalId() + " | PO Line Item: " + apiID + "|"
								+ "ActualReferenceDoc: " + apiType.getActualReferenceDoc() + "|"
								+ "ActualReferenceHeaderText: " + apiType.getActualReferenceHeaderText() + "|"
								+ "CommitmentID: " + apiType.getCommitmentID() + "|"
								+ "ControllingArea: " + apiType.getControllingArea() + "|"
								+ "CostAccountID: " + apiType.getCostAccountID() + "|"
								+ "CostAccountName: " + apiType.getCostAccountName() + "|"
								+ "CostObjectExternalKey: " + apiType.getCostObjectExternalKey() + "|"
								+ "CostObjectID: " + apiType.getCostObjectID() + "|"
								+ "CurrencyCostObjectCode: " + apiType.getCurrencyCostObjectCode() + "|"
								+ "CurrencyTransactionCode: " + apiType.getCurrencyTransactionCode() + "|"
								+ "ExternalKey: " + apiType.getExternalKey() + "|"
								+ "FIDocumentNumber: " + apiType.getFIDocumentNumber() + "|"
								+ "OffsettingAccountNumber: " + apiType.getOffsettingAccountNumber() + "|"
								+ "SAPAccrualID: " + apiType.getSAPAccrualID() + "|"
								+ "SAPPurcharsingOrderSeqNumber: " + apiType.getSAPPurcharsingOrderSeqNumber() + "|"
								+ "SAPPurchasingDocumentLineItemNumber: " + apiType.getSAPPurchasingDocumentLineItemNumber() + "|"
								+ "SAPUniqueID: " + apiType.getSAPUniqueID() + "|"
								+ "SAPWBSElement: " + apiType.getSAPWBSElement() + "|"
								+ "TransactionExchangeRateSource: " + apiType.getTransactionExchangeRateSource() + "|"
								+ "VendorID: " + apiType.getVendorID() + "|"
								+ "VendorName: " + apiType.getVendorName() + "|"
								+ "VersionID: " + apiType.getVersionID() + "|"
								+ "AlternateCostTransactionCurrency: " + apiType.getAlternateCostTransactionCurrency() + "|"
								+ "ConversionRateCostObjectCurrency: " + apiType.getConversionRateCostObjectCurrency() + "|"
								+ "CostCostObjectCurrency: " + apiType.getCostCostObjectCurrency()+  "|"
								+ "CostTransactionCurrency: " + apiType.getCostTransactionCurrency() + "|"
								+ "PostingRow: " + apiType.getPostingrow() + "|"
								+ "SAPExchangeRate: " + apiType.getSAPExchangeRate()+ "|"
								+ "Transaction Date: " + apiType.getTransactionDate() + "|" + or.isSuccessFlag() + "|" + str + "");

						itgIssueLog.add(getIntegrationIssuesAPIType("",
								"Actual charge "+ apiType.getExternalKey() + " does not have a valid co parent", projectId, apiType.getExternalKey()));
					}
					else {
						logger.error("ERROR --> InternalId: " + or.getInternalId() + " | ExternalId: " + or.getExternalId() + " | PO Line Item: " + apiID + "|" 
								+ "ActualReferenceDoc: " + apiType.getActualReferenceDoc() + "|"
								+ "ActualReferenceHeaderText: " + apiType.getActualReferenceHeaderText() + "|"
								+ "CommitmentID: " + apiType.getCommitmentID() + "|"
								+ "ControllingArea: " + apiType.getControllingArea() + "|"
								+ "CostAccountID: " + apiType.getCostAccountID() + "|"
								+ "CostAccountName: " + apiType.getCostAccountName() + "|"
								+ "CostObjectExternalKey: " + apiType.getCostObjectExternalKey() + "|"
								+ "CostObjectID: " + apiType.getCostObjectID() + "|"
								+ "CurrencyCostObjectCode: " + apiType.getCurrencyCostObjectCode() + "|"
								+ "CurrencyTransactionCode: " + apiType.getCurrencyTransactionCode() + "|"
								+ "ExternalKey: " + apiType.getExternalKey() + "|"
								+ "FIDocumentNumber: " + apiType.getFIDocumentNumber() + "|"
								+ "OffsettingAccountNumber: " + apiType.getOffsettingAccountNumber() + "|"
								+ "SAPAccrualID: " + apiType.getSAPAccrualID() + "|"
								+ "SAPPurcharsingOrderSeqNumber: " + apiType.getSAPPurcharsingOrderSeqNumber() + "|"
								+ "SAPPurchasingDocumentLineItemNumber: " + apiType.getSAPPurchasingDocumentLineItemNumber() + "|"
								+ "SAPUniqueID: " + apiType.getSAPUniqueID() + "|"
								+ "SAPWBSElement: " + apiType.getSAPWBSElement() + "|"
								+ "TransactionExchangeRateSource: " + apiType.getTransactionExchangeRateSource() + "|" 
								+ "VendorID: " + apiType.getVendorID() + "|"
								+ "VendorName: " + apiType.getVendorName() + "|"
								+ "VersionID: " + apiType.getVersionID() + "|"  
								+ "AlternateCostTransactionCurrency: " + apiType.getAlternateCostTransactionCurrency() + "|"
								+ "ConversionRateCostObjectCurrency: " + apiType.getConversionRateCostObjectCurrency() + "|"
								+ "CostCostObjectCurrency: " + apiType.getCostCostObjectCurrency()+  "|"
								+ "CostTransactionCurrency: " + apiType.getCostTransactionCurrency() + "|"
								+ "PostingRow: " + apiType.getPostingrow() + "|"
								+ "SAPExchangeRate: " + apiType.getSAPExchangeRate()+ "|"
								+ "Transaction Date: " + apiType.getTransactionDate() + "|" + or.isSuccessFlag() + "|" + str + "\n");	

						//itgIssueLog.add(getIntegrationIssuesAPIType("", "Actual charge "+ apiType.getExternalKey() + " does not have a valid co parent", projectId, apiType.getExternalKey()));
					}

					statusMsg.setStatus(GlobalConstants.STAGING_RECORD_STATUS_FAILED);
					statusMsg.setErrorMsg(str);	
				}
				errorList.add(statusMsg);			
			}
			//System.out.println(c);
		}
		return session;
	}

	//End Section Process Actuals


	//TODO Need to decide if adding Cost Account Error table


	private void processStatusMessages(List<ActApiErrorDAO> errorList, boolean isNew)  throws SystemException {
		try{
			//stgDBMgr.insertWbsBatch(this.wbsAPIErrorDAOList);
			if (isNew)
				stgDBMgr.insertActBatch(errorList);
			else
				stgDBMgr.updateActBatch(errorList);

		} catch(Exception e) {
			throw new SystemException (e);
		}
	}



	private PolcoApiErrorDAO getDirectChargeCOAPIErrorDAO(BayerDirectChargeCOAPIType apiType)
	{
		PolcoApiErrorDAO apiError = new PolcoApiErrorDAO();
		apiError.setParentCostObjectExternalKey(apiType.getParentCostObjectExternalKey());
		//apiError.setCostObjectHierarchyPathID(apiType.getCostObjectHierarchyPathID());
		apiError.setCostObjectID(apiType.getCostObjectID());
		apiError.setExternalKey(apiType.getExternalKey());
		apiError.setCostObjectTypeName(apiType.getCostObjectTypeName());
		return apiError;
	}

	private ActApiErrorDAO getActAPIErrorDAO(BayerActualsAPIType apiType)
	{
		ActApiErrorDAO apiError = new ActApiErrorDAO();
		apiError.setCommitmentID(apiType.getCommitmentID());
		apiError.setControllingArea(apiType.getControllingArea());
		apiError.setConversionRateCostObjectCurrency(apiType.getConversionRateCostObjectCurrency());
		apiError.setCostAccountID(apiType.getCostAccountID());
		apiError.setCostAccountName(apiType.getCostAccountName());
		apiError.setCostCostObjectCurrency(apiType.getCostCostObjectCurrency());
		apiError.setCostObjectExternalKey(apiType.getCostObjectExternalKey());
		//apiError.setCostObjectHierarchyPathID(apiType.getCostObjectHierarchyPathID());
		apiError.setCostObjectID(apiType.getCostObjectID());
		apiError.setCostTransactionCurrency(apiType.getCostTransactionCurrency());
		apiError.setAlternateCostTransactionCurrency(apiType.getAlternateCostTransactionCurrency());
		apiError.setCurrencyCostObjectCode(apiType.getCurrencyCostObjectCode());
		apiError.setCurrencyTransactionCode(apiType.getCurrencyTransactionCode());
		apiError.setExternalKey(apiType.getExternalKey());
		apiError.setFIDocumentNumber(apiType.getFIDocumentNumber());
		apiError.setSAPWBSElement(apiType.getSAPWBSElement());
		apiError.setVendorID(apiType.getVendorID());
		apiError.setVendorName(apiType.getVendorName());
		apiError.setTransactionDate(apiType.getTransactionDate());
		apiError.setVersionID(apiType.getVersionID());
		apiError.setActualReferenceDoc(apiType.getActualReferenceDoc());
		apiError.setActualReferenceHeaderText(apiType.getActualReferenceHeaderText());
		apiError.setSAPPurchasingDocumentLineItemNumber(apiType.getSAPPurchasingDocumentLineItemNumber());
		apiError.setSAPUniqueID(apiType.getSAPUniqueID());
		apiError.setOffsettingAccountNumber(apiType.getOffsettingAccountNumber());
		apiError.setSAPPurcharsingOrderSeqNumber(apiType.getSAPPurcharsingOrderSeqNumber());
		apiError.setAlternateCostTransactionCurrency(apiType.getAlternateCostTransactionCurrency());
		apiError.setPostingrow(apiType.getPostingrow());
		apiError.setSAPExchangeRate(apiType.getSAPExchangeRate());
		apiError.setSAPAccrualID(apiType.getSAPAccrualID());

		return apiError;
	}

	private PolcoApiErrorDAO getPolcoAPIErrorDAO(BayerDirectChargeCOAPIType apiType)
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
	private List<ActApiErrorDAO> getErrorListWithId(List<ActApiErrorDAO> oldErrorList, List<ActApiErrorDAO> newErrorList){
		for (int i = 0; i < newErrorList.size(); i++)
		{
			long j = this.getErrorId(newErrorList.get(i).getCostObjectExternalKey(), 
					newErrorList.get(i).getFIDocumentNumber(),
					oldErrorList);
			newErrorList.get(i).setId(j);;
		}
		return newErrorList;
	}

	//Get ErrorID for reprocessed Error
	private long getErrorId(String pathId, String commitId, List<ActApiErrorDAO> errorList){
		long id = 0;
		for (int i = 0; i < errorList.size(); i++)
		{

			if(errorList.get(i).getCostObjectExternalKey().equals(pathId)
					&&errorList.get(i).getFIDocumentNumber().equals(commitId))
				id = errorList.get(i).getId();
		}
		return id;
	}

	//Convert PMOrderAPIErrorDAO list to BayerPMOrderAPIType List
	private List<BayerActualsAPIType> getBayerActualsAPITypesFromError(List<ActApiErrorDAO> errorList){

		BayerActualsAPIRequestType request = new BayerActualsAPIRequestType();
		for (int i = 0; i < errorList.size(); i++)
		{
			ActApiErrorDAO errorDAO = errorList.get(i);
			BayerActualsAPIType apiType = this.getBayerActualsAPITypeFromError(errorDAO);
			request.getBayerActualsAPI().add(apiType);
		}
		return request.getBayerActualsAPI();
	}

	private BayerActualsAPIType getBayerActualsAPITypeFromError(ActApiErrorDAO errorDAO)
	{
		BayerActualsAPIType apiType = new BayerActualsAPIType();
		apiType.setCommitmentID(errorDAO.getCommitmentID());
		apiType.setControllingArea(errorDAO.getControllingArea());
		apiType.setConversionRateCostObjectCurrency(errorDAO.getConversionRateCostObjectCurrency());
		apiType.setCostAccountID(errorDAO.getCostAccountID());
		apiType.setCostAccountName(errorDAO.getCostAccountName());
		apiType.setCostCostObjectCurrency(errorDAO.getCostCostObjectCurrency());
		apiType.setCostObjectExternalKey(errorDAO.getCostObjectExternalKey());
		//apiType.setCostObjectHierarchyPathID(errorDAO.getCostObjectHierarchyPathID());
		apiType.setCostObjectID(errorDAO.getCostObjectID());
		apiType.setCostTransactionCurrency(errorDAO.getCostTransactionCurrency());
		apiType.setCurrencyCostObjectCode(errorDAO.getCurrencyCostObjectCode());
		apiType.setCurrencyTransactionCode(errorDAO.getCurrencyTransactionCode());
		apiType.setExternalKey(errorDAO.getExternalKey());
		apiType.setFIDocumentNumber(errorDAO.getFIDocumentNumber());
		apiType.setSAPWBSElement(errorDAO.getSAPWBSElement());
		apiType.setVendorID(errorDAO.getVendorID());
		apiType.setVendorName(errorDAO.getVendorName());
		apiType.setTransactionDate(errorDAO.getTransactionDate());
		apiType.setVersionID(errorDAO.getVersionID());
		apiType.setActualReferenceDoc(errorDAO.getActualReferenceDoc());
		apiType.setActualReferenceHeaderText(errorDAO.getActualReferenceHeaderText());
		apiType.setSAPPurchasingDocumentLineItemNumber(errorDAO.getSAPPurchasingDocumentLineItemNumber());
		apiType.setAlternateCostTransactionCurrency(errorDAO.getAlternateCostTransactionCurrency());
		apiType.setOffsettingAccountNumber(errorDAO.getOffsettingAccountNumber());
		apiType.setPostingrow(errorDAO.getPostingrow());
		apiType.setSAPExchangeRate(errorDAO.getSAPExchangeRate());
		apiType.setSAPAccrualID(errorDAO.getSAPAccrualID());
		apiType.setSAPPurcharsingOrderSeqNumber(errorDAO.getSAPPurcharsingOrderSeqNumber());
		apiType.setSAPUniqueID(errorDAO.getSAPUniqueID());
		return apiType;
	}


	private boolean isValidAPIType (BayerActualsAPIType apiType)
	{
		boolean isValid = true;
		//String projectId = apiType.getProjectDefinition();
		String pmoId = apiType.getCostObjectExternalKey();
		String wbsElement = apiType.getSAPWBSElement();
		if ((pmoId == null || pmoId.equals("")))
			isValid = false;
		return isValid;
	}

	public List<BayerActualsAPIType> getBayerActualsAPITypesValid (List<BayerActualsAPIType> apiTypes, List<BayerCommitmentLICOV2APIType> commitmentLICOs, String sapProjectId) {	
		BayerActualsAPIRequestType request = new BayerActualsAPIRequestType();	    	
		// Check if POL has valid parent cost object in EcoSys
		Set<String> extKeys = null;
		Map<String, String> parentMap = commitmentLICOs.stream()
				.collect(Collectors.toMap(e -> e.getExternalKey(), p -> p.getParentCostObjectExternalKey()));							
		try {
			List<BayerWBSReadAPIType> wbsPmoType = readCOByType(sapProjectId, "PM Order");		
			List<BayerWBSReadAPIType> wbsWbsType = readCOByType(sapProjectId, "WBS");

			wbsPmoType.addAll(wbsWbsType);
			extKeys = wbsPmoType.stream()
					.map(ext -> ext.getExternalKey())
					.collect(Collectors.toSet());

			logger.debug("EcoSys returned " +extKeys.size() + " unique External Keys");
			logger.debug("LICO mapped " +parentMap.size() + " mapped External Keys");
		} catch (SystemException e) {
			System.out.println("System exception occurred: " + e + " integration continuing");
		}
		// cycle apiTypes to check validity
		for (int i = 0; i < apiTypes.size(); i++)
		{
			BayerActualsAPIType apiType = apiTypes.get(i);
			// retrieve parent from map
			String par = parentMap.get(apiType.getExternalKey());
			// check for parent in EcoSys external keys
			if (!extKeys.contains(par)) {
				logger.debug("Parent not found");
				continue;
			}
			if (isValidAPIType(apiType))
			{
				request.getBayerActualsAPI().add(apiType);
			}
		}
		return request.getBayerActualsAPI();
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
	public List<BayerActualsAPIType> getBayerActualsAPITypesValid(List<BayerActualsAPIType> apiTypes){

		BayerActualsAPIRequestType request = new BayerActualsAPIRequestType();
		for (int i = 0; i < apiTypes.size(); i++)
		{
			BayerActualsAPIType apiType = apiTypes.get(i);
			if (isValidAPIType(apiType))
			{
				request.getBayerActualsAPI().add(apiType);
			}
		}
		logger.debug(request.getBayerActualsAPI() + " valid records");		
		return request.getBayerActualsAPI();
	}

	//Start Section Processing Direct Charge COs

	private List<PolcoApiErrorDAO> processDirectChargeCOs( List<BayerDirectChargeCOAPIType> prjRecords,
			String projectId) throws SystemException{

		logger.debug("Importing Direct Charge Actuals to EPC as Cost Objects...");

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
		logger.debug("Creating/Updating of Direct Charge Actuals as Direct Charge Cost Objects for Project Completed: " + projectId);
		logger.debug("Complete!");
		return retStatusMsgList;
	}	


	private Cookie requestCO(List<BayerDirectChargeCOAPIType> subList, Cookie session,
			String baseUri, String projectId, List<PolcoApiErrorDAO> errorList) throws SystemException {

		// API BayerDirectChargeCOAPI
		BayerDirectChargeCOAPIRequestType request = new BayerDirectChargeCOAPIRequestType();
		request.getBayerDirectChargeCOAPI().addAll(subList);

		DCObjectFactory objectFactory = new DCObjectFactory();
		JAXBElement<BayerDirectChargeCOAPIRequestType> requestWrapper = objectFactory.createBayerDirectChargeCOAPIRequest(request);
		HashMap<String, String> prjMap = new HashMap<String, String>();
		prjMap.put(GlobalConstants.EPC_PROJECT_PARAM, projectId);
		ClientResponse response = epcRestMgr
				.postApplicationXmlAsApplicationXml(client, requestWrapper,
						baseUri, GlobalConstants.EPC_REST_IMPORT_SAP_DC_CO,
						session, prjMap);

		logger.debug(response);
		BayerDirectChargeCOAPIResultType result = epcRestMgr.responseToObject(response, BayerDirectChargeCOAPIResultType.class);
		if(session == null)
			session = epcRestMgr.getSessionCookie(response);

		if(!result.isSuccessFlag()){
			String errMsg="The interface failed to load any record due to data issues; please verify data.";
			if (result.getError() != null)
				errMsg=result.getError().toString();
			for(BayerDirectChargeCOAPIType pol: subList)
			{
				//convert to APIErrorDAO type
				PolcoApiErrorDAO statusMsg = this.getDirectChargeCOAPIErrorDAO(pol);
				statusMsg.setRootCostObjectID(projectId);
				statusMsg.setStatus(GlobalConstants.STAGING_RECORD_STATUS_FAILED);
				statusMsg.setErrorMsg(errMsg);
				errorList.add(statusMsg);
			}
			throw new SystemException(errMsg);
		} else {

			//List<StatusMessage> statusMsgList = new ArrayList<StatusMessage>();
			int i=0;
			for(DCObjectResultType or : result.getObjectResult()) {
				BayerDirectChargeCOAPIType dcCO = subList.get(i++);
				String dcID = dcCO.getCostObjectID();

				//convert to APIErrorDAO type
				PolcoApiErrorDAO statusMsg = this.getPolcoAPIErrorDAO(dcCO);
				statusMsg.setRootCostObjectID(projectId);

				if(or.isSuccessFlag()) {
					logger.debug("UPDATE RESULT --> " + or.getInternalId());
					logger.debug("Record with External Key ID of : " + dcID +" "+ GlobalConstants.STAGING_RECORD_STATUS_LOADED);
					/*
					logger.debug("Record updated with External Key ID of : " + dcID + "|"
							 + "CostObjectID: " + dcCO.getCostObjectID() + "|"
							 + "CostObjectName: " + dcCO.getCostObjectName()+ "|"
							 + "CostObjectStatus: " + dcCO.getCostObjectStatus() + "|"
							 + "CostObjectTypeName: " + dcCO.getCostObjectTypeName() + "|"
						     + "ExternalKey: " + dcCO.getExternalKey()+ "|"
							 + "ParentCostObjectExternalKey: " + dcCO.getParentCostObjectExternalKey() + "|"					
							+"|"+ GlobalConstants.STAGING_RECORD_STATUS_LOADED);
					 */


					statusMsg.setStatus(GlobalConstants.STAGING_RECORD_STATUS_LOADED);

				} else {
					String str = ((ElementNSImpl)or.getResultMessage().get(0).getMessage()).getTextContent();
					if (str.equals("Cannot find parent by path name.")) {
						logger.error("ACTUAL COST OBJECT : MISSING PARENT :: "
								+ " internal id " + or.getInternalId() + " | " + dcID + " | "
								+ "CostObjectID: " + dcCO.getCostObjectID() + " | "
								+ "CostObjectName: " + dcCO.getCostObjectName()+ " | "
								+ "CostObjectStatus: " + dcCO.getCostObjectStatus() + " | "
								+ "CostObjectTypeName: " + dcCO.getCostObjectTypeName() + " | "
								+ "ExternalKey: " + dcCO.getExternalKey()+ " | "
								+ "ParentCostObjectExternalKey: " + dcCO.getParentCostObjectExternalKey() + " | "
								+ "CostObjectCurrencyCode: " + dcCO.getCostObjectCurrencyCode() + " | "
								+ " |" + or.isSuccessFlag() + " | " + str );	
						
						itgIssueLog.add(getIntegrationIssuesAPIType("",
								"Actual charge "+ dcCO.getExternalKey() + " does not have a valid co parent", projectId, dcCO.getExternalKey()));
					}
					else
						logger.error("ERROR --> internal id " + or.getInternalId() + " | " + dcID + " | "
								+ "CostObjectID: " + dcCO.getCostObjectID() + " | "
								+ "CostObjectName: " + dcCO.getCostObjectName()+ " | "
								+ "CostObjectStatus: " + dcCO.getCostObjectStatus() + " | "
								+ "CostObjectTypeName: " + dcCO.getCostObjectTypeName() + " | "
								+ "ExternalKey: " + dcCO.getExternalKey()+ " | "
								+ "ParentCostObjectExternalKey: " + dcCO.getParentCostObjectExternalKey() + " | "
								+ "CostObjectCurrencyCode: " + dcCO.getCostObjectCurrencyCode() + " | "
								+ " |" + or.isSuccessFlag() + " | " + str + "\n");	

					statusMsg.setStatus(GlobalConstants.STAGING_RECORD_STATUS_FAILED);
					statusMsg.setErrorMsg(str);				
				}
				errorList.add(statusMsg);				
			}
		}
		return session;
	}

	private boolean isValidCODataTypeSample (SapACTODataType oDataType)
	{
		boolean isValid = true;
		BayerActualsAPIType apiType = new BayerActualsAPIType();
		String projectId = oDataType.getProjectDefinition();
		String pmoId = oDataType.getPmOrderid();
		String wbsElement = oDataType.getWbsElement();
		String pol = oDataType.getPurchOrdItem();

		//For Direct Charge, Actuals should not have PO/POL assigned.
		if (pol!=null && !pol.equals(""))
			isValid = false;

		if ((pmoId == null || pmoId.equals(""))&&
				(wbsElement == null||wbsElement.equals("")))
			isValid = false;
		return isValid;
	}

	private boolean isValidCODataType (BayerActualsAPIType actAPIType)
	{
		boolean isValid = true;
		String wbsId = actAPIType.getCostObjectExternalKey();
		String wbsElement = actAPIType.getSAPWBSElement();
		String poh = actAPIType.getCommitmentID();
		String pol = actAPIType.getSAPPurchasingDocumentLineItemNumber();
		if (wbsId.equals(wbsElement + "-" + poh + "-"+pol))
		{
			isValid=false;
			//if (wbsId.equalsIgnoreCase("362750109991"))
			//  logger.debug("Not Direct Charge Rows for 36275010991: ID is" + wbsId + " Amount is:" + actAPIType.getCostTransactionCurrency());

		}
		/*
		if (wbsId.equalsIgnoreCase("362750109991") && isValid==true)
		{
	           logger.debug("Special Direct Charge Rows for 36275010991: ID is" + wbsId + " Amount is:" + actAPIType.getCostTransactionCurrency());
		}*/

		/*For Direct Charge, Actuals should not have PO/POL assigned.
		if (pol!=null && !pol.equals(""))
			isValid = false;

		if ((wbsId == null || wbsId.equals(""))&&
				(wbsElement == null||wbsElement.equals("")))
			isValid = false;
		 */
		return isValid;
	}


	public List<BayerDirectChargeCOAPIType> getBayerDirectChargeCOAPITypes(String projectCurrency, List<BayerActualsAPIType> actAPITypes, List<BayerCommitmentLICOAPIType> currentList){

		BayerDirectChargeCOAPIRequestType request = new BayerDirectChargeCOAPIRequestType();
		Map<String, BayerDirectChargeCOAPIType> coMap = new HashMap<String, BayerDirectChargeCOAPIType>(); 
		for (int i = 0; i < actAPITypes.size(); i++)
		{
			BayerActualsAPIType actApiType = actAPITypes.get(i);
			BayerDirectChargeCOAPIType apiType = new BayerDirectChargeCOAPIType();

			// if (actApiType.getExternalKey().equalsIgnoreCase("H230-118691516-001"))
			//	System.out.println("condition met");

			if (isValidCODataType(actApiType)) // if it does not have a valid po header and po line then it is a direct charge
			{
				apiType = this.getDirectChargeCOAPIType(actApiType);

				// Test method in Actual -------------------------------
				apiType.setCostObjectCurrencyCode(projectCurrency);
				// ------------------------------------------------

				if (!coMap.containsKey(apiType.getExternalKey()))
				{
					request.getBayerDirectChargeCOAPI().add(apiType);
					coMap.put(apiType.getExternalKey(), apiType);
				}
			}
			else
			{ 	if (isMissingLICO(actApiType, currentList)) // check cost object external exists in lico list
			{
				apiType = this.getDirectChargeCOAPITypeFromMissing(actApiType);  // create direct charge CO
				apiType.setCostObjectCurrencyCode(projectCurrency);

				if (!coMap.containsKey(apiType.getExternalKey()))
				{
					request.getBayerDirectChargeCOAPI().add(apiType);
					coMap.put(apiType.getExternalKey(), apiType);
				}
			}
			}
		}
		logger.info(request.getBayerDirectChargeCOAPI().size() + " direct charge actuals identified\n");
		return request.getBayerDirectChargeCOAPI();
	}

	public List<BayerActualsAPIType> getBayerActualsAPITypesDC(List<BayerActualsAPIType> actAPITypes, List<BayerCommitmentLICOAPIType> currentList){

		BayerActualsAPIRequestType request = new BayerActualsAPIRequestType();
		for (int i = 0; i < actAPITypes.size(); i++)
		{
			BayerActualsAPIType actApiType = actAPITypes.get(i);
			if (isValidCODataType(actApiType))
			{
				/*if (actApiType.getCostObjectExternalKey().equalsIgnoreCase("362750109991"))
        		{
        			actApiType.setCostObjectExternalKey("362750109991-Direct Charge");
        		}*/
				actApiType.setCostObjectExternalKey(actApiType.getCostObjectExternalKey() 
						+"-" + GlobalConstants.EPC_API_WBS_DIRECT_CHARGE_ID);
			}
			else
			{
				if (isMissingLICO(actApiType, currentList))
				{
					actApiType.setCostObjectExternalKey(actApiType.getSAPWBSElement() + "-" + GlobalConstants.EPC_API_WBS_DIRECT_CHARGE_ID);
				}
			}
			//if (actApiType.getCostObjectExternalKey().equalsIgnoreCase("362750109991"))
			//{
			//  logger.debug("Actuals for 36275010991 with PO Line Item: " + actApiType.getCommitmentID() + actApiType.getSAPPurchasingDocumentLineItemNumber() + " And amount is: " + actApiType.getCostTransactionCurrency());
			//}
			request.getBayerActualsAPI().add(actApiType);

		}
		logger.info(request.getBayerActualsAPI().size() + " actuals validated\n");		
		return request.getBayerActualsAPI();
	}

	public BayerDirectChargeCOAPIType getDirectChargeCOAPITypeFromMissing(BayerActualsAPIType actApiType)
	{
		BayerDirectChargeCOAPIType apiType = new BayerDirectChargeCOAPIType();
		//String projectId = oDataType.getProjectDefinition();
		//String Id = oDataType.getPurchReq();
		String externalId = new String();
		if (actApiType.getCostObjectExternalKey()!=null && !actApiType.getCostObjectExternalKey().equals(""))
		{
			externalId = actApiType.getSAPWBSElement() + "-" + GlobalConstants.EPC_API_WBS_DIRECT_CHARGE_ID;
			apiType.setParentCostObjectExternalKey(actApiType.getSAPWBSElement());
		} 
		apiType.setCostObjectID(GlobalConstants.EPC_API_WBS_DIRECT_CHARGE_ID);
		apiType.setCostObjectName(GlobalConstants.EPC_API_WBS_DIRECT_CHARGE_NAME);
		apiType.setExternalKey(externalId);
		apiType.setCostObjectTypeName(GlobalConstants.EPC_REST_COSTOBJECTTYPE_DC);

		return apiType;
	}

	public BayerDirectChargeCOAPIType getDirectChargeCOAPIType(BayerActualsAPIType actApiType)
	{
		BayerDirectChargeCOAPIType apiType = new BayerDirectChargeCOAPIType();
		//String projectId = oDataType.getProjectDefinition();
		//String Id = oDataType.getPurchReq();
		String externalId = new String();
		if (actApiType.getCostObjectExternalKey()!=null && !actApiType.getCostObjectExternalKey().equals(""))
		{
			externalId = actApiType.getCostObjectExternalKey() +"-" + GlobalConstants.EPC_API_WBS_DIRECT_CHARGE_ID;
			apiType.setParentCostObjectExternalKey(actApiType.getCostObjectExternalKey());
		} 
		apiType.setCostObjectID(GlobalConstants.EPC_API_WBS_DIRECT_CHARGE_ID);
		apiType.setCostObjectName(GlobalConstants.EPC_API_WBS_DIRECT_CHARGE_NAME);
		apiType.setExternalKey(externalId);
		apiType.setCostObjectTypeName(GlobalConstants.EPC_REST_COSTOBJECTTYPE_DC);

		return apiType;
	}

	private void processStatusMessagesCO(List<PolcoApiErrorDAO> errorList, boolean isNew)  throws SystemException {
		try{
			//stgDBMgr.insertWbsBatch(this.wbsAPIErrorDAOList);
			if (isNew)
				stgDBMgr.insertPolcoBatch(errorList, GlobalConstants.EPC_DCO_API_ERROR_BATCH_INSERT);
			else
				stgDBMgr.updatePolcoBatch(errorList, GlobalConstants.EPC_DCO_API_ERROR_BATCH_UPDATE);

		} catch(Exception e) {
			throw new SystemException (e);
		}
	}
	//End Section Processing Direct Charge COs

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
		logger.debug("Processing of Actuals related Cost Elements Completed for Project: " + projectId);
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
				CaApiErrorDAO statusMsg = this.getCaAPIErrorDAO(ca, projectId);
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
				CaApiErrorDAO statusMsg = this.getCaAPIErrorDAO(ca, projectId);
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
					itgIssueLog.add(getIntegrationIssuesAPIType("",	str, projectId, ""));

				}
				errorList.add(statusMsg);				
			}
		}
		return session;
	}	
	private Cookie requestTRC(List<BayerTransactionCategoryAPIType> subList, Cookie session,
			String baseUri, String projectId, String categoryType, List<TrcApiErrorDAO> errorList) throws SystemException {

		BayerTransactionCategoryAPIRequestType request = new BayerTransactionCategoryAPIRequestType();
		request.getBayerTransactionCategoryAPI().addAll(subList);

		TRCObjectFactory objectFactory = new TRCObjectFactory();
		JAXBElement<BayerTransactionCategoryAPIRequestType> requestWrapper = objectFactory.createBayerTransactionCategoryAPIRequest(request);

		HashMap<String, String> paramMap = new HashMap<String, String>();
		paramMap.put(GlobalConstants.EPC_TRANSACTION_CATEGORY_PARAM, categoryType);

		ClientResponse response = epcRestMgr
				.postApplicationXmlAsApplicationXml(client, requestWrapper,
						baseUri, GlobalConstants.EPC_REST_IMPORT_SAP_TRC,
						session, paramMap);

		logger.debug(response);
		BayerTransactionCategoryAPIResultType result = epcRestMgr.responseToObject(response, BayerTransactionCategoryAPIResultType.class);
		if(session == null)
			session = epcRestMgr.getSessionCookie(response);

		if(!result.isSuccessFlag()){
			String errMsg="The interface failed to load any record due to data issues; please verify data.";
			if (result.getError() != null)
				errMsg=result.getError().toString();
			for(BayerTransactionCategoryAPIType ca: subList)
			{
				//convert to APIErrorDAO type
				TrcApiErrorDAO statusMsg = this.getTrcAPIErrorDAO(ca, categoryType);
				statusMsg.setRootCostObjectID(projectId);
				statusMsg.setStatus(GlobalConstants.STAGING_RECORD_STATUS_FAILED);
				statusMsg.setErrorMsg(errMsg);
				errorList.add(statusMsg);
			}
			throw new SystemException(errMsg);
		} else {

			//List<StatusMessage> statusMsgList = new ArrayList<StatusMessage>();
			int i=0;
			for(TRCObjectResultType or : result.getObjectResult()) {
				BayerTransactionCategoryAPIType ca = subList.get(i++);
				String caID = ca.getTransactionCategoryID();

				//convert to APIErrorDAO type
				TrcApiErrorDAO statusMsg = this.getTrcAPIErrorDAO(ca, categoryType);
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
					itgIssueLog.add(getIntegrationIssuesAPIType("",	str, projectId, ""));
				}
				errorList.add(statusMsg);				
			}
		}
		return session;
	}
	private CaApiErrorDAO getCaAPIErrorDAO(BayerCostAccountsAPIType apiType, String projectId)
	{
		CaApiErrorDAO apiError = new CaApiErrorDAO();
		apiError.setID(apiType.getID());
		apiError.setPathID(apiType.getID());
		//apiError.setCostObjectHierarchyPathID(apiType.getCostObjectHierarchyPathID());
		apiError.setName(apiType.getName());
		apiError.setRootCostObjectID(projectId);
		apiError.setSAPSource(GlobalConstants.SAP_SOURCE_ACTUAL);
		return apiError;
	}

	private boolean isValidCADataType (BayerActualsAPIType apiType)
	{
		boolean isValid = false;
		String caId = apiType.getCostAccountID();
		if (caId!=null || !caId.equals(""))
			isValid = true;
		return isValid;
	}

	public BayerCostAccountsAPIType getBayerCostAccountsAPIType(BayerActualsAPIType actApiType)
	{
		BayerCostAccountsAPIType apiType = new BayerCostAccountsAPIType();
		//String projectId = oDataType.getProjectDefinition();
		apiType.setID(actApiType.getCostAccountID());
		apiType.setName(actApiType.getCostAccountName());
		//apiType.setName(oDataType.getCostElement());
		return apiType;
	}

	//Convert BayerActualsAPIType to BayerCostAccountsAPIType
	public List<BayerCostAccountsAPIType> getBayerCostAccountsAPITypes(List<BayerActualsAPIType> apiTypes){

		BayerCostAccountsAPIRequestType request = new BayerCostAccountsAPIRequestType();
		Map<String, BayerCostAccountsAPIType> caMap = new HashMap<String, BayerCostAccountsAPIType>();
		for (int i = 0; i < apiTypes.size(); i++)
		{
			BayerActualsAPIType actApiType = apiTypes.get(i);

			if (isValidCADataType(actApiType))
			{
				BayerCostAccountsAPIType apiType = this.getBayerCostAccountsAPIType(actApiType);
				if (!caMap.containsKey(apiType.getID()))
				{
					request.getBayerCostAccountsAPI().add(apiType);
					caMap.put(apiType.getID(), apiType);
				}
			}
		}
		return request.getBayerCostAccountsAPI();
	}
	//End Section Processing Cost Accounts

	//Start Section Processing Categories: Vendor
	//Convert BayerActualsAPIType to BayerTransactionCategoryAPIType
	public List<BayerTransactionCategoryAPIType> getBayerTransactionCategoryAPITypes(List<BayerActualsAPIType> apiTypes, String categoryType){

		BayerTransactionCategoryAPIRequestType request = new BayerTransactionCategoryAPIRequestType();
		Map<String, BayerTransactionCategoryAPIType> trcMap = new HashMap<String, BayerTransactionCategoryAPIType>();
		for (int i = 0; i < apiTypes.size(); i++)
		{
			BayerActualsAPIType actApiType = apiTypes.get(i);

			if (isValidTRCVendorDataType(actApiType))
			{
				BayerTransactionCategoryAPIType apiType = this.getBayerTRCVendorAPIType(actApiType, categoryType);
				if (!trcMap.containsKey(apiType.getTransactionCategoryID()))
				{
					request.getBayerTransactionCategoryAPI().add(apiType);
					trcMap.put(apiType.getTransactionCategoryID(), apiType);
				}
			}
		}
		return request.getBayerTransactionCategoryAPI();
	}

	private boolean isValidTRCVendorDataType (BayerActualsAPIType apiType)
	{
		boolean isValid = false;
		String catgId = apiType.getVendorID();
		if (catgId!=null || !catgId.equals(""))
			isValid = true;
		return isValid;
	}

	public BayerTransactionCategoryAPIType getBayerTRCVendorAPIType(BayerActualsAPIType actApiType, String categoryType)
	{
		BayerTransactionCategoryAPIType apiType = new BayerTransactionCategoryAPIType();
		//String projectId = oDataType.getProjectDefinition();

		//apiType.setHierarchyPathID(actApiType.getVendorID());
		apiType.setTransactionCategoryID(actApiType.getVendorID());
		apiType.setTransactionCategoryName(actApiType.getVendorName());
		apiType.setActive(true);
		apiType.setCategoryID(categoryType);
		//apiType.setName(oDataType.getCostElement());
		return apiType;
	}



	private TrcApiErrorDAO getTrcAPIErrorDAO(BayerTransactionCategoryAPIType apiType, String categoryType)
	{
		TrcApiErrorDAO apiError = new TrcApiErrorDAO();
		apiError.setCategoryID(categoryType);
		apiError.setTransactionCategoryID(apiType.getTransactionCategoryID());
		//apiError.setCostObjectHierarchyPathID(apiType.getCostObjectHierarchyPathID());
		apiError.setTransactionCategoryName(apiType.getTransactionCategoryName());
		apiError.setActive(apiType.isActive());
		//apiError.setHierarchyPathID(apiType.getHierarchyPathID());
		return apiError;
	}

	//End Section Processing Categories: Vendor
	//Start Section Read SAP Data
	private List<BayerActualsAPIType> readSapData(String projectId, String systemId, String startDate, 
			String endDate) throws SystemException {
		Map<String, Map<String, Object>> dataRows = odataSvcMgr.readActuals(projectId, systemId, startDate, endDate);
		List<BayerActualsAPIType> apiTypes = new ArrayList<BayerActualsAPIType>();
		if (dataRows!=null)
		{
			logger.info(dataRows.size()+ " rows of odata read\n");		
			Map<String, BayerActualsAPIType> apiList = odataSvcMgr.mapActualsForImport(dataRows);
			apiTypes.addAll(apiList.values());
		}
		return apiTypes;
	}

	public List<ActApiErrorDAO> getSapInputInvalid(List<BayerActualsAPIType> apiTypes){

		List<ActApiErrorDAO> errorList = new ArrayList<ActApiErrorDAO>();
		for (int i = 0; i < apiTypes.size(); i++)
		{
			BayerActualsAPIType apiType = apiTypes.get(i);
			if (!isValidAPIType(apiType))
			{
				ActApiErrorDAO errorDAO = this.getActAPIErrorDAO(apiType);
				errorDAO.setStatus(GlobalConstants.STAGING_RECORD_STATUS_FAILED);
				errorList.add(errorDAO);
			}
		}
		return errorList;
	}
	//End Section Read SAP Data

	private List<PrjApiErrorDAO> UpdateProject( BayerProjectAPIType projAPIType,
			String projectId) throws SystemException{

		logger.debug("Updating Project for LastRunDate");

		List<PrjApiErrorDAO> retStatusMsgList = new ArrayList<PrjApiErrorDAO>();
		try {

			//Prepare for the REST call
			String baseUri = GlobalConstants.EPC_REST_PROTOCOL + "://" + GlobalConstants.EPC_REST_SERVER + ":" + 
					GlobalConstants.EPC_REST_PORT + "/" + GlobalConstants.EPC_REST_BASEURI2;					

			Stopwatch timerBatch = new Stopwatch();
			Cookie session = null;
			List<PrjApiErrorDAO> statusMsgList = new ArrayList<PrjApiErrorDAO>();
			timerBatch.start();
			session = this.requestPRJ(projAPIType, 
					session, baseUri, projectId, statusMsgList);
			logger.info("Elapsed time: " + timerBatch.stop().toString(ISOPeriodFormat.alternateExtended()));
			retStatusMsgList.addAll(statusMsgList);
			//this.epcRestMgr.logout(client, baseUri, session);

		} catch(Exception e) {
			throw new SystemException(e);
		}
		logger.debug("Complete!");
		return retStatusMsgList;
	}	


	//Start Section Update Project on LastRunTime
	private Cookie requestPRJ(BayerProjectAPIType projAPIType, Cookie session,
			String baseUri, String projectId, List<PrjApiErrorDAO> errorList) throws SystemException {

		BayerProjectAPIRequestType request = new BayerProjectAPIRequestType();
		//request.getBayerProjectAPI().addAll(subList);
		request.getBayerProjectAPI().add(projAPIType);
		PRJObjectFactory objectFactory = new PRJObjectFactory();
		JAXBElement<BayerProjectAPIRequestType> requestWrapper = objectFactory.createBayerProjectAPIRequest(request);
		//HashMap<String, String> prjMap = new HashMap<String, String>();
		//prjMap.put("ProjectId", projAPIType.getInternalID());
		ClientResponse response = epcRestMgr
				.postApplicationXmlAsApplicationXml(client, requestWrapper,
						baseUri, GlobalConstants.EPC_REST_READ_ECOSYS_PROJECTS,	session);

		logger.debug(response);
		BayerProjectAPIResultType result = epcRestMgr.responseToObject(response, BayerProjectAPIResultType.class);
		if(session == null)
			session = epcRestMgr.getSessionCookie(response);

		if(!result.isSuccessFlag()){
			String errMsg="The interface failed to load any record due to data issues; please verify data.";
			if (result.getError() != null)
				errMsg=result.getError().toString();
			//convert to APIErrorDAO type
			PrjApiErrorDAO statusMsg = this.getPrjAPIErrorDAO(projAPIType);
			statusMsg.setRootCostObjectID(projectId);
			statusMsg.setStatus(GlobalConstants.STAGING_RECORD_STATUS_FAILED);
			statusMsg.setErrorMsg(errMsg);
			errorList.add(statusMsg);
			throw new SystemException(errMsg);
		} else {

			//List<StatusMessage> statusMsgList = new ArrayList<StatusMessage>();
			int i=0;
			for(PRJObjectResultType or : result.getObjectResult()) {
				BayerProjectAPIType ca = projAPIType;
				String caID = ca.getID();

				//convert to APIErrorDAO type
				PrjApiErrorDAO statusMsg = this.getPrjAPIErrorDAO(ca);
				statusMsg.setRootCostObjectID(projectId);

				if(or.isSuccessFlag()) {
					logger.debug("UPDATE RESULT --> " + or.getInternalId());
					logger.debug("Record with External Key ID of : " + caID +" "+ GlobalConstants.STAGING_RECORD_STATUS_LOADED);
					statusMsg.setStatus(GlobalConstants.STAGING_RECORD_STATUS_LOADED);

				} else {
					//String str = ((ElementNSImpl)or.getResultMessage().get(0).getMessage()).getTextContent();
					String str = or.getResultMessage().get(0).getMessage();
					logger.error("ERROR --> " + or.getInternalId() + "|" + caID + "|" + or.isSuccessFlag() + "|" + str);	

					statusMsg.setStatus(GlobalConstants.STAGING_RECORD_STATUS_FAILED);
					statusMsg.setErrorMsg(str);				
					itgIssueLog.add(getIntegrationIssuesAPIType("",	str, projectId, ""));
				}
				errorList.add(statusMsg);				
			}
		}
		return session;
	}	

	private PrjApiErrorDAO getPrjAPIErrorDAO(BayerProjectAPIType apiType)
	{
		PrjApiErrorDAO apiError = new PrjApiErrorDAO();
		apiError.setID(apiType.getID());
		//apiError.setCostObjectHierarchyPathID(apiType.getCostObjectHierarchyPathID());
		apiError.setCostObjectName(apiType.getCostObjectName());
		apiError.setCostObjectStatus(apiType.getCostObjectStatus());
		apiError.setCostObjectTypeName(apiType.getCostObjectTypeName());
		apiError.setCostObjectHierarchyLevel(apiType.getCostObjectHierarchyLevel());
		apiError.setSAPSystemID(apiType.getSAPSystemID());
		apiError.setInternalID(apiType.getInternalID());
		apiError.setProjectStatusID(apiType.getProjectStatusID());
		apiError.setProjectTypeID(apiType.getProjectTypeID());
		apiError.setParentCostObjectID(apiType.getParentCostObjectID());
		apiError.setSAPLastRunDate(apiType.getSAPLastRunDate());
		return apiError;
	}

	private void processStatusMessagesPRJ(List<PrjApiErrorDAO> errorList, boolean isNew)  throws SystemException {
		try{
			//stgDBMgr.insertWbsBatch(this.wbsAPIErrorDAOList);
			if (isNew)
				stgDBMgr.insertPrjBatch(errorList, GlobalConstants.EPC_PRJ_API_ERROR_BATCH_INSERT);
			else
				stgDBMgr.updatePrjBatch(errorList, GlobalConstants.EPC_PRJ_API_ERROR_BATCH_UPDATE);

		} catch(Exception e) {
			throw new SystemException (e);
		}
	}

	//End Section Update Project on LastRuntime


	//Start Section Hexagon Sample Section
	//Convert SAPACTODataType List to BayerDirectChargeCOAPIType List
	public List<BayerDirectChargeCOAPIType> getBayerDirectChargeCOAPITypesSample(List<SapACTODataType> oDataTypes){

		BayerDirectChargeCOAPIRequestType request = new BayerDirectChargeCOAPIRequestType();
		for (int i = 0; i < oDataTypes.size(); i++)
		{
			SapACTODataType oDataType = oDataTypes.get(i);
			if (isValidCODataTypeSample(oDataType))
			{
				BayerDirectChargeCOAPIType apiType = this.getBayerDirectChargeCOAPITypeSample(oDataType);
				request.getBayerDirectChargeCOAPI().add(apiType);
			}
		}
		return request.getBayerDirectChargeCOAPI();
	}

	public BayerDirectChargeCOAPIType getBayerDirectChargeCOAPITypeSample(SapACTODataType oDataType)
	{
		BayerDirectChargeCOAPIType apiType = new BayerDirectChargeCOAPIType();
		String projectId = oDataType.getProjectDefinition();
		//String Id = oDataType.getPurchReq();
		String externalId = new String();
		if (oDataType.getPmOrderid()!=null && !oDataType.getPmOrderid().equals(""))
		{
			externalId = oDataType.getPmOrderid()+"-" + GlobalConstants.EPC_API_WBS_DIRECT_CHARGE_ID;
			apiType.setParentCostObjectExternalKey(oDataType.getPmOrderid());
		} 
		else if (oDataType.getWbsElement()!=null && !oDataType.getWbsElement().equals(""))
		{
			externalId = oDataType.getWbsElement()+"-" + GlobalConstants.EPC_API_WBS_DIRECT_CHARGE_ID;
			apiType.setParentCostObjectExternalKey(oDataType.getWbsElement());
		}

		apiType.setCostObjectID(GlobalConstants.EPC_API_WBS_DIRECT_CHARGE_ID);
		apiType.setCostObjectName(GlobalConstants.EPC_API_WBS_DIRECT_CHARGE_NAME);
		apiType.setExternalKey(externalId);
		apiType.setCostObjectTypeName(GlobalConstants.EPC_REST_COSTOBJECTTYPE_DC);

		return apiType;
	}
	//Convert SAPACTODataType List to BayerDirectChargeCOAPIType List
	public List<BayerCostAccountsAPIType> getBayerCostAccountsAPITypesSample(List<SapACTODataType> oDataTypes){

		BayerCostAccountsAPIRequestType request = new BayerCostAccountsAPIRequestType();
		Map<String, BayerCostAccountsAPIType> caMap = new HashMap<String, BayerCostAccountsAPIType>();
		for (int i = 0; i < oDataTypes.size(); i++)
		{
			SapACTODataType oDataType = oDataTypes.get(i);

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

	//Convert SAPACTODataType List to BayerDirectChargeCOAPIType List
	public List<BayerCostAccountsAPIType> getBayerCostAccountsAPITypesSampleRaw(List<SapACTODataType> oDataTypes){

		BayerCostAccountsAPIRequestType request = new BayerCostAccountsAPIRequestType();
		Map<String, BayerCostAccountsAPIType> caMap = new HashMap<String, BayerCostAccountsAPIType>();
		for (int i = 0; i < oDataTypes.size(); i++)
		{
			SapACTODataType oDataType = oDataTypes.get(i);

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
	public BayerCostAccountsAPIType getBayerCostAccountsAPITypeSample(SapACTODataType oDataType)
	{
		BayerCostAccountsAPIType apiType = new BayerCostAccountsAPIType();
		String projectId = oDataType.getProjectDefinition();
		apiType.setID(oDataType.getCostElement());
		//apiType.setName(oDataType.getCostElement());
		return apiType;
	}

	private boolean isValidCADataTypeSample (SapACTODataType oDataType)
	{
		boolean isValid = false;
		BayerActualsAPIType apiType = new BayerActualsAPIType();
		String caId = oDataType.getCostElement();
		if (caId!=null || !caId.equals(""))
			isValid = true;

		return isValid;
	}
	/* Convert SapWBSODataType Object to BayerWBSAPIType Object
	 * 
	 */

	public BayerActualsAPIType getActualsAPITypeSampleRaw(SapACTODataType oDataType)
	{
		BayerActualsAPIType apiType = new BayerActualsAPIType();
		String projectId = oDataType.getProjectDefinition();
		//String Id = oDataType.getPurchReq();
		String externalId = new String();
		if (oDataType.getPmOrderid()!=null && !oDataType.getPmOrderid().equals(""))
		{
			externalId = oDataType.getPmOrderid();
			apiType.setSAPWBSElement(oDataType.getPmOrderid());
		} 
		else if (oDataType.getWbsElement()!=null && !oDataType.getWbsElement().equals(""))
		{
			externalId = oDataType.getWbsElement();
			apiType.setSAPWBSElement(oDataType.getWbsElement());
		}
		else
			externalId = "";

		if (oDataType.getPurchOrd()!=null && !oDataType.getPurchOrd().equals(""))
			externalId = oDataType.getPurchOrd()+ "_" + oDataType.getPurchOrdItem();

		apiType.setCostObjectExternalKey(externalId);
		//apiType.setActualEndDate(oDataType.getEndDate());
		//apiType.setActualStartDate(oDataType.getStartDate());
		apiType.setControllingArea(oDataType.getControllingArea());
		apiType.setCostTransactionCurrency(oDataType.getAmountTcurr());
		apiType.setCostCostObjectCurrency(oDataType.getAmountOcurr());
		apiType.setCostAccountID(oDataType.getCostElement());
		apiType.setCurrencyTransactionCode(oDataType.getTransCurr());
		apiType.setCurrencyCostObjectCode(oDataType.getObjectCurr());
		apiType.setFIDocumentNumber(oDataType.getDocumentNo());
		apiType.setConversionRateCostObjectCurrency(oDataType.getExchangeRate());
		apiType.setTransactionDate(oDataType.getPostingDate());
		apiType.setActualReferenceDoc(oDataType.getAcReferenceDoc());
		apiType.setActualReferenceHeaderText(oDataType.getAcHeaderTxt());
		apiType.setCommitmentID(oDataType.getPurchOrd());
		apiType.setSAPPurchasingDocumentLineItemNumber(oDataType.getPurchOrdItem());
		apiType.setVendorID(oDataType.getVendorId());
		apiType.setVendorName(oDataType.getVendorName());
		apiType.setSAPUniqueID(oDataType.getControllingArea()+"-" 
				+ oDataType.getDocumentNo() 
				+ "-" + oDataType.getPostingRow());
		apiType.setExternalKey(apiType.getSAPUniqueID());
		return apiType;
	}
	public BayerActualsAPIType getActualsAPITypeSample(SapACTODataType oDataType)
	{
		BayerActualsAPIType apiType = new BayerActualsAPIType();
		String projectId = oDataType.getProjectDefinition();
		//String Id = oDataType.getPurchReq();
		String externalId = new String();
		if (oDataType.getPmOrderid()!=null && !oDataType.getPmOrderid().equals(""))
		{
			externalId = oDataType.getPmOrderid()+"-" + GlobalConstants.EPC_API_WBS_DIRECT_CHARGE_ID;
			apiType.setSAPWBSElement(oDataType.getPmOrderid());
		} 
		else if (oDataType.getWbsElement()!=null && !oDataType.getWbsElement().equals(""))
		{
			externalId = oDataType.getWbsElement()+"-" + GlobalConstants.EPC_API_WBS_DIRECT_CHARGE_ID;
			apiType.setSAPWBSElement(oDataType.getWbsElement());
		}
		else
			externalId = "";

		if (oDataType.getPurchOrd()!=null && !oDataType.getPurchOrd().equals(""))
			externalId = oDataType.getPurchOrd()+ "_" + oDataType.getPurchOrdItem();

		apiType.setCostObjectExternalKey(externalId);
		//apiType.setActualEndDate(oDataType.getEndDate());
		//apiType.setActualStartDate(oDataType.getStartDate());
		apiType.setControllingArea(oDataType.getControllingArea());
		apiType.setCostTransactionCurrency(oDataType.getAmountTcurr());
		apiType.setCostCostObjectCurrency(oDataType.getAmountOcurr());
		apiType.setCostAccountID(oDataType.getCostElement());
		apiType.setCurrencyTransactionCode(oDataType.getTransCurr());
		apiType.setCurrencyCostObjectCode(oDataType.getObjectCurr());
		apiType.setFIDocumentNumber(oDataType.getDocumentNo());
		apiType.setConversionRateCostObjectCurrency(oDataType.getExchangeRate());
		apiType.setTransactionDate(oDataType.getPostingDate());
		apiType.setActualReferenceDoc(oDataType.getAcReferenceDoc());
		apiType.setActualReferenceHeaderText(oDataType.getAcHeaderTxt());
		apiType.setCommitmentID(oDataType.getPurchOrd());
		apiType.setSAPPurchasingDocumentLineItemNumber(oDataType.getPurchOrdItem());
		//apiType.setOffsettingAccountNumber(oDataType.);
		apiType.setVendorID(oDataType.getVendorId());
		apiType.setVendorName(oDataType.getVendorName());
		apiType.setSAPUniqueID(oDataType.getControllingArea()+"-" 
				+ oDataType.getDocumentNo() 
				+ "-" + oDataType.getPostingRow());
		apiType.setExternalKey(apiType.getSAPUniqueID());
		return apiType;
	}


	//Convert SAPACTODataType List to BayerActualsAPIType List
	public List<BayerActualsAPIType> getBayerActualsAPITypesSampleRaw(List<SapACTODataType> oDataTypes){

		BayerActualsAPIRequestType request = new BayerActualsAPIRequestType();
		for (int i = 0; i < oDataTypes.size(); i++)
		{
			SapACTODataType oDataType = oDataTypes.get(i);
			BayerActualsAPIType apiType = this.getActualsAPITypeSampleRaw(oDataType);
			request.getBayerActualsAPI().add(apiType);
		}
		return request.getBayerActualsAPI();
	}

	//Convert SAPACTODataType List to BayerActualsAPIType List
	public List<BayerActualsAPIType> getBayerActualsAPITypesValidSample(List<SapACTODataType> oDataTypes){

		BayerActualsAPIRequestType request = new BayerActualsAPIRequestType();
		for (int i = 0; i < oDataTypes.size(); i++)
		{
			SapACTODataType oDataType = oDataTypes.get(i);
			if (isValidODataTypeSample(oDataType))
			{
				BayerActualsAPIType apiType = this.getActualsAPITypeSample(oDataType);
				request.getBayerActualsAPI().add(apiType);
			}
		}
		return request.getBayerActualsAPI();
	}

	public List<ActApiErrorDAO> getBayerActAPITypesInvalidSample(List<SapACTODataType> oDataTypes){

		List<ActApiErrorDAO> errorList = new ArrayList<ActApiErrorDAO>();
		for (int i = 0; i < oDataTypes.size(); i++)
		{
			SapACTODataType oDataType = oDataTypes.get(i);
			if (!isValidODataTypeSample(oDataType))
			{
				BayerActualsAPIType apiType = this.getActualsAPITypeSample(oDataType);
				ActApiErrorDAO errorDAO = this.getActAPIErrorDAO(apiType);
				errorDAO.setStatus(GlobalConstants.STAGING_RECORD_STATUS_SKIPPED);
				errorList.add(errorDAO);
			}
		}
		return errorList;
	}

	private boolean isValidODataTypeSample (SapACTODataType oDataType)
	{
		boolean isValid = true;
		BayerActualsAPIType apiType = new BayerActualsAPIType();
		String projectId = oDataType.getProjectDefinition();
		String pmoId = oDataType.getPmOrderid();
		String wbsElement = oDataType.getWbsElement();
		if ((pmoId == null || pmoId.equals(""))&&
				(wbsElement == null||wbsElement.equals("")))
			isValid = false;
		return isValid;
	}

	private  List<BayerCommitmentLICOAPIType> readCommitmentLICOs(String projectId) throws SystemException{

		List<BayerCommitmentLICOAPIType> bayerCommitmentLICOs = new ArrayList<BayerCommitmentLICOAPIType>();
		try 
		{	
			//Read PM Order Data from EcoSys using PMOrder API
			Cookie session = null;
			String baseUri2 = GlobalConstants.EPC_REST_PROTOCOL + "://" + GlobalConstants.EPC_REST_SERVER + ":"
					+ GlobalConstants.EPC_REST_PORT + "/" + GlobalConstants.EPC_REST_BASEURI2;				

			HashMap<String, String> aMap = new HashMap<String, String>();
			aMap.put(GlobalConstants.EPC_PROJECT_PARAM, projectId);
			bayerCommitmentLICOs = this.getCommitmentLICOAPITypes(session, baseUri2, aMap);
			return bayerCommitmentLICOs;

		} catch(Exception e) {
			throw new SystemException(e);
		}
	}

	private List<BayerCommitmentLICOAPIType> getCommitmentLICOAPITypes(Cookie session, String baseUri, HashMap<String, String> polcoMap) throws SystemException {

		ClientResponse response = epcRestMgr
				.getAsApplicationXml(client, baseUri,
						GlobalConstants.EPC_REST_IMPORT_SAP_PO_LNITM_CO,session,polcoMap);

		logger.debug(response);
		BayerCommitmentLICOAPIResultType result = epcRestMgr.responseToObject(response, BayerCommitmentLICOAPIResultType.class);
		List<BayerCommitmentLICOAPIType> apiTypes = result.getBayerCommitmentLICOAPI();
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
	private  List<BayerCommitmentLICOAPIType> getMovedCommitmentLICOs(List<BayerCommitmentLICOAPIType> currentList, List<BayerCommitmentLICOAPIType> newList, XMLGregorianCalendar currentDate){

		List<BayerCommitmentLICOAPIType> movedCOs = new ArrayList<BayerCommitmentLICOAPIType>();
		for(BayerCommitmentLICOAPIType lico: currentList)
		{
			if (isMoved(lico, newList)==true)
			{
				lico.setCOParentChangedID(GlobalConstants.EPC_API_ERROR_FLAG_Y);
				lico.setParentCOChangeDate(currentDate);
				lico.setExternalKey(lico.getExternalKey() + GlobalConstants.EPC_CO_Changed);
				movedCOs.add(lico);
			}
			if (lico.getExternalKey().equalsIgnoreCase("120750132637"))
			{
				lico.setCOParentChangedID(GlobalConstants.EPC_API_ERROR_FLAG_Y);
				lico.setParentCOChangeDate(currentDate);
				lico.setExternalKey(lico.getExternalKey() + GlobalConstants.EPC_CO_Changed);
				//pmorder.setCostObjectID((pmorder.getCostObjectID() + GlobalConstants.EPC_CO_Changed));
				lico.setParentCostObjectExternalKey("A00NC-003247-C2H11");
				movedCOs.add(lico);
			}
		}
		return movedCOs;
	}

	private  boolean isMissingLICO(BayerActualsAPIType actualAPI, List<BayerCommitmentLICOAPIType> currentList){

		boolean isMissing = true;

		String actCOId = actualAPI.getCostObjectExternalKey();
		for(BayerCommitmentLICOAPIType lico: currentList)
		{
			if (lico.getExternalKey().equalsIgnoreCase(actCOId))
				isMissing = false;
		}
		return isMissing;
	}

	private boolean isMoved (BayerCommitmentLICOAPIType apiType,List<BayerCommitmentLICOAPIType> newList )
	{
		boolean isMoved = false;
		for(BayerCommitmentLICOAPIType lico: newList)
		{
			if (lico.getExternalKey().equalsIgnoreCase(apiType.getExternalKey())
					&& !lico.getParentCostObjectExternalKey().equalsIgnoreCase(apiType.getParentCostObjectExternalKey()))
				isMoved = true;
		}
		return isMoved;
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

	private List<String> postprocessLIs( String masterProjectId, String projectId, String projectPeriod, boolean isTracking) throws SystemException{

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

			/*comment out for HEN3. With HEN3, history tracking become the same for new and existing projects
			if (isTracking == true)
				session = this.requestPostProcess(masterProjectId, projectId, projectPeriod, session, baseUri);
			else
				session = this.requestPostProcessIni(masterProjectId, projectId, projectPeriod, session, baseUri);
			 */

			session = this.requestPostProcess(masterProjectId, projectId, projectPeriod, session, baseUri);

			retStatusMsgList.addAll(statusMsgList);


			//this.epcRestMgr.logout(client, baseUri, session);

		} catch(Exception e) {
			throw new SystemException(e);
		}
		logger.debug("Complete!");
		return retStatusMsgList;
	}

	private Cookie requestPostProcess(String masterProjectId, String projectId, String projectPeriod, Cookie session, 
			String baseUri) throws SystemException {

		BayerPostProcessAPIResultType request = new BayerPostProcessAPIResultType();
		//request.getBayerCommitmentLIAPI().addAll(subList);
		String snapDesc = GlobalConstants.EPC_API_SNAPSHOT_DESCRIPTION + "_"+ projectPeriod;
		HashMap<String,String> filterMap = new HashMap<String, String>();
		filterMap.put(GlobalConstants.EPC_SNAPSHOTDESCRIPTION_PARAM, snapDesc);
		filterMap.put(GlobalConstants.EPC_PROJECTPERIOD_PARAM, projectPeriod);
		filterMap.put(GlobalConstants.EPC_ROOTCOSTOBJECT_PARAM, masterProjectId);
		filterMap.put(GlobalConstants.EPC_SUBPROJECT_PARAM, projectId);

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



	private String getPeriodId(Integer year, Integer month)
	{
		String periodId = "";
		String sMonth = "";
		if (month<10) 
			sMonth = "0"+month.toString();
		else
			sMonth = month.toString();

		periodId = year.toString()+"-"+sMonth;
		return periodId;
	}


	private List<String> deletePeriodActuals( String projectId, String periodId) throws SystemException{

		logger.debug("Deleting Period Actuals for Project: " + projectId + " for Period: " + periodId);
		List<String> retStatusMsgList = new ArrayList<String>();
		try {

			//Prepare for the REST call
			String baseUri = GlobalConstants.EPC_REST_PROTOCOL + "://" + GlobalConstants.EPC_REST_SERVER + ":" + 
					GlobalConstants.EPC_REST_PORT + "/" + GlobalConstants.EPC_REST_BASEURI2;

			Stopwatch timerBatch = new Stopwatch();			
			Cookie session = null;

			List<String> statusMsgList = new ArrayList<String>();
			timerBatch.start();
			session = this.deleteRequest(projectId, periodId, session, baseUri);
			//logger.info(end + " of " + total + " - elapsed time: " + timerBatch.stop().toString(ISOPeriodFormat.alternateExtended()));
			retStatusMsgList.addAll(statusMsgList);

			//this.epcRestMgr.logout(client, baseUri, session);

		} catch(Exception e) {
			throw new SystemException(e);
		}
		logger.debug("Complete!");
		return retStatusMsgList;
	}

	private Cookie deleteRequest(String projectId, String periodId, Cookie session, 
			String baseUri) throws SystemException {

		BayerActualsPeriodDeleteAPIResultType request = new BayerActualsPeriodDeleteAPIResultType();

		HashMap<String,String> filterMap = new HashMap<String, String>();
		filterMap.put("ProjectId", projectId);
		filterMap.put("ReportPeriod", periodId);

		ActpdObjectFactory objectFactory = new ActpdObjectFactory();
		JAXBElement<BayerActualsPeriodDeleteAPIResultType> requestWrapper = objectFactory.createBayerActualsPeriodDeleteAPIResult(request);

		ClientResponse response = epcRestMgr
				.postApplicationXmlAsApplicationXml(client, requestWrapper,
						baseUri, GlobalConstants.EPC_REST_DELETE_SAP_ACT_PERIOD,
						session, filterMap);

		logger.debug(response);
		BayerActualsPeriodDeleteAPIResultType result = epcRestMgr.responseToObject(response, BayerActualsPeriodDeleteAPIResultType.class);

		if(session == null)
			session = epcRestMgr.getSessionCookie(response);

		if(!result.isSuccessFlag()){
			String errMsg="The interface failed to load any record due to data issues; please verify data.";
			if (result.getError() != null)
				errMsg=result.getError().toString();

			throw new SystemException(errMsg);
		} else {

			//List<StatusMessage> statusMsgList = new ArrayList<StatusMessage>();
			logger.debug("3000 -- Period Actuals Deleting Completed Successfully for Project: "+ projectId);
			int i=0;
		}
		return session;
	}

	//Aug 2021: Phase 3 CR2 section
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

	private boolean isParentMissing (BayerActualsAPIType apiType,List<BayerWBSReadAPIType> parentList )
	{
		boolean isMissing = true;
		for(BayerWBSReadAPIType pco: parentList)
		{
			if (apiType.getCostObjectExternalKey().equalsIgnoreCase(pco.getExternalKey()))
				isMissing = false;
		}
		return isMissing;
	}

	private  List<BayerActualsAPIType> getValidParentCoLIs(List<BayerActualsAPIType> inputList, List<BayerActualsAPIType> missingList, List<BayerWBSReadAPIType> parentList){

		List<BayerActualsAPIType> withParentCOs = new ArrayList<BayerActualsAPIType>();
		for(BayerActualsAPIType lico: inputList)
		{
			if (!this.isParentMissing(lico, parentList))
			{
				withParentCOs.add(lico);
			}
			else
			{
				//lico.setParentCostObjectExternalKey(defaultParent);
				missingList.add(lico);
			}
		}
		return withParentCOs;
	}

	private List<ActApiErrorDAO> processMissingCOLIs(List<BayerActualsAPIType> missingCOActs, String projectId)
	{
		List<ActApiErrorDAO> errorList = new ArrayList<ActApiErrorDAO>();
		for(BayerActualsAPIType apiType: missingCOActs)
		{

			logger.error("ERROR Missing Owner Cost Object in EcoSys --> Project ID: " + projectId + "|"
					+ "CostObjectExternalKey: " + apiType.getCostObjectExternalKey() + "|"
					+ "CostObjectID: " + apiType.getCostObjectID() + "|"
					+ "ExternalKey: " + apiType.getExternalKey() + "|" 
					+ "ActualReferenceDoc: " + apiType.getActualReferenceDoc() + "|"
					+ "ActualReferenceHeaderText: " + apiType.getActualReferenceHeaderText() + "|"
					+ "CommitmentID: " + apiType.getCommitmentID() + "|"
					+ "ControllingArea: " + apiType.getControllingArea() + "|"
					+ "CostAccountID: " + apiType.getCostAccountID() + "|"
					+ "CostAccountName: " + apiType.getCostAccountName() + "|"
					+ "CurrencyCostObjectCode: " + apiType.getCurrencyCostObjectCode() + "|"
					+ "CurrencyTransactionCode: " + apiType.getCurrencyTransactionCode() + "|"
					+ "ExternalKey: " + apiType.getExternalKey() + "|"
					+ "FIDocumentNumber: " + apiType.getFIDocumentNumber() + "|"
					+ "OffsettingAccountNumber: " + apiType.getOffsettingAccountNumber() + "|"
					+ "SAPAccrualID: " + apiType.getSAPAccrualID() + "|"
					+ "SAPPurcharsingOrderSeqNumber: " + apiType.getSAPPurcharsingOrderSeqNumber() + "|"
					+ "SAPPurchasingDocumentLineItemNumber: " + apiType.getSAPPurchasingDocumentLineItemNumber() + "|"
					+ "SAPUniqueID: " + apiType.getSAPUniqueID() + "|"
					+ "SAPWBSElement: " + apiType.getSAPWBSElement() + "|"
					+ "TransactionExchangeRateSource: " + apiType.getTransactionExchangeRateSource() + "|" 
					+ "VendorID: " + apiType.getVendorID() + "|"
					+ "VendorName: " + apiType.getVendorName() + "|"
					+ "VersionID: " + apiType.getVersionID() + "|"  
					+ "AlternateCostTransactionCurrency: " + apiType.getAlternateCostTransactionCurrency() + "|"
					+ "ConversionRateCostObjectCurrency: " + apiType.getConversionRateCostObjectCurrency() + "|"
					+ "CostCostObjectCurrency: " + apiType.getCostCostObjectCurrency()+  "|"
					+ "CostTransactionCurrency: " + apiType.getCostTransactionCurrency() + "|"
					+ "PostingRow: " + apiType.getPostingrow() + "|"
					+ "SAPExchangeRate: " + apiType.getSAPExchangeRate()+ "|"
					+ "Transaction Date: " + apiType.getTransactionDate() + "|");	

			String errMsg="The interface skipped to load actual for " + apiType.getCostObjectExternalKey() + " due to owner Cost Object missing in EcoSys; please verify data.";
			logger.error(errMsg);

			ActApiErrorDAO statusMsg = this.getActAPIErrorDAO(apiType);
			statusMsg.setRootCostObjectID(projectId);
			statusMsg.setStatus(GlobalConstants.STAGING_RECORD_STATUS_FAILED);
			statusMsg.setErrorMsg(errMsg);
			errorList.add(statusMsg);
		}
		return errorList;
	}

	//End Section Phase 3 CR02
	//private List<String> processCCL( String projectId) throws SystemException{
	//		
	//		logger.debug("validating CCL assignments for Project: " + projectId);
	//		
	//		List<String> retStatusMsgList = new ArrayList<String>();
	//		try {
	//						
	//			//Prepare for the REST call
	//			String baseUri = GlobalConstants.EPC_REST_PROTOCOL + "://" + GlobalConstants.EPC_REST_SERVER + ":" + 
	//					GlobalConstants.EPC_REST_PORT + "/" + GlobalConstants.EPC_REST_BASEURI2;
	//					
	//			Stopwatch timerBatch = new Stopwatch();			
	//			Cookie session = null;
	//			
	//			List<String> statusMsgList = new ArrayList<String>();
	//			timerBatch.start();
	//			session = this.requestCCL(projectId, session, baseUri);
	//			retStatusMsgList.addAll(statusMsgList);
	//			
	//		} catch(Exception e) {
	//			throw new SystemException(e);
	//		}
	//		logger.debug("Complete!");
	//		return retStatusMsgList;
	//	}
	//
	//	private Cookie requestCCL(String projectId, Cookie session, 
	//			String baseUri) throws SystemException {
	//	
	//		BayerCCLValidationAPIResultType request = new BayerCCLValidationAPIResultType();
	//		HashMap<String,String> filterMap = new HashMap<String, String>();
	//		filterMap.put(GlobalConstants.EPC_ROOTCOSTOBJECT_PARAM, projectId);
	//	
	//		ObjectFactoryCCL objectFactory = new ObjectFactoryCCL();
	//		JAXBElement<BayerCCLValidationAPIResultType> requestWrapper = objectFactory.createBayerCCLValidationAPIResult(request);
	//		ClientResponse response = epcRestMgr
	//				.postApplicationXmlAsApplicationXml(client, requestWrapper,
	//						baseUri, GlobalConstants.EPC_REST_EVT_CCL,
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
	//		if (result.getError() != null)
	//			errMsg=result.getError().toString();
	//	
	//		throw new SystemException(errMsg);
	//		} else {
	//	
	//			logger.debug("3000 Actuals Import | Trigger Action Batch to validate CLL completed Project: "+ projectId);
	//			int i=0;
	//		}
	//		return session;
	//	}

}
