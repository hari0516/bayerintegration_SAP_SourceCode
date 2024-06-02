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
import com.bayer.integration.rest.projcpdel.BayerProjectCopyDeleteAPIResultType;
import com.bayer.integration.rest.projcpdel.PrjCpdelObjectFactory;
import com.bayer.integration.rest.projcpdrlist.BayerProjectCopyListForDRAPIType;
import com.ecosys.exception.SystemException;
import com.ecosys.util.Stopwatch;
import com.sun.jersey.api.client.ClientResponse;

/**
 * @author skdas
 *
 */
public class ProjectCopyDeleteMgrImpl extends ImportManagerBase implements
		ImportManager {

	/* (non-Javadoc)
	 * @see com.bayer.integration.service.ImportManager#importData()
	 */
	//@Override

	
	public int importData() {
		int retCode=GlobalConstants.PROJECT_COPY_DELETE_SUCCESS;
		try{			
			if (!GlobalConstants.SKIP_ECO_PRJCP_DELETE_INTERFACE) {
				
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
				List<BayerProjectCopyListForDRAPIType> projectCopyListForDRAPITypes = prjExpMgr.getBayerProjectCopyListForDRAPITypes();
				
				Integer sCopyLimit = Integer.valueOf(GlobalConstants.EPC_REST_PROJECT_COPY_SYSTEM_LIMIT);
				Integer uCopyLimit = Integer.valueOf(GlobalConstants.EPC_REST_PROJECT_COPY_USER_LIMIT);
				
				DatatypeFactory dFactory = DatatypeFactory.newInstance();
		    	XMLGregorianCalendar currentDate = dFactory.newXMLGregorianCalendar(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
							

				//Loop through the project list
				for (int i = 0; i < projectCopyListAPITypes.size(); i++) 
		 		{
					BayerProjectForCopyListAPIType projectAPIType = projectCopyListAPITypes.get(i);
					String projectId = projectAPIType.getID();
					
					int intCounterS = 0;
					int intCounterU = 0;
					String catgProjectId = "";
					
					List<BayerProjectCopyListForDRAPIType> projectCopyUList = new ArrayList<BayerProjectCopyListForDRAPIType>();
					List<BayerProjectCopyListForDRAPIType> projectCopySList = new ArrayList<BayerProjectCopyListForDRAPIType>();
		
					if (projectAPIType.getCatgProjectID()!=null);
						catgProjectId = projectAPIType.getCatgProjectID();
						
					//Loop through the project list
					if (!(projectCopyListForDRAPITypes.size() > 0))
						continue;
					
					for (int j = 0; j < projectCopyListForDRAPITypes.size(); j++) 
			 		{
						BayerProjectCopyListForDRAPIType projectCpAPIType = projectCopyListForDRAPITypes.get(j);
						String projectCpId = projectCpAPIType.getID();

						GlobalConstants.EPC_PROJECT_ID_PROCESSED = projectCpId;
						
						String projectCpType = projectCpAPIType.getProjectCopyTypeID();
						
						String catgProjectCpId = "";
						if (projectCpAPIType.getCatgProjectID()!=null);
							catgProjectCpId = projectCpAPIType.getCatgProjectID();
							
						
						if (projectCpAPIType.getProjectCopyStatusID()==GlobalConstants.EPC_PROJECT_COPY_STATUS_DELETE)
						{
							try
							{
								//soft delete existing records from EcoSys
								logger.debug("Delete Project Copy in EcoSys for Project Copy: "+ projectCpId);
								this.deleteProject(projectId);
								logger.debug("Delete Project Copy in EcoSys completed for Project Copy: "+ projectCpId);
								
							}
				 			catch(SystemException se) {
				 				logger.error("9105 -- Delete Project Copy Failed for Project Copy: " + GlobalConstants.EPC_PROJECT_ID_PROCESSED, se);
				 				retCode=GlobalConstants.PROJECT_COPY_DELETE_FAILED;
				 				continue;
				 			}
						}
						
						//add System Copy to System Copy list
						else if (projectCpType.equalsIgnoreCase(GlobalConstants.EPC_PROJECT_COPY_TYPE_SYSTEM))
						{

							if (catgProjectId!=null && catgProjectId.equalsIgnoreCase(catgProjectCpId))
							{
								projectCopySList.add(projectCpAPIType);
							}
						}
						
						//add Use Copy to User Copy List
						else if (projectCpType.equalsIgnoreCase(GlobalConstants.EPC_PROJECT_COPY_TYPE_USER))
						{		
							if (catgProjectId!=null && catgProjectId.equalsIgnoreCase(catgProjectCpId))
							{
								projectCopyUList.add(projectCpAPIType);
							}
						}
						
			 		}
					
					//Process Project System Copy List
					if (projectCopySList.size()>sCopyLimit)
					{
						projectCopySList = this.updateDeleteList(projectCopySList, sCopyLimit,currentDate);
						for (int k = 0; k < projectCopySList.size(); k++) 
						{	
							BayerProjectCopyListForDRAPIType projCopy = projectCopySList.get(k);
							if (projCopy.getProjectCopyStatusID().equalsIgnoreCase(GlobalConstants.EPC_PROJECT_COPY_STATUS_DELETE))
							{
								String projectCpId = projCopy.getID(); 
								try
								{
									//soft delete existing records from EcoSys
									logger.debug("Delete Project System Copy in EcoSys for Project Copy: "+ projectCpId);
									this.deleteProject(projectCpId);
									logger.debug("Delete Project System Copy in EcoSys completed for Project Copy: "+ projectCpId);
									
								}
					 			catch(SystemException se) {
					 				logger.error("9105 -- Delete Project System Copy Failed for Project Copy: " + GlobalConstants.EPC_PROJECT_ID_PROCESSED, se);
					 				retCode=GlobalConstants.PROJECT_COPY_DELETE_FAILED;
					 				continue;
					 			}
							}
						}
					}
					
					//Process Project User Copy List
					if (projectCopyUList.size()>uCopyLimit)
					{
						projectCopyUList = this.updateDeleteList(projectCopyUList, uCopyLimit,currentDate);
						for (int k = 0; k < projectCopyUList.size(); k++) 
						{	
							BayerProjectCopyListForDRAPIType projCopy = projectCopyUList.get(k);
							if (projCopy.getProjectCopyStatusID().equalsIgnoreCase(GlobalConstants.EPC_PROJECT_COPY_STATUS_DELETE))
							{
								String projectCpId = projCopy.getID(); 
								try
								{
									//soft delete existing records from EcoSys
									logger.debug("Delete Project User Copy in EcoSys for Project Copy: "+ projectCpId);
									this.deleteProject(projectCpId);
									logger.debug("Delete Project User Copy in EcoSys completed for Project Copy: "+ projectCpId);
									
								}
					 			catch(SystemException se) {
					 				logger.error("9105 -- Delete Project User Copy Failed for Project Copy: " + GlobalConstants.EPC_PROJECT_ID_PROCESSED, se);
					 				retCode=GlobalConstants.PROJECT_COPY_DELETE_FAILED;
					 				continue;
					 			}
							}
						}
					}
		 		}
				
			} else {
				logger.info("Skipped Project Interface. Change the skip property to 'false'");				
			}		
		}
		catch(Exception e) {			
			logger.error("9105 -- Delete Project Copy Failed", e);			
			retCode=GlobalConstants.PROJECT_COPY_DELETE_FAILED;
		}
		
		if (retCode==GlobalConstants.PROJECT_COPY_DELETE_SUCCESS)
			logger.debug("9100 -- Delete Project Copies Completed Successfully");
		
		return retCode;
	}

		private List<String> deleteProject( String projectId) throws SystemException{
			
			logger.debug("Delete Project Copy: " + projectId);
			
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
		
		private Cookie requestDelete(String projectId, Cookie session, 
				String baseUri) throws SystemException {

				BayerProjectCopyDeleteAPIResultType request = new BayerProjectCopyDeleteAPIResultType();
					//request.getBayerCommitmentLIAPI().addAll(subList);

				HashMap<String,String> filterMap = new HashMap<String, String>();
				filterMap.put(GlobalConstants.EPC_PROJECT_PARAM, projectId);

				PrjCpdelObjectFactory objectFactory = new PrjCpdelObjectFactory();
				JAXBElement<BayerProjectCopyDeleteAPIResultType> requestWrapper = objectFactory.createBayerProjectCopyDeleteAPIResult(request);

				ClientResponse response = epcRestMgr
						.postApplicationXmlAsApplicationXml(client, requestWrapper,
				baseUri, GlobalConstants.EPC_REST_PROJECT_COPY_DELETE,
				session, filterMap);

				logger.debug(response);
				BayerProjectCopyDeleteAPIResultType result = epcRestMgr.responseToObject(response, BayerProjectCopyDeleteAPIResultType.class);

				if(session == null)
					session = epcRestMgr.getSessionCookie(response);

				if(!result.isSuccessFlag()){
					String errMsg="The interface failed to load any record due to data issues; please verify data.";
					if (result.getError() != null)
						errMsg=result.getError().toString();

					throw new SystemException(errMsg);
				} else {

					//List<StatusMessage> statusMsgList = new ArrayList<StatusMessage>();
					logger.debug("9100 -- Delete Project Copy Completed Successfully for Project Copy: "+ projectId);
					int i=0;
				}
				return session;
		}
		
		
		private  List<BayerProjectCopyListForDRAPIType> updateDeleteList(List<BayerProjectCopyListForDRAPIType> currentList, Integer intLimit, XMLGregorianCalendar currentDate){
			
			List<BayerProjectCopyListForDRAPIType> newList = new ArrayList<BayerProjectCopyListForDRAPIType>();
			List<BayerProjectCopyListForDRAPIType> copyList = currentList;
			for(BayerProjectCopyListForDRAPIType aCopy: currentList)
			{
				int counter = 0;
				for (int i = 0; i < copyList.size(); i++) 
				{	
					BayerProjectCopyListForDRAPIType projCopy = copyList.get(i);
					if (aCopy.getID().equalsIgnoreCase(projCopy.getID()))
						continue;
					
					//else if (aCopy.getCreateDate().compare(projCopy.getCreateDate()) == DatatypeConstants.GREATER )
					else if (Integer.valueOf(aCopy.getInternalID()) < Integer.valueOf(projCopy.getInternalID()))
					{
						counter = counter + 1;
					}
				}
				if (counter > intLimit)	
					aCopy.setProjectCopyStatusID(GlobalConstants.EPC_PROJECT_COPY_STATUS_DELETE);
				
				newList.add(aCopy);
			}
			return newList;
		}
}
