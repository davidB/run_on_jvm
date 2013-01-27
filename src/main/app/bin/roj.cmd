@REM ----------------------------------------------------------------------------
@REM 	This is free and unencumbered software released into the public domain.
@REM
@REM 	Anyone is free to copy, modify, publish, use, compile, sell, or
@REM 	distribute this software, either in source code form or as a compiled
@REM 	binary, for any purpose, commercial or non-commercial, and by any
@REM 	means.
@REM
@REM 	In jurisdictions that recognize copyright laws, the author or authors
@REM 	of this software dedicate any and all copyright interest in the
@REM 	software to the public domain. We make this dedication for the benefit
@REM 	of the public at large and to the detriment of our heirs and
@REM 	successors. We intend this dedication to be an overt act of
@REM 	relinquishment in perpetuity of all present and future rights to this
@REM 	software under copyright law.
@REM
@REM 	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
@REM 	EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
@REM 	MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
@REM 	IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
@REM 	OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
@REM 	ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
@REM 	OTHER DEALINGS IN THE SOFTWARE.
@REM
@REM 	For more information, please refer to <http://unlicense.org/>
@REM ----------------------------------------------------------------------------

@echo off

set ERROR_CODE=0

:init
@REM Decide how to startup depending on the version of windows

@REM -- Win98ME
if NOT "%OS%"=="Windows_NT" goto Win9xArg

@REM set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" @setlocal

@REM -- 4NT shell
if "%eval[2+2]" == "4" goto 4NTArgs

@REM -- Regular WinNT shell
set CMD_LINE_ARGS=%*
goto WinNTGetScriptDir

@REM The 4NT Shell from jp software
:4NTArgs
set CMD_LINE_ARGS=%$
goto WinNTGetScriptDir

:Win9xArg
@REM Slurp the command line arguments.  This loop allows for an unlimited number
@REM of arguments (up to the command line limit, anyway).
set CMD_LINE_ARGS=
:Win9xApp
if %1a==a goto Win9xGetScriptDir
set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1
shift
goto Win9xApp

:Win9xGetScriptDir
set SAVEDIR=%CD%
%0\
cd %0\..\.. 
set BASEDIR=%CD%
cd %SAVEDIR%
set SAVE_DIR=
goto repoSetup

:WinNTGetScriptDir
set BASEDIR=%~dp0\..

:repoSetup


if "%JAVACMD%"=="" set JAVACMD=java

if "%REPO%"=="" set REPO=%BASEDIR%\lib

set CLASSPATH="%BASEDIR%"\etc;"%REPO%"\org\sonatype\aether\aether-api\1.13.1\aether-api-1.13.1.jar;"%REPO%"\org\sonatype\aether\aether-spi\1.13.1\aether-spi-1.13.1.jar;"%REPO%"\org\sonatype\aether\aether-util\1.13.1\aether-util-1.13.1.jar;"%REPO%"\org\sonatype\aether\aether-impl\1.13.1\aether-impl-1.13.1.jar;"%REPO%"\org\sonatype\aether\aether-connector-file\1.13.1\aether-connector-file-1.13.1.jar;"%REPO%"\org\sonatype\aether\aether-connector-wagon\1.13.1\aether-connector-wagon-1.13.1.jar;"%REPO%"\org\codehaus\plexus\plexus-utils\2.1\plexus-utils-2.1.jar;"%REPO%"\org\apache\maven\wagon\wagon-provider-api\1.0\wagon-provider-api-1.0.jar;"%REPO%"\org\sonatype\maven\wagon-ahc\1.2.1\wagon-ahc-1.2.1.jar;"%REPO%"\com\ning\async-http-client\1.6.5\async-http-client-1.6.5.jar;"%REPO%"\org\jboss\netty\netty\3.2.5.Final\netty-3.2.5.Final.jar;"%REPO%"\org\apache\maven\maven-aether-provider\3.0.4\maven-aether-provider-3.0.4.jar;"%REPO%"\org\apache\maven\maven-model\3.0.4\maven-model-3.0.4.jar;"%REPO%"\org\apache\maven\maven-model-builder\3.0.4\maven-model-builder-3.0.4.jar;"%REPO%"\org\codehaus\plexus\plexus-interpolation\1.14\plexus-interpolation-1.14.jar;"%REPO%"\org\apache\maven\maven-repository-metadata\3.0.4\maven-repository-metadata-3.0.4.jar;"%REPO%"\org\codehaus\plexus\plexus-component-annotations\1.5.5\plexus-component-annotations-1.5.5.jar;"%REPO%"\org\codehaus\plexus\plexus-classworlds\2.4.2\plexus-classworlds-2.4.2.jar;"%REPO%"\org\slf4j\slf4j-api\1.7.2\slf4j-api-1.7.2.jar;"%REPO%"\org\slf4j\slf4j-jdk14\1.7.2\slf4j-jdk14-1.7.2.jar;"%REPO%"\net\alchim31\runner\run_on_jvm\0.3.0-SNAPSHOT\run_on_jvm-0.3.0-SNAPSHOT.jar
goto endInit

@REM Reaching here means variables are defined and arguments have been captured
:endInit

%JAVACMD% %JAVA_OPTS%  -classpath %CLASSPATH_PREFIX%;%CLASSPATH% -Dapp.name="roj" -Dapp.repo="%REPO%" -Dapp.home="%BASEDIR%" -Dbasedir="%BASEDIR%" net_alchim31_runner.Main %CMD_LINE_ARGS%
if ERRORLEVEL 1 goto error
goto end

:error
if "%OS%"=="Windows_NT" @endlocal
set ERROR_CODE=%ERRORLEVEL%

:end
@REM set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" goto endNT

@REM For old DOS remove the set variables from ENV - we assume they were not set
@REM before we started - at least we don't leave any baggage around
set CMD_LINE_ARGS=
goto postExec

:endNT
@REM If error code is set to 1 then the endlocal was done already in :error.
if %ERROR_CODE% EQU 0 @endlocal


:postExec

if "%FORCE_EXIT_ON_ERROR%" == "on" (
  if %ERROR_CODE% NEQ 0 exit %ERROR_CODE%
)

exit /B %ERROR_CODE%
