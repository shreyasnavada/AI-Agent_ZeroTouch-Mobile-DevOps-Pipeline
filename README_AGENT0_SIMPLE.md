### Agent 0 — Mobile Build Automation (Phases 1–4)

End‑to‑end mobile build distribution with one command. Suitable for quick sharing in a public repo.

### Run
```bash
cd adobe-mcp-servers-main
./run_automation_clean.bat
```

### Flowchart
```
┌─────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌──────┐
│  START  │───▶│    PICK     │───▶│  DOWNLOAD   │───▶│  GENERATE   │───▶│ AUTO EMAIL  │───▶│ AUTO SHARE  │───▶│ DONE │
└─────────┘    │   LATEST    │    │   BUILD     │    │ QR CODES &  │    │ DELIVERY    │    │BUILD LINKS  │    └──────┘
               │   BUILDS    │    │ TO LOCAL    │    │BUILD LINKS  │    │OF QR CODES  │    │TO SLACK     │
               │FROM ARTIFAC │    │             │    │             │    │             │    │  CHANNEL    │
               │   TORY      │    │             │    │             │    │             │    │             │
               └─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
```

### Key Files
- Orchestrator: `run_automation_clean.bat`
- Phase 1: `src/jfrog-artifactory/download_latest_build.js`
- Phase 2: `DiawiBuildUploader/` (entry: `com.automation.DiawiFinalLoginFixed`)
- Phase 3: `phase3_email_qr_codes_fixed.ps1`
- Phase 4: `phase4_slack_notification.ps1`

### Minimal Prereqs
- Windows + PowerShell (Outlook installed)
- Node.js 18+, Java 17+, Maven
- Slack Workflow trigger URL configured in `phase4_slack_notification.ps1`

### Notes
- Phase 3 supports `-RecipientEmail "name@company.com"` when run directly.
- QR images saved under `DiawiBuildUploader/` and archived in `App Build QR codes/`.


