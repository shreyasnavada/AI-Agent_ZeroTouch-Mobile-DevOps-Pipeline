const axios = require('axios');
const https = require('https');
const fs = require('fs');
const path = require('path');

// Direct configuration - your Adobe Artifactory credentials
const config = {
    baseUrl: 'https://artifactory.corp.adobe.com',
    apiToken: 'YOUR_API_TOKEN_HERE', // Replace with your actual token
    username: 'YOUR_USERNAME_HERE'   // Replace with your username
};

// Configuration for Adobe Artifactory paths
const REPOSITORY = 'npm-cprime-dev';
const ARCHIVE_BASE_PATH = 'mobileAppBuilds/staging_m44_develop/Release/archive/';
const OUTPUT_BASE = 'C:\\Cursor_QA\\adobe-mcp-servers-main\\Mobile_App_Builds';

class LatestBuildDownloader {
    constructor(config) {
        this.client = axios.create({
            baseURL: config.baseUrl,
            headers: {
                'Authorization': `Bearer ${config.apiToken}`,
                'Content-Type': 'application/json',
            },
            timeout: 30000,
            // Fix SSL certificate issues
            httpsAgent: new https.Agent({
                rejectUnauthorized: false,
                secureProtocol: 'TLS_method'
            }),
            // Additional SSL bypass options
            maxRedirects: 5,
            validateStatus: function (status) {
                return status >= 200 && status < 300;
            }
        });
    }

    async listArchiveBuilds() {
        try {
            const url = `/artifactory/api/storage/${REPOSITORY}/${ARCHIVE_BASE_PATH}`;
            console.log(`📋 Checking archive for latest builds: ${ARCHIVE_BASE_PATH}`);

            const response = await this.client.get(url);
            return response.data;
        } catch (error) {
            console.error(`❌ Error listing archive builds:`, error.response?.data?.message || error.message);
            return null;
        }
    }

    async findLatestBuildNumber() {
        const archiveData = await this.listArchiveBuilds();
        
        if (!archiveData || !archiveData.children) {
            console.log('❌ No builds found in archive');
            return null;
        }

        console.log('📂 Available builds in archive:');
        const buildNumbers = [];
        
        for (const child of archiveData.children) {
            if (child.folder && child.uri) {
                // Extract build number from folder name
                const buildMatch = child.uri.match(/\/(\d+)\/$/);
                if (buildMatch) {
                    const buildNumber = parseInt(buildMatch[1]);
                    buildNumbers.push(buildNumber);
                    console.log(`   📁 Build ${buildNumber}`);
                }
            }
        }

        if (buildNumbers.length === 0) {
            console.log('❌ No valid build numbers found');
            return null;
        }

        // Find the highest build number
        const latestBuildNumber = Math.max(...buildNumbers);
        console.log(`\n🎯 Latest build identified: ${latestBuildNumber}`);
        
        // Output clean version for batch file parsing
        console.log(`Latest build identified: ${latestBuildNumber}`);
        
        return latestBuildNumber;
    }

    async listArtifacts(repository, folderPath = '') {
        try {
            const url = `/artifactory/api/storage/${repository}/${folderPath}`;
            const response = await this.client.get(url);
            return response.data;
        } catch (error) {
            console.error(`❌ Error listing artifacts in ${folderPath}:`, error.response?.data?.message || error.message);
            return null;
        }
    }

    async downloadFile(repository, artifactPath, outputPath) {
        try {
            const url = `/artifactory/${repository}/${artifactPath}`;
            console.log(`⬇️ Downloading: ${path.basename(artifactPath)}`);
            
            const response = await this.client.get(url, {
                responseType: 'stream'
            });

            const writer = fs.createWriteStream(outputPath);
            response.data.pipe(writer);

            return new Promise((resolve, reject) => {
                writer.on('finish', () => {
                    console.log(`✅ Downloaded: ${path.basename(outputPath)}`);
                    console.log(`   📁 Saved as: ${path.basename(outputPath)}`);
                    resolve();
                });
                writer.on('error', reject);
            });

        } catch (error) {
            console.error(`❌ Error downloading ${artifactPath}:`, error.response?.data?.message || error.message);
            throw error;
        }
    }

    async downloadLatestBuilds() {
        console.log('🚀 Starting Latest Build Download from Adobe Artifactory...');
        console.log('');
        console.log(`📍 Target: ${config.baseUrl}`);
        console.log(`👤 User: ${config.username}`);
        console.log(`📦 Repository: ${REPOSITORY}`);
        console.log(`📂 Archive Path: ${ARCHIVE_BASE_PATH}`);
        console.log('');

        try {
            // Find latest build number
            const latestBuildNumber = await this.findLatestBuildNumber();
            
            if (!latestBuildNumber) {
                throw new Error('Could not determine latest build number');
            }

            const latestBuildPath = `${ARCHIVE_BASE_PATH}${latestBuildNumber}/`;
            console.log(`📂 Accessing latest build: ${latestBuildPath}`);
            console.log('');

            // Create output directories
            const androidOutput = path.join(OUTPUT_BASE, 'Android');
            const iosOutput = path.join(OUTPUT_BASE, 'iOS');
            
            if (!fs.existsSync(androidOutput)) {
                fs.mkdirSync(androidOutput, { recursive: true });
            }
            if (!fs.existsSync(iosOutput)) {
                fs.mkdirSync(iosOutput, { recursive: true });
            }

            // Download iOS builds
            await this.downloadIOSBuilds(latestBuildNumber, latestBuildPath, iosOutput);
            
            // Download Android builds  
            await this.downloadAndroidBuilds(latestBuildNumber, latestBuildPath, androidOutput);

            console.log('🎉 Latest build download process completed!');
            console.log(`📂 iOS builds saved to: ${iosOutput}`);
            console.log(`📂 Android builds saved to: ${androidOutput}`);
            console.log('🏷️ Files renamed with Vader naming convention:');
            console.log(`   📱 iOS: Vader-iOS-${latestBuildNumber}`);
            console.log(`   🤖 Android: Vader-Android-${latestBuildNumber}`);
            console.log('🗑️ Old files automatically replaced');

        } catch (error) {
            console.error('❌ Download process failed:', error.message);
            process.exit(1);
        }
    }

    async downloadIOSBuilds(buildNumber, buildPath, outputDir) {
        console.log('📱 Checking iOS folder in latest build...');
        const iosPath = `${buildPath}ios/`;
        const iosData = await this.listArtifacts(REPOSITORY, iosPath);
        
        if (!iosData || !iosData.children) {
            console.log('❌ No iOS builds found');
            return;
        }

        console.log(`📋 Listing: ${iosPath}`);
        console.log(`   Found ${iosData.children.length} items in iOS folder:`);
        
        const ipaFiles = iosData.children.filter(child => 
            !child.folder && child.uri.endsWith('.ipa')
        );

        for (const child of iosData.children) {
            console.log(`   📄 ${child.uri}`);
        }
        console.log('');

        if (ipaFiles.length === 0) {
            console.log('❌ No IPA files found in iOS folder');
            return;
        }

        console.log(`📱 Downloading ${ipaFiles.length} iOS builds:`);
        
        for (const ipaFile of ipaFiles) {
            const sourceFile = ipaFile.uri.substring(1); // Remove leading slash
            const sourcePath = `${iosPath}${sourceFile}`;
            const outputFileName = `Vader-iOS-${buildNumber}.ipa`;
            const outputPath = path.join(outputDir, outputFileName);
            
            // Remove old file if exists
            if (fs.existsSync(outputPath)) {
                fs.unlinkSync(outputPath);
                console.log(`🗑️ Removed old iOS file: ${outputFileName}`);
            }
            
            await this.downloadFile(REPOSITORY, sourcePath, outputPath);
        }
    }

    async downloadAndroidBuilds(buildNumber, buildPath, outputDir) {
        console.log('🤖 Checking Android folder in latest build...');
        const androidPath = `${buildPath}android/`;
        const androidData = await this.listArtifacts(REPOSITORY, androidPath);
        
        if (!androidData || !androidData.children) {
            console.log('❌ No Android builds found');
            return;
        }

        console.log(`📋 Listing: ${androidPath}`);
        console.log(`   Found ${androidData.children.length} items in Android folder:`);
        
        const apkFiles = androidData.children.filter(child => 
            !child.folder && child.uri.endsWith('.apk')
        );

        for (const child of androidData.children) {
            console.log(`   📄 ${child.uri}`);
        }
        console.log('');

        if (apkFiles.length === 0) {
            console.log('❌ No APK files found in Android folder');
            return;
        }

        console.log(`🤖 Downloading ${apkFiles.length} Android builds:`);
        
        for (const apkFile of apkFiles) {
            const sourceFile = apkFile.uri.substring(1); // Remove leading slash
            const sourcePath = `${androidPath}${sourceFile}`;
            const outputFileName = `Vader-Android-${buildNumber}.apk`;
            const outputPath = path.join(outputDir, outputFileName);
            
            // Remove old file if exists
            if (fs.existsSync(outputPath)) {
                fs.unlinkSync(outputPath);
                console.log(`🗑️ Removed old Android file: ${outputFileName}`);
            }
            
            await this.downloadFile(REPOSITORY, sourcePath, outputPath);
        }
    }
}

// Create downloader instance and start download
const downloader = new LatestBuildDownloader(config);
downloader.downloadLatestBuilds();