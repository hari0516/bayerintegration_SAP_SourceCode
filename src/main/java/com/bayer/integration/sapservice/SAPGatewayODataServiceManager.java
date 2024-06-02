/**
 * 
 */
package com.bayer.integration.sapservice;

import java.util.List;
import java.util.Map;

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

/**
 * @author skdas
 *
 */
public interface SAPGatewayODataServiceManager {
	

	public Map<String, Map<String, Object>> readWBSHierarchy(String projectID, String instanceID) throws SystemException;
	public Map<String, Map<String, Object>> readPMOrders(String projectID, String instanceID) throws SystemException;
	public Map<String, Map<String, Object>> readActuals(String projectID, String instanceID, String startDate, String endDate) throws SystemException;
	public Map<String, Map<String, Object>> readPOHeader(String projectID, String instanceID) throws SystemException;
	public Map<String, Map<String, Object>> readPRHeader(String projectID, String instanceID) throws SystemException;	
	public Map<String, Map<String, Object>> readPOLineItems(String projectID, String instanceID) throws SystemException;
	public Map<String, Map<String, Object>> readPRLineItems(String projectID, String instanceID) throws SystemException;
	public Map<String, BayerWBSAPIType> mapWBSHierarchyForImport(Map<String, Map<String, Object>> dataRows, boolean isSub, String masterProjectId) 
			throws SystemException;
	public Map<String, BayerPMOrderAPIType> mapPMOrderForImport(Map<String, Map<String, Object>> dataRows) 
			throws SystemException;
	public Map<String, BayerPMOrderV2APIType> mapPMOrderV2ForImport(Map<String, Map<String, Object>> dataRows) 
			throws SystemException;
	
	public Map<String, BayerActualsAPIType> mapActualsForImport(Map<String, Map<String, Object>> dataRows) 
			throws SystemException;
	public Map<String, BayerPOPRHeadersAPIType> mapPOHeaderForImport(String projectHirarchyPathId, Map<String, Map<String, Object>> dataRows) 
			throws SystemException;
	public Map<String, BayerPOPRHeadersAPIType> mapPRHeaderForImport(String projectHirarchyPathId, Map<String, Map<String, Object>> dataRows) 
			throws SystemException;
	
	public Map<String, BayerPOPRHeadersV2APIType> mapPRHeaderV2ForImport(String projectHirarchyPathId, Map<String, Map<String, Object>> dataRows) 
			throws SystemException;
	public Map<String, BayerPOPRHeadersV2APIType> mapPOHeaderV2ForImport(String projectHirarchyPathId, Map<String, Map<String, Object>> dataRows) 
			throws SystemException;
	
	public Map<String, BayerCommitmentLIAPIType> mapPOLineItemForImport(Map<String, Map<String, Object>> dataRows) 
			throws SystemException;
	
	public Map<String, BayerCommitmentLICOAPIType> mapPOLineItemCOForImport(Map<String, Map<String, Object>> dataRows) 
			throws SystemException;
	public Map<String, BayerCommitmentLICOV2APIType> mapPOLineItemCOV2ForImport(Map<String, Map<String, Object>> dataRows) 
			throws SystemException;
	
	public Map<String, BayerCommitmentLIAPIType> mapPRLineItemForImport(Map<String, Map<String, Object>> dataRows) 
			throws SystemException;
	public Map<String, BayerCommitmentLICOAPIType> mapPRLineItemCOForImport(Map<String, Map<String, Object>> dataRows) 
			throws SystemException;
}
