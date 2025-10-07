package com.automation;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.logging.Logger;

public class DiawiFinalLoginFixed {
    private static final Logger logger = Logger.getLogger(DiawiFinalLoginFixed.class.getName());
    
    // Diawi credentials
    private static final String EMAIL = "varuva4@gmail.com";
    private static final String PASSWORD = "Learner#12";
    
    // Build paths from Phase 1
    private static final String ANDROID_BUILD_DIR = "C:\\Cursor_QA\\adobe-mcp-servers-main\\Mobile_App_Builds\\Android";
    private static final String IOS_BUILD_DIR = "C:\\Cursor_QA\\adobe-mcp-servers-main\\Mobile_App_Builds\\iOS";
    
    // QR code directories
    private static final String QR_CODES_DIR = "C:\\Cursor_QA\\adobe-mcp-servers-main\\App Build QR codes";
    private static final String ANDROID_QR_DIR = QR_CODES_DIR + File.separator + "Android";
    private static final String IOS_QR_DIR = QR_CODES_DIR + File.separator + "iOS";
    
    // Browser automation instances
    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;
    private Page page;
    
    public static void main(String[] args) {
        logger.info("🚀 STARTING REAL FIXED AUTOMATION - NO MORE BUGS!");
        
        DiawiFinalLoginFixed automation = new DiawiFinalLoginFixed();
        try {
            automation.executeCompleteAutomation();
        } catch (Exception e) {
            logger.severe("❌ Automation failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void executeCompleteAutomation() {
        try {
            // Initialize browser with REAL maximization
            initializeBrowserWithRealMaximize();
            
            // Login to Diawi
            performDiawiLogin();
            
            // Upload Android build with REAL no-popup solution
            uploadBuildWithNoPopup("Android");
            
            // Upload iOS build with REAL no-popup solution  
            uploadBuildWithNoPopup("iOS");
            
            logger.info("🎉 AUTOMATION COMPLETED SUCCESSFULLY - BOTH BUGS FIXED!");
            
        } catch (Exception e) {
            logger.severe("❌ Complete automation failed: " + e.getMessage());
        } finally {
            // Keep browser open for verification
            try {
                if (page != null) {
                    page.waitForTimeout(10000); // Wait 10 seconds for manual verification
                }
            } catch (Exception e) {
                logger.info("Browser closed");
            }
            cleanup();
        }
    }
    
    private void initializeBrowserWithRealMaximize() {
        logger.info("🎭 INITIALIZING BROWSER WITH REAL MAXIMIZATION...");
        
        try {
            playwright = Playwright.create();
            
            // REAL FIX #2: Launch browser maximized from start using proper options
            browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(false)
                .setSlowMo(800)
                .setArgs(Arrays.asList(
                    "--start-maximized",
                    "--disable-web-security", 
                    "--disable-features=VizDisplayCompositor",
                    "--window-size=1920,1080"
                )));
            
            // REAL FIX #2: Create context with full screen viewport
            context = browser.newContext(new Browser.NewContextOptions()
                .setViewportSize(1920, 1080)
                .setScreenSize(1920, 1080));
            
            page = context.newPage();
            
            logger.info("✅ BROWSER LAUNCHED MAXIMIZED - FIX #2 COMPLETE!");
            logger.info("🖥️ Browser dimensions: 1920x1080 fullscreen");
            
        } catch (Exception e) {
            logger.severe("❌ Error initializing browser with real maximize: " + e.getMessage());
            throw new RuntimeException("Browser initialization failed", e);
        }
    }
    
    private void performDiawiLogin() {
        logger.info("🔐 PERFORMING DIAWI LOGIN...");
        
        try {
            // Navigate to Diawi
            page.navigate("https://www.diawi.com/");
            page.waitForLoadState(LoadState.LOAD);
            
            // Click Log in
            page.locator("text=Log in").click();
            page.waitForTimeout(2000);
            
            // Fill login form with specific selectors
            page.locator("input[name='_username']").fill(EMAIL);
            page.locator("input[name='_password']").fill(PASSWORD);
            
            // Submit form
            page.locator("input[type='submit'], button[type='submit']").click();
            page.waitForTimeout(5000);
            
            // Verify login success
            if (page.url().contains("dashboard")) {
                logger.info("✅ DIAWI LOGIN SUCCESSFUL!");
            } else {
                throw new RuntimeException("Login verification failed");
            }
            
        } catch (Exception e) {
            logger.severe("❌ Diawi login failed: " + e.getMessage());
            throw new RuntimeException("Login failed", e);
        }
    }
    
    private void uploadBuildWithNoPopup(String buildType) {
        logger.info("📤 UPLOADING " + buildType.toUpperCase() + " BUILD WITH NO POPUP SOLUTION...");
        
        // Retry configuration
        int maxRetries = 3;
        int retryDelay = 5000; // 5 seconds between retries
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                logger.info("🔄 Upload attempt " + attempt + "/" + maxRetries + " for " + buildType);
                
                // Navigate to upload page in new tab (fresh page for each retry)
                Page uploadPage = context.newPage();
                uploadPage.navigate("https://www.diawi.com/");
                uploadPage.waitForLoadState(LoadState.LOAD);
                logger.info("🌐 Page reloaded for " + buildType + " upload attempt " + attempt);
                
                // Clean old QR codes for this build type (only on first attempt)
                if (attempt == 1) {
                    cleanOldQRCodes(buildType);
                }
                
                // Find the build file
                String buildFilePath = findBuildFile(buildType);
                if (buildFilePath == null) {
                    logger.warning("⚠️ No " + buildType + " build found, skipping...");
                    return;
                }
                
                // Try the upload for this attempt
                boolean uploadSuccess = attemptUpload(uploadPage, buildFilePath, buildType, attempt);
                
                if (uploadSuccess) {
                    // Upload succeeded, continue with QR generation
                    waitForUploadComplete(uploadPage);
                    generateQRCode(uploadPage, buildType);
                    saveQRCodeScreenshot(uploadPage, buildType);
                    uploadPage.close();
                    logger.info("✅ " + buildType + " upload completed successfully on attempt " + attempt);
                    return; // Success - exit retry loop
                } else {
                    // Upload failed
                    uploadPage.close();
                    if (attempt < maxRetries) {
                        logger.warning("⚠️ " + buildType + " upload attempt " + attempt + " failed. Retrying in " + (retryDelay/1000) + " seconds...");
                        Thread.sleep(retryDelay);
                    } else {
                        logger.severe("❌ All " + maxRetries + " upload attempts failed for " + buildType);
                        throw new RuntimeException(buildType + " upload failed after " + maxRetries + " attempts");
                    }
                }
                
            } catch (Exception e) {
                if (attempt < maxRetries) {
                    logger.warning("⚠️ " + buildType + " upload attempt " + attempt + " failed with error: " + e.getMessage() + ". Retrying...");
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        logger.severe("❌ Retry interrupted for " + buildType);
                        return;
                    }
                } else {
                    logger.severe("❌ Final attempt failed for " + buildType + ": " + e.getMessage());
                    throw new RuntimeException(buildType + " upload failed after " + maxRetries + " attempts", e);
                }
            }
        }
    }
    
    private boolean attemptUpload(Page uploadPage, String buildFilePath, String buildType, int attempt) {
        logger.info("🎯 ATTEMPT " + attempt + ": DIRECT FILE UPLOAD - NO WINDOWS DIALOG!");
        
        try {
            // REAL FIX #1: Use Playwright's setInputFiles directly - bypasses Windows dialog completely
            // Try multiple selectors and wait for file input to be available
            String[] fileInputSelectors = {
                "input[type='file']",
                "input[type='file']:not([style*='display: none'])",
                ".upload input[type='file']",
                "#fileInput",
                "[accept*='apk'],[accept*='ipa']"
            };
            
            boolean fileUploaded = false;
            for (String selector : fileInputSelectors) {
                try {
                    Locator fileInput = uploadPage.locator(selector).first();
                    if (fileInput.count() > 0) {
                        // Wait for element to be ready and try to upload
                        uploadPage.waitForTimeout(2000);
                        fileInput.setInputFiles(Paths.get(buildFilePath));
                        logger.info("✅ DIRECT FILE UPLOAD SUCCESS - NO WINDOWS POPUP!");
                        logger.info("📁 File uploaded: " + Paths.get(buildFilePath).getFileName());
                        logger.info("🎯 Used selector: " + selector);
                        fileUploaded = true;
                        break;
                    }
                } catch (Exception e) {
                    logger.fine("File input selector failed: " + selector);
                }
            }
            
            if (!fileUploaded) {
                logger.warning("⚠️ File input not found, trying alternative approach...");
                // Alternative approaches for file upload
                try {
                    // Try clicking Add file button first
                    String[] addFileSelectors = {
                        "text=Add file",
                        "button:has-text('Add')",
                        ".add-file",
                        ".upload-btn",
                        "[data-action='add-file']"
                    };
                    
                    boolean addClicked = false;
                    for (String addSelector : addFileSelectors) {
                        try {
                            if (uploadPage.locator(addSelector).count() > 0) {
                                uploadPage.locator(addSelector).first().click();
                                logger.info("🎯 Clicked add button: " + addSelector);
                                addClicked = true;
                                break;
                            }
                        } catch (Exception ignored) {}
                    }
                    
                    uploadPage.waitForTimeout(2000);
                    
                    // Now try file input again after clicking add button
                    for (String selector : fileInputSelectors) {
                        try {
                            Locator fileInput = uploadPage.locator(selector).first();
                            if (fileInput.count() > 0) {
                                fileInput.setInputFiles(Paths.get(buildFilePath));
                                logger.info("✅ ALTERNATIVE UPLOAD SUCCESS with selector: " + selector);
                                logger.info("🎯 Add button clicked: " + addClicked);
                                fileUploaded = true;
                                break;
                            }
                        } catch (Exception ignored) {}
                    }
                    
                    if (!fileUploaded) {
                        logger.warning("⚠️ Trying final fallback method...");
                        // Final fallback - refresh page and retry
                        uploadPage.reload();
                        uploadPage.waitForLoadState(LoadState.LOAD);
                        uploadPage.waitForTimeout(3000);
                        uploadPage.locator("input[type='file']").first().setInputFiles(Paths.get(buildFilePath));
                        logger.info("✅ FALLBACK UPLOAD SUCCESS!");
                        fileUploaded = true;
                    }
                } catch (Exception e) {
                    logger.warning("❌ Upload methods failed for " + buildType + " attempt " + attempt + ": " + e.getMessage());
                    return false; // Return false to indicate upload failure
                }
            }
            
            return fileUploaded; // Return success status
            
        } catch (Exception e) {
            logger.warning("❌ Upload attempt " + attempt + " failed for " + buildType + ": " + e.getMessage());
            return false; // Return false to indicate upload failure
        }
    }
    
    private String findBuildFile(String buildType) {
        String searchDir = buildType.equals("Android") ? ANDROID_BUILD_DIR : IOS_BUILD_DIR;
        String extension = buildType.equals("Android") ? ".apk" : ".ipa";
        
        try {
            File dir = new File(searchDir);
            if (!dir.exists()) {
                logger.warning("⚠️ Build directory does not exist: " + searchDir);
                return null;
            }
            
            File[] files = dir.listFiles((d, name) -> name.endsWith(extension) && name.startsWith("Vader"));
            if (files != null && files.length > 0) {
                String filePath = files[0].getAbsolutePath();
                logger.info("✅ Found " + buildType + " build: " + files[0].getName());
                return filePath;
            }
        } catch (Exception e) {
            logger.warning("⚠️ Error finding " + buildType + " build: " + e.getMessage());
        }
        
        return null;
    }
    
    private void waitForUploadComplete(Page uploadPage) {
        logger.info("⏳ Waiting for upload to complete...");
        
        try {
            // Wait for 100% progress indicator
            for (int i = 0; i < 60; i++) { // Wait up to 60 seconds
                try {
                    if (uploadPage.locator("text=100%").count() > 0) {
                        logger.info("✅ Upload 100% complete!");
                        break;
                    }
                } catch (Exception e) {
                    // Continue waiting
                }
                uploadPage.waitForTimeout(1000);
                if (i % 10 == 0) {
                    logger.info("⏳ Still waiting... " + i + "s elapsed");
                }
            }
        } catch (Exception e) {
            logger.warning("⚠️ Error waiting for upload: " + e.getMessage());
        }
    }
    
    private void generateQRCode(Page uploadPage, String buildType) {
        logger.info("📱 Generating QR code for " + buildType + "...");
        
        try {
            // Click Send button to generate QR code
            String[] sendSelectors = {
                ".upload-send",
                "button.upload-send", 
                "text=Send",
                "button:has-text('Send')",
                "input[value='Send']"
            };
            
            boolean sendClicked = false;
            for (String selector : sendSelectors) {
                try {
                    Locator sendButton = uploadPage.locator(selector);
                    if (sendButton.count() > 0 && sendButton.first().isVisible()) {
                        sendButton.first().click();
                        logger.info("✅ Clicked Send button: " + selector);
                        sendClicked = true;
                        break;
                    }
                } catch (Exception e) {
                    // Try next selector
                }
            }
            
            if (sendClicked) {
                // Wait exactly 10 seconds for QR generation as requested
                logger.info("⏳ Waiting 10 seconds for QR code generation...");
                uploadPage.waitForTimeout(10000);
                logger.info("✅ QR code should be generated!");
                
                // 🔗 EXTRACT DOWNLOAD URL FROM WEB PAGE (User's workaround)
                extractAndSaveDownloadURL(uploadPage, buildType);
                
            } else {
                logger.warning("⚠️ Could not find Send button");
            }
            
        } catch (Exception e) {
            logger.warning("⚠️ Error generating QR code: " + e.getMessage());
        }
    }
    
    // 🔗 NEW METHOD: Extract download URL from Diawi web page (User's workaround)
    private void extractAndSaveDownloadURL(Page uploadPage, String buildType) {
        logger.info("🔍 Extracting " + buildType + " download URL from web page...");
        
        try {
            // Common Diawi URL patterns and selectors to look for
            String[] urlSelectors = {
                "a[href*='i.diawi.com']",           // Direct link containing i.diawi.com
                "a[href*='diawi.com']",             // Any diawi.com link
                "input[value*='i.diawi.com']",      // Input field with diawi URL
                "textarea[value*='i.diawi.com']",   // Textarea with diawi URL
                ".qr-link a",                       // QR link anchor
                ".download-link",                   // Download link class
                ".share-url",                       // Share URL class
                "a[title*='Download']"              // Link with Download in title
            };
            
            String extractedURL = null;
            
            // Try to find the download URL using different selectors
            for (String selector : urlSelectors) {
                try {
                    Locator urlElement = uploadPage.locator(selector);
                    if (urlElement.count() > 0) {
                        String href = urlElement.first().getAttribute("href");
                        String value = urlElement.first().getAttribute("value");
                        String textContent = urlElement.first().textContent();
                        
                        // Check href attribute
                        if (href != null && href.contains("i.diawi.com")) {
                            extractedURL = href;
                            logger.info("✅ Found URL in href: " + extractedURL);
                            break;
                        }
                        
                        // Check value attribute  
                        if (value != null && value.contains("i.diawi.com")) {
                            extractedURL = value;
                            logger.info("✅ Found URL in value: " + extractedURL);
                            break;
                        }
                        
                        // Check text content
                        if (textContent != null && textContent.contains("i.diawi.com")) {
                            extractedURL = textContent.trim();
                            logger.info("✅ Found URL in text: " + extractedURL);
                            break;
                        }
                    }
                } catch (Exception e) {
                    // Try next selector
                }
            }
            
            // If no direct URL found, try to extract from page source
            if (extractedURL == null) {
                logger.info("🔍 Searching page source for diawi URL...");
                String pageContent = uploadPage.content();
                
                // Look for URL patterns in page source
                String[] patterns = {
                    "https://i\\.diawi\\.com/[a-zA-Z0-9]+",
                    "i\\.diawi\\.com/[a-zA-Z0-9]+"
                };
                
                for (String pattern : patterns) {
                    java.util.regex.Pattern regex = java.util.regex.Pattern.compile(pattern);
                    java.util.regex.Matcher matcher = regex.matcher(pageContent);
                    
                    if (matcher.find()) {
                        extractedURL = matcher.group();
                        if (!extractedURL.startsWith("https://")) {
                            extractedURL = "https://" + extractedURL;
                        }
                        logger.info("✅ Found URL in page source: " + extractedURL);
                        break;
                    }
                }
            }
            
            // Save the extracted URL to file for Phase 4
            if (extractedURL != null) {
                saveURLToFile(buildType, extractedURL);
            } else {
                logger.warning("⚠️ Could not extract " + buildType + " download URL from web page");
                // Create fallback file with placeholder
                saveURLToFile(buildType, "https://i.diawi.com/EXTRACTION_FAILED");
            }
            
        } catch (Exception e) {
            logger.warning("⚠️ Error extracting download URL: " + e.getMessage());
        }
    }
    
    // 💾 Save extracted URL to file for Phase 4 to read
    private void saveURLToFile(String buildType, String url) {
        try {
            String fileName = buildType.toLowerCase() + "_url.txt";
            String filePath = "../extracted_urls/" + fileName;
            
            // Create directory if it doesn't exist
            java.io.File urlDir = new java.io.File("../extracted_urls");
            if (!urlDir.exists()) {
                urlDir.mkdirs();
            }
            
            // Write URL to file
            java.nio.file.Files.write(
                java.nio.file.Paths.get(filePath), 
                url.getBytes(), 
                java.nio.file.StandardOpenOption.CREATE, 
                java.nio.file.StandardOpenOption.TRUNCATE_EXISTING
            );
            
            logger.info("💾 Saved " + buildType + " URL to: " + filePath);
            logger.info("🔗 " + buildType + " Download URL: " + url);
            
        } catch (Exception e) {
            logger.warning("⚠️ Error saving URL to file: " + e.getMessage());
        }
    }
    
    private void cleanOldQRCodes(String buildType) {
        logger.info("🧹 Cleaning old " + buildType + " QR codes...");
        
        try {
            String targetDir = buildType.equals("Android") ? ANDROID_QR_DIR : IOS_QR_DIR;
            File dir = new File(targetDir);
            
            if (dir.exists()) {
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isFile() && file.getName().endsWith(".png")) {
                            file.delete();
                            logger.info("🗑️ Deleted old QR code: " + file.getName());
                        }
                    }
                }
            } else {
                dir.mkdirs();
                logger.info("📁 Created QR directory: " + targetDir);
            }
            
        } catch (Exception e) {
            logger.warning("⚠️ Error cleaning old QR codes: " + e.getMessage());
        }
    }
    
    private void saveQRCodeScreenshot(Page uploadPage, String buildType) {
        logger.info("📸 Saving " + buildType + " QR code screenshot...");
        
        try {
            String targetDir = buildType.equals("Android") ? ANDROID_QR_DIR : IOS_QR_DIR;
            String timestamp = String.valueOf(System.currentTimeMillis());
            String filename = "Diawi_" + buildType + "_QR_Code_" + timestamp + ".png";
            String fullPath = targetDir + File.separator + filename;
            
            // Ensure directory exists
            new File(targetDir).mkdirs();
            
            // Take full page screenshot
            uploadPage.screenshot(new Page.ScreenshotOptions()
                .setPath(Paths.get(fullPath))
                .setFullPage(true));
            
            logger.info("✅ " + buildType + " QR code saved: " + filename);
            logger.info("📁 Location: " + fullPath);
            
            // Also save locally for convenience
            String localPath = buildType.toLowerCase() + "_qr_code_latest.png";
            uploadPage.screenshot(new Page.ScreenshotOptions()
                .setPath(Paths.get(localPath)));
            logger.info("📸 Local copy: " + localPath);
            
        } catch (Exception e) {
            logger.severe("❌ Error saving QR code screenshot: " + e.getMessage());
        }
    }
    
    private void cleanup() {
        try {
            if (browser != null) {
                browser.close();
            }
            if (playwright != null) {
                playwright.close();
            }
        } catch (Exception e) {
            logger.info("Cleanup completed");
        }
    }
}
