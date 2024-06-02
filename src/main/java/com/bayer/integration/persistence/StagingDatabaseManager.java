/**
 * 
 */
package com.bayer.integration.persistence;

import java.sql.Connection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

import com.ecosys.exception.SystemException;
import com.bayer.integration.persistence.*;



/**
 * @author sdas
 *
 */
public interface StagingDatabaseManager {
	
	public void setEpcDataSource(DataSource ds);
	
	public JdbcTemplate getEpcJDBCTemplate();
	
	public Connection getConnection();
	
	public DataSource getDataSource();
	
	public void insertWbs(WbsApiErrorDAO wbs);
	
	public void insertWbsBatch(final List<WbsApiErrorDAO> wbses);
	
	public int[] updateWbsBatch(final List<WbsApiErrorDAO> wbses);
	
	public List<WbsApiErrorDAO> findFailedWbsByProject(String projectID);
	
    public List<PmOrderApiErrorDAO> findFailedPmoByProject(String projectID);
	
	public void insertPmoBatch(final List<PmOrderApiErrorDAO> pmOrders);
	
    public int[] updatePmoBatch(final List<PmOrderApiErrorDAO> pmOrders);
    
    public int[] updatePmoBatchRetired(final List<PmOrderApiErrorDAO> pmOrders);
    
    public List<PohApiErrorDAO> findFailedPohByProject(String projectID, String sql);
    
    public void insertPohBatch(final List<PohApiErrorDAO> pohs, String sql);
    
    public int[] updatePohBatch(final List<PohApiErrorDAO> pohs, String sql);
    
    public List<PolcoApiErrorDAO> findFailedPolcoByProject(String projectID);
	
	public void insertPolcoBatch(final List<PolcoApiErrorDAO> polcos, String sql);
	
    public int[] updatePolcoBatch(final List<PolcoApiErrorDAO> polcos, String sql);
    
    public int[] updatePolcoBatchRetired(final List<PolcoApiErrorDAO> polcos);

    public List<PolApiErrorDAO> findFailedPolByProject(String projectID, String sql);
    
    public void insertPolBatch(final List<PolApiErrorDAO> pols, String sql);    
    
    public int[] updatePolBatch(final List<PolApiErrorDAO> pols, String sql);
    
    public List<ActApiErrorDAO> findFailedActByProject(String projectID);
    
    public void insertActBatch(final List<ActApiErrorDAO> pols);
    
    public int[] updateActBatch(final List<ActApiErrorDAO> pols);
    
    public int deleteBatch(String projectId, String sql);
    
    
    public List<CaApiErrorDAO> findFailedCaByProject(String projectID);
	
	public void insertCaBatch(final List<CaApiErrorDAO> polcos, String sql);
	
    public int[] updateCaBatch(final List<CaApiErrorDAO> polcos, String sql);
    
    public int[] updateCaBatchRetired(final List<CaApiErrorDAO> polcos);
    
    public List<DcoApiErrorDAO> findFailedDcoByProject(String projectID);
	
	public void insertDcoBatch(final List<DcoApiErrorDAO> polcos, String sql);
	
    public int[] updateDcoBatch(final List<DcoApiErrorDAO> polcos, String sql);
    
    public int[] updateDcoBatchRetired(final List<DcoApiErrorDAO> polcos);
    
    //public List<PrjApiErrorDAO> findFailedPrjByProject(String projectID);
	
	public void insertPrjBatch(final List<PrjApiErrorDAO> polcos, String sql);
	
    public int[] updatePrjBatch(final List<PrjApiErrorDAO> polcos, String sql);
    
    public int[] updatePrjBatchRetired(final List<PrjApiErrorDAO> polcos);
    
    
	public void insertTrcBatch(final List<TrcApiErrorDAO> trcs, String sql);
	
    public int[] updateTrcBatch(final List<TrcApiErrorDAO> trcs, String sql);
    
    public int[] updateTrcBatchRetired(final List<TrcApiErrorDAO> trcs);
    
    
	public void insertTacBatch(final List<TacApiErrorDAO> tacs, String sql);
	
    public int[] updateTacBatch(final List<TacApiErrorDAO> tacs, String sql);
    
    public int[] updateTacBatchRetired(final List<TacApiErrorDAO> tacs);
}
