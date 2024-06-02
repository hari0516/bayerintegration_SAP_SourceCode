/**
 * 
 */
package com.bayer.integration.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.core.Cookie;
import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.bayer.integration.properties.GlobalConstants;
import com.bayer.integration.rest.postproc.BayerPostProcessAPIResultType;
import com.bayer.integration.rest.postproc.PPObjectFactory;
import com.bayer.integration.rest.project.BayerProjectAPIType;
import com.ecosys.exception.SystemException;
import com.ecosys.util.Stopwatch;
import com.sun.jersey.api.client.ClientResponse;


/**
 * @author pwng
 *
 */
public class MonthlyClosingMgrImpl extends ImportManagerBase implements
		ImportManager {

	/* (non-Javadoc)
	 * @see com.bayer.integration.service.ImportManager#importData()
	 */
	//@Override
	public int importData() {
		int retCode=GlobalConstants.IMPORT_SAP_MON_SUCCESS;
		try{			
			if (!GlobalConstants.SKIP_SAP_MON_INTERFACE) {
				
				boolean skipError = GlobalConstants.SKIP_LOG;
				//change the value to false if using sample data
				boolean isLive = GlobalConstants.IS_LIVE_SAP;
				//Create Web Service Client
				if (client == null) 
					setClient(epcRestMgr.createClient(GlobalConstants.EPC_REST_USERNAME, GlobalConstants.EPC_REST_PASSWORD));
				
				//Read Project Data from EcoSys using project API
				Cookie session = null;

				
				//Retrieve list of projects for SAP Integration from EcoSys				
				prjExpMgr.ExportData();
				List<BayerProjectAPIType> projectAPITypes = prjExpMgr.getBayerProjectAPITypes();
				
				List<BayerProjectAPIType> projClosedList = new ArrayList<BayerProjectAPIType>();

		    	DatatypeFactory dFactory = DatatypeFactory.newInstance();
				XMLGregorianCalendar currentDate = dFactory.newXMLGregorianCalendar(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
				
				//Phase IV - Closing Year 
				if (GlobalConstants.PROCESS_SAP_YEARCLOSE && !GlobalConstants.EPC_YEAR_CLOSE_DATE.equalsIgnoreCase("2000-01-01"))
					currentDate = dFactory.newXMLGregorianCalendar(GlobalConstants.EPC_YEAR_CLOSE_DATE);
				
				//Test First Day situation
				//currentDate.add(dFactory.newDuration("-P16D"));;
				
				XMLGregorianCalendar reportDate = (XMLGregorianCalendar)currentDate.clone();
				reportDate.add(dFactory.newDuration("-P1D"));
				Integer dayInMonth = currentDate.getDay();

				//Loop through the project list

				for (int i = 0; i < projectAPITypes.size(); i++) 
		 		{
					BayerProjectAPIType projectAPIType = projectAPITypes.get(i);
					String masterProjectId = projectAPIType.getRootCostObjectID();
					String projectId = projectAPIType.getID();
					String projectInternalId = projectAPIType.getInternalID();
					GlobalConstants.EPC_PROJECT_ID_PROCESSED = projectId;
					String sapProjectId = projectAPIType.getSapProjectId();
					String migrationFlag = null;
					String startDate = null;
					String endDate = null;
					boolean isTracking = false;
					
					startDate = GlobalConstants.SAP_ACTUAL_START_DATE;
					try
					{

						//DatatypeFactory dFactory = DatatypeFactory.newInstance();
						if (projectAPIType.getMigrationFlagID()!=null)
							migrationFlag = projectAPIType.getMigrationFlagID();
						
						if (projectAPIType.getPOHistoryTrackedID()!=null 
								&& projectAPIType.getPOHistoryTrackedID().equals(GlobalConstants.EPC_API_ERROR_FLAG_Y))
							isTracking = true;
						
						/* with decision on CR1, no need for this
						if (projectAPIType.getNewAssignedProjectFlagID()!=null 
								&& projectAPIType.getNewAssignedProjectFlagID().equalsIgnoreCase(GlobalConstants.EPC_API_ERROR_FLAG_Y))
							isNewPP = true;
						*/
						boolean isSub = false;
						if (projectAPIType.getProjectTypeID().equals(GlobalConstants.EPC_API_PROJECT_TYPE_SUB))
								isSub = true;
						
						XMLGregorianCalendar lastRunDate = null;
						XMLGregorianCalendar filterEndDate = dFactory.newXMLGregorianCalendar(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
						
						//test First Date situation
						//filterEndDate = currentDate;
						
						filterEndDate.add(dFactory.newDuration("-P1D"));
						
						XMLGregorianCalendar filterEndDate2 = (XMLGregorianCalendar)filterEndDate.clone();
						filterEndDate2.add(dFactory.newDuration("P1D"));
						
						if (migrationFlag!=null && migrationFlag.equals("Y") && projectAPIType.getSAPActualImportEndDate()!=null)
							filterEndDate = projectAPIType.getSAPActualImportEndDate();

						//
						
						//endDate = filterEndDate.toString();
						String endMonth = String.valueOf(filterEndDate.getMonth());
						if (endMonth.length()==1)
							endMonth = "0"+endMonth;
						String endDay = String.valueOf(filterEndDate.getDay());
						if (endDay.length()==1)
							endDay = "0"+endDay;
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
						
						session = null;	
						//dayInMonth = 1;
						if (dayInMonth == 1 && !GlobalConstants.SKIP_SAP_PP_INTERFACE)
						{
							//String reportPeriod = this.getPeriodId(reportDate.getYear(), reportDate.getMonth());
							//delete existing last period actuals from EcoSys
							//logger.debug("Purging last period actuals from EcoSys for Project"+ projectId);
							//this.deletePeriodActuals(projectId, reportPeriod);
							//logger.debug("Purging last period actuals from EcoSys completed for Project"+ projectId);
							if (isSub)
							{
								if (isSub)
								{
									if (!this.isProjectClosed(projectAPIType, projClosedList))
									{
										logger.info("Running Post Processing Action Batch (Monthly Closing) from EcoSys for Master Project with Sub: "+ masterProjectId);
										String projectPeriod = this.getPeriodId(currentDate.getYear(), currentDate.getMonth());

										this.postprocessLIs(masterProjectId, masterProjectId, projectPeriod, isTracking);
										logger.info("Running Post Processing Action Batch (Monthly Closing) from EcoSys completed for Master Project with Sub: "+ masterProjectId);
										projClosedList.add(projectAPIType);
									}
								}
								else
									logger.info("Skipped Running Post Processing Action Batch (Monthly Closing) from EcoSys for Standalone Master Project: "+ projectId);

							}
						}
						else if (!GlobalConstants.SKIP_POST_PROCESS) 
						{
							if (isSub)
							{
								if (!this.isProjectClosed(projectAPIType, projClosedList))
								{
									logger.info("Running Post Processing Action Batch (Monthly Closing) from EcoSys for Master Project with Sub: "+ masterProjectId);
									String projectPeriod = this.getPeriodId(currentDate.getYear(), currentDate.getMonth());

									this.postprocessLIs(masterProjectId, masterProjectId, projectPeriod, isTracking);
									logger.info("Running Post Processing Action Batch (Monthly Closing) from EcoSys completed for Master Project with Sub: "+ masterProjectId);
									projClosedList.add(projectAPIType);
								}
							}
							else
								logger.info("Skipped Running Post Processing Action Batch (Monthly Closing) from EcoSys for Standalone Master Project: "+ projectId);
						}
						else
						{
							if (isSub)
								logger.info("Skipped Running Post Processing Action Batch (Monthly Closing) from EcoSys for Master Project: " + masterProjectId + " with Sub: "+ projectId);
							else
								logger.info("Skipped Running Post Processing Action Batch (Monthly Closing) from EcoSys for Standalone Master Project: "+ projectId);
						}
					}
		 			catch(SystemException se) {
		 				logger.error("9305 -- Monthly Closing Failed for Master Project: " + GlobalConstants.EPC_PROJECT_ID_PROCESSED, se);
		 				retCode=GlobalConstants.IMPORT_SAP_MON_FAILED;
		 				continue;
		 			}
		 		}				
			} else {
				logger.info("Skipped Monthly Closing Interface. Change the skip property to 'false'");				
			}
			
		}catch(Exception e) {			
			logger.error("9305 -- Monthly Closing for Master Projects with SAP Sub Failed", e);			
			retCode=GlobalConstants.IMPORT_SAP_MON_FAILED;
		}
		
		if (retCode==GlobalConstants.IMPORT_SAP_MON_SUCCESS)
			logger.debug("9300 -- Monthly Closing for Master Projects with SAP Sub Completed Successfully");
		
		return retCode;
	}

    	
    //End Section Update Project on LastRuntime
		
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
			logger.debug("8000 -- Post Process Completed Successfully for Mastre Project: "+ masterProjectId);
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
	

	
	//Aug 2021: Phase 3 CR2 section
	
	private boolean isProjectClosed (BayerProjectAPIType apiType,List<BayerProjectAPIType> closedList )
	{
		boolean isClosed = false;
		if (closedList.size()>0)
		{
			for(BayerProjectAPIType proj: closedList)
			{
				if (apiType.getRootCostObjectID().equalsIgnoreCase(proj.getRootCostObjectID()) )
					isClosed = true;
			}
		}

		return isClosed;
	}

	//End Section Phase 3 CR02
}
