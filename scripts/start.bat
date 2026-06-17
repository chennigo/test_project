@echo off
REM Build and start Systelm ERP (Windows)
REM 1. cd frontend && npm run build
REM 2. xcopy /E /I /Y frontend\dist\* backend\src\main\resources\static\
REM 3. cd backend && mvn package -DskipTests
REM 4. java -jar backend\target\backend-0.1.0.jar

cd /d %~dp0..
java -jar backend\target\backend-0.1.0.jar
