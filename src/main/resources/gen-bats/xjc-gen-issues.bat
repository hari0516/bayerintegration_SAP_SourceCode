@ECHO OFF

@REM set JAVA_BIN=C:\"Program Files"\Java\jdk1.8.0_201\bin
set PATH=%path%/;C:\Program Files\java\jdk1.8.0_201\bin

xjc -p com.bayer.integration.rest.log IntegrationIssuesAPI.xsd
REM xjc -d .\gen-src -p com.bayer.integration.rest.log IntegrationIssuesAPI.xsd
pause