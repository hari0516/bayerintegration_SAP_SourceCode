@ECHO OFF

@REM set JAVA_BIN=C:\"Program Files"\Java\jdk1.8.0_201\bin
set PATH=%path%/;C:\Program Files\java\jdk1.8.0_201\bin


xjc -d .\gen-src -p com.bayer.integration.rest.polhis BayerCommitmentLIHistoryAPI.xsd