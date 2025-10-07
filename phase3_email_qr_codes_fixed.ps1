# PHASE 3: EMAIL AUTOMATION - ENHANCED VERSION
# Email QR codes with comprehensive build details and runtime info

param(
    [string]$BuildNumber = "1042",
    [string]$RecipientEmail = "vshreyas+1@adobetest.com"
)

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "   PHASE 3: EMAIL AUTOMATION" -ForegroundColor Cyan  
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "Target: $RecipientEmail" -ForegroundColor Green
Write-Host "Build: $BuildNumber" -ForegroundColor Green
Write-Host ""

# Check QR code files
$AndroidQR = "DiawiBuildUploader\android_qr_code_latest.png"
$iOSQR = "DiawiBuildUploader\ios_qr_code_latest.png"

Write-Host "Checking QR code files..." -ForegroundColor Yellow

if (!(Test-Path $AndroidQR) -or !(Test-Path $iOSQR)) {
    Write-Host "ERROR: QR code files not found!" -ForegroundColor Red
    Write-Host "Please run Phase 1 + Phase 2 first." -ForegroundColor Yellow
    pause
    exit 1
}

Write-Host "✅ QR codes found" -ForegroundColor Green
Write-Host "   Android: $AndroidQR" -ForegroundColor White  
Write-Host "   iOS: $iOSQR" -ForegroundColor White

# Email settings
    $Subject = "Adobe Learning Manager - Mobile App QR Codes - Build $BuildNumber"
    $CurrentDate = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    
    # Calculate suite runtime (typical execution time)
    $SuiteRuntimeMinutes = "1:15"
    
    # Get build file details
    $AndroidBuildPath = "Mobile_App_Builds\Android\Vader-Android-$BuildNumber.apk"
    $IosBuildPath = "Mobile_App_Builds\iOS\Vader-iOS-$BuildNumber.ipa"
    
    $AndroidSizeMB = if (Test-Path $AndroidBuildPath) { 
        [math]::Round((Get-Item $AndroidBuildPath).Length / 1MB, 1) 
    } else { "N/A" }
    
    $IosSizeMB = if (Test-Path $IosBuildPath) { 
        [math]::Round((Get-Item $IosBuildPath).Length / 1MB, 1) 
    } else { "N/A" }

$EmailBody = @"
Hi Team,

Latest mobile app builds are ready for testing!

ANDROID BUILD:
- File: Vader-Android-$BuildNumber.apk
- Scan the Android QR code to install

iOS BUILD:
- File: Vader-iOS-$BuildNumber.ipa  
- Scan the iOS QR code to install

TESTING INSTRUCTIONS:
1. Scan the QR code with your mobile device
2. Download and install the app
3. Test functionality
4. Report issues to development team

BUILD DETAILS:
- Build Number: $BuildNumber
- Generated: $CurrentDate
- Source: Adobe Artifactory
- QR Platform: Diawi

Thanks for testing!
Adobe Learning Manager Build Team
"@

Write-Host ""
Write-Host "Creating and sending email..." -ForegroundColor Blue

try {
    # Enhanced automated email setup
    Write-Host "Initializing automated email system..." -ForegroundColor Green
    
    # Check if Outlook is running, start if needed  
    $OutlookProcess = Get-Process -Name "OUTLOOK" -ErrorAction SilentlyContinue
    if (-not $OutlookProcess) {
        Write-Host "Starting Outlook application..." -ForegroundColor Yellow
        Start-Process "outlook" -WindowStyle Hidden
        Start-Sleep -Seconds 5
    }
    
    # Create Outlook COM object with retry logic
    $retryCount = 3
    $Outlook = $null
    
    for ($i = 1; $i -le $retryCount; $i++) {
        try {
            Write-Host "Connecting to Outlook (attempt $i/$retryCount)..." -ForegroundColor Blue
            $Outlook = New-Object -ComObject Outlook.Application
            break
        } catch {
            if ($i -eq $retryCount) { throw }
            Write-Host "Retry in 3 seconds..." -ForegroundColor Yellow
            Start-Sleep -Seconds 3
        }
    }
    
    # Create email item
    Write-Host "Creating automated email message..." -ForegroundColor Green
    $Mail = $Outlook.CreateItem(0)
    
    $Mail.Subject = $Subject
    $Mail.Body = $EmailBody
    $Mail.To = $RecipientEmail
    $Mail.Importance = 2
    
    # Attach QR codes
    Write-Host "Attaching QR codes..." -ForegroundColor Blue
    $AndroidPath = (Resolve-Path $AndroidQR).Path
    $iOSPath = (Resolve-Path $iOSQR).Path
    
    $Mail.Attachments.Add($AndroidPath)
    $Mail.Attachments.Add($iOSPath)
    
    # Automated email sending with confirmation
    Write-Host "Sending automated email to: $RecipientEmail" -ForegroundColor Blue
    $Mail.Send()
    
    # Wait for email to be sent
    Start-Sleep -Seconds 2
    
    # Cleanup COM objects
    [System.Runtime.Interopservices.Marshal]::ReleaseComObject($Mail) | Out-Null
    [System.Runtime.Interopservices.Marshal]::ReleaseComObject($Outlook) | Out-Null
    [System.GC]::Collect()
    
    Write-Host ""
    Write-Host "================================================" -ForegroundColor Green
    Write-Host "   AUTOMATED EMAIL SENT SUCCESSFULLY!" -ForegroundColor Green
    Write-Host "================================================" -ForegroundColor Green   
    Write-Host "To: $RecipientEmail" -ForegroundColor White
    Write-Host "QR codes delivered automatically!" -ForegroundColor White
    Write-Host "Email automation completed!" -ForegroundColor Green
    Write-Host ""
    Write-Host "PHASE 3 COMPLETED!" -ForegroundColor Cyan
    
} catch {
    Write-Host ""
    Write-Host "ERROR: Could not send email" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    Write-Host "BACKUP: Copying QR codes to Desktop..." -ForegroundColor Yellow
    
    try {
        Copy-Item $AndroidQR "$env:USERPROFILE\Desktop\Android_QR_Build_$BuildNumber.png" -Force
        Copy-Item $iOSQR "$env:USERPROFILE\Desktop\iOS_QR_Build_$BuildNumber.png" -Force
        Write-Host "✅ QR codes copied to Desktop" -ForegroundColor Green
        Write-Host "Manually email these files to: $RecipientEmail" -ForegroundColor Yellow
    } catch {
        Write-Host "Could not copy to Desktop" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "PHASE 3 EMAIL AUTOMATION COMPLETE" -ForegroundColor Cyan
