package com.bayer.integration.startup;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.joda.time.format.ISOPeriodFormat;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bayer.integration.properties.GlobalConstants;
import com.bayer.integration.service.EcoSysEventTrigger;
import com.bayer.integration.service.ImportManager;
import com.bayer.integration.service.MasterDataManagerImpl;
import com.ecosys.exception.SystemException;
import com.ecosys.properties.AppProperties;
import com.ecosys.util.Stopwatch;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import javax.sql.DataSource;

import com.bayer.integration.service.MasterDataManagerImpl;
import com.bayer.integration.utils.DebugBanner;

/**
 * @author vharman
 *
 */
public class SAPImport {

	protected static ApplicationContext springCtx;
	protected static Logger logger = Logger.getLogger(SAPImport.class);
	static boolean validArgs = true;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Stopwatch timerTotal = new Stopwatch();
		timerTotal.start();				
		int exit_code = 0;

		try {
			//Import data from staging tables for all interfaces
			logger.debug("Initiating Data Import...");
			springCtx = new ClassPathXmlApplicationContext(new String[]{"applicationContext-db.xml","applicationContext-services.xml", "applicationContext-properties.xml"});
			AppProperties appProperties = (AppProperties)springCtx.getBean("appProperties", AppProperties.class);
			if(GlobalConstants.DEBUGMODE)
				DebugBanner.outputBanner("STARTING IN SAP IMPORT");
			readArguments(args);
			if (validArgs) {
			logger.info("DEBUG MODE: " + GlobalConstants.DEBUGMODE + "\n" +
					"  Skip SAP WBS Interface:\t\t" + GlobalConstants.SKIP_SAP_WBS_INTERFACE + "\n" +
					"  Skip SAP Work Package Interface:\t" + GlobalConstants.SKIP_SAP_PMO_INTERFACE + "\n" +
					"  Skip SAP Requisitions Header Interface:" + GlobalConstants.SKIP_SAP_PRH_INTERFACE + "\n" +
					"  Skip SAP Requisition Lines Interface:\t" + GlobalConstants.SKIP_SAP_PRL_INTERFACE + "\n" +
					"  Skip SAP Commitments Header Interface:" + GlobalConstants.SKIP_SAP_POH_INTERFACE + "\n" +
					"  Skip SAP Commitment Lines Interface:\t" + GlobalConstants.SKIP_SAP_POL_INTERFACE + "\n" +
					"  Skip SAP Actuals Interfaces:\t\t" + GlobalConstants.SKIP_SAP_ACT_INTERFACE + "\n" +
					"  Skip SAP Master Projects Monthly Closing: " + GlobalConstants.SKIP_SAP_MON_INTERFACE + "\n" +
					"  Skip CCL Validation:\t" + GlobalConstants.SKIP_EVT_CCL_INTERFACE + "\n" +
					"  Run SAP Year Close :\t" + GlobalConstants.PROCESS_SAP_YEARCLOSE);

			List<String> errorCodes = buildErrorCodesList();

			Stopwatch timerRead = new Stopwatch();			
			@SuppressWarnings("unchecked")
			Map<String, ImportManager> importMgrMap = springCtx.getBeansOfType(ImportManager.class);
			if (importMgrMap != null){
				Collection<ImportManager> importMgrList = importMgrMap.values();
				if (importMgrList != null){
					for (ImportManager importMgr : importMgrList) {
						try {
							timerRead.start();
							int retCode = importMgr.importData();
							if ((exit_code != 1) && errorCodes.contains(retCode+"")){
								exit_code=1;
							}
							logger.info("Total Loader Time: " + importMgr.getClass().getSimpleName() + " - " + timerRead.stop().toString(ISOPeriodFormat.alternateExtended()));
						} catch(Exception e) {
							logger.error("Error loading Data for: " + importMgr.getClass().getSimpleName() + " - " + e.getMessage());
						}
					}
				}
			}
			logger.debug("Data Import is now complete!");
		}

		} catch(Exception e) {
			System.out.println(e);
			logger.error(e.getMessage(), e);
			exit_code = 1;
		}

		if(GlobalConstants.DEBUGMODE) 
			DebugBanner.outputBanner("Exiting SAP Import");
		
		logger.info("Elapsed time: " + timerTotal.stop().toString(ISOPeriodFormat.alternateExtended()));	
		logger.info("Finished importing data, exiting with an EXIT_CODE of: " + exit_code);	
		System.exit(exit_code);
	}	

	protected static void readArguments(String[] args) {
		String argString = "Arguments";
		if (args!= null && args.length>0) {
			for (String arg : args){
			argString =argString+ " : " +arg;	
			}
			logger.debug(argString);
			
			GlobalConstants.SKIP_SAP_WBS_INTERFACE=true;
			GlobalConstants.SKIP_SAP_PMO_INTERFACE=true;
			GlobalConstants.SKIP_SAP_PRH_INTERFACE=true;
			GlobalConstants.SKIP_SAP_PRL_INTERFACE=true;
			GlobalConstants.SKIP_SAP_POH_INTERFACE=true;
			GlobalConstants.SKIP_SAP_POL_INTERFACE=true;
			GlobalConstants.SKIP_SAP_ACT_INTERFACE=true;
			GlobalConstants.SKIP_SAP_PP_INTERFACE=true;
			GlobalConstants.SKIP_SAP_MON_INTERFACE=true;
			GlobalConstants.SKIP_EVT_CCL_INTERFACE=false;
			GlobalConstants.SKIP_EPC_NEW_VENDOR=true;		
			GlobalConstants.PROCESS_SAP_YEARCLOSE=false;		
		}
		if (args != null && args.length==3 && args[0].equalsIgnoreCase("CREATE_NEW_VENDOR")){
			GlobalConstants.SKIP_EPC_NEW_VENDOR=false;
			GlobalConstants.EPC_VENDOR_ID =args[1];		
			GlobalConstants.EPC_VENDOR_NAME=args[2]; // may not be able to pass parameter from EcoSys if it contains whitespace			
			GlobalConstants.SKIP_EVT_CCL_INTERFACE=true;
			}
		else
		{		
			if (args != null && args.length > 0) {		
				int argNo = args.length;
				int argCount = 0;
				for (String arg:args){
					if (argCount == 0){
						if (arg != null && arg.equals("SAP_WBS")){
							GlobalConstants.SKIP_SAP_WBS_INTERFACE=false;	
						} else if (arg != null && arg.equals("SAP_WBS_PMO")){
							GlobalConstants.SKIP_SAP_WBS_INTERFACE=false;
							GlobalConstants.SKIP_SAP_PMO_INTERFACE=false;
						} 
						else if (arg != null && arg.equals("SAP_DECEMBER_CLOSE")){
							GlobalConstants.SKIP_SAP_ACT_INTERFACE=false;
							GlobalConstants.PROCESS_SAP_YEARCLOSE=true;

						} else if (arg != null && arg.equals("SAP_PMO")){
							GlobalConstants.SKIP_SAP_PMO_INTERFACE=false;
							
						} else if (arg != null && arg.equals("SAP_ACT")){
							GlobalConstants.SKIP_SAP_ACT_INTERFACE=false;					
						} else if (arg != null && arg.equals("SAP_POH")){
							GlobalConstants.SKIP_SAP_POH_INTERFACE=false;	
							
						} else if (arg != null && arg.equals("SAP_POL")){
							GlobalConstants.SKIP_SAP_POL_INTERFACE=false;
							
						} else if (arg != null && arg.equals("SAP_PRH")){
							GlobalConstants.SKIP_SAP_PRH_INTERFACE=false;
							
						} else if (arg != null && arg.equals("SAP_PRL")){
							GlobalConstants.SKIP_SAP_PRL_INTERFACE=false;
							
						} else if (arg != null && arg.equals("SAP_POH_PRH")){
							GlobalConstants.SKIP_SAP_POH_INTERFACE=false;
							GlobalConstants.SKIP_SAP_PRH_INTERFACE=false;
							
						} else if (arg != null && arg.equals("SAP_POL_PRL")){
							GlobalConstants.SKIP_SAP_POL_INTERFACE=false;
							GlobalConstants.SKIP_SAP_PRL_INTERFACE=false;
							
						} else if (arg != null && arg.equals("SAP_PO_PR")){
							GlobalConstants.SKIP_SAP_POH_INTERFACE=false;
							GlobalConstants.SKIP_SAP_PRH_INTERFACE=false;
							GlobalConstants.SKIP_SAP_POL_INTERFACE=false;
							GlobalConstants.SKIP_SAP_PRL_INTERFACE=false;
							
						} else if (arg != null && arg.equals("SAP_ALL")){
							GlobalConstants.SKIP_SAP_WBS_INTERFACE=false;
							GlobalConstants.SKIP_SAP_PMO_INTERFACE=false;
							GlobalConstants.SKIP_SAP_ACT_INTERFACE=false;	
							GlobalConstants.SKIP_SAP_POH_INTERFACE=false;
							GlobalConstants.SKIP_SAP_POL_INTERFACE=false;
							GlobalConstants.SKIP_SAP_PRH_INTERFACE=false;
							GlobalConstants.SKIP_SAP_PRL_INTERFACE=false;
						}
						else {
							logger.debug("Invalid Input Parameter(s) found, program terminates.");
							validArgs=false;
							GlobalConstants.SKIP_EVT_CCL_INTERFACE=true;						
							break;
						}
						argCount = argCount +1;
					}
					if (argCount == 1)
					{
						GlobalConstants.EPC_PROJECT_ID = arg;
					}
				}
			}
			else {
				logger.debug("No Input Parameter found for main interfaces, using skip properties from application config file");
			}
		}
	}

	protected static List<String> buildErrorCodesList(){
		List<String> errorCodesList = new ArrayList<String>();
		errorCodesList.add(GlobalConstants.IMPORT_SAP_WBS_FAILED +"");
		errorCodesList.add(GlobalConstants.IMPORT_SAP_PMO_FAILED +"");
		errorCodesList.add(GlobalConstants.IMPORT_SAP_ACT_FAILED +"");
		errorCodesList.add(GlobalConstants.IMPORT_SAP_POH_FAILED +"");
		errorCodesList.add(GlobalConstants.IMPORT_SAP_POL_FAILED +"");
		errorCodesList.add(GlobalConstants.EPC_CREATE_NEW_VENDOR_FAIL +"");
		errorCodesList.add(GlobalConstants.EPC_EVT_CCL_FAIL +"");
		return errorCodesList;
	}


}
