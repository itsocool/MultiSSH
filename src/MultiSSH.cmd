@echo off
@set currdir=%~dp0
@cd /d "%currdir%"
@java -version >NUL 2>&1
if %ERRORLEVEL% == 0 goto FOUND
 	echo [Not found java] >&2
goto END

:FOUND
	@set host=%1
	@set user=%2
	@set pass=%3
	@set port=%4
	@set command=%5

	..\..\Nolsia\src\assets\bin\jre7\bin\java -Xms16m -Xmx1024m -jar MultiSSH.jar nolsia:123456@220.64.140.216:22;nolsia:123456@220.64.140.217:22;nolsia:123456@220.64.140.218:22;nolsia:123456@220.64.140.219:22;

	if not %ERRORLEVEL% == 0 echo [ERROR] >&2
goto end
	
:END