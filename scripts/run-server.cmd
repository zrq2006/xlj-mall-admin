@echo off
rem Start the Xlj Mall admin jar detached from the caller.
pushd "%~dp0.."
if not exist "logs" mkdir "logs"
start "" /b java -jar "backend\target\xlj-mall-admin.jar" >> "logs\app.log" 2>&1
popd
