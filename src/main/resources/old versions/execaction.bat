@echo off
@REM ----------------------------------------------------------------------
@REM -- EcoSys Execute Command Batch Job Script
@REM ----------------------------------------------------------------------

@ECHO ExecAction.bat processing argument [%1]

@REM -- -------------------------------------------------------------------
@REM -- CHOOSE JOB ACTION
@REM -- -------------------------------------------------------------------
IF "%1" == "SAP_WBS"    				GOTO :RUN_SAP_WBS_PMO_IMPORT
IF "%1" == "SAP_PMO"		    		GOTO :RUN_SAP_WBS_PMO_IMPORT
IF "%1" == "SAP_WBS_PMO"    			GOTO :RUN_SAP_WBS_PMO_IMPORT
IF "%1" == "SAP_PO_PR"    			    GOTO :RUN_SAP_PO_PR_IMPORT
IF "%1" == "SAP_POH_POL"		    	GOTO :RUN_SAP_PO_PR_IMPORT
IF "%1" == "SAP_PRH_PRL"		    	GOTO :RUN_SAP_PO_PR_IMPORT
IF "%1" == "SAP_ACT"		    		GOTO :RUN_SAP_ACT_IMPORT
IF "%1" == "SAP_NEW_VEND"		    	GOTO :RUN_SAP_NEW_VENDOR


@ECHO EXECACTION: Unrecognized parameter "%1", not able to continue
GOTO EOF

@REM -- -------------------------------------------------------------------
@REM -- RUN THE PROCESS FOR THIS ACTION
@REM -- -------------------------------------------------------------------

:RUN_SAP_WBS_PMO_IMPORT
@ECHO EXPORT EXEC:  %1
cd /D C:/EcoSys/batch/custom/app
bayerintg.bat %1 %2
GOTO :EOF

:RUN_SAP_PO_PR_IMPORT
@ECHO EXPORT EXEC:  %1
cd /D C:/EcoSys/batch/custom/app
bayerint.bat %1 %2
GOTO :EOF

:RUN_SAP_ACT_IMPORT
@ECHO EXPORT EXEC:  %1
cd /D C:/EcoSys/batch/custom/app
bayerintg.bat %1 %2
GOTO :EOF

:RUN_SAP_NEW_VENDOR
@ECHO EXPORT EXEC:  %1
cd /D C:/EcoSys/batch/custom/app
bayerintg.bat %1 %2 %3
GOTO :EOF

:EOF