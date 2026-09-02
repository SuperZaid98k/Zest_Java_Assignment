@REM Maven Wrapper startup batch script
@echo off
set ERROR_CODE=0

set MAVEN_PROJECTBASEDIR=%MAVEN_BASEDIR%
if "%MAVEN_PROJECTBASEDIR%"=="" set MAVEN_PROJECTBASEDIR=%CD%

set WRAPPER_JAR="%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
set WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

if exist %WRAPPER_JAR% goto run

mkdir "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper"
powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; (New-Object Net.WebClient).DownloadFile('https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar', '%WRAPPER_JAR%')"

:run
set JVM_CONFIG_FILE="%MAVEN_PROJECTBASEDIR%\.mvn\jvm.config"
if exist %JVM_CONFIG_FILE% (
  for /F "usebackq delims=" %%a in (%JVM_CONFIG_FILE%) do set JVM_CONFIG=%%a
)

%JAVA_HOME%\bin\java.exe %JVM_CONFIG% -classpath %WRAPPER_JAR% %WRAPPER_LAUNCHER% %*
if ERRORLEVEL 1 goto error
goto end

:error
set ERROR_CODE=1

:end
cmd /c exit /b %ERROR_CODE%
