/**
 * 
 */
package com.bayer.integration.properties;

import com.ecosys.properties.AppProperties;

/**
 * @author sdas
 *
 */
public class GlobalConstants {
	
	protected AppProperties appProperties;
	public void setAppProperties(AppProperties appProperties) {
		this.appProperties = appProperties;

		ODATA_SERVICE_ROOT = this.appProperties.getProperty("odata.service.root");
		ODATA_SERVICE_USER = this.appProperties.getProperty("odata.service.user");
		ODATA_SERVICE_PWD = this.appProperties.getProperty("odata.service.password");
		ODATA_SERVICE_ENTITY_CONTAINER = this.appProperties.getProperty("odata.service.entitycontainer");

		EPC_REST_PROTOCOL = this.appProperties.getProperty("epc.rest.protocal");
		EPC_REST_SERVER = this.appProperties.getProperty("epc.rest.server");
		EPC_REST_PORT = this.appProperties.getProperty("epc.rest.port");
		EPC_REST_BASEURI = this.appProperties.getProperty("epc.rest.baseuri");
		EPC_REST_BASEURI2 = this.appProperties.getProperty("epc.rest.baseuri2");
		EPC_REST_USERNAME = this.appProperties.getProperty("epc.rest.username");
		EPC_REST_PASSWORD = this.appProperties.getProperty("epc.rest.password");
		EPC_REST_HIERARCHY_PATH_SEPERATOR = this.appProperties.getProperty("epc.rest.hierarchypathseperator");
		EPC_REST_COSTOBJECTTYPE_WBS_INTERNAL_ID = this.appProperties.getProperty("epc.rest.costobjecttypewbsinternalid");
		EPC_REST_COSTOBJECTTYPE_PMO_INTERNAL_ID = this.appProperties.getProperty("epc.rest.costobjecttypepmointernalid");
		
		EPC_REST_DEFAULT_COSTOBJECT_STATUS = this.appProperties.getProperty("epc.rest.default.costobjectstatus");
		EPC_REST_COMMITMENT_TYPE_PO = this.appProperties.getProperty("epc.rest.commitmenttypepo");
		EPC_REST_COMMITMENT_TYPE_PR = this.appProperties.getProperty("epc.rest.commitmenttypepr");
		
		EPC_REST_BATCHSIZE = Integer.parseInt(this.appProperties.getProperty("epc.rest.batchsize"));
		EPC_JDBC_BATCHSIZE = Integer.parseInt(this.appProperties.getProperty("epc.jdbc.batchsize"));
		EPC_PROJECT_ID = "";
		EPC_TRANSACTION_CATEGORY_VENDOR="Vendor";
		EPC_TASK_CATEGORY_VENDOR="Vendor";
		EPC_REST_IMPORT_SAP_WBS = appProperties.getProperty("epc.rest.import.sap.wbs");
		EPC_REST_IMPORT_SAP_PMO = appProperties.getProperty("epc.rest.import.sap.pmo");
		EPC_REST_IMPORT_SAP_PMO_V2 = appProperties.getProperty("epc.rest.import.sap.pmo2");
		EPC_REST_IMPORT_SAP_PMO_TRAN = appProperties.getProperty("epc.rest.import.sap.pmo.tran");
		EPC_REST_IMPORT_SAP_ACT = appProperties.getProperty("epc.rest.import.sap.act");
		EPC_REST_IMPORT_SAP_DC_CO = appProperties.getProperty("epc.rest.import.sap.dc_co");
		EPC_REST_IMPORT_SAP_CA = appProperties.getProperty("epc.rest.import.sap.ca");
		EPC_REST_IMPORT_SAP_TRC = appProperties.getProperty("epc.rest.import.sap.trc");
		EPC_REST_IMPORT_SAP_TAC = appProperties.getProperty("epc.rest.import.sap.tac");
		EPC_REST_IMPORT_SAP_PO_HDR = appProperties.getProperty("epc.rest.import.sap.po.header");
		EPC_REST_IMPORT_SAP_PO_HDR_V2 = appProperties.getProperty("epc.rest.import.sap.po2.header");
		EPC_REST_IMPORT_SAP_PO_LNITM_CO = appProperties.getProperty("epc.rest.import.sap.po.line_item_co");
		EPC_REST_IMPORT_SAP_PO_LNITM_CO_V2 = appProperties.getProperty("epc.rest.import.sap.po.line_item_co2");
		EPC_REST_IMPORT_SAP_PO_LNITM = appProperties.getProperty("epc.rest.import.sap.po.line_item");
		EPC_REST_IMPORT_SAP_PO_LNITM_PRD = appProperties.getProperty("epc.rest.import.sap.po.line_item_prd");
		EPC_REST_IMPORT_SAP_PO_LNITM_HIS = appProperties.getProperty("epc.rest.import.sap.po.line_item_his");
		//EPC_REST_IMPORT_SAP_PO_LNITM_HISTG = appProperties.getProperty("epc.rest.import.sap.po.line_item_histg");
		EPC_REST_IMPORT_SAP_PR_HDR = appProperties.getProperty("epc.rest.import.sap.pr.header");
		EPC_REST_IMPORT_SAP_PR_HDR_V2 = appProperties.getProperty("epc.rest.import.sap.pr2.header");
		EPC_REST_IMPORT_SAP_PR_LNITM_CO = appProperties.getProperty("epc.rest.import.sap.pr.line_item_co");
		EPC_REST_IMPORT_SAP_PR_LNITM = appProperties.getProperty("epc.rest.import.sap.pr.line_item");
		EPC_REST_READ_ECOSYS_PROJECTS = appProperties.getProperty("epc.rest.read.ecosys.projects");
		EPC_REST_UPDATE_ECOSYS_PROJECTS = appProperties.getProperty("epc.rest.update.ecosys.projects");
		EPC_REST_DELETE_SAP_PO_LNITM = appProperties.getProperty("epc.rest.delete.sap.po.line_item");		
		EPC_REST_TRACK_SAP_PO_LNITM = appProperties.getProperty("epc.rest.track.sap.po.line_item_track");
		EPC_REST_TRACK_INI_SAP_PO_LNITM = appProperties.getProperty("epc.rest.track.sap.po.line_item_track_ini");
		EPC_REST_DELETE_SAP_PR_LNITM = appProperties.getProperty("epc.rest.delete.sap.pr.line_item");
		EPC_REST_DELETE_SAP_ACT_PERIOD = appProperties.getProperty("epc.rest.delete.sap.act.period");
		EPC_REST_CALC_WBS_ID = appProperties.getProperty("epc.rest.calc.wbs.id");
		EPC_REST_POST_PROCESS = appProperties.getProperty("epc.rest.pp.ecosys.post_process");
		EPC_REST_IMPORT_INTEGRATION_LOG = appProperties.getProperty("epc.rest.import.log.integration");
		EPC_REST_POST_PROCESS_INI = appProperties.getProperty("epc.rest.pp.ecosys.post_process_ini");
		//EPC_REST_POST_PROCESS_NEW = appProperties.getProperty("epc.rest.pp.ecosys.post_process_new");
		//EPC_REST_POST_PROCESS_NEW_INI = appProperties.getProperty("epc.rest.pp.ecosys.post_process_new_ini");
		EPC_REST_POST_PROCESS_PMO = appProperties.getProperty("epc.rest.pp.ecosys.post_process_pmo");
		EPC_REST_PRE_PROCESS_PMO = appProperties.getProperty("epc.rest.pp.ecosys.pre_process_pmo");
		EPC_REST_EVT_CCL = appProperties.getProperty("epc.rest.evt.ccl");
		EPC_REST_IMPORT_CATEGORY = appProperties.getProperty("epc.rest.import.sap.cat");
		
		//Project Copy Related
		EPC_REST_PROJECT_FOR_COPY_LIST = appProperties.getProperty("epc.rest.pc.ecosys.project_for_copy_list");
		EPC_REST_PROJECT_COPY_FOR_DR_LIST = appProperties.getProperty("epc.rest.pc.ecosys.project_copy_for_dr_list");
		EPC_REST_PROJECT_COPY_CREATE = appProperties.getProperty("epc.rest.pc.ecosys.project_copy_create");
		EPC_REST_PROJECT_COPY_DELETE = appProperties.getProperty("epc.rest.pc.ecosys.project_copy_delete");
		EPC_REST_PROJECT_COPY_RESTORE = appProperties.getProperty("epc.rest.pc.ecosys.project_copy_restore");
		EPC_REST_PROJECT_COPY_USER_LIMIT = appProperties.getProperty("epc.rest.project_user_copy_limit");
		EPC_REST_PROJECT_COPY_SYSTEM_LIMIT = appProperties.getProperty("epc.rest.project_system_copy_limit");

		SAP_ACTUAL_START_DATE = appProperties.getProperty("sap.actual.startdate");
		
		DEBUGMODE = (appProperties.getProperty("debug.mode") != null && appProperties.getProperty("debug.mode").equals("true"));
		//HEN4
		PROCESS_SAP_YEARCLOSE = (appProperties.getProperty("process.sap.yearclose") != null && appProperties.getProperty("process.sap.yearclose").equals("true"));
		EPC_YEAR_CLOSE_DATE = appProperties.getProperty("epc.rest.yearclosedate");
		EPC_TEST_MISSING_PARENT = appProperties.getProperty("epc.rest.test.missingparent");
		
		SKIP_EVT_CCL_INTERFACE = (appProperties.getProperty("skip.evt.ccl.interface") != null && appProperties.getProperty("skip.evt.ccl.interface").equals("true"));
		SKIP_SAP_WBS_INTERFACE = (appProperties.getProperty("skip.sap.wbs.interface") != null && appProperties.getProperty("skip.sap.wbs.interface").equals("true"));
		SKIP_SAP_PMO_INTERFACE = (appProperties.getProperty("skip.sap.pmo.interface") != null && appProperties.getProperty("skip.sap.pmo.interface").equals("true"));
		SKIP_SAP_ACT_INTERFACE = (appProperties.getProperty("skip.sap.act.interface") != null && appProperties.getProperty("skip.sap.act.interface").equals("true"));
		SKIP_SAP_POH_INTERFACE = (appProperties.getProperty("skip.sap.poh.interface") != null && appProperties.getProperty("skip.sap.poh.interface").equals("true"));
		SKIP_SAP_POL_INTERFACE = (appProperties.getProperty("skip.sap.pol.interface") != null && appProperties.getProperty("skip.sap.pol.interface").equals("true"));
		SKIP_SAP_PRH_INTERFACE = (appProperties.getProperty("skip.sap.prh.interface") != null && appProperties.getProperty("skip.sap.prh.interface").equals("true"));
		SKIP_SAP_PRL_INTERFACE = (appProperties.getProperty("skip.sap.prl.interface") != null && appProperties.getProperty("skip.sap.prl.interface").equals("true"));
		SKIP_SAP_PP_INTERFACE = (appProperties.getProperty("skip.sap.pp.interface") != null && appProperties.getProperty("skip.sap.pp.interface").equals("true"));

		SKIP_ECO_PRJCP_CREAT_INTERFACE = (appProperties.getProperty("skip.eco.prjcpcre.interface") != null && appProperties.getProperty("skip.eco.prjcpcre.interface").equals("true"));
		SKIP_ECO_PRJCP_DELETE_INTERFACE = (appProperties.getProperty("skip.eco.prjcpdel.interface") != null && appProperties.getProperty("skip.eco.prjcpdel.interface").equals("true"));
		SKIP_ECO_PRJCP_RESTORE_INTERFACE = (appProperties.getProperty("skip.eco.prjcpres.interface") != null && appProperties.getProperty("skip.eco.prjcpres.interface").equals("true"));

		
		SKIP_LOG = (appProperties.getProperty("skip.errorlog") != null && appProperties.getProperty("skip.errorlog").equals("true"));
		IS_LIVE_SAP = (appProperties.getProperty("process.sap.live") != null && appProperties.getProperty("process.sap.live").equals("true"));
		SKIP_POST_PROCESS = (appProperties.getProperty("skip.postprocess") != null && appProperties.getProperty("skip.postprocess").equals("true"));
		
		//Aug 2021: CR2 & 3 Related
		EPC_REST_READ_ECOSYS_WBS = appProperties.getProperty("epc.rest.read.ecosys.wbs");
		//EPC_REST_DEFAULT_WBS = appProperties.getProperty("epc.rest.default.wbs");
		SKIP_SAP_MON_INTERFACE = (appProperties.getProperty("skip.sap.mon.interface") != null && appProperties.getProperty("skip.sap.mon.interface").equals("true"));
		
		STAGING_RECORD_STATUS_LOADED = appProperties.getProperty("staging.record.status.loaded");
		STAGING_RECORD_STATUS_FAILED = appProperties.getProperty("staging.record.status.failed");
		STAGING_RECORD_STATUS_SKIPPED = appProperties.getProperty("staging.record.status.skipped");
		EPC_API_ERROR_FLAG_Y = "Y";
		EPC_API_ERROR_FLAG_N="N";
		EPC_API_PROJECT_TYPE_MASTER = appProperties.getProperty("epc.rest.projecttypemaster");
		EPC_API_PROJECT_TYPE_SUB = appProperties.getProperty("epc.rest.projecttypesub");
		EPC_API_SNAPSHOT_DESCRIPTION = appProperties.getProperty("epc.rest.snapshot.description");
		EPC_API_WBS_DIRECT_CHARGE_ID = "Direct Charge";
		EPC_API_WBS_DIRECT_CHARGE_NAME = "Direct Charge Cost Object";
		//wbs Error queries
		EPC_WBS_API_ERROR_SELECT_BY_PROJECT = "SELECT * FROM fmuser.ecointg_wbs_api_error " + 
				"WHERE Status = 'F' and RootCostObjectID= ";
		
		EPC_WBS_API_ERROR_BATCH_INSERT = "INSERT INTO fmuser.ecointg_wbs_api_error " + 
				"(Status, ErrorMsg, CostObjectID, CostObjectName, "
				+ "CostObjectStatus, ObjectClass, LocationID, ResponsibleCostCenter, "
				+ "PersonResponsible, SAPProjectTypeID, CostObjectTypeName, "
				+ "HierarchyPathID, ExternalKey, ProfitCenter, SAPDeleteFlag, SAPStatus, RootCostObjectID) "
				+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		EPC_WBS_API_ERROR_BATCH_UPDATE = "UPDATE fmuser.ecointg_wbs_api_error " +
				"SET Status = ? "+ 
				"WHERE Id = ?";
		
		//Pmo Error queries
		EPC_PMO_API_ERROR_SELECT_BY_PROJECT = "SELECT * FROM fmuser.ecointg_pmo_api_error " + 
				"WHERE Status = 'F' and Retired = 'N' and CostObjectTypeName = 'PM Order' and RootCostObjectID= ";
		
		EPC_PMO_API_ERROR_BATCH_INSERT = "INSERT INTO fmuser.ecointg_pmo_api_error " + 
				"(Status, ErrorMsg, CostObjectID, CostObjectName, "
				+ "PMEstimatedCost, PMActualCost, CostObjectStatus, "
				+ "CostObjectTypeName, ParentCostObjectExternalKey, ExternalKey, PmStatus, RootCostObjectID, Retired) "
				+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		EPC_PMO_API_ERROR_BATCH_UPDATE = "UPDATE fmuser.ecointg_pmo_api_error " +
				"SET Status = ? "+ 
				"WHERE Retired = 'N' AND Id = ?";

		EPC_PMO_API_ERROR_BATCH_UPDATE_RETIRED = "UPDATE fmuser.ecointg_pmo_api_error " +
				"SET Retired = 'Y' "+ 
				"WHERE Id = ?";
		
		//Poh Error queries
		EPC_POH_API_ERROR_SELECT_BY_PROJECT = "SELECT * FROM fmuser.ecointg_poh_api_error " + 
				"WHERE Status = 'F' and RootCostObjectID= ";
		
		EPC_POH_API_ERROR_BATCH_INSERT = "INSERT INTO fmuser.ecointg_poh_api_error " + 
				"(Status, ErrorMsg, TaskID, TaskName, "
				+ "VendorID, VendorName, Requestor, Receiver, "
				+ "LastInvoiceDate, CommittedDate, OwnerCostObjectExternalKey, "
				+ "CommitmentTypeHierarchyPathID, RootCostObjectID) "
				+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		EPC_POH_API_ERROR_BATCH_UPDATE = "UPDATE fmuser.ecointg_poh_api_error " +
				"SET Status = ? "+ 
				"WHERE Id = ?";
		
		//Polco Error queries
		EPC_POLCO_API_ERROR_SELECT_BY_PROJECT = "SELECT * FROM fmuser.ecointg_polco_api_error " + 
				"WHERE Status = 'F' and Retired = 'N' and CostObjectTypeName = 'PO Line Item' and RootCostObjectID= ";
		
		EPC_POLCO_API_ERROR_BATCH_INSERT = "INSERT INTO fmuser.ecointg_pmo_api_error " + 
				"(Status, ErrorMsg, CostObjectID, CostObjectName, "
				+ "CostObjectStatus, CostObjectTypeName, ParentCostObjectExternalKey, "
				+ "ExternalKey, RootCostObjectID, Retired) "
				+ "VALUES (?,?,?,?,?,?,?,?,?,?)";
		
		EPC_POLCO_API_ERROR_BATCH_UPDATE = "UPDATE fmuser.ecointg_polco_api_error " +
				"SET Status = ? "+ 
				"WHERE Retired = 'N' AND Id = ?";

		EPC_POLCO_API_ERROR_BATCH_UPDATE_RETIRED = "UPDATE fmuser.ecointg_polco_api_error " +
				"SET Retired = 'Y' "+ 
				"WHERE Id = ?";
		
		//Pol error queries
		EPC_POL_API_ERROR_SELECT_BY_PROJECT = "SELECT * FROM fmuser.ecointg_pol_api_error " + 
				"WHERE Status = 'F' and RootCostObjectID= ";
		
		EPC_POL_API_ERROR_BATCH_INSERT = "INSERT INTO fmuser.ecointg_pol_api_error " + 
				"(Status, ErrorMsg, CommitmentID, "
				+ "SAPPurchasingDocumentNumberID, SAPPurchasingDocumentLineItemNumber, "
				+ "CostObjectID,CostObjectName, SAPWBSElement, CostAccountID, CostAccountName, "
				+ "CostCostObjectCurrency, CurrencyTransactionCode, CurrencyCostObjectCode, "
				+ "VersionID, ConversionRateCostObjectCurrency, TransactionDate, "
				+ "DeletionFlagID, ExternalKey, SAPPurchaseRequisitionNumber, "
				+ "SAPPurchaseRequisitionLineItemNumber, CostObjectExternalKey, "
				+ "CostTransactionCurrency,FinalConfirmationID,"
				+ "ActualCostTransactionCurrency,Obligo, "
				+ "POQuantity, UnitofMeasureID, PODistributionPCT, SAPPurcharsingOrderSeqNumber, "
				+ "SAPExchangeRate, RootCostObjectID) "
				+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		EPC_POL_API_ERROR_BATCH_DELETE = "DELETE FROM fmuser.ecointg_pol_api_error " +
				"WHERE RootCostObjectID = ";
		
		//Prh Error queries
		EPC_PRH_API_ERROR_SELECT_BY_PROJECT = "SELECT * FROM fmuser.ecointg_prh_api_error " + 
				"WHERE Status = 'F' and RootCostObjectID= ";
		
		EPC_PRH_API_ERROR_BATCH_INSERT = "INSERT INTO fmuser.ecointg_prh_api_error " + 
				"(Status, ErrorMsg, TaskID, TaskName, "
				+ "VendorID, VendorName, Requestor, Receiver, "
				+ "LastInvoiceDate, CommittedDate, OwnerCostObjectExternalKey, "
				+ "CommitmentTypeHierarchyPathID, RootCostObjectID) "
				+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		EPC_PRH_API_ERROR_BATCH_UPDATE = "UPDATE fmuser.ecointg_prh_api_error " +
				"SET Status = ? "+ 
				"WHERE Id = ?";
		
		//Prl error queries
		EPC_PRL_API_ERROR_SELECT_BY_PROJECT = "SELECT * FROM fmuser.ecointg_prl_api_error " + 
				"WHERE Status = 'F' and RootCostObjectID= ";
		
		EPC_PRL_API_ERROR_BATCH_INSERT = "INSERT INTO fmuser.ecointg_prl_api_error " + 
				"(Status, ErrorMsg, CommitmentID, "
				+ "SAPPurchasingDocumentNumberID, SAPPurchasingDocumentLineItemNumber, "
				+ "CostObjectID, CostObjectName,SAPWBSElement, CostAccountID, CostAccountName, "
				+ "CostCostObjectCurrency, CurrencyTransactionCode, CurrencyCostObjectCode, "
				+ "VersionID, ConversionRateCostObjectCurrency, TransactionDate, "
				+ "DeletionFlagID, ExternalKey, SAPPurchaseRequisitionNumber, "
				+ "SAPPurchaseRequisitionLineItemNumber, CostObjectExternalKey, "
				+ "CostTransactionCurrency, ActualCostTransactionCurrency,"
				+ "Obligo,"
				+ "PRQuantity, UnitofMeasureID, PODistributionPCT, SAPPurcharsingOrderSeqNumber, "
				+ "SAPPRProcessingState, SAPPRProcessingStatus, SAPExchangeRate, RootCostObjectID) "
				+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		EPC_PRL_API_ERROR_BATCH_UPDATE = "UPDATE fmuser.ecointg_prl_api_error " +
				"SET Status = ? "+ 
				"WHERE Id = ?";
		
		EPC_PRL_API_ERROR_BATCH_DELETE = "DELETE FROM fmuser.ecointg_prl_api_error " +
				"WHERE RootCostObjectID = ";
		
		//Actuals error queries
		EPC_ACT_API_ERROR_SELECT_BY_PROJECT = "SELECT * FROM fmuser.ecointg_actuals_api_error " + 
				"WHERE Status = 'F' and RootCostObjectID= ";
		
		EPC_ACT_API_ERROR_BATCH_INSERT = "INSERT INTO fmuser.ecointg_actuals_api_error " + 
				"(Status, ErrorMsg, CostObjectID, SAPWBSElement, "
				+ "CostCostObjectCurrency, CurrencyCostObjectCode, "
				+ "CostTransactionCurrency, CurrencyTransactionCode, CostAccountID, CostAccountName, "
				+ "TransactionDate, FIDocumentNumber, CommitmentID, "
				+ "VendorID, VendorName, ConversionRateCostObjectCurrency, "
				+ "ExternalKey, VersionID, CostObjectExternalKey, "
				+ "ControllingArea, ActualReferenceHeaderText, ActualReferenceDoc,"
				+ "SAPPurchasingDocumentLineItemNumber, SAPUniqueID, "
				+ "OffsettingAccountNumber, SAPPurcharsingOrderSeqNumber, SAPAccrualID, PostingRow, SAPExchangeRate"
				+ "RootCostObjectID) "
				+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		EPC_ACT_API_ERROR_BATCH_UPDATE = "UPDATE fmuser.ecointg_actuals_api_error " +
				"SET Status = ? "+ 
				"WHERE Id = ?";	
		
		//Cost Accounts Error queries
		EPC_CA_API_ERROR_SELECT_BY_PROJECT = "SELECT * FROM fmuser.ecointg_ca_api_error " + 
				"WHERE Status = 'F' and Retired = 'N' and RootCostObjectID= ";
		EPC_CA_API_ERROR_SELECT_BY_PROJECT_ACT = "SELECT * FROM fmuser.ecointg_ca_api_error " + 
				"WHERE Status = 'F' and Retired = 'N' and SAPSource = 'ACT' and RootCostObjectID= ";
		EPC_CA_API_ERROR_SELECT_BY_PROJECT_POL = "SELECT * FROM fmuser.ecointg_ca_api_error " + 
				"WHERE Status = 'F' and Retired = 'N' and SAPSource = 'POL' and RootCostObjectID= ";
		EPC_CA_API_ERROR_SELECT_BY_PROJECT_PRL = "SELECT * FROM fmuser.ecointg_ca_api_error " + 
				"WHERE Status = 'F' and Retired = 'N' and SAPSource = 'PRL' and RootCostObjectID= ";
		
		EPC_CA_API_ERROR_BATCH_INSERT = "INSERT INTO fmuser.ecointg_ca_api_error " + 
				"(Status, ErrorMsg, CostAccountID, CostAccountName, "
				+ "Active, StartDate, EndDate, PathID,"
				+ "SAPSource, RootCostObjectID, Retired) "
				+ "VALUES (?,?,?,?,?,?,?,?,?,?,?)";
		
		EPC_CA_API_ERROR_BATCH_UPDATE = "UPDATE fmuser.ecointg_ca_api_error " +
				"SET Status = ? "+ 
				"WHERE Retired = 'N' AND Id = ?";

		EPC_CA_API_ERROR_BATCH_UPDATE_RETIRED = "UPDATE fmuser.ecointg_ca_api_error " +
				"SET Retired = 'Y' "+ 
				"WHERE Id = ?";
		
		
		//DCO - Direct Charge Cost Objects Error queries
		EPC_DCO_API_ERROR_SELECT_BY_PROJECT = "SELECT * FROM fmuser.ecointg_dco_api_error " + 
				"WHERE Status = 'F' and Retired = 'N' and CostObjectTypeName = 'Direct Charge' and RootCostObjectID= ";
		
		EPC_DCO_API_ERROR_BATCH_INSERT = "INSERT INTO fmuser.ecointg_dco_api_error " + 
				"(Status, ErrorMsg, CostObjectID, CostObjectName, "
				+ "CostObjectStatus, CostObjectTypeName, ParentCostObjectExternalKey, "
				+ "ExternalKey, RootCostObjectID, Retired) "
				+ "VALUES (?,?,?,?,?,?,?,?,?,?)";
		
		EPC_DCO_API_ERROR_BATCH_UPDATE = "UPDATE fmuser.ecointg_dco_api_error " +
				"SET Status = ? "+ 
				"WHERE Retired = 'N' AND Id = ?";

		EPC_DCO_API_ERROR_BATCH_UPDATE_RETIRED = "UPDATE fmuser.ecointg_dco_api_error " +
				"SET Retired = 'Y' "+ 
				"WHERE Id = ?";
		
		//Projects Error queries
		EPC_PRJ_API_ERROR_SELECT_BY_PROJECT = "SELECT * FROM fmuser.ecointg_prj_api_error " + 
				"WHERE Status = 'F' and Retired = 'N' and ProjectID= ";
		
		EPC_PRJ_API_ERROR_BATCH_INSERT = "INSERT INTO fmuser.ecointg_prj_api_error " + 
				"(Status, ErrorMsg, ProjectID, CostObjectName, "
				+ "CostObjectStatus, CostObjectTypeName, CostObjectHierarchyLevel, "
				+ "SAPSystemID, InternalID, ProjectStatusID, ProjectTypeID, "
				+ "ParentCostObjectID, SAPLastRunDate, RootCostObjectID, Retired) "
				+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		EPC_PRJ_API_ERROR_BATCH_UPDATE = "UPDATE fmuser.ecointg_prj_api_error " +
				"SET Status = ? "+ 
				"WHERE Retired = 'N' AND Id = ?";

		EPC_PRJ_API_ERROR_BATCH_UPDATE_RETIRED = "UPDATE fmuser.ecointg_prj_api_error " +
				"SET Retired = 'Y' "+ 
				"WHERE Id = ?";
	}

	
	public static String ODATA_SERVICE_ROOT;
	public static String ODATA_SERVICE_USER;
	public static String ODATA_SERVICE_PWD;
	public static String ODATA_SERVICE_ENTITY_CONTAINER;
	
	public static String EPC_REST_PROTOCOL;
	public static String EPC_REST_SERVER;
	public static String EPC_REST_PORT;
	public static String EPC_REST_BASEURI;
	public static String EPC_REST_BASEURI2;
	
	public static String EPC_REST_USERNAME;
	public static String EPC_REST_PASSWORD;
	
	public static int EPC_REST_BATCHSIZE;
	public static int EPC_JDBC_BATCHSIZE;

	
	public static String EPC_REST_IMPORT_SAP_WBS;
	public static String EPC_REST_IMPORT_SAP_PMO;
	public static String EPC_REST_IMPORT_SAP_PMO_V2;
	public static String EPC_REST_IMPORT_SAP_PMO_TRAN;
	public static String EPC_REST_IMPORT_SAP_DC_CO;
	public static String EPC_REST_IMPORT_SAP_CA;
	public static String EPC_REST_IMPORT_SAP_TRC;
	public static String EPC_REST_IMPORT_SAP_TAC;
	
	public static String EPC_REST_IMPORT_SAP_ACT;
	public static String EPC_REST_IMPORT_SAP_PO_HDR;
	public static String EPC_REST_IMPORT_SAP_PO_HDR_V2;
	public static String EPC_REST_IMPORT_SAP_PO_LNITM_CO;
	public static String EPC_REST_IMPORT_SAP_PO_LNITM_CO_V2;
	public static String EPC_REST_IMPORT_SAP_PO_LNITM;
	public static String EPC_REST_IMPORT_SAP_PO_LNITM_PRD;
	public static String EPC_REST_IMPORT_SAP_PO_LNITM_HIS;
	//public static String EPC_REST_IMPORT_SAP_PO_LNITM_HISTG;
	public static String EPC_REST_IMPORT_SAP_PR_HDR;
	public static String EPC_REST_IMPORT_SAP_PR_HDR_V2;
	public static String EPC_REST_IMPORT_SAP_PR_LNITM_CO;
	public static String EPC_REST_IMPORT_SAP_PR_LNITM;
	public static String EPC_REST_READ_ECOSYS_PROJECTS;
	public static String EPC_REST_READ_ECOSYS_WBS;
	public static String EPC_REST_DEFAULT_WBS = "";
	
	public static String EPC_REST_UPDATE_ECOSYS_PROJECTS;
	public static String EPC_REST_DELETE_SAP_PO_LNITM;
	public static String EPC_REST_DELETE_SAP_PR_LNITM;
	public static String EPC_REST_DELETE_SAP_ACT_PERIOD;
	public static String EPC_REST_TRACK_SAP_PO_LNITM;
	public static String EPC_REST_TRACK_INI_SAP_PO_LNITM;
	public static String EPC_REST_CALC_WBS_ID;
	public static String EPC_REST_POST_PROCESS;
	public static String EPC_REST_IMPORT_INTEGRATION_LOG;
	public static String EPC_REST_POST_PROCESS_INI;
	public static String EPC_REST_POST_PROCESS_PMO;
	public static String EPC_REST_PRE_PROCESS_PMO;
	
	public static String EPC_REST_EVT_CCL;
	public static String EPC_REST_IMPORT_CATEGORY;

	
	public static String EPC_REST_PROJECT_FOR_COPY_LIST;
	public static String EPC_REST_PROJECT_COPY_FOR_DR_LIST;
	
	public static String EPC_REST_PROJECT_COPY_CREATE;
	public static String EPC_REST_PROJECT_COPY_DELETE;
	public static String EPC_REST_PROJECT_COPY_RESTORE;
	
	// phase 4 VH
	public static boolean DEBUGMODE=false;
	public static boolean SKIP_EVT_CCL_INTERFACE=false;
	public static boolean PROCESS_SAP_YEARCLOSE=false;
	public static boolean SKIP_EPC_NEW_VENDOR=true;
	
	public static boolean SKIP_SAP_WBS_INTERFACE=false;
	public static boolean SKIP_SAP_PMO_INTERFACE=false;
	public static boolean SKIP_SAP_ACT_INTERFACE=false;
	public static boolean SKIP_SAP_POH_INTERFACE=false;
	public static boolean SKIP_SAP_POL_INTERFACE=false;
	public static boolean SKIP_SAP_PRH_INTERFACE=false;
	public static boolean SKIP_SAP_PRL_INTERFACE=false;
	public static boolean SKIP_SAP_PP_INTERFACE=false;
	public static boolean SKIP_SAP_PHS_INTERFACE=false;
		
	public static boolean SKIP_ECO_PRJCP_CREAT_INTERFACE=false;
	public static boolean SKIP_ECO_PRJCP_DELETE_INTERFACE=false;
	public static boolean SKIP_ECO_PRJCP_RESTORE_INTERFACE=false;
	
	public static boolean SKIP_LOG=false;
	public static boolean SKIP_POST_PROCESS=false;
	
	public static int EXPORT_ECOSYS_PROJECT_SUCCESS=100;
	public static int EXPORT_ECOSYS_PROJECT_FAILED=105;

	public static int IMPORT_SAP_WBS_SUCCESS=1000;
	public static int IMPORT_SAP_WBS_SKIPPED=1001;
	public static int IMPORT_SAP_WBS_FAILED=1005;
	public static int IMPORT_SAP_PMO_SUCCESS=2000;
	public static int IMPORT_SAP_PMO_FAILED=2005;
	public static int IMPORT_SAP_ACT_SUCCESS=3000;
	public static int IMPORT_SAP_ACT_FAILED=3005;
	public static int IMPORT_SAP_POH_SUCCESS=4000;
	public static int IMPORT_SAP_POH_FAILED=4005;
	public static int IMPORT_SAP_POL_SUCCESS=5000;
	public static int IMPORT_SAP_POL_FAILED=5005;
	public static int IMPORT_SAP_PRH_SUCCESS=6000;
	public static int IMPORT_SAP_PRH_FAILED=6005;
	public static int IMPORT_SAP_PRL_SUCCESS=7000;
	public static int IMPORT_SAP_PRL_FAILED=7005;
	public static int IMPORT_SAP_PHS_SUCCESS=7500;
	public static int IMPORT_SAP_PHS_FAILED=7505;
	public static int IMPORT_POST_PROCESS_SUCCESS=8000;
	public static int IMPORT_POST_PROCESS_FAILED=8005;
	
	public static int PROJECT_COPY_CREATE_SUCCESS=9000;
	public static int PROJECT_COPY_CREATE_FAILED=9005;
	public static int PROJECT_COPY_DELETE_SUCCESS=9100;
	public static int PROJECT_COPY_DELETE_FAILED=9105;
	public static int PROJECT_COPY_RESTORE_SUCCESS=9200;
	public static int PROJECT_COPY_RESTORE_FAILED=9205;
	
	//Aug 2021 - Phase 3 CR3 Snapshoting for Master project
	public static int IMPORT_SAP_MON_SUCCESS=9300;
	public static int IMPORT_SAP_MON_FAILED=9305;

	public static int EPC_CREATE_NEW_VENDOR_SUCCESS=10000;
	public static int EPC_CREATE_NEW_VENDOR_FAIL=10005;
	public static int EPC_EVT_CCL_SUCCESS=11000;
	public static int EPC_EVT_CCL_FAIL=11005;
	
	public static  String STAGING_RECORD_STATUS_LOADED;
	public static  String STAGING_RECORD_STATUS_FAILED;
	public static  String STAGING_RECORD_STATUS_SKIPPED;
	
	public static String SAP_ACT_LINE_ITEM_IMPORT_INTERFACE_NAME="SAP_ACT_IMPORT";
	public static String SAP_PO_HEADER_IMPORT_INTERFACE_NAME="SAP_PO_HEADER_IMPORT";
	public static String SAP_PO_LINE_ITM_IMPORT_INTERFACE_NAME="SAP_PO_LINE_ITEM_IMPORT";
	
	public static String EPC_REST_COSTOBJECTTYPE_WBS_INTERNAL_ID;
	public static String EPC_REST_COSTOBJECTTYPE_PMO_INTERNAL_ID;
	public static String EPC_REST_COSTOBJECTTYPE_DC="Direct Charge";
	public static String EPC_REST_COSTOBJECTTYPE_POC="PO Line Item";
	public static String EPC_REST_HIERARCHY_PATH_SEPERATOR;
	public static String EPC_REST_DEFAULT_COSTOBJECT_STATUS;	
	public static String EPC_REST_COMMITMENT_TYPE_PO;
	public static String EPC_REST_COMMITMENT_TYPE_PR;
	
	public static String EPC_DATASOURCE_USER_NAME;
	public static String EPC_DATASOURCE_PASSWORD;
	public static String EPC_DATASOURCE_URL;
	public static String EPC_DATASOURCE_DRIVER_CLASS_BANE;
	
	public static String EPC_WBS_API_ERROR_SELECT_BY_PROJECT;
	public static String EPC_WBS_API_ERROR_BATCH_INSERT;
	public static String EPC_WBS_API_ERROR_BATCH_UPDATE;
	public static String EPC_WBS_API_ERROR_BATCH_UPDATE_RETIRED;
	
	public static String EPC_PMO_API_ERROR_SELECT_BY_PROJECT;
	public static String EPC_PMO_API_ERROR_BATCH_INSERT;
	public static String EPC_PMO_API_ERROR_BATCH_UPDATE;
	public static String EPC_PMO_API_ERROR_BATCH_UPDATE_RETIRED;
	
	public static String EPC_POH_API_ERROR_SELECT_BY_PROJECT;
	public static String EPC_POH_API_ERROR_BATCH_INSERT;
	public static String EPC_POH_API_ERROR_BATCH_UPDATE;
	
	public static String EPC_PRH_API_ERROR_SELECT_BY_PROJECT;
	public static String EPC_PRH_API_ERROR_BATCH_INSERT;
	public static String EPC_PRH_API_ERROR_BATCH_UPDATE;

	
	public static String EPC_POLCO_API_ERROR_SELECT_BY_PROJECT;
	public static String EPC_POLCO_API_ERROR_BATCH_INSERT;
	public static String EPC_POLCO_API_ERROR_BATCH_UPDATE;
	public static String EPC_POLCO_API_ERROR_BATCH_UPDATE_RETIRED;
	
	public static String EPC_POL_API_ERROR_SELECT_BY_PROJECT;
	public static String EPC_POL_API_ERROR_BATCH_INSERT;
	public static String EPC_POL_API_ERROR_BATCH_UPDATE;
	public static String EPC_POL_API_ERROR_BATCH_DELETE;

	public static String EPC_PRL_API_ERROR_SELECT_BY_PROJECT;
	public static String EPC_PRL_API_ERROR_BATCH_INSERT;
	public static String EPC_PRL_API_ERROR_BATCH_UPDATE;
	public static String EPC_PRL_API_ERROR_BATCH_DELETE;
	
	public static String EPC_ACT_API_ERROR_SELECT_BY_PROJECT;
	public static String EPC_ACT_API_ERROR_BATCH_INSERT;
	public static String EPC_ACT_API_ERROR_BATCH_UPDATE;
	public static String EPC_API_ERROR_FLAG_Y;
	public static String EPC_API_ERROR_FLAG_N;
	
	public static String EPC_CA_API_ERROR_SELECT_BY_PROJECT;
	public static String EPC_CA_API_ERROR_SELECT_BY_PROJECT_ACT;
	public static String EPC_CA_API_ERROR_SELECT_BY_PROJECT_PRL;
	public static String EPC_CA_API_ERROR_SELECT_BY_PROJECT_POL;
	public static String EPC_CA_API_ERROR_BATCH_INSERT;
	public static String EPC_CA_API_ERROR_BATCH_UPDATE;
	public static String EPC_CA_API_ERROR_BATCH_UPDATE_RETIRED;
	
	public static String EPC_PRJ_API_ERROR_SELECT_BY_PROJECT;
	public static String EPC_PRJ_API_ERROR_BATCH_INSERT;
	public static String EPC_PRJ_API_ERROR_BATCH_UPDATE;
	public static String EPC_PRJ_API_ERROR_BATCH_UPDATE_RETIRED;
	
	public static String EPC_DCO_API_ERROR_SELECT_BY_PROJECT;
	public static String EPC_DCO_API_ERROR_BATCH_INSERT;
	public static String EPC_DCO_API_ERROR_BATCH_UPDATE;
	public static String EPC_DCO_API_ERROR_BATCH_UPDATE_RETIRED;
	
	public static String EPC_API_PROJECT_TYPE_MASTER;
	public static String EPC_API_PROJECT_TYPE_SUB;
	public static String EPC_API_WBS_DIRECT_CHARGE_ID;
	public static String EPC_API_WBS_DIRECT_CHARGE_NAME;
	public static String SAP_ACTUAL_START_DATE;
	public static String EPC_API_SNAPSHOT_DESCRIPTION;
	
	public static final String APPLICATION_JSON = "application/json";
	public static final String APPLICATION_XML = "application/xml";
	public static final String APPLICATION_ATOM_XML = "application/atom+xml";
	public static final String HTTP_METHOD_GET = "GET";
	public static final String METADATA = "$metadata"; 
	
	public static final String ODATA_WBS_ELEMENT_SET = "WBS_ElementSet";
	public static final String ODATA_PM_ORDER_SET = "PM_OrderSet";
	public static final String ODATA_PROJECT_ACTUALS_SET = "Project_ActualsSet";
	public static final String ODATA_PO_HEADER_SET = "PurchOrd_HdrSet";
	public static final String ODATA_PO_LINE_ITEM_SET = "PurchOrd_ItemsSet";
	public static final String ODATA_PR_HEADER_SET = "PurchReq_HdrSet";
	public static final String ODATA_PR_LINE_ITEM_SET = "PurchReq_ItemsSet";
	
	public static String EPC_PROJECT_ID;
	public static final String EPC_TRANSACTION_CATEGORY_PARAM = "Category";
	public static final String EPC_TASK_CATEGORY_PARAM = "Category";
	public static final String EPC_PROJECT_PARAM = "ProjectId";

	public static String EPC_TRANSACTION_CATEGORY_VENDOR;
	public static String EPC_VENDOR_ID;
	public static String EPC_VENDOR_NAME;
	
	public static final String EPC_PROJECT_COPY_TYPE_PARAM = "CopyType";
	public static final String EPC_PROJECT_COPY_TYPE_SYSTEM = "S";
	public static final String EPC_PROJECT_COPY_TYPE_USER = "U";
	public static final String EPC_PROJECT_COPY_STATUS_DELETE = "CD";
	
	public static final String EPC_ROOTCOSTOBJECT_PARAM = "RootCostObject";
	public static final String EPC_SUBPROJECT_PARAM = "SubProjectID";
	public static final String EPC_PROJECTPERIOD_PARAM ="ProjectPeriod";
	public static final String EPC_SNAPSHOTDESCRIPTION_PARAM ="Description";
	public static String EPC_TASK_CATEGORY_VENDOR;
	public static boolean IS_LIVE_SAP = false;
	public static final String SAP_SOURCE_ACTUAL = "ACT";
	public static final String SAP_SOURCE_POL = "POL";
	public static final String SAP_SOURCE_PRL = "PRL";
	public static final String SAP_PR_PROC_STATUS_B = "B";
	public static final String SAP_PR_PROC_STATUS_N = "N";
	//public static final String SAP_PO_PROC_STATE_RELES = "05";
	public static final String SAP_PR_PROC_STATE_05 = "05";
	public static final String SAP_OFFSET_ACNO_ACCRUAL = "2403000";
	public static final String SAP_PO_DELFLAG_L = "L";
	public static final String SAP_PO_DELFLAG_S = "S";
	public static final String SAP_PR_DELIND_X = "X";
	public static final String EPC_PMOTran_Version = "Commitment - PMOrder Stg";
	
	public static final String EPC_REST_COSTOBJECTTYPE_WBS = "WBS";
	public static final String EPC_CO_Changed = "--COCHANGED--";
	public static final String EPC_CO_RE_Changed = "-2-";
	public static int errorMsgSize =255;
	public static String EPC_PROJECT_ID_PROCESSED = "";
	
	//Project Copy Limit
	public static String EPC_REST_PROJECT_COPY_USER_LIMIT;
	public static String EPC_REST_PROJECT_COPY_SYSTEM_LIMIT;
	
	//Aug 2021: CR2 & 3
	public static boolean SKIP_SAP_MON_INTERFACE=false;
	
	//June 2023 Phase IV
	
	public static String EPC_YEAR_CLOSE_DATE;
	public static String EPC_TEST_MISSING_PARENT;

};
