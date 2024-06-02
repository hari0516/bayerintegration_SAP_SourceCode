/**
 * 
 */
package com.bayer.integration.sapservice;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.ep.feed.ODataFeed;
import org.apache.log4j.Logger;

import com.bayer.integration.odata.SapPMOODataType;
import com.bayer.integration.properties.GlobalConstants;
import com.bayer.integration.rest.wbs.BayerWBSAPIType;
import com.bayer.integration.rest.pmorder.BayerPMOrderAPIType;
import com.bayer.integration.rest.pmorder2.BayerPMOrderV2APIType;
import com.bayer.integration.rest.actual.BayerActualsAPIType;
import com.bayer.integration.rest.poprh.BayerPOPRHeadersAPIType;
import com.bayer.integration.rest.poprh2.BayerPOPRHeadersV2APIType;
import com.bayer.integration.rest.pol.BayerCommitmentLIAPIType;
import com.bayer.integration.rest.polco.BayerCommitmentLICOAPIType;
import com.bayer.integration.rest.polco2.BayerCommitmentLICOV2APIType;
import com.ecosys.exception.SystemException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
/**
 * @author skdas
 *
 */


public class SAPGatewayODataServiceMgrImpl implements SAPGatewayODataServiceManager {
	protected static Logger logger = Logger.getLogger(SAPGatewayODataServiceMgrImpl.class);

	//@Override
	public Map<String, Map<String, Object>> readWBSHierarchy(String projectID, String instanceID)
			throws SystemException {
		
		//return readODataServiceEndPoint(/*"Products"*/"WBS_ElementSet", projectID, instanceID);		
		return readODataServiceEndPoint(GlobalConstants.ODATA_WBS_ELEMENT_SET, projectID, instanceID);		
	}

	//@Override
	public  Map<String, Map<String, Object>> readPMOrders(String projectID, String instanceID)
			throws SystemException {
		
		return readODataServiceEndPoint(GlobalConstants.ODATA_PM_ORDER_SET, projectID, instanceID);
	}

	//@Override
	public  Map<String, Map<String, Object>> readActuals(String projectID, String instanceID, String startDate, String endDate)
			throws SystemException {
		
		return readODataServiceEndPointDateRange(GlobalConstants.ODATA_PROJECT_ACTUALS_SET, projectID, instanceID, startDate, endDate);
	}
	
	//@Override
	public  Map<String, Map<String, Object>> readPOHeader(String projectID, String instanceID)
			throws SystemException {	
		return readODataServiceEndPoint(GlobalConstants.ODATA_PO_HEADER_SET, projectID, instanceID);
	}

	//@Override
	public  Map<String, Map<String, Object>> readPRHeader(String projectID, String instanceID)
			throws SystemException {
		return readODataServiceEndPoint(GlobalConstants.ODATA_PR_HEADER_SET, projectID, instanceID);
	}
	
	//@Override
	public  Map<String, Map<String, Object>> readPOLineItems(String projectID, String instanceID)
			throws SystemException {
		return readODataServiceEndPoint(GlobalConstants.ODATA_PO_LINE_ITEM_SET, projectID, instanceID);
	}

	//@Override
	public  Map<String, Map<String, Object>> readPRLineItems(String projectID, String instanceID)
			throws SystemException {
		return readODataServiceEndPoint(GlobalConstants.ODATA_PR_LINE_ITEM_SET, projectID, instanceID);
	}
	

	private  Map<String, Map<String, Object>> readODataServiceEndPoint(
			String serviceEndPoint, String projectID, String instanceID) throws SystemException {
		return readODataServiceEndPoint(serviceEndPoint, projectID, instanceID, null, null);
	}


	private  Map<String, Map<String, Object>> readODataServiceEndPointDateRange(
			String serviceEndPoint, String projectID, String instanceID, String startDate, String endDate) throws SystemException {
		return readODataServiceEndPoint(serviceEndPoint, projectID, instanceID, startDate, endDate);
	}
	
    private  Map<String, Map<String, Object>> readODataServiceEndPoint(
            String serviceEndPoint, String projectID, String instanceID, String startDate, String endDate) throws SystemException {
        logger.debug("Reading oDATA Service Endpoint: " + serviceEndPoint);
        
        Map<String, Map<String, Object>> retResultList = null;
        
        try{                          
        	String filters = prepareServiceFilter(serviceEndPoint, projectID, instanceID, startDate, endDate);
        		
        	Client oClient = new Client(GlobalConstants.ODATA_SERVICE_ROOT,GlobalConstants.ODATA_SERVICE_USER, 
                    GlobalConstants.ODATA_SERVICE_PWD );
            
        	//List<EdmEntitySetInfo> edmEntitySetInfo = oClient.getEntitySets();
            //Edm edm2 = oClient.getEdm();
            //ODataFeed oFeed = oClient.readFeed(GlobalConstants.ODATA_SERVICE_ENTITY_CONTAINER,GlobalConstants.ODATA_WBS_ELEMENT_SET
            //			,filters, GlobalConstants.APPLICATION_XML);
            
            ODataFeed oFeed = oClient.readFeed(GlobalConstants.ODATA_SERVICE_ENTITY_CONTAINER,serviceEndPoint
        			,filters, GlobalConstants.APPLICATION_XML);

            if (oFeed.getEntries().size() > 0)
            {	
                logger.debug("Retrieved oDATA Service total rows " + oFeed.getEntries().size());
            	retResultList = new HashMap<String, Map<String, Object>> ();
	            Integer counter = 0;
                for (ODataEntry entry : oFeed.getEntries()) 
                {
                	counter = counter +1;
                	Map<String, Object> dataRow =  entry.getProperties();
                	String key = counter.toString();
                	retResultList.put(key, dataRow);
                	if (counter == 50 || counter == 100 || counter == 150 || counter == 200 || counter == 250)
                	{
                        //logger.debug("Reading oDATA Service Endpoint Pause row " + counter + ": " + entry.toString());
                	}
                    logger.debug("Reading oDATA Service Endpoint row " + counter + ": " + entry.toString());
                    //if (counter == 50)
                      //  logger.debug("Reading oDATA Service Endpoint row " + counter + ": " + entry.toString());
                }
            }

        }catch (Exception e) {
               logger.debug("Exception condition occurred: "+ e.getMessage());
               throw new SystemException(e);
        }
        
        return retResultList;
    	
    }
	
    private String prepareServiceFilter(String serviceEndPoint, String projectID, String instanceID, String startDate, String endDate) {
           StringBuffer filterStr = new StringBuffer("");
           
           if (projectID != null)
                  filterStr.append("ProjectDefinition%20eq%20'"+projectID+"'");
           
           if (instanceID != null) {
                  if (filterStr.length() == 0) 
                        filterStr.append("SystemId eq '"+instanceID+"'");
                  else
                        filterStr.append("%20and%20SystemId%20eq%20'"+instanceID+"'");
           }
           
           if (startDate != null) {
                  if (filterStr.length() == 0) 
                        filterStr.append("StartDate%20eq%20 '"+startDate+"'");
                  else
                        filterStr.append("%20and%20StartDate%20eq%20'"+startDate+"'");
           }
           
           if (endDate != null) {
                  if (filterStr.length() == 0) 
                        filterStr.append("EndDate%20eq%20'"+endDate+"'");
                  else
                        filterStr.append("%20and%20EndDate%20eq%20'"+endDate+"'");
           }
           
           return filterStr.toString();
    }

	public Map<String, BayerWBSAPIType> mapWBSHierarchyForImport(Map<String, Map<String, Object>> dataRows, boolean isSub, String masterProjectId) 
			throws SystemException {
		Map<String, BayerWBSAPIType> returnMap = new HashMap<String, BayerWBSAPIType>();
		Map<String, BayerWBSAPIType> rawMap = new HashMap<String, BayerWBSAPIType>();
		Set<String> rowKeys = dataRows.keySet();
		String projectId = "";
		for (String rowKey : rowKeys){
			//logger.debug("Row Key is: " + rowKey);

			BayerWBSAPIType ecoWBS = new BayerWBSAPIType();

			Map<String, Object> rowVal = dataRows.get(rowKey);			
			Set<String> fieldNames = rowVal.keySet();
			for (String fieldName : fieldNames) {
				if (rowVal.get(fieldName)!=null)
				{
					String fieldVal = rowVal.get(fieldName).toString().trim();
					//logger.debug("Field Name: "+ fieldName);
					//logger.debug("Field Value: "+ fieldVal);
					if (fieldName != null && fieldName.equalsIgnoreCase("ProjectDefinition"))
						projectId = fieldVal;
					
					if (fieldName != null && fieldName.equalsIgnoreCase("WbsElement"))
						ecoWBS.setExternalKey(fieldVal);
					else if (fieldName != null && fieldName.equalsIgnoreCase("WbsId"))
						ecoWBS.setCostObjectID(fieldVal);
					else if (fieldName != null && fieldName.equalsIgnoreCase("WbsLevel"))
					{
						if (fieldVal!=null && !fieldVal.equals("") )
							ecoWBS.setCostObjectHierarchyLevel(Integer.parseInt(fieldVal));
						else
							ecoWBS.setCostObjectHierarchyLevel(-1);
					}
					else if (fieldName != null && fieldName.equalsIgnoreCase("ParentWbs"))
						ecoWBS.setParentCostObjectHierarchyPathID(fieldVal);
					else if (fieldName != null && fieldName.equalsIgnoreCase("Description"))
						ecoWBS.setCostObjectName(fieldVal);
					else if (fieldName != null && fieldName.equalsIgnoreCase("ResponsibleNo"))
						ecoWBS.setPersonResponsible(fieldVal);
					else if (fieldName != null && fieldName.equalsIgnoreCase("RProfitCtr"))
						ecoWBS.setProfitCenter(fieldVal);
					else if (fieldName != null && fieldName.equalsIgnoreCase("ProjType"))
						ecoWBS.setSAPProjectTypeID(fieldVal);
					else if (fieldName != null && fieldName.equalsIgnoreCase("Objectclass"))
						ecoWBS.setObjectClass(fieldVal);
					else if (fieldName != null && fieldName.equalsIgnoreCase("ODeletionFlag"))
						ecoWBS.setSAPDeleteFlagID(fieldVal);
					else if (fieldName != null && fieldName.equalsIgnoreCase("RespsblCctr"))
						ecoWBS.setResponsibleCostCenter(fieldVal);
					else if (fieldName != null && fieldName.equalsIgnoreCase("Location"))
						ecoWBS.setLocationID(fieldVal);
					else if (fieldName != null && fieldName.equalsIgnoreCase("Status"))
						ecoWBS.setSAPStatus(fieldVal);
				}
			}
			if (ecoWBS.getExternalKey() != null && !ecoWBS.getExternalKey().equalsIgnoreCase(projectId))
			{
			//	if (ecoWBS.getCostObjectHierarchyLevel()>3)
				//	ecoWBS.setCostObjectID(calculateID2(ecoWBS));
				String newId = ecoWBS.getExternalKey().substring(projectId.length()+1);
				ecoWBS.setCostObjectID(newId);
				returnMap.put(ecoWBS.getExternalKey(), ecoWBS);	
				//rawMap.put(ecoWBS.getExternalKey(), ecoWBS);	
				logger.debug("WBS with ExternalKey: "+ ecoWBS.getExternalKey() + "; Parent WBS="+ ecoWBS.getParentCostObjectHierarchyPathID());
				//logger.debug("Field Value: "+ fieldVal);
				if (ecoWBS.getExternalKey().equalsIgnoreCase(projectId+"-CGA5"))
				{
					logger.debug("Data with Key="+ " " + rowKey + " without WbsElement");
				}
			}
			else
			{
				logger.debug("Data with Key="+ " " + rowKey + " without WbsElement");
			}
			if (ecoWBS.getCostObjectHierarchyLevel()==-1) {
				int newLevel = calculateHierarchyLevel(ecoWBS, projectId);
				ecoWBS.setCostObjectHierarchyLevel(newLevel);
			}
		}
		//returnMap= calculateID( rawMap, projectId);
		if (!isSub)
			calculateHierarchyPath( returnMap, projectId);
		else
			calculateHierarchyPath( returnMap, projectId);
			//calculateHierarchyPathSub(returnMap, masterProjectId);
		
		BayerWBSAPIType missingWBS = returnMap.get(projectId+"-CGA5");
		return returnMap;
	}
	private String calculateID4(BayerWBSAPIType ecoWBS)
	{
		String cId = "";
		String pId = "";
		
		String key = ecoWBS.getExternalKey();
		int lastPos = key.lastIndexOf("-");
		int parentLen = ecoWBS.getParentCostObjectHierarchyPathID().length();
		
		 if (lastPos>0)	
		{
			pId = key.substring(lastPos+1);
			cId = pId.substring(0,1);
			if (cId.equals("A")||cId.equals("B")||cId.equals("C")
					||cId.equals("D")||cId.equals("E")||cId.equals("F")||cId.equals("G")	
					||cId.equals("H")||cId.equals("I")||cId.equals("J")||cId.equals("K")||cId.equals("L")
					||cId.equals("M")||cId.equals("N")||cId.equals("O")||cId.equals("P")	
					||cId.equals("Q")||cId.equals("R")||cId.equals("S")||cId.equals("T")||cId.equals("U")
					||cId.equals("V")||cId.equals("W")||cId.equals("X")||cId.equals("Y")||cId.equals("Z"))
				{
					pId = pId.substring(1);
				}	
		}
		else
		{
			if (ecoWBS.getCostObjectID()==null || ecoWBS.getCostObjectID().equalsIgnoreCase(""))
			{
				pId = key.substring(parentLen+1);
			}
			else
				pId = ecoWBS.getCostObjectID();
		}
	
		 return pId;
	}
	private String calculateID2(BayerWBSAPIType ecoWBS, BayerWBSAPIType parentWBS)
	{
		String cId = ecoWBS.getCostObjectID();
		String pId = parentWBS.getCostObjectID();
		
		String key = ecoWBS.getExternalKey();
		String pKey = parentWBS.getExternalKey();
		String newKey = pKey+cId;
		if (newKey.equalsIgnoreCase(key))
		{
			return cId;
		}
		return calculateID4(ecoWBS);
	}
	private String calculateID(BayerWBSAPIType ecoWBS)
	{
		String cId = "";
		String pId = "";
		
		String key = ecoWBS.getExternalKey();
		int parentLen = ecoWBS.getParentCostObjectHierarchyPathID().length();
		if (ecoWBS.getCostObjectID()==null || ecoWBS.getCostObjectID().equalsIgnoreCase(""))
		{
			cId = key.substring(parentLen+1);
		}
		else
		{
			cId = ecoWBS.getCostObjectID();
			pId = key.substring(key.length()-2, key.length()-1);
			if (cId.equals("0")||cId.equals("1")||cId.equals("2")||cId.equals("3")
					||cId.equals("4")||cId.equals("5")||cId.equals("6")||cId.equals("7")	
					||cId.equals("8")||cId.equals("9"))
				
			{	if (pId.equals("1")||pId.equals("2")||pId.equals("3")
					||pId.equals("4")||pId.equals("5")||pId.equals("6")||pId.equals("7")	
					||pId.equals("8")||pId.equals("9"))
				{
					cId = pId + cId;
				}		
			}
		}
		return cId;
	}
	private int calculateHierarchyLevel(BayerWBSAPIType ecoWBS, String projectID)
	{
		int newLevel = 0;
		String parentID = ecoWBS.getParentCostObjectHierarchyPathID();
		int parentLen = parentID.length();
		if (parentLen == projectID.length())
			newLevel = 2;
		if (parentLen == projectID.length()+2)
			newLevel = 3;
		if (parentLen > projectID.length()+2)
			newLevel = 4;
		return newLevel;
	}
	
	private void calculateHierarchyPath( Map<String, BayerWBSAPIType> wbsMap, String projectId) {
		if (wbsMap != null) {
			Set<String> keys = wbsMap.keySet();
			for (String key:keys) {
				BayerWBSAPIType wbs = wbsMap.get(key);
				int hierarchyLevel = (wbs != null ? wbs.getCostObjectHierarchyLevel() : 0);
				String pathID=null;
				while(hierarchyLevel > 1) 
				{
					String parentPath = null;
					if (wbs.getParentCostObjectHierarchyPathID()!=null)
					{
						parentPath = wbs.getParentCostObjectHierarchyPathID();
						String parentID = null;
						
						//
						//if (hierarchyLevel == 2)
							//parentID = parentPath;
						if (parentPath.equalsIgnoreCase(projectId))
							parentID = parentPath;
						else
						{
							//parentID = parentPath.substring(parentPath.lastIndexOf("-")+1);
							if (parentPath!=null && !parentPath.equals(""))
							{
								BayerWBSAPIType parentWbs = wbsMap.get(parentPath);
								if (parentWbs!=null)
								{
									if (parentWbs.getCostObjectHierarchyLevel()==wbs.getCostObjectHierarchyLevel())
									{
										wbs.setCostObjectHierarchyLevel(wbs.getCostObjectHierarchyLevel()+1);
										wbs.setCostObjectID(calculateID4(wbs));
										hierarchyLevel++;
									}
									parentID = parentWbs.getCostObjectID();
								}	
								else
								{
									pathID = projectId+"."+wbs.getCostObjectID();
									logger.debug("No Parent found for Wbs with External Key: "+ wbsMap.get(key).getExternalKey() + ", set to projectId");	
									break;
								}
							}
						}
							//logger.debug("Parent ID: " + parentID);
						if (pathID == null || pathID.equals("")) 
							pathID = parentID + "." + wbs.getCostObjectID();
						else
							pathID = parentID + "." + pathID;
						
						hierarchyLevel--;
						String newKey = wbs.getParentCostObjectHierarchyPathID();
						if (wbsMap.get(newKey)!=null)
							wbs = wbsMap.get(newKey);
					}
				}		
				if (pathID !=null)
				{
					wbsMap.get(key).setHierarchyPathID(pathID);
					logger.debug("External Key: "+ wbsMap.get(key).getExternalKey() + " | Hierarchy Path: "+ wbsMap.get(key).getHierarchyPathID());
				}
				else
					logger.debug("No Parent find for Wbs with External Key: "+ wbsMap.get(key).getExternalKey());	
			}
		}
	}
	
	private Map<String, BayerWBSAPIType> calculateID( Map<String, BayerWBSAPIType> wbsMap, String projectId) {
		Map<String, BayerWBSAPIType> newMap = new HashMap<String, BayerWBSAPIType>();
		if (wbsMap != null) {
			Set<String> keys = wbsMap.keySet();
			for (String key:keys) {
				BayerWBSAPIType wbs = wbsMap.get(key);
				BayerWBSAPIType pWbs =new BayerWBSAPIType();				
				if (wbs.getParentCostObjectHierarchyPathID()!=null && !wbs.getExternalKey().equalsIgnoreCase(projectId))
				{
					 pWbs =wbsMap.get(wbs.getParentCostObjectHierarchyPathID());
					 if (pWbs!=null)
					 {
						 wbs.setCostObjectID(calculateID2(wbs,pWbs));
					 }
					 else
					 {
						logger.debug("No Parent found for Wbs with External Key: "+ wbsMap.get(key).getExternalKey() + " Parent set to Project ID: " + projectId);
						 wbs.setCostObjectID(wbs.getExternalKey().substring(projectId.length()+1));
						 wbs.setParentCostObjectHierarchyPathID(projectId);
						 wbs.setCostObjectHierarchyLevel(2);
					 }
				}	
				newMap.put(key, wbs);
			}
		}
		return newMap;
	}
	private void calculateHierarchyPathSub( Map<String, BayerWBSAPIType> wbsMap, String masterProjectId) {
		if (wbsMap != null) {
			Set<String> keys = wbsMap.keySet();
			for (String key:keys) {
				BayerWBSAPIType wbs = wbsMap.get(key);
				int hierarchyLevel = (wbs != null ? wbs.getCostObjectHierarchyLevel() : 0);
				String pathID=null;
				while(hierarchyLevel > 1) {
					String parentPath = wbs.getParentCostObjectHierarchyPathID();
					String parentID = null;
					if (hierarchyLevel == 2)
						parentID = parentPath;
					else
						parentID = parentPath.substring(parentPath.lastIndexOf("-")+1);
					//logger.debug("Parent ID: " + parentID);
					if (pathID == null) 
						pathID = parentID + "." + wbs.getCostObjectID();
					else
						pathID = parentID + "." + pathID;
					hierarchyLevel--;
					String newKey = wbs.getParentCostObjectHierarchyPathID();
					wbs = wbsMap.get(newKey);
				}				
				if (pathID !=null)
				{
					pathID = masterProjectId + "."+pathID;
					wbsMap.get(key).setHierarchyPathID(pathID);
					//logger.debug("External Key: "+ wbsMap.get(key).getExternalKey() + " | Hierarchy Path: "+ wbsMap.get(key).getHierarchyPathID());
				}
				else
					logger.debug("No Parent found for Wbs with External Key: "+ wbsMap.get(key).getExternalKey());
			}		
		}
	}
	
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

	public Map<String, BayerPMOrderAPIType> mapPMOrderForImport(Map<String, Map<String, Object>> dataRows) 
			throws SystemException {
		Map<String, BayerPMOrderAPIType> returnMap = new HashMap<String, BayerPMOrderAPIType>();
		
		Set<String> rowKeys = dataRows.keySet();
		for (String rowKey : rowKeys){
			//logger.debug("Row Key is: " + rowKey);

			BayerPMOrderAPIType apiType = new BayerPMOrderAPIType();

			Map<String, Object> rowVal = dataRows.get(rowKey);			
			Set<String> fieldNames = rowVal.keySet();
			
			for (String fieldName : fieldNames) {
				if (rowVal.get(fieldName)!=null)
				{
					String fieldVal = rowVal.get(fieldName).toString().trim();
					//String fieldVal = rowVal.get(fieldName);
					//logger.debug("Field Name: "+ fieldName);
					//logger.debug("Field Value: "+ fieldVal);
					if (fieldName != null && fieldName.equalsIgnoreCase("PmOrderid"))
					{
						apiType.setExternalKey(fieldVal);
						apiType.setCostObjectID(fieldVal);
					}
					else if (fieldName != null && fieldName.equalsIgnoreCase("WbsElement"))
					{
						apiType.setParentCostObjectExternalKey(fieldVal);
						apiType.setSAPWBSElement(fieldVal);
					}
					else if (fieldName != null && fieldName.equalsIgnoreCase("PmDescription"))
						apiType.setCostObjectName(fieldVal);
					else if (fieldName != null && fieldName.equalsIgnoreCase("PmEstCost"))
						apiType.setPMEstimatedCost(Double.parseDouble(fieldVal));
					else if (fieldName != null && fieldName.equalsIgnoreCase("PmActCost"))
						apiType.setPMActualCost(Double.parseDouble(fieldVal));
					else if (fieldName != null && fieldName.equalsIgnoreCase("PmStatus"))
						apiType.setPmStatus(fieldVal);
				}
			}
			if (apiType.getExternalKey() != null) {
				returnMap.put(apiType.getExternalKey(), apiType);				
			}
		}
		return returnMap;
	}
	
	public Map<String, BayerPMOrderV2APIType> mapPMOrderV2ForImport(Map<String, Map<String, Object>> dataRows) 
			throws SystemException {
		Map<String, BayerPMOrderV2APIType> returnMap = new HashMap<String, BayerPMOrderV2APIType>();
		
		Set<String> rowKeys = dataRows.keySet();
		for (String rowKey : rowKeys){
			//logger.debug("Row Key is: " + rowKey);

			BayerPMOrderV2APIType apiType = new BayerPMOrderV2APIType();

			Map<String, Object> rowVal = dataRows.get(rowKey);			
			Set<String> fieldNames = rowVal.keySet();
			
			for (String fieldName : fieldNames) {
				if (rowVal.get(fieldName)!=null)
				{
					String fieldVal = rowVal.get(fieldName).toString().trim();
					//String fieldVal = rowVal.get(fieldName);
					//logger.debug("Field Name: "+ fieldName);
					//logger.debug("Field Value: "+ fieldVal);
					if (fieldName != null && fieldName.equalsIgnoreCase("PmOrderid"))
					{
						apiType.setExternalKey(fieldVal);
						apiType.setCostObjectID(fieldVal);
					}
					else if (fieldName != null && fieldName.equalsIgnoreCase("WbsElement"))
					{
						apiType.setParentCostObjectExternalKey(fieldVal);
						apiType.setSAPWBSElement(fieldVal);
						apiType.setSAPWBSParent(fieldVal);
					}
					else if (fieldName != null && fieldName.equalsIgnoreCase("PmDescription"))
						apiType.setCostObjectName(fieldVal);
					else if (fieldName != null && fieldName.equalsIgnoreCase("PmEstCost"))
						apiType.setPMEstimatedCost(Double.parseDouble(fieldVal));
					else if (fieldName != null && fieldName.equalsIgnoreCase("PmActCost"))
						apiType.setPMActualCost(Double.parseDouble(fieldVal));
					else if (fieldName != null && fieldName.equalsIgnoreCase("PmStatus"))
						apiType.setPmStatus(fieldVal);
				}
			}
			if (apiType.getExternalKey() != null) {
				returnMap.put(apiType.getExternalKey(), apiType);				
			}
		}
		return returnMap;
	}
	
	public Map<String, BayerActualsAPIType> mapActualsForImport(Map<String, Map<String, Object>> dataRows) 
			throws SystemException {
		Map<String, BayerActualsAPIType> returnMap = new HashMap<String, BayerActualsAPIType>();
		
		Set<String> rowKeys = dataRows.keySet();
		for (String rowKey : rowKeys){
			//logger.debug("Row Key is: " + rowKey);

			BayerActualsAPIType apiType = new BayerActualsAPIType();

			Map<String, Object> rowVal = dataRows.get(rowKey);			
			Set<String> fieldNames = rowVal.keySet();
			String externalKey = "";
			String wbsElement = "";
			String pmOrder = "";
			String poNo = "";
			String poLineItem = "";
			String postingRow = "";
			for (String fieldName : fieldNames) {

				if (rowVal.get(fieldName)!=null)
				{
					String fieldVal = rowVal.get(fieldName).toString().trim();
					//logger.debug("Field Name: "+ fieldName);
					//logger.debug("Field Value: "+ fieldVal);
					if (fieldName != null && fieldName.equalsIgnoreCase("ControllingArea"))
						apiType.setControllingArea(fieldVal);
					if (fieldName != null && fieldName.equalsIgnoreCase("PostingRow"))
					{
						postingRow = fieldVal;
						apiType.setPostingrow(Double.parseDouble(fieldVal));
					}
					if (fieldName != null && fieldName.equalsIgnoreCase("DocumentNo"))
						apiType.setFIDocumentNumber(fieldVal);
					else if (fieldName != null && fieldName.equalsIgnoreCase("PostingDate"))
					{
						if (fieldVal!=null&&!fieldVal.equals(""))
						try {
								apiType.setTransactionDate(this.getXMLDate(fieldVal));
						}
						catch (DatatypeConfigurationException e){
						}
					}
					else if (fieldName != null && fieldName.equalsIgnoreCase("AmountTcurr"))
						apiType.setCostTransactionCurrency(Double.parseDouble(fieldVal));
					else if (fieldName != null && fieldName.equalsIgnoreCase("AmountOcurr"))
					{
						apiType.setCostCostObjectCurrency(Double.parseDouble(fieldVal));
						apiType.setAlternateCostTransactionCurrency(Double.parseDouble(fieldVal));
					}
						//else if (fieldName != null && fieldName.equalsIgnoreCase("PurchOrd"))
						//apiType.setCommitmentID(fieldVal);
					else if (fieldName != null && fieldName.equalsIgnoreCase("WbsElement"))
					{
						if (fieldVal!=null && !fieldVal.equals(""))
							wbsElement = fieldVal;
					}
					else if (fieldName != null && fieldName.equalsIgnoreCase("PMOrder"))
					{
						if (fieldVal!=null && !fieldVal.equals(""))
							pmOrder=fieldVal;
					}
					else if (fieldName != null && fieldName.equalsIgnoreCase("CostElement"))
						apiType.setCostAccountID(fieldVal);
					else if (fieldName != null && fieldName.equalsIgnoreCase("TransCurr"))
						apiType.setCurrencyTransactionCode(fieldVal);
					else if (fieldName != null && fieldName.equalsIgnoreCase("ObjectCurr"))
						apiType.setCurrencyCostObjectCode(fieldVal);
					else if (fieldName != null && fieldName.equalsIgnoreCase("ExchangeRate"))
					{
						if (Double.parseDouble(fieldVal)!=0.00)
							apiType.setConversionRateCostObjectCurrency(1/Double.parseDouble(fieldVal));
						else
							apiType.setConversionRateCostObjectCurrency(0.00);
						
						apiType.setSAPExchangeRate(Double.parseDouble(fieldVal));
					}
					else if (fieldName != null && fieldName.equalsIgnoreCase("PurchOrd"))
					{
						if (fieldVal!=null && !fieldVal.equals(""))
						{
							apiType.setCommitmentID(fieldVal);
							poNo = fieldVal;
						}
					}
					else if (fieldName != null && fieldName.equalsIgnoreCase("PurchOrdItem"))
					{
						if (fieldVal!=null && !fieldVal.equals(""))
						{
							apiType.setSAPPurchasingDocumentLineItemNumber(fieldVal);
							poLineItem = fieldVal;
						}
					}
					else if (fieldName != null && fieldName.equalsIgnoreCase("AcReferenceDoc"))
						apiType.setActualReferenceDoc(fieldVal);
					else if (fieldName != null && fieldName.equalsIgnoreCase("AcHeaderTxt"))
						apiType.setActualReferenceHeaderText(fieldVal);
					else if (fieldName != null && fieldName.equalsIgnoreCase("VendorId"))
						apiType.setVendorID(fieldVal);
					else if (fieldName != null && fieldName.equalsIgnoreCase("VendorName"))
						apiType.setVendorName(fieldVal);
					
					else if (fieldName != null && fieldName.equalsIgnoreCase("PurchOrdSeq"))
						apiType.setSAPPurcharsingOrderSeqNumber(fieldVal);

					
					else if (fieldName != null && fieldName.equalsIgnoreCase("OffsetAcno"))
					{
						if (fieldVal!=null && !fieldVal.equals(""))
							apiType.setOffsettingAccountNumber(fieldVal);
						if (fieldVal.equalsIgnoreCase(GlobalConstants.SAP_OFFSET_ACNO_ACCRUAL))
							apiType.setSAPAccrualID(GlobalConstants.EPC_API_ERROR_FLAG_Y);;
					}
				}
				
			}
			//Set owner cost object
			if (pmOrder!=null && !pmOrder.equals(""))
			{
				apiType.setCostObjectExternalKey(pmOrder);
				apiType.setSAPWBSElement(pmOrder);
				if (pmOrder.equalsIgnoreCase("362750073214"))
	                logger.debug("Reading 362750073214 row " + rowVal.toString());
				if (pmOrder.equalsIgnoreCase("362750109991"))
	                logger.debug("Reading 362750109991 row " + rowVal.toString());
			}
			

			else if (wbsElement!=null && !wbsElement.equals(""))
			{
				apiType.setCostObjectExternalKey(wbsElement);
				apiType.setSAPWBSElement(wbsElement);
			}
			if (poNo!=null && !poNo.equals(""))
			{
				poLineItem = poNo + "-" + poLineItem;
				poLineItem = apiType.getCostObjectExternalKey() + "-"+ poLineItem;
				apiType.setCostObjectExternalKey(poLineItem);
			}

			//Set external key
			
			//if (apiType.getCostObjectExternalKey()!=null && !apiType.getCostObjectExternalKey().equals(""))
				//apiType.setExternalKey(apiType.getCostObjectExternalKey()+ "-"+rowKey);
			
			if (apiType.getCostObjectExternalKey()!=null && !apiType.getCostObjectExternalKey().equals(""))
			{
				apiType.setExternalKey(	apiType.getControllingArea() 
										+ "-" + apiType.getFIDocumentNumber()
										+ "-" + postingRow);
				apiType.setSAPUniqueID(apiType.getExternalKey());
			}
			
			if (apiType.getExternalKey() != null && !isExcluded(apiType)) 
			{
				//Special Cost Account Filter 3020000
				if (!apiType.getCostAccountID().equalsIgnoreCase("3020000"))
					
				{
					/*if (apiType.getSAPWBSElement().equalsIgnoreCase("A00GV-282646-CAA6002")
							||apiType.getSAPWBSElement().equalsIgnoreCase("362750102503")
							||apiType.getSAPWBSElement().equalsIgnoreCase("362750102903")
							||apiType.getSAPWBSElement().equalsIgnoreCase("362750103173"))
						
				
					{
						if (apiType.getCostTransactionCurrency()==16.50
						||apiType.getCostTransactionCurrency()==439.08)
						{
							//returnMap.put(apiType.getExternalKey(), apiType);
						}
					}*/
					
					returnMap.put(apiType.getExternalKey(), apiType);
				}
			}
		}
		return returnMap;
	}
	
	public boolean isExcluded(BayerActualsAPIType api)
	{
		boolean isEx = false;
		String costElement = "";
		if (api.getCostAccountID()!=null && api.getCostAccountID().equals(""))
		{
			costElement = api.getCostAccountID();
			if (costElement.equalsIgnoreCase(""))
				isEx = true;
		}
			//isEx = false;
		return isEx;
	}
	
	public Map<String, BayerPOPRHeadersAPIType> mapPOHeaderForImport
			(String projectHierarchyPathId, Map<String, Map<String, Object>> dataRows) 
					throws SystemException {
		Map<String, BayerPOPRHeadersAPIType> returnMap = new HashMap<String, BayerPOPRHeadersAPIType>();
		
		Set<String> rowKeys = dataRows.keySet();
		for (String rowKey : rowKeys){
			//logger.debug("Row Key is: " + rowKey);

			BayerPOPRHeadersAPIType apiType = new BayerPOPRHeadersAPIType();

			Map<String, Object> rowVal = dataRows.get(rowKey);			
			Set<String> fieldNames = rowVal.keySet();
			
			for (String fieldName : fieldNames) {
				if (rowVal.get(fieldName)!=null)
				{
					String fieldVal = rowVal.get(fieldName).toString().trim();
					//logger.debug("Field Name: "+ fieldName);
					//logger.debug("Field Value: "+ fieldVal);
					if (fieldName != null && fieldName.equalsIgnoreCase("PurchOrd"))
					{
						apiType.setTaskID(fieldVal);
					}
					else if (fieldName != null && fieldName.equalsIgnoreCase("PurchOrdDesc"))
						apiType.setTaskName(fieldVal);
					else if (fieldName != null && fieldName.equalsIgnoreCase("PoComCost"))
						apiType.setWorkingForecastCosts(Double.parseDouble(fieldVal));
					else if (fieldName != null && fieldName.equalsIgnoreCase("PoActCost"))
						apiType.setActualCosts(Double.parseDouble(fieldVal));
					else if (fieldName != null && fieldName.equalsIgnoreCase("VendorId"))
						apiType.setVendorID(fieldVal);
					else if (fieldName != null && fieldName.equalsIgnoreCase("VendorName"))
						apiType.setVendorName(fieldVal);
					else if (fieldName != null && fieldName.equalsIgnoreCase("Requestor"))
						apiType.setRequestor(fieldVal);
					else if (fieldName != null && fieldName.equalsIgnoreCase("Receiver"))
						apiType.setReceiver(fieldVal);
					else if (fieldName != null && fieldName.equalsIgnoreCase("LastInvDt"))
					{
						if (fieldVal!=null&&!fieldVal.equals(""))
						try {
								apiType.setLastInvoiceDate(this.getXMLDate(fieldVal));
						}
						catch (DatatypeConfigurationException e){
						}
					}
					else if (fieldName != null && fieldName.equalsIgnoreCase("ProjectDefinition"))
					{
						apiType.setOwnerCostObjectExternalKey(fieldVal);
						apiType.setOwnerCostObjectHierarchyPathID(projectHierarchyPathId);
					}
				}	
			} 
			if (apiType.getTaskName()==null || apiType.getTaskName().equals(""))
				apiType.setTaskName(apiType.getTaskID());
			
			apiType.setCommitmentTypeHierarchyPathID(GlobalConstants.EPC_REST_COMMITMENT_TYPE_PO);
			
			if (apiType.getTaskID() != null) {
				returnMap.put(apiType.getTaskID(), apiType);				
			}
            logger.debug("Reading PO " + apiType.getTaskID() + ": " + rowVal.toString());
			if (apiType.getTaskID().equalsIgnoreCase("2111249375"))
                logger.debug("Reading 2111249375 row " + rowVal.toString());
		}
		return returnMap;
	}
	
	public Map<String, BayerPOPRHeadersV2APIType> mapPOHeaderV2ForImport
	(String projectHierarchyPathId, Map<String, Map<String, Object>> dataRows) 
			throws SystemException {
			Map<String, BayerPOPRHeadersV2APIType> returnMap = new HashMap<String, BayerPOPRHeadersV2APIType>();

		Set<String> rowKeys = dataRows.keySet();
		for (String rowKey : rowKeys){
			//logger.debug("Row Key is: " + rowKey);

			BayerPOPRHeadersV2APIType apiType = new BayerPOPRHeadersV2APIType();

			Map<String, Object> rowVal = dataRows.get(rowKey);			
			Set<String> fieldNames = rowVal.keySet();
	
			for (String fieldName : fieldNames) {
				if (rowVal.get(fieldName)!=null)
				{
					String fieldVal = rowVal.get(fieldName).toString().trim();
					//logger.debug("Field Name: "+ fieldName);
					//logger.debug("Field Value: "+ fieldVal);
					if (fieldName != null && fieldName.equalsIgnoreCase("PurchOrd"))
					{
						apiType.setTaskID(fieldVal);
					}
					else if (fieldName != null && fieldName.equalsIgnoreCase("PurchOrdDesc"))
						apiType.setTaskName(fieldVal);
					else if (fieldName != null && fieldName.equalsIgnoreCase("PoComCost"))
						apiType.setWorkingForecastCosts(Double.parseDouble(fieldVal));
					else if (fieldName != null && fieldName.equalsIgnoreCase("PoActCost"))
						apiType.setActualCosts(Double.parseDouble(fieldVal));
					else if (fieldName != null && fieldName.equalsIgnoreCase("VendorId"))
						apiType.setVendorID(fieldVal);
					else if (fieldName != null && fieldName.equalsIgnoreCase("VendorName"))
						apiType.setVendorName(fieldVal);
					else if (fieldName != null && fieldName.equalsIgnoreCase("Requestor"))
						apiType.setRequestor(fieldVal);
					else if (fieldName != null && fieldName.equalsIgnoreCase("Receiver"))
						apiType.setReceiver(fieldVal);
					else if (fieldName != null && fieldName.equalsIgnoreCase("LastInvDt"))
					{
						if (fieldVal!=null&&!fieldVal.equals(""))
							try {
									apiType.setLastInvoiceDate(this.getXMLDate(fieldVal));
							}
						catch (DatatypeConfigurationException e){
						}
					}
					else if (fieldName != null && fieldName.equalsIgnoreCase("ProjectDefinition"))
					{
						apiType.setOwnerCostObjectExternalKey(fieldVal);
						apiType.setOwnerCostObjectHierarchyPathID(projectHierarchyPathId);
					}
				}	
			} 
			if (apiType.getTaskName()==null || apiType.getTaskName().equals(""))
				apiType.setTaskName(apiType.getTaskID());
	
			apiType.setCommitmentTypeHierarchyPathID(GlobalConstants.EPC_REST_COMMITMENT_TYPE_PO);
	
			if (apiType.getTaskID() != null) {
				returnMap.put(apiType.getTaskID(), apiType);				
			}
			logger.debug("Reading PO " + apiType.getTaskID() + ": " + rowVal.toString());
			//if (apiType.getTaskID().equalsIgnoreCase("2111249375"))
				//logger.debug("Reading 2111249375 row " + rowVal.toString());
		}
		return returnMap;
	}
	
	public Map<String, BayerPOPRHeadersAPIType> mapPRHeaderForImport(
				String projectHierarchyPathId, Map<String, Map<String, Object>> dataRows) 
			throws SystemException {
		Map<String, BayerPOPRHeadersAPIType> returnMap = new HashMap<String, BayerPOPRHeadersAPIType>();
		
		Set<String> rowKeys = dataRows.keySet();
		for (String rowKey : rowKeys){
			//logger.debug("Row Key is: " + rowKey);

			BayerPOPRHeadersAPIType apiType = new BayerPOPRHeadersAPIType();

			Map<String, Object> rowVal = dataRows.get(rowKey);			
			Set<String> fieldNames = rowVal.keySet();
			
			for (String fieldName : fieldNames) {
				if (rowVal.get(fieldName)!=null)
				{
					String fieldVal = rowVal.get(fieldName).toString().trim();
					//logger.debug("Field Name: "+ fieldName);
					//logger.debug("Field Value: "+ fieldVal);
					if (fieldName != null && fieldName.equalsIgnoreCase("PurchReq"))
					{
						apiType.setTaskID(fieldVal);
						apiType.setTaskName(fieldVal);
					}
					else if (fieldName != null && fieldName.equalsIgnoreCase("PurchReqDesc"))
						apiType.setTaskName(fieldVal);
					else if (fieldName != null && fieldName.equalsIgnoreCase("PrComCost"))
						apiType.setWorkingForecastCosts(Double.parseDouble(fieldVal));
					else if (fieldName != null && fieldName.equalsIgnoreCase("PrActCost"))
						apiType.setActualCosts(Double.parseDouble(fieldVal));
					else if (fieldName != null && fieldName.equalsIgnoreCase("VendorId"))
						apiType.setVendorID(fieldVal);
					else if (fieldName != null && fieldName.equalsIgnoreCase("VendorName"))
						apiType.setVendorName(fieldVal);
					else if (fieldName != null && fieldName.equalsIgnoreCase("ProjectDefinition"))
					{
						apiType.setOwnerCostObjectExternalKey(fieldVal);
						apiType.setOwnerCostObjectHierarchyPathID(projectHierarchyPathId);
					}
				}
			} 
			apiType.setCommitmentTypeHierarchyPathID(GlobalConstants.EPC_REST_COMMITMENT_TYPE_PR);
			if (apiType.getTaskName()==null || apiType.getTaskName().equals(""))
				apiType.setTaskName(apiType.getTaskID());
			if (apiType.getTaskID() != null) {
				returnMap.put(apiType.getTaskID(), apiType);				
			}
		}
		return returnMap;
	}
	
	public Map<String, BayerPOPRHeadersV2APIType> mapPRHeaderV2ForImport(
			String projectHierarchyPathId, Map<String, Map<String, Object>> dataRows) 
					throws SystemException {
		Map<String, BayerPOPRHeadersV2APIType> returnMap = new HashMap<String, BayerPOPRHeadersV2APIType>();
	
		Set<String> rowKeys = dataRows.keySet();
		for (String rowKey : rowKeys){
			//logger.debug("Row Key is: " + rowKey);

			BayerPOPRHeadersV2APIType apiType = new BayerPOPRHeadersV2APIType();

			Map<String, Object> rowVal = dataRows.get(rowKey);			
			Set<String> fieldNames = rowVal.keySet();
		
			for (String fieldName : fieldNames) {
				if (rowVal.get(fieldName)!=null)
				{
					String fieldVal = rowVal.get(fieldName).toString().trim();
					//logger.debug("Field Name: "+ fieldName);
					//logger.debug("Field Value: "+ fieldVal);
					if (fieldName != null && fieldName.equalsIgnoreCase("PurchReq"))
					{
						apiType.setTaskID(fieldVal);
						apiType.setTaskName(fieldVal);
					}
					else if (fieldName != null && fieldName.equalsIgnoreCase("PurchReqDesc"))
						apiType.setTaskName(fieldVal);
					else if (fieldName != null && fieldName.equalsIgnoreCase("PrComCost"))
						apiType.setWorkingForecastCosts(Double.parseDouble(fieldVal));
					else if (fieldName != null && fieldName.equalsIgnoreCase("PrActCost"))
						apiType.setActualCosts(Double.parseDouble(fieldVal));
					else if (fieldName != null && fieldName.equalsIgnoreCase("VendorId"))
						apiType.setVendorID(fieldVal);
					else if (fieldName != null && fieldName.equalsIgnoreCase("VendorName"))
						apiType.setVendorName(fieldVal);
					else if (fieldName != null && fieldName.equalsIgnoreCase("ProjectDefinition"))
					{
						apiType.setOwnerCostObjectExternalKey(fieldVal);
						apiType.setOwnerCostObjectHierarchyPathID(projectHierarchyPathId);
					}
				}
			} 
			apiType.setCommitmentTypeHierarchyPathID(GlobalConstants.EPC_REST_COMMITMENT_TYPE_PR);
			if (apiType.getTaskName()==null || apiType.getTaskName().equals(""))
				apiType.setTaskName(apiType.getTaskID());
			if (apiType.getTaskID() != null) {
				returnMap.put(apiType.getTaskID(), apiType);				
			}
		}
		return returnMap;
	}	
	
	public Map<String, BayerCommitmentLICOAPIType> mapPOLineItemCOForImport(Map<String, Map<String, Object>> dataRows) 
			throws SystemException {
		Map<String, BayerCommitmentLICOAPIType> returnMap = new HashMap<String, BayerCommitmentLICOAPIType>();
		
		Set<String> rowKeys = dataRows.keySet();
		//int keyCount = 0;
		for (String rowKey : rowKeys){
			//logger.debug("Row Key is: " + rowKey);

			BayerCommitmentLICOAPIType apiType = new BayerCommitmentLICOAPIType();

			Map<String, Object> rowVal = dataRows.get(rowKey);			
			Set<String> fieldNames = rowVal.keySet();
			String coId = "";
			String coItem = "";
			for (String fieldName : fieldNames) {
				if (rowVal.get(fieldName)!=null)
				{
					String fieldVal = rowVal.get(fieldName).toString().trim();
					//logger.debug("Field Name: "+ fieldName);
					//logger.debug("Field Value: "+ fieldVal);
					if (fieldName != null && fieldName.equalsIgnoreCase("WbsElement"))
					{
						if (fieldVal != null && !fieldVal.equals(""))
							apiType.setParentCostObjectExternalKey(fieldVal);
					}
					else if (fieldName != null && fieldName.equalsIgnoreCase("PmOrder"))
					{
						if (fieldVal != null && !fieldVal.equals(""))
							apiType.setParentCostObjectExternalKey(fieldVal);
					}
					else if (fieldName != null && fieldName.equalsIgnoreCase("PurchOrd"))
						coId = fieldVal;
					else if (fieldName != null && fieldName.equalsIgnoreCase("PurchOrdItem"))
					{
						coItem = fieldVal;
					}
					else if (fieldName != null && fieldName.equalsIgnoreCase("PurchOrdDesc"))
						apiType.setCostObjectName(fieldVal);
				}
			}
			
			if (!coId.equals("") && !coItem.equals(""))
			{
				coId = coId + "-" + coItem;
				apiType.setCostObjectID(coId);
			}
			
			if (apiType.getParentCostObjectExternalKey() != null
					&& apiType.getCostObjectID() !=null ) {
				coId = apiType.getParentCostObjectExternalKey() + "-"+coId;
				apiType.setExternalKey(coId);
				returnMap.put(apiType.getExternalKey(), apiType);				
			}
		}
		return returnMap;
	}

	public Map<String, BayerCommitmentLICOV2APIType> mapPOLineItemCOV2ForImport(Map<String, Map<String, Object>> dataRows) 
			throws SystemException {
		Map<String, BayerCommitmentLICOV2APIType> returnMap = new HashMap<String, BayerCommitmentLICOV2APIType>();
		
		Set<String> rowKeys = dataRows.keySet();
		//int keyCount = 0;
		for (String rowKey : rowKeys){
			//logger.debug("Row Key is: " + rowKey);

			BayerCommitmentLICOV2APIType apiType = new BayerCommitmentLICOV2APIType();

			Map<String, Object> rowVal = dataRows.get(rowKey);			
			Set<String> fieldNames = rowVal.keySet();
			String coId = "";
			String coItem = "";
			for (String fieldName : fieldNames) {
				if (rowVal.get(fieldName)!=null)
				{
					String fieldVal = rowVal.get(fieldName).toString().trim();
					//logger.debug("Field Name: "+ fieldName);
					//logger.debug("Field Value: "+ fieldVal);
					if (fieldName != null && fieldName.equalsIgnoreCase("WbsElement"))
					{
						if (fieldVal != null && !fieldVal.equals(""))
						{
							apiType.setParentCostObjectExternalKey(fieldVal);
							apiType.setSAPWBSParent(fieldVal);
						}
					}
					else if (fieldName != null && fieldName.equalsIgnoreCase("PmOrder"))
					{
						if (fieldVal != null && !fieldVal.equals(""))
						{
							apiType.setParentCostObjectExternalKey(fieldVal);
							apiType.setSAPWBSParent(fieldVal);
						}	
					}
					else if (fieldName != null && fieldName.equalsIgnoreCase("PurchOrd"))
						coId = fieldVal;
					else if (fieldName != null && fieldName.equalsIgnoreCase("PurchOrdItem"))
					{
						coItem = fieldVal;
					}
					else if (fieldName != null && fieldName.equalsIgnoreCase("PurchOrdDesc"))
						apiType.setCostObjectName(fieldVal);
					
					else if (fieldName != null && fieldName.equalsIgnoreCase("FinalConf"))
					{ 
						if (fieldVal!=null && fieldVal.equalsIgnoreCase("X"))
							apiType.setFinalConfirmationStagingID(GlobalConstants.EPC_API_ERROR_FLAG_Y);
						else
							apiType.setFinalConfirmationStagingID(GlobalConstants.EPC_API_ERROR_FLAG_N);
					}
					else if (fieldName != null && fieldName.equalsIgnoreCase("DelFlag"))
					{
						if (fieldVal!=null && !fieldVal.equals(""))
						{
							apiType.setDeletionFlagStagingID(fieldVal);
						}	
					}
					
					else if (fieldName != null && fieldName.equalsIgnoreCase("PurchOrdPerc"))
						apiType.setSAPPODistributionStagingPCT(Double.parseDouble(fieldVal));
				}
			}
			
			if (!coId.equals("") && !coItem.equals(""))
			{
				coId = coId + "-" + coItem;
				apiType.setCostObjectID(coId);
			}
			
			if (apiType.getParentCostObjectExternalKey() != null
					&& apiType.getCostObjectID() !=null ) {
				coId = apiType.getParentCostObjectExternalKey() + "-"+coId;
				apiType.setExternalKey(coId);
				returnMap.put(apiType.getExternalKey(), apiType);				
			}
		}
		return returnMap;
	}
	
	public Map<String, BayerCommitmentLICOAPIType> mapPRLineItemCOForImport(Map<String, Map<String, Object>> dataRows) 
			throws SystemException {
		Map<String, BayerCommitmentLICOAPIType> returnMap = new HashMap<String, BayerCommitmentLICOAPIType>();
		
		Set<String> rowKeys = dataRows.keySet();
		for (String rowKey : rowKeys){
			//logger.debug("Row Key is: " + rowKey);

			BayerCommitmentLICOAPIType apiType = new BayerCommitmentLICOAPIType();

			Map<String, Object> rowVal = dataRows.get(rowKey);			
			Set<String> fieldNames = rowVal.keySet();
			String coId = "";
			String coItem = "";
			for (String fieldName : fieldNames) {
				if (rowVal.get(fieldName)!=null)
				{
					String fieldVal = rowVal.get(fieldName).toString().trim();
					//logger.debug("Field Name: "+ fieldName);
					//logger.debug("Field Value: "+ fieldVal);
					if (fieldName != null && fieldName.equalsIgnoreCase("WbsElement"))
					{
						if (fieldVal != null && !fieldVal.equals(""))
							apiType.setParentCostObjectExternalKey(fieldVal);
					}
					else if (fieldName != null && fieldName.equalsIgnoreCase("PmOrder"))
					{
						if (fieldVal != null && !fieldVal.equals(""))
							apiType.setParentCostObjectExternalKey(fieldVal);
					}
					else if (fieldName != null && fieldName.equalsIgnoreCase("PurchReq"))
						coId = fieldVal;
					else if (fieldName != null && fieldName.equalsIgnoreCase("PurchReqItem"))
					{
						coItem = fieldVal;
					}
					else if (fieldName != null && fieldName.equalsIgnoreCase("PurchReqDesc"))
						apiType.setCostObjectName(fieldVal);
				}
			}
			
			if (!coId.equals("") && !coItem.equals(""))
			{
				coId = coId + "-" + coItem;
				apiType.setCostObjectID(coId);
				apiType.setExternalKey(coId);
			}
			
			if (apiType.getParentCostObjectExternalKey() != null
					&& apiType.getCostObjectID() !=null ) {
				returnMap.put(apiType.getExternalKey(), apiType);				
			}
		}
		return returnMap;
	}
	
	public Map<String, BayerCommitmentLIAPIType> mapPOLineItemForImport(Map<String, Map<String, Object>> dataRows) 
			throws SystemException {
		Map<String, BayerCommitmentLIAPIType> returnMap = new HashMap<String, BayerCommitmentLIAPIType>();
		
		Set<String> rowKeys = dataRows.keySet();
		for (String rowKey : rowKeys){
			//logger.debug("Row Key is: " + rowKey);

			BayerCommitmentLIAPIType apiType = new BayerCommitmentLIAPIType();

			Map<String, Object> rowVal = dataRows.get(rowKey);			
			Set<String> fieldNames = rowVal.keySet();
			String coId = "";
			String coItem = "";
			String parentCoId = "";
			Double obligo = 0.0;
			for (String fieldName : fieldNames) {
				if (rowVal.get(fieldName)!=null)
				{
					String fieldVal = rowVal.get(fieldName).toString().trim();
					//logger.debug("Field Name: "+ fieldName);
					//logger.debug("Field Value: "+ fieldVal);
					if (fieldName != null && fieldName.equalsIgnoreCase("PurchOrd"))
					{
						apiType.setCommitmentID(fieldVal);
						apiType.setSAPPurchasingDocumentNumberID(fieldVal);
						coId = fieldVal;
					}
					else if (fieldName != null && fieldName.equalsIgnoreCase("PurchOrdItem"))
					{
						coItem = fieldVal;
						apiType.setSAPPurchasingDocumentLineItemNumber(fieldVal);
					}
					if (fieldName != null && fieldName.equalsIgnoreCase("WbsElement"))
					{
						if (fieldVal != null && !fieldVal.equals(""))
							parentCoId = fieldVal;
					}
					else if (fieldName != null && fieldName.equalsIgnoreCase("PmOrder"))
					{
						if (fieldVal != null && !fieldVal.equals(""))
							parentCoId = fieldVal;
					}
					else if (fieldName != null && fieldName.equalsIgnoreCase("PurchReq"))
						apiType.setSAPPurchaseRequisitionNumber(fieldVal);
					else if (fieldName != null && fieldName.equalsIgnoreCase("PurchReqItem"))
						apiType.setSAPPurchaseRequisitionLineItemNumbe(fieldVal);				
					else if (fieldName != null && fieldName.equalsIgnoreCase("CostElement"))
						apiType.setCostAccountID(fieldVal);
					else if (fieldName != null && fieldName.equalsIgnoreCase("CostElementDesc"))
						apiType.setCostAccountName(fieldVal);
					else if (fieldName != null && fieldName.equalsIgnoreCase("PoComCost"))
						apiType.setCostTransactionCurrency(Double.parseDouble(fieldVal));
					else if (fieldName != null && fieldName.equalsIgnoreCase("PoActCost"))
						apiType.setActualCostTransactionCurrency(Double.parseDouble(fieldVal));
					else if (fieldName != null && fieldName.equalsIgnoreCase("PoDocCurr"))
						apiType.setCurrencyTransactionCode(fieldVal);
					else if (fieldName != null && fieldName.equalsIgnoreCase("ExchangeRate"))
					{
						if (Double.parseDouble(fieldVal)!=0.00 
								&& Double.parseDouble(fieldVal)!=1.00)
							apiType.setConversionRateCostObjectCurrency(Double.parseDouble(fieldVal)/100);
							//apiType.setConversionRateCostObjectCurrency(100/Double.parseDouble(fieldVal));
						else
							apiType.setConversionRateCostObjectCurrency(1.00);
						
						apiType.setSAPExchangeRate(Double.parseDouble(fieldVal));
					}
					else if (fieldName != null && fieldName.equalsIgnoreCase("PoDate"))
					{
						if (fieldVal!=null&&!fieldVal.equals(""))
						try {
								apiType.setTransactionDate(this.getXMLDate(fieldVal));
						}
						catch (DatatypeConfigurationException e){
						}
					}
					else if (fieldName != null && fieldName.equalsIgnoreCase("FinalConf"))
					{ 
						if (fieldVal!=null && fieldVal.equalsIgnoreCase("X"))
							apiType.setFinalConfirmationID(GlobalConstants.EPC_API_ERROR_FLAG_Y);
						else
							apiType.setFinalConfirmationID(GlobalConstants.EPC_API_ERROR_FLAG_N);
					}
					else if (fieldName != null && fieldName.equalsIgnoreCase("DelFlag"))
					{
						if (fieldVal!=null && !fieldVal.equals(""))
						{
							apiType.setDeletionFlagID(fieldVal);
						}	
					}
					else if (fieldName != null && fieldName.equalsIgnoreCase("PurchOrdSeq"))
						apiType.setSAPPurcharsingOrderSeqNumber(fieldVal);
					else if (fieldName != null && fieldName.equalsIgnoreCase("PurchOrdQty"))
						apiType.setPOQuantity(Double.parseDouble(fieldVal));
					else if (fieldName != null && fieldName.equalsIgnoreCase("PurchOrdUnitm"))
					{
						if (fieldVal!=null && !fieldVal.equals(""))
							apiType.setUnitofMeasureID(fieldVal);						
					}
					else if (fieldName != null && fieldName.equalsIgnoreCase("PurchOrdPerc"))
						apiType.setPODistributionPCT(Double.parseDouble(fieldVal));
					else if (fieldName != null && fieldName.equalsIgnoreCase("Requester"))
						apiType.setRequestor(fieldVal);
					else if (fieldName != null && fieldName.equalsIgnoreCase("Receiver"))
						apiType.setReceiver(fieldVal);
					else if (fieldName != null && fieldName.equalsIgnoreCase("PurchOrdDesc"))
						apiType.setLineItemText(fieldVal);
				}
			}
			
			if (apiType.getActualCostTransactionCurrency()!=null)
			{	
				if (apiType.getCostTransactionCurrency()!=null)
					obligo = apiType.getCostTransactionCurrency() - apiType.getActualCostTransactionCurrency();
				else
					obligo = 0.0 - - apiType.getActualCostTransactionCurrency();;
			}
			else
			{
				if (apiType.getCostTransactionCurrency()!=null)
					obligo = apiType.getCostTransactionCurrency();
				else
					obligo = 0.0;
			}	
			if (obligo < 0.0 || (apiType.getFinalConfirmationID()!=null 
										&& apiType.getFinalConfirmationID().equals(GlobalConstants.EPC_API_ERROR_FLAG_Y))
							  || (apiType.getDeletionFlagID()!=null
							  			&& apiType.getDeletionFlagID().equals(GlobalConstants.SAP_PO_DELFLAG_L))
							  || (apiType.getDeletionFlagID()!=null
					  			&& apiType.getDeletionFlagID().equals(GlobalConstants.SAP_PO_DELFLAG_S)))					
				obligo = 0.0;
			
			obligo = obligo * apiType.getConversionRateCostObjectCurrency();
			apiType.setObligo(obligo);
			
			if (!coId.equals("") && !coItem.equals(""))
			{
				coId = coId + "-" + coItem;
				apiType.setCostObjectID(coId);
				coId = parentCoId + "-"+coId;
				apiType.setCostObjectExternalKey(coId);
				apiType.setExternalKey(coId);
			}
			if (apiType.getCostObjectExternalKey() != null) {
				returnMap.put(apiType.getCostObjectExternalKey(), apiType);				
			}
		}
		return returnMap;
	}
	
	public Map<String, BayerCommitmentLIAPIType> mapPRLineItemForImport(Map<String, Map<String, Object>> dataRows) 
			throws SystemException {
		Map<String, BayerCommitmentLIAPIType> returnMap = new HashMap<String, BayerCommitmentLIAPIType>();
		
		Set<String> rowKeys = dataRows.keySet();
		//int kCount =0;
		for (String rowKey : rowKeys){
			//logger.debug("Row Key is: " + rowKey);

			BayerCommitmentLIAPIType apiType = new BayerCommitmentLIAPIType();

			Map<String, Object> rowVal = dataRows.get(rowKey);			
			Set<String> fieldNames = rowVal.keySet();
			String pmId = "";
			String wbsId = "";
			Double obligo = 0.0;
			String prNumAndLine = "";
			for (String fieldName : fieldNames) {
				if (rowVal.get(fieldName)!=null)
				{
					String fieldVal = rowVal.get(fieldName).toString().trim();
					//logger.debug("Field Name: "+ fieldName);
					//logger.debug("Field Value: "+ fieldVal);
					if (fieldName != null && fieldName.equalsIgnoreCase("PurchReq"))
					{
						apiType.setCommitmentID(fieldVal);
						apiType.setSAPPurchaseRequisitionNumber(fieldVal);
						//coId = fieldVal;
					}
					else if (fieldName != null && fieldName.equalsIgnoreCase("PurchReqItem"))
					{
						//coItem = fieldVal;
						apiType.setSAPPurchaseRequisitionLineItemNumbe(fieldVal);
					}
					else if (fieldName != null && fieldName.equalsIgnoreCase("PmOrder"))
						pmId = fieldVal;
					else if (fieldName != null && fieldName.equalsIgnoreCase("WbsElement"))
						wbsId = fieldVal;
					else if (fieldName != null && fieldName.equalsIgnoreCase("PurchReq"))
						apiType.setSAPPurchaseRequisitionNumber(fieldVal);
					else if (fieldName != null && fieldName.equalsIgnoreCase("PurchOrd"))
						apiType.setSAPPurchasingDocumentNumberID(fieldVal);
					else if (fieldName != null && fieldName.equalsIgnoreCase("PurchOrdItem"))
						apiType.setSAPPurchasingDocumentLineItemNumber(fieldVal);
					else if (fieldName != null && fieldName.equalsIgnoreCase("CostElement"))
						apiType.setCostAccountID(fieldVal);
					else if (fieldName != null && fieldName.equalsIgnoreCase("CostElementDesc"))
						apiType.setCostAccountName(fieldVal);
					else if (fieldName != null && fieldName.equalsIgnoreCase("PrComCost"))
						apiType.setCostTransactionCurrency(Double.parseDouble(fieldVal));
					else if (fieldName != null && fieldName.equalsIgnoreCase("PrActCost"))
						apiType.setActualCostTransactionCurrency(Double.parseDouble(fieldVal));
					else if (fieldName != null && fieldName.equalsIgnoreCase("PrDocCurr"))
						apiType.setCurrencyTransactionCode(fieldVal);
			
					else if (fieldName != null && fieldName.equalsIgnoreCase("PrXchgRate"))
					{ 
						if (Double.parseDouble(fieldVal)!= 0.00
								&& Double.parseDouble(fieldVal)!= 1.00 )
							apiType.setConversionRateCostObjectCurrency(Double.parseDouble(fieldVal)/100);
							//apiType.setConversionRateCostObjectCurrency(100/Double.parseDouble(fieldVal));
						else
							apiType.setConversionRateCostObjectCurrency(1.00);
						apiType.setSAPExchangeRate(Double.parseDouble(fieldVal));
						//apiType.setConversionRateCostObjectCurrency(1.00);
					}

					else if (fieldName != null && fieldName.equalsIgnoreCase("PrDate"))
					{
						if (fieldVal!=null&&!fieldVal.equals(""))
						try {
								if(!fieldVal.equalsIgnoreCase("-  -")
										&& !fieldVal.equalsIgnoreCase("X   -  -"))
									apiType.setTransactionDate(this.getXMLDate(fieldVal));
						}
						catch (DatatypeConfigurationException e){
						}
					}
					
					else if (fieldName != null && fieldName.equalsIgnoreCase("FinalConf"))
						apiType.setFinalConfirmationID(fieldVal);
					
					else if (fieldName != null && fieldName.equalsIgnoreCase("DeleteInd"))
					{
						if (fieldVal!=null && fieldVal.equalsIgnoreCase(GlobalConstants.SAP_PR_DELIND_X))
							apiType.setDeletionFlagID(GlobalConstants.EPC_API_ERROR_FLAG_Y);
					}
					
					//added for Obligo calcl
					else if (fieldName != null && fieldName.equalsIgnoreCase("PrQuantity"))
						apiType.setPRQuantity(Double.parseDouble(fieldVal));
					
					else if (fieldName != null && fieldName.equalsIgnoreCase("PrUnit"))
					{ if (fieldVal != null && !fieldVal.equals(""))
						apiType.setUnitofMeasureID(fieldVal);
					}
					
					else if (fieldName != null && fieldName.equalsIgnoreCase("PrPerc"))
						apiType.setPODistributionPCT(Double.parseDouble(fieldVal));
					
					
					else if (fieldName != null && fieldName.equalsIgnoreCase("PurchReqSeq"))
					{ if (fieldVal != null && !fieldVal.equals(""))
						apiType.setSAPPurcharsingOrderSeqNumber(fieldVal);
					}
					else if (fieldName != null && fieldName.equalsIgnoreCase("PrProcsState"))
					{ if (fieldVal != null && !fieldVal.equals(""))
						apiType.setSAPPRProcessingState(fieldVal);
					}
					else if (fieldName != null && fieldName.equalsIgnoreCase("PrProcsStatu"))
					{ if (fieldVal != null && !fieldVal.equals(""))
						apiType.setSAPPRProcessingStatus(fieldVal);
					}
					
					else if (fieldName != null && fieldName.equalsIgnoreCase("Requester"))
						apiType.setRequestor(fieldVal);
					else if (fieldName != null && fieldName.equalsIgnoreCase("Receiver"))
						apiType.setReceiver(fieldVal);
					else if (fieldName != null && fieldName.equalsIgnoreCase("PurchReqDesc"))
						apiType.setLineItemText(fieldVal);
				}

			}
			
			if (pmId!=null && !pmId.equals("") && pmId.length()>4)
			{
				apiType.setCostObjectID(pmId);
				apiType.setCostObjectExternalKey(pmId);
			}
			else
			{
				apiType.setCostObjectID(wbsId);
				apiType.setCostObjectExternalKey(wbsId);
			}
			
			obligo = (apiType.getCostTransactionCurrency() - apiType.getActualCostTransactionCurrency()) * apiType.getConversionRateCostObjectCurrency()*100;
			
			if (obligo < 0.0 || (apiType.getDeletionFlagID()!=null 
								&& apiType.getDeletionFlagID().equalsIgnoreCase(GlobalConstants.EPC_API_ERROR_FLAG_Y)))
				obligo = 0.0;
			
			if (apiType.getSAPPRProcessingStatus()!=null 
					&& !apiType.getSAPPRProcessingStatus().equals("")
					&& apiType.getSAPPRProcessingStatus().equalsIgnoreCase(GlobalConstants.SAP_PR_PROC_STATUS_B))
				obligo = 0.0;
			
			if (apiType.getSAPPRProcessingStatus()!=null 
					&& !apiType.getSAPPRProcessingStatus().equals("")
					&& apiType.getSAPPRProcessingStatus().equalsIgnoreCase(GlobalConstants.SAP_PR_PROC_STATUS_N)
					&& !apiType.getSAPPRProcessingState().equalsIgnoreCase(GlobalConstants.SAP_PR_PROC_STATE_05))
				obligo = 0.0;
			
			apiType.setObligo(obligo);
			
			//kCount = kCount + 1;
			
			if (apiType.getCommitmentID()!= null && !apiType.getCommitmentID().equalsIgnoreCase("")) {
				prNumAndLine = apiType.getCommitmentID();
				if (apiType.getSAPPurchaseRequisitionLineItemNumbe()!=null && !apiType.getSAPPurchaseRequisitionLineItemNumbe().equalsIgnoreCase(""))
					prNumAndLine = prNumAndLine + "-" + apiType.getSAPPurchaseRequisitionLineItemNumbe();
				{	
						returnMap.put(prNumAndLine, apiType);			
				}
			}
		}
		return returnMap;
	}
	
	
	public Map<String, BayerCommitmentLIAPIType> mapPRLineItemForImportWithCO(Map<String, Map<String, Object>> dataRows) 
			throws SystemException {
		Map<String, BayerCommitmentLIAPIType> returnMap = new HashMap<String, BayerCommitmentLIAPIType>();
		
		Set<String> rowKeys = dataRows.keySet();
		for (String rowKey : rowKeys){
			//logger.debug("Row Key is: " + rowKey);

			BayerCommitmentLIAPIType apiType = new BayerCommitmentLIAPIType();

			Map<String, Object> rowVal = dataRows.get(rowKey);			
			Set<String> fieldNames = rowVal.keySet();
			String coId = "";
			String coItem = "";
			Double obligo = 0.0;
			for (String fieldName : fieldNames) {
				if (rowVal.get(fieldName)!=null)
				{
					String fieldVal = rowVal.get(fieldName).toString().trim();
					//logger.debug("Field Name: "+ fieldName);
					//logger.debug("Field Value: "+ fieldVal);
					if (fieldName != null && fieldName.equalsIgnoreCase("PurchReq"))
					{
						apiType.setCommitmentID(fieldVal);
						apiType.setSAPPurchasingDocumentNumberID(fieldVal);
						coId = fieldVal;
					}
					else if (fieldName != null && fieldName.equalsIgnoreCase("PurchReqItem"))
					{
						coItem = fieldVal;
						apiType.setSAPPurchasingDocumentLineItemNumber(fieldVal);
					}
					else if (fieldName != null && fieldName.equalsIgnoreCase("PurchReq"))
						apiType.setSAPPurchaseRequisitionNumber(fieldVal);
					else if (fieldName != null && fieldName.equalsIgnoreCase("CostElement"))
						apiType.setCostAccountID(fieldVal);
					else if (fieldName != null && fieldName.equalsIgnoreCase("CostElementDesc"))
						apiType.setCostAccountName(fieldVal);
					else if (fieldName != null && fieldName.equalsIgnoreCase("PrComCost"))
						apiType.setCostTransactionCurrency(Double.parseDouble(fieldVal));
					else if (fieldName != null && fieldName.equalsIgnoreCase("PrActCost"))
						apiType.setActualCostTransactionCurrency(Double.parseDouble(fieldVal));
					else if (fieldName != null && fieldName.equalsIgnoreCase("PrDocCurr"))
						apiType.setCurrencyTransactionCode(fieldVal);
					else if (fieldName != null && fieldName.equalsIgnoreCase("ExchangeRate"))
					{
						if (Double.parseDouble(fieldVal)!=0.00)
							apiType.setConversionRateCostObjectCurrency(1/Double.parseDouble(fieldVal));
						else
							apiType.setConversionRateCostObjectCurrency(0.00);
					}

					else if (fieldName != null && fieldName.equalsIgnoreCase("PrDate"))
					{
						if (fieldVal!=null&&!fieldVal.equals(""))
						try {
								if(!fieldVal.equalsIgnoreCase("    -  -  ")
									&& !fieldVal.equalsIgnoreCase("X   -  -  "))	
									apiType.setTransactionDate(this.getXMLDate(fieldVal));
						}
						catch (DatatypeConfigurationException e){
							apiType.setTransactionDate(null);
						}
					}
					else if (fieldName != null && fieldName.equalsIgnoreCase("FinalConf"))
						apiType.setFinalConfirmationID(fieldVal);
					else if (fieldName != null && fieldName.equalsIgnoreCase("DeleteInd"))
						apiType.setDeletionFlagID(fieldVal);
				}

			}
			
			if (!coId.equals("") && !coItem.equals(""))
			{
				coId = coId + "-" + coItem;
				apiType.setCostObjectID(coId);
				apiType.setCostObjectExternalKey(coId);
				apiType.setExternalKey(coId);
			}
			
			obligo = apiType.getCostTransactionCurrency() - apiType.getActualCostTransactionCurrency();
			if (obligo < 0.0)
				obligo = 0.0;
			
			apiType.setObligo(obligo);
			
			if (apiType.getCostObjectExternalKey() != null) {
				returnMap.put(apiType.getCostObjectExternalKey(), apiType);				
			}
		}
		return returnMap;
	}
	private XMLGregorianCalendar getXMLDate(String sapDate) throws DatatypeConfigurationException{
    	DatatypeFactory dFactory = DatatypeFactory.newInstance();
    	//sapDate = "2018-11-02";
    	if (!sapDate.equals("0000-00-00")&&!sapDate.equals(""))
    		return dFactory.newXMLGregorianCalendar(sapDate);
    	else 
    		return null;
	}
	//The following section was for testing purpose
    private  Map<String, Map<String, String>> readODataServiceEndPointSimpleHttp(
            String serviceEndPoint, String projectID, String instanceID, String startDate, String endDate) throws SystemException {
        logger.debug("Reading oDATA Service Endpoint: " + serviceEndPoint);
        
        Map<String, Map<String, String>> retResultList = null;
        
        try{
               String filters = prepareServiceFilter(serviceEndPoint, projectID, instanceID, startDate, endDate);
               
               DefaultHttpClient httpclient = new DefaultHttpClient();
               httpclient.getCredentialsProvider().setCredentials(
                       new AuthScope("by-dcgfd.de.bayer.cnb", 8000),
                       new UsernamePasswordCredentials(GlobalConstants.ODATA_SERVICE_USER, GlobalConstants.ODATA_SERVICE_PWD));

               HttpGet httpget = new HttpGet("http://by-dcgfd.de.bayer.cnb:8000/sap/opu/odata/BAY0/AD_ECOSYS_ODATA_SRV/WBS_ElementSet?%24format=json&%24filter=ProjectDefinition%20eq%20'A00GV-999990'and%20SystemId%20eq%20'X2R'");

               System.out.println("executing request" + httpget.getRequestLine());
               HttpResponse response = httpclient.execute(httpget);
               HttpEntity entity = response.getEntity();
               InputStream inStream = entity.getContent();
               String result = IOUtils.toString(inStream, StandardCharsets.UTF_8);
               System.out.println("Result are: " + result);
               int statusCode = response.getStatusLine()
                 .getStatusCode();

        }catch (Exception e) {
               logger.debug("Exception condition occurred: "+ e.getMessage());
               throw new SystemException(e);
        }
        
        return retResultList;	
    }
}
