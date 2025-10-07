🚀 Agent Vader Automation - Phase 1 to Phase 4 Summary

Agent Vader is a 4-phase mobile build automation suite that delivers fresh Android and iOS builds to development teams with zero manual intervention.

What it does:
   Automatically detects the latest mobile builds from Adobe Artifactory
   Downloads and renames APK/IPA files with Vader naming convention
   Generates QR codes via automated browser upload to distribution platform
   Extracts download URLs directly from web pages during QR generation
   Emails QR codes to development team and QA Teams with build details and attachments
   Notifies Slack channel with build metadata and clickable download links

Technologies:
Node.js (download) → Java/Playwright (QR generation) → PowerShell (email/Slack) → Complete team communication

Single Command Execution:
.\run_automation_clean.bat

Screenshots of Slack Notifications
<img width="1273" height="591" alt="image" src="https://github.com/user-attachments/assets/2ef0e66e-0af0-40a5-bab8-e0b7c525e1e0" />

