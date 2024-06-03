# Set-Location "C:\Program Files\Java\jdk1.8.0_202\jre\bin"
New-Variable -Name "JRE" -Value “C:\Program Files\Java\jre1.8.0_202\lib\security\cacerts”
Set-Variable -Name "JDK" -Value “C:\Program Files\Java\jdk1.8.0_202\jre\lib\security\cacerts”
Set-Variable -Name "CERT_TO_INSTALL" -Value "C:\ecosysdev\ecosysdev.crt"
Set-Variable -Name "ALIAS" -Name "BayerRoot"

Set-Variable -Name "LOCATION" -Value Get-Variable %JRE
# IF "%2" == "jdk" SET LOCATION = %JDK% else SET LOCATION = %JRE%

Write-Host "Location: " %LOCATION%
Write-Host "Certificate: " %CERT_TO_INSTALL
Read-Host -Prompt "Press any key to continue"

# IF ("%1 == "check")	GOTO :CHECK_CACERTS
# IF ("%1 == "install")	{GOTO :INSTALL_NEW_CERT}

Write-Output "bad parameter input program ending"
GOTO EOF:

: CHECK_CACERTS
# Checking the store
Write-Output Line24

Read-Host -Prompt "Press any key to continue"
keytool -list -keystore %LOCATION%
GOTO EOF:

: INSTALL_NEW_CERT
# Certificate install
Write-Output Line33
Write-Output "keytool -import -alias " %ALIAS% "-keystore " %LOCATION% " -file " %CERT_TO_INSTALL%

$Input = Read-Host -Prompt "Continue?"
Write-Output %Input
Read-Host -Prompt "Press any key to continue"
IF (%Input == "y") {
    keytool -import -alias %ALIAS% -keystore %LOCATION% -file %CERT_TO_INSTALL% 
}

: EOF
Set-Location "C:\ecosysdev"
Read-Host -Prompt "Reached EOF: Press any key to continue"


# keytool -import -alias BayerRoot -keystore "C:\Program Files\Java\jre1.8.0_202\lib\security\cacerts" -file C:\ecosysdev\BayerRoot.crt
# keytool -import -alias BayerRoot -keystore "C:\Program Files\Java\jdk1.8.0_202\jre\lib\security\cacerts" -file C:\ecosysdev\BayerRoot.crt
# keytool -import -alias ecosysdev -keystore "C:\Program Files\Java\jre1.8.0_202\lib\security\cacerts" -file C:\ecosysdev\ecosysdev.crt
# keytool -import -alias ecosysdev -keystore "C:\Program Files\Java\jdk1.8.0_202\jre\lib\security\cacerts" -file C:\ecosysdev\ecosysdev.crt
# keytool -import -alias BayerDevice -keystore "C:\Program Files\Java\jre1.8.0_202\lib\security\cacerts" -file C:\ecosysdev\BayerDevice.crt
# keytool -import -alias BayerDevice -keystore "C:\Program Files\Java\jdk1.8.0_202\jre\lib\security\cacerts" -file C:\ecosysdev\BayerDevice.crt
