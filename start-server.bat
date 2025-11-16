@echo off
REM Woodlanders Server Launcher Script (Windows)
REM This script reads JVM configuration and starts the server

REM Default JVM settings
if not defined JVM_MEMORY_MIN set JVM_MEMORY_MIN=4G
if not defined JVM_MEMORY_MAX set JVM_MEMORY_MAX=4G
if not defined JVM_GC set JVM_GC=G1GC
if not defined JVM_GC_PAUSE set JVM_GC_PAUSE=50
if not defined JVM_PARALLEL_REF set JVM_PARALLEL_REF=true

REM Server settings
set SERVER_JAR=build\libs\woodlanders-server.jar

REM Build JVM arguments
set JVM_ARGS=-Xms%JVM_MEMORY_MIN% -Xmx%JVM_MEMORY_MAX%

if "%JVM_GC%"=="G1GC" (
    set JVM_ARGS=%JVM_ARGS% -XX:+UseG1GC
)

if defined JVM_GC_PAUSE (
    set JVM_ARGS=%JVM_ARGS% -XX:MaxGCPauseMillis=%JVM_GC_PAUSE%
)

if "%JVM_PARALLEL_REF%"=="true" (
    set JVM_ARGS=%JVM_ARGS% -XX:+ParallelRefProcEnabled
)

REM Print configuration
echo ==========================================
echo   Woodlanders Server Launcher
echo ==========================================
echo JVM Configuration:
echo   Memory: %JVM_MEMORY_MIN% - %JVM_MEMORY_MAX%
echo   GC: %JVM_GC%
echo   Max GC Pause: %JVM_GC_PAUSE%ms
echo   Parallel Ref Processing: %JVM_PARALLEL_REF%
echo.
echo Starting server...
echo ==========================================
echo.

REM Start the server
java %JVM_ARGS% -jar "%SERVER_JAR%" %*
