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
import com.bayer.integration.rest.proj4cplist.BayerProjectForCopyListAPIType;
import com.bayer.integration.rest.projcpcre.BayerProjectCopyCreateAPIResultType;
import com.bayer.integration.rest.projcpcre.PrjCpcreObjectFactory;
import com.ecosys.exception.SystemException;
import com.ecosys.util.Stopwatch;
import com.sun.jersey.api.client.ClientResponse;

/**
 * @author skdas
 *
 */
public class ProjectCopyCreateMgrImpl extends ImportManagerBase implements
		ImportManager {

	/* (non-Javadoc)
	 * @see com.bayer.integration.service.ImportManager#importData()
	 */
	//@Override

	
	public int importData() {
		int retCode=GlobalConstants.PROJECT_COPY_CREATE_SUCCESS;
		try{			
			if (!GlobalConstants.SKIP_ECO_PRJCP_CREAT_INTERFACE) {
				
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

				List<BayerProjectForCopyListAPIType> projectCopyListAPITypes = prjExpMgr.getBayerProjectForCopyListAPITypes();
				
		    	DatatypeFactory dFactory = DatatypeFactory.newInstance();
				XMLGregorianCalendar currentDate = dFactory.newXMLGregorianCalendar(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
							
				//Loop through the project list
				for (int i = 0; i < projectCopyListAPITypes.size(); i++) 
		 		{
					BayerProjectForCopyListAPIType projectAPIType = projectCopyListAPITypes.get(i);
					String projectId = projectAPIType.getID();
					GlobalConstants.EPC_PROJECT_ID_PROCESSED = projectId;
					//String sapProjectId = projectAPIType.getSapProjectId();					

					try
					{
						//soft delete existing records from EcoSys
						logger.debug("Create Project Copy in EcoSys for Project: "+ projectId);
						this.copyProject(projectId);
						logger.debug("Create Project Copy in EcoSys completed for Project: "+ projectId);
						
					}
		 			catch(SystemException se) {
		 				logger.error("9005 -- Create Project Copy Failed for Project: " + GlobalConstants.EPC_PROJECT_ID_PROCESSED, se);
		 				retCode=GlobalConstants.PROJECT_COPY_CREATE_FAILED;
		 				continue;
		 			}
		 		}
				
			} else {
				logger.info("Skipped Project Interface. Change the skip property to 'false'");				
			}		
		}
		catch(Exception e) {			
			logger.error("9005 -- Create Project Copy Failed", e);			
			retCode=GlobalConstants.PROJECT_COPY_CREATE_FAILED;
		}
		
		if (retCode==GlobalConstants.PROJECT_COPY_CREATE_SUCCESS)
			logger.debug("9000 -- Create Project Copies Completed Successfully");
		
		return retCode;
	}

	
		private List<String> copyProject( String projectId) throws SystemException{
			
			logger.debug("Create Copye for Project: " + projectId);
			
			List<String> retStatusMsgList = new ArrayList<String>();
			try {
				
				
				//Prepare for the REST call
				String baseUri = GlobalConstants.EPC_REST_PROTOCOL + "://" + GlobalConstants.EPC_REST_SERVER + ":" + 
						GlobalConstants.EPC_REST_PORT + "/" + GlobalConstants.EPC_REST_BASEURI2;
						

				Stopwatch timerBatch = new Stopwatch();			
				Cookie session = null;
				
				List<String> statusMsgList = new ArrayList<String>();
				timerBatch.start();
				session = this.requestCopy(projectId, session, baseUri);
				//logger.info(end + " of " + total + " - elapsed time: " + timerBatch.stop().toString(ISOPeriodFormat.alternateExtended()));
				retStatusMsgList.addAll(statusMsgList);

				
				//this.epcRestMgr.logout(client, baseUri, session);
				
			} catch(Exception e) {
				throw new SystemException(e);
			}
			logger.debug("Complete!");
			return retStatusMsgList;
		}
		
		private Cookie requestCopy(String projectId, Cookie session, 
				String baseUri) throws SystemException {

				BayerProjectCopyCreateAPIResultType request = new BayerProjectCopyCreateAPIResultType();
					//request.getBayerCommitmentLIAPI().addAll(subList);

				HashMap<String,String> filterMap = new HashMap<String, String>();
				filterMap.put(GlobalConstants.EPC_PROJECT_PARAM, projectId);
				filterMap.put(GlobalConstants.EPC_PROJECT_COPY_TYPE_PARAM, GlobalConstants.EPC_PROJECT_COPY_TYPE_SYSTEM);
				PrjCpcreObjectFactory objectFactory = new PrjCpcreObjectFactory();
				JAXBElement<BayerProjectCopyCreateAPIResultType> requestWrapper = objectFactory.createBayerProjectCopyCreateAPIResult(request);

				ClientResponse response = epcRestMgr
						.postApplicationXmlAsApplicationXml(client, requestWrapper,
				baseUri, GlobalConstants.EPC_REST_PROJECT_COPY_CREATE,
				session, filterMap);

				logger.debug(response);
				BayerProjectCopyCreateAPIResultType result = epcRestMgr.responseToObject(response, BayerProjectCopyCreateAPIResultType.class);

				if(session == null)
					session = epcRestMgr.getSessionCookie(response);

				if(!result.isSuccessFlag()){
					String errMsg="The interface failed to load any record due to data issues; please verify data.";
					if (result.getError() != null)
						errMsg=result.getError().toString();

					throw new SystemException(errMsg);
				} else {

					//List<StatusMessage> statusMsgList = new ArrayList<StatusMessage>();
					logger.debug("9000 -- Create Project Copy Completed Successfully for Project: "+ projectId);
					int i=0;
				}
				return session;
		}
		
}
