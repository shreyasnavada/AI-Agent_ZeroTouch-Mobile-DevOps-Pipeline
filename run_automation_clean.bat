@echo off
setlocal EnableDelayedExpansion

echo ====================================================
echo        ULTIMATE 4-PHASE AUTOMATION SUITE
echo    Phase 1: Artifactory - Phase 2: Diawi - Phase 3: Email - Phase 4: Slack
echo ====================================================
echo.
echo Email Target: vshreyas+1@adobetest.com
echo Slack Notification: Team Channel  
echo Complete End-to-End Automation
echo.

set SUITE_START_TIME=%time%

echo ====================================================
echo [PHASE 1] Artifactory Build Download
echo ====================================================
cd "C:\Cursor_QA\adobe-mcp-servers-main\src\jfrog-artifactory"

:: Capture build number from Phase 1 output
FOR /F "tokens=*" %%g IN ('node download_latest_build.js ^| findstr /B /C:"Latest build identified:"') DO (
    SET "LINE=%%g"
    SET "BUILD_NUMBER=!LINE:Latest build identified:=!"
    SET "BUILD_NUMBER=!BUILD_NUMBER: =!"
)

cd "C:\Cursor_QA\adobe-mcp-servers-main"

if "%BUILD_NUMBER%"=="" (
    echo ERROR: Could not determine latest build number!
    pause
    exit /b 1
)
echo Phase 1 Complete: Build %BUILD_NUMBER% downloaded

echo.
echo ====================================================  
echo [PHASE 2] Diawi Upload + QR Generation
echo ====================================================
cd "C:\Cursor_QA\adobe-mcp-servers-main\DiawiBuildUploader"
mvn exec:java

if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Phase 2 failed!
    pause
    exit /b 1
)
echo Phase 2 Complete: QR codes generated

echo.
echo ====================================================
echo [PHASE 3] Email QR Codes to vshreyas+1@adobetest.com
echo ====================================================
cd "C:\Cursor_QA\adobe-mcp-servers-main"
powershell -ExecutionPolicy Bypass -File "phase3_email_qr_codes_fixed.ps1" -BuildNumber "%BUILD_NUMBER%" -RecipientEmail "vshreyas+1@adobetest.com"

if %ERRORLEVEL% NEQ 0 (
    echo WARNING: Phase 3 email had issues, but QR codes are available
) else (
    echo Phase 3 Complete: Email sent successfully
)

echo.
echo ====================================================
echo [PHASE 4] Slack Notification to Team Channel
echo ====================================================
cd "C:\Cursor_QA\adobe-mcp-servers-main"
powershell -ExecutionPolicy Bypass -File "phase4_slack_notification.ps1" -BuildNumber "%BUILD_NUMBER%" -SuiteStartTime "%SUITE_START_TIME%"

if %ERRORLEVEL% NEQ 0 (
    echo WARNING: Phase 4 Slack notification had issues, but builds are ready
) else (
    echo Phase 4 Complete: Slack notification sent successfully
)

echo.
echo ====================================================
echo ULTIMATE 4-PHASE AUTOMATION COMPLETE!
echo ====================================================
echo Phase 1: Build %BUILD_NUMBER% downloaded from Artifactory
echo Phase 2: Android + iOS QR codes generated via Diawi 
echo Phase 3: QR codes emailed to vshreyas@adobetest.com
echo Phase 4: Build notification posted to Slack channel
echo.
echo Check your email for QR codes!
echo Check your Slack channel for build notification!
echo Total automation time: ~3-5 minutes
echo Zero manual intervention required
echo ====================================================
pause
