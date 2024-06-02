/**
 * 
 */
package com.bayer.integration.persistence;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.sql.Connection;
import java.sql.Statement;

import javax.sql.DataSource;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import com.bayer.integration.properties.GlobalConstants;
import java.math.BigDecimal;


/**
 * @author sdas, pwang
 *
 */
public class StagingDatabaseManagerImpl implements StagingDatabaseManager {	
	private static Logger logger = Logger.getLogger(StagingDatabaseManagerImpl.class);
	
	
	private JdbcTemplate epcJDBCTemplate;
	protected Connection connection;
	private DataSource dataSource;
	private int id;
	
	public void setEpcDataSource(DataSource ds){
		this.dataSource = ds;
		epcJDBCTemplate = new JdbcTemplate(ds);
		//epcJDBCTemplate.setDataSource(ds);
		
		//Disabled DB Connection for Error Log tables
		//this.setConnection(ds);
		
		//this.testConnection(connection);
		//logger.info("Connected to EcoSys Database");		
	}
	
	public JdbcTemplate getEpcJDBCTemplate()
	{
		return this.epcJDBCTemplate;
	}
	
	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	public void setConnection(DataSource ds){
        try
        {
    		this.connection = ds.getConnection();

        } catch (Exception e)
        {
			logger.error("Connect to EcoSys Database Failed", e);			
        }

	}
    
	public Connection getConnection(){
		return this.connection;
	}

	
	public void insertWbs(WbsApiErrorDAO wbs) {
		String sql = GlobalConstants.EPC_WBS_API_ERROR_BATCH_INSERT;
		//epcJDBCTemplate = new JdbcTemplate(this.datasource);
		this.epcJDBCTemplate.update(sql, new Object[] {wbs.getStatus(),
				wbs.getErrorMsg(),wbs.getCostObjectID(), wbs.getCostObjectName(),
				wbs.getCostObjectStatus(), wbs.getObjectClass(),wbs.getLocationID(),
				wbs.getResponsibleCostCenter(), wbs.getPersonResponsible(), wbs.getSAPProjectTypeID(),
				wbs.getCostObjectTypeName(), wbs.getHierarchyPathID(), wbs.getExternalKey(),
				wbs.getProfitCenter(),wbs.getSAPDeleteFlagID(),wbs.getSAPStatus(),wbs.getRootCostObjectID()});
		/*
		EPC_WBS_API_ERROR_BATCH_INSERT = "INSERT INTO fmuser.ecointg_wbs_api_error " + 
				"(Status, ErrorMsg, CostObjectID, CostObjectName, "
				+ "CostObjectStatus, ObjectClass, LocationID, ResponsibleCostCenter, "
				+ "PersonResponsible, ProjectTypeID, CostObjectTypeName, "
				+ "HierarchyPathID, ExternalKey, ProfitCenter, SAPDeleteFlag, SAPStatus, RootCostObjectID) "
				+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";		*/
	}
	
    @SuppressWarnings("rawtypes")
    public List<WbsApiErrorDAO> findFailedWbsByProject(String projectID){
    	epcJDBCTemplate = new JdbcTemplate(dataSource);
        String sql = GlobalConstants.EPC_WBS_API_ERROR_SELECT_BY_PROJECT + "'" + projectID + "'";
        List<WbsApiErrorDAO> epcErrors = new ArrayList<WbsApiErrorDAO>();
        List<Map<String, Object>> rows = epcJDBCTemplate.queryForList(sql);
        for (Map row : rows) {
        	WbsApiErrorDAO epcError = new WbsApiErrorDAO();
            //epcError.setId(int(row.get("ID"))));
        	epcError.setId(Integer.parseInt(String.valueOf(row.get("Id"))));
            epcError.setStatus((String)row.get("Status"));
            epcError.setErrorMsg((String)row.get("ErrorMsg"));
            epcError.setCostObjectID((String)row.get("CostObjectID"));
            epcError.setCostObjectName((String)row.get("CostObjectName"));
            epcError.setCostObjectStatus((String)row.get("CostObjectStatus"));
            epcError.setObjectClass((String)row.get("ObjectClass"));
            epcError.setLocationID((String)row.get("LocationID"));            
            epcError.setResponsibleCostCenter((String)row.get("ResponsibleCostCenter"));     
            epcError.setPersonResponsible((String)row.get("PersonResponsible"));
            epcError.setSAPProjectTypeID((String)row.get("SAPProjectTypeID"));           
            epcError.setCostObjectTypeName((String)row.get("CostObjectTypeName"));
            epcError.setHierarchyPathID((String)row.get("HierarchyPathID"));
            epcError.setExternalKey((String)row.get("ExternalKey"));
            epcError.setProfitCenter((String)row.get("ProfitCenter"));
            epcError.setSAPDeleteFlagID((String)row.get("SAPDeleteFlag"));
            epcError.setSAPStatus((String)row.get("SAPStatus"));
            epcError.setRootCostObjectID((String)row.get("RootCostObjectID"));
            //epcError.set
            epcErrors.add(epcError);
        }
        return epcErrors;
    }
    
    public void insertWbsBatch(final List<WbsApiErrorDAO> wbses){
 
        //epcJDBCTemplate = new JdbcTemplate(dataSource);

          String sql = GlobalConstants.EPC_WBS_API_ERROR_BATCH_INSERT;
          //boolean isNewError = isNew;
          epcJDBCTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
          
            public void setValues(PreparedStatement ps, int i) throws SQLException {         
            	WbsApiErrorDAO wbs = wbses.get(i);
                ps.setString(1, wbs.getStatus());
                ps.setString(2, wbs.getErrorMsg() );
                ps.setString(3, wbs.getCostObjectID());
                ps.setString(4, wbs.getCostObjectName());
                ps.setString(5, wbs.getCostObjectStatus());
                ps.setString(6, wbs.getObjectClass());
                ps.setString(7, wbs.getLocationID());
                ps.setString(8, wbs.getResponsibleCostCenter());
                ps.setString(9, wbs.getPersonResponsible());
                ps.setString(10, wbs.getSAPProjectTypeID());
                ps.setString(11, wbs.getCostObjectTypeName());
                ps.setString(12, wbs.getHierarchyPathID());
                ps.setString(13, wbs.getExternalKey());
                ps.setString(14, wbs.getProfitCenter());
                ps.setString(15, wbs.getSAPDeleteFlagID());
                ps.setString(16, wbs.getSAPStatus());
                ps.setString(17, wbs.getRootCostObjectID());
            }
            public int getBatchSize() {
                return wbses.size();
            }
          });
        }
    

    public int[] updateWbsBatch(final List<WbsApiErrorDAO> wbses){
 
        epcJDBCTemplate = new JdbcTemplate(dataSource);

          String sql = GlobalConstants.EPC_WBS_API_ERROR_BATCH_UPDATE;
          //boolean isNewError = isNew;
          int[] updateCounts = epcJDBCTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
          
            public void setValues(PreparedStatement ps, int i) throws SQLException {         
            	WbsApiErrorDAO wbs = wbses.get(i);
                ps.setString(1, wbs.getStatus());
                ps.setLong(2, wbs.getId());
            }
            public int getBatchSize() {
                return wbses.size();
            }
          });
          return updateCounts;
        }


    
    @SuppressWarnings("rawtypes")
    public List<PmOrderApiErrorDAO> findFailedPmoByProject(String projectID){
    	epcJDBCTemplate = new JdbcTemplate(dataSource);
        String sql = GlobalConstants.EPC_PMO_API_ERROR_SELECT_BY_PROJECT + "'" + projectID + "'";
        List<PmOrderApiErrorDAO> epcErrors = new ArrayList<PmOrderApiErrorDAO>();
        List<Map<String, Object>> rows = epcJDBCTemplate.queryForList(sql);
        for (Map row : rows) {
        	PmOrderApiErrorDAO epcError = new PmOrderApiErrorDAO();
            //epcError.setId(Integer.parseInt(String.valueOf(row.get("ID"))));
        	epcError.setId(Integer.parseInt(String.valueOf(row.get("Id"))));
            epcError.setStatus((String)row.get("Status"));
            epcError.setCostObjectID((String)row.get("CostObjectID"));
            epcError.setCostObjectName((String)row.get("CostObjectName"));
            epcError.setCostObjectStatus((String)row.get("CostObjectStatus"));
            epcError.setCostObjectTypeName((String)row.get("CostObjectTypeName"));
            epcError.setErrorMsg((String)row.get("ErrorMsg"));
            epcError.setExternalKey((String)row.get("ExternalKey"));
            //epcError.setHierarchyPathID((String)row.get("HierarchyPathID"));
            epcError.setParentCostObjectExternalKey((String)row.get("ParentCostObjectExternalKey"));
            epcError.setPMActualCost(((BigDecimal)row.get("PMActualCost")).doubleValue());
            epcError.setPMEstimatedCost(((BigDecimal)row.get("PMEstimatedCost")).doubleValue());
            epcError.setPmStatus((String)row.get("PmStatus"));
            epcError.setRootCostObjectID((String)row.get("RootCostObjectID"));
            epcErrors.add(epcError);
        }
        return epcErrors;
    }
    
    public void insertPmoBatch(final List<PmOrderApiErrorDAO> pmOrders){
   	 
        //epcJDBCTemplate = new JdbcTemplate(dataSource);

          String sql = GlobalConstants.EPC_PMO_API_ERROR_BATCH_INSERT;

          epcJDBCTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
          
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                PmOrderApiErrorDAO wbs = pmOrders.get(i);
                ps.setString(1, wbs.getStatus());
                ps.setString(2, wbs.getErrorMsg() );
                ps.setString(3, wbs.getExternalKey());
                ps.setString(4, wbs.getCostObjectName());
                ps.setDouble(5, wbs.getPMEstimatedCost());
                ps.setDouble(6, wbs.getPMActualCost());
                ps.setString(7, wbs.getCostObjectStatus());
                ps.setString(8, wbs.getCostObjectTypeName());
                ps.setString(9, wbs.getParentCostObjectExternalKey());
                ps.setString(10, wbs.getExternalKey());
                ps.setString(11, wbs.getPmStatus());
                ps.setString(12, wbs.getRootCostObjectID());
                ps.setString(13, GlobalConstants.EPC_API_ERROR_FLAG_N);
            }

            
            public int getBatchSize() {
                return pmOrders.size();
            }
          });
        }
    
    public int[] updatePmoBatch(final List<PmOrderApiErrorDAO> pmOrders){
    	 
        //epcJDBCTemplate = new JdbcTemplate(dataSource);

          String sql = GlobalConstants.EPC_PMO_API_ERROR_BATCH_UPDATE;
          //boolean isNewError = isNew;
          int[] updateCounts = epcJDBCTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
          
            public void setValues(PreparedStatement ps, int i) throws SQLException {         
            	PmOrderApiErrorDAO apiError = pmOrders.get(i);
                ps.setString(1, apiError.getStatus());
                ps.setLong(2, apiError.getId());
            }
            public int getBatchSize() {
                return pmOrders.size();
            }
          });
          return updateCounts;
        }
    
    public int[] updatePmoBatchRetired(final List<PmOrderApiErrorDAO> pmOrders){
   	 
        //epcJDBCTemplate = new JdbcTemplate(dataSource);

          String sql = GlobalConstants.EPC_PMO_API_ERROR_BATCH_UPDATE_RETIRED;
          //boolean isNewError = isNew;
          int[] updateCounts = epcJDBCTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
          
            public void setValues(PreparedStatement ps, int i) throws SQLException {         
            	PmOrderApiErrorDAO apiError = pmOrders.get(i);
                //ps.setString(1, apiError.getStatus());
                ps.setLong(1, apiError.getId());
            }
            public int getBatchSize() {
                return pmOrders.size();
            }
          });
          return updateCounts;
        }
    
    @SuppressWarnings("rawtypes")
    public List<PohApiErrorDAO> findFailedPohByProject(String projectID, String strSql){
    	epcJDBCTemplate = new JdbcTemplate(dataSource);
        String sql = strSql + "'" + projectID + "'";
        List<PohApiErrorDAO> epcErrors = new ArrayList<PohApiErrorDAO>();
        List<Map<String, Object>> rows = epcJDBCTemplate.queryForList(sql);
        for (Map row : rows) {
        	PohApiErrorDAO epcError = new PohApiErrorDAO();
            //epcError.setId(Integer.parseInt(String.valueOf(row.get("ID"))));
        	epcError.setId(Integer.parseInt(String.valueOf(row.get("Id"))));
            epcError.setStatus((String)row.get("Status"));
            epcError.setErrorMsg((String)row.get("ErrorMsg"));
            epcError.setTaskID((String)row.get("TaskID"));
            epcError.setTaskName((String)row.get("TaskName"));
            
            if(row.get("ActualCosts")!=null)
            	epcError.setActualCosts(((BigDecimal)row.get("ActualCosts")).doubleValue());
            
            epcError.setCommitmentTypeHierarchyPathID((String)row.get("CommitmentTypeHierarchyPathID"));
            
            if (row.get("CommittedDate")!=null)
            	epcError.setCommittedDate(toXMLGregorianCalendar((java.sql.Date)row.get("CommittedDate")));
            
            if (row.get("LastInvoiceDate")!=null)
            	epcError.setLastInvoiceDate(toXMLGregorianCalendar((java.sql.Date)row.get("LastInvoiceDate")));
            
            
            epcError.setOwnerCostObjectExternalKey((String)row.get("OwnerCostObjectExternalKey"));
            epcError.setReceiver((String)row.get("Receiver"));
            epcError.setRequestor((String)row.get("Requestor"));
            epcError.setVendorID((String)row.get("VendorID"));
            epcError.setVendorName((String)row.get("VendorName"));
            
            if(row.get("WorkingForecastCosts")!=null)
            	epcError.setWorkingForecastCosts(((BigDecimal)row.get("WorkingForecastCosts")).doubleValue());
            
            epcError.setRootCostObjectID((String)row.get("RootCostObjectID"));
            epcErrors.add(epcError);
        }
        return epcErrors;
    }
	
    public void insertPohBatch(final List<PohApiErrorDAO> pohs, String sql){
      	 
        //epcJDBCTemplate = new JdbcTemplate(dataSource);

          epcJDBCTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
          
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                PohApiErrorDAO errorDAO = pohs.get(i);
                ps.setString(1, errorDAO.getStatus());
                ps.setString(2, errorDAO.getErrorMsg() );
                ps.setString(3, errorDAO.getTaskID());
                ps.setString(4, errorDAO.getTaskName());
                //ps.setDouble(5, errorDAO.getWorkingForecastCosts());
               // ps.setDouble(6, errorDAO.getActualCosts());
                ps.setString(5, errorDAO.getVendorID());
                ps.setString(6, errorDAO.getVendorName());
                ps.setString(7, errorDAO.getRequestor());
                ps.setString(8, errorDAO.getReceiver());
                
                if (errorDAO.getLastInvoiceDate()!=null)
                	ps.setDate(9, new java.sql.Date(errorDAO.getLastInvoiceDate()
                			.toGregorianCalendar().getTimeInMillis()));
                else
                	ps.setDate(9, null);
                
                if (errorDAO.getCommittedDate()!=null)
                	ps.setDate(10, new java.sql.Date(errorDAO.getCommittedDate()
                			.toGregorianCalendar().getTimeInMillis()));
                else
                	ps.setDate(10, null);
                
                ps.setString(11, errorDAO.getOwnerCostObjectExternalKey());
                ps.setString(12, errorDAO.getCommitmentTypeHierarchyPathID());
                ps.setString(13, errorDAO.getRootCostObjectID());
            }

            
            public int getBatchSize() {
                return pohs.size();
            }
          });
        }
    
    public int[] updatePohBatch(final List<PohApiErrorDAO> pohs, String sql){
    	 
        //epcJDBCTemplate = new JdbcTemplate(dataSource);

          //String sql = GlobalConstants.EPC_POH_API_ERROR_BATCH_UPDATE;
          //boolean isNewError = isNew;
          int[] updateCounts = epcJDBCTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
          
            public void setValues(PreparedStatement ps, int i) throws SQLException {         
            	PohApiErrorDAO apiError = pohs.get(i);
                ps.setString(1, apiError.getStatus());
                ps.setLong(2, apiError.getId());
            }
            public int getBatchSize() {
                return pohs.size();
            }
          });
          return updateCounts;
        }
    
    @SuppressWarnings("rawtypes")
    public List<PolcoApiErrorDAO> findFailedPolcoByProject(String projectID){
    	epcJDBCTemplate = new JdbcTemplate(dataSource);
        String sql = GlobalConstants.EPC_POLCO_API_ERROR_SELECT_BY_PROJECT + "'" + projectID + "'";
        List<PolcoApiErrorDAO> epcErrors = new ArrayList<PolcoApiErrorDAO>();
        List<Map<String, Object>> rows = epcJDBCTemplate.queryForList(sql);
        for (Map row : rows) {
        	PolcoApiErrorDAO epcError = new PolcoApiErrorDAO();
            //epcError.setId(Integer.parseInt(String.valueOf(row.get("ID"))));
        	epcError.setId(Integer.parseInt(String.valueOf(row.get("Id"))));
            epcError.setStatus((String)row.get("Status"));
            epcError.setCostObjectID((String)row.get("CostObjectID"));
            epcError.setCostObjectName((String)row.get("CostObjectName"));
            epcError.setCostObjectStatus((String)row.get("CostObjectStatus"));
            epcError.setCostObjectTypeName((String)row.get("CostObjectTypeName"));
            epcError.setErrorMsg((String)row.get("ErrorMsg"));
            epcError.setExternalKey((String)row.get("ExternalKey"));
            //epcError.setHierarchyPathID((String)row.get("HierarchyPathID"));
            epcError.setParentCostObjectExternalKey((String)row.get("ParentCostObjectExternalKey"));
            epcError.setRootCostObjectID((String)row.get("RootCostObjectID"));
            epcErrors.add(epcError);
        }
        return epcErrors;
    }
    
    public void insertPolcoBatch(final List<PolcoApiErrorDAO> polcos, String sql){
      	 
        //epcJDBCTemplate = new JdbcTemplate(dataSource);

          //String sql = GlobalConstants.EPC_POLCO_API_ERROR_BATCH_INSERT;

          epcJDBCTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
          
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                PolcoApiErrorDAO wbs = polcos.get(i);
                ps.setString(1, wbs.getStatus());
                ps.setString(2, wbs.getErrorMsg() );
                ps.setString(3, wbs.getCostObjectID());
                ps.setString(4, wbs.getCostObjectName());
                ps.setString(5, wbs.getCostObjectStatus());
                ps.setString(6, wbs.getCostObjectTypeName());
                ps.setString(7, wbs.getParentCostObjectExternalKey());
                ps.setString(8, wbs.getExternalKey());
                ps.setString(9, wbs.getRootCostObjectID());
                ps.setString(10, GlobalConstants.EPC_API_ERROR_FLAG_N);
            }

            
            public int getBatchSize() {
                return polcos.size();
            }
          });
        }
    
    public int[] updatePolcoBatch(final List<PolcoApiErrorDAO> polcos, String sql){
    	 
        //epcJDBCTemplate = new JdbcTemplate(dataSource);

         // String sql = GlobalConstants.EPC_POLCO_API_ERROR_BATCH_UPDATE;
          //boolean isNewError = isNew;
          int[] updateCounts = epcJDBCTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
          
            public void setValues(PreparedStatement ps, int i) throws SQLException {         
            	PolcoApiErrorDAO apiError = polcos.get(i);
                ps.setString(1, apiError.getStatus());
                ps.setLong(2, apiError.getId());
            }
            public int getBatchSize() {
                return polcos.size();
            }
          });
          return updateCounts;
        }
    
    public int[] updatePolcoBatchRetired(final List<PolcoApiErrorDAO> polcos){
   	 
        //epcJDBCTemplate = new JdbcTemplate(dataSource);

          String sql = GlobalConstants.EPC_POLCO_API_ERROR_BATCH_UPDATE_RETIRED;
          //boolean isNewError = isNew;
          int[] updateCounts = epcJDBCTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
          
            public void setValues(PreparedStatement ps, int i) throws SQLException {         
            	PolcoApiErrorDAO apiError = polcos.get(i);
                //ps.setString(1, apiError.getStatus());
                ps.setLong(1, apiError.getId());
            }
            public int getBatchSize() {
                return polcos.size();
            }
          });
          return updateCounts;
        }
    
    @SuppressWarnings("rawtypes")
    public List<PolApiErrorDAO> findFailedPolByProject(String projectID, String sql){
    	epcJDBCTemplate = new JdbcTemplate(dataSource);
        String sql2 = sql + "'" + projectID + "'";
        List<PolApiErrorDAO> epcErrors = new ArrayList<PolApiErrorDAO>();
        List<Map<String, Object>> rows = epcJDBCTemplate.queryForList(sql2);
        for (Map row : rows) {
        	PolApiErrorDAO epcError = new PolApiErrorDAO();
            //epcError.setId(Integer.parseInt(String.valueOf(row.get("ID"))));
        	epcError.setId(Integer.parseInt(String.valueOf(row.get("Id"))));
            epcError.setStatus((String)row.get("Status"));
            epcError.setErrorMsg((String)row.get("ErrorMsg"));
            //epcError.setCostObjectHierarchyPathID((String)row.get("CostObjectHierarchyPathID"));
            epcError.setCommitmentID((String)row.get("CommitmentID"));
            if (row.get("ConversionRateCostObjectCurrency")!=null)
            	epcError.setConversionRateCostObjectCurrency(((BigDecimal)row.get
            			("ConversionRateCostObjectCurrency")).doubleValue());
                        
            epcError.setCostAccountID((String)row.get("CostAccountID"));
            epcError.setCostAccountName((String)row.get("CostAccountName"));
            
            if(row.get("CostCostObjectCurrency")!=null)
            	epcError.setCostCostObjectCurrency(((BigDecimal)row.get
            			("CostCostObjectCurrency")).doubleValue());
            
            epcError.setCostObjectExternalKey((String)row.get("CostObjectExternalKey"));
            epcError.setCostObjectID((String)row.get("CostObjectID"));
            epcError.setCostObjectName((String)row.get("CostObjectName"));
            if(row.get("CostTransactionCurrency")!=null)
            	epcError.setCostTransactionCurrency(((BigDecimal)row.get
            			("CostTransactionCurrency")).doubleValue());
            
            if(row.get("ActualCostTransactionCurrency")!=null)
            	epcError.setActualCostTransactionCurrency(((BigDecimal)row.get
            			("ActualCostTransactionCurrency")).doubleValue());
            
            if(row.get("Obligo")!=null)
            	epcError.setObligo(((BigDecimal)row.get
            			("Obligo")).doubleValue());
            
            epcError.setCurrencyCostObjectCode((String)row.get("CurrencyCostObjectCode"));
            epcError.setCurrencyTransactionCode((String)row.get("CurrencyTransactionCode")); 
            epcError.setDeletionFlagID((String)row.get("DeletionFlagID"));
            epcError.setFinalConfirmationID((String)row.get("FinalConfirmationID"));
            
            epcError.setExternalKey((String)row.get("ExternalKey"));
            
            epcError.setSAPPurchaseRequisitionLineItemNumbe((String)row.get("SAPPurchaseRequisitionLineItemNumber"));
            epcError.setSAPPurchaseRequisitionNumber((String)row.get("SAPPurchaseRequisitionNumber")); 
            epcError.setSAPPurchasingDocumentLineItemNumber((String)row.get("SAPPurchasingDocumentLineItemNumber"));
            epcError.setSAPPurchasingDocumentNumberID((String)row.get("SAPPurchasingDocumentNumberID"));
            epcError.setSAPWBSElement((String)row.get("SAPWBSElement"));
            
            if (row.get("TransactionDate")!=null)
            	epcError.setTransactionDate(toXMLGregorianCalendar((java.sql.Date)row.get("TransactionDate")));
            
            epcError.setVersionID((String)row.get("VersionID"));    
            
            if(row.get("SAPExchangeRate")!=null)
            	epcError.setSAPExchangeRate(((BigDecimal)row.get
            			("SAPExchangeRate")).doubleValue());
            if(row.get("PRQuantity")!=null)
            	epcError.setPRQuantity(((BigDecimal)row.get
            			("PRQuantity")).doubleValue());          
            if(row.get("POQuantity")!=null)
            	epcError.setPOQuantity(((BigDecimal)row.get
            			("POQuantity")).doubleValue());
            if(row.get("Obligo")!=null)
            	epcError.setPOQuantity(((BigDecimal)row.get
            			("Obligo")).doubleValue());            
            if(row.get("ActualCostTransactionCurrency")!=null)
                        	epcError.setPOQuantity(((BigDecimal)row.get
                        			("ActualCostTransactionCurrency")).doubleValue());
            if(row.get("PODistributionPCT")!=null)
            	epcError.setPOQuantity(((BigDecimal)row.get
            			("PODistributionPCT")).doubleValue());
            epcError.setUnitofMeasureID((String)row.get("UnitofMeasureID"));    
            epcError.setSAPPRProcessingState((String)row.get("SAPPRProcessingState"));            
            epcError.setSAPPRProcessingStatus((String)row.get("SAPPRProcessingStatus"));     
            
            epcError.setRootCostObjectID((String)row.get("RootCostObjectID"));
            epcErrors.add(epcError);
        }
        return epcErrors;
    }
	
    public void insertPolBatch(final List<PolApiErrorDAO> pols, final String sql){
      	 
        //epcJDBCTemplate = new JdbcTemplate(dataSource);
          //String strSql = sql;
          epcJDBCTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
          
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                PolApiErrorDAO errorDAO = pols.get(i);
                ps.setString(1, errorDAO.getStatus());
                ps.setString(2, errorDAO.getErrorMsg() );
                ps.setString(3, errorDAO.getCommitmentID());
                ps.setString(4, errorDAO.getSAPPurchasingDocumentNumberID());
                ps.setString(5, errorDAO.getSAPPurchasingDocumentLineItemNumber());
                ps.setString(6, errorDAO.getCostObjectID());
                ps.setString(7, errorDAO.getCostObjectName());
                ps.setString(8, errorDAO.getSAPWBSElement());
                ps.setString(9, errorDAO.getCostAccountID());
                ps.setString(10, errorDAO.getCostAccountName());
                
                if (errorDAO.getCostCostObjectCurrency()!=null)
                	ps.setDouble(11, errorDAO.getCostCostObjectCurrency());
                else 
                	ps.setDouble(11, 0.0);
                
                ps.setString(12, errorDAO.getCurrencyTransactionCode());
                ps.setString(13, errorDAO.getCurrencyCostObjectCode());
                ps.setString(14, errorDAO.getVersionID());
                
                if (errorDAO.getConversionRateCostObjectCurrency()!=null)
                	ps.setDouble(15, errorDAO.getConversionRateCostObjectCurrency());
                else 
                	ps.setDouble(15, 0.0);
                
                if (errorDAO.getTransactionDate()!=null)
                	ps.setDate(16, new java.sql.Date(errorDAO.getTransactionDate()
                			.toGregorianCalendar().getTimeInMillis()));
                else
                	ps.setDate(16, null);
                
                ps.setString(17, errorDAO.getDeletionFlagID());
                ps.setString(18, errorDAO.getExternalKey());
                ps.setString(19, errorDAO.getSAPPurchaseRequisitionNumber());              
                ps.setString(20, errorDAO.getSAPPurchaseRequisitionLineItemNumbe());
                ps.setString(21, errorDAO.getCostObjectExternalKey());
                if (errorDAO.getCostTransactionCurrency()!=null)
                	ps.setDouble(22, errorDAO.getCostTransactionCurrency());
                else 
                	ps.setDouble(22, 0.0);
                
                if (sql.equals(GlobalConstants.EPC_PRL_API_ERROR_BATCH_INSERT))
                {
                	ps.setDouble(23, errorDAO.getActualCostTransactionCurrency());
                	ps.setDouble(24, errorDAO.getObligo());
                	ps.setDouble(25, errorDAO.getPRQuantity());
                	ps.setString(26, errorDAO.getUnitofMeasureID());
                	ps.setDouble(27, errorDAO.getPODistributionPCT());
                	ps.setString(28, errorDAO.getSAPPurcharsingOrderSeqNumber());
                	ps.setString(29, errorDAO.getSAPPRProcessingState());
                	ps.setString(30, errorDAO.getSAPPRProcessingStatus());
                	ps.setDouble(31, errorDAO.getConversionRateCostObjectCurrency());
                	ps.setString(32, errorDAO.getRootCostObjectID());
                }
                else
                {
                    if (errorDAO.getFinalConfirmationID()!=null)
                    	ps.setString(23, errorDAO.getFinalConfirmationID());
                	ps.setDouble(24, errorDAO.getActualCostTransactionCurrency());
                	ps.setDouble(25, errorDAO.getObligo());
                	ps.setDouble(26, errorDAO.getPOQuantity());
                	ps.setString(27, errorDAO.getUnitofMeasureID());
                	ps.setDouble(28, errorDAO.getPODistributionPCT());
                	ps.setString(29, errorDAO.getSAPPurcharsingOrderSeqNumber());
                	ps.setDouble(30, errorDAO.getConversionRateCostObjectCurrency());
                	ps.setString(31, errorDAO.getRootCostObjectID());
                }
                /*
                if (errorDAO.getCostObjectHierarchyPathID()!=null)
                	ps.setString(3, errorDAO.getCostObjectHierarchyPathID());
                else
                    ps.setString(3, " ");
                */
            }

            
            public int getBatchSize() {
                return pols.size();
            }
          });
        }
    
    public int[] updatePolBatch(final List<PolApiErrorDAO> pols, String sql){
    	 
        //epcJDBCTemplate = new JdbcTemplate(dataSource);

          //String sql = GlobalConstants.EPC_POL_API_ERROR_BATCH_UPDATE;
          //boolean isNewError = isNew;
    	int[] updateCounts = epcJDBCTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
          
            public void setValues(PreparedStatement ps, int i) throws SQLException {         
            	PolApiErrorDAO apiError = pols.get(i);
                ps.setString(1, apiError.getStatus());
                ps.setLong(2, apiError.getId());
            }
            public int getBatchSize() {
                return pols.size();
            }
          });
          return updateCounts;
        }
    
    public int deleteBatch(String projectId, String sql){
   	 
        //epcJDBCTemplate = new JdbcTemplate(dataSource);
    	String strSql = sql + "'"+ projectId + "'";
    	Object[] params = {projectId};
        int deleteCounts = epcJDBCTemplate.update(strSql);
        //epcJDBCTemplate.up
        return deleteCounts;
   }
    
    @SuppressWarnings("rawtypes")
    public List<ActApiErrorDAO> findFailedActByProject(String projectID){
    	epcJDBCTemplate = new JdbcTemplate(dataSource);
        String sql = GlobalConstants.EPC_ACT_API_ERROR_SELECT_BY_PROJECT + "'" + projectID + "'";
        List<ActApiErrorDAO> epcErrors = new ArrayList<ActApiErrorDAO>();
        List<Map<String, Object>> rows = epcJDBCTemplate.queryForList(sql);
        for (Map row : rows) {
        	ActApiErrorDAO epcError = new ActApiErrorDAO();
            //epcError.setId(Integer.parseInt(String.valueOf(row.get("ID"))));
        	epcError.setId(Integer.parseInt(String.valueOf(row.get("Id"))));
            epcError.setStatus((String)row.get("Status"));
            epcError.setErrorMsg((String)row.get("ErrorMsg"));
            

            epcError.setCommitmentID((String)row.get("CommitmentID"));
            epcError.setControllingArea((String)row.get("ControllingArea"));
            
            if (row.get("ConversionRateCostObjectCurrency")!=null)
            	epcError.setConversionRateCostObjectCurrency(((BigDecimal)row.get
            			("ConversionRateCostObjectCurrency")).doubleValue());
            
            epcError.setCostAccountID((String)row.get("CostAccountID"));
            epcError.setCostAccountName((String)row.get("CostAccountName"));
            
            if(row.get("CostCostObjectCurrency")!=null)
            	epcError.setCostCostObjectCurrency(((BigDecimal)row.get
            			("CostCostObjectCurrency")).doubleValue());
            
            epcError.setCostObjectExternalKey((String)row.get("CostObjectExternalKey"));
            epcError.setCostObjectID((String)row.get("CostObjectID"));     
            
            if(row.get("CostTransactionCurrency")!=null)
            	epcError.setCostTransactionCurrency(((BigDecimal)row.get
            			("CostTransactionCurrency")).doubleValue());
            
            epcError.setCurrencyCostObjectCode((String)row.get("CurrencyCostObjectCode"));
            epcError.setCurrencyTransactionCode((String)row.get("CurrencyTransactionCode")); 
            epcError.setExternalKey((String)row.get("ExternalKey"));
            
            epcError.setFIDocumentNumber((String)row.get("FIDocumentNumber"));
            epcError.setSAPWBSElement((String)row.get("SAPWBSElement")); 
            epcError.setVendorID((String)row.get("VendorID"));
            epcError.setVendorName((String)row.get("VendorName"));
            epcError.setVersionID((String)row.get("VersionID"));
            
            if (row.get("TransactionDate")!=null)
            	epcError.setTransactionDate(toXMLGregorianCalendar((java.util.Date)row.get("TransactionDate")));
            epcError.setActualReferenceDoc((String)row.get("ActualReferenceDoc"));
            epcError.setActualReferenceHeaderText((String)row.get("ActualReferenceHeaderText"));
            epcError.setSAPPurchasingDocumentLineItemNumber((String)row.get("SAPPurchasingDocumentLineItemNumber"));
            epcError.setSAPUniqueID((String)row.get("SAPUniqueID"));
            epcError.setOffsettingAccountNumber((String)row.get("OffsettingAccountNumber"));
            epcError.setSAPPurcharsingOrderSeqNumber((String)row.get("SAPPurcharsingOrderSeqNumber"));
            epcError.setSAPAccrualID((String)row.get("SAPAccrualID"));
            
            if(row.get("PostingRow")!=null)
            	epcError.setPostingrow(((BigDecimal)row.get
            			("PostingRow")).doubleValue());
            
            if(row.get("SAPExchangeRate")!=null)
            	epcError.setSAPExchangeRate(((BigDecimal)row.get
            			("SAPExchangeRate")).doubleValue());
            
            epcError.setRootCostObjectID((String)row.get("RootCostObjectID"));
            epcErrors.add(epcError);
        }
        return epcErrors;
    }
    

    
    public void insertActBatch(final List<ActApiErrorDAO> acts){
      	 
        //epcJDBCTemplate = new JdbcTemplate(dataSource);
          String sql = GlobalConstants.EPC_ACT_API_ERROR_BATCH_INSERT;
          epcJDBCTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
          
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ActApiErrorDAO errorDAO = acts.get(i);
                ps.setString(1, errorDAO.getStatus());
                ps.setString(2, errorDAO.getErrorMsg() );
                
                if (errorDAO.getCostObjectID()!=null)
                	ps.setString(3, errorDAO.getCostObjectID());
                else if (errorDAO.getCostObjectExternalKey().length()>40)
                	ps.setString(3, errorDAO.getCostObjectExternalKey().substring(0, 39));
                else
                	ps.setString(3, errorDAO.getCostObjectExternalKey());

                if (errorDAO.getSAPWBSElement()!=null)
                	ps.setString(4, errorDAO.getSAPWBSElement());
                else 
                	ps.setString(4, errorDAO.getCostObjectExternalKey());
                
                if (errorDAO.getCostCostObjectCurrency()!=null)
                	ps.setDouble(5, errorDAO.getCostCostObjectCurrency());
                else 
                	ps.setDouble(5, 0.0);
                
                if (errorDAO.getCurrencyCostObjectCode()!=null)
                	ps.setString(6, errorDAO.getCurrencyCostObjectCode());
                else 
                	ps.setString(6, null);
                
                if (errorDAO.getCostTransactionCurrency()!=null)
                	ps.setDouble(7, errorDAO.getCostTransactionCurrency());
                else 
                	ps.setDouble(7, 0.0);
                
                //ps.setString(8, errorDAO.getCurrencyTransactionCode());
                if (errorDAO.getCurrencyTransactionCode()!=null)
                	ps.setString(8, errorDAO.getCurrencyTransactionCode());
                else 
                	ps.setString(8, null);        
                
                if (errorDAO.getCostAccountID()!=null)
                	ps.setString(9, errorDAO.getCostAccountID());
                else
                	ps.setString(9, null);
                
                if (errorDAO.getCostAccountName()!=null)
                	ps.setString(10, errorDAO.getCostAccountName());
                else
                	ps.setString(10, null);
                
                if (errorDAO.getTransactionDate()!=null)
                	ps.setDate(11, new java.sql.Date(errorDAO.getTransactionDate()
                			.toGregorianCalendar().getTimeInMillis()));
                else
                	ps.setDate(11, null);
                
                if (errorDAO.getFIDocumentNumber()!=null)
                	ps.setString(12, errorDAO.getFIDocumentNumber());
                else 
                	ps.setString(12, null);
                
                if (errorDAO.getCommitmentID()!=null)
                	ps.setString(13, errorDAO.getCommitmentID());
                else 
                	ps.setString(13, null);

                if (errorDAO.getVendorID()!=null)
                	ps.setString(14, errorDAO.getVendorID());
                else 
                	ps.setString(14, null);
                
                if (errorDAO.getVendorName()!=null)
                	ps.setString(15, errorDAO.getVendorName());
                else 
                	ps.setString(15, null);
                
                if (errorDAO.getConversionRateCostObjectCurrency()!=null)
                	ps.setDouble(16, errorDAO.getConversionRateCostObjectCurrency());
                else 
                	ps.setDouble(16, 0.0);
                
                if (errorDAO.getExternalKey()!=null)
                	ps.setString(17, errorDAO.getExternalKey());
                else 
                	ps.setString(17, null);
                
                if (errorDAO.getVersionID()!=null)
                	ps.setString(18, errorDAO.getVersionID());
                else 
                	ps.setString(18, null);

                if (errorDAO.getCostObjectExternalKey()!=null)
                	ps.setString(19, errorDAO.getCostObjectExternalKey());
                else 
                	ps.setString(19, null);

                if (errorDAO.getControllingArea()!=null)
                	ps.setString(20, errorDAO.getControllingArea());
                else 
                	ps.setString(20, null);

                if (errorDAO.getActualReferenceHeaderText()!=null)
                	ps.setString(21, errorDAO.getActualReferenceHeaderText());
                else 
                	ps.setString(21, null);
                
                if (errorDAO.getActualReferenceDoc()!=null)
                	ps.setString(22, errorDAO.getActualReferenceDoc());
                else 
                	ps.setString(22, null);
                
                if (errorDAO.getSAPPurchasingDocumentLineItemNumber()!=null)
                	ps.setString(23, errorDAO.getSAPPurchasingDocumentLineItemNumber());
                else 
                	ps.setString(23, null);
                
                if (errorDAO.getSAPUniqueID()!=null)
                	ps.setString(24, errorDAO.getSAPUniqueID());
                else 
                	ps.setString(24, null);
                
                if (errorDAO.getOffsettingAccountNumber()!=null)
                	ps.setString(25, errorDAO.getOffsettingAccountNumber());
                else 
                	ps.setString(25, null);
                
                if (errorDAO.getSAPPurcharsingOrderSeqNumber()!=null)
                	ps.setString(26, errorDAO.getSAPPurcharsingOrderSeqNumber());
                else 
                	ps.setString(26, null);
                
                if (errorDAO.getSAPAccrualID()!=null)
                	ps.setString(27, errorDAO.getSAPAccrualID());
                else 
                	ps.setString(27, null);
                
                if (errorDAO.getPostingrow()!=null)
                	ps.setDouble(28, errorDAO.getPostingrow());
                else 
                	ps.setDouble(28, 0);
                
                if (errorDAO.getSAPExchangeRate()!=null)
                	ps.setDouble(29, errorDAO.getSAPExchangeRate());
                else 
                	ps.setDouble(29, 0);
                
                ps.setString(30, errorDAO.getRootCostObjectID());
            }

            
            public int getBatchSize() {
                return acts.size();
            }
          });
        }
    
    public int[] updateActBatch(final List<ActApiErrorDAO> acts){
    	 
        //epcJDBCTemplate = new JdbcTemplate(dataSource);

          String sql = GlobalConstants.EPC_ACT_API_ERROR_BATCH_UPDATE;
          //boolean isNewError = isNew;
          int[] updateCounts = epcJDBCTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
          
            public void setValues(PreparedStatement ps, int i) throws SQLException {         
            	ActApiErrorDAO apiError = acts.get(i);
                ps.setString(1, apiError.getStatus());
                ps.setLong(2, apiError.getId());
            }
            public int getBatchSize() {
                return acts.size();
            }
          });
          return updateCounts;
        }
    
    public static XMLGregorianCalendar toXMLGregorianCalendar(Date date)
    {
    	GregorianCalendar gCal  = new GregorianCalendar();
    	gCal.setTime(date);
    	XMLGregorianCalendar xmlCalendar = null;
    	try {
    		xmlCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gCal);
    	}
    	catch (DatatypeConfigurationException ex) {
    		Logger.getLogger(ex.getMessage());
    	}
    	return xmlCalendar;
    }
    
    public static void testConnection(Connection con)
    {
        try
        {
            //Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            //String userName = "fmuser";
            //String password = "fmuser";
            //String url = "jdbc:sqlserver://PA798.ecosys.local:1433;database=esfm";  
        	//Connection con = DriverManager.getConnection(url, userName, password);
            
        	Statement s1 = con.createStatement();
            ResultSet rs = s1.executeQuery("SELECT TOP 1 * FROM fmuser.ecointg_pol_api_error");

            String[] result = new String[20];
            if(rs!=null){
                while (rs.next()){
                    for(int i = 0; i <result.length ;i++)
                    {
                        for(int j = 0; j <result.length;j++)
                        {
                            result[j]=rs.getString(i);
                        System.out.println(result[j]);
                    }
                    }
                }
            }

            //String result = new result[20];

        } catch (Exception e)
        {
            e.printStackTrace();
        }

    }
    
    @SuppressWarnings("rawtypes")
    public List<DcoApiErrorDAO> findFailedDcoByProject(String projectID){
    	epcJDBCTemplate = new JdbcTemplate(dataSource);
        String sql = GlobalConstants.EPC_DCO_API_ERROR_SELECT_BY_PROJECT + "'" + projectID + "'";
        List<DcoApiErrorDAO> epcErrors = new ArrayList<DcoApiErrorDAO>();
        List<Map<String, Object>> rows = epcJDBCTemplate.queryForList(sql);
        for (Map row : rows) {
        	DcoApiErrorDAO epcError = new DcoApiErrorDAO();
            //epcError.setId(Integer.parseInt(String.valueOf(row.get("ID"))));
        	epcError.setId(Integer.parseInt(String.valueOf(row.get("Id"))));
            epcError.setStatus((String)row.get("Status"));
            epcError.setCostObjectID((String)row.get("CostObjectID"));
            epcError.setCostObjectName((String)row.get("CostObjectName"));
            epcError.setCostObjectStatus((String)row.get("CostObjectStatus"));
            epcError.setCostObjectTypeName((String)row.get("CostObjectTypeName"));
            epcError.setErrorMsg((String)row.get("ErrorMsg"));
            epcError.setExternalKey((String)row.get("ExternalKey"));
            //epcError.setHierarchyPathID((String)row.get("HierarchyPathID"));
            epcError.setParentCostObjectExternalKey((String)row.get("ParentCostObjectExternalKey"));
            epcError.setRootCostObjectID((String)row.get("RootCostObjectID"));
            epcErrors.add(epcError);
        }
        return epcErrors;
    }
    
    public void insertDcoBatch(final List<DcoApiErrorDAO> dcos, String sql){
      	 
        //epcJDBCTemplate = new JdbcTemplate(dataSource);

          //String sql = GlobalConstants.EPC_POLCO_API_ERROR_BATCH_INSERT;

          epcJDBCTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
          
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                DcoApiErrorDAO wbs = dcos.get(i);
                ps.setString(1, wbs.getStatus());
                ps.setString(2, wbs.getErrorMsg() );
                ps.setString(3, wbs.getCostObjectID());
                ps.setString(4, wbs.getCostObjectName());
                ps.setString(5, wbs.getCostObjectStatus());
                ps.setString(6, wbs.getCostObjectTypeName());
                ps.setString(7, wbs.getParentCostObjectExternalKey());
                ps.setString(8, wbs.getExternalKey());
                ps.setString(9, wbs.getRootCostObjectID());
                ps.setString(10, GlobalConstants.EPC_API_ERROR_FLAG_N);
            }

            
            public int getBatchSize() {
                return dcos.size();
            }
          });
        }
    
    public int[] updateDcoBatch(final List<DcoApiErrorDAO> dcos, String sql){
    	 
        //epcJDBCTemplate = new JdbcTemplate(dataSource);

         // String sql = GlobalConstants.EPC_POLCO_API_ERROR_BATCH_UPDATE;
          //boolean isNewError = isNew;
          int[] updateCounts = epcJDBCTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
          
            public void setValues(PreparedStatement ps, int i) throws SQLException {         
            	DcoApiErrorDAO apiError = dcos.get(i);
                ps.setString(1, apiError.getStatus());
                ps.setLong(2, apiError.getId());
            }
            public int getBatchSize() {
                return dcos.size();
            }
          });
          return updateCounts;
        }
    
    public int[] updateDcoBatchRetired(final List<DcoApiErrorDAO> dcos){
   	 
        //epcJDBCTemplate = new JdbcTemplate(dataSource);

          String sql = GlobalConstants.EPC_DCO_API_ERROR_BATCH_UPDATE_RETIRED;
          //boolean isNewError = isNew;
          int[] updateCounts = epcJDBCTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
          
            public void setValues(PreparedStatement ps, int i) throws SQLException {         
            	DcoApiErrorDAO apiError = dcos.get(i);
                //ps.setString(1, apiError.getStatus());
                ps.setLong(1, apiError.getId());
            }
            public int getBatchSize() {
                return dcos.size();
            }
          });
          return updateCounts;
        }
    
    @SuppressWarnings("rawtypes")
    public void insertPrjBatch(final List<PrjApiErrorDAO> prjs, String sql){
      	 
        //epcJDBCTemplate = new JdbcTemplate(dataSource);

          //String sql = GlobalConstants.EPC_POLCO_API_ERROR_BATCH_INSERT;

          epcJDBCTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
          
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                PrjApiErrorDAO wbs = prjs.get(i);
                ps.setString(1, wbs.getStatus());
                ps.setString(2, wbs.getErrorMsg() );
                ps.setString(3, wbs.getID());
                ps.setString(4, wbs.getCostObjectName());
                ps.setString(5, wbs.getCostObjectStatus());
                ps.setString(6, wbs.getCostObjectTypeName());
                ps.setInt(7, wbs.getCostObjectHierarchyLevel());
                ps.setString(8, wbs.getSAPSystemID());
                ps.setString(9, wbs.getInternalID());
                ps.setString(10, wbs.getProjectStatusID());
                ps.setString(11, wbs.getProjectTypeID());
                ps.setString(12, wbs.getParentCostObjectID());
                if (wbs.getSAPLastRunDate()!=null)
                	ps.setDate(13, new java.sql.Date(wbs.getSAPLastRunDate()
                			.toGregorianCalendar().getTimeInMillis()));
                else
                	ps.setDate(13, null);
                ps.setString(14, wbs.getRootCostObjectID());
                ps.setString(15, GlobalConstants.EPC_API_ERROR_FLAG_N);
            }

            
            public int getBatchSize() {
                return prjs.size();
            }
          });
        }
    
    public int[] updatePrjBatch(final List<PrjApiErrorDAO> prjs, String sql){
    	 
        //epcJDBCTemplate = new JdbcTemplate(dataSource);

         // String sql = GlobalConstants.EPC_POLCO_API_ERROR_BATCH_UPDATE;
          //boolean isNewError = isNew;
          int[] updateCounts = epcJDBCTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
          
            public void setValues(PreparedStatement ps, int i) throws SQLException {         
            	PrjApiErrorDAO apiError = prjs.get(i);
                ps.setString(1, apiError.getStatus());
                ps.setLong(2, apiError.getId());
            }
            public int getBatchSize() {
                return prjs.size();
            }
          });
          return updateCounts;
        }
    
    public int[] updatePrjBatchRetired(final List<PrjApiErrorDAO> prjs){
   	 
        //epcJDBCTemplate = new JdbcTemplate(dataSource);

          String sql = GlobalConstants.EPC_PRJ_API_ERROR_BATCH_UPDATE_RETIRED;
          //boolean isNewError = isNew;
          int[] updateCounts = epcJDBCTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
          
            public void setValues(PreparedStatement ps, int i) throws SQLException {         
            	PrjApiErrorDAO apiError = prjs.get(i);
                //ps.setString(1, apiError.getStatus());
                ps.setLong(1, apiError.getId());
            }
            public int getBatchSize() {
                return prjs.size();
            }
          });
          return updateCounts;
        }
    
    @SuppressWarnings("rawtypes")
    public List<CaApiErrorDAO> findFailedCaByProject(String projectID){
    	epcJDBCTemplate = new JdbcTemplate(dataSource);
        String sql = GlobalConstants.EPC_CA_API_ERROR_SELECT_BY_PROJECT + "'" + projectID + "'";
        List<CaApiErrorDAO> epcErrors = new ArrayList<CaApiErrorDAO>();
        List<Map<String, Object>> rows = epcJDBCTemplate.queryForList(sql);
        for (Map row : rows) {
        	CaApiErrorDAO epcError = new CaApiErrorDAO();
            //epcError.setId(Integer.parseInt(String.valueOf(row.get("ID"))));
        	epcError.setId(Integer.parseInt(String.valueOf(row.get("Id"))));
            epcError.setStatus((String)row.get("Status"));
            epcError.setID((String)row.get("CostAccountID"));
            epcError.setName((String)row.get("CostAccountName"));
            
            if (row.get("Active")!=null &&  row.get("StartDate").equals(GlobalConstants.EPC_API_ERROR_FLAG_Y))
            	epcError.setActive(true);
            if (row.get("Active")!=null &&  row.get("StartDate").equals(GlobalConstants.EPC_API_ERROR_FLAG_N))
            	epcError.setActive(false);
            
            if (row.get("StartDate")!=null)
            	epcError.setStartDate(toXMLGregorianCalendar((java.util.Date)row.get("StartDate")));
            if (row.get("EndDate")!=null)
            	epcError.setEndDate(toXMLGregorianCalendar((java.util.Date)row.get("EndDate")));

            epcError.setPathID((String)row.get("PathID"));
            epcError.setSAPSource((String)row.get("SAPSource"));
            epcError.setErrorMsg((String)row.get("ErrorMsg"));
            epcError.setRootCostObjectID((String)row.get("RootCostObjectID"));
            epcErrors.add(epcError);
        }
        return epcErrors;
    }
    
    public void insertCaBatch(final List<CaApiErrorDAO> cas, String sql){
      	 
          //String sql = GlobalConstants.EPC_POLCO_API_ERROR_BATCH_INSERT;

          epcJDBCTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
          
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                CaApiErrorDAO wbs = cas.get(i);
                ps.setString(1, wbs.getStatus());
                ps.setString(2, wbs.getErrorMsg() );
                ps.setString(3, wbs.getID());
                if (wbs.getName()!=null)
                	ps.setString(4, wbs.getName());
                else 
                	ps.setString(4, null);
                
                if (wbs.isActive()!=null)
                {
                if (wbs.isActive())
                	ps.setString(5, GlobalConstants.EPC_API_ERROR_FLAG_Y);
                else
                	ps.setString(5, GlobalConstants.EPC_API_ERROR_FLAG_N);
                }
                else
                	ps.setString(5,  null);
                
                if (wbs.getStartDate()!=null)
                	ps.setDate(6, new java.sql.Date(wbs.getStartDate()
                			.toGregorianCalendar().getTimeInMillis()));
                else
                	ps.setDate(6, null);
                
                if (wbs.getEndDate()!=null)
                	ps.setDate(7, new java.sql.Date(wbs.getEndDate()
                			.toGregorianCalendar().getTimeInMillis()));
                else
                	ps.setDate(7, null);
                ps.setString(8, wbs.getPathID());
                ps.setString(9, wbs.getSAPSource());
                ps.setString(10, wbs.getRootCostObjectID());
                ps.setString(11, GlobalConstants.EPC_API_ERROR_FLAG_N);
            }

            
            public int getBatchSize() {
                return cas.size();
            }
          });
        }
    
    public int[] updateCaBatch(final List<CaApiErrorDAO> cas, String sql){
    	 
        //epcJDBCTemplate = new JdbcTemplate(dataSource);

         // String sql = GlobalConstants.EPC_POLCO_API_ERROR_BATCH_UPDATE;
          //boolean isNewError = isNew;
          int[] updateCounts = epcJDBCTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
          
            public void setValues(PreparedStatement ps, int i) throws SQLException {         
            	CaApiErrorDAO apiError = cas.get(i);
                ps.setString(1, apiError.getStatus());
                ps.setLong(2, apiError.getId());
            }
            public int getBatchSize() {
                return cas.size();
            }
          });
          return updateCounts;
        }
    
    public int[] updateCaBatchRetired(final List<CaApiErrorDAO> cas){
   	 
        //epcJDBCTemplate = new JdbcTemplate(dataSource);

          String sql = GlobalConstants.EPC_CA_API_ERROR_BATCH_UPDATE_RETIRED;
          //boolean isNewError = isNew;
          int[] updateCounts = epcJDBCTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
          
            public void setValues(PreparedStatement ps, int i) throws SQLException {         
            	CaApiErrorDAO apiError = cas.get(i);
                //ps.setString(1, apiError.getStatus());
                ps.setLong(1, apiError.getId());
            }
            public int getBatchSize() {
                return cas.size();
            }
          });
          return updateCounts;
        } 
    
    public void insertTrcBatch(final List<TrcApiErrorDAO> cas, String sql){
     	 
        //String sql = GlobalConstants.EPC_POLCO_API_ERROR_BATCH_INSERT;

        epcJDBCTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
        
          public void setValues(PreparedStatement ps, int i) throws SQLException {
              TrcApiErrorDAO api = cas.get(i);
              ps.setString(1, api.getStatus());
              ps.setString(2, api.getErrorMsg() );
              ps.setString(3, api.getCategoryID());
              ps.setString(4, api.getTransactionCategoryID());
              ps.setString(5, api.getTransactionCategoryName());
              if (api.isActive())
              	ps.setString(6, GlobalConstants.EPC_API_ERROR_FLAG_Y);
              else
              	ps.setString(6, GlobalConstants.EPC_API_ERROR_FLAG_N);
              
              if (api.getParentCategoryID()!=null)
              	ps.setString(7, api.getParentCategoryID());
              else
              	ps.setString(7, null);
              
              ps.setString(8, api.getSAPSource());
              ps.setString(9, api.getRootCostObjectID());
              ps.setString(10, GlobalConstants.EPC_API_ERROR_FLAG_N);
          }

          
          public int getBatchSize() {
              return cas.size();
          }
        });
      }
  
  public int[] updateTrcBatch(final List<TrcApiErrorDAO> cas, String sql){
  	 
      //epcJDBCTemplate = new JdbcTemplate(dataSource);

       // String sql = GlobalConstants.EPC_POLCO_API_ERROR_BATCH_UPDATE;
        //boolean isNewError = isNew;
        int[] updateCounts = epcJDBCTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
        
          public void setValues(PreparedStatement ps, int i) throws SQLException {         
          	TrcApiErrorDAO apiError = cas.get(i);
              ps.setString(1, apiError.getStatus());
              ps.setLong(2, apiError.getId());
          }
          public int getBatchSize() {
              return cas.size();
          }
        });
        return updateCounts;
      }
  
  public int[] updateTrcBatchRetired(final List<TrcApiErrorDAO> cas){
 	 
      //epcJDBCTemplate = new JdbcTemplate(dataSource);

        String sql = GlobalConstants.EPC_CA_API_ERROR_BATCH_UPDATE_RETIRED;
        //boolean isNewError = isNew;
        int[] updateCounts = epcJDBCTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
        
          public void setValues(PreparedStatement ps, int i) throws SQLException {         
          	TrcApiErrorDAO apiError = cas.get(i);
              //ps.setString(1, apiError.getStatus());
              ps.setLong(1, apiError.getId());
          }
          public int getBatchSize() {
              return cas.size();
          }
        });
        return updateCounts;
      } 
  
  
  public void insertTacBatch(final List<TacApiErrorDAO> cas, String sql){
   	 
      //String sql = GlobalConstants.EPC_POLCO_API_ERROR_BATCH_INSERT;

      epcJDBCTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
      
        public void setValues(PreparedStatement ps, int i) throws SQLException {
            TacApiErrorDAO api = cas.get(i);
            ps.setString(1, api.getStatus());
            ps.setString(2, api.getErrorMsg() );
            ps.setString(3, api.getParentCategoryID());
            ps.setString(4, api.getTaskCategoryID());
            ps.setString(5, api.getTaskCategoryName());
            if (api.isActive())
            	ps.setString(6, GlobalConstants.EPC_API_ERROR_FLAG_Y);
            else
            	ps.setString(6, GlobalConstants.EPC_API_ERROR_FLAG_N);
            
            if (api.getParentCategoryID()!=null)
            	ps.setString(7, api.getParentCategoryID());
            else
            	ps.setString(7, null);
            
            ps.setString(8, api.getSAPSource());
            ps.setString(9, api.getRootCostObjectID());
            ps.setString(10, GlobalConstants.EPC_API_ERROR_FLAG_N);
        }

        
        public int getBatchSize() {
            return cas.size();
        }
      });
    }

public int[] updateTacBatch(final List<TacApiErrorDAO> cas, String sql){
	 
    //epcJDBCTemplate = new JdbcTemplate(dataSource);

     // String sql = GlobalConstants.EPC_POLCO_API_ERROR_BATCH_UPDATE;
      //boolean isNewError = isNew;
      int[] updateCounts = epcJDBCTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
      
        public void setValues(PreparedStatement ps, int i) throws SQLException {         
        	TacApiErrorDAO apiError = cas.get(i);
            ps.setString(1, apiError.getStatus());
            ps.setLong(2, apiError.getId());
        }
        public int getBatchSize() {
            return cas.size();
        }
      });
      return updateCounts;
    }

public int[] updateTacBatchRetired(final List<TacApiErrorDAO> cas){
	 
    //epcJDBCTemplate = new JdbcTemplate(dataSource);

      String sql = GlobalConstants.EPC_CA_API_ERROR_BATCH_UPDATE_RETIRED;
      //boolean isNewError = isNew;
      int[] updateCounts = epcJDBCTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
      
        public void setValues(PreparedStatement ps, int i) throws SQLException {         
        	TacApiErrorDAO apiError = cas.get(i);
            //ps.setString(1, apiError.getStatus());
            ps.setLong(1, apiError.getId());
        }
        public int getBatchSize() {
            return cas.size();
        }
      });
      return updateCounts;
    } 
}