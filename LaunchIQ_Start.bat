
@echo off
REM LaunchIQ start script for Windows
SETLOCAL ENABLEDELAYEDEXPANSION

IF "%JAVA_HOME%"=="" (
  echo Please ensure JAVA_HOME is set to JDK17 installation path.
  pause
  exit /b 1
)

REM Resolve script directory
set SCRIPT_DIR=%~dp0
pushd "%SCRIPT_DIR%"

REM Use data directory under installation folder by default
set DATA_DIR=%SCRIPT_DIR%data
if not exist "%DATA_DIR%" mkdir "%DATA_DIR%"

REM Normalize to forward slashes for JDBC/Java
set DATA_DIR_FWD=%DATA_DIR:\=/%
set DB_PATH=%DATA_DIR_FWD%/launchiq_windows.db

REM Run Spring Boot app (adjust JVM args as needed)
mvn -Dapp.encryption.key=QASecretKey2025 -Dapp.db.path="%DB_PATH%" spring-boot:run

popd
ENDLOCAL
pause
