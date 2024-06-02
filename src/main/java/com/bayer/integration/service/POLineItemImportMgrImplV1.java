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

import com.bayer.integration.odata.SapPOLODataType;
import com.bayer.integration.persistence.CaApiErrorDAO;
import com.bayer.integration.persistence.PolApiErrorDAO;
import com.bayer.integration.persistence.PolcoApiErrorDAO;
import com.bayer.integration.properties.GlobalConstants;
import com.bayer.integration.rest.actual.BayerActualsAPIType;
import com.bayer.integration.rest.calcwbs.BayerCalculateWBSIDAPIResultType;
import com.bayer.integration.rest.calcwbs.CalcWObjectFactory;
import com.bayer.integration.rest.costaccount.BayerCostAccountsAPIRequestType;
import com.bayer.integration.rest.costaccount.BayerCostAccountsAPIResultType;
import com.bayer.integration.rest.costaccount.BayerCostAccountsAPIType;
import com.bayer.integration.rest.costaccount.CAObjectFactory;
import com.bayer.integration.rest.costaccount.CAObjectResultType;
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
import com.bayer.integration.rest.pold.BayerCommitmentPOLIDeleteAPIResultType;
import com.bayer.integration.rest.pold.PoldObjectFactory;
import com.bayer.integration.rest.polth.BayerCommitmentPOLITrackHistoryAPIResultType;
import com.bayer.integration.rest.polth.PolthObjectFactory;
import com.bayer.integration.rest.polthi.BayerCommitmentPOLITrackHistoryIniAPIResultType;
import com.bayer.integration.rest.polthi.PolthiObjectFactory;
import com.bayer.integration.rest.postproc.BayerPostProcessAPIResultType;
import com.bayer.integration.rest.postproc.PPObjectFactory;
import com.bayer.integration.rest.postprocini.BayerPostProcessIniAPIResultType;
import com.bayer.integration.rest.postprocini.PPIniObjectFactory;
import com.bayer.integration.rest.project.BayerProjectAPIType;
import com.ecosys.exception.SystemException;
import com.ecosys.util.Stopwatch;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.org.apache.xerces.internal.dom.ElementNSImpl;

/**
 * @author pwng
 *
 */
public class POLineItemImportMgrImplV1 extends ImportManagerBase implements
		ImportManager {

	/* (non-Javadoc)
	 * @see com.bayer.integration.service.ImportManager#importData()
	 */
	//@Override
	public int importData() {
		int retCode=GlobalConstants.IMPORT_SAP_POL_SUCCESS;
		try{			
			if (!GlobalConstants.SKIP_SAP_POL_INTERFACE) {
				
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
				XMLGregorianCalendar currentDate = dFactory.newXMLGregorianCalendar(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
				
				Integer dayInMonth = currentDate.getDay();
				//Loop through the project list
				for (int i = 0; i < projectAPITypes.size(); i++) 
		 		{
					boolean isTracking = false;
					BayerProjectAPIType projectAPIType = projectAPITypes.get(i);
					String projectId = projectAPIType.getID();
					GlobalConstants.EPC_PROJECT_ID_PROCESSED = projectId;
					String systemId = projectAPIType.getSAPSystemID();
					String sapProjectId = projectAPIType.getSapProjectId();
					if (projectAPIType.getPOHistoryTrackedID()!=null && projectAPIType.getPOHistoryTrackedID().equals(GlobalConstants.EPC_API_ERROR_FLAG_Y))
						isTracking = true;
					
					List<CaApiErrorDAO> errorCaDAOList = new ArrayList<CaApiErrorDAO>();
					List<PolcoApiErrorDAO> errorCoDAOList = new ArrayList<PolcoApiErrorDAO>();
					List<PolApiErrorDAO> errorDAOList = new ArrayList<PolApiErrorDAO>();
					List<BayerCommitmentLICOAPIType> inputListCO = new ArrayList<BayerCommitmentLICOAPIType>();
					List<BayerCommitmentLICOAPIType> currentListCO = new ArrayList<BayerCommitmentLICOAPIType>();					
					List<BayerCommitmentLICOAPIType> movedRecords = new ArrayList<BayerCommitmentLICOAPIType>();
					
					List<BayerCommitmentLIAPIType> inputListPOL = new ArrayList<BayerCommitmentLIAPIType>();
					List<BayerCommitmentLIAPIType> inputList = new ArrayList<BayerCommitmentLIAPIType>();
					List<BayerCommitmentLIAPIType> inputListPRL = new ArrayList<BayerCommitmentLIAPIType>();
					List<PolApiErrorDAO> invalidRecords = new ArrayList<PolApiErrorDAO>(); 
					List<BayerCostAccountsAPIType> costAccounts = new ArrayList<BayerCostAccountsAPIType>();
					List<BayerCommitmentLICOAPIType> commitmentLICOs = new ArrayList<BayerCommitmentLICOAPIType>();
					List<BayerCommitmentLIAPIType> validRecords = new ArrayList<BayerCommitmentLIAPIType>();
					List<BayerCommitmentLICOAPIType> splitLICOs = new ArrayList<BayerCommitmentLICOAPIType>();
					
					session = null;

					try
					{
						//soft delete existing records from EcoSys
						logger.debug("Purging existing PO Line Item data from EcoSys for Project: "+ projectId);
						this.deleteLIs(projectId);
						logger.debug("Purging existing PO Line Item data from EcoSys completed for Project: "+ projectId);
						
						//Delete existing records from ERR Log table
						logger.debug("Purging existing PO Line Item log records from ERR Log table for Project: "+ projectId);
						//int logCount = this.deleteLogs(projectId, GlobalConstants.EPC_POL_API_ERROR_BATCH_DELETE);
						//logger.debug("Purging " + logCount + " existing PO Line Item log records from ERR Log table for Project: "+ projectId);
					
						//Invoke EcoSys API with input records
						logger.debug("Reading PO Line Item data from SAP Input for Project: "+ projectId);
						


						if (isLive)
						{
							commitmentLICOs = readSapDataCO(sapProjectId, systemId);
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
							commitmentLICOs = this.getBayerCommitmentLICOAPITypesSample(inputListSample);
							inputList = this.getBayerCommitmentLIAPITypesSample(inputListSample);
							invalidRecords = this.getBayerCommitmentLIAPITypesInvalidSample(inputListSample);
							//End Hexagon Dev Sample Data Section
						}
							
						costAccounts = this.getBayerCostAccountsAPITypes(inputList);
						validRecords = this.getBayerCommitmentLIAPITypesValid(inputList);
						
						currentListCO = this.readCommitmentLICOs(projectId);
						
						//Process Invalid Records
						logger.debug("Processing Invalid Records, If Any for Project: " + projectId);
						if (invalidRecords.size()>0 && !skipError)
							this.processStatusMessages(invalidRecords, true);
						logger.debug("Processing Invalid Records Completed for Project: "+ projectId);	
						
						//Process  PO Line Items related Cost Elements
						logger.debug("Processing PO Line Items related Cost Elements for Project: " + projectId);
						errorCaDAOList = processCostAccounts(costAccounts, projectId);			
						logger.debug("Processing PO Line Items related Cost Elements completed for Project: " + projectId);
						
						//Process Moved Records
						movedRecords = this.getMovedCommitmentLICOs(currentListCO, commitmentLICOs, currentDate);
						if (movedRecords.size()>0)
						{						
							errorCoDAOList = processCommitmentLICOs(movedRecords, projectId);
							//Process Status Messages
							if (!skipError)
							{
								logger.debug("Processing ERR Messages, If Any for Project: " + projectId);
								this.processStatusMessages(errorDAOList, true);
								logger.debug("Processing ERR Message Completed for Project: "+ projectId);
							}
						}
						
						//Process PO Line Items as CO
						logger.debug("Creating/Updating PO Line Items as Cost Objects for Project: " + projectId);
						errorCoDAOList = processCommitmentLICOs(commitmentLICOs, projectId);			
						logger.debug("Creating/Updating PO Line Items as Cost Objects completed for Project: " + projectId);
						
						//Process Status Messages
						if (!skipError)
						{
							logger.debug("Processing ERR Messages, If Any for Project: " + projectId);
							//this.processStatusMessagesCo(errorCoDAOList, true);
							logger.debug("Processing ERR Message Completed for Project: "+ projectId);	
						}
						
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

					}
		 			catch(SystemException se) {
		 				logger.error("5005 � PO Line Item Import Failed: " + GlobalConstants.EPC_PROJECT_ID_PROCESSED, se);
		 				retCode=GlobalConstants.IMPORT_SAP_POL_FAILED;
		 				continue;
		 			}
				}
			 }
					
			 else {
				logger.info("Skipped Project Interface. Change the skip property to 'false'");				
			}
			
		}catch(Exception e) {			
			logger.error("5005 � PO Line Item Import Failed", e);			
			retCode=GlobalConstants.IMPORT_SAP_POL_FAILED;
		}
		
		if (retCode==GlobalConstants.IMPORT_SAP_POL_SUCCESS)
			logger.debug("5000 � PO Line Item Import Completed Successfully");
		
		return retCode;
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

private List<PolcoApiErrorDAO> processCommitmentLICOs( List<BayerCommitmentLICOAPIType> prjRecords,
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


	private Cookie requestCO(List<BayerCommitmentLICOAPIType> subList, Cookie session,
			String baseUri, String projectId, List<PolcoApiErrorDAO> errorList) throws SystemException {
		
		BayerCommitmentLICOAPIRequestType request = new BayerCommitmentLICOAPIRequestType();
		request.getBayerCommitmentLICOAPI().addAll(subList);

		COObjectFactory objectFactory = new COObjectFactory();
		JAXBElement<BayerCommitmentLICOAPIRequestType> requestWrapper = objectFactory.createBayerCommitmentLICOAPIRequest(request);
		HashMap<String, String> prjMap = new HashMap<String, String>();
		prjMap.put(GlobalConstants.EPC_PROJECT_PARAM, projectId);
		ClientResponse response = epcRestMgr
					.postApplicationXmlAsApplicationXml(client, requestWrapper,
							baseUri, GlobalConstants.EPC_REST_IMPORT_SAP_PO_LNITM_CO,
							session, prjMap);
							
		logger.debug(response);
		BayerCommitmentLICOAPIResultType result = epcRestMgr.responseToObject(response, BayerCommitmentLICOAPIResultType.class);
		if(session == null)
			session = epcRestMgr.getSessionCookie(response);
		
		if(!result.isSuccessFlag()){
			String errMsg="The interface failed to load any record due to data issues; please verify data.";
			if (result.getError() != null)
				errMsg=result.getError().toString();
				for(BayerCommitmentLICOAPIType pol: subList)
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
				BayerCommitmentLICOAPIType poLI = subList.get(i++);
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
					String str = ((ElementNSImpl)or.getResultMessage().get(0).getMessage()).getTextContent();
					logger.error("ERROR --> " + or.getInternalId() + "|" + polID + "|" 
									 + "CostObjectID: " + poLI.getCostObjectID() + "|"
									 + "CostObjectName: " + poLI.getCostObjectName()+ "|"
									 + "CostObjectStatus: " + poLI.getCostObjectStatus() + "|"
									 + "CostObjectTypeName: " + poLI.getCostObjectTypeName() + "|"
								     + "ExternalKey: " + poLI.getExternalKey()+ "|"
									 + "ParentCostObjectExternalKey: " + poLI.getParentCostObjectExternalKey() + "|"					
									 + "COParentChangedID: " + poLI.getCOParentChangedID() + "|"
									 + "ParentCOChangeDate " + poLI.getParentCOChangeDate()+ "|"
										     
							+ or.isSuccessFlag() + "|" + str);	
				
					statusMsg.setStatus(GlobalConstants.STAGING_RECORD_STATUS_FAILED);
					statusMsg.setErrorMsg(str);				
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
					//String str = ((ElementNSImpl)or.getResultMessage().get(0).getMessage()).getTextContent();
					String str = or.getResultMessage().get(0).getMessage();
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
			logger.debug("5000 � PO Line Item Delete Completed Successfully for Project: "+ projectId);
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
			logger.debug("8000 � Post Process Completed Successfully for Project: "+ projectId);
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
			logger.debug("8000 � Post Process Completed Successfully for Project: "+ projectId);
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
			logger.debug("5000 � PO Line Item Track History Completed Successfully for Project: "+ projectId);
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
			logger.debug("5000 � PO Line Item Track History Initiation Completed Successfully for Project: "+ projectId);
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

	private PolcoApiErrorDAO getPolcoAPIErrorDAO(BayerCommitmentLICOAPIType apiType)
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
	
	  
		private List<BayerCommitmentLICOAPIType> readSapDataCO(String projectId, String systemId) throws SystemException {
			Map<String, Map<String, Object>> dataRows = odataSvcMgr.readPOLineItems(projectId, systemId);
			List<BayerCommitmentLICOAPIType> apiTypes = new ArrayList<BayerCommitmentLICOAPIType>();
			if (dataRows!=null)
			{
				Map<String, BayerCommitmentLICOAPIType> apiList = odataSvcMgr.mapPOLineItemCOForImport(dataRows);
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
		
	    public List<BayerCommitmentLIAPIType> getBayerCommitmentLIAPITypesValid(List<BayerCommitmentLIAPIType> apiTypes){
			
	    	BayerCommitmentLIAPIRequestType request = new BayerCommitmentLIAPIRequestType();
	    	for (int i = 0; i < apiTypes.size(); i++)
	    	{
	    		BayerCommitmentLIAPIType apiType = apiTypes.get(i);
	    		if (isValidAPIType(apiType))
	    		{
	        		request.getBayerCommitmentLIAPI().add(apiType);
	    		}
	    	}
	        return request.getBayerCommitmentLIAPI();
	    }
	    
	    public List<BayerCommitmentLICOAPIType> getBayerCommitmentLIAPITypes(List<BayerCommitmentLIAPIType> colApiTypes){			
		    BayerCommitmentLICOAPIRequestType request = new BayerCommitmentLICOAPIRequestType();
		    for (int i = 0; i < colApiTypes.size(); i++)
		    {
		    	BayerCommitmentLIAPIType colApiType = colApiTypes.get(i);
		    	if (isValidAPIType(colApiType))
		    	{
		    		BayerCommitmentLICOAPIType apiType = this.getBayerCommitmentLICOAPIType(colApiType);
		    		request.getBayerCommitmentLICOAPI().add(apiType);
		    	}
		    }
		    return request.getBayerCommitmentLICOAPI();
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
	    
		public BayerCommitmentLICOAPIType getBayerCommitmentLICOAPIType(BayerCommitmentLIAPIType colApiType)
		{
			BayerCommitmentLICOAPIType apiType = new BayerCommitmentLICOAPIType();
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
	    public List<BayerCommitmentLICOAPIType> getBayerCommitmentLICOAPITypesSample(List<SapPOLODataType> oDataTypes){			
		    BayerCommitmentLICOAPIRequestType request = new BayerCommitmentLICOAPIRequestType();
		    for (int i = 0; i < oDataTypes.size(); i++)
		    {
		    	SapPOLODataType oDataType = oDataTypes.get(i);
		    	if (isValidODataTypeSample(oDataType))
		    	{
		    		BayerCommitmentLICOAPIType apiType = this.getCommitmentLICOAPITypeSample(oDataType);
		    		request.getBayerCommitmentLICOAPI().add(apiType);
		    	}
		    }
		    return request.getBayerCommitmentLICOAPI();
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
		
	    /* Convert SapWBSODataType Object to BayerCommitmentLICOAPIType Object
	     * 
	     */
		public BayerCommitmentLICOAPIType getCommitmentLICOAPITypeSample(SapPOLODataType oDataType)
		{
			BayerCommitmentLICOAPIType apiType = new BayerCommitmentLICOAPIType();
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
				logger.debug("1000 � WBS Import - WBS ID Recalculateion Completed Successfully");
				int i=0;
			}
			return session;
		}
}