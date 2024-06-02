/**
 * 
 */
package com.bayer.integration.service;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.core.Cookie;
import javax.xml.bind.JAXBElement;

import org.joda.time.format.ISOPeriodFormat;

import com.bayer.integration.properties.GlobalConstants;
import com.bayer.integration.rest.project.BayerProjectAPIType;
import com.bayer.integration.rest.project.PRJObjectFactory;
import com.bayer.integration.rest.project.PRJObjectResultType;
import com.bayer.integration.rest.project.BayerProjectAPIRequestType;
import com.bayer.integration.rest.project.BayerProjectAPIResultType;

import com.bayer.integration.rest.proj4cplist.BayerProjectForCopyListAPIType;
import com.bayer.integration.rest.proj4cplist.BayerProjectForCopyListAPIResultType;

import com.bayer.integration.rest.projcpdrlist.BayerProjectCopyListForDRAPIType;
import com.bayer.integration.rest.projcpdrlist.BayerProjectCopyListForDRAPIResultType;
import com.ecosys.exception.SystemException;
import com.ecosys.util.Stopwatch;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.org.apache.xerces.internal.dom.ElementNSImpl;

import com.bayer.integration.utils.DebugBanner;

/**
 * @author pwang
 *
 */
public class ProjectExportManagerImpl extends ImportManagerBase implements
ExportManager {

	/* (non-Javadoc)
	 * @see com.bayer.integration.service.ExportManager#ExportData()
	 */
	//@Override
	protected List<BayerProjectAPIType> bayerProjectAPI;
	protected List<BayerProjectForCopyListAPIType> bayerProjectForCopyListAPI;
	protected List<BayerProjectCopyListForDRAPIType> bayerProjectCopyListForDRAPI;
	protected List<BayerProjectAPIType> bayerProjectAPIDebug = new ArrayList<BayerProjectAPIType>();

	// list of debug projects - commented out are duplicates and result in a 500 http status
	private String[] projects = {"A00GV-130342"};

/*			"A00NC-004285","A00GV-901453","A00GV-284199","A00NC-900207","A006Z-000157","A006Z-000226","A00BT-500502",
			"A00BT-500505","A00BT-900160","A00BT-900168","A00BT-900181","A00BT-900183","A00GV-000024","A00GV-000903",
			"A00GV-001893","A00GV-002418","A00GV-002724","A00GV-003096","A00GV-003218","A00GV-003366","A00GV-003495",
			"A00GV-004004","A00GV-100059","A00GV-100100","A00GV-100166","A00GV-100217","A00GV-100347","A00GV-123079",
			"A00GV-127238","A00GV-127975","A00GV-128058","A00GV-128470","A00GV-128620","A00GV-128656","A00GV-129248",
			"A00GV-129661","A00GV-129792","A00GV-129876","A00GV-130312","A00GV-130313","A00GV-130315","A00GV-130520",
			"A00GV-130786","A00GV-131225","A00GV-131320","A00GV-131341","A00GV-131474","A00GV-131477","A00GV-131626",
			"A00GV-131794","A00GV-151553","A00GV-281628","A00GV-283141","A00GV-284199","A00GV-284308","A00GV-284340",
			"A00GV-284387","A00GV-284403","A00GV-284408","A00GV-284409","A00GV-284413","A00GV-284491","A00GV-284527",
			"A00GV-284528","A00GV-284529","A00GV-284531","A00GV-284540","A00GV-284541","A00GV-284542","A00GV-284551",
			"A00GV-284552","A00GV-284572","A00GV-284574","A00GV-284633","A00GV-284685","A00GV-285555","A00GV-288003",
			"A00GV-288004","A00GV-288005","A00GV-301453","A00GV-327975","A00GV-328183","A00GV-328620","A00GV-328656",
			"A00GV-329248","A00GV-329661","A00GV-329792","A00GV-330342","A00GV-330520","A00GV-330786","A00GV-381628",
			"A00GV-382030","A00GV-384199","A00GV-384308","A00GV-384324","A00GV-507387","A00GV-901446","A00GV-901448",
			"A00GV-910006",

	"A00GV-910007"};*/



	//			/*"A00BT-500503","A00BT-500504",*/*"A00E4-000006",*//*"A00GV-002032",*//*"A00GV-130342",*/*"A00GV-130024",*//*"A00GV-131248",*//*"A00GV-330024",*//*"A00GV-400008",*/
	//			
	//			"A00J6-400171","A00J6-900163","A00J6-900165","A00J6-900168","A00NC-000846",
	//			"A00NC-001891","A00NC-002753","A00NC-002802","A00NC-002949","A00NC-003247","A00NC-003365","A00NC-003799",
	//			"A00NC-003818","A00NC-003824","A00NC-003827","A00NC-003828","A00NC-003829","A00NC-003932","A00NC-003933",
	//			"A00NC-003935","A00NC-003936","A00NC-003970","A00NC-003996","A00NC-003997","A00NC-004013","A00NC-004099",
	//			"A00NC-004104","A00NC-004236","A00NC-004285","A00NC-004441","A00NC-900207","A00UI-601892","A00US-901005",
	//			"A00US-901434","A00US-901632","A00gv-110520","A00gv-131734","A00gv-282646","AXXJS-900128"
	//			};

	public int ExportData() {
		int retCode=GlobalConstants.EXPORT_ECOSYS_PROJECT_SUCCESS;
		try{
			//Create Web Service Client
			if (client == null) setClient(epcRestMgr.createClient(GlobalConstants.EPC_REST_USERNAME, GlobalConstants.EPC_REST_PASSWORD));

			//Read Project Data from EcoSys using project API
			HashMap<String, String> prjMap = new HashMap<String, String>();
			if (GlobalConstants.EPC_PROJECT_ID!=null && !GlobalConstants.EPC_PROJECT_ID.equals(""))
				prjMap.put("ProjectId", GlobalConstants.EPC_PROJECT_ID);

			Cookie session = null;
			String baseUri2 = GlobalConstants.EPC_REST_PROTOCOL + "://" + GlobalConstants.EPC_REST_SERVER + ":"
					+ GlobalConstants.EPC_REST_PORT + "/" + GlobalConstants.EPC_REST_BASEURI2;				

			bayerProjectAPI = this.getProjectAPITypes(client, session, baseUri2, prjMap);

			/*
			 * // print all projects for list of debug projects // System.out.print("{");
			 * bayerProjectAPI.forEach(s ->
			 * System.out.print('"' + s.getSapProjectId() + '"' + ",")); //
			 * System.out.print("};");
			 */	

			if (GlobalConstants.DEBUGMODE) {
				DebugBanner.outputBanner(this.getClass().toString());
				bayerProjectAPIDebug.clear(); // create a 2nd project list for debug, clears for subsequent interface runs
				System.out.print("Debug projects");
				for (String project : projects ) {
					for (int i =0 ; i<bayerProjectAPI.size() ; i++) {
						if (project.equalsIgnoreCase(bayerProjectAPI.get(i).getSapProjectId())) {
							bayerProjectAPIDebug.add(bayerProjectAPI.get(i));
							System.out.print(" : " + project);}
					}
				}
				System.out.println();
			}
			/*
			//The following is to retrieve list of project for project copy
			if (GlobalConstants.EPC_PROJECT_ID!=null && !GlobalConstants.EPC_PROJECT_ID.equals(""))
				prjMap.put("ProjectId", GlobalConstants.EPC_PROJECT_ID);

			baseUri2 = GlobalConstants.EPC_REST_PROTOCOL + "://" + GlobalConstants.EPC_REST_SERVER + ":"
					+ GlobalConstants.EPC_REST_PORT + "/" + GlobalConstants.EPC_REST_BASEURI2;				

			bayerProjectForCopyListAPI = this.getProjectForCopyListAPITypes(client, session, baseUri2, prjMap);

			//The following is to retrieve list of project copy for delete or restore
			if (GlobalConstants.EPC_PROJECT_ID!=null && !GlobalConstants.EPC_PROJECT_ID.equals(""))
				prjMap.put("ProjectId", GlobalConstants.EPC_PROJECT_ID);

			baseUri2 = GlobalConstants.EPC_REST_PROTOCOL + "://" + GlobalConstants.EPC_REST_SERVER + ":"
					+ GlobalConstants.EPC_REST_PORT + "/" + GlobalConstants.EPC_REST_BASEURI2;				

			bayerProjectCopyListForDRAPI = this.getProjectCopyListForDRAPITypes(client, session, baseUri2, prjMap);
			 */

		}
		catch(SystemException se) {			
			logger.error("105 -- Project Export Failed", se);			
			retCode=GlobalConstants.EXPORT_ECOSYS_PROJECT_FAILED;
		}catch(Exception e) {			
			logger.error("105 -- Project Export Failed", e);			
			retCode=GlobalConstants.EXPORT_ECOSYS_PROJECT_FAILED;
		}

		if (retCode==GlobalConstants.EXPORT_ECOSYS_PROJECT_SUCCESS)
			logger.debug("100 -- Project Export Completed Successfully");

		return retCode;
	}

	private List<BayerProjectAPIType> getProjectAPITypes(Client client, Cookie session, String baseUri, HashMap prjMap) throws SystemException {

		ClientResponse response = null;
		if (prjMap.size() == 0)		
			response= epcRestMgr
			.getAsApplicationXml(client, baseUri, 
					GlobalConstants.EPC_REST_READ_ECOSYS_PROJECTS,session);

		else
			response = epcRestMgr
			.getAsApplicationXml(client, baseUri, 
					GlobalConstants.EPC_REST_READ_ECOSYS_PROJECTS,session, prjMap);

		BayerProjectAPIResultType result = new BayerProjectAPIResultType();
		result = epcRestMgr.responseToObject(response, BayerProjectAPIResultType.class);
		List<BayerProjectAPIType> projectAPITypes = result.getBayerProjectAPI();
		if(session == null)
			session = epcRestMgr.getSessionCookie(response);

		if(!result.isSuccessFlag()){
			String errMsg="The interface failed to retrieve project list from EcoSys API; please verify connection.";
			if (result.getError() != null)
				errMsg=result.getError().toString();

			throw new SystemException(errMsg);
		} 
		return projectAPITypes;
	}

	private List<BayerProjectForCopyListAPIType> getProjectForCopyListAPITypes(Client client, Cookie session, String baseUri, HashMap prjMap) throws SystemException {

		ClientResponse response = null;
		if (prjMap.size() == 0)		
			response= epcRestMgr
			.getAsApplicationXml(client, baseUri, 
					GlobalConstants.EPC_REST_PROJECT_FOR_COPY_LIST,session);

		else
			response = epcRestMgr
			.getAsApplicationXml(client, baseUri, 
					GlobalConstants.EPC_REST_PROJECT_FOR_COPY_LIST,session, prjMap);

		BayerProjectForCopyListAPIResultType result = new BayerProjectForCopyListAPIResultType();
		result = epcRestMgr.responseToObject(response, BayerProjectForCopyListAPIResultType.class);
		List<BayerProjectForCopyListAPIType> projectAPITypes = result.getBayerProjectForCopyListAPI();
		if(session == null)
			session = epcRestMgr.getSessionCookie(response);

		if(!result.isSuccessFlag()){
			String errMsg="The interface failed to retrieve project list from EcoSys API; please verify connection.";
			if (result.getError() != null)
				errMsg=result.getError().toString();

			throw new SystemException(errMsg);
		} 
		return projectAPITypes;
	}

	private List<BayerProjectCopyListForDRAPIType> getProjectCopyListForDRAPITypes(Client client, Cookie session, String baseUri, HashMap prjMap) throws SystemException {

		ClientResponse response = null;
		if (prjMap.size() == 0)		
			response= epcRestMgr
			.getAsApplicationXml(client, baseUri, 
					GlobalConstants.EPC_REST_PROJECT_COPY_FOR_DR_LIST,session);

		else
			response = epcRestMgr
			.getAsApplicationXml(client, baseUri, 
					GlobalConstants.EPC_REST_PROJECT_COPY_FOR_DR_LIST,session, prjMap);

		BayerProjectCopyListForDRAPIResultType result = new BayerProjectCopyListForDRAPIResultType();
		result = epcRestMgr.responseToObject(response, BayerProjectCopyListForDRAPIResultType.class);
		List<BayerProjectCopyListForDRAPIType> projectAPITypes = result.getBayerProjectCopyListForDRAPI();
		if(session == null)
			session = epcRestMgr.getSessionCookie(response);

		if(!result.isSuccessFlag()){
			String errMsg="The interface failed to retrieve project list from EcoSys API; please verify connection.";
			if (result.getError() != null)
				errMsg=result.getError().toString();

			throw new SystemException(errMsg);
		} 
		return projectAPITypes;
	}

	public List<BayerProjectAPIType> getBayerProjectAPITypes() {

		if (GlobalConstants.DEBUGMODE) {
			DebugBanner.outputBanner("Only debug projects will be exported");
			return this.bayerProjectAPIDebug;
		}
		else return this.bayerProjectAPI;
	}

	public List<BayerProjectForCopyListAPIType> getBayerProjectForCopyListAPITypes() {
		return this.bayerProjectForCopyListAPI;
	}

	public List<BayerProjectCopyListForDRAPIType> getBayerProjectCopyListForDRAPITypes() {
		return this.bayerProjectCopyListForDRAPI;
	}

	public int updateProjectLastTimeRunDate(BayerProjectAPIType apiType) {
		int retCode=0;
		try{		
			//Create Web Service Client
			if (client == null) 
				setClient(epcRestMgr.createClient(GlobalConstants.EPC_REST_USERNAME, GlobalConstants.EPC_REST_PASSWORD));

			//Read Project Data from EcoSys using project API
			Cookie session = null;
			//ImportManagerHelper importHelper = new ImportManagerHelper();

			List<BayerProjectAPIType> projectAPITypes = new ArrayList<BayerProjectAPIType>();
			projectAPITypes.add(apiType);
			retCode = updateProject(projectAPITypes, apiType.getID());			

		}
		catch(SystemException se) {			
			logger.error("106 -- Last Time Run Parameter Update Failed", se);			
			retCode=GlobalConstants.IMPORT_SAP_ACT_FAILED;
		}catch(Exception e) {			
			logger.error("106 -- Last Time Run Parameter Update Failed", e);			
			retCode=GlobalConstants.IMPORT_SAP_ACT_FAILED;
		}

		if (retCode==GlobalConstants.IMPORT_SAP_ACT_SUCCESS)
			logger.debug("101 -- Last Time Run Parameter Update Successfully");

		return retCode;
	}


	private int updateProject( List<BayerProjectAPIType> prjRecords, String projectId) throws SystemException{

		logger.debug("Update Project Last Time Run field to EPC for Project: " + projectId + "...");

		//List<PmOrderApiErrorDAO> retStatusMsgList = new ArrayList<PmOrderApiErrorDAO>();
		try {

			if(prjRecords == null || prjRecords.size() == 0) {
				return 0;
			}

			//Prepare for the REST call
			String baseUri = GlobalConstants.EPC_REST_PROTOCOL + "://" + GlobalConstants.EPC_REST_SERVER + ":" + 
					GlobalConstants.EPC_REST_PORT + "/" + GlobalConstants.EPC_REST_BASEURI2;

			long total = prjRecords.size();
			Stopwatch timerBatch = new Stopwatch();
			Cookie session = null;
			for(int i = 0; i < prjRecords.size(); i += GlobalConstants.EPC_REST_BATCHSIZE) {
				int end = (prjRecords.size() < (i + GlobalConstants.EPC_REST_BATCHSIZE) ? prjRecords.size() : GlobalConstants.EPC_REST_BATCHSIZE + i);
				//List<PmOrderApiErrorDAO> statusMsgList = new ArrayList<PmOrderApiErrorDAO>();
				timerBatch.start();
				session = this.request(prjRecords.subList(i, end), session, baseUri, projectId);
				logger.info(end + " of " + total + " - elapsed time: " + timerBatch.stop().toString(ISOPeriodFormat.alternateExtended()));
				//retStatusMsgList.addAll(statusMsgList);
			}

			//this.epcRestMgr.logout(client, baseUri, session);

		} catch(Exception e) {
			throw new SystemException(e);
		}
		logger.debug("Complete!");
		return 1;
	}

	private Cookie request(List<BayerProjectAPIType> subList,
			Cookie session, String baseUri, String projectId) throws SystemException {

		BayerProjectAPIRequestType request = new BayerProjectAPIRequestType();
		request.getBayerProjectAPI().addAll(subList);

		PRJObjectFactory objectFactory = new PRJObjectFactory();
		JAXBElement<BayerProjectAPIRequestType> requestWrapper = objectFactory.createBayerProjectAPIRequest(request);

		ClientResponse response = epcRestMgr
				.postApplicationXmlAsApplicationXml(client, requestWrapper,
						baseUri, GlobalConstants.EPC_REST_READ_ECOSYS_PROJECTS,
						session);

		logger.debug(response);
		BayerProjectAPIResultType result = epcRestMgr.responseToObject(response, BayerProjectAPIResultType.class);
		if(session == null)
			session = epcRestMgr.getSessionCookie(response);

		if(!result.isSuccessFlag()){
			String errMsg="The interface failed to load any record due to data issues; please verify data.";
			if (result.getError() != null)
				errMsg=result.getError().toString();
			for(BayerProjectAPIType apiType: subList)
			{
				//convert to APIErrorDAO type
				/*
					PmOrderApiErrorDAO statusMsg = this.getPMOrderAPIErrorDAO(pmorder);
					statusMsg.setRootCostObjectID(projectId);
					statusMsg.setStatus(GlobalConstants.STAGING_RECORD_STATUS_FAILED);
					statusMsg.setErrorMsg(errMsg);
					errorList.add(statusMsg);*/
			}
			throw new SystemException(errMsg);
		} else {

			//List<PMOrderAPIErrorDAO> statusMsgList = new ArrayList<PMOrderAPIErrorDAO>();
			int i=0;
			for(PRJObjectResultType or : result.getObjectResult()) {
				BayerProjectAPIType apiType = subList.get(i++);
				String cId = apiType.getID();

				//convert to APIErrorDAO type
				//PmOrderApiErrorDAO statusMsg = this.getPMOrderAPIErrorDAO(pmorder);
				//statusMsg.setRootCostObjectID(projectId);

				if(or.isSuccessFlag()) {
					logger.debug("UPDATE RESULT --> " + or.getInternalId());
					logger.debug("Record with External Key ID of : " + cId +" "+ GlobalConstants.STAGING_RECORD_STATUS_LOADED);

					//statusMsg.setStatus(GlobalConstants.STAGING_RECORD_STATUS_LOADED);
				} else {
					//String str = ((ElementNSImpl)or.getResultMessage().get(0).getMessage()).getTextContent();
					String str = or.getResultMessage().get(0).getMessage();
					logger.error("ERROR --> " + or.getInternalId() + "|" + cId + "|" + or.isSuccessFlag() + "|" + str);	

					//statusMsg.setStatus(GlobalConstants.STAGING_RECORD_STATUS_FAILED);
					//statusMsg.setErrorMsg(str);
				}
				//statusMsgList.add(statusMsg);
				//errorList.add(statusMsg);			
			}
		}
		return session;
	}

}
