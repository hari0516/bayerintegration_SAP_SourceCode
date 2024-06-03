@ECHO OFF

@REM set JAVA_BIN=C:\"Program Files"\Java\jdk1.7.0_79\bin
set PATH=%path%/;C:\Program Files\java\jdk1.7.0_21\bin


xjc -d .\gen-src -p com.bayer.integration.rest.prld BayerCommitmentPRLIDeleteAPI.xsd
