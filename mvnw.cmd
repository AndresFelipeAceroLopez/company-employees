@REM ----------------------------------------------------------------------------
@REM Maven Wrapper startup batch script
@REM ----------------------------------------------------------------------------
@IF "%__MVNW_ARG0_NAME__%"=="" (SET "MVN_CMD=mvn.cmd") ELSE (SET "MVN_CMD=%__MVNW_ARG0_NAME__%")
@SET MAVEN_PROJECTBASEDIR=%~dp0
@SET MAVEN_WRAPPER_JAR="%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.jar"
@SET MAVEN_WRAPPER_PROPERTIES="%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.properties"

@FOR /F "usebackq tokens=1,2 delims==" %%A IN (%MAVEN_WRAPPER_PROPERTIES%) DO (
    @IF "%%A"=="distributionUrl" SET DISTRIBUTION_URL=%%B
)

@SET JAVA_HOME_CANDIDATE=D:\ducument\JAVA
@IF EXIST "%JAVA_HOME_CANDIDATE%\bin\java.exe" SET JAVA_HOME=%JAVA_HOME_CANDIDATE%

@SET JAVA_CMD="%JAVA_HOME%\bin\java.exe"
@IF NOT EXIST %JAVA_CMD% SET JAVA_CMD=java

@SET MAVEN_PROJECTBASEDIR_NO_SLASH=%MAVEN_PROJECTBASEDIR:~0,-1%
@%JAVA_CMD% -classpath %MAVEN_WRAPPER_JAR% "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR_NO_SLASH%" org.apache.maven.wrapper.MavenWrapperMain %*
